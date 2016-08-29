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
	// "errors"
)

type StepModify struct {
	FullList bool
	Steps    []ControlStep
}

func ControlStuff(readSensors func() []onewire.TempReading, cfg Configuration) (stopControl func(), getState func() State, getCfg func() Configuration, setMode func(string), modifySteps func(StepModify)) {

	quit := make(chan int)
	stopControl = func() { quit <- 1 }
	tickPins := time.Tick(100 * time.Millisecond)
	tickDuty := time.Tick(1000 * time.Millisecond)
	tickUpdateTimes := time.Tick(250 * time.Millisecond)

	requestState := make(chan int)
	receiveState := make(chan State)
	getState = func() State {
		requestState <- 1
		state := <-receiveState
		return state
	}
	requestCfg := make(chan int)
	receiveCfg := make(chan Configuration)
	getCfg = func() Configuration {
		requestCfg <- 1
		cfg := <-receiveCfg
		return cfg
	}
	setModeC := make(chan string)
	setMode = func(mode string) {
		setModeC <- mode
	}

	modifyStepsC := make(chan StepModify)
	modifySteps = func(stepModify StepModify) {
		modifyStepsC <- stepModify
	}

	// TODO load old status?
	state := StateDefault(cfg)
	state.Mode = MODE_ON

	controlPoints := state.Steps[0].ControlPoints
	for i := range controlPoints {
		cp := &controlPoints[i]
		cp.Duty = 33
		cp.On = true
		cp.HasDuty = true
	}

	fmt.Println(state.ConfigurationVersion)
	loop := func() {
		for {
			select {
			case <-tickDuty:
				StateUpdateSensors(&state, readSensors())
				StateUpdateDuty(&state)
			case <-tickUpdateTimes:
				UpdateStepTimer(&state, &cfg)
			case <-tickPins:
				UpdatePinsForSetDuty(&state, cfg.BrewLayout.MaxAmps)
			case <-requestState:
				receiveState <- state
			case <-requestCfg:
				receiveCfg <- cfg
			case mode := <-setModeC:
				state.Mode = mode
				UpdateOnOff(&state, state.Mode != MODE_OFF);
			case stepModify := <-modifyStepsC:
				updateStateForSteps(stepModify, &state)
			case <-quit:
				return
			}
		}
	}
	go loop()
	return
}

func UpdateOnOff(state *State, on bool){
	if len(state.Steps) > 0 {
		controlPoints := state.Steps[0].ControlPoints
		for i := range controlPoints {
			cp := &controlPoints[i]
			if on != cp.On {
				resetDutyState(cp)
				cp.On = on
			}
		}
	}

}

func UpdateStepTimer(state *State, cfg *Configuration) {
	lockSteps()
	if len(state.Steps) > 0 {
		step := &state.Steps[0]
		if state.Mode == MODE_ON || state.Mode == MODE_HEAT_OFF {
			now := millis()
			if !step.Active {
				step.lastOnTime = now
				step.Active = true
			}
			stepTime := step.StepTime
			if stepTime > 0 {
				onTime := now - step.lastOnTime
				if onTime > 0 {
					for onTime > 1000 {
						newStepTime := stepTime - 1
						onTime -= 1000
						step.lastOnTime += 1000
						if newStepTime <= 0 {
							// Step is complete.  Go to next
							state.Steps = state.Steps[1:]
						} else {
							step.StepTime = newStepTime
						}
					}
				}
			}
		} else if state.Mode == MODE_HOLD || state.Mode == MODE_OFF {
			step.lastOnTime = 0
			step.Active = true
		}
	}
	if len(state.Steps) == 0 {
		state.Steps = append(state.Steps, StepDefault(*cfg))
	}
	unlockSteps()
}

func updateControlPoints(modPoints []ControlPoint, points []ControlPoint) {
	for i := range modPoints {
		modPoint := &modPoints[i]
		found := false
		for j := range points {
			point := &points[j]
			if point.Id == modPoint.Id {
				found = true
				copyControlPointDuty(point, modPoint)
				break
			}
		}
		if !found {
			initControlPointDuty(modPoint)
		}
	}
}

func updateStateForSteps(stepModify StepModify, state *State) {
	modSteps := stepModify.Steps
	// if first step is the same, overlay control point state
	if len(modSteps) > 0 && len(state.Steps) > 0 {
		modStep1 := &modSteps[0]
		step1 := &state.Steps[0]
		if step1.Id == modStep1.Id {
			updateControlPoints(modStep1.ControlPoints, step1.ControlPoints)
		}
	}
	if stepModify.FullList {
		state.Steps = modSteps
	} else {
		for i := range state.Steps {
			step := &state.Steps[i]
			for j := range modSteps {
				modStep := modSteps[j]
				if modStep.Id == step.Id {
					state.Steps[i] = modStep
				}
			}
		}
	}
}
