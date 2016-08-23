package main

import (
	"fmt"
	"github.com/mohaine/onewire"
	"time"
)

const SYS_PATH = "/home/graessle/source/BrewCtrl/mock/sys/"

func main() {
	cfg, err := LoadCfg("../BrewControllerConfig.json")
	if err == nil {

		fmt.Printf("Failed to load Cfg File: %v\n", err)

		cfg, err = LoadCfg("../BrewControllerConfig.json.dist")
		if err != nil {
			panic(err)
		}
	}
	readSensors, stopReading := onewire.SensorLoop(100*time.Millisecond, SYS_PATH+"bus/w1/devices/")
	stopControl := ControlStuff(readSensors, cfg)

	time.Sleep(1 * time.Second)
	stopControl()
	stopReading()
	time.Sleep(100 * time.Millisecond)
}
