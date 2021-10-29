package reachmatrix;

import java.io.FileNotFoundException;
import java.util.*;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DirectedSubgraph;

import StaticReachabilityAnalysis.Sample;

public class DynamicChange {
	
	DirectedSubgraph<Node, Link> basenetwork;
	Node[] nodeary;
	Link[] linkary;

	public DynamicChange(DirectedSubgraph<Node, Link> basenet)
	{
		this.basenetwork = basenet;
		nodeary = basenet.vertexSet().toArray(new Node[0]);
		linkary = basenet.edgeSet().toArray(new Link[0]);
	}
	
	public DirectedSubgraph<Node, Link> removeonenode(int nodeind)
	{
		Set<Node> nodeset = new HashSet<Node>(basenetwork.vertexSet());
		nodeset.remove(nodeary[nodeind]);
		return new DirectedSubgraph<Node, Link>(basenetwork, nodeset, null);
	}
	
	public DirectedSubgraph<Node, Link> removeonelink(int linkind)
	{
		Set<Link> linkset = new HashSet<Link>(basenetwork.edgeSet());
		linkset.remove(linkary[linkind]);
		return new DirectedSubgraph<Node, Link>(basenetwork, basenetwork.vertexSet(), linkset);
	}
	
	public void testnoderemove() throws FileNotFoundException
	{
		int round = 50;
		int[] nodeinds = new int[round];
		Sample.GetSample(round, nodeary.length, nodeinds);
		for(int i = 0; i < nodeinds.length; i ++)
		{
			DirectedSubgraph<Node, Link> removednet = removeonenode(nodeinds[i]);
			ReachMatrixComputation rmc = new ReachMatrixComputation(removednet, 50);
			rmc.calculateselected();
		}
	}
	
	public void testlinkremove() throws FileNotFoundException
	{
		int round = 50;
		int[] linkinds = new int[round];
		Sample.GetSample(round, linkary.length, linkinds);
		for(int i = 0; i < linkinds.length; i ++)
		{
			DirectedSubgraph<Node, Link> removednet = removeonelink(linkinds[i]);
			ReachMatrixComputation rmc = new ReachMatrixComputation(removednet, 50);
			rmc.calculateselected();
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException{
		//System.out.println("in dynamic change");
		
		ReachGraph rg = new ReachGraph("purdue.ser", "purdue-BDDAP.ser");
		NetworkFactory netf = new NetworkFactory(rg,3);
		Set<Node> ns = netf.expandselection(10);
		GenerateExpNetwork gen = new GenerateExpNetwork(rg);
		DynamicChange dc = new DynamicChange(gen.generatesubnetwork(ns));
		System.out.println("test remove a node");
		dc.testnoderemove();
		System.out.println("test remove a link");
		dc.testlinkremove();
		
	}
}
