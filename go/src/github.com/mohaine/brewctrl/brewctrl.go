package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"github.com/mohaine/onewire"
	"log"
	"net/http"
	"strings"
	"time"
	// "bytes"
)

var SYS_PATH = "/sys/"
var CFG_FILE = "BrewControllerConfig.json"

func sendError(w http.ResponseWriter, msg string, code int) {
	http.Error(w, msg, code)
}

func main() {

	mock := flag.Bool("mock", false, "Use Mock GPIO/Sensors")
	port := flag.Uint("port", 80, "Web Server Port")
	flag.Parse()

	if *mock {
		SYS_PATH = "mock/sys/"
	}

	cfg, err := LoadCfg(CFG_FILE)
	if err != nil {
		fmt.Printf("Failed to load cfg File: %v\n", err)
		cfg, err = LoadCfg("BrewControllerConfig.json.dist")
		if err != nil {
			panic(err)
		}
	}
	readSensors, stopReading := onewire.SensorLoop(100*time.Millisecond, SYS_PATH+"bus/w1/devices/")
	stopControl, getState, getConfig, setMode, modifySteps, modifyConfig := ControlStuff(readSensors, cfg)

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
				// if err != nil {
				// 	sendError(w,fmt.Sprintf("Failed to parse steps:%v",err),http.StatusBadRequest)
				// 	return
				// }
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
				// if err != nil {
				// 	sendError(w,fmt.Sprintf("Failed to parse steps:%v",err),http.StatusBadRequest)
				// 	return
				// }
			}
		}

		state := getState()
		j, err := json.Marshal(state)
		if err != nil {
			fmt.Println("error:", err)
		}
		w.Header().Set("Content-Type", "application/json")
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
			fmt.Println("error:", err)
		}
		w.Header().Set("Content-Type", "application/json")
		w.Write(j)
	})
	http.HandleFunc("/brewctrl/", func(w http.ResponseWriter, r *http.Request) {
		http.ServeFile(w, r, "web/brewctrl/index.html")
	})
	http.Handle("/", http.FileServer(http.Dir("web/")))

	log.Fatal(http.ListenAndServe(fmt.Sprintf(":%v", *port), nil))

	time.Sleep(1 * time.Second)
	stopControl()
	stopReading()
	time.Sleep(100 * time.Millisecond)
}
