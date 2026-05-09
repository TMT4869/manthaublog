package storage

import (
	"context"
	"fmt"
	"io"
	"path/filepath"
	"strings"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

type MinioStorage struct {
	client   *minio.Client
	bucket   string
	endpoint string
	useSSL   bool
}

func NewMinio(endpoint, accessKey, secretKey, bucket string, useSSL bool) (*MinioStorage, error) {
	client, err := minio.New(endpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(accessKey, secretKey, ""),
		Secure: useSSL,
	})
	if err != nil {
		return nil, err
	}

	ctx := context.Background()
	exists, err := client.BucketExists(ctx, bucket)
	if err != nil {
		return nil, err
	}
	if !exists {
		if err := client.MakeBucket(ctx, bucket, minio.MakeBucketOptions{}); err != nil {
			return nil, err
		}
		// Allow public read so URLs are directly accessible without presigning
		policy := fmt.Sprintf(`{
			"Version":"2012-10-17",
			"Statement":[{
				"Effect":"Allow",
				"Principal":{"AWS":["*"]},
				"Action":["s3:GetBucketLocation","s3:ListBucket"],
				"Resource":["arn:aws:s3:::%s"]
			},{
				"Effect":"Allow",
				"Principal":{"AWS":["*"]},
				"Action":["s3:GetObject"],
				"Resource":["arn:aws:s3:::%s/*"]
			}]
		}`, bucket, bucket)
		if err := client.SetBucketPolicy(ctx, bucket, policy); err != nil {
			return nil, err
		}
	}

	return &MinioStorage{client: client, bucket: bucket, endpoint: endpoint, useSSL: useSSL}, nil
}

func (s *MinioStorage) Save(src io.Reader, userID, filename string, size int64) error {
	_, err := s.client.PutObject(
		context.Background(),
		s.bucket,
		userID+"/"+filename,
		src, size,
		minio.PutObjectOptions{ContentType: "application/octet-stream"},
	)
	return err
}

func (s *MinioStorage) Get(userID, filename string) (io.ReadCloser, error) {
	return s.client.GetObject(
		context.Background(),
		s.bucket,
		userID+"/"+filename,
		minio.GetObjectOptions{},
	)
}

func (s *MinioStorage) Delete(userID, filename string) error {
	return s.client.RemoveObject(
		context.Background(),
		s.bucket,
		userID+"/"+filename,
		minio.RemoveObjectOptions{},
	)
}

func (s *MinioStorage) PublicURL(userID, filename, variant string) string {
	scheme := "http"
	if s.useSSL {
		scheme = "https"
	}
	if variant == "original" {
		return fmt.Sprintf("%s://%s/%s/%s/%s", scheme, s.endpoint, s.bucket, userID, filename)
	}
	base := strings.TrimSuffix(filename, filepath.Ext(filename))
	return fmt.Sprintf("%s://%s/%s/%s/%s_%s.jpg", scheme, s.endpoint, s.bucket, userID, variant, base)
}
