package main

import (
	// "encoding/json"
	// "io"
	"io/ioutil"
	"log"
	// "os"
	"fmt"
	// "strings"
)

const IO_OUT = false
const IO_IN = true

var seenGpios = make([]int32, 0)

// func invertGpio(int io, bool invert) {
// 	if (io < MAX_GPIO) {
// 		HAS_CONTROLLED_GPIO[io] = true;
// 		INVERT_GPIO[io] = invert;
// 	}
// // }
//

func gpioRoot() string {
	return SYS_PATH + "class/gpio"

}

func ioMode(io int32, inout bool) {
	// // #ifdef MOCK
	direction := "out"
	if inout != IO_OUT {
		direction = "in"
	}
	// fmt.Printf("  Pin %v In/Out to %v\n", io, direction)
	// Export pin
	path := fmt.Sprintf("%v/export", gpioRoot())
	ioutil.WriteFile(path, []byte(fmt.Sprintf("%v", io)), 0644)
	// These fail if called twice
	// if err != nil {
	// 	log.Panic("Failed export io %v In/Out to %v\n", io, direction)
	// }

	// Set Direction
	path = fmt.Sprintf("%v/gpio%v/direction", gpioRoot(), io)
	ioutil.WriteFile(path, []byte(direction), 0644)
	// if err != nil {
	// 	log.Panic("Failed to set direction on io %v In/Out to %v\n", io, direction)
	// }
}

//
func turnIoTo(io int32, hilow bool) {

	// if (INVERT_GPIO[io]) {
	// 	hilow = !hilow;
	// }

	// fmt.Printf("Pin %v set to %v\n", io, hilow)
	// // #else
	oneZero := "0"
	if hilow {
		oneZero = "1"
	}
	path := fmt.Sprintf("%v/gpio%v/value", gpioRoot(), io)
	err := ioutil.WriteFile(path, []byte(oneZero), 0644)
	if err != nil {
		onOff := "off"
		if hilow {
			onOff = "on"
		}
		log.Printf(fmt.Sprintf("Failed to set output on io %v to %v (%v): %v\n", io, onOff, path, err))
	}
}

//
func turnOffSeenControls() {
	for i := range seenGpios {
		turnIoTo(seenGpios[i], false)
	}
}

func initControlPointDuty(hs *ControlPoint) {
	seenGpios = append(seenGpios, hs.Io)
	ioMode(hs.Io, IO_OUT)
	turnIoTo(hs.Io, false)
	hs.lastUpdateOnOffTimes = millis()
	hs.dutyTimeOn = 0
	hs.dutyTimeOff = 0
	hs.duty = 0
	// hs.On = false
	hs.ioState = false
}

func copyControlPointDuty(from *ControlPoint, to *ControlPoint) {
	to.lastUpdateOnOffTimes = from.lastUpdateOnOffTimes

	to.dutyTimeOn = from.dutyTimeOn
	to.dutyTimeOff = from.dutyTimeOff
	to.duty = from.duty
	// if from.On == to.On {
	// } else {
	// 	to.On = from.On
	// 	resetDutyState(to)
	// }
	to.ioState = from.ioState
}

func resetDutyState(hs *ControlPoint) {
	hs.dutyTimeOn = 0
	hs.dutyTimeOff = 0
	hs.lastUpdateOnOffTimes = millis()
}

func updateForPinState(hs *ControlPoint, newHeatPinState bool) {
	now := millis()

	timeSinceLast := now - hs.lastUpdateOnOffTimes
	if timeSinceLast > 30000 {
		// Over 30 seconds since last change. Dump silly values
		hs.dutyTimeOn = 0
		hs.dutyTimeOff = 0
		timeSinceLast = 0
	}

	if hs.ioState {
		hs.dutyTimeOn += (timeSinceLast)
	} else {
		hs.dutyTimeOff += (timeSinceLast)
	}
	hs.lastUpdateOnOffTimes = now

	newHeatPinState = newHeatPinState // && hs.On
	if newHeatPinState != hs.ioState {
		hs.ioState = newHeatPinState
		turnIoTo(hs.Io, hs.ioState)
	}
}

// func setHeatOn(hs *ControlPoint, newState bool) {
// 	if hs.On != newState {
// 		hs.On = newState
// 		if newState {
// 			resetDutyState(hs)
// 		} else {
// 			updateForPinState(hs, false)
// 		}
// 	}
// }

func updateForOverAmps(hs *ControlPoint) {
	updateForPinState(hs, false)
}

func updateIoForStateAndDuty(hs *ControlPoint) {
	newPinState := false
	// if hs.On {
	if hs.duty == 100 {
		newPinState = true
	} else if hs.duty == 0 {
		newPinState = false
	} else {
		timeSinceLast := millis() - hs.lastUpdateOnOffTimes
		timeOn := hs.dutyTimeOn
		timeOff := hs.dutyTimeOff

		if hs.ioState {
			timeOn += (timeSinceLast)
		} else {
			timeOff += (timeSinceLast)
		}
		totalTime := timeOn + timeOff
		percentOn := float32(timeOn) / float32(totalTime)
		percentOnTest := int32(percentOn * 1000)
		if percentOnTest >= hs.duty*10 {
			newPinState = false
		} else {
			newPinState = true
		}
		/*
		 if (hs.controlIo == 10) {
		 DBG("     On: %s OnTime: %lu Off Time: %lu totalTime:  %lu  Persent ON  : %f\n",(newHeatPinState?"ON " : "OFF"), timeOn , timeOff , totalTime , percentOn * 100);
		 }
		*/
	}
	// } else {
	// 	newPinState = false
	// }
	updateForPinState(hs, newPinState)
}

func setHeatDuty(hs *ControlPoint, duty int32) {
	if duty < 0 {
		duty = 0
	}
	if duty != hs.duty {
		hs.duty = duty
		resetDutyState(hs)
	}
}
func initHardware() {
	turnOffSeenControls()
}

func lockSteps() {
}
func unlockSteps() {
}
func UpdatePinsForSetDuty(cfg *Configuration, state *State) {
	maxAmps := cfg.BrewLayout.MaxAmps
	currentAmps := int32(0)
	lockSteps()
	if len(state.Steps) > 0 {
		controlPoints := state.Steps[0].ControlPoints
		for i := range controlPoints {
			cp := &controlPoints[i]
			// setupControlPoint(cp);

			actuallyOn := state.Mode != MODE_OFF
			if state.Mode == MODE_HEAT_OFF {
				heater := IsHeater(cfg, cp.Io)
				actuallyOn = !heater
			}

			if actuallyOn != cp.ActuallyOn {
				// Clear duty state so it doesn't go nuts tring to catch up
				resetDutyState(cp)
				cp.ActuallyOn = actuallyOn
			}
			if actuallyOn {
				duty := cp.Duty
				if currentAmps+cp.FullOnAmps > maxAmps {
					updateForOverAmps(cp)
				} else if cp.HasDuty {
					setHeatDuty(cp, duty)
					updateIoForStateAndDuty(cp)
				} else {
					updateForPinState(cp, duty > 0)
				}
				if cp.ioState {
					currentAmps += cp.FullOnAmps
				}
			} else if cp.ioState {
				turnIoTo(cp.Io, false)

			}
		}
	}
	unlockSteps()

	//DBG("***********  updatePinsForSetDuty - END *************** \n");
}
