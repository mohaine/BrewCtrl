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


func ComplexConfig1() (cfg Configuration) {
	SYS_PATH = "mock/sys/"
	cfg.Version = "ComplexConfig1"
	var pump1 Pump
	pump1.Io = 10
	pump1.Id = "pump1"
	cfg.BrewLayout.Pumps = append(cfg.BrewLayout.Pumps, pump1)
	var pump2 Pump
	pump2.Io = 11
	pump2.Id = "pump2"
	cfg.BrewLayout.Pumps = append(cfg.BrewLayout.Pumps, pump2)
	var tank1 Tank
	tank1.Heater.Io = 12
	tank1.Id = "tank1"
	cfg.BrewLayout.Tanks = append(cfg.BrewLayout.Tanks, tank1)
	var tank2 Tank
	tank2.Heater.Io = 13
	tank2.Id = "tank2"
	cfg.BrewLayout.Tanks = append(cfg.BrewLayout.Tanks, tank2)
	return
}

func ComplexConfig2() (cfg Configuration) {
	SYS_PATH = "mock/sys/"
	var pump1 Pump
	pump1.Io = 10
	pump1.Id = "Pump1"
	cfg.Version = "ComplexConfig2"
	cfg.BrewLayout.Pumps = append(cfg.BrewLayout.Pumps, pump1)
	var pump2 Pump
	pump2.Io = 17
	cfg.BrewLayout.Pumps = append(cfg.BrewLayout.Pumps, pump2)
	var tank1 Tank
	tank1.Heater.Io = 13
	tank1.Id = "tank1"
	cfg.BrewLayout.Tanks = append(cfg.BrewLayout.Tanks, tank1)
	var tank2 Tank
	tank2.Heater.Io = 12
	tank2.Id = "tank2"
	cfg.BrewLayout.Tanks = append(cfg.BrewLayout.Tanks, tank2)
	return
}

func TestNewCfg(t *testing.T) {
	cfg := ComplexConfig1()
	state := StateDefault(cfg)
	state.Mode = MODE_HOLD

	cfgNew := ComplexConfig2()
	expectStringToBe(t, state.ConfigurationVersion, cfg.Version)

	updateForNewConfiguration(&cfgNew,&state,&cfg)
	expectStringToNotBe(t, state.ConfigurationVersion, cfg.Version)

}
