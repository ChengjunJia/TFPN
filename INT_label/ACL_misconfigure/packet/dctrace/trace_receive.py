import redis
import os
import struct
import datetime
import logging
import argparse

from scapy.all import get_if_addr
from scapy.all import sniff, wrpcap
import scapy.all as scapy

def get_packet(pkt):
    # Get the IP
    logger = logging.getLogger()
    logger.info("The packet is %s" % pkt.show(dump=True))
    wrpcap("trace_receive.pcap", pkt, append=True)

def trace_listen():
    # Get the IP
    logger = logging.getLogger()
    if_list = scapy.get_if_list()
    logger.info("The interface list is %s" % (if_list))
    if len(if_list) == 1:
        iface = if_list[0]
    elif len(if_list) == 2:
        assert("lo" in if_list)
        iface = if_list[1 - if_list.index("lo")]
    else:
        logger.error("interfaces are too many (>2).")
    ip = get_if_addr(iface)
    logger.info("IP address is %s" % ip)

    sniff(filter="", iface=iface, prn=get_packet)
    

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Packet sending')
    parser.add_argument('-i', '--index', type=str, default="0")
    args = parser.parse_args()
    logging.basicConfig(filename="./trace_receive_"+args.index+".log", format="%(asctime)s-%(levelname)s:%(message)s", level=logging.DEBUG, filemode="a")
    trace_listen()