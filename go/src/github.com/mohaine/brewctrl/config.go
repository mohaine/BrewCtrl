package main

import (
	"encoding/json"
	"io"
	// "io/ioutil"
	// "log"
	"fmt"
	"os"
	// "strings"
)

type SensorAddress struct {
	Address string `json:"address,omitempty"`
}

type SensorConfig struct {
	Name     string `json:"name,omitempty"`
	Address  string `json:"address,omitempty"`
	Location string `json:"location,omitempty"`
}

type HeatElement struct {
	Id         string `json:"id,omitempty"`
	Name       string `json:"name,omitempty"`
	Io         int32  `json:"io,omitempty"`
	HasDuty    bool   `json:"hasDuty,omitempty"`
	InvertIo   bool   `json:"invertIo,omitempty"`
	FullOnAmps int32  `json:"fullOnAmps,omitempty"`
}

type Tank struct {
	Id     string        `json:"id,omitempty"`
	Name   string        `json:"name,omitempty"`
	Sensor SensorAddress `json:"sensor,omitempty"`
	Heater HeatElement   `json:"heater,omitempty"`
}
type Pump struct {
	Id       string `json:"id,omitempty"`
	Name     string `json:"name,omitempty"`
	Io       int32  `json:"io,omitempty"`
	HasDuty  bool   `json:"hasDuty,omitempty"`
	InvertIo bool   `json:"invertIo,omitempty"`
}

type BreweryLayout struct {
	MaxAmps int32  `json:"maxAmps,omitempty"`
	Tanks   []Tank `json:"tanks,omitempty"`
	Pumps   []Pump `json:"pumps,omitempty"`
}

type StepControlPoint struct {
	TargetTemp  float32 `json:"targetTemp,omitempty"`
	TargetName  string  `json:"targetName,omitempty"`
	ControlName string  `json:"controlName,omitempty"`
}

type Step struct {
	Name          string             `json:"name,omitempty"`
	Time          string             `json:"time,omitempty"`
	ControlPoints []StepControlPoint `json:"controlPoints,omitempty"`
}

type StepList struct {
	Name  string `json:"name,omitempty"`
	Steps []Step `json:"steps,omitempty"`
}

type Configuration struct {
	Version     string         `json:"version,omitempty"`
	LogMessages bool           `json:"logMessages,omitempty"`
	BrewLayout  BreweryLayout  `json:"brewLayout,omitempty"`
	Sensors     []SensorConfig `json:"sensors,omitempty"`
	StepLists   []StepList     `json:"stepLists,omitempty"`
}

func LoadCfg(path string) (Configuration, error) {
	var cfg Configuration
	var err error
	var f io.Reader
	fmt.Printf("Load Cfg File %v\n", path)
	f, err = os.Open(path)
	if err == nil {
		fmt.Printf("Failed to open Cfg File %v\n", path)
		dec := json.NewDecoder(f)
		err = dec.Decode(&cfg)
	}
	return cfg, err
}
