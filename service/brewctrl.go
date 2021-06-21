package service

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"strings"
	"time"

	"github.com/mohaine/brewctrl/gpio"
	"github.com/mohaine/brewctrl/onewire"
)

func enableCors(w *http.ResponseWriter) {
	(*w).Header().Set("Access-Control-Allow-Origin", "*")
}

var CFG_FILE = "cfg.json"
var CFG_FILE_OLD = "BrewControllerConfig.json"

func sendError(w http.ResponseWriter, msg string, code int) {
	http.Error(w, msg, code)
}

func StartServer(mock bool, port uint) {


	cfg, err := LoadCfg(CFG_FILE)
	if err != nil {
		log.Printf("Failed to load cfg file: %v\n", err)
		cfg, err = LoadCfg(fmt.Sprintf("%s.dist", CFG_FILE))
		if err != nil {
			log.Printf("Failed to load cfg file: %v\n", err)
			cfg, err = LoadCfg(CFG_FILE_OLD)
			if err != nil {
				panic(err)
			}
		}
	}

	var readSensors func() []onewire.TempReading = nil
	var stopReading func() = nil
	var initIo func(int32) = nil
	var turnIoTo func(int32, bool) = nil

	if mock {
		readSensors, stopReading, initIo, turnIoTo = SensorLoopMock(100*time.Millisecond, cfg)
	} else {
		readSensors, stopReading = onewire.SensorLoop(100*time.Millisecond, "/sys/bus/w1/devices/")
		initIo = func(io int32) {
			gpio.IoMode(io, gpio.IO_OUT)
			gpio.TurnIoTo(io, false)
		}
		turnIoTo = func(io int32, inout bool) {
			gpio.TurnIoTo(io, inout)
		}
	}

	stopControl, getState, getConfig, setMode, modifySteps, modifyConfig := ControlStuff(readSensors, cfg, initIo, turnIoTo)

	hub := newHub()
	go hub.run(getState)

	http.HandleFunc("/ws", func(w http.ResponseWriter, r *http.Request) {
		serveWs(hub, w, r, getState)
	})

	http.HandleFunc("/cmd/status", func(w http.ResponseWriter, r *http.Request) {
		mode := r.FormValue("mode")
		if len(mode) > 0 {
			setMode(mode)
		}

		// Modify ALL steps (aka, this is the full new list)
		stepsParam := r.FormValue("steps")
		if len(stepsParam) > 0 {
			var steps []ControlStep
			dec := json.NewDecoder(strings.NewReader(stepsParam))
			err := dec.Decode(&steps)
			if err != nil {
				sendError(w, "Failed to parse steps", http.StatusBadRequest)
				return
			} else {
				modifySteps(StepModify{true, steps})
			}
		}

		// Modify existing steps
		modifyStepsParam := r.FormValue("modifySteps")
		if len(modifyStepsParam) > 0 {
			var steps []ControlStep
			dec := json.NewDecoder(strings.NewReader(modifyStepsParam))
			err := dec.Decode(&steps)
			if err != nil {
				sendError(w, "Failed to parse steps", http.StatusBadRequest)
				return
			} else {
				modifySteps(StepModify{false, steps})
			}
		}

		state := getState()
		j, err := json.Marshal(state)
		if err != nil {
			log.Println("error:", err)
		}
		w.Header().Set("Content-Type", "application/json")
		if mock {
			enableCors(&w)
		}
		w.Write(j)
	})
	http.HandleFunc("/cmd/configuration", func(w http.ResponseWriter, r *http.Request) {
		configurationParam := r.FormValue("configuration")
		if len(configurationParam) > 0 {
			var newCfg Configuration
			dec := json.NewDecoder(strings.NewReader(configurationParam))
			err := dec.Decode(&newCfg)
			if err != nil {
				sendError(w, "Failed to parse Configuration", http.StatusBadRequest)
				return
			} else {
				modifyConfig(newCfg)
				// if err != nil {
				// 	sendError(w,fmt.Sprintf("Failed to parse steps:%v",err),http.StatusBadRequest)
				// 	return
				// }
			}
		}

		cfg := getConfig()
		j, err := json.Marshal(cfg)
		if err != nil {
			log.Println("error:", err)
		}
		w.Header().Set("Content-Type", "application/json")
		if mock {
			enableCors(&w)
		}
		var out bytes.Buffer
		json.Indent(&out, j, "", "  ")

		w.Write(out.Bytes())
	})

	http.HandleFunc("/brewctrl/", func(w http.ResponseWriter, r *http.Request) {
		http.ServeFile(w, r, "web/index.html")
	})
	http.Handle("/", http.FileServer(http.Dir("web/")))

	log.Fatal(http.ListenAndServe(fmt.Sprintf(":%v", port), nil))

	time.Sleep(1 * time.Second)
	stopControl()
	stopReading()
	time.Sleep(100 * time.Millisecond)
}
