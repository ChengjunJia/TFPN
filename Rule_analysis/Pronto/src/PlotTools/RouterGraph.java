package PlotTools;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import StaticReachabilityAnalysis.*;

public class RouterGraph implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5699294613624126225L;
	String graphName;
	int numEdges;
	int numNodes;
	// a list of config1, config2, ...
	ArrayList<String> nodeList;
	// for the ith node, its neighbors are an array of IDs [2, 6, ...]
	ArrayList<int[]> nodeNeighbors;
	// for the ith node, its neighbors are a String array [config3, config7, ...]
	ArrayList<String[]> nodeNeighborsByName;
	//store pairs ('config1', 0), ...
	Hashtable<String, Integer> nodeIDInv; //can get node ID by its name

	Layer layer;

	enum Layer {
		L2, L3
	}

	public RouterGraph(String name)
	{
		graphName = name;
	}

	public RouterGraph(String name, NetworkConfig net, Layer l)
	{
		graphName = name;
		layer = l;
		instantiateGraph(net);
	}
	
	/*
	 * input: a hash table of routers (Hashtable<String, RouterConfig>)
	 * instantiate numEdges, numNodes, nodeID, nodeNeighbors
	 */
	public void instantiateGraph(NetworkConfig net)
	{
		numNodes = net.getSize();

		nodeList = new ArrayList<String>();
		nodeNeighborsByName = new ArrayList<String []>();
		nodeIDInv = new Hashtable<String, Integer>();

		Collection<RouterConfig> rList = net.getRouterCollection();

		int edgeCtr = 0;

		for(RouterConfig rc : rList)
		{
			nodeList.add(rc.getname());
			nodeIDInv.put(rc.getname(), nodeList.size() - 1);
			
			HashSet<String> neighborSet;
			if(layer == Layer.L2){
				neighborSet = net.getNeighborL2(rc.getname());
			}else
			{
				neighborSet = net.getNeighbor(rc.getname());
			}

			edgeCtr = edgeCtr + neighborSet.size();

			//System.out.println(neighborSet.size());
			String[] neighbors = new String[neighborSet.size()];
			neighborSet.toArray(neighbors);
			nodeNeighborsByName.add(neighbors);
		}

		numEdges = edgeCtr/2;

		nodeNeighbors = new ArrayList<int[]>();
		for(int i = 0; i < numNodes; i ++)
		{
			int [] neighborID = new int[nodeNeighborsByName.get(i).length];
			for(int j = 0; j < neighborID.length; j ++)
			{
				neighborID[j] = getIDByName(nodeNeighborsByName.get(i)[j]);
			}
			nodeNeighbors.add(neighborID);
		}
	}

	private int getIDByName(String name)
	{
		return nodeIDInv.get(name);
	}

	public int getNodeNum()
	{
		return numNodes;
	}

	public int getEdgeNum()
	{
		return numEdges;
	}

	public void Brief()
	{
		System.out.println("network name: " + graphName);
		System.out.println("number of nodes: " + getNodeNum());
		System.out.println("number of edges: " + getEdgeNum());
	}

	public void Debug(int nodeID)
	{
		System.out.println("node ID: " + nodeID);
		String nodeName = nodeList.get(nodeID);
		System.out.println("node name: " + nodeName);
		System.out.println("In the node name-ID map: " + nodeIDInv.get(nodeName));
		System.out.println("Its neighbors: ");
		for(int i = 0; i < nodeNeighbors.get(nodeID).length; i ++)
		{
			System.out.println(nodeNeighbors.get(nodeID)[i] +": " + 
					nodeNeighborsByName.get(nodeID)[i]);
		}
	}

	/*
	 * convert an int array to a string with separator " "
	 * add int elements by offset to fit the output file format
	 */
	private static String ArrayToString(int[] a, int offset)
	{
		String s = "";
		for (int i = 0; i < a.length; i ++)
		{
			if(i == 0)
			{//the first element
				s = s + (a[i] + offset);
			}else
			{
				s = s + " " + (a[i] + offset);
			}
		}
		return s;
	}

	public boolean generateInputForClust()
	{

		FileWriter fstream;
		try {
			fstream = new FileWriter(graphName+".graph");
			PrintWriter out = new PrintWriter(fstream);
			//write # of nodes and edges
			out.println(numNodes + " " + numEdges);

			for(int i = 0; i < nodeNeighbors.size(); i ++)
			{
				//the neighbors of ith node
				int[] neighbor = nodeNeighbors.get(i);
				// "1" is used to get the correct input file format
				out.println(ArrayToString(neighbor, 1));
			}

			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		System.out.println("File " + graphName + ".graph created successfully.");

		return true;
	}

	/* 
	 * generate a dot file representing the network
	 * used for further analysis with Gephi
	 */
	public boolean generateInputForDot(GraphVizSimple gvs)
	{

		gvs.start_graph();

		for(int i = 0; i < nodeList.size(); i ++)
		{
			String configA = nodeList.get(i);
			for(int j = 0; j < nodeNeighbors.get(i).length; j ++)
			{
				int configBID = nodeNeighbors.get(i)[j];
				// do not duplicate edges
				if(configBID > i)
				{
					String configB = nodeList.get(configBID);
					gvs.addEdge(configA, configB);
				}
			}
		}

		gvs.end_graph();
		return true;
	} 

	public boolean SaveObject(String name, Object obj)
	{

		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			fos = new FileOutputStream(graphName + name +".ser");
			out = new ObjectOutputStream(fos);
			out.writeObject(obj);
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			return false;
		}

		System.out.println(name + " is saved.");
		return true;
	}


	public static void main(String[] args) throws IOException
	{
		/*
		 * load from a file
		 */
		NetworkConfig net = ParseTools.LoadNetwork("purdue.ser");

		RouterGraph rg = new RouterGraph(net.getName(), net, Layer.L2);
		rg.Brief();

		GraphVizSimple gvs = new GraphVizSimple("purduetopoL2");		
		rg.generateInputForDot(gvs);

		gvs.writeDotSourceToFile();

		/*
		rg.generateInputForClust();
		rg.SaveObject("NodeList", rg.nodeList);
		rg.SaveObject("NodeNeighbors", rg.nodeNeighbors);
		 */
		//rg.Debug(25);
	}

}
