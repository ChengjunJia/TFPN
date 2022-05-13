import redis
import time
import os
import logging
from threading import Timer
import random

def add_fault_acl_rule(sw_id=2):
    with open("./switch%s.txt" % (sw_id), "w") as f:
        f.write('table_add MyIngress.ingress_acl drop 521 =>')
    os.system('sudo ../flow_table/simple_switch_CLI --thrift-port %d < ./switch%s.txt' % (int(sw_id)+9090,sw_id))

def remove_fault_acl_rule(sw_id=2):
    with open("./switch%s.txt" % (sw_id), "w") as f:
        f.write('table_clear MyIngress.ingress_acl') # Clear the ACL table
    os.system('sudo ../flow_table/simple_switch_CLI --thrift-port %d < ./switch%s.txt' % (int(sw_id)+9090,sw_id))

class Analyzer:
    def __init__(self, redis_db_id_list):
        REDIS_PORT = 6390
        self.redis_db_list = []
        self.last_recv_num = []
        self.last_recv_time = []
        self.last_trigger_num = []
        for db_id in redis_db_id_list:
            r = redis.Redis(unix_socket_path='/var/run/redis/redis-server.sock',port=REDIS_PORT,db=db_id)
            self.redis_db_list.append(r)
            self.last_recv_num.append(0)
            self.last_recv_time.append(0)
            self.last_trigger_num.append(0)
        logging.basicConfig(filename="./analyzer.log", format="%(asctime)s-%(levelname)s:%(message)s", level=logging.DEBUG, filemode="a")
        self.logger = logging.getLogger()

    def clear(self):
        for r in self.redis_db_list:
            r.flushdb() # r.flushall() # Flush the database store
            r.set('recv_trace_pkt_num', 0)
            r.set('trigger_check_num', 0)

    # Periodically check the number of received trace packets
    def period_check(self):
        r = self.redis_db_list[0] # default, listen to the first database
        recv = r.get('recv_trace_pkt_num')
        recv = int(recv)
        self.logger.info("recv: %d" % recv)
        if recv > 0 and self.last_recv_num[0] == recv:
            return False
        self.last_recv_num[0] = recv
        return True

    # Trigger check
    def trigger_check(self):
        r = self.redis_db_list[0]
        trigger_num = r.get('trigger_check_num')
        trigger_num = int(trigger_num)
        if trigger_num > 0 and self.last_trigger_num[0] != trigger_num:
            self.logger.info("The trigger check num is %d" % trigger_num)
            self.last_trigger_num[0] = trigger_num
            return False
        return True

    def analyze(self):
        # Get the information from all databases and analyze them
        ret = self.period_check()
        if not ret:
            self.solve_bug()

    def solve_bug(self):
        self.logger.info("Find error now!")
        REDIS_PORT = 6390
        TRACE_SEND_DB = 15
        sendDB = redis.Redis(unix_socket_path='/var/run/redis/redis-server.sock',port=REDIS_PORT,db=TRACE_SEND_DB)
        sendDB.incr('check_all_path')
        self.logger.info("Command the senders to send out packets")
        now = time.time()
        while int(sendDB.get('check_all_path')) != 0 or float(sendDB.get('last_send_time')) < now:
            # self.logger.info("Still waiting...")
            continue
        # self.logger.info("The trace packets have been sent")
        now = time.time()
        trigger_send_time = float(sendDB.get('last_send_time'))
        if now < trigger_send_time + 0.07:
            time.sleep(trigger_send_time + 0.07 - now)
            # wait for 50ms to ensure the trigger packets reached
        r1 = self.redis_db_list[1]
        r2 = self.redis_db_list[2]
        recv1_num = int(r1.get('recv_trace_pkt_num'))
        recv2_num = int(r2.get('recv_trace_pkt_num'))
        if recv1_num == 0 and recv2_num == 0:
            remove_fault_acl_rule()
            self.logger.info("Get the error switch as 2")
        elif recv1_num == 0 and recv2_num != 0:
            remove_fault_acl_rule(sw_id=1)
            self.logger.info("Get the error switch as 1")
        else:
            remove_fault_acl_rule(sw_id=0)
            self.logger.info("Get the error switch as 0")
        r1.set('recv_trace_pkt_num', 0)
        r2.set('recv_trace_pkt_num', 0)
        # time.sleep(2)
        # exit(0)

    def run(self):
        # Always insert random fault rule
        # Timer(10, add_fault_acl_rule, (0, ) ).start()
        # Timer(13, add_fault_acl_rule, (2, ) ).start()
        for start_point in range(10, 200, 1):
            start_time = start_point * 0.5 + random.random() * 0.25
            fault_sw = random.randint(0, 2)
            Timer(start_time, add_fault_acl_rule, (fault_sw,)).start()
        while True:
            start = time.time()
            self.analyze()
            end = time.time()
            
            # Method 1: Trigger based
            PERIOD = 10
            if end - start < PERIOD:
                while time.time() < start + PERIOD:
                    ret = self.trigger_check()
                    if not ret:
                        self.solve_bug()
                        r = self.redis_db_list[0] 
                        trigger_num = r.get('trigger_check_num')
                        trigger_num = int(trigger_num)
                        self.last_trigger_num[0] = trigger_num


            # Method 2: Periodic check
            # PERIOD_CHECK = 5
            # if end - start < PERIOD_CHECK:
            #     time.sleep(PERIOD_CHECK - (end - start))

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