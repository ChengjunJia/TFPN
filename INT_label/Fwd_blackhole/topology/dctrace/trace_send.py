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
import os

from scapy.all import sendp, send, get_if_list, get_if_hwaddr, get_if_addr, conf
from scapy.all import Packet
from scapy.all import Ether, IP, UDP, TCP
current_path = os.path.dirname(os.path.realpath(__file__))
config_file_path =  os.path.join(current_path, "..")
sys.path.append(config_file_path)
from dcntraceconfig import DCNTraceConfig

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
        # get_if_addr(iface) # Get the IP address
    return iface

def get_pkt(src_ip, dst_ip, trace_len=1, path_id = 10, control_id = 1, trace_id = 2):
    trace_enable = '\x12\x34' 
    assert(trace_len < (1<<6))
    assert(path_id < (1<<32))
    assert(control_id < (1<<8))
    assert(trace_id < (1<<24))
    trace_header1 = trace_enable+chr(trace_len)+'\x00'
    trace_header_path_id = struct.pack("!I", path_id) # 4B path id
    traec_header_control_id_trace_id = struct.pack("!I", (control_id << 24) + trace_id)
    data= '\x77\x77'*10

    mac_id = 5 + 1024
    hexValue = hex(mac_id) + "00000000000000"
    srcMac = "00:00:00:%s:%s:%s" % (hexValue[2:4], hexValue[4:6], hexValue[6:8])
    mac_id = 1 + 1024
    hexValue = hex(mac_id) + "00000000000000"
    dstMac = "00:00:00:%s:%s:%s" % (hexValue[2:4], hexValue[4:6], hexValue[6:8])

    pkt = Ether(src=srcMac, dst=dstMac) / IP(src=src_ip, dst=dst_ip) / UDP(sport = 1212, dport=521) 
    pkt = pkt / trace_header1 / trace_header_path_id / traec_header_control_id_trace_id 
    pkt = pkt / data
    return pkt

def main(src_ip, dst_ip):
    logger = logging.getLogger()
    logger.info("Program starts")

    iface = get_if()
    # source=get_if_hwaddr(iface)
    
    total_packet = 0
    s = conf.L2socket(iface=iface)
    
    logger.info("Start to send the packet")
    # Clear the sent trace
    dcnConfig = DCNTraceConfig()
    REDIS_PORT = dcnConfig.redis_port
    REDIS_ADDR = dcnConfig.redis_addr
    TRACE_SEND_DB = dcnConfig.trace_send_db
    db = redis.Redis(unix_socket_path=REDIS_ADDR, port=REDIS_PORT, db=TRACE_SEND_DB)
    db.set("send_trace_pkt_num", 0)
    db.set("last_send_time", time.time())
    db.set("check_all_path", 0)
    trace_interval = 0.05
    db.set("send_interval", trace_interval)

    default_pkt = get_pkt(src_ip, dst_ip)
    while True:
        s.send(default_pkt)
        db.incr("send_trace_pkt_num") # send a trace packet
        db.set("last_send_time", time.time())
        t1 = time.time()
        while time.time() - t1 < trace_interval:
            if int(db.get("check_all_path")) > 0:
                logger.info("Check all path is set and we start to check all")
                db.decr("check_all_path")
                for i in range(0, 32):
                    pkt = get_pkt(src_ip, dst_ip, i)
                    s.send(pkt)
                db.set("last_send_time", time.time())
                break
        now = time.time()
        if now < t1 + trace_interval:
            time.sleep(t1 + trace_interval - now)
        total_packet += 1
        logger.info("Send packet %d" % total_packet)
        # time.sleep(sleep_time)
        
if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Packet sending')
    parser.add_argument('-s', '--src_ip', type=str, default="10.0.0.5")
    parser.add_argument('-d', '--dst_ip', type=str, default="10.0.0.1")
    parser.add_argument('-i', '--index', type=str, default="0")
    args = parser.parse_args()
    logging.basicConfig(filename="./trace_send_"+args.index+".log", format="%(asctime)s-%(levelname)s:%(message)s", level=logging.DEBUG, filemode="a")
    main(args.src_ip, args.dst_ip)
