import os

class p4_table():
    def p4_table_gen(self, cmd_file_path, topo):

        p4_cmd = "../flow_table/simple_switch_CLI"
        p4_table_dir = "../flow_table/p4_table_cmd"

        with open(cmd_file_path, "w") as f:
            f.write("") # clear the file
        with open(cmd_file_path, "w") as f:
            for sw_id in topo.swID_list:
                thrift_port = topo.swID2ThriftPort[sw_id]
                cmd = "sudo "+ p4_cmd
                cmd += (" --thrift-port %d " % (thrift_port) )
                cmd += "< "+ p4_table_dir + "/" + ( "s%d.txt\n" % (sw_id) )
                f.write( cmd )

        # For each switch
        for sw_id in topo.swID_list:
            with open(p4_table_dir+"/"+( "s%d.txt" % (sw_id) ), "w" ) as f:
                # f.write("table_set_default manual_modify change_nxt_dst 2\n")
                f.write("table_set_default MyIngress.trace_fwd change_nxt_dst 3\n")
                f.write("table_add MyIngress.fwd change_nxt_dst 10.0.0.1/32 => 1\n")
                f.write("table_add MyIngress.fwd change_nxt_dst 10.0.0.5/32 => 2\n")
                # f.write("table_add int_table do_int => 10")
        