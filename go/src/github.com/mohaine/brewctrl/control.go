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

func ControlStuff(readSensors func() []onewire.TempReading, cfg Configuration) (stopControl func()) {

	quit := make(chan int)
	stopControl = func() { quit <- 1 }
	tickPins := time.Tick(100 * time.Millisecond)
	tickDuty := time.Tick(1000 * time.Millisecond)

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
			case <-quit:
				return
			}
		}
	}
	go loop()
	return

}
