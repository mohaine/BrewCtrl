package main

import (
	// "encoding/json"
	// "io"
	// "io/ioutil"
	"log"
	// "os"
	"fmt"
	// "strings"
	"github.com/mohaine/id"
	"github.com/mohaine/pid"
	"github.com/mohaine/onewire"
)

const MODE_OFF = "Off"
const MODE_ON = "On"
var NilSensor = Sensor{Address : "", TemperatureC : 0, Reading: false}

type Sensor struct {
	Address      string  `json:"address"`
	TemperatureC float32 `json:"temperatureC"`
	Reading      bool    `json:"reading"`
}

type DutyController struct {
	lastUpdateOnOffTimes uint64
	dutyTimeOn           uint64
	dutyTimeOff          uint64
	duty                 int32
	on                   bool
	ioState              bool
	io                   int32
}

type ControlPoint struct {
	// initComplete bool `json:"initComplete"`;
	Io                int32   `json:"controlIo"`
	Duty              int32   `json:"duty"`
	FullOnAmps        int32   `json:"fullOnAmps"`
	SensorAddress string  `json:"tempSensorAddress"`
	TargetTemp        float32 `json:"targetTemp"`
	HasDuty           bool    `json:"hasDuty"`
	AutomaticControl  bool    `json:"automaticControl"`
	On                bool    `json:"on"`
	dutyController    DutyController
	pid pid.Pid
}

type ControlStep struct {
	Id            string         `json:"id"`
	Name          string         `json:"name"`
	StepTime      int32          `json:"stepTime"`
	Active        bool           `json:"active"`
	ControlPoints []ControlPoint `json:"controlPoints"`
}

type State struct {
	Mode                 string        `json:"mode"`
	ListName             string        `json:"listName"`
	ConfigurationVersion string        `json:"configurationVersion"`
	Sensors              []Sensor      `json:"sensors"`
	Steps                []ControlStep `json:"steps"`
}

func StateDefault(cfg Configuration) (state State) {
	state.Mode = MODE_OFF
	state.ConfigurationVersion = cfg.Version
	state.ListName = "Default List"
	state.Steps = append(state.Steps, StepDefault(cfg))
	return
}

func StepDefault(cfg Configuration) (step ControlStep) {
	step.Name = "Manual Step"
	step.Id = id.RandomId()
	step.StepTime = 0
	step.Active = true

	pumps := cfg.BrewLayout.Pumps
	for i := 0; i < len(pumps); i++ {
		pump := pumps[i]
		var cp ControlPoint
		fmt.Printf("Pump: %s\n", pumps[i].Name)
		cp.Io = pump.Io
		cp.FullOnAmps = 0
		cp.HasDuty = pump.HasDuty
		cp.On = false
		cp.Duty = 0
		setupDutyController(&cp.dutyController, cp.Io)
		step.ControlPoints = append(step.ControlPoints, cp)
	}
	return
}

func StateUpdateSensors(state *State, sensorReadings []onewire.TempReading) {
	var sensors []Sensor
	for i := range sensorReadings {
		reading := sensorReadings[i]
		var sensor Sensor
		sensor.Address = reading.Id
		sensor.TemperatureC = reading.TempC()
		sensor.Reading = true
		sensors = append(sensors, sensor)
	}
	state.Sensors = sensors
}

func FindSensor(state *State,address string) Sensor {
	for i := range state.Sensors {
		sensor:= state.Sensors[i]
		if(sensor.Address == address){
			return sensor
		}
	}
	return NilSensor
}

func StateUpdateDuty(state *State) {
	fmt.Println("StateUpdateDuty")
	if(state.Mode == MODE_ON){
	controlPoints := state.Steps[0].ControlPoints
	for i := range controlPoints {
		cp := controlPoints[i]
		fmt.Printf("CP: %v\n", cp)

		cp.dutyController.on = true;
		if (cp.AutomaticControl) {
			sensor := FindSensor(state, cp.SensorAddress);
			if (sensor != NilSensor) {
				// if (hasVaildTemp(sensor)) {
					if (cp.HasDuty) {
						cp.Duty = pid.GetDuty(&cp.pid, cp.TargetTemp, sensor.TemperatureC);
					} else {
						if (sensor.TemperatureC < cp.TargetTemp) {
							cp.Duty = 100;
						} else {
							cp.Duty = 0;
						}
					}
				// } else {
				// 	cp.duty = 0;
				// }

			} else {
				log.Printf("Failed to find sensor %v\n" , cp.SensorAddress );
			}
		}
	}
}

}
