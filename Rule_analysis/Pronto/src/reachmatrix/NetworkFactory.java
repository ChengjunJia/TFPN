package reachmatrix;

import java.util.*;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * this class generate sets of subnets which are supposed to be connected together 
 * 
 * @author hongkun
 *
 */
public class NetworkFactory {

	ReachGraph rg;
	UndirectedGraph<Node, DefaultEdge> rg_undir;
	NeighborIndex<Node, DefaultEdge> rg_neighbor;
	Node[] allnodes; // all nodes in rg_undir
	int centernum;
	
	// node picked up from centers
	HashSet<Node> nodeselected;
	HashSet<Node> newlyadded;
	// number of hops considered
	int level;
	
	// sort nodes by their numbers of neighbors
	class nodecomparator implements Comparator<Node>{
		public int compare(Node n1, Node n2)
		{
			return - rg_undir.degreeOf(n1) + rg_undir.degreeOf(n2);
		}
	}
	
	public NetworkFactory(ReachGraph rg, int centernum)
	{
		this.rg = rg;
		convertToundir();
		rg_neighbor = new NeighborIndex<Node, DefaultEdge>(rg_undir);
		allnodes = rg_undir.vertexSet().toArray(new Node[0]);
		this.centernum = centernum;
		getcenters();
		
		level = 0;
		nodeselected = new HashSet<Node>();
		for(int i = 0; i < centernum; i ++)
		{
			nodeselected.add(allnodes[i]);
		}
		newlyadded = new HashSet<Node>(nodeselected);
		
	}
	
	private void getcenters()
	{
		//just sort Node[] allnodes, so the first centernum nodes are what we want
		Arrays.sort(allnodes, new nodecomparator());
	}
	
	private void convertToundir()
	{
		rg_undir = new SimpleGraph<Node, DefaultEdge>(DefaultEdge.class);
		Node[] nodes = rg.getGraph().vertexSet().toArray(new Node[0]);

		// first add vertex
		for(int i = 0; i < nodes.length; i ++)
		{
			rg_undir.addVertex(nodes[i]);
		}

		// then edges
		for(int i = 0; i < nodes.length; i ++)
			for(int j = i + 1; j < nodes.length; j ++)
			{
				if(rg.getGraph().containsEdge(nodes[i], nodes[j]))
				{
					rg_undir.addEdge(nodes[i], nodes[j]);
				}
			}
		System.out.println("In the undirected graph:");
		System.out.println("number of vertices: " + rg_undir.vertexSet().size());
		System.out.println("number of edges: " + rg_undir.edgeSet().size());
		
		ConnectivityInspector<Node, DefaultEdge> ci = new ConnectivityInspector<Node, DefaultEdge>(rg_undir);
		System.out.println("connectivity components: " + ci.connectedSets().size());
		for(int i = 0; i < ci.connectedSets().size(); i ++)
		{
			System.out.print(ci.connectedSets().get(i).size() + ",");
		}
		System.out.println();
	}
	
	public Set<Node> expandselection(int newlevel)
	{
		if(level > newlevel)
		{
			//this is illegal
			System.err.println("illegal new level value: " + newlevel + 
					". Current level is already " + level);
			System.exit(1);
		}
		
		for(int i = level; i < newlevel; i ++)
		{
			levelinc_I();
		}
		level = newlevel;
		
		Set<Node> subnetselected = new HashSet<Node>();
		//only return subnets
		for(Node n:nodeselected)
		{
			if(!n.isRouter())
			{
				subnetselected.add(n);
				//System.out.println(n);
			}
		}
		return subnetselected;
	}
	
	private void levelinc_I()
	{
		HashSet<Node> tmps = new HashSet<Node>();
		for(Node n : newlyadded)
		{
			List<Node> nl = rg_neighbor.neighborListOf(n);
			for(int i = 0; i < nl.size(); i ++)
			{
				Node n_tmp = nl.get(i);
				if(!nodeselected.contains(n_tmp))
				{
					//this is a newly added node
					tmps.add(n_tmp);
				}
			}
		}
		nodeselected.addAll(tmps);
		newlyadded = tmps;
	}
	
	public static void main(String [] args){
		
		ReachGraph rg = new ReachGraph("purdue.ser", "purdue-BDDAP.ser");
		NetworkFactory netf = new NetworkFactory(rg,2);
		netf.expandselection(1);
		/*
		for(int i = 1; i < 15; i ++)
		{
			Set<Node> ns = netf.expandselection(i);
			System.out.println(ns.size());
		}*/
	}
}
