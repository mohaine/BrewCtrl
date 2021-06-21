package service

import (
	"github.com/mohaine/brewctrl/pid"
)

var seenGpios = make([]int32, 0)

func turnOffSeenControls(turnIoTo func(int32, bool)) {
	for i := range seenGpios {
		turnIoTo(seenGpios[i], false)
	}
}

func initControlPointDuty(hs *ControlPoint, initIo func(int32)) {
	seenGpios = append(seenGpios, hs.Io)
	initIo(hs.Io)
	resetDutyState(hs)
	hs.duty = 0
	hs.ioState = false
}

func copyControlPointDuty(from *ControlPoint, to *ControlPoint) {
	to.lastUpdateOnOffTimes = from.lastUpdateOnOffTimes

	to.dutyTimeOn = from.dutyTimeOn
	to.dutyTimeOff = from.dutyTimeOff
	to.duty = from.duty
	to.pid = from.pid
	to.ioState = from.ioState
}

func resetDutyState(hs *ControlPoint) {
	pid.InitPid(&hs.pid, 78.2577, 269.7192, 0.9532, 0, 100)
	hs.dutyTimeOn = 0
	hs.dutyTimeOff = 0
	hs.lastUpdateOnOffTimes = millis()
}

func updateForPinState(hs *ControlPoint, newHeatPinState bool, turnIoTo func(int32, bool)) {
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
	if newHeatPinState != hs.ioState || !hs.ioStateKnow {
		hs.ioStateKnow = true
		hs.ioState = newHeatPinState
		turnIoTo(hs.Io, hs.ioState)
	}
}

func updateForOverAmps(hs *ControlPoint, turnIoTo func(int32, bool)) {
	updateForPinState(hs, false, turnIoTo)
}

func updateIoForStateAndDuty(hs *ControlPoint, turnIoTo func(int32, bool)) {
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
	updateForPinState(hs, newPinState, turnIoTo)
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
func initHardware(turnIoTo func(int32, bool)) {
	turnOffSeenControls(turnIoTo)
}

func UpdatePinsForSetDuty(cfg *Configuration, state *State, turnIoTo func(int32, bool)) {
	maxAmps := cfg.BrewLayout.MaxAmps
	currentAmps := int32(0)
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
					updateForOverAmps(cp, turnIoTo)
				} else if cp.HasDuty {
					setHeatDuty(cp, duty)
					updateIoForStateAndDuty(cp, turnIoTo)
				} else {
					updateForPinState(cp, duty > 0, turnIoTo)
				}
				if cp.ioState {
					currentAmps += cp.FullOnAmps
				}
			} else if cp.ioState {
				turnIoTo(cp.Io, false)
				cp.ioState = false
			}
		}
	}

	//DBG("***********  updatePinsForSetDuty - END *************** \n");
}
