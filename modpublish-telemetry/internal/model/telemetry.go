package model

type TelemetryRequest struct {
	MinecraftVersion []string `json:"minecraftVersion"`
	PublishTarget    []string `json:"publishTarget"`
	Loader           []string `json:"loader"`
}
