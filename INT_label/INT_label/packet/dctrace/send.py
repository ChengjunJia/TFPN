#!/usr/bin/env python
import argparse
import sys
import socket
import random
import struct
import time
import redis
import datetime

from scapy.all import sendp, send, get_if_list, get_if_hwaddr, get_if_addr
from scapy.all import Packet
from scapy.all import Ether, IP, UDP, TCP

sleep_time=0

def get_if():
    # ifs=get_if_list()
    iface=None # "h1-eth0"
    for i in get_if_list():
        if "eth0" in i:
            iface=i
            break
    if not iface:
        print("Cannot find eth0 interface")
        exit(1)
    return iface

def main():

    iface = get_if()
    source=get_if_hwaddr(iface)
    
    data= '\x77\x77'*400
    
    r4 = redis.Redis(unix_socket_path='/var/run/redis/redis-server.sock',port=6390,db=3) # data database
    

    pkt =  Ether(src=source, dst='00:00:00:01:01:02', type=1792)
    pkt = pkt / '\x00\x00' / '\x80\x04' / '\x07\x01' / '\x00\x00'
    pkt = pkt / data

    last_time = datetime.datetime.now()
    total_bytes = 0
    while True:
        # r4.incr('send')
        # r4.set('send',int(r4.get('send'))+1)
        batch = 800
        sendp(pkt, iface=iface, verbose=False, count=batch)
        total_bytes += len(pkt) * batch
        now_time = datetime.datetime.now()
        diff_duration = now_time - last_time
        if diff_duration.seconds >= 2:
            total_microseconds = diff_duration.seconds *1e6 + diff_duration.microseconds
            throughput_MBps = total_bytes / total_microseconds
            print("The throughput is %.2f MBps\n" % (throughput_MBps))
            with open("./send.log", "a") as f:
                f.write("New throughput as %.2f Mbps and bytes %d/%d\n" % (throughput_MBps*8, total_bytes, total_bytes/len(pkt)))
            last_time = datetime.datetime.now()
            total_bytes = 0
        
if __name__ == '__main__':
    main()
