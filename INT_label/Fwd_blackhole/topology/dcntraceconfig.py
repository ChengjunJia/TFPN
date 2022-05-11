import yaml
import os
class DCNTraceConfig:
    def __init__(self, config_file_path="config.yaml"):
        if config_file_path[0] != ".":
            current_path = os.path.dirname(os.path.realpath(__file__))
            config_file_path =  os.path.join(current_path, config_file_path)
        temp = yaml.load(open(config_file_path))
        self.redis_addr = temp["redis_sock_file_addr"]
        self.redis_port = temp["redis_port"]
        self.trace_send_db = temp["trace_send_db"]