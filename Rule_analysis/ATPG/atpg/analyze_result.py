#!/usr/bin/env python
'''
 <Analyze the results>

@author: Chengjun Jia
'''
from utils.load_stanford_backbone import *
from headerspace.applications import *
from headerspace.hs import *
from multiprocessing import Pool, cpu_count
from config_parser.cisco_router_parser import ciscoRouter
import random, time, sqlite3, os, json, socket, struct
from argparse import ArgumentParser

ntf_global = ""
ttf_global = ""
src_port_ids_global = set()
dst_port_ids_global = set()

DATABASE_FILE = "work/stanford-20.sqlite"
TABLE_NETWORK_RULES = "network_rules"
TABLE_TOPOLOGY_RULES = "topology_rules"
TABLE_RESULT_RULES = "result_rules"
TABLE_TEST_PACKETS = "test_packets"
TABLE_TEST_PACKETS_GLOBALLY_COMPRESSED = "test_packets_globally_compressed"
TABLE_TEST_PACKETS_LOCALLY_COMPRESSED = "test_packets_locally_compressed"

TABLE_SCRATCHPAD = "scratch_pad"
CPU_COUNT = cpu_count()

port_reverse_map_global = {}
port_map_global = {}

def main():
    # Load .tf files
    # ntf_global = load_stanford_backbone_ntf()
    # ttf_global = load_stanford_backbone_ttf()
    (port_map_global, port_reverse_map_global) = load_stanford_backbone_port_to_id_map()
    # Generate all ports
    src_port_ids_global = set()
    for rtr in port_map_global.keys():
        src_port_ids_global |= set(port_map_global[rtr].values())

    # Database information 
    conn = sqlite3.connect(DATABASE_FILE, 600)
    query = "SELECT * FROM %s " % (TABLE_TOPOLOGY_RULES)
    rows = conn.execute(query)
    topology_rules = []
    for row in rows:
        topology_rules.append(row)
    conn.close()
    print topology_rules
#    print port_map_global # str -> port_id
    for k in port_map_global:
        print len(port_map_global[k])


    # Table Information or format
    # conn.execute('CREATE TABLE %s (rule TEXT, input_port TEXT, output_port TEXT, action TEXT, file TEXT, line TEXT)' % TABLE_NETWORK_RULES)
    # conn.execute('CREATE TABLE %s (rule TEXT, input_port TEXT, output_port TEXT)' % TABLE_TOPOLOGY_RULES)
    # conn.execute('CREATE TABLE %s (header TEXT, input_port INTEGER, output_port INTEGER, ports TEXT, no_of_ports INTEGER, rules TEXT, no_of_rules INTEGER)' % TABLE_TEST_PACKETS)
    # conn.execute('CREATE TABLE %s (header TEXT, input_port INTEGER, output_port INTEGER, ports TEXT, no_of_ports INTEGER, rules TEXT, no_of_rules INTEGER)' % TABLE_TEST_PACKETS_LOCALLY_COMPRESSED)
    # conn.execute('CREATE TABLE %s (rules TEXT, no_of_rules INTEGER)' % TABLE_TEST_PACKETS_GLOBALLY_COMPRESSED)
    # conn.execute('CREATE TABLE %s (rule TEXT)' % TABLE_RESULT_RULES


if __name__ == "__main__":
    main()