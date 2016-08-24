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
	"github.com/mohaine/onewire"
	"github.com/mohaine/pid"
)

const MODE_OFF = "Off"
const MODE_ON = "On"

var NilSensor = Sensor{Address: "", TemperatureC: 0, Reading: false}

type Sensor struct {
	Address      string  `json:"address,omitempty"`
	TemperatureC float32 `json:"temperatureC,omitempty"`
	Reading      bool    `json:"reading,omitempty"`
}

type ControlPoint struct {
	// initComplete bool `json:"initComplete,omitempty"`;
	Io                   int32   `json:"controlIo,omitempty"`
	Duty                 int32   `json:"duty,omitempty"`
	FullOnAmps           int32   `json:"fullOnAmps,omitempty"`
	SensorAddress        string  `json:"tempSensorAddress,omitempty"`
	TargetTemp           float32 `json:"targetTemp,omitempty"`
	HasDuty              bool    `json:"hasDuty,omitempty"`
	AutomaticControl     bool    `json:"automaticControl,omitempty"`
	On                   bool    `json:"on,omitempty"`
	lastUpdateOnOffTimes uint64
	dutyTimeOn           uint64
	dutyTimeOff          uint64
	duty                 int32
	ioState              bool
	pid                  pid.Pid
}

type ControlStep struct {
	Id            string         `json:"id,omitempty"`
	Name          string         `json:"name,omitempty"`
	StepTime      int32          `json:"stepTime,omitempty"`
	Active        bool           `json:"active,omitempty"`
	ControlPoints []ControlPoint `json:"controlPoints,omitempty"`
}

type State struct {
	Mode                 string        `json:"mode,omitempty"`
	ListName             string        `json:"listName,omitempty"`
	ConfigurationVersion string        `json:"configurationVersion,omitempty"`
	Sensors              []Sensor      `json:"sensors,omitempty"`
	Steps                []ControlStep `json:"steps,omitempty"`
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

	tanks := cfg.BrewLayout.Tanks
	for i := 0; i < len(tanks); i++ {
		tank := tanks[i]
		heater := tank.Heater
		fmt.Printf("Tank: %v\n", tank.Name)
		fmt.Printf("Heater: %v\n", tank.Heater)
		if heater.Io > 0 {
			cp := createControlPoint(heater.Io, heater.HasDuty)
			step.ControlPoints = append(step.ControlPoints, cp)
		}
	}
	pumps := cfg.BrewLayout.Pumps
	for i := 0; i < len(pumps); i++ {
		pump := pumps[i]
		fmt.Printf("Pump: %v\n", pump.Name)
		cp := createControlPoint(pump.Io, pump.HasDuty)
		step.ControlPoints = append(step.ControlPoints, cp)
	}
	return
}

func createControlPoint(io int32, hashDuty bool) (cp ControlPoint) {
	cp.Io = io
	cp.FullOnAmps = 0
	cp.HasDuty = hashDuty
	cp.On = false
	cp.Duty = 0
	initControlPoint(&cp)
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

func FindSensor(state *State, address string) Sensor {
	for i := range state.Sensors {
		sensor := state.Sensors[i]
		if sensor.Address == address {
			return sensor
		}
	}
	return NilSensor
}

func StateUpdateDuty(state *State) {
	fmt.Println("StateUpdateDuty")
	if state.Mode != MODE_OFF && len(state.Steps) > 0 {
		controlPoints := state.Steps[0].ControlPoints
		for i := range controlPoints {
			cp := controlPoints[i]
			if cp.AutomaticControl {
				sensor := FindSensor(state, cp.SensorAddress)
				if sensor != NilSensor {
					// if (hasVaildTemp(sensor)) {
					if cp.HasDuty {
						cp.Duty = pid.GetDuty(&cp.pid, cp.TargetTemp, sensor.TemperatureC)
					} else {
						if sensor.TemperatureC < cp.TargetTemp {
							cp.Duty = 100
						} else {
							cp.Duty = 0
						}
					}
				} else {
					log.Printf("Failed to find sensor %v\n", cp.SensorAddress)
					cp.Duty = 0
				}
			}
		}
	}

}
