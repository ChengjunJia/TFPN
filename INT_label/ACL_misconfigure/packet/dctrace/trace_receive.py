import redis
import os
import struct
import time
import logging
import argparse
from threading import Timer

from scapy.all import get_if_addr
from scapy.all import sniff, wrpcap
import scapy.all as scapy

db = None
trigger_event = None
CHECK_THREAD_INTERVAL = 0.1

def trigger_check():
    global db, trigger_event
    db.incr("trigger_check_num")
    if trigger_event is not None:
        trigger_event.cancel()
    trigger_event = Timer(CHECK_THREAD_INTERVAL, trigger_check)
    trigger_event.start()

def get_packet(pkt):
    # Get the IP
    global db, trigger_event
    logger = logging.getLogger()
    logger.info("recv a trace packet")
    # logger.info("The packet is %s" % pkt.show(dump=True))
    # wrpcap("trace_receive.pcap", pkt, append=True)
    db.incr("recv_trace_pkt_num") # recv a trace packet
    # Analyze the packet information: with IP, port and others as the key, and the traceID(time, trace_info) as the value
    # TODO: add the record of file
    db.set("last_recv_time", str(time.time()))
    # logger.info("last_recv_time: %f" % float(db.get("last_recv_time")))
    if trigger_event is not None:
        # logger.info("Cancel the event")
        trigger_event.cancel()
    # logger.info("Start a new event")
    trigger_event = Timer(CHECK_THREAD_INTERVAL, trigger_check)
    trigger_event.start()
    # logger.info("recv finishes")



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
    # TODO: move the config file to a separate file
    REDIS_PORT = 6390
    db = redis.Redis(unix_socket_path='/var/run/redis/redis-server.sock',port=REDIS_PORT, db=args.index)
    trace_listen()