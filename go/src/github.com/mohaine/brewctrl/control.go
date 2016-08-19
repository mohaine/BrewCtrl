package main

import (
	"fmt"
	"github.com/mohaine/brewctrl/config"
	"github.com/mohaine/onewire"
	"time"
)

func ControlStuff(readSensors func() []onewire.TempReading, cfg config.Configuration) (stopControl func()) {

	quit := make(chan int)
	stopControl = func() { quit <- 1 }
	tick := time.Tick(100 * time.Millisecond)

	fmt.Println(cfg.Version)
  loop := func() {
		for {
			select {
			case <-tick:
				fmt.Println("tickXXX", readSensors())
			case <-quit:
				fmt.Println("QUIT Tick!")
				return
			}
		}
	}
	go loop()
	return

}
