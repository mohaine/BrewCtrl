package main

import (
	"fmt"
	"testing"
)

func expectBoolToBe(t *testing.T, expected bool, actual bool) {
	if actual != expected {
		t.Error(fmt.Sprintf("Expected %v, got %v", expected, actual))
	}
}
func expectInt32ToBe(t *testing.T, expected int32, actual int32) {
	if actual != expected {
		t.Error(fmt.Sprintf("Expected %v, got %v", expected, actual))
	}
}
func expectIntToBe(t *testing.T, expected int, actual int) {
	if actual != expected {
		t.Error(fmt.Sprintf("Expected %v, got %v", expected, actual))
	}
}
func expectStringToBe(t *testing.T, expected string, actual string) {
	if actual != expected {
		t.Error(fmt.Sprintf("Expected %v, got %v", expected, actual))
	}
}

func SimpleTestCfg() (cfg Configuration) {
	var pump Pump
	pump.Io = 10
	cfg.BrewLayout.Pumps = append(cfg.BrewLayout.Pumps, pump)
	return
}

func TestDefaultState(t *testing.T) {
	cfg := SimpleTestCfg()
	state := StateDefault(cfg)
	expectStringToBe(t, state.Mode, MODE_OFF)
	steps := state.Steps
	expectIntToBe(t, len(steps), 1)
	controlPoints := steps[0].ControlPoints
	expectIntToBe(t, len(controlPoints), 1)
	controlPoint := controlPoints[0]
	expectInt32ToBe(t, controlPoint.Io, cfg.BrewLayout.Pumps[0].Io)
	expectBoolToBe(t, controlPoint.HasDuty, false)
	expectInt32ToBe(t, controlPoint.FullOnAmps, 0)
}

func TestStateUpdateDuty(t *testing.T) {
	cfg := SimpleTestCfg()
	state := StateDefault(cfg)
	steps := state.Steps
	controlPoints := steps[0].ControlPoints
	pump := controlPoints[0]

	expectInt32ToBe(t, pump.Duty, 0)

	StateUpdateDuty(&state)

}
