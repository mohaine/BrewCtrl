package main

import (
	"encoding/json"
	"io"
	// "io/ioutil"
	// "log"
	"os"
	// "fmt"
	// "strings"
)

type SensorAddress struct {
	Address string `json:"address"`
}

type SensorConfig struct {
	Name     string `json:"name"`
	Address  string `json:"address"`
	Location string `json:"location"`
}

type HeatElement struct {
	Id         string `json:"id"`
	Name       string `json:"name"`
	Io         int32  `json:"io"`
	HasDuty    bool   `json:"hasDuty"`
	InvertIo   bool   `json:"invertIo"`
	FullOnAmps int32  `json:"fullOnAmps"`
}

type Tank struct {
	Id     string        `json:"id"`
	Name   string        `json:"name"`
	Sensor SensorAddress `json:"sensor"`
	Heater HeatElement   `json:"heater"`
}
type Pump struct {
	Id       string `json:"id"`
	Name     string `json:"name"`
	Io       int32  `json:"io"`
	HasDuty  bool   `json:"hasDuty"`
	InvertIo bool   `json:"invertIo"`
}

type BreweryLayout struct {
	MaxAmps int32  `json:"maxAmps"`
	Tanks   []Tank `json:"tanks"`
	Pumps   []Pump `json:"pumps"`
}

type StepControlPoint struct {
	TargetTemp  float32 `json:"targetTemp"`
	TargetName  string  `json:"targetName"`
	ControlName string  `json:"controlName"`
}

type Step struct {
	Name          string             `json:"name"`
	Time          string             `json:"time"`
	ControlPoints []StepControlPoint `json:"controlPoints"`
}

type StepList struct {
	Name  string `json:"name"`
	Steps []Step `json:"steps"`
}

type Configuration struct {
	Version     string         `json:"version"`
	LogMessages bool           `json:"logMessages"`
	BrewLayout  BreweryLayout  `json:"brewLayout"`
	Sensors     []SensorConfig `json:"sensors"`
	StepLists   []StepList     `json:"stepLists"`
}

func LoadCfg(path string) (Configuration, error) {
	var cfg Configuration
	var err error
	var f io.Reader
	f, err = os.Open(path)
	if err == nil {
		dec := json.NewDecoder(f)
		err = dec.Decode(&cfg)
	}
	return cfg, err
}
