/* -*- P4_16 -*- */
#include <core.p4>
#include <v1model.p4>

// The programming for DCNTrace

/*************************************************************************
*********************** H E A D E R S  ***********************************
*************************************************************************/

typedef bit<48> mac_addr_t;
typedef bit<32> ipv4_addr_t;

header ethernet_h {
    mac_addr_t dst_addr;
    mac_addr_t src_addr;
    bit<16> ether_type;
}

header ipv4_h {
    bit<4> version;
    bit<4> ihl;
    bit<8> diffserv;
    bit<16> total_len;
    bit<16> identification;
    bit<3> flags;
    bit<13> frag_offset;
    bit<8> ttl;
    bit<8> protocol;
    bit<16> hdr_checksum;
    ipv4_addr_t src_addr;
    ipv4_addr_t dst_addr;
}

header tcp_h {
    bit<16> src_port;
    bit<16> dst_port;
    bit<32> seq_no;
    bit<32> ack_no;
    bit<4> data_offset;
    bit<4> res;
    bit<8> flags;
    bit<16> window;
    bit<16> checksum;
    bit<16> urgent_ptr;
}

header udp_h {
    bit<16> src_port;
    bit<16> dst_port;
    bit<16> length;
    bit<16> checksum;
}

header int_option_t {   // len = 1B
    bit<16>    int_enable;
    bit<2>    type;
    // 0 = default; 
    // int probe; 1 = specific flow
    bit<6>    ttl;         
    // the remain ttl 
    bit<8>    int_num;
    bit<32>   path_id;
    bit<8>    control_id;
    bit<24>   trace_id;
}

header inthdr_t {   // len = 28B
    bit<8>    sw_id;
    bit<8>    ingress_port;
    bit<8>    egress_port;
    bit<8>    rsvd0;
    bit<48>   ingress_global_timestamp;
    bit<48>   egress_global_timestamp;
    bit<32>   enq_timestamp;
    bit<8>    rsvd1;
    bit<24>   enq_qdepth;
    bit<32>   deq_timedelta;
    bit<8>    rsvd2;
    bit<24>   deq_qdepth;
}

header dcntrace_t {
    bit<8>    sw_id;
    bit<8>    ingress_port;
    bit<8>    egress_port;
    bit<8>    rsvd0;
    bit<8>    action_id_1;
    bit<8>    action_id_2;
    bit<8>    action_id_3;
    bit<8>    action_id_4;
    bit<48>   ingress_global_timestamp;
    bit<48>   egress_global_timestamp;
}

header last_egress_global_timestamp_md_t {
    bit<48>   last_egress_global_timestamp;
}

header int_sampling_flag_md_t {
    bit<8>   int_sampling_flag;    // 0: sampling; 1: all
}

header int_number_md_t {
    bit<8>   int_num;    
}

header int_flag_t {
    bool isTraceEnable;
    bool isLastStep;
    bit<6> rsvd;
}

struct headers {
    ethernet_h      ethernet; 
    ipv4_h          ipv4;
    udp_h           udp;
    tcp_h           tcp;
    int_option_t    int_option;
    dcntrace_t        dcntrace;
    inthdr_t         inthdr_md;
}

struct metadata {
    last_egress_global_timestamp_md_t last_egress_global_timestamp_md;
    int_flag_t int_flag;
}

// register<bit<48>>(960) last_egress_global_timestamp;
// register<bit<48>>(960) T1_value;
register<bit<32>>(960) packet_count_list;

/*************************************************************************
*********************** P A R S E R  ***********************************
*************************************************************************/

#define ETHERTYPE_IPV4 0x0800
#define ETHERTYPE_ARP  0x0806
#define ETHERTYPE_VLAN 0x8100
#define ETHERTYPE_IPV6 0x86dd
#define ETHERTYPE_MPLS 0x8847
#define ETHERTYPE_PTP  0x88F7
#define ETHERTYPE_FCOE 0x8906
#define ETHERTYPE_ROCE 0x8915
#define ETHERTYPE_BFN  0x9000

#define IP_PROTOCOLS_ICMP   1
#define IP_PROTOCOLS_IGMP   2
#define IP_PROTOCOLS_IPV4   4
#define IP_PROTOCOLS_TRACE  5
#define IP_PROTOCOLS_TCP    6
#define IP_PROTOCOLS_UDP    17
#define IP_PROTOCOLS_IPV6   41
#define IP_PROTOCOLS_SRV6   43
#define IP_PROTOCOLS_GRE    47
#define IP_PROTOCOLS_ICMPV6 58

#define UDP_PORT_VXLAN  4789
#define UDP_PORT_ROCEV2 4791
#define UDP_PORT_GENV   6081
#define UDP_PORT_SFLOW  6343
#define UDP_PORT_MPLS   6635

#define GRE_PROTOCOLS_ERSPAN_TYPE_3 0x22EB
#define GRE_PROTOCOLS_NVGRE         0x6558
#define GRE_PROTOCOLS_IP            0x0800
#define GRE_PROTOCOLS_ERSPAN_TYPE_2 0x88BE

#define VLAN_DEPTH 2
#define MPLS_DEPTH 3

parser MyParser(packet_in pkt,
                out headers hdr,
                inout metadata meta,
                inout standard_metadata_t standard_metadata) {

    state start {
        transition parse_ethernet;
    }

    state parse_ethernet {
        pkt.extract(hdr.ethernet);
        transition select(hdr.ethernet.ether_type) {
            ETHERTYPE_IPV4: parse_ipv4;
            ETHERTYPE_ARP: parse_arp;
            default: accept;
        }
    }
    
    state parse_arp {
        transition accept;
    }

    state parse_ipv4 {
        pkt.extract(hdr.ipv4);
        transition select(hdr.ipv4.protocol) {
            IP_PROTOCOLS_ICMP: accept;
            IP_PROTOCOLS_TRACE: parse_trace;
            IP_PROTOCOLS_TCP: parse_tcp;
            IP_PROTOCOLS_UDP: parse_udp;
            default: accept;
        }
    }

    state parse_udp {
        pkt.extract(hdr.udp);
        transition select(hdr.udp.dst_port) {
            // 12345: parse_trace;            
            // default: accept;
            default: parse_trace;
        }
    }

    state parse_tcp {
        pkt.extract(hdr.tcp);
        transition select(hdr.tcp.dst_port) {
            // 12345: parse_trace;
            // default: accept;
            default: parse_trace;
        }
    }

    state parse_trace {
        pkt.extract(hdr.int_option);
        transition accept;
    }
}

/*************************************************************************
************   C H E C K S U M    V E R I F I C A T I O N   *************
*************************************************************************/

control MyVerifyChecksum(inout headers hdr, inout metadata meta) {   
    apply {  }
}

/*************************************************************************
**************  I N G R E S S   P R O C E S S I N G   *******************
*************************************************************************/

control MyIngress(inout headers hdr,
                  inout metadata meta,
                  inout standard_metadata_t standard_metadata) {
    action drop() {
        mark_to_drop(standard_metadata);
    }
    
    action update_trace_enable_flag() {
        meta.int_flag.isTraceEnable = true; // TODO: 增加其他可能性, 只拿出最后一跳的决策结果(如果table过多)
        meta.int_flag.isLastStep = (hdr.int_option.ttl == 0)? true : false; // TODO: last step, forward to other port!
        // meta.int_flag.isLastStep = true;
        hdr.int_option.ttl = hdr.int_option.ttl - 1;
        // Add an INT header to the packet
        hdr.int_option.int_num = hdr.int_option.int_num + 1;
        hdr.dcntrace.setValid();
    }

    action change_nxt_dst(bit<9> nxt_port) {
        standard_metadata.egress_spec = nxt_port;
    }
    table fwd {
        key = {
            hdr.ipv4.dst_addr : lpm;
        }
        actions = {
            change_nxt_dst;
            NoAction;
            drop;
        }
        default_action = drop();
    }

    table trace_fwd {
        actions = {
            change_nxt_dst;
            NoAction;
            drop;
        }
        default_action = drop();
    }

    action ttl_drop() {
        mark_to_drop(standard_metadata);
    }

    apply {
        if ( hdr.int_option.isValid() && hdr.int_option.int_enable == 0x1234 ) {
            update_trace_enable_flag();
        } else {
            meta.int_flag.isTraceEnable = false;
            meta.int_flag.isLastStep = false;
        }

        // 如果是Trace网包并且是最后一跳, 则转发到Trace端口
        if ( meta.int_flag.isTraceEnable && meta.int_flag.isLastStep ) {
            trace_fwd.apply();
        } else {
            fwd.apply();
        }


        // Update the TTL of IPv4
        hdr.ipv4.ttl = hdr.ipv4.ttl - 1;
        if (hdr.ipv4.ttl == 0) {
            ttl_drop();
        }
    }
}

/*************************************************************************
****************  E G R E S S   P R O C E S S I N G   *******************
*************************************************************************/

control MyEgress(inout headers hdr,
                 inout metadata meta,
                 inout standard_metadata_t standard_metadata) {  
    
    apply {

    }
}

/*************************************************************************
*************   C H E C K S U M    C O M P U T A T I O N   **************
*************************************************************************/

control MyComputeChecksum(inout headers hdr, inout metadata meta) {
    apply {
        update_checksum(
            hdr.ipv4.isValid(),
            { hdr.ipv4.version,
              hdr.ipv4.ihl,
              hdr.ipv4.diffserv,
              hdr.ipv4.total_len,
              hdr.ipv4.identification,
              hdr.ipv4.flags,
              hdr.ipv4.frag_offset,
              hdr.ipv4.ttl,
              hdr.ipv4.protocol,
              hdr.ipv4.src_addr,
              hdr.ipv4.dst_addr },
            hdr.ipv4.hdr_checksum,
            HashAlgorithm.csum16);
    }
}

/*************************************************************************
***********************  D E P A R S E R  *******************************
*************************************************************************/

control MyDeparser(packet_out packet, in headers hdr) {
    apply {
        packet.emit(hdr.ethernet);
        packet.emit(hdr.ipv4);
        packet.emit(hdr.udp);
        packet.emit(hdr.tcp);
        packet.emit(hdr.int_option);
        packet.emit(hdr.dcntrace);
        packet.emit(hdr.inthdr_md);
    }
}

/*************************************************************************
***********************  S W I T C H  *******************************
*************************************************************************/

V1Switch(
MyParser(),
MyVerifyChecksum(),
MyIngress(),
MyEgress(),
MyComputeChecksum(),
MyDeparser()
) main;
