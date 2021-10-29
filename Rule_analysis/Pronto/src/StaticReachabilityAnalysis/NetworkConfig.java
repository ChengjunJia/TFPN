package StaticReachabilityAnalysis;
/*
 * NetworkConfig.java
 *
 */
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;

public class NetworkConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4109581133301083019L;
	String networkName; // Network name
	Hashtable<String, RouterConfig> tableOfRouters; // Table of pointers to Routers
	/** Creates a new instance of NetworkConfig */
	NetworkConfig() {
		networkName = null;
		tableOfRouters = new Hashtable<String, RouterConfig>();
	}
	
	public NetworkConfig(String name)
	{
		networkName = name;
		tableOfRouters = new Hashtable<String, RouterConfig>();
	}
	
	public String getName()
	{
		return networkName;
	}
	
	public int getSize()
	{
		return tableOfRouters.size();
	}
	
	public Hashtable<String, RouterConfig> getTableofRouters()
	{
		return tableOfRouters;
	}
	
	public Collection<RouterConfig> getRouterCollection()
	{
		return tableOfRouters.values();
	}
	
	/*
	 * input: the name of  a router
	 * return: if the router is not in the network, return null
	 * otherwise, a set of router's neighbors (by name).
	 */
	public HashSet<String> getNeighbor(String routerName)
	{
		RouterConfig rc = tableOfRouters.get(routerName);
		if(rc == null)
		{
			System.err.println(routerName + " is not in network " + networkName);
			return null;
		}
		
		HashSet<String> neighbor = new HashSet<String> ();
		Collection<InterfaceConfig> iList = rc.tableOfInterfaceByIPs.values();
		
		for(InterfaceConfig ic : iList)
		{
			for(int i = 0; i < ic.neighbors.size(); i ++)
			{
				String neighborStr = ic.neighbors.get(i);
				int routerInd = neighborStr.indexOf("-");
				String neighborRouterStr = neighborStr.substring(0, routerInd);
				RouterConfig neighborRouter = tableOfRouters.get(neighborRouterStr);
				if(neighborRouter == null)
				{
					System.err.println("find bad router name: " + neighborRouterStr);
					return null;
				}
				
				neighbor.add(neighborRouterStr);
			}
		}
		return neighbor;
	}
	
	/*
	 * input: the name of  a router
	 * return: if the router is not in the network, return null
	 * otherwise, a set of router's Layer 2 neighbors (by name).
	 */
	public HashSet<String> getNeighborL2(String routerName){
		RouterConfig rc = tableOfRouters.get(routerName);
		return rc.neighborsLayer2;
	}
	
	public String toString() {
		return networkName;
	}
	
	public void saveTopology() throws FileNotFoundException
	{
	    PrintWriter printWriter = new PrintWriter (networkName + "Topology");
	    for(RouterConfig arc : tableOfRouters.values())
	    {
	    	printWriter.println(arc.hostName + ":" + arc.printLayer2Neighbors());
	    }
	    printWriter.close ();  
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		NetworkConfig net = ParseTools.LoadNetwork("purdue.ser");
		// show number of subnet prefixes
		int sub_count = 0;
		for (RouterConfig rc : net.tableOfRouters.values())
		{
			sub_count += rc.tableOfInterfaceByIPs.size();
		}
		System.out.println(sub_count + " subnet prefixes");
		//net.saveTopology();
	}
}