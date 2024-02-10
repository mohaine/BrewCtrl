
pwd = $(shell pwd)

local:
	go build brewctrl.go
	yarn install
	yarn run build
	
rpi:
	env GOOS=linux GOARCH=arm GOARM=5 go build brewctrl.go
	yarn install
	yarn run build


clean:
	rm -rf build