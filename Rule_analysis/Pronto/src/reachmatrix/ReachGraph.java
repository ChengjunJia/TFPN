package reachmatrix;


import java.io.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;

import StaticReachabilityAnalysis.*;

public class ReachGraph implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	DirectedGraph<Node, Link> reachabilitygraph;
	//String - ip prefix of the subnet
	HashMap<String, Node> subnets;
	// String - hostname
	HashMap<String, Node> routers;

	public static String oneline = "--------------------------------------------------";

	public Node getRouterNode(String name)
	{
		Node n = routers.get(name);
		if(n == null)
		{
			System.err.println("Router " + name + " not found!" );
			System.exit(1);
		}
		return n;
	}

	public DirectedGraph<Node, Link> getGraph()
	{
		return reachabilitygraph;
	}

	public ReachGraph(String netfile, String bddapfile)
	{
		NetworkConfig net = ParseTools.LoadNetwork(netfile);
		BDDAP ap = BDDAP.LoadBDDAP(bddapfile);
		StoreACL sa = ap.getACLInfo();

		subnets = new HashMap<String, Node> (); 
		routers = new HashMap<String, Node> ();
		
		int aclcount = 0;
		int aclrulecount = 0;
		
		// set the set of all aps		
		APSet.setUniverse(ap.getAllAP());
		
		reachabilitygraph = new DefaultDirectedGraph<Node, Link>(Link.class);

		//for each router - get a node
		Collection<RouterConfig> routercollection = net.getRouterCollection();
		for(RouterConfig r : routercollection)
		{
			//this is a router
			Node onerouter = new Node(r.getname(), Node.type.router);
			routers.put(r.getname(), onerouter);
			reachabilitygraph.addVertex(onerouter);

			Collection<InterfaceConfig> interfacelist = r.getInterfaceWithIP();
			//each interface has 
			//this is a subnet
			for(InterfaceConfig interfacec : interfacelist)
			{
				String ipprefix = interfacec.getInterfacePrefix();
				//append the prefix with its length
				ipprefix = ipprefix + "/" + interfacec.getInterfacePrefixLength();
				Node subnetofinterface = subnets.get(ipprefix);
				if(subnetofinterface == null)
				{
					//new subnet
					subnetofinterface = new Node(ipprefix, Node.type.subnet);
					subnets.put(ipprefix, subnetofinterface);
					reachabilitygraph.addVertex(subnetofinterface);

				}
				//interface is seen as two links from and to the subnet
				Link linkto = new Link(onerouter, subnetofinterface, interfacec.getName());
				Link linkfrom = new Link(subnetofinterface, onerouter, interfacec.getName());

				/**
				 * do the ap stuff here, give sets of ap to each link
				 * note that when the link has no acls, it is allow all by default
				 * in this case, we also given an acl id to the link
				 */
				int allowid = 0 + sa.getACLNum();
				
				//linkto, outfilters
				if(interfacec.getoutFilters().isEmpty())
				{
					// this means allow all
					linkto.setap(BDDACLWrapper.BDDTrue);
					linkto.setACLId(allowid);
					linkto.setACLName();
				}else
				{
					String aclname = interfacec.getoutFilters().get(0);		
					HashSet<Integer> apexp = ap.getAPExp(r.getname(), aclname);
					
					aclcount ++;
					aclrulecount = aclrulecount + sa.getACLSize(r.getname(), aclname);
					linkto.setACLId(sa.getACLId(r.getname(), aclname));
					linkto.setACLName(r.getname(), aclname);
					

					if(apexp.contains(BDDACLWrapper.BDDFalse))
					{
						linkto.setap(BDDACLWrapper.BDDFalse);
					}else if(apexp.contains(BDDACLWrapper.BDDTrue))
					{
						linkto.setap(BDDACLWrapper.BDDTrue);
					}else
					{
						linkto.setap(apexp);
					}
				}

				//linkfrom, infilters
				if(interfacec.getinFilters().isEmpty())
				{
					linkfrom.setap(BDDACLWrapper.BDDTrue);
					linkfrom.setACLId(allowid);
					linkfrom.setACLName();
					
				}else
				{
					String aclname = interfacec.getinFilters().get(0);
					HashSet<Integer> apexp = ap.getAPExp(r.getname(), aclname);
					
					aclcount ++;
					aclrulecount = aclrulecount + sa.getACLSize(r.getname(), aclname);
					linkfrom.setACLId(sa.getACLId(r.getname(), aclname));
					linkfrom.setACLName(r.getname(), aclname);

					if(apexp.contains(BDDACLWrapper.BDDFalse))
					{
						linkfrom.setap(BDDACLWrapper.BDDFalse);
					}else if(apexp.contains(BDDACLWrapper.BDDTrue))
					{
						linkfrom.setap(BDDACLWrapper.BDDTrue);
					}else
					{
						linkfrom.setap(apexp);
					}
				}

				reachabilitygraph.addEdge(onerouter, subnetofinterface, linkto);
				reachabilitygraph.addEdge(subnetofinterface, onerouter, linkfrom);
			}
		}

		System.out.println(oneline);
		System.out.println("the reachability graph for the network is generated.");
		System.out.println("totally " + reachabilitygraph.vertexSet().size() + " nodes");
		System.out.println("and " + reachabilitygraph.edgeSet().size() + " links");
		System.out.println(subnets.size() + " subnets.");
		System.out.println(routers.size() + " routers.");
		System.out.println(aclcount + " acls.");
		System.out.println(aclrulecount + " rules.");
		System.out.println(oneline);

	}

	public static void main(String [] args)
	{
		ReachGraph rg = new ReachGraph("purdue.ser", "purdue-BDDAP.ser");
		Node n1 = rg.getRouterNode("config822");
		Node n2 = rg.getRouterNode("config824");
		DijkstraShortestPath<Node, Link> dsp = new DijkstraShortestPath<Node, Link>(rg.getGraph(), n1, n2);
		System.out.println("shortest path:");
		GraphPath<Node,Link> path = dsp.getPath();
		System.out.println(path);
		Link concatenatedlink = ReachUtil.pathConcatenation(path);
		System.out.println(concatenatedlink.getAPSize());
		List<Link> edges = path.getEdgeList();
		System.out.println(path.getEdgeList().size() + " edges:");
		for(int i = 0; i < edges.size(); i ++)
		{
			Link l = edges.get(i);
			if(l.getACLId() != -1)
			{
				System.out.println(l.getACLId());
			}
		}
		
		/*
		System.out.println(oneline);
		KShortestPaths<Node, Link> ksp = new KShortestPaths<Node, Link>(rg.getGraph(), n1, 2, 20);
		Node n3 = rg.getRouterNode("config1515");
		ArrayList<GraphPath<Node, Link>> paths = (ArrayList<GraphPath<Node, Link>>) ksp.getPaths(n3);
		for(int i = 0; i < paths.size(); i ++)
			System.out.println(paths.get(i));
			*/
	}


}


