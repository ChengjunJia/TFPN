package reachmatrix;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.*;
import org.jgrapht.graph.*;



public class GraphComponent {

	ReachGraph rg;
	UndirectedGraph<Node, DefaultEdge> rg_undir;
	BiconnectivityInspector<Node, DefaultEdge> rg_bicomp;
	
	UndirectedGraph<String, DefaultEdge> rg_reduced; // try to cluster the graph according to bi-connectivity components
	int clusterthreshold = 10;
	ArrayList<String> clusters;
	ArrayList<Set<Node>> components;
	ArrayList<Set<Node>> cutpoints;
	ArrayList<Node> subnets; //unclustered ones
	ArrayList<Node> routers; //unclustered ones

	public GraphComponent(String netfile, String apfile)
	{
		rg = new ReachGraph(netfile, apfile);
		convertToundir();
		rg_bicomp = new BiconnectivityInspector<Node, DefaultEdge>(rg_undir);
	}

	public void showComponentsInfo()
	{
		
		System.out.println("number of large components: " + clusters.size());
		for(int i = 0; i < clusters.size(); i ++)
		{
			System.out.println("cluster " + i + " has " + components.get(i).size() + " vertices, and "
					+ cutpoints.get(i).size() + " cut points");
		}
		
		System.out.println("number of single routers: " + routers.size());
		System.out.println("number of single subnets: " + subnets.size());
	}
	
	public void clusterGraph()
	{
		clusters = new ArrayList<String>();
		components = new ArrayList<Set<Node>>();
		cutpoints = new ArrayList<Set<Node>>();
		subnets = new ArrayList<Node>();
		routers = new ArrayList<Node>();
		
		Set<Set<Node>> bicomps = rg_bicomp.getBiconnectedVertexComponents();
		Iterator<Set<Node>> ite = bicomps.iterator();
		int clustercount = 0;
		while(ite.hasNext())
		{
			Set<Node> comp = ite.next();
			if(comp.size() > clusterthreshold)
			{
				// large enough
				clusters.add("c" + clustercount);
				clustercount ++;
				
				components.add(comp);
			}
		}
		fillcutpoints();
		fillsingletons();
		
	}
	
	/**
	 * based on clusters, we reduce the original graph
	 */
	public void reduceGraph()
	{
		rg_reduced = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		
		// add vertices
		for(int i = 0; i < clusters.size(); i ++)
		{
			rg_reduced.addVertex(clusters.get(i));
		}
		for(int i = 0; i < subnets.size(); i ++)
		{
			rg_reduced.addVertex("subnet" + i);
		}
		for(int i = 0; i < routers.size(); i ++)
		{
			rg_reduced.addVertex("router" + i);
		}
		
		// add edges
		// between singletons
		for(int i = 0; i < subnets.size(); i ++)
			for(int j = 0; j < routers.size(); j ++)
			{
				if(rg_undir.containsEdge(subnets.get(i), routers.get(j)))
				{
					rg_reduced.addEdge("subnet" + i, "router" + j);
				}
			}
		
		// between singletons and clusters
		for(int i = 0; i < clusters.size(); i ++)
		{
			for(int j = 0; j < subnets.size(); j ++)
			{
				if(singletonclusterconnection(i, subnets.get(j)))
				{
					rg_reduced.addEdge(clusters.get(i), "subnet" + j);
				}
			}
			for(int j = 0; j < routers.size(); j ++)
			{
				if(singletonclusterconnection(i, routers.get(j)))
				{
					rg_reduced.addEdge(clusters.get(i), "router" + j);
				}
			}
		}
		
		// between clusters and clusters
		for(int i = 0; i < clusters.size(); i ++)
			for(int j = i+1; j < clusters.size(); j ++)
			{
				HashSet<Node> comcutp = new HashSet<Node>(cutpoints.get(i));
				comcutp.retainAll(cutpoints.get(j));
				if(!comcutp.isEmpty())
				{
					rg_reduced.addEdge(clusters.get(i), clusters.get(j));
				}
			}
		
		ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<String, DefaultEdge>(rg_reduced);
		
		System.out.println("in the clustered graph:");
		System.out.println("number of vertices: " + rg_reduced.vertexSet().size());
		System.out.println("number of edges: " + rg_reduced.edgeSet().size());
		System.out.println("connectivity components: " + ci.connectedSets().size());
	}
	
	/**
	 * 
	 * @return - true, connected; false, otherwise.
	 */
	private boolean singletonclusterconnection(int clusterid, Node n)
	{
		Set<Node> cutpoint = cutpoints.get(clusterid);
		for(Node nc : cutpoint)
		{
			if(rg_undir.containsEdge(nc, n))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @author hongkun
	 * for each bi-component, find cut points belong to it
	 */
	private void fillcutpoints()
	{
		Set<Node> allcutpoints = rg_bicomp.getCutpoints();
		
		for(int i = 0; i < clusters.size(); i ++)
		{
			Set<Node> comp = components.get(i);
			Set<Node> cutpoint = new HashSet<Node>(comp);
			cutpoint.retainAll(allcutpoints);
			cutpoints.add(cutpoint);
		}
	}
	
	/**
	 * fill subnets and routers arraylist 
	 */
	private void fillsingletons()
	{
		HashSet<Node> allcomponents = new HashSet<Node>();
		for(int i = 0; i < components.size(); i ++)
		{
			allcomponents.addAll(components.get(i));
		}
		
		Node[] nodes = rg.getGraph().vertexSet().toArray(new Node[0]);
		for(int i = 0; i < nodes.length; i ++)
		{
			if(!allcomponents.contains(nodes[i]))
			{
				if(nodes[i].isRouter())
				{
					routers.add(nodes[i]);
				}else
				{
					subnets.add(nodes[i]);
				}
			}
		}
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
	
	public UndirectedGraph<String, DefaultEdge> getReducedGraph()
	{
		return this.rg_reduced;
	}

	public static void main(String [] args){

		GraphComponent gc = new GraphComponent("purdue.ser", "purdue-BDDAP.ser");
		gc.clusterGraph();
		gc.showComponentsInfo();
		gc.reduceGraph();
		
		System.out.println("--------------------------------------------------");
		System.out.println("list of degrees of nodes in the component");
		for(Node n : gc.components.get(0))
		{
			System.out.println(gc.rg_undir.degreeOf(n));
		}
		/*
		Node[] nodeincomp = gc.components.get(0).toArray(new Node[0]);
		for(int i = 0; i < nodeincomp.length; i ++)
		{
			System.out.println(nodeincomp[i]);
		}*/
	}
	

}
