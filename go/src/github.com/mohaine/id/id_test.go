package id

import (
	"fmt"
	"testing"
)

func expectToBe(t *testing.T, expected string, acutal string) {
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
	id := RandomId()
	if len(id) != 10 {
		t.Error("Default length not 10", len(id))
	}
	id = RandomId(20)
	if len(id) != 20 {
		t.Error("Specific length not 20", len(id))
	}

}
