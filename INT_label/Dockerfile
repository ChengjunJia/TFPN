FROM p4lang/p4app:latest
MAINTAINER Chengjun Jia <chengjunjia1997@qq.com>

# Install Redis
WORKDIR /root
# 1. 创建redis文件夹 
RUN mkdir redis      
WORKDIR /root/redis  
# 2.下载redis压缩包并安装
RUN apt-get update
RUN apt-get install software-properties-common -y
RUN apt-get install -y wget 
RUN wget http://download.redis.io/releases/redis-3.2.8.tar.gz 
RUN tar xvzf redis-3.2.8.tar.gz
WORKDIR ./redis-3.2.8 
RUN make
RUN make install
# 3. 安装redis的python依赖
RUN pip install redis 
# Run Redis
RUN apt-get install -y dbus-user-session
RUN mkdir /var/run/redis
# 复制配置文件到容器中， 据说ADD命令比COPY好. 这里的redis.conf修改了daemon(默认背景运行)和unixsocket地址
COPY ./redis.conf /etc/redis.conf
# RUN redis-server /etc/redis.conf

# Install ovs
# RUN apt-get install -y openvswitch-switch
# RUN service openvswitch-switch start

# Install INT label
COPY INT_label /INT_label
WORKDIR /INT_label/controller
# RUN python coverage.py &
WORKDIR /INT_label
RUN chmod +x /INT_label/flow_table/simple_switch_CLI

# ENTRYPOINT ["./p4apprunner.py"]
ENTRYPOINT [""] 
# redis初始化(sh /INT_label/init.sh): 
# 1. redis-server /etc/redis.conf
# 2. redis-cli config set notify-keyspace-events KEA

# 注意运行容器的时候, 需要--privileged=true命令