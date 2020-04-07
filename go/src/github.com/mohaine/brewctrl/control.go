package main

import (
	// "fmt"
	"github.com/mohaine/id"
	"github.com/mohaine/onewire"
	"time"
	// "encoding/json"
	"log"
	// "bytes"
	// "os"
	// "errors"
)

type StepModify struct {
	FullList bool
	Steps    []ControlStep
}

func ControlStuff(readSensors func() []onewire.TempReading, cfg Configuration, initIo func(int32), turnIoTo func(int32, bool)) (stopControl func(), getState func() State, getCfg func() Configuration, setMode func(string), modifySteps func(StepModify), modifyCfg func(Configuration)) {

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
	state := StateDefault(cfg, initIo)
	state.Mode = MODE_OFF

	loop := func() {
		for {
			select {
			case <-tickDuty:
				sensors := readSensors()
				StateUpdateSensors(&state, sensors)
				SelectReadingSensors(&cfg, &state, sensors)
				StateUpdateDuty(&state)
			case <-tickUpdateTimes:
				UpdateStepTimer(&state, &cfg, initIo)
			case <-tickPins:
				UpdatePinsForSetDuty(&cfg, &state, turnIoTo)
			case <-requestState:
				receiveState <- state
			case <-requestCfg:
				receiveCfg <- cfg
			case mode := <-setModeC:
				state.Mode = mode
				UpdatePinsForSetDuty(&cfg, &state, turnIoTo)
			case stepModify := <-modifyStepsC:
				updateStateForSteps(stepModify, &state, initIo)
			case newCfg := <-requestModifyCfgC:
				updateForNewConfiguration(&newCfg, &state, &cfg, initIo, turnIoTo)
			case <-quit:
				return
			}
		}
	}
	go loop()
	return
}

func SelectReadingSensors(cfg *Configuration, state *State, sensorReadings []onewire.TempReading) {
	configDirty := false
	// Add to cfg list
	for i := range sensorReadings {
		reading := sensorReadings[i]
		found := false
		for j := range cfg.Sensors {
			cSensor := cfg.Sensors[j]
			if cSensor.Address == reading.Id {
				found = true
				break
			}
		}

		if !found {
			log.Printf("Add Sensor: %v", reading.Id)
			var cSensor SensorConfig
			cSensor.Address = reading.Id
			cfg.Sensors = append(cfg.Sensors, cSensor)
			configDirty = true
		}
	}

	// Update Tank selected Sensors
	tanks := cfg.BrewLayout.Tanks
	for i := range tanks {
		tank := &tanks[i]
		if len(tank.Sensor.Address) > 0 {
			found := false
			for j := range sensorReadings {
				reading := sensorReadings[j]
				if tank.Sensor.Address == reading.Id {
					found = true
					break
				}
			}
			if found {
				// Sensor for this tank is being read. Make sure it is still for this tank
				for j := range cfg.Sensors {
					cSensor := cfg.Sensors[j]
					if cSensor.Address == tank.Sensor.Address {
						if cSensor.Location != tank.Name {
							log.Printf("Clear Tank Sensor %v for tank %v", tank.Sensor.Address, tank.Name)
							tank.Sensor.Address = ""
							configDirty = true
							ChangeSpepControlSensorTo(state, tank.Heater.Io, "")
						}
					}
				}
			} else {
				tank.Sensor.Address = ""
				configDirty = true
			}
		}

		if len(tank.Sensor.Address) == 0 {
			for j := range cfg.Sensors {
				cSensor := cfg.Sensors[j]
				if cSensor.Location == tank.Name {
					for k := range sensorReadings {
						reading := sensorReadings[k]
						if cSensor.Address == reading.Id {
							log.Printf("Select Tank Sensor %v for tank %v", cSensor.Address, tank.Name)
							tank.Sensor.Address = cSensor.Address
							// Change Control Points As well
							ChangeSpepControlSensorTo(state, tank.Heater.Io, cSensor.Address)
							configDirty = true
							break
						}
					}
				}
			}
		}
	}
	if configDirty {
		cfg.Version = id.RandomId()
		state.ConfigurationVersion = cfg.Version
		WriteConfiguration(cfg)
	}
}

func ChangeSpepControlSensorTo(state *State, io int32, address string) {
	for l := range state.Steps {
		step := &state.Steps[l]
		for m := range step.ControlPoints {
			cp := &step.ControlPoints[m]
			if cp.AutomaticControl && cp.Io == io {
				cp.SensorAddress = address
			}
		}
	}
}

func updateForNewConfiguration(newCfg *Configuration, state *State, cfg *Configuration, initIo func(int32), turnIoTo func(int32, bool)) {
	// TODO Update target sensors

	IdEverything(newCfg)

	oldMap := IoToOwnerIdMap(cfg)
	newMap := IoToOwnerIdMap(newCfg)

	if newCfg.Version == cfg.Version {
		cfg.Version = id.RandomId()
	} else {
		cfg.Version = newCfg.Version
	}
	state.ConfigurationVersion = cfg.Version
	cfg.LogMessages = newCfg.LogMessages
	cfg.BrewLayout = newCfg.BrewLayout
	cfg.Sensors = newCfg.Sensors
	cfg.StepLists = newCfg.StepLists

	modifiedIos := false
	for k := range oldMap {
		if newMap[k] != oldMap[k] {
			modifiedIos = true
		}
	}
	for k := range newMap {
		if newMap[k] != oldMap[k] {
			modifiedIos = true
		}
	}
	if modifiedIos {
		// Moved an IO. Shouldn't happen while ON, if it does, clear everything
		turnOffSeenControls(turnIoTo)
		state.Steps = state.Steps[:0]
		SetToStateDefault(*cfg, state, initIo)
	}
	WriteConfiguration(cfg)
}

func UpdateStepTimer(state *State, cfg *Configuration, initIo func(int32)) {
	if len(state.Steps) > 0 {
		step := &state.Steps[0]
		if state.Mode == MODE_ON || state.Mode == MODE_HEAT_OFF {
			// fmt.Printf("WaitForTargetTemp: %v pastTargetTemp: %v\n", step.WaitForTargetTemp, step.pastTargetTemp)
			if !step.WaitForTargetTemp || step.pastTargetTemp {
				// Active step counting down
				now := millis()
				if !step.Active {
					step.lastOnTime = now
					step.Active = true
					step.pastTargetTemp = false					
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
								//TODO FIX ME
							} else {
								step.StepTime = newStepTime
							}
						}
					}
				}
			} else {
				// Check for pastTargetTemp
				allAboveTarget := true
				for m := range step.ControlPoints {
					cp := &step.ControlPoints[m]
					if cp.AutomaticControl {
						cpAboveTarget := false
						sensor := FindSensor(state, cp.SensorAddress)
						if sensor != NilSensor {
							cpAboveTarget = sensor.TemperatureC >= cp.TargetTemp
						}
						allAboveTarget = allAboveTarget && cpAboveTarget
					}
					step.pastTargetTemp = allAboveTarget
					if allAboveTarget {
						UpdateStepTimer(state, cfg, initIo)
					}
				}
			}

		} else if state.Mode == MODE_HOLD || state.Mode == MODE_OFF {
			step.lastOnTime = 0
			step.Active = true
		}
	}
	if len(state.Steps) == 0 {
		state.Steps = append(state.Steps, StepDefault(*cfg, initIo))
	}
}

func updateControlPoints(modPoints []ControlPoint, points []ControlPoint, initIo func(int32)) {
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
			initControlPointDuty(modPoint, initIo)
		}
	}
}

func updateStateForSteps(stepModify StepModify, state *State, initIo func(int32)) {
	modSteps := stepModify.Steps
	// if first step is the same, overlay control point state
	if len(modSteps) > 0 && len(state.Steps) > 0 {
		modStep1 := &modSteps[0]
		step1 := &state.Steps[0]
		if step1.Id == modStep1.Id {
			updateControlPoints(modStep1.ControlPoints, step1.ControlPoints, initIo)
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
