package fetcher

import (
	"io"
	"log"
	"net/http"
	"os"
	"sync"
	"time"

	"github.com/bytedance/sonic"
)

type MojangManifest struct {
	Versions []struct {
		ID string `json:"id"`
	} `json:"versions"`
}

type MCFetcher struct {
	validVersions   map[string]bool
	orderedVersions []string
	mu              sync.RWMutex
}

func NewMCFetcher() *MCFetcher {
	f := &MCFetcher{
		validVersions: make(map[string]bool),
	}
	return f
}

const cacheFile = "mc_versions.json"

func (f *MCFetcher) Start() {
	f.updateMCVersions()
	ticker := time.NewTicker(90 * time.Hour)
	for range ticker.C {
		f.fetchAndCache()
	}
}

func (f *MCFetcher) updateMCVersions() {
	info, err := os.Stat(cacheFile)
	if err == nil {
		if time.Since(info.ModTime()) < 90*time.Hour {
			log.Println("Loading MC versions from local cache...")
			if err := f.loadFromCache(); err == nil {
				return
			}
			log.Println("Failed to load from cache, fetching again:", err)
		}
	}
	f.fetchAndCache()
}

func (f *MCFetcher) loadFromCache() error {
	file, err := os.Open(cacheFile)
	if err != nil {
		return err
	}
	defer file.Close()

	var manifest MojangManifest
	if err := sonic.ConfigFastest.NewDecoder(file).Decode(&manifest); err != nil {
		return err
	}
	f.setVersions(manifest)
	return nil
}

func (f *MCFetcher) fetchAndCache() {
	log.Println("Fetching latest Minecraft versions...")

	req, err := http.NewRequest("GET", "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json", nil)
	if err != nil {
		log.Println("Error creating request:", err)
		return
	}
	req.Header.Set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 modpublish-telemetry")

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		log.Println("Error fetching MC versions:", err)
		return
	}
	defer resp.Body.Close()

	file, err := os.Create(cacheFile)
	if err != nil {
		log.Println("Error creating MC versions cache file:", err)
		return
	}

	_, err = io.Copy(file, resp.Body)
	file.Close()
	if err != nil {
		log.Println("Error saving MC versions cache:", err)
		return
	}

	if err := f.loadFromCache(); err != nil {
		log.Println("Error loading fetched MC versions:", err)
		return
	}

	log.Println("Successfully updated Minecraft versions from network.")
}

func (f *MCFetcher) setVersions(manifest MojangManifest) {
	newVersions := make(map[string]bool)
	ordered := make([]string, 0, len(manifest.Versions))
	for _, v := range manifest.Versions {
		newVersions[v.ID] = true
		ordered = append(ordered, v.ID)
	}

	f.mu.Lock()
	f.validVersions = newVersions
	f.orderedVersions = ordered
	f.mu.Unlock()
	log.Printf("Loaded %d Minecraft versions.\n", len(newVersions))
}

func (f *MCFetcher) IsValidMCVersion(version string) bool {
	f.mu.RLock()
	defer f.mu.RUnlock()
	return f.validVersions[version]
}

func (f *MCFetcher) GetValidVersions() []string {
	f.mu.RLock()
	defer f.mu.RUnlock()
	versions := make([]string, len(f.orderedVersions))
	copy(versions, f.orderedVersions)
	return versions
}
