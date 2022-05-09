#!/usr/bin/env python
import argparse
import sys
import socket
import random
import struct
import time
import redis
import datetime
import logging

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

def main(src_ip, dst_ip):
    logger = logging.getLogger()
    logger.info("Program starts")

    iface = get_if()
    # source=get_if_hwaddr(iface)
    trace_header = '\x12\x34\x02\x00'
    data= '\x77\x77'*10
    pkt = IP(src=get_if_addr(iface), dst=dst_ip) / UDP(sport = 1212, dport=12345) / trace_header
    pkt = pkt / data
    total_packet = 0
    pkts = []
    while True:
        send(pkt, iface=iface, verbose=False)
        logger.info("Send packet %d" % total_packet)
        total_packet += 1
        time.sleep(sleep_time)

        
if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Packet sending')
    parser.add_argument('-s', '--src_ip', type=str, default="10.0.0.1")
    parser.add_argument('-d', '--dst_ip', type=str, default="10.0.0.5")
    parser.add_argument('-i', '--index', type=str, default="0")
    args = parser.parse_args()
    logging.basicConfig(filename="./trace_send_"+args.index+".log", format="%(asctime)s-%(levelname)s:%(message)s", level=logging.DEBUG, filemode="a")
    main(args.src_ip, args.dst_ip)
