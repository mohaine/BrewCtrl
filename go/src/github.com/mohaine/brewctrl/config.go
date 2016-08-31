package main

import (
	"encoding/json"
	"github.com/mohaine/id"
	"io"
	"io/ioutil"
	"fmt"
	"os"
	"bytes"
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

func IdEverything(cfg *Configuration) {
	idMap := make(map[string]bool)
	tanks := cfg.BrewLayout.Tanks
	for i := 0; i < len(tanks); i++ {
		tank := &tanks[i]
		if len(tank.Id) == 0 || idMap[tank.Id] {
			tank.Id = id.RandomId()
		}
		idMap[tank.Id] = true
	}
	pumps := cfg.BrewLayout.Pumps
	for i := 0; i < len(pumps); i++ {
		pump := &pumps[i]
		if len(pump.Id) == 0 || idMap[pump.Id] {
			pump.Id = id.RandomId()
		}
		idMap[pump.Id] = true
	}
}

func IoToOwnerIdMap(cfg *Configuration) (ioMap map[int32]string) {
	ioMap = make(map[int32]string)
	tanks := cfg.BrewLayout.Tanks
	for i := 0; i < len(tanks); i++ {
		tank := tanks[i]
		heater := tank.Heater
		if heater.Io > 0 {
			ioMap[heater.Io] = tank.Id
		}
	}
	pumps := cfg.BrewLayout.Pumps
	for i := 0; i < len(pumps); i++ {
		pump := pumps[i]
		ioMap[pump.Io] = pump.Id
	}
	return
}

func WriteConfiguration(cfg *Configuration) {
	j, err := json.Marshal(cfg)
	if err != nil {
		fmt.Println("error:", err)
	}
	var out bytes.Buffer
	json.Indent(&out, j, "", "  ")
  err = ioutil.WriteFile(CFG_FILE, out.Bytes(), 0644)
	if err != nil {
		fmt.Println("Failed to write to ", CFG_FILE," error ", err)
	}
}

func LoadCfg(path string) (Configuration, error) {
	var cfg Configuration
	var err error
	var f io.Reader
	fmt.Printf("Load Cfg File %v\n", path)
	f, err = os.Open(path)
	if err == nil {
		dec := json.NewDecoder(f)
		err = dec.Decode(&cfg)
	}
	return cfg, err
}
