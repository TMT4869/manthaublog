package storage

import (
	"io"
	"os"
	"path/filepath"
	"strings"
)

// Storage is the interface both LocalStorage and MinioStorage implement.
// Callers never deal with absolute paths — only userID + filename.
type Storage interface {
	Save(src io.Reader, userID, filename string, size int64) error
	Get(userID, filename string) (io.ReadCloser, error)
	Delete(userID, filename string) error
	PublicURL(userID, filename, variant string) string
}

type LocalStorage struct {
	BasePath string
}

func NewLocal(basePath string) *LocalStorage {
	return &LocalStorage{BasePath: basePath}
}

func (s *LocalStorage) Save(src io.Reader, userID, filename string, _ int64) error {
	path := filepath.Join(s.BasePath, userID, filename)
	if err := os.MkdirAll(filepath.Dir(path), 0755); err != nil {
		return err
	}
	f, err := os.Create(path)
	if err != nil {
		return err
	}
	defer f.Close()
	_, err = io.Copy(f, src)
	return err
}

func (s *LocalStorage) Get(userID, filename string) (io.ReadCloser, error) {
	return os.Open(filepath.Join(s.BasePath, userID, filename))
}

func (s *LocalStorage) Delete(userID, filename string) error {
	return os.Remove(filepath.Join(s.BasePath, userID, filename))
}

func (s *LocalStorage) PublicURL(userID, filename, variant string) string {
	if variant == "original" {
		return "/media/" + userID + "/" + filename
	}
	base := strings.TrimSuffix(filename, filepath.Ext(filename))
	return "/media/" + userID + "/" + variant + "_" + base + ".jpg"
}
