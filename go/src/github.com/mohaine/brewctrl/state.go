package main

import (
	// "encoding/json"
	// "io"
	// "io/ioutil"
	"log"
	// "os"
	// "fmt"
	// "strings"
	"github.com/mohaine/id"
	"github.com/mohaine/onewire"
	"github.com/mohaine/pid"
)

const MODE_OFF = "OFF"
const MODE_ON = "ON"
const MODE_HOLD = "HOLD"
const MODE_HEAT_OFF = "HEAT_OFF"

var NilSensor = Sensor{Address: "", TemperatureC: 0, Reading: false}

type Sensor struct {
	Address      string  `json:"address,omitempty"`
	TemperatureC float32 `json:"temperatureC,omitempty"`
	Reading      bool    `json:"reading,omitempty"`
}

type ControlPoint struct {
	// initComplete bool `json:"initComplete,omitempty"`;
	Id                    string `json:"id,omitempty"`
	Io                    int32  `json:"controlIo"`
	Duty                  int32  `json:"duty"`
	FullOnAmps            int32
	MaxDuty               int32
	SensorAddress         string  `json:"tempSensorAddress"`
	TargetTemp            float32 `json:"targetTemp"`
	HasDuty               bool    `json:"hasDuty"`
	MinStateChangeSeconds int32
	LastStateChangeTime   uint64
	AutomaticControl      bool `json:"automaticControl,omitempty"`
	ActuallyOn            bool `json:"on"`
	lastUpdateOnOffTimes  uint64
	dutyTimeOn            uint64
	dutyTimeOff           uint64
	duty                  int32
	ioState               bool
	ioStateKnow           bool
	pid                   pid.Pid
}

type ControlStep struct {
	Id                string         `json:"id"`
	Name              string         `json:"name"`
	StepTime          int32          `json:"stepTime"`
	Active            bool           `json:"active"`
	WaitForTargetTemp bool           `json:"waitForTargetTemp"`
	ControlPoints     []ControlPoint `json:"controlPoints"`
	pastTargetTemp    bool
	lastOnTime        uint64
}

type State struct {
	Mode                 string        `json:"mode"`
	ListName             string        `json:"listName"`
	ConfigurationVersion string        `json:"configurationVersion"`
	Sensors              []Sensor      `json:"sensors"`
	Steps                []ControlStep `json:"steps"`
}

func StateDefault(cfg Configuration, initIo func(int32)) (state State) {
	SetToStateDefault(cfg, &state, initIo)
	return
}

func SetToStateDefault(cfg Configuration, state *State, initIo func(int32)) {
	state.Mode = MODE_OFF
	state.ConfigurationVersion = cfg.Version
	state.ListName = "Default List"
	state.Steps = append(state.Steps, StepDefault(cfg, initIo))
	return
}

func IsHeater(cfg *Configuration, io int32) bool {
	tanks := cfg.BrewLayout.Tanks
	for i := 0; i < len(tanks); i++ {
		tank := tanks[i]
		heater := tank.Heater
		if heater.Io > 0 && heater.Io == io {
			return true
		}
	}
	return false
}

func StepDefault(cfg Configuration, initIo func(int32)) (step ControlStep) {
	step.Name = "Manual Step"
	step.Id = id.RandomId()
	step.StepTime = 0
	step.Active = true

	tanks := cfg.BrewLayout.Tanks
	for i := 0; i < len(tanks); i++ {
		tank := tanks[i]
		heater := tank.Heater
		if heater.Io > 0 {
			cp := createControlPoint(heater.Io, heater.HasDuty, heater.MinStateChangeSeconds, heater.FullOnAmps, heater.MaxDuty, initIo)
			step.ControlPoints = append(step.ControlPoints, cp)
		}
	}
	pumps := cfg.BrewLayout.Pumps
	for i := 0; i < len(pumps); i++ {
		pump := pumps[i]
		cp := createControlPoint(pump.Io, pump.HasDuty, pump.MinStateChangeSeconds, 0, 100, initIo)
		step.ControlPoints = append(step.ControlPoints, cp)
	}
	return
}

func createControlPoint(io int32, hasDuty bool, minStateChangeSeconds int32, fullOnAmps int32, maxDuty int32, initIo func(int32)) (cp ControlPoint) {
	cp.Id = id.RandomId()
	cp.Io = io
	cp.FullOnAmps = fullOnAmps
	cp.MaxDuty = maxDuty
	cp.HasDuty = hasDuty
	cp.MinStateChangeSeconds = minStateChangeSeconds
	// cp.On = false
	cp.Duty = 0
	initControlPointDuty(&cp, initIo)
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

func Min(x, y int32) int32 {
	if x > y {
		return y
	}
	return x
}

func StateUpdateDuty(state *State) {
	if state.Mode != MODE_OFF && len(state.Steps) > 0 {
		controlPoints := state.Steps[0].ControlPoints
		for i := range controlPoints {
			cp := &controlPoints[i]
			if cp.AutomaticControl {
				sensor := FindSensor(state, cp.SensorAddress)
				if sensor != NilSensor {
					// if (hasVaildTemp(sensor)) {
					if cp.HasDuty {
						cp.Duty = Min(cp.MaxDuty, pid.GetDuty(&cp.pid, cp.TargetTemp, sensor.TemperatureC))
						// log.Printf("Target %v but at %v duty: %v\n", cp.TargetTemp, sensor.TemperatureC,cp.Duty)
					} else {

						newDuty := int32(0)
						if sensor.TemperatureC < cp.TargetTemp {
							newDuty = 100
						}
						if newDuty != cp.Duty {
							// Only change state if > MinStateChangeSeconds since last
							now := millis()
							timeSinceLast := millis() - cp.LastStateChangeTime
							if timeSinceLast/1000 > uint64(cp.MinStateChangeSeconds) {
								cp.LastStateChangeTime = now
								cp.Duty = newDuty
							}
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
