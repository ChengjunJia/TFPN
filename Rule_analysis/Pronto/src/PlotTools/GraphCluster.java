package PlotTools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import StaticReachabilityAnalysis.*;

public class GraphCluster {

	int numNodes;
	int numClusters;
	int[] nodeClusters;
	String graphName;
	ArrayList<String> nodeList;
	ArrayList<int[]> nodeNeighbors;
	ArrayList<Integer>[] clusters;

	@SuppressWarnings("unchecked")
	public GraphCluster(String name, String nodeFile, String neighborFile, String clusterFile, int numCl)
	{
		graphName = name;
		nodeList = LoadNodeList(nodeFile);
		nodeNeighbors = LoadNodeNeighbors(neighborFile);
		numNodes = nodeList.size();
		numClusters = numCl;
		nodeClusters = ProcessClusterFile(clusterFile);

		clusters = new ArrayList [numClusters];
		for(int i = 0; i < clusters.length; i ++)
		{
			clusters[i] = new ArrayList<Integer>();
		}
		for(int i = 0; i < nodeClusters.length; i ++)
		{
			int clusterOfNode = nodeClusters[i];
			clusters[clusterOfNode].add(i);
		}
	}
	
	public void Debug()
	{
		System.out.println("number of nodes: " + numNodes);
		System.out.println("number of clusters: " + numClusters);
		for(int i = 0; i < clusters.length; i ++)
		{
			System.out.println("the size of cluster " + i + " : " + clusters[i].size());
		}
		
	}

	private int[] ProcessClusterFile(String filename) 
	{
		int[] nc = new int[numNodes];
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			int nodeCtr = 0;
			while(true)
			{
				String aLine = in.readLine();
				if(aLine == null)
					break;

				int cluster = Integer.parseInt(aLine);
				nc[nodeCtr] = cluster;
				nodeCtr ++;
			}
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
			return nc;
		}

		return nc;
	}

	private static ArrayList<String> LoadNodeList(String filename)
	{
		@SuppressWarnings("unchecked")
		ArrayList<String> nl = (ArrayList<String>) Utility.LoadObject(filename);
		return nl;
	}

	private static ArrayList<int[]> LoadNodeNeighbors(String filename)
	{
		@SuppressWarnings("unchecked")
		ArrayList<int[]> nn = (ArrayList<int[]>) Utility.LoadObject(filename);
		return nn;
	}
	
	/*
	 * generate a dot file 
	 * the router graph is partitioned into several clusters
	 */
	public void generateDot()
	{
		GraphViz gPlot = new GraphViz(graphName, "pdf");
		gPlot.start_graph();
		
		//compound = true
		gPlot.addln("compound=true;");
		
		//edge will not be plotted within clusters
		for(int i = 0; i < numClusters; i ++)
		{
			//begin cluster
			gPlot.addln("subgraph cluster" + i + " {");
			for(int j = 0; j < clusters[i].size(); j ++)
			{
				String configName = nodeList.get(clusters[i].get(j));
				gPlot.addln(configName + ";");
			}
			//end cluster
			gPlot.addln("}");
		}
		
		//plot edge between clusters
		for(int i = 0; i < numClusters; i ++)
		{
			ArrayList<Integer> clusterI = clusters[i];
			for(int j = i + 1; j < numClusters; j ++)
			{
				ArrayList<Integer> clusterJ = clusters[j];
				
				ArrayList<dotEdge> edges = findEdgeBetweenClusters(clusterI, clusterJ);
				//plot these edges
				for(int k = 0; k < edges.size(); k ++)
				{
					dotEdge e = edges.get(k);
					e.Undirected();
					e.setLHead("cluster" + i);
					e.setLTail("cluster" + j);
					gPlot.addln(e.toString() + ";");
				}
				
			}
		}
		
		gPlot.end_graph();
		gPlot.writeDotSourceToFile();
	}
	
	/*
	 * input two clusters,
	 * find edges between the two clusters
	 */
	private ArrayList<dotEdge> findEdgeBetweenClusters(ArrayList<Integer> cI, ArrayList<Integer> cJ)
	{
		ArrayList<dotEdge> edges = new ArrayList<dotEdge>();
		
		for(int i = 0; i < cI.size(); i ++)
			for(int j = 0; j < cJ.size(); j ++)
			{
				int nodeI = cI.get(i);
				int nodeJ = cJ.get(j);
				int[] neighborI = nodeNeighbors.get(nodeI);
				
				//is notJ a neighbor of nodeI?
				if(isIn(neighborI, nodeJ))
				{
					edges.add(new dotEdge(nodeList.get(nodeI), nodeList.get(nodeJ)));
				}
				
			}
		
		return edges;
	}
	
	private boolean isIn(int[] arr, int target)
	{
		for(int i = 0; i < arr.length; i ++)
		{
			if(target == arr[i])
				return true;
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		GraphCluster gc = new GraphCluster("purdue", "purdueNodeList.ser", "purdueNodeneighbors.ser", 
				"purdue.graph.part.40", 40);
		gc.Debug();
		gc.generateDot();
	}
}
