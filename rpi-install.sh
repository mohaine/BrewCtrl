#!/bin/bash
make rpi
sudo mkdir -p /opt/brewctrl
sudo cp -r brewctrl brewctrl.service web cfg.json.dist /opt/brewctrl/
sudo chown -R pi /opt/brewctrl
[ ! -f /etc/systemd/system/brewctrl.service ] sudo ln -s /opt/brewctrl/brewctrl.service  /etc/systemd/system
sudo systemctl enable brewctrl
sudo systemctl restart brewctrl

while true; do
    read -p "Do you wish to boot to BrewCtrl Kiosk mode?" yn
    case $yn in
        [Yy]* ) sudo apt install -y chromium-browser unclutter lightdm lwm; cp -r .Xsession ~/; break;;
        [Nn]* ) break;;
        * ) echo "Please answer yes or no.";;
    esac
done

