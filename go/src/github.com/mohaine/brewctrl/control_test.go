package main

import (
	"testing"
	// "fmt"
)

func TestUpdateStepTimer_ON(t *testing.T) {
	cfg := SimpleTestCfg()
	state := StateDefault(cfg)
	state.Mode = MODE_ON
	steps := state.Steps
	stepId := steps[0].Id
	steps[0].StepTime = 1
	startTime = startTime - 50
	UpdateStepTimer(&state, &cfg)
	startTime = startTime - 550
	UpdateStepTimer(&state, &cfg)
	startTime = startTime - 550
	UpdateStepTimer(&state, &cfg)
	expectStringToNotBe(t, state.Steps[0].Id, stepId)
}
func TestUpdateStepTimer_HEAT_OFF(t *testing.T) {
	cfg := SimpleTestCfg()
	state := StateDefault(cfg)
	state.Mode = MODE_HEAT_OFF
	steps := state.Steps
	stepId := steps[0].Id
	steps[0].StepTime = 1
	startTime = startTime - 50
	UpdateStepTimer(&state, &cfg)
	startTime = startTime - 550
	UpdateStepTimer(&state, &cfg)
	startTime = startTime - 550
	UpdateStepTimer(&state, &cfg)
	expectStringToNotBe(t, state.Steps[0].Id, stepId)
}
func TestUpdateStepTimer_OFF(t *testing.T) {
	cfg := SimpleTestCfg()
	state := StateDefault(cfg)
	state.Mode = MODE_OFF
	steps := state.Steps
	stepId := steps[0].Id
	steps[0].StepTime = 1
	startTime = startTime - 50
	UpdateStepTimer(&state, &cfg)
	startTime = startTime - 550
	UpdateStepTimer(&state, &cfg)
	startTime = startTime - 550
	UpdateStepTimer(&state, &cfg)
	expectStringToBe(t, state.Steps[0].Id, stepId)
}
func TestUpdateStepTimer_HOLD(t *testing.T) {
	cfg := SimpleTestCfg()
	state := StateDefault(cfg)
	state.Mode = MODE_HOLD
	steps := state.Steps
	stepId := steps[0].Id
	steps[0].StepTime = 1
	startTime = startTime - 50
	UpdateStepTimer(&state, &cfg)
	startTime = startTime - 550
	UpdateStepTimer(&state, &cfg)
	startTime = startTime - 550
	UpdateStepTimer(&state, &cfg)
	expectStringToBe(t, state.Steps[0].Id, stepId)
}
