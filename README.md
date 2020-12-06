# BrewCtrl
=============

Software for automated brew mash control
-------------

# Raspberry Pi Images

Strandard Raspberry Pi images can be downloaded from here:

https://www.graessle.net/beer/brewctrl/releases/

There are two flavors:

1) headless - Brings up web server UI only.  Meant for use only remotely via other devices on network.
2) kiosk - Same as above, but also boots directly into Chromium fullscreen with UI loaded.

The above are very simualar to the dafault Raspbian images.  For install instructions, please see:

https://www.raspberrypi.org/documentation/raspbian/


# To Develop on a Desktop Machine
    
    npm install
    npm start
    
    export GOPATH=$PWD/go
    go run github.com/mohaine/brewctrl --mock --port 2739

    load http://localhost:3000/ in your browser

# To view GPIO states in mock mode  

    python pinwatch.py 

