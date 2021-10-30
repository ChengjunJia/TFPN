package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import bean.Network;
import bean.basis.Rule;
import bean.basis.Node;

public class Gephi{
	public static void network2gml(Network network) {
		HashSet<Integer> nodes = new HashSet<Integer>();
		try {
            BufferedWriter out = new BufferedWriter(new FileWriter("network.gml"));
    		out.write("graph [\n");
            for(Rule rule : network.getNTF().rules) {
    			for(int port : rule.getInPorts()) {
    				nodes.add(port);
    			}
    			for(int port : rule.getOutPorts()) {
    				nodes.add(port);
    			}
    		}
    		for(Rule rule : network.getTTF().rules) {
    			for(int port : rule.getInPorts()) {
    				nodes.add(port);
    			}
    			for(int port : rule.getOutPorts()) {
    				nodes.add(port);
    			}
    		}
    		for(int port : nodes) {
    			out.write("  node [\n");
    			out.write("    id " + Integer.toString(port) + "\n");
    			out.write("    label " + Integer.toString(port) + "\n");
    			out.write("  ]\n");
    		}
    		for(Rule rule : network.getNTF().rules) {
    			for(int inport : rule.getInPorts()) {
    				for(int outport : rule.getOutPorts()) {
    					if(inport == outport) {
    						continue;
    					}
    					out.write("  edge [\n");
    					out.write("    source " + Integer.toString(inport) + "\n");
    					out.write("    target " + Integer.toString(outport) + "\n");
    					out.write("  ]\n");
    				}
    			}
    		}
    		for(Rule rule : network.getTTF().rules) {
    			for(int inport : rule.getInPorts()) {
    				for(int outport : rule.getOutPorts()) {
    					if(inport == outport) {
    						continue;
    					}
    					out.write("  edge [\n");
    					out.write("    source " + Integer.toString(inport) + "\n");
    					out.write("    target " + Integer.toString(outport) + "\n");
    					out.write("  ]\n");
    				}
    			}
    		}
    		out.write("]");
    		out.close();
            System.out.println("Network gml output success");
        } catch (IOException e) {
        }
	}
	
	public static void path2gml(Network network, ArrayList<Node> path) {
		HashSet<Integer> nodes = new HashSet<Integer>();
		for(int i = 0; i< path.size(); i++) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("path" + Integer.toString(i) + ".gml"));
				out.write("graph [\n");
				for(Rule rule : network.getNTF().rules) {
					for(int port : rule.getInPorts()) {
						nodes.add(port);
					}
					for(int port : rule.getOutPorts()) {
						nodes.add(port);
					}
				}
				for(Rule rule : network.getTTF().rules) {
					for(int port : rule.getInPorts()) {
						nodes.add(port);
					}
					for(int port : rule.getOutPorts()) {
						nodes.add(port);
					}
				}
				for(int port : nodes) {
					out.write("  node [\n");
					out.write("    id " + Integer.toString(port) + "\n");
					out.write("    label " + Integer.toString(port) + "\n");
					out.write("  ]\n");
				}
				for(Rule rule : network.getNTF().rules) {
					for(int inport : rule.getInPorts()) {
						for(int outport : rule.getOutPorts()) {
							if(inport == outport) {
								continue;
							}
							out.write("  edge [\n");
							out.write("    source " + Integer.toString(inport) + "\n");
							out.write("    target " + Integer.toString(outport) + "\n");
							out.write("  ]\n");
						}
					}
				}
				for(Rule rule : network.getTTF().rules) {
					for(int inport : rule.getInPorts()) {
						for(int outport : rule.getOutPorts()) {
							if(inport == outport) {
								continue;
							}
							out.write("  edge [\n");
							out.write("    source " + Integer.toString(inport) + "\n");
							out.write("    target " + Integer.toString(outport) + "\n");
							out.write("  ]\n");
						}
					}
				}
				Node node = path.get(i);
				if(node.getVisits().size() != 0) {
					for(int j = 0; j < node.getVisits().size()-1; j++) {
						out.write("  edge [\n");
						out.write("    source " + Integer.toString(node.getVisits().get(j)) + "\n");
						out.write("    target " + Integer.toString(node.getVisits().get(j+1)) + "\n");
						out.write("    label " + node.getHdr().toString().replace(" ","") + "\n");
						out.write("  ]\n");
					}
					out.write("  edge [\n");
					out.write("    source " + Integer.toString(node.getVisits().get(node.getVisits().size()-1)) + "\n");
					out.write("    target " + Integer.toString(node.getPort()) + "\n");
					out.write("    label " + node.getHdr().toString().replaceAll(" ", "") + "\n");
					out.write("  ]\n");
				}
				out.write("]");
				out.close();
				System.out.println("Path gml " + Integer.toString(i) + " output success");
			} catch (IOException e) {
			}
		}
	}
}