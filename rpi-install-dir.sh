#!/bin/bash

BUILD_DIR=build

rm -rf $BUILD_DIR

make rpi
mkdir -p ${BUILD_DIR}
cp -r brewctrl brewctrl.service web cfg.json.dist ${BUILD_DIR}/
# sudo chown -R pi ${BUILD_DIR}
# [ ! -f /etc/systemd/system/brewctrl.service ] sudo ln -s ${BUILD_DIR}/brewctrl.service  /etc/systemd/system
# sudo systemctl enable brewctrl
# sudo systemctl restart brewctrl

# while true; do
#     read -p "Do you wish to boot to BrewCtrl Kiosk mode?" yn
#     case $yn in
#         [Yy]* ) sudo apt install -y chromium-browser unclutter lightdm lwm; cp -r .Xsession ~/; break;;
#         [Nn]* ) break;;
#         * ) echo "Please answer yes or no.";;
#     esac
# done

