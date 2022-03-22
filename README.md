# TFPN

Test For Future Programmable Network, based on the [INT_LABEL](https://github.com/graytower/INT_LABEL) work

## INT_LABEL

运行INT_LABEL; INT_LABEL的整体架构逻辑为:

1. 每个switch按照一定概率向网包中插入INT信息(INT信息被插入越多的网包再被插入的概率越大)，以此实现INT信息收集的汇总。每个交换机的行为就是读取网包包头的INT信息(已经插入了多少INT信息)，然后决策是否插入(出入时间、队列长度等)。
2. 端侧/最后一跳交换机把信息收集出来并汇总到Redis数据库中。
3. 源码设计中通过单独一个脚本检测Redis数据库信息。

使用Dockerfile来配置相关环境，基于p4lang/p4app的官方BMv2镜像，安装Redis数据库并做相关配置(拷贝redis.conf文件以覆盖redis数据库配置)。注意在redis.conf配置中修改`unixsocket /var/run/redis/redis-server.sock`以和INT_LABEL原生的redis数据库sock一致；同时修改`daemonize yes`以开启redis默认后台运行。

构建自己的docker镜像, 使用命令

```sh
docker build -f redis.dockerfile -t jake/p4redis:v1 .
docker build -f int.dockerfile -t jake/intlabel:v1 .
```

进入docker的shell后，运行以下命令分别运行redis数据库、检测redis数据库和运行mininet测试(clos.py里面是详细的mininet网络配置)。

```sh
redis-server /etc/redis.conf
redis-cli config set notify-keyspace-events KEA
cd controller/
python coverage.py
python detect1.py
python detect2.py
cd topology/
python clos.py
```

**特别需要注意的是，运行容器的时候使用 `--privileged=true`配置，否则mininet会无法运行，卡死在Adding Host过程**
启动容器
` sudo docker container run --privileged=true --name fpnt -v /home/venus/NetworkTest/TFPN/INT_label/INT_label:/INT_label  -it jake/intlabel:v1 /bin/bash

- [x] 这里可以加上文件夹映射, 以方便程序进行调试

打开多个shell运行相关命令
` sudo docker exec -it ID /bin/bash

## 网络测试路径生成

- [] Rule_analysis目录下，存放已有的ATPG(CONEXT'12)和Pronto(ICDCS'17)工作；正在汇总补充AP-Keep和Mahjong工作。
- [x] 基于Mahjong工作, 完成测试路径生成(使用Pronto的BDD包头表示形式)，然后参考ATPG的算法去重(保证每个端口的单次覆盖和每条规则的单次覆盖即可);
- [x] 针对TFPN，我们使用增量方式生成待选的测试路径集。

## 其他相关论文

使用Tofino当做测试仪, HyperTester: High-performance Network Testing Driven by Programmable Switches.
