package main

import (
	"fmt"
	"github.com/mohaine/brewctrl/config"
	"github.com/mohaine/onewire"
	"time"
)

func ProcessTemps(quit chan int, readSensors func() []onewire.TempReading) {
	tick := time.Tick(100 * time.Millisecond)
	for {
		select {
		case <-tick:
			fmt.Println("tick", readSensors())
		case <-quit:
			fmt.Println("QUIT Tick!")
			return
		}
	}
}

func main() {

	cfg, err := config.LoadCfg("../BrewControllerConfig.json")
	if err == nil {
		println(cfg.Version)
	}

	readSensors, stopReading := onewire.SensorLoop(100 * time.Millisecond)
	quitC := make(chan int)
	go ProcessTemps(quitC, readSensors)

	time.Sleep(2 * time.Second)

	quitC <- 1
	stopReading()

	time.Sleep(1 * time.Second)
	fmt.Println("QUIT!")
}
