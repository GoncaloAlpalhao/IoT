#!/bin/bash

# checks if wlan0 is connected to a network
if iwconfig wlan0 | grep -q "ESSID:\"\""; then
	echo "Wi-Fi not connected, starting AP mode."
        sudo systemctl start hostapd
        sudo systemctl start dnsmasq
        sudo python3 /home/pico/servidor.py &

else
	echo "Wi-Fi connected, no need to start AP mode"
	sudo systemctl stop hostapd
	sudo systemctl stop dnsmasq
fi
