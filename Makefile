
pwd = $(shell pwd)

local:
	env GOPATH=$(pwd)/go go build github.com/mohaine/brewctrl



rpi:
	env GOPATH=$(pwd)/go GOOS=linux GOARCH=arm GOARM=5 go build github.com/mohaine/brewctrl
	npm install
	npm run build-prod
