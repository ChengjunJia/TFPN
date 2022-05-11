FROM jake/p4redis:v1
LABEL authors="Chengjun Jia<chengjunjia1997@qq.com>"
# output the tag as jake/intlabel:v1

# 复制配置文件到容器中， 据说ADD命令比COPY好. 这里的redis.conf修改了daemon(默认背景运行)和unixsocket地址
COPY ./redis.conf /etc/redis.conf
# RUN redis-server /etc/redis.conf

# Install ovs
# RUN apt-get install -y openvswitch-switch
# RUN service openvswitch-switch start
RUN apt-get install -y python3-redis tcpreplay iperf3 iftop sysstat python-yaml python3-yaml

# Install INT label
# COPY INT_label /INT_label
# WORKDIR /INT_label
WORKDIR /
# RUN chmod +x /INT_label/flow_table/simple_switch_CLI
# RUN chmod +x /INT_label/init.sh

# RUN /usr/sbin/sshd -p 22
# EXPOSE 22
# # RUN python coverage.py &

# ENTRYPOINT ["./p4apprunner.py"]
ENTRYPOINT [""] 
# redis初始化(sh /INT_label/init.sh): 
# 1. redis-server /etc/redis.conf
# 2. redis-cli config set notify-keyspace-events KEA

# 注意运行容器的时候, 需要--privileged=true命令