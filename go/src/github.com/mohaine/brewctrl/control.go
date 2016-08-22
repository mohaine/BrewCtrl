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
	tick := time.Tick(100 * time.Millisecond)

	// TODO load old status?
	state := StateDefault(cfg)
	fmt.Println(state.ConfigurationVersion)
	loop := func() {
		for {
			select {
			case <-tick:
				StateUpdateSensors(&state, readSensors())

				StateUpdateDuty(&state)

				// fmt.Println("tick", readSensors())
			case <-quit:
				return
			}
		}
	}
	go loop()
	return

}
