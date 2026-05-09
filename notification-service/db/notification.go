package db

import (
	"context"
	"encoding/json"
	"time"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Notification struct {
	ID        string    `json:"id,omitempty"`
	UserID    string    `json:"userId"`
	Type      string    `json:"type"`
	Title     string    `json:"title"`
	Body      string    `json:"body,omitempty"`
	Link      string    `json:"link,omitempty"`
	IsRead    bool      `json:"isRead"`
	CreatedAt time.Time `json:"createdAt,omitempty"`
}

func (n Notification) ToJSON() string {
	b, _ := json.Marshal(n)
	return string(b)
}

type Storer interface {
	Save(ctx context.Context, n Notification) (Notification, error)
	BatchInsert(ctx context.Context, notifications []Notification) error
	List(ctx context.Context, userID string, limit, offset int) ([]Notification, error)
	MarkRead(ctx context.Context, id, userID string) error
	MarkAllRead(ctx context.Context, userID string) error
}

type Store struct {
	pool *pgxpool.Pool
}

func New(pool *pgxpool.Pool) *Store {
	return &Store{pool: pool}
}

func (s *Store) Migrate(ctx context.Context) error {
	_, err := s.pool.Exec(ctx, `
		CREATE TABLE IF NOT EXISTS notifications (
			id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
			user_id    UUID NOT NULL,
			type       VARCHAR(50) NOT NULL,
			title      VARCHAR(255) NOT NULL,
			body       TEXT,
			link       VARCHAR(500),
			is_read    BOOLEAN NOT NULL DEFAULT false,
			created_at TIMESTAMP NOT NULL DEFAULT now()
		);
		CREATE INDEX IF NOT EXISTS idx_notifications_user_unread
			ON notifications (user_id, is_read, created_at DESC);
	`)
	return err
}

// Save inserts one notification and returns it with the DB-generated ID.
func (s *Store) Save(ctx context.Context, n Notification) (Notification, error) {
	err := s.pool.QueryRow(ctx,
		`INSERT INTO notifications (user_id, type, title, body, link)
		 VALUES ($1, $2, $3, $4, $5)
		 RETURNING id, created_at`,
		n.UserID, n.Type, n.Title, n.Body, n.Link,
	).Scan(&n.ID, &n.CreatedAt)
	return n, err
}

// BatchInsert inserts many notifications in a single round-trip (no IDs returned).
func (s *Store) BatchInsert(ctx context.Context, notifications []Notification) error {
	if len(notifications) == 0 {
		return nil
	}
	batch := &pgx.Batch{}
	for _, n := range notifications {
		batch.Queue(
			`INSERT INTO notifications (user_id, type, title, body, link) VALUES ($1, $2, $3, $4, $5)`,
			n.UserID, n.Type, n.Title, n.Body, n.Link,
		)
	}
	return s.pool.SendBatch(ctx, batch).Close()
}

func (s *Store) List(ctx context.Context, userID string, limit, offset int) ([]Notification, error) {
	rows, err := s.pool.Query(ctx,
		`SELECT id, user_id, type, title,
		        COALESCE(body, ''), COALESCE(link, ''), is_read, created_at
		 FROM notifications
		 WHERE user_id = $1
		 ORDER BY created_at DESC
		 LIMIT $2 OFFSET $3`,
		userID, limit, offset,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []Notification
	for rows.Next() {
		var n Notification
		if err := rows.Scan(&n.ID, &n.UserID, &n.Type, &n.Title, &n.Body, &n.Link, &n.IsRead, &n.CreatedAt); err != nil {
			return nil, err
		}
		result = append(result, n)
	}
	return result, rows.Err()
}

func (s *Store) MarkRead(ctx context.Context, id, userID string) error {
	_, err := s.pool.Exec(ctx,
		`UPDATE notifications SET is_read = true WHERE id = $1 AND user_id = $2`,
		id, userID,
	)
	return err
}

func (s *Store) MarkAllRead(ctx context.Context, userID string) error {
	_, err := s.pool.Exec(ctx,
		`UPDATE notifications SET is_read = true WHERE user_id = $1`,
		userID,
	)
	return err
}
