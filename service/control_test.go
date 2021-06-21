package main

import (
	"fmt"
	"testing"
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

func ComplexConfig1() (cfg Configuration) {
	InitMockPath()
	cfg.Version = "ComplexConfig1"
	var tank1 Tank
	tank1.Heater.Io = 12
	tank1.Id = "tank1"
	cfg.BrewLayout.Tanks = append(cfg.BrewLayout.Tanks, tank1)
	var tank2 Tank
	tank2.Heater.Io = 13
	tank2.Id = "tank2"
	cfg.BrewLayout.Tanks = append(cfg.BrewLayout.Tanks, tank2)
	var pump1 Pump
	pump1.Io = 10
	pump1.Id = "pump1"
	cfg.BrewLayout.Pumps = append(cfg.BrewLayout.Pumps, pump1)
	var pump2 Pump
	pump2.Io = 11
	pump2.Id = "pump2"
	cfg.BrewLayout.Pumps = append(cfg.BrewLayout.Pumps, pump2)
	return
}

func ComplexConfig2() (cfg Configuration) {
	InitMockPath()
	cfg.Version = "ComplexConfig2"
	var tank1 Tank
	tank1.Heater.Io = 13
	tank1.Id = "tank1"
	cfg.BrewLayout.Tanks = append(cfg.BrewLayout.Tanks, tank1)
	var tank2 Tank
	tank2.Heater.Io = 12
	tank2.Id = "tank2"
	cfg.BrewLayout.Tanks = append(cfg.BrewLayout.Tanks, tank2)
	var pump1 Pump
	pump1.Io = 10
	pump1.Id = "Pump1"
	cfg.BrewLayout.Pumps = append(cfg.BrewLayout.Pumps, pump1)
	var pump2 Pump
	pump2.Io = 17
	pump2.Id = "pump2"
	cfg.BrewLayout.Pumps = append(cfg.BrewLayout.Pumps, pump2)
	return
}

func TestNewCfg(t *testing.T) {
	cfg := ComplexConfig1()
	cfgNew := ComplexConfig2()

	origVersion := cfg.Version

	state := StateDefault(cfg)
	state.Mode = MODE_HOLD

	expectStringToBe(t, origVersion, state.ConfigurationVersion)
	stepBefore := state.Steps[0]
	fmt.Printf("%v,%v,%v,%v\n", stepBefore.ControlPoints[0].Io, stepBefore.ControlPoints[1].Io, stepBefore.ControlPoints[2].Io, stepBefore.ControlPoints[3].Io)
	expectInt32ToBe(t, 12, stepBefore.ControlPoints[0].Io)
	expectInt32ToBe(t, 13, stepBefore.ControlPoints[1].Io)
	expectInt32ToBe(t, 10, stepBefore.ControlPoints[2].Io)
	expectInt32ToBe(t, 11, stepBefore.ControlPoints[3].Io)

	updateForNewConfiguration(&cfgNew, &state, &cfg)
	expectStringToNotBe(t, origVersion, state.ConfigurationVersion)

	stepAfter := state.Steps[0]
	fmt.Printf("%v,%v,%v,%v\n", stepAfter.ControlPoints[0].Io, stepAfter.ControlPoints[1].Io, stepAfter.ControlPoints[2].Io, stepAfter.ControlPoints[3].Io)
	expectInt32ToBe(t, 13, stepAfter.ControlPoints[0].Io)
	expectInt32ToBe(t, 12, stepAfter.ControlPoints[1].Io)
	expectInt32ToBe(t, 10, stepAfter.ControlPoints[2].Io)
	expectInt32ToBe(t, 17, stepAfter.ControlPoints[3].Io)

}
