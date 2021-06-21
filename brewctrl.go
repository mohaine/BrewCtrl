package main

import (
	"flag"
	"github.com/mohaine/brewctrl/service"
	"log"
)

func main() {
	mock := flag.Bool("mock", false, "Use Mock GPIO/Sensors")
	port := flag.Uint("port", 80, "Web Server Port")
	flag.Parse()

	log.Printf("mock: %v port: %v\n", *mock, *port)

	service.StartServer(*mock, *port)
}
