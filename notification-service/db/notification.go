package db

import (
	"context"
	"encoding/json"
	"time"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
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
	col *mongo.Collection
}

func New(col *mongo.Collection) *Store {
	return &Store{col: col}
}

func (s *Store) Migrate(ctx context.Context) error {
	_, err := s.col.Indexes().CreateOne(ctx, mongo.IndexModel{
		Keys: bson.D{
			{Key: "user_id", Value: 1},
			{Key: "is_read", Value: 1},
			{Key: "created_at", Value: -1},
		},
	})
	return err
}

type notifDoc struct {
	ID        bson.ObjectID `bson:"_id,omitempty"`
	UserID    string        `bson:"user_id"`
	Type      string        `bson:"type"`
	Title     string        `bson:"title"`
	Body      string        `bson:"body"`
	Link      string        `bson:"link"`
	IsRead    bool          `bson:"is_read"`
	CreatedAt time.Time     `bson:"created_at"`
}

func (d notifDoc) toNotification() Notification {
	return Notification{
		ID:        d.ID.Hex(),
		UserID:    d.UserID,
		Type:      d.Type,
		Title:     d.Title,
		Body:      d.Body,
		Link:      d.Link,
		IsRead:    d.IsRead,
		CreatedAt: d.CreatedAt,
	}
}

func (s *Store) Save(ctx context.Context, n Notification) (Notification, error) {
	doc := notifDoc{
		UserID:    n.UserID,
		Type:      n.Type,
		Title:     n.Title,
		Body:      n.Body,
		Link:      n.Link,
		IsRead:    false,
		CreatedAt: time.Now().UTC(),
	}
	res, err := s.col.InsertOne(ctx, doc)
	if err != nil {
		return n, err
	}
	n.ID = res.InsertedID.(bson.ObjectID).Hex()
	n.CreatedAt = doc.CreatedAt
	return n, nil
}

func (s *Store) BatchInsert(ctx context.Context, notifications []Notification) error {
	if len(notifications) == 0 {
		return nil
	}
	now := time.Now().UTC()
	docs := make([]any, len(notifications))
	for i, n := range notifications {
		docs[i] = notifDoc{
			UserID:    n.UserID,
			Type:      n.Type,
			Title:     n.Title,
			Body:      n.Body,
			Link:      n.Link,
			IsRead:    false,
			CreatedAt: now,
		}
	}
	_, err := s.col.InsertMany(ctx, docs)
	return err
}

func (s *Store) List(ctx context.Context, userID string, limit, offset int) ([]Notification, error) {
	opts := options.Find().
		SetSort(bson.D{{Key: "created_at", Value: -1}}).
		SetLimit(int64(limit)).
		SetSkip(int64(offset))

	cur, err := s.col.Find(ctx, bson.M{"user_id": userID}, opts)
	if err != nil {
		return nil, err
	}
	defer cur.Close(ctx)

	var result []Notification
	for cur.Next(ctx) {
		var doc notifDoc
		if err := cur.Decode(&doc); err != nil {
			return nil, err
		}
		result = append(result, doc.toNotification())
	}
	return result, cur.Err()
}

func (s *Store) MarkRead(ctx context.Context, id, userID string) error {
	oid, err := bson.ObjectIDFromHex(id)
	if err != nil {
		return err
	}
	_, err = s.col.UpdateOne(ctx,
		bson.M{"_id": oid, "user_id": userID},
		bson.M{"$set": bson.M{"is_read": true}},
	)
	return err
}

func (s *Store) MarkAllRead(ctx context.Context, userID string) error {
	_, err := s.col.UpdateMany(ctx,
		bson.M{"user_id": userID},
		bson.M{"$set": bson.M{"is_read": true}},
	)
	return err
}
