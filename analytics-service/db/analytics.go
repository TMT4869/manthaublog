package db

import (
	"context"
	"time"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type StatRow struct {
	PostID string
	Date   string
	Views  int64
}

type ViewDetail struct {
	PostID          string
	ViewerID        *string
	IPHash          *string
	ReadTimeSeconds int
	Referrer        *string
	ViewedAt        time.Time
}

type PostStats struct {
	PostID             string `json:"post_id"`
	TotalViews         int64  `json:"total_views"`
	UniqueViews        int64  `json:"unique_views"`
	AvgReadTimeSeconds int64  `json:"avg_read_time_seconds"`
}

type DailyStat struct {
	Date               string `json:"date"`
	Views              int    `json:"views"`
	UniqueViews        int    `json:"unique_views"`
	AvgReadTimeSeconds int    `json:"avg_read_time_seconds"`
}

type Analytics struct {
	pool *pgxpool.Pool
}

func New(pool *pgxpool.Pool) *Analytics {
	return &Analytics{pool: pool}
}

func (a *Analytics) BatchUpsertStats(ctx context.Context, rows []StatRow) error {
	query := `
		INSERT INTO post_stats_daily (post_id, date, views)
		VALUES ($1, $2, $3)
		ON CONFLICT (post_id, date)
		DO UPDATE SET views = post_stats_daily.views + EXCLUDED.views
	`
	batch := &pgx.Batch{}
	for _, row := range rows {
		batch.Queue(query, row.PostID, row.Date, row.Views)
	}
	return a.pool.SendBatch(ctx, batch).Close()
}

func (a *Analytics) BatchInsertViews(ctx context.Context, details []ViewDetail) error {
	if len(details) == 0 {
		return nil
	}
	query := `
		INSERT INTO post_views (post_id, viewer_id, ip_hash, read_time_seconds, referrer, viewed_at)
		VALUES ($1, $2, $3, $4, $5, $6)
	`
	batch := &pgx.Batch{}
	for _, d := range details {
		batch.Queue(query, d.PostID, d.ViewerID, d.IPHash, d.ReadTimeSeconds, d.Referrer, d.ViewedAt)
	}
	return a.pool.SendBatch(ctx, batch).Close()
}

func (a *Analytics) GetTotalViews(ctx context.Context, postID string) (int64, error) {
	var total int64
	err := a.pool.QueryRow(ctx,
		`SELECT COALESCE(SUM(views), 0) FROM post_stats_daily WHERE post_id = $1`,
		postID,
	).Scan(&total)
	return total, err
}

func (a *Analytics) GetStats(ctx context.Context, postID string) (*PostStats, error) {
	stats := &PostStats{PostID: postID}
	err := a.pool.QueryRow(ctx, `
		SELECT
			COALESCE(SUM(views), 0),
			COALESCE(SUM(unique_views), 0),
			COALESCE(AVG(avg_read_time_seconds)::BIGINT, 0)
		FROM post_stats_daily
		WHERE post_id = $1
	`, postID).Scan(&stats.TotalViews, &stats.UniqueViews, &stats.AvgReadTimeSeconds)
	return stats, err
}

func (a *Analytics) GetDailyStats(ctx context.Context, postID string, days int) ([]DailyStat, error) {
	rows, err := a.pool.Query(ctx, `
		SELECT date::text, views, unique_views, avg_read_time_seconds
		FROM post_stats_daily
		WHERE post_id = $1
		ORDER BY date DESC
		LIMIT $2
	`, postID, days)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []DailyStat
	for rows.Next() {
		var s DailyStat
		if err := rows.Scan(&s.Date, &s.Views, &s.UniqueViews, &s.AvgReadTimeSeconds); err != nil {
			return nil, err
		}
		result = append(result, s)
	}
	return result, rows.Err()
}

func (a *Analytics) AppendReadTime(ctx context.Context, postID string, seconds int) error {
	_, err := a.pool.Exec(ctx,
		`INSERT INTO post_views (post_id, read_time_seconds, viewed_at) VALUES ($1, $2, NOW())`,
		postID, seconds,
	)
	return err
}
