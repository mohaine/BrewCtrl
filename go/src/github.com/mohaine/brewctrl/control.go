package main

import (
	"fmt"
	"github.com/mohaine/onewire"
	"github.com/mohaine/id"
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

func ControlStuff(readSensors func() []onewire.TempReading, cfg Configuration) (stopControl func(), getState func() State, getCfg func() Configuration, setMode func(string), modifySteps func(StepModify), modifyCfg  func(Configuration)) {

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

	requestModifyCfgC := make(chan Configuration)
	modifyCfg = func(cfg Configuration) {
		requestModifyCfgC <- cfg
	}

	// TODO load old status?
	state := StateDefault(cfg)
	state.Mode = MODE_OFF

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
				UpdatePinsForSetDuty(&cfg, &state)
			case <-requestState:
				receiveState <- state
			case <-requestCfg:
				receiveCfg <- cfg
			case mode := <-setModeC:
				state.Mode = mode
				UpdatePinsForSetDuty(&cfg, &state)
			case stepModify := <-modifyStepsC:
				updateStateForSteps(stepModify, &state)
			case newCfg := <-requestModifyCfgC:
				updateForNewConfiguration(&newCfg, &state, &cfg)
			case <-quit:
				return
			}
		}
	}
	go loop()
	return
}

func updateForNewConfiguration(newCfg *Configuration, state *State, cfg *Configuration) {
	fmt.Printf("%v %v %v", state)
	// TODO Turn off IOs that are no longer inuse
	// TODO Create new Control Points on all steps for new Ios
	// TODO Update target sensors

   cfg.Version = id.RandomId()
	 cfg.LogMessages = newCfg.LogMessages
	 cfg.BrewLayout = newCfg.BrewLayout
	 cfg.Sensors = newCfg.Sensors
	 cfg.StepLists = newCfg.StepLists

}


func UpdateStepTimer(state *State, cfg *Configuration) {
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
