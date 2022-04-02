import socket
import redis
import os
import struct
import datetime

from scapy.all import get_if_addr
# time_out=1*10 #2*20ms

class parse():

    def filter(self, pkt_raw): #filter int packet
        pkt_len = len(pkt_raw)
        pkt = struct.unpack("!14s%ds" % (pkt_len-14), pkt_raw)
        ethernet = self.parse_ethernet(pkt[0])
        if ethernet[2] == 1793:
            pkt = struct.unpack("!2s%ds" % (pkt_len-14-2), pkt[1])
            int_option = self.parse_int_option(pkt[0])
            if int_option[0] == 0:
                data = self.int_process(pkt_raw)
                return data
        else:
            return False

class receive():
    def listen_speed(self):
        print("Program starts...")
        s = socket.socket(socket.PF_PACKET, socket.SOCK_RAW, socket.htons(0x0003))
        # r = redis.Redis(unix_socket_path='/var/run/redis/redis-server.sock',port=6390)       # aging database
        # r2 = redis.Redis(unix_socket_path='/var/run/redis/redis-server.sock',port=6390,db=1) # persist database
        # r4 = redis.Redis(unix_socket_path='/var/run/redis/redis-server.sock',port=6390,db=3) # data database
        # src_ip = get_if_addr("eth0")
        # parse1 = parse.parse()
        last_time = datetime.datetime.now()
        total_bytes = 0
        while True:
            data = s.recv(2048)
            if not data:
                print ("Client has exist")
                continue         
            # rs = parse1.filter(data)    # []: without INT data; False: not data packet
            total_bytes += len(data)
            now_time = datetime.datetime.now()
            diff_duration = now_time - last_time
            if diff_duration.seconds >= 2:
                total_microseconds = diff_duration.seconds *1e6 + diff_duration.microseconds
                throughput_MBps = total_bytes / total_microseconds
                print("The throughput is %.2f MBps\n" % (throughput_MBps))
                with open("./recv.log", "a") as f:
                    f.write("New throughput as %.2f Mbps and bytes %d/%d\n" % (throughput_MBps*8, total_bytes, total_bytes/len(data)))
                last_time = datetime.datetime.now()
                total_bytes = 0
            # rs= dip,dmac,port1,port2,port3,delta_time
            # print(rs)

        s.close()


if __name__ == "__main__":
    print("Start the receive")
    receive1 = receive()
    receive1.listen_speed()
