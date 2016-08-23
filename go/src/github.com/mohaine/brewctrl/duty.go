package main

import (
	// "encoding/json"
	// "io"
	// "io/ioutil"
	// "log"
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

func ioMode(io int32, inout bool) {
	// // #ifdef MOCK
	if inout == IO_OUT {
		fmt.Printf("          Pin %v In/Out to OUT\n", io)
	} else {
		fmt.Printf("          Pin %v In/Out to IN\n", io)
	}
}

// // #else
// // 	char tmp[10];
// // 	char path[PATH_MAX];
// // 	sprintf(path, "%s/export", GPIO_ROOT);
// //
// // 	FILE* f = fopen(path, "wb");
// // 	if (f) {
// // 		sprintf(tmp, "%d", io);
// // 		fwrite(tmp, 1, strlen(tmp), f);
// // 		fclose(f);
// // 	} else {
// // 		ERR("Failed export io %d In/Out to %s\n", io, inout ? "In" : "Out");
// // 	}
// // 	sprintf(path, "%s/gpio%d/direction", GPIO_ROOT, io);
// // 	f = fopen(path, "wb");
// // 	if (f) {
// // 		sprintf(tmp, "%s", inout ? "in" : "out");
// // 		fwrite(tmp, 1, strlen(tmp), f);
// // 		fclose(f);
// // 	} else {
// // 		ERR("Failed to set direction on io %d In/Out to %s\n", io, inout ? "In" : "Out");
// // 	}
// // #endif
// }
//
func turnIoTo(io int32, hilow bool) {
	//
	// 	if (INVERT_GPIO[io]) {
	// 		hilow = !hilow;
	// 	}
	//
	// // #ifdef MOCK
	fmt.Printf("Pin %v set to %v\n", io, hilow)
	// // #else
	// // 	char tmp[10];
	// // 	char path[PATH_MAX];
	// // 	sprintf(path, "%s/gpio%d/value", GPIO_ROOT, io);
	// // 	FILE* f = fopen(path, "wb");
	// // 	if (f) {
	// // 		sprintf(tmp, "%s", hilow ? "1" : "0");
	// // 		fwrite(tmp, 1, strlen(tmp), f);
	// // 		fclose(f);
	// // 	} else {
	// // 		ERR("Failed to set output on io %d to %s\n", io, hilow ? "On" : "Off");
	// // 	}
	// // #endif
	//
}

//
func turnOffSeenControls() {
	for i := range seenGpios {
		turnIoTo(seenGpios[i], false)
	}
}

func initControlPoint(hs *ControlPoint) {
	seenGpios = append(seenGpios, hs.Io)
	ioMode(hs.Io, IO_OUT)
	turnIoTo(hs.Io, false)
	hs.lastUpdateOnOffTimes = millis()
	hs.dutyTimeOn = 0
	hs.dutyTimeOff = 0
	hs.duty = 0
	hs.On = false
	hs.ioState = false
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

	newHeatPinState = newHeatPinState && hs.On
	if newHeatPinState != hs.ioState {
		hs.ioState = newHeatPinState
		turnIoTo(hs.Io, hs.ioState)
	}
}

func setHeatOn(hs *ControlPoint, newState bool) {
	if hs.On != newState {
		hs.On = newState
		if newState {
			resetDutyState(hs)
		} else {
			updateForPinState(hs, false)
		}
	}
}

func updateForOverAmps(hs *ControlPoint) {
	updateForPinState(hs, false)
}

func updateIoForStateAndDuty(hs *ControlPoint) {
	newPinState := false
	if hs.On {
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
	} else {
		newPinState = false
	}
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
func UpdatePinsForSetDuty(state *State, maxAmps int32) {
	if state.Mode != MODE_OFF {
		currentAmps := int32(0)
		lockSteps()
		if len(state.Steps) > 0 {
			controlPoints := state.Steps[0].ControlPoints
			for i := range controlPoints {
        cp := &controlPoints[i]
				// setupControlPoint(cp);
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
			}
	}
		unlockSteps()
	}
	//DBG("***********  updatePinsForSetDuty - END *************** \n");
}
