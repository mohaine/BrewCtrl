package main

import (
	"flag"
	"log"
	"github.com/mohaine/brewctrl/service"
)



func main() {
	mock := flag.Bool("mock", false, "Use Mock GPIO/Sensors")
	port := flag.Uint("port", 80, "Web Server Port")
	flag.Parse()

	log.Printf("mock: %v port: %v\n", *mock, *port)


	service.StartServer(*mock,*port)
}
