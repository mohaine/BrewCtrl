package id

import (
	"math/rand"
	"time"
)

const alpha = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789"

func init() {
	rand.Seed(time.Now().UTC().UnixNano())
}

func RandomId(params ...int) string {
	size := 10
	if len(params) > 0 {
		size = params[0]
	}
	buf := make([]byte, size)
	for i := 0; i < size; i++ {
		buf[i] = alpha[rand.Intn(len(alpha))]
	}
	return string(buf)
}
