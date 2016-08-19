package status

import (
	"encoding/json"
	"io"
	// "io/ioutil"
	// "log"
	"os"
	// "fmt"
	// "strings"
)

type Sensor struct {
	Address string `json:"address"`
}

type Status struct {
	Mode string;
	ListName string;
	ConfigurationVersion string;
}

type ControlPoint struct {
	// initComplete bool `json:"initComplete"`;
	ControlIo int32 `json:"controlIo"`;
	Duty int32 `json:"duty"`;
	FullOnAmps int32 `json:"fullOnAmps"`;
	TempSensorAddress string `json:"tempSensorAddress"`;
	TargetTemp double `json:"targetTemp"`;
	HasDuty bool `json:"hasDuty"`;
	AutomaticControl bool `json:"automaticControl"`;
	DutyController DutyController `json:"dutyController"`;
	On bool `json:"on"`;
	// pid Pid `json:"pid"`;
} ;

 type ControlStep struct {
	Id  string `json:"id"`;
	Name string `json:"name"`;
	StepTime int32 `json:"stepTime"`;active bool `json:"active"`;
	ControlPoints []ControlPoint `json:"controlPoints"`;
} ;

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
