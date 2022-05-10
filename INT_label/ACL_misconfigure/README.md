# ACL错误配置的仿真验证

使用DCNTrace程序检测ACL错误配置的仿真验证。
拓扑为:
 0 - 5 - 6 - 7 - 4
     1   2   3
其中主航道为0 - 5 - 6 - 7 - 4，收集Trace通过1 - 2 - 3
因此，0-5对5而言其port_id为1，而5-6对5而言其port_id为2，5-1对5而言其port_id为3，6和7同理。

我们错误配置一个ACL，使用4 -> 0的流量发生异常（控制在6或者7号交换机上）。
从4 -> 0 保持发送Trace探测网包；发现异常后，通知controller，controller然后命令trace_send发送各个endhost的trace网包，然后controller通过1、2、3的接收情况，判断哪一跳交换机错误；然后做更正。

controller收集信息的方法有两种，redis的pub/sub(主动地trigger)、周期性地收集。
redis沟通方法, 1、2、3分别产生一个redis数据库，controller同时订阅这三个数据库


