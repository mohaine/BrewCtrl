PI_HOST=$1

echo install to $PI_HOST
ssh pi@$PI_HOST 'sudo apt update'
scp -r .Xsession pi@$PI_HOST:
ssh pi@$PI_HOST 'sudo apt install chromium-browser unclutter lightdm lwm'
