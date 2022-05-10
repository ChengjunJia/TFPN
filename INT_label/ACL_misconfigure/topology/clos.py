from mininet.net import Mininet
from mininet.topo import Topo
from mininet.log import setLogLevel, info
from mininet.cli import CLI
from mininet.link import TCLink
from p4_mininet import P4Switch, P4Host
from p4_table_gen import p4_table
from multiprocessing import Process

import argparse
import time
import os
from controller import Analyzer

os.system("sudo mn -c")

parser = argparse.ArgumentParser(description='CLOS architecture topology')
parser.add_argument('--behavioral-exe', help='Path to behavioral executable',
                    type=str, action="store", default="/usr/local/bin/simple_switch") # /home/ng/behavioral-model/targets/simple_switch/simple_switch
parser.add_argument('--thrift-port', help='Thrift server port for table updates',
                    type=int, action="store", default=9090)
parser.add_argument('--json', help='Path to JSON config file',
                    type=str, action="store", default="../p4_source_code/int_test.json")
parser.add_argument('--topo-file', help='Path to topology file',
                    type=str, action="store", required=False, default="./topo.txt")
parser.add_argument('--pcap-dump', help='Dump packets on interfaces to pcap files',
                    type=str, action="store", required=False, default=False)
args = parser.parse_args()

TIME_OUT=2*10 #2*20ms
Loss=0 # %
BW=100
MAX_Q_SIZE=10*1000

class clos(Topo):

    def __init__(self, behavioral_exe, thrift_port, json, topo_file, pcap_dump, **opts):

        Topo.__init__(self, **opts)

        # Store the P4Switch and P4Host
        self.switch_list = []
        self.host_list = []
        
        # Store the sw_id and link
        self.swID_list = []
        self.hostID_list = []
        self.link_list = []
        
        # Store the map of sw_id->switch_list; host_id->
        self.swID2P4ID = {}
        self.hostID2P4ID = {}
        self.swID2ThriftPort = {} # the map from swID to the thriftPort
        self.id2P4 = {}
        self.hostID2IP = {}
        self.hostID2Mac = {}

        device_id = 0
        with open(topo_file, "r") as f:
            # 1. read the parameter number
            _items = f.readline().strip().split(",")
            _items = [int(n) for n in _items]
            total_node_num, sw_num, link_num = _items[0], _items[1], _items[2]
            # 2. read the switches
            _items = f.readline().strip().split(",")
            _items = [int(n) for n in _items]
            assert(len(_items) == sw_num) # Check the switch number
            self.swID_list = _items
            for sw_id in self.swID_list:
                sw = self.addSwitch('s%d' % (sw_id),
                                    sw_path=behavioral_exe,
                                    json_path=json,
                                    thrift_port=thrift_port,
                                    nanolog="ipc:///tmp/bm-%d-log.ipc" % sw_id,
                                    device_id=device_id,
                                    pcap_dump=pcap_dump)
                self.switch_list.append(sw)
                self.swID2P4ID[sw_id] = device_id
                self.swID2ThriftPort[sw_id] = thrift_port
                self.id2P4[sw_id] = sw
                thrift_port += 1
                device_id += 1
            # 3. init the hosts
            p4host_id_no = 0
            for host_id in xrange(total_node_num):
                if host_id in self.swID_list:
                    continue
                self.hostID_list.append(host_id)
                ip_addr=  host_id + 1 # 0 is not allowed
                ipAddr = "10.%d.%d.%d" % ( (ip_addr>>16) & 0xFF, (ip_addr>>8) & 0xFF, ip_addr & 0xFF)
                mac_id = ip_addr + 1024
                hexValue = hex(mac_id) + "00000000000000"
                macAddr = "00:00:00:%s:%s:%s" % (hexValue[2:4], hexValue[4:6], hexValue[6:8])
                h = self.addHost('h%d' % (host_id), ip=ipAddr, mac=macAddr)
                self.host_list.append(h)
                self.hostID2P4ID[host_id] = p4host_id_no
                self.id2P4[host_id] = h
                self.hostID2IP[host_id] = ipAddr
                self.hostID2Mac[host_id] = macAddr
                p4host_id_no  += 1
            # 4. add links
            for i in xrange(link_num):
                _items = f.readline().strip().split(",")
                _items = [int(n) for n in _items]
                src_id, dst_id = _items[0], _items[1]
                src, dst = self.id2P4[src_id], self.id2P4[dst_id]
                self.addLink(src, dst,loss=Loss,bw=BW,max_queue_size=MAX_Q_SIZE)
            # Manual links for test
            # for pair in [(0,1), (1,2), (2,3)]:
            #     src_id, dst_id = pair[0], pair[1]
            #     src, dst = self.id2P4[src_id], self.id2P4[dst_id]
            #     self.addLink(src, dst, loss=Loss, bw=BW, max_queue_size=MAX_Q_SIZE)

def main():
    os.system('sh ../p4_source_code/run.sh')
    # os.system("rm *.pcap")
    # TODO: compress the log and archive them
    os.system('tar -zcvf ./result/arch/log-%s.tar.gz *.log' % (time.strftime("%Y-%m-%d-%H-%M-%S", time.localtime())))
    os.system('rm *.log')
    
    process_list = []
    analyzer = Analyzer([1, 2, 3])
    analyzer.clear()
    p1 = Process(target=analyzer.run)
    process_list.append(p1)

    topo = clos(args.behavioral_exe,
                args.thrift_port,
                args.json,
                args.topo_file,
                args.pcap_dump)
    net = Mininet(topo=topo,
                  host=P4Host,
                  switch=P4Switch,
                  controller=None,
                  link=TCLink)
    net.start()
    info("mininet starts...\n")

    # Place the switch cmd
    # cmd_file_path = "../flow_table/topo_direct_command.sh"
    # os.system("sh "+cmd_file_path)
    cmd_file_path = "../flow_table/flow_place_command.sh"
    table_gen = p4_table()
    table_gen.p4_table_gen(cmd_file_path, topo)
    os.system("sh "+cmd_file_path)
    info("switch commands are placed...\n")
    
    net.staticArp()
    # Place the server cmd
    
    h0 = net.get(topo.id2P4[0])
    h4 = net.get(topo.id2P4[4])

    # Generate the Traffic
    # h0 --> h4, iperf traffic running (max speed is about 100Mbps, so we limit 30M to ensure that there is no queue at the switch)
    # PORT1 = 520
    # PORT2 = 521 # The traffic of 521 will be dropped by misconfigured ACL
    # h0.cmd('iperf -s -p %d &' % PORT1)
    # h0.cmd('iperf -s -p %d &' % PORT2)
    # h0.cmd('tcpdump -i eth0 -w h0_eth0.pcap &')
    # h4.cmd('iperf -c 10.0.0.1 -u -b 25M -t 10 -i 0.5 -p %d > ./h4_iperf_c1.log &' % PORT1)
    # h4.cmd('iperf -c 10.0.0.1 -u -b 15M -t 10 -i 0.5 -p %d > ./h4_iperf_c2.log &' % PORT2)
    h4.cmd('tcpreplay -i eth0 -l 100 -M 40 ./h0_eth0.pcap &') # replay the traffic
    h0.cmd('sar -n DEV 1 100 > ./h0_sar.log &')

    h4.cmd("python3 ../packet/dctrace/trace_send.py -i 0 &")
    for i in range(1,4):
        h = net.get(topo.id2P4[i])
        h.cmd("python3 ../packet/dctrace/trace_receive.py -i %d &" % i)

    print("Start the Testing...")

    for p in process_list:
        p.start()

    CLI(net)
    time.sleep(10)
    net.stop()

    for p in process_list:
        p.join()
    # h0_popen.terminate()
    # h4_popen.terminate()
    return 0

if __name__ == '__main__':
    setLogLevel('info')
    info("Start...\n")
    main()
