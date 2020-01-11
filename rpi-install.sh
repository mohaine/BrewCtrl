#!/bin/bash

make rpi
mkdir -p /opt/brewctrl
cp -r brewctrl brewctrl.service web cfg.json.dist /opt/brewctrl/
chown -R pi /opt/brewctrl
ln -s /opt/brewctrl/brewctrl.service  /etc/systemd/system
systemctl enable brewctrl
systemctl restart brewctrl

while true; do
    read -p "Do you wish to boot to BrewCtrl Kiosk mode?" yn
    case $yn in
        [Yy]* ) sudo apt install -y chromium-browser unclutter lightdm lwm; cp -r .Xsession ~/; break;;
        [Nn]* ) break;;
        * ) echo "Please answer yes or no.";;
    esac
done

