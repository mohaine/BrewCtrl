package service

import (
	"fmt"
	"github.com/mohaine/brewctrl/gpio"
	"github.com/mohaine/brewctrl/onewire"
	"time"
	// "log"
)

type MockTank struct {
	id         string
	tempMilliC int32
}

func SensorLoopMock(interval time.Duration, cfg Configuration) (read func() []onewire.TempReading, quit func(), initIo func(int32), turnIoTo func(int32, bool)) {
	quitC := make(chan int)
	quit = func() { quitC <- 1 }

	gpio.SYS_PATH = "mock/sys/"
	initIo = func(io int32) {
		gpio.IoMode(io, gpio.IO_OUT)
		gpio.TurnIoTo(io, false)
	}
	turnIoTo = func(io int32, inout bool) {
		gpio.TurnIoTo(io, inout)
	}

	tick := time.Tick(interval)

	ambientTemp := int32(26202)
	currReadings := make([]onewire.TempReading, 0)
	read = func() []onewire.TempReading {
		now := currReadings
		readings := make([]onewire.TempReading, len(now))
		copy(readings, now)
		return readings
	}

	mockTanks := make([]MockTank, 0, len(cfg.BrewLayout.Tanks))
	tanks := cfg.BrewLayout.Tanks
	for i := range tanks {
		tank := &tanks[i]
		found := false
		for j := range mockTanks {
			mockTank := mockTanks[j]
			if mockTank.id == tank.Id {
				found = true
				break
			}
		}
		if !found {
			mockTank := new(MockTank)
			mockTank.id = tank.Id
			mockTank.tempMilliC = ambientTemp
			mockTanks = append(mockTanks, *mockTank)
		}
	}

	loop := func() {
		for {
			select {
			case <-tick:
				readings := make([]onewire.TempReading, 0, len(mockTanks))
				for i := range mockTanks {
					mockTank := mockTanks[i]
					temp := new(onewire.TempReading)
					temp.TempMilliC = mockTank.tempMilliC
					temp.Id = fmt.Sprintf("sensor%v", i)
					readings = append(readings, *temp)
				}
				currReadings = readings
			case <-quitC:
				return
			}
		}
	}
	go loop()
	return
}
