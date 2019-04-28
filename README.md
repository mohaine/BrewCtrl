# BrewCtrl
Software for automated brew mash control


To create an image:

1) Write base system image to SD Card (These directions are for RASPBIAN STRETCH LITE)
2) Boot to system
2) run  "sudo raspi-config"
   1) Setup WIFI if needed (Network Options)
   1) Change Hostname to brewctrl (Network Options)
   2) Enable SSH (Interface Options)
   3) Enable 1-Wiere (Interface Options)
   3) Enable Boot to desktop (Boot Options -> Desktop/ CLI -> Desktop Autologin)


   1) Update
      sudo apt update
   2) Install Configuration
      sudo apt install chromium-browser unclutter lightdm lwm



1) install go/npm for your Destktop OS
2) run "make rpi"
3) ./rpi-init.sh HOSTNAME
4) ./rpi-install.sh HOSTNAME
