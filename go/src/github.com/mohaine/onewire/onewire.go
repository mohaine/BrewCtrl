package onewire


import (
	"io/ioutil"
	"strconv"
	"strings"
	"time"
	"errors"
)

type TempReading struct {
	tempMilliC int32
	id string
}


func (tr *TempReading) tempC() float32 {
	return float32(tr.tempMilliC) / 1000.0
}

func parseTemp(w1SlaveData string) (temp TempReading, err error) {
	index := strings.LastIndex(w1SlaveData, " t=")
	if index > -1 {
		tempString := w1SlaveData[index+3 : index+8]
		var temp64 int64
		temp64, err = strconv.ParseInt(tempString, 10, 32)
		if err == nil {
			temp.tempMilliC = int32(temp64)
		}
	} else {
		err = errors.New("could not find t= in temp data")
	}
	return
}

func readTempSensor(filename string) (temp TempReading, err error) {
	var data []byte
	data, err = ioutil.ReadFile(filename)
	if err == nil {
		temp, err = parseTemp(string(data))
	}
	return
}

func SensorLoop(interval time.Duration) (read func()([]TempReading),quit func()) {
	return SensorLoopDir(interval, "/home/graessle/source/BrewCtrl/mock/sys/bus/w1/devices/")
}

func SensorLoopDir(interval time.Duration, searchDir string) (read func()([]TempReading), quit func()) {
	quitC := make(chan int)
	quit = func() { quitC <- 1 }
	tick := time.Tick(interval)

	currReadings := make([]TempReading, 0)
	read = func () ([]TempReading) {
		 now:= currReadings
	   readings := make([]TempReading, len(now))
		 copy(readings,now)
		 return readings
	 }


	loop := func() {
		for {
			select {
			case <-tick:
				files, _ := ioutil.ReadDir(searchDir)
				readings := make([]TempReading, 0, len(files))
				for _, f := range files {
					if f.IsDir() {
						w1FileName := searchDir + "/" + f.Name() + "/w1_slave"
						temp, err := readTempSensor(w1FileName)
						if err == nil {
							temp.id = f.Name()
							readings = append(readings, temp)
						}
					}
				}
				currReadings = readings
			case <-quitC:
				return
			}
		}
	}
	go loop()
	return
}
