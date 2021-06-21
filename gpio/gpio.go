package gpio

import (
	"io/ioutil"
	"log"
	"fmt"
)

const IO_OUT = false
const IO_IN = true
var SYS_PATH = "/sys/"

func gpioRoot() string {
	return SYS_PATH + "class/gpio"
}

func IoMode(io int32, inout bool) {
	// // #ifdef MOCK
	direction := "out"
	if inout != IO_OUT {
		direction = "in"
	}
	// fmt.Printf("  Pin %v In/Out to %v\n", io, direction)
	// Export pin
	path := fmt.Sprintf("%v/export", gpioRoot())
	ioutil.WriteFile(path, []byte(fmt.Sprintf("%v", io)), 0644)
	// These fail if called twice
	// if err != nil {
	// 	log.Panic("Failed export io %v In/Out to %v\n", io, direction)
	// }

	// Set Direction
	path = fmt.Sprintf("%v/gpio%v/direction", gpioRoot(), io)
	ioutil.WriteFile(path, []byte(direction), 0644)
	// if err != nil {
	// 	log.Panic("Failed to set direction on io %v In/Out to %v\n", io, direction)
	// }
}

//
func TurnIoTo(io int32, hilow bool) {
	// if (INVERT_GPIO[io]) {
	// 	hilow = !hilow;
	// }

	// fmt.Printf("Pin %v set to %v\n", io, hilow)
	// // #else
	oneZero := "0"
	if hilow {
		oneZero = "1"
	}
	path := fmt.Sprintf("%v/gpio%v/value", gpioRoot(), io)
	err := ioutil.WriteFile(path, []byte(oneZero), 0644)
	if err != nil {
		onOff := "off"
		if hilow {
			onOff = "on"
		}
		log.Printf(fmt.Sprintf("Failed to set output on io %v to %v (%v): %v\n", io, onOff, path, err))
	}
}
