#!/usr/bin/env python   

import sys
print "parsing pcap..."

#We import the package of scapy as A, so that in case python has a function with the same name as scapy, there is not a misunderstanding
import scapy.all as A

print "1"

name = sys.argv[1]

print "2: " + 'traces/' + name + '.pcap'

tmp = A.rdpcap('traces/' + name + '.pcap')

print "3"

f = file('traces/' + name + '.csv', 'w')

print "4"

import csv

print "5"

v = csv.writer(f)

print "6"

v.writerow(['Idx', 'Src', 'Dst', 'Load'])

print "7"

lost_pckts = 0
written_pckts = 0

print "converting..."
for idx, ii in enumerate(tmp):
    try:
        v.writerow([idx, ii.src, ii.dst, ii.load])
	written_pckts += 1
    except Exception as e:
	#if a line doesn't have one of the searched field, an exception is caught and the packet counted as discarded (for 		example: beacon frames)
	lost_pckts += 1

f.close()

print "Packets written: {0}, Packets lost: {1}".format(written_pckts, lost_pckts)

