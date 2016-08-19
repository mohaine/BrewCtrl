package main

import (
	"fmt"
	"github.com/mohaine/brewctrl/config"
	"github.com/mohaine/onewire"
	"time"
)


func main() {

	cfg, err := config.LoadCfg("../BrewControllerConfig.json")
	if err == nil {
		cfg, err = config.LoadCfg("../BrewControllerConfig.json.dist")
		if err != nil {
			panic(err)
		}
	}
	readSensors, stopReading := onewire.SensorLoop(100 * time.Millisecond)
	stopControl := ControlStuff(readSensors, cfg)



	time.Sleep(1 * time.Second)
	stopControl()
	stopReading()

	time.Sleep(100 * time.Millisecond)
	fmt.Println("QUIT!")
}
