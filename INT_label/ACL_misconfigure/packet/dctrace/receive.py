import socket
import redis
import os
import struct
import datetime

from scapy.all import get_if_addr

class receive():
    def listen_speed(self):
        print("Program starts...")
        with open("./recv.log", "a") as f:
            f.write("Program starts at %s\n" % (datetime.datetime.now()))
        s = socket.socket(socket.PF_PACKET, socket.SOCK_RAW, socket.htons(0x0003))
        last_time = datetime.datetime.now()
        total_bytes = 0
        while True:
            data = s.recv(2048)
            if not data:
                print ("Client has exist")
                continue         
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
    # receive1 = receive()
    # receive1.listen_speed()
