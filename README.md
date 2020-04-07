# BrewCtrl
=============

Software for automated brew mash control
-------------

# To install to Raspberry Pi

Write base system image to SD Card (These directions are for RASPBIAN STRETCH LITE)

Boot to system

run  "sudo raspi-config"
   1) Setup WIFI if needed (Network Options)
   2) Change Hostname to brewctrl (Network Options)
   3) Enable SSH (Interface Options)
   4) Enable 1-Wire (Interface Options)


Build and install on Raspbian:

    sudo apt-get update && sudo apt-get install git -y
    git clone https://github.com/mohaine/BrewCtrl.git ~/brewctrl
    cd ~/brewctrl
    ./rpi-install.sh

If using a minimal install, and you would like to run in kiosk mode:

    apt-get install chromium-browser unclutter lightdm lwm
    cp .Xsession ~/



# To Develop on a Desktop Machine
    
    npm install
    
    export GOPATH=$PWD/go
    go run github.com/mohaine/brewctrl --mock --port 2739

    load http://localhost:3000/ in your browser



