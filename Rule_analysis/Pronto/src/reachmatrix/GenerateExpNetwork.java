package reachmatrix;

import java.io.*;
import java.util.*;

import org.jgrapht.graph.*;

import StaticReachabilityAnalysis.BDDAP;
import StaticReachabilityAnalysis.StoreACL;

public class GenerateExpNetwork {

	ReachGraph rg;
	// generated from rg
	DefaultDirectedGraph<Node, Link> basenetwork;
	int size; // number of nodes in derivednetwork
	Node[] subnets;
	// reach vector from subnetid to other subnets
	// store the mapping from a subnet to its index in subnets[]
	HashMap<Node, Integer> reversemap;
	int[][] ACLgraph1;
	int[][] ACLgraph2;
	String[][] ACLNamegraph1;
	String[][] ACLNamegraph2;

	public int getreversemapID(Node n) {
		return reversemap.get(n);
	}

	public String getACLNamegraph1(int x, int y) {
		return ACLNamegraph1[x][y];
	}

	public String getACLNamegraph2(int x, int y) {
		return ACLNamegraph2[x][y];
	}

	public GenerateExpNetwork(ReachGraph rg) {
		this.rg = rg;
		size = rg.subnets.size();
		generatebasenetwork();
	}

	private void generatebasenetwork() {
		basenetwork = new DefaultDirectedGraph<Node, Link>(Link.class);
		// add nodes to it
		// set up reverse map
		subnets = (Node[]) rg.subnets.values().toArray(new Node[0]);
		reversemap = new HashMap<Node, Integer>();
		for (int i = 0; i < subnets.length; i++) {
			basenetwork.addVertex(subnets[i]);
			reversemap.put(subnets[i], i);
		}

		ACLgraph1 = new int[size][size];
		ACLgraph2 = new int[size][size];
		ACLNamegraph1 = new String[size][size];
		ACLNamegraph2 = new String[size][size];

		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++) {
				ACLgraph1[i][j] = -1;
				ACLgraph2[i][j] = -1;
			}

		Node[] routers = (Node[]) rg.routers.values().toArray(new Node[0]);

		for (int i = 0; i < routers.length; i++) {
			Node n1 = routers[i];
			Set<Link> links = rg.reachabilitygraph.outgoingEdgesOf(n1);
			addlinks(links);
		}
		System.out.println("basenetwork is generated.");
	}

	private void addlinks(Set<Link> links) {
		Link[] linkarray = links.toArray(new Link[0]);
		for (int i = 0; i < linkarray.length; i++)
			for (int j = 0; j < linkarray.length; j++) {
				if (i != j) {
					// have the same n1
					Node n1 = linkarray[i].getn1();
					Node n2i = linkarray[i].getn2();
					Node n2j = linkarray[j].getn2();
					// create a link denoting n2i -> n2j
					Link tmplink = new Link(rg.getGraph().getEdge(n2i, n1));

					// first add to aclgraph1, aclgraph2
					int ind1 = reversemap.get(n2i);
					int ind2 = reversemap.get(n2j);
					ACLgraph1[ind1][ind2] = tmplink.getACLId();
					ACLNamegraph1[ind1][ind2] = tmplink.getACLName();
					ACLgraph2[ind1][ind2] = linkarray[j].getACLId();
					ACLNamegraph2[ind1][ind2] = linkarray[j].getACLName();

					tmplink.concatenateLink(linkarray[j]);
					basenetwork.addEdge(n2i, n2j, tmplink);

				}
			}
	}

	/**
	 * output the graph for the input of FDD
	 * 
	 * @throws FileNotFoundException
	 */
	public void outputgraph() throws FileNotFoundException {
		// set an output file
		PrintStream outps = new PrintStream(new FileOutputStream("aclgraph"));
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++) {
				if (ACLgraph1[i][j] != -1) {
					// two lines
					outps.println(i + " " + j);
					outps.println(ACLgraph1[i][j] + " " + ACLgraph2[i][j]);
				}
			}

		System.out.println("generate output for FDD");
	}

	public DirectedSubgraph<Node, Link> generatesubnetwork(Set<Node> nodes) throws FileNotFoundException {
		String outfn = "nodeids-" + nodes.size();
		PrintStream outps = new PrintStream(new FileOutputStream(outfn));
		for (Node n : nodes) {
			outps.println(reversemap.get(n));
		}
		outps.close();
		System.out.println(outfn + " is saved.");
		return new DirectedSubgraph<Node, Link>(basenetwork, nodes, null);
	}

	public long generatesubnetwork_sim(Set<Node> nodes, String apfile) {
		BDDAP bap = BDDAP.LoadBDDAP(apfile);
		StoreACL sa = bap.getACLInfo();
		long start = System.nanoTime();
		for (Node n1 : nodes)
			for (Node n2 : nodes) {
				if (basenetwork.containsEdge(n1, n2)) {
					int ind1 = reversemap.get(n1);
					int ind2 = reversemap.get(n2);
					int aclid1 = ACLgraph1[ind1][ind2];
					int aclid2 = ACLgraph2[ind1][ind2];
					HashSet<Integer> is1 = null;
					HashSet<Integer> is2 = null;
					if (aclid1 < sa.getACLNum()) {
						is1 = bap.getAPExp(aclid1);
					}
					if (aclid2 < sa.getACLNum()) {
						is2 = bap.getAPExp(aclid2);
					}

					if (is1 != null && is2 != null) {
						is1.retainAll(is2);
					}
				}
			}
		long end = System.nanoTime();
		return end - start;

	}

	public DirectedSubgraph<Node, Link> testgeneration() throws FileNotFoundException {
		int nodenum = 30;
		HashSet<Node> nodes = new HashSet<Node>();
		for (int i = 0; i < nodenum; i++) {
			nodes.add(subnets[i]);
		}
		return generatesubnetwork(nodes);
	}

	/**
	 * 
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		ReachGraph rg = new ReachGraph("purdue.ser", "purdue-BDDAP.ser");
		GenerateExpNetwork gen = new GenerateExpNetwork(rg);
		NetworkFactory netf = new NetworkFactory(rg, 2);
		Set<Node> ns = netf.expandselection(1);
		gen.generatesubnetwork(ns);
		// gen.outputgraph();
	}
}
