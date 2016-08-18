package onewire

import (
	"fmt"
	"testing"
	"errors"
)
func expectErrorToBe(t *testing.T, expected error,err error) {
	if err.Error() != expected.Error() {
		t.Error(fmt.Sprintf("Expected '%v', got '%v'",expected, err))
	}
}
func expectTempToBe(t *testing.T, expected int32, temp *TempReading,err error) {
	noError(t, err)
	if temp.tempMilliC != expected {
		t.Error( fmt.Sprintf("Expected %v, got ",expected), temp.tempMilliC)
	}
}
func expectTempCToBe(t *testing.T, expected float32, temp *TempReading,err error) {
	noError(t, err)
	if temp.tempC() != expected {
		t.Error( fmt.Sprintf("Expected %v, got ",expected), temp.tempC())
	}
}

func noError(t *testing.T, err error) {
	if err != nil {
		t.Error("Expected Success got error ", err)
	}
}

func TestReadFile(t *testing.T) {
	var temp TempReading
	var err error

	temp, err = parseTemp("this should work t=22222")
	expectTempToBe(t,22222,&temp,err)

	temp, err = parseTemp("this should work t=11111X")
	expectTempToBe(t,11111,&temp,err)
	expectTempCToBe(t,11.111,&temp,err)

	temp, err = parseTemp("this should not work t11111X")

	fmt.Println(err)

	expectErrorToBe(t,err,errors.New("could not find t= in temp data"))

}
