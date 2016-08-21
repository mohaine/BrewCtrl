package pid

import (
	"fmt"
)

type Pid struct {
	Kp            float32
	Kd            float32
	Ki            float32
	MaxOutput     float32
	MinOutput     float32
	previousError float32
	integral      float32
	updateInt     byte
}

func CreatePid(Kp float32, Kd float32, Ki float32, MinOutput float32, MaxOutput float32) (pid Pid) {
	pid.Kp = Kp
	pid.Kd = Kd
	pid.Ki = Ki
	pid.MinOutput = MinOutput
	pid.MaxOutput = MaxOutput
	pid.previousError = 0
	pid.integral = 0
	return
}

func GetDuty(pid *Pid, targetTemp float32, currentTemp float32) (duty int32) {
	dt := float32(1.0)

	error := targetTemp - currentTemp

	// println(fmt.Sprintf("Error %v",error))
	// println(fmt.Sprintf("pid.previousError %v",pid.previousError))

	pid.integral = pid.integral + (error * dt * pid.Ki)

	println(fmt.Sprintf("*** pid.integral %v", pid.integral))

	if pid.integral > pid.MaxOutput {
		pid.integral = pid.MaxOutput
	} else if pid.integral < pid.MinOutput {
		pid.integral = pid.MinOutput
	}

	derivative := (error - pid.previousError) / dt
	newOutput := (pid.Kp * error) + (pid.integral) + (pid.Kd * derivative)
	pid.previousError = error

	if newOutput >= pid.MaxOutput {
		newOutput = pid.MaxOutput
	} else if newOutput <= pid.MinOutput {
		newOutput = pid.MinOutput
	}
	duty = int32(newOutput)
	return
}
