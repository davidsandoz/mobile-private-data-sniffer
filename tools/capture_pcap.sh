#!/usr/bin/env bash

#this is just to get the same effect as doing right-click Network Manager and untick 'Enable Wireless'.
dbus-send --system --type=method_call --dest=org.freedesktop.NetworkManager /org/freedesktop/NetworkManager org.freedesktop.DBus.Properties.Set string:org.freedesktop.NetworkManager string:WirelessEnabled variant:boolean:false

#disabling wireless take time therefore we need to wait before continuing.
sleep 2

#Set up wireless card, interface $1, channel $2
cat .mypass | sudo -S iwconfig $1 mode monitor

sudo iwconfig $1 channel $2
sudo ifconfig $1 up

echo -e "\nStarting tshark ...\n"
pwd

#starting sniffing packages
touch traces/$4.pcap
chmod 777 traces/$4.pcap
sudo tshark -i $1 -a duration:$3 -f "tcp" -w traces/$4.pcap
sudo chown $USER traces/$4.pcap

#run the python script to convert the pcap file into csv format (readable text)
./pcap_to_csv.py $4
