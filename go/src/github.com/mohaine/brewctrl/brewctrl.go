package main

import (
	"flag"
	"fmt"
	"github.com/mohaine/onewire"
	"time"
)

var SYS_PATH = "/sys/"

func main() {

	mock := flag.Bool("mock", false, "Use Mock GPIO/Sensors")

	flag.Parse()

	if *mock {
		SYS_PATH = "mock/sys/"
	}
	cfg, err := LoadCfg("BrewControllerConfig.json")
	if err != nil {
		fmt.Printf("Failed to load Cfg File: %v\n", err)
		cfg, err = LoadCfg("BrewControllerConfig.json.dist")
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
