PI_HOST=$1

echo install to $PI_HOST
ssh pi@$PI_HOST 'sudo mkdir -p /opt/brewctrl; sudo chown pi /opt/brewctrl'
scp -r brewctrl brewctrl.service web cfg.json.dist pi@$PI_HOST:/opt/brewctrl/
ssh pi@$PI_HOST 'sudo ln -s /opt/brewctrl/brewctrl.service  /etc/systemd/system; sudo systemctl enable brewctrl'
ssh pi@$PI_HOST 'sudo systemctl restart brewctrl'
