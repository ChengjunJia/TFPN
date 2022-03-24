/* -*- P4_16 -*- */
#include <core.p4>
#include <v1model.p4>

// The programming for DCNTrace

/*************************************************************************
*********************** H E A D E R S  ***********************************
*************************************************************************/

header ethernet_t {  
    bit<48>   dstAddr;
    bit<48>   srcAddr;
}

header type_fwd_t {
    bit<16>   type;    // 0x1234:sr; else:find route table
}

header sr_hdr_t {
    bit<1>    next_hdr;
    bit<7>    rsvd;
    bit<8>    sr_length;
}

header sr_t {   
    bit<1>    type;     // 0:not last sr; 1:last sr
    bit<7>    rsvd;
    bit<8>    next_port;
}

header type_int_t {   
    bit<16>   type;     // 0x701:int; 0x800:ipv4
}

header int_option_t {   // len = 1B
    bit<2>    type;     
    // 0 = int probe; 1 = specific flow
    bit<6>    ttl;         
    // the remain ttl 
    bit<8>    int_num;         
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
    bit<32>   rule_id_1;
    bit<32>   rule_id_2;
    bit<32>   rule_id_3;
    bit<32>   rule_id_4;
    bit<48>   ingress_global_timestamp;
    bit<48>   egress_global_timestamp;
    bit<32>   pkt_count;
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
    ethernet_t      ethernet; 
    type_fwd_t[1]     type_fwd;
    sr_hdr_t[1]     sr_hdr;
    sr_t[10]         sr;
    type_int_t        type_int;
    int_option_t    int_option;
    dcntrace_t        inthdr;
}

struct metadata {
    last_egress_global_timestamp_md_t last_egress_global_timestamp_md;
    int_number_md_t int_num_md;
    int_flag_t int_flag;
}

// register<bit<48>>(960) last_egress_global_timestamp;
// register<bit<48>>(960) T1_value;
register<bit<32>>(960) packet_count_list;

/*************************************************************************
*********************** P A R S E R  ***********************************
*************************************************************************/

parser MyParser(packet_in packet,
                out headers hdr,
                inout metadata meta,
                inout standard_metadata_t standard_metadata) {

    state start {
        transition parse_ethernet;
    }

    state parse_ethernet {
        packet.extract(hdr.ethernet);
        transition parse_type_fwd;
    }

    state parse_type_fwd {
        packet.extract(hdr.type_fwd[0]);
        transition select(hdr.type_fwd[0].type) {
            0x1234: parse_sr_hdr;    
            default: accept;
        }
        
    }

    state parse_sr_hdr {
        packet.extract(hdr.sr_hdr[0]);
        transition parse_sr;
    }

    state parse_sr {
        packet.extract(hdr.sr.next);
        transition select(hdr.sr.last.type) {
            0: parse_sr;
            1: parse_type_int;
        }
    }

    state parse_type_int {
        packet.extract(hdr.type_int);
        transition select(hdr.type_int.type) {
            0x701: parse_int_option;
            0x800: accept;
        }
    }

    state parse_int_option {
        packet.extract(hdr.int_option);
        meta.int_num_md.int_num=hdr.int_option.int_num;
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

    action do_sr_final() {
        hdr.type_fwd.pop_front(1);
        hdr.sr_hdr.pop_front(1);
        hdr.inthdr.action_id_1 = meta.int_flag.isTraceEnable? 8w1: 8w0;
    }
    
    action do_sr() {
        standard_metadata.egress_spec = (bit<9>)hdr.sr[0].next_port;
        hdr.sr.pop_front(1);
        hdr.inthdr.action_id_2 = meta.int_flag.isTraceEnable? 8w2: 8w0;
    }
    
    action update_trace_enable_flag() {
        meta.int_flag.isTraceEnable = true; // TODO: 增加其他可能性, 只拿出最后一跳的决策结果(如果table过多)
        meta.int_flag.isLastStep = (hdr.int_option.ttl == 1)? true : false; // TODO: last step, forward to other port!
        hdr.int_option.ttl = hdr.int_option.ttl - 1;
        hdr.inthdr.setValid();
    }
    
    action change_nxt_dst(bit<9> nxt_port) {
        standard_metadata.egress_spec = nxt_port;
    }
    table manual_modify {
        actions = {
            change_nxt_dst;
            NoAction;
        }
        default_action = NoAction();
    }

    apply {
        if ( hdr.int_option.isValid() ) {
            update_trace_enable_flag();
        }
        if ( hdr.sr_hdr[0].isValid() ) {
            if( hdr.sr[0].type==1) {
                do_sr_final();
            }
            do_sr();
        }
        manual_modify.apply();
    }
}

/*************************************************************************
****************  E G R E S S   P R O C E S S I N G   *******************
*************************************************************************/

control MyEgress(inout headers hdr,
                 inout metadata meta,
                 inout standard_metadata_t standard_metadata) {  
    action drop() {
        mark_to_drop(standard_metadata);
    }  

    action do_int(bit<8> sw_id) {

        hdr.inthdr.sw_id = sw_id;
        hdr.inthdr.ingress_port = (bit<8>)standard_metadata.ingress_port;
        hdr.inthdr.egress_port = (bit<8>)standard_metadata.egress_port;
        hdr.inthdr.ingress_global_timestamp=(bit<48>)standard_metadata.ingress_global_timestamp;
        hdr.inthdr.egress_global_timestamp=(bit<48>)standard_metadata.egress_global_timestamp;
        
        hdr.int_option.int_num = hdr.int_option.int_num + 1;
    }

    table int_table {
        actions = {
            do_int;
            NoAction;
        }
        default_action = NoAction();
    }

    action action1(bit<32> rule_id) {
        hdr.inthdr.action_id_4 = meta.int_flag.isTraceEnable? 8w4: 8w0;
        hdr.inthdr.rule_id_4 = rule_id;
    }

    action drop1() {
        hdr.inthdr.action_id_3 = meta.int_flag.isTraceEnable? 8w3: 8w0;
        mark_to_drop(standard_metadata);
    }

    table egress_table1 {
        
        actions = {
            action1;
            drop1;
            NoAction;
        }
        default_action = NoAction();
    }

    
    apply {
        if (hdr.int_option.isValid()) {
            int_table.apply();
            packet_count_list.read( hdr.inthdr.pkt_count, (bit<32>)32w0);
            hdr.inthdr.pkt_count = hdr.inthdr.pkt_count + 1;
            packet_count_list.write( (bit<32>)32w0, hdr.inthdr.pkt_count);
        }
        egress_table1.apply();

        // else{
        //     bit<48> T=50000;
        //     bit<48> T1=T*0/100;
        //     T1_value.write(1, (bit <48>) T1);
        //     bit<48> MAX_hop=5; // for fattree topology
        //     bit<48> a=1; // weights for P_num
        //     bit<48> b=0; // weights for P_time
        //     last_egress_global_timestamp.read(meta.last_egress_global_timestamp_md.last_egress_global_timestamp, (bit<32>)standard_metadata.egress_port);
        //     if(standard_metadata.egress_global_timestamp - meta.last_egress_global_timestamp_md.last_egress_global_timestamp >T)
        //     {
        //         int_table_sampling.apply();
        //         last_egress_global_timestamp.write((bit<32>)standard_metadata.egress_port, standard_metadata.egress_global_timestamp);
        //     }
        //     else if(standard_metadata.egress_global_timestamp - meta.last_egress_global_timestamp_md.last_egress_global_timestamp >T1){
        //         bit<8> int_num_val=meta.int_num_md.int_num;
        //         if(int_num_val>3){
        //             int_num_val=(bit <8>) MAX_hop;
        //         }
        //         bit<48> rand_val;
        //         random(rand_val,0,a*(T-T1)+b*(T-T1));
        //         if(rand_val<a*(bit <48>) int_num_val*((T-T1)/MAX_hop)+b*(standard_metadata.egress_global_timestamp - meta.last_egress_global_timestamp_md.last_egress_global_timestamp-T1)){
        //             int_table_sampling2.apply();
        //             last_egress_global_timestamp.write((bit<32>)standard_metadata.egress_port, standard_metadata.egress_global_timestamp);
        //         }
        //     }
        // }
        
        // else {
        //     NoAction();
        // }
    }
}

/*************************************************************************
*************   C H E C K S U M    C O M P U T A T I O N   **************
*************************************************************************/

control MyComputeChecksum(inout headers hdr, inout metadata meta) {
    apply {}
}

/*************************************************************************
***********************  D E P A R S E R  *******************************
*************************************************************************/

control MyDeparser(packet_out packet, in headers hdr) {
    apply {
        packet.emit(hdr.ethernet);
        packet.emit(hdr.type_fwd);
        packet.emit(hdr.sr_hdr);
        packet.emit(hdr.sr);
        packet.emit(hdr.type_int);
        packet.emit(hdr.int_option);
        packet.emit(hdr.inthdr);
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
