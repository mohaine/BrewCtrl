package onewire


import (
	"io/ioutil"
	"strconv"
	"strings"
	"time"
	"errors"
	"os"
)

type TempReading struct {
	TempMilliC int32
	Id string
}


func (tr *TempReading) TempC() float32 {
	return float32(tr.TempMilliC) / 1000.0
}

func parseTemp(w1SlaveData string) (temp TempReading, err error) {
	index := strings.LastIndex(w1SlaveData, " t=")
	if index > -1 && index + 3 < len(w1SlaveData) {
		tempString := strings.Trim(w1SlaveData[index+3 : ],"\n ")	  	
		var temp64 int64
		temp64, err = strconv.ParseInt(tempString, 10, 32)
		if err == nil {
			temp.TempMilliC = int32(temp64)
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


func SensorLoop(interval time.Duration, searchDir string) (read func()([]TempReading), quit func()) {
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
					w1FileName := searchDir + "/" + f.Name() + "/w1_slave"
					if _, err := os.Stat(w1FileName); err == nil {
						temp, err := readTempSensor(w1FileName)
						if err == nil {
							temp.Id = f.Name()
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
