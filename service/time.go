package service

import (
	"time"
)

var startTime = time.Now().UnixNano() / int64(time.Millisecond)

// func init() {
//   startTime := time.Now().UnixNano() / int64(time.Millisecond)
// }

func millis() uint64 {
	return uint64((time.Now().UnixNano() / int64(time.Millisecond)) - startTime)
}
