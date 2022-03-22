FROM p4lang/p4app:latest
LABEL authors="Chengjun Jia<chengjunjia1997@qq.com>"
# output the tag as jake/p4redis:v1
#  P4 with Redis

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
WORKDIR /root/redis/redis-3.2.8 
RUN make
RUN make install
# 3. 安装redis的python依赖
RUN pip install redis 
# Run Redis
RUN apt-get install -y dbus-user-session
RUN mkdir /var/run/redis
