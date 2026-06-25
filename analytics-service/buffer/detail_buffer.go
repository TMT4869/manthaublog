package buffer

import (
	"sync"
	"time"
)

type ViewDetail struct {
	PostID          string
	ViewerID        *string
	IPHash          *string
	ReadTimeSeconds int
	Referrer        *string
	ViewedAt        time.Time
}

type DetailBuffer struct {
	mu      sync.Mutex
	details []ViewDetail
}

func NewDetailBuffer() *DetailBuffer {
	return &DetailBuffer{}
}

func (b *DetailBuffer) Append(d ViewDetail) {
	b.mu.Lock()
	b.details = append(b.details, d)
	b.mu.Unlock()
}

func (b *DetailBuffer) Drain() []ViewDetail {
	b.mu.Lock()
	defer b.mu.Unlock()
	out := b.details
	b.details = nil
	return out
}
