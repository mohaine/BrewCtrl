package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"github.com/mohaine/onewire"
	"log"
	"net/http"
	"time"
)

var SYS_PATH = "/sys/"

func main() {

	mock := flag.Bool("mock", false, "Use Mock GPIO/Sensors")
	port := flag.Uint("port", 80, "Web Server Port")
	flag.Parse()

	if *mock {
		SYS_PATH = "mock/sys/"
	}

	cfg, err := LoadCfg("BrewControllerConfig.json")
	if err != nil {
		fmt.Printf("Failed to load Cfg File: %v\n", err)
		cfg, err = LoadCfg("BrewControllerConfig.json.dist")
		if err != nil {
			panic(err)
		}
	}
	readSensors, stopReading := onewire.SensorLoop(100*time.Millisecond, SYS_PATH+"bus/w1/devices/")
	stopControl, getState, getConfig, setMode := ControlStuff(readSensors, cfg)

	http.HandleFunc("/cmd/status", func(w http.ResponseWriter, r *http.Request) {

		mode := r.FormValue("mode")
		if len(mode) > 0 {
			setMode(mode)
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
