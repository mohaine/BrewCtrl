
pwd = $(shell pwd)

local:
	env GOPATH=$(pwd)/go go build github.com/mohaine/brewctrl
	yarn install
	yarn run build
	
rpi:
	env GOPATH=$(pwd)/go GOOS=linux GOARCH=arm GOARM=5 go build github.com/mohaine/brewctrl
	yarn install
	yarn run build
