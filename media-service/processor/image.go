package processor

import (
	"bytes"
	"image"
	"path/filepath"
	"strings"

	"media-service/storage"

	"github.com/disintegration/imaging"
	_ "golang.org/x/image/webp"
)

type variant struct {
	name   string
	width  int
	height int // 0 = keep aspect ratio
}

var variants = []variant{
	{"thumbnail", 200, 200},
	{"medium", 800, 0},
	{"large", 1600, 0},
}

type Processor struct {
	storage storage.Storage
}

func New(s storage.Storage) *Processor {
	return &Processor{storage: s}
}

// Process downloads the original from storage, resizes into 3 variants, and re-uploads as JPEG.
func (p *Processor) Process(userID, filename string) error {
	rc, err := p.storage.Get(userID, filename)
	if err != nil {
		return err
	}
	defer rc.Close()

	src, err := imaging.Decode(rc)
	if err != nil {
		return err
	}

	base := strings.TrimSuffix(filename, filepath.Ext(filename))

	for _, v := range variants {
		var resized image.Image
		if v.height == 0 {
			resized = imaging.Resize(src, v.width, 0, imaging.Lanczos)
		} else {
			resized = imaging.Fill(src, v.width, v.height, imaging.Center, imaging.Lanczos)
		}

		var buf bytes.Buffer
		if err := imaging.Encode(&buf, resized, imaging.JPEG, imaging.JPEGQuality(85)); err != nil {
			return err
		}

		variantName := v.name + "_" + base + ".jpg"
		data := buf.Bytes()
		if err := p.storage.Save(bytes.NewReader(data), userID, variantName, int64(len(data))); err != nil {
			return err
		}
	}
	return nil
}
