package client

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

type UserClient struct {
	baseURL    string
	httpClient *http.Client
}

func NewUserClient(baseURL string) *UserClient {
	return &UserClient{
		baseURL:    baseURL,
		httpClient: &http.Client{Timeout: 5 * time.Second},
	}
}

// GetFollowersPaged returns a page of follower user IDs for authorID.
// Returns (ids, hasNextPage). Called by auth-service internal API.
func (c *UserClient) GetFollowersPaged(authorID string, page, pageSize int) ([]string, bool) {
	url := fmt.Sprintf("%s/internal/users/%s/followers?page=%d&size=%d", c.baseURL, authorID, page, pageSize)
	resp, err := c.httpClient.Get(url)
	if err != nil || resp.StatusCode != http.StatusOK {
		return nil, false
	}
	defer resp.Body.Close()

	var result struct {
		UserIDs []string `json:"userIds"`
		HasNext bool     `json:"hasNext"`
	}
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, false
	}
	return result.UserIDs, result.HasNext
}

// GetAdmins returns all admin user IDs. Called by auth-service internal API.
func (c *UserClient) GetAdmins() []string {
	url := fmt.Sprintf("%s/internal/users/admins", c.baseURL)
	resp, err := c.httpClient.Get(url)
	if err != nil || resp.StatusCode != http.StatusOK {
		return nil
	}
	defer resp.Body.Close()

	var result struct {
		UserIDs []string `json:"userIds"`
	}
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil
	}
	return result.UserIDs
}
