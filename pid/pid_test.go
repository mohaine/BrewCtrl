package pid

import (
	"fmt"
	"testing"
)

func expectToBe(t *testing.T, expected int32, acutal int32) {
	println(fmt.Sprintf("Expected %v, got %v", expected, acutal))
	if acutal != expected {
		t.Error(fmt.Sprintf("Expected %v, got %v", expected, acutal))
	}
}

func noError(t *testing.T, err error) {
	if err != nil {
		t.Error("Expected Success got error ", err)
	}
}

func TestPid(t *testing.T) {
	pid := CreatePid(78.2577, 269.7192, 0.9532, 0, 100)
	expectToBe(t, 100, GetDuty(&pid, 100, 0))
	expectToBe(t, 100, GetDuty(&pid, 100, 10))
	expectToBe(t, 100, GetDuty(&pid, 100, 20))
	expectToBe(t, 100, GetDuty(&pid, 100, 30))
	expectToBe(t, 100, GetDuty(&pid, 100, 40))
	expectToBe(t, 100, GetDuty(&pid, 100, 50))
	expectToBe(t, 100, GetDuty(&pid, 100, 60))
	expectToBe(t, 0, GetDuty(&pid, 100, 70))
	expectToBe(t, 0, GetDuty(&pid, 100, 80))
	expectToBe(t, 0, GetDuty(&pid, 100, 90))
	expectToBe(t, 0, GetDuty(&pid, 100, 100))
}
