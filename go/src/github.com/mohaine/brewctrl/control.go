package main

import (
	"fmt"
	"github.com/mohaine/onewire"
	// "github.com/mohaine/pid"
	"time"
	// "encoding/json"
	// "log"
	// "bytes"
	// "os"
)

func ControlStuff(readSensors func() []onewire.TempReading, cfg Configuration) (stopControl func(), getState func() State, getCfg func() Configuration, setMode func(string)) {

	quit := make(chan int)
	stopControl = func() { quit <- 1 }
	tickPins := time.Tick(100 * time.Millisecond)
	tickDuty := time.Tick(1000 * time.Millisecond)

	requestState := make(chan int)
	receiveState := make(chan State)
	getState = func() State {
		requestState <- 1
		state := <-receiveState
		return state
	}
	requestCfg := make(chan int)
	receiveCfg := make(chan Configuration)
	getCfg = func() Configuration {
		requestCfg <- 1
		cfg := <-receiveCfg
		return cfg
	}
	setModeC := make(chan string)
	setMode = func(mode string) {
		setModeC <- mode
	}

	// TODO load old status?
	state := StateDefault(cfg)
	state.Mode = MODE_ON

	controlPoints := state.Steps[0].ControlPoints
	for i := range controlPoints {
		cp := &controlPoints[i]
		cp.Duty = 33
		cp.On = true
		cp.HasDuty = true
	}

	fmt.Println(state.ConfigurationVersion)
	loop := func() {
		for {
			select {
			case <-tickDuty:
				StateUpdateSensors(&state, readSensors())
				StateUpdateDuty(&state)
			case <-tickPins:
				UpdatePinsForSetDuty(&state, cfg.BrewLayout.MaxAmps)
			case <-requestState:
				receiveState <- state
			case <-requestCfg:
				receiveCfg <- cfg
			case mode := <-setModeC:
				state.Mode = mode
			case <-quit:
				return
			}
		}
	}
	go loop()
	return

}
