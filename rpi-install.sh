make rpi
mkdir -p /opt/brewctrl
cp -r brewctrl brewctrl.service web cfg.json.dist /opt/brewctrl/
chown -R pi /opt/brewctrl
ln -s /opt/brewctrl/brewctrl.service  /etc/systemd/system
systemctl enable brewctrl
systemctl restart brewctrl

