import redis
import time
import os
import logging

class Analyzer:
    def __init__(self, redis_db_id_list):
        REDIS_PORT = 6390
        self.redis_db_list = []
        for db_id in redis_db_id_list:
            r = redis.Redis(unix_socket_path='/var/run/redis/redis-server.sock',port=REDIS_PORT,db=db_id)
            self.redis_db_list.append(r)
        logging.basicConfig(filename="./analyzer.log", format="%(asctime)s-%(levelname)s:%(message)s", level=logging.DEBUG, filemode="a")
        self.logger = logging.getLogger()

    def clear(self):
        for r in self.redis_db_list:
            r.flushdb() # r.flushall() # Flush the database store
            r.set('recv_trace_pkt_num', 0)

    def analyze(self):
        # Get the information from all databases and analyze them
        for r in self.redis_db_list:
            recv = r.get('recv_trace_pkt_num')
            self.logger.info('recv: %s' % recv)
    
    def add_fault_acl_rule(self):
        sw_id = 2
        with open("./switch%s.txt" % (sw_id), "w") as f:
            f.write('table_add MyIngress.ingress_acl drop 521 =>')
        os.system('sudo ../flow_table/simple_switch_CLI --thrift-port %d < ./switch%s.txt' % (int(sw_id)+9090,sw_id))

    def remvoe_fault_acl_rule(self):
        sw_id = 2
        with open("./switch%s.txt" % (sw_id), "w") as f:
            f.write('table_clear MyIngress.ingress_acl') # Clear the ACL table
        os.system('sudo ../flow_table/simple_switch_CLI --thrift-port %d < ./switch%s.txt' % (int(sw_id)+9090,sw_id))

    def run(self):
        while True:
            self.analyze()
            time.sleep(5)
            # self.add_fault_acl_rule()
            time.sleep(5)
            # self.remvoe_fault_acl_rule()

"""
init the database
"""
def database_init(r):    
    nodes_list = [1]*10
    spine_num = nodes_list[0]
    leaf_num = nodes_list[1]
    tor_num = nodes_list[2]
    pod_num = nodes_list[4]
    
    d={}
    t=0
    keys=[] 

    for i in range(pod_num*leaf_num):
        d[str(t)]=spine_num+tor_num
        t+=1
    
    for k,v in d.items():
        for i in range(v):
            keys.append(k+'-'+str(i+1))
    
    for key in keys:
        r.lpush(key,-1,-1)
        r.lpush(key,-1,-1)
        r.pexpire(key,100)