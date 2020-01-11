# BrewCtrl
Software for automated brew mash control


To create an image:

1) Write base system image to SD Card (These directions are for RASPBIAN STRETCH LITE)
2) Boot to system
3) run  "sudo raspi-config"
   1) Setup WIFI if needed (Network Options)
   2) Change Hostname to brewctrl (Network Options)
   3) Enable SSH (Interface Options)
   4) Enable 1-Wire (Interface Options)

4) If using a minimal install, and you would like to run in kiosk mode
   1) apt-get install chromium-browser unclutter lightdm lwm
   2) copy .Xsession to you home directory

5) Complete install
	bash -e `curl https://raw.githubusercontent.com/mohaine/BrewCtrl/master/easy.sh`






