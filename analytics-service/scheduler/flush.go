package scheduler

import (
	"analytics-service/buffer"
	"analytics-service/db"
	"analytics-service/publisher"
	"context"
	"strings"

	"github.com/robfig/cron/v3"
	"go.uber.org/zap"
)

type Scheduler struct {
	viewBuf   *buffer.ViewBuffer
	detailBuf *buffer.DetailBuffer
	db        *db.Analytics
	pub       *publisher.Publisher
	logger    *zap.Logger
}

func New(viewBuf *buffer.ViewBuffer, detailBuf *buffer.DetailBuffer, store *db.Analytics, pub *publisher.Publisher, logger *zap.Logger) *Scheduler {
	return &Scheduler{viewBuf: viewBuf, detailBuf: detailBuf, db: store, pub: pub, logger: logger}
}

func (s *Scheduler) Start() {
	c := cron.New()
	c.AddFunc("@every 1m", s.flush)
	c.Start()
}

func (s *Scheduler) flush() {
	ctx := context.Background()

	data, err := s.viewBuf.FlushAll()
	if err != nil {
		s.logger.Error("flush view buffer failed", zap.Error(err))
		return
	}
	if len(data) == 0 {
		return
	}
	rows, postIDs := s.parseViewData(data)

	if err := s.db.BatchUpsertStats(ctx, rows); err != nil {
		s.logger.Error("batch upsert stats failed", zap.Error(err))
		return
	}

	details := s.detailBuf.Drain()
	if len(details) > 0 {
		if err := s.insertDetails(ctx, details); err != nil {
			s.logger.Error("batch insert views failed", zap.Error(err))
		}
	}

	s.publishUpdates(ctx, postIDs)

	s.logger.Info("flush completed", zap.Int("posts", len(postIDs)), zap.Int("details", len(details)))
}

func (s *Scheduler) parseViewData(data map[string]int) ([]db.StatRow, map[string]struct{}) {
	var rows []db.StatRow
	postIDs := make(map[string]struct{})
	for key, count := range data {
		// key format: analytics:views:{postId}:{date}
		parts := strings.Split(key, ":")
		if len(parts) != 4 {
			s.logger.Warn("unexpected key format", zap.String("key", key))
			continue
		}
		postID, date := parts[2], parts[3]
		rows = append(rows, db.StatRow{PostID: postID, Date: date, Views: count})
		postIDs[postID] = struct{}{}
	}
	return rows, postIDs
}

func (s *Scheduler) insertDetails(ctx context.Context, details []buffer.ViewDetail) error {
	dbDetails := make([]db.ViewDetail, len(details))
	for i, d := range details {
		dbDetails[i] = db.ViewDetail{
			PostID:          d.PostID,
			ViewerID:        d.ViewerID,
			IPHash:          d.IPHash,
			ReadTimeSeconds: d.ReadTimeSeconds,
			Referrer:        d.Referrer,
			ViewedAt:        d.ViewedAt,
		}
	}
	return s.db.BatchInsertViews(ctx, dbDetails)
}

func (s *Scheduler) publishUpdates(ctx context.Context, postIDs map[string]struct{}) {
	for postID := range postIDs {
		total, err := s.db.GetTotalViews(ctx, postID)
		if err != nil {
			s.logger.Error("get total views failed", zap.String("post_id", postID), zap.Error(err))
			continue
		}
		if err := s.pub.PublishViewCountUpdated(ctx, publisher.ViewCountUpdatedEvent{
			PostID:    postID,
			ViewCount: total,
		}); err != nil {
			s.logger.Error("publish view.count.updated failed", zap.String("post_id", postID), zap.Error(err))
		}
	}
}
