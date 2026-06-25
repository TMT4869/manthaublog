package middleware

import (
	"crypto/rsa"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"math/big"
	"net/http"
	"strings"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
)

const (
	errInvalidToken = "invalid token"
)

type Claims struct {
	UserID string `json:"user_id"`
	Role   string `json:"role"`
	jwt.RegisteredClaims
}

type JWKCache struct {
	jwksURI string
	client  *http.Client
	mu      sync.RWMutex
	keys    map[string]*rsa.PublicKey
}

type jwkSet struct {
	Keys []jwk `json:"keys"`
}

type jwk struct {
	Kty string `json:"kty"`
	Kid string `json:"kid"`
	Use string `json:"use"`
	Alg string `json:"alg"`
	N   string `json:"n"`
	E   string `json:"e"`
}

func NewJWKCache(jwksURI string) *JWKCache {
	return &JWKCache{
		jwksURI: jwksURI,
		client: &http.Client{
			Timeout: 3 * time.Second,
		},
		keys: make(map[string]*rsa.PublicKey),
	}
}

func JWTMiddleware(jwkCache *JWKCache) gin.HandlerFunc {
	return func(c *gin.Context) {
		tokenStr := extractToken(c)
		if tokenStr == "" {
			c.AbortWithStatusJSON(401, gin.H{"error": "missing token"})
			return
		}
		claims, err := validateJWT(tokenStr, jwkCache)
		if err != nil {
			c.AbortWithStatusJSON(401, gin.H{"error": errInvalidToken})
			return
		}
		if claims.UserID == "" {
			claims.UserID = claims.Subject
		}
		if claims.UserID == "" {
			c.AbortWithStatusJSON(401, gin.H{"error": errInvalidToken})
			return
		}
		c.Request.Header.Set("X-User-ID", claims.UserID)
		c.Request.Header.Set("X-User-Role", claims.Role)
		c.Set("role", claims.Role)
		c.Next()
	}
}

func extractToken(c *gin.Context) string {
	auth := c.GetHeader("Authorization")
	if after, ok :=strings.CutPrefix(auth, "Bearer "); ok  {
		return after
	}
	return ""
}

func validateJWT(tokenStr string, jwkCache *JWKCache) (*Claims, error) {
	claims := &Claims{}
	parser := jwt.NewParser(jwt.WithValidMethods([]string{jwt.SigningMethodRS256.Alg()}))
	token, err := parser.ParseWithClaims(tokenStr, claims, jwkCache.keyFunc)
	if err != nil {
		return nil, err
	}
	if !token.Valid {
		return nil, errors.New(errInvalidToken)
	}
	return claims, nil
}

func (c *JWKCache) keyFunc(token *jwt.Token) (interface{}, error) {
	if token.Method.Alg() != jwt.SigningMethodRS256.Alg() {
		return nil, fmt.Errorf("unexpected signing method: %s", token.Method.Alg())
	}

	kid, ok := token.Header["kid"].(string)
	if !ok || kid == "" {
		return nil, errors.New("jwt header is missing kid")
	}

	if key, ok := c.cachedKey(kid); ok {
		return key, nil
	}

	c.mu.Lock()
	defer c.mu.Unlock()
	if key, ok := c.keys[kid]; ok {
		return key, nil
	}

	if err := c.refreshLocked(); err != nil {
		return nil, err
	}
	key, ok := c.keys[kid]
	if !ok {
		return nil, fmt.Errorf("no jwk found for kid: %s", kid)
	}
	return key, nil
}

func (c *JWKCache) cachedKey(kid string) (*rsa.PublicKey, bool) {
	c.mu.RLock()
	defer c.mu.RUnlock()
	key, ok := c.keys[kid]
	return key, ok
}

func (c *JWKCache) refreshLocked() error {
	req, err := http.NewRequest(http.MethodGet, c.jwksURI, nil)
	if err != nil {
		return err
	}
	resp, err := c.client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode < http.StatusOK || resp.StatusCode >= http.StatusMultipleChoices {
		return fmt.Errorf("jwks endpoint returned http %d", resp.StatusCode)
	}

	var set jwkSet
	if err := json.NewDecoder(resp.Body).Decode(&set); err != nil {
		return err
	}

	for _, candidate := range set.Keys {
		if candidate.Kty != "RSA" || candidate.Kid == "" || candidate.N == "" || candidate.E == "" {
			continue
		}
		key, err := rsaPublicKey(candidate)
		if err != nil {
			continue
		}
		c.keys[candidate.Kid] = key
	}
	return nil
}

func rsaPublicKey(key jwk) (*rsa.PublicKey, error) {
	modulus, err := base64URLBigInt(key.N)
	if err != nil {
		return nil, err
	}
	exponent, err := base64URLBigInt(key.E)
	if err != nil {
		return nil, err
	}
	if !exponent.IsInt64() || exponent.Int64() > int64(^uint(0)>>1) {
		return nil, errors.New("invalid rsa exponent")
	}
	return &rsa.PublicKey{
		N: modulus,
		E: int(exponent.Int64()),
	}, nil
}

func base64URLBigInt(value string) (*big.Int, error) {
	decoded, err := base64.RawURLEncoding.DecodeString(value)
	if err != nil {
		decoded, err = base64.URLEncoding.DecodeString(value)
	}
	if err != nil {
		return nil, err
	}
	return new(big.Int).SetBytes(decoded), nil
}
