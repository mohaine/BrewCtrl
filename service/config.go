package service

import (
	"bytes"
	"encoding/json"
	"github.com/mohaine/brewctrl/id"
	"io"
	"io/ioutil"
	"log"
	"os"
)

type SensorAddress struct {
	Address string `json:"address,omitempty"`
}

type SensorConfig struct {
	Name        string  `json:"name,omitempty"`
	CorrectionC float32 `json:"correctionC,omitempty"`
	Address     string  `json:"address,omitempty"`
	Location    string  `json:"location,omitempty"`
}

type HeatElement struct {
	Id                    string `json:"id,omitempty"`
	Name                  string `json:"name,omitempty"`
	Io                    int32  `json:"io,omitempty"`
	HasDuty               bool   `json:"hasDuty,omitempty"`
	MinStateChangeSeconds int32  `json:"minStateChangeSeconds,omitempty"`
	InvertIo              bool   `json:"invertIo,omitempty"`
	FullOnAmps            int32  `json:"fullOnAmps,omitempty"`
	MaxDuty               int32  `json:"maxDuty,omitempty"`
}

type Tank struct {
	Id     string        `json:"id,omitempty"`
	Name   string        `json:"name,omitempty"`
	Sensor SensorAddress `json:"sensor,omitempty"`
	Heater HeatElement   `json:"heater,omitempty"`
}
type Pump struct {
	Id                    string `json:"id,omitempty"`
	Name                  string `json:"name,omitempty"`
	Io                    int32  `json:"io,omitempty"`
	HasDuty               bool   `json:"hasDuty,omitempty"`
	MinStateChangeSeconds int32  `json:"minStateChangeSeconds,omitempty"`
	InvertIo              bool   `json:"invertIo,omitempty"`
}

type BreweryLayout struct {
	MaxAmps int32  `json:"maxAmps"`
	Tanks   []Tank `json:"tanks"`
	Pumps   []Pump `json:"pumps"`
}

type StepControlPoint struct {
	TargetTemp  float32 `json:"targetTemp,omitempty"`
	TargetName  string  `json:"targetName,omitempty"`
	ControlName string  `json:"controlName,omitempty"`
	Duty        int32   `json:"duty,omitempty"`
}

type Step struct {
	Name          string             `json:"name,omitempty"`
	Time          string             `json:"time,omitempty"`
	ControlPoints []StepControlPoint `json:"controlPoints,omitempty"`
}

type StepList struct {
	Id    string `json:"id,omitempty"`
	Name  string `json:"name,omitempty"`
	Steps []Step `json:"steps"`
}

type Configuration struct {
	Version     string         `json:"version,omitempty"`
	LogMessages bool           `json:"logMessages,omitempty"`
	BrewLayout  BreweryLayout  `json:"brewLayout"`
	Sensors     []SensorConfig `json:"sensors"`
	StepLists   []StepList     `json:"stepLists"`
}

func IdEverything(cfg *Configuration) {
	idMap := make(map[string]bool)

	stepLists := cfg.StepLists
	for i := 0; i < len(stepLists); i++ {
		stepList := &stepLists[i]
		if len(stepList.Id) == 0 || idMap[stepList.Id] {
			stepList.Id = id.RandomId()
		}
		idMap[stepList.Id] = true
	}

	tanks := cfg.BrewLayout.Tanks
	for i := 0; i < len(tanks); i++ {
		tank := &tanks[i]
		if len(tank.Id) == 0 || idMap[tank.Id] {
			tank.Id = id.RandomId()
		}
		idMap[tank.Id] = true

		if len(tank.Heater.Id) == 0 || idMap[tank.Heater.Id] {
			tank.Heater.Id = id.RandomId()
		}
		idMap[tank.Heater.Id] = true
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

func SetMaxDutyIfNot(cfg *Configuration) {
	tanks := cfg.BrewLayout.Tanks
	for i := 0; i < len(tanks); i++ {
		tank := &tanks[i]
		if tank.Heater.MaxDuty == 0 {
			tank.Heater.MaxDuty = 100
		}
	}
}

func SetPumpsMinChangeTime(cfg *Configuration) {
	pumps := cfg.BrewLayout.Pumps
	for i := 0; i < len(pumps); i++ {
		pump := &pumps[i]
		if !pump.HasDuty && pump.MinStateChangeSeconds <= 0 {
			pump.MinStateChangeSeconds = 15
			log.Println("Update pump ", pump.Name, "min time change to default of 15 seconds")

		}
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
		log.Println("error:", err)
	}
	var out bytes.Buffer
	json.Indent(&out, j, "", "  ")
	err = ioutil.WriteFile(CFG_FILE, out.Bytes(), 0644)
	if err != nil {
		log.Println("Failed to write to ", CFG_FILE, " error ", err)
	}
}

func LoadCfg(path string) (Configuration, error) {
	var cfg Configuration
	var err error
	var f io.Reader
	log.Printf("Load Cfg File %v\n", path)
	f, err = os.Open(path)
	if err == nil {
		dec := json.NewDecoder(f)
		err = dec.Decode(&cfg)
	}
	if err == nil {
		IdEverything(&cfg)
		SetMaxDutyIfNot(&cfg)
		SetPumpsMinChangeTime(&cfg)
	}
	return cfg, err
}
