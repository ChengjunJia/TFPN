package reachmatrix;

import java.io.*;
import java.util.*;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;

public class EndToEndPaths {

	ReachGraph rg;
	int nodenumused; // now only use subnet
	Node[] allnodes; // all nodes in the network, the first part is node used.

	int srcnodeind; // node index used to generate all paths from this node

	static String datafoldername = "./end2enddata/";
	String [] tracefiles; // to store acls on these paths
	String [] timefiles; // to store time to calculate reachability along these paths
	String [] lengthfiles; // has to columns, the first one has the lengths of paths, the second
	// has the numbers of these paths
	int maxpathnum = 50;
	KShortestPaths<Node, Link> ksp;

	public EndToEndPaths(ReachGraph rg) throws IOException
	{
		this.rg = rg;
		Node[] nodeused = (Node[]) rg.subnets.values().toArray(new Node[0]);
		nodenumused = nodeused.length;
		//nodenumused = 2;

		allnodes = new Node[nodeused.length + rg.routers.size()];
		for(int i = 0; i < nodeused.length; i ++)
		{
			allnodes[i] = nodeused[i];
		}
		Node[] routerset = (Node[]) rg.routers.values().toArray(new Node[0]);
		for(int i = nodeused.length; i < nodeused.length + routerset.length; i ++)
		{
			allnodes[i] = routerset[i - nodeused.length];
		}

		// create a folder to store data and trace
		//new File(datafoldername).mkdir();
		clearFolder(datafoldername);
	}
	
	/**
	 * if folder name does not exist, then create the folder
	 * if exists, then delete all files in the folder
	 */
	private void clearFolder(String foldername)
	{
		File folderLocation = new File(foldername);
		if(folderLocation.exists())
		{
			File[] files = folderLocation.listFiles();
			if(files == null)
			{
				System.err.println(foldername + " is not a directory!");
				System.exit(1);
			}else
			{
				for(File f: files)
				{
					if(!f.delete())
					{
						System.err.println("File " + f.getName() + " cannot be deleted!");
						System.exit(1);
					}
				}
			}
		}else
		{
			folderLocation.mkdir();
		}
	}


	/**
	 * 
	 * @param ind - to generate all paths from ind
	 * @throws FileNotFoundException 
	 */
	private void setExperiment(int ind) throws FileNotFoundException
	{
		srcnodeind = ind;
		ksp = new KShortestPaths<Node, Link> (rg.getGraph(), allnodes[srcnodeind], maxpathnum);

		tracefiles = new String[nodenumused];
		timefiles = new String[nodenumused];
		lengthfiles = new String[nodenumused];
		for(int i = 0; i < nodenumused; i ++)
		{
			tracefiles[i] = datafoldername + srcnodeind + "-" + i;
			timefiles[i] = datafoldername + srcnodeind + "-" + i + ".time";
			lengthfiles[i] = datafoldername + srcnodeind + "-" + i + ".length";
		}

	}


	/**
	 * 
	 * @param numofsubnets - number of subnets involved;
	 * @param maxpath - number of paths calculated for each pair of src and dest
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void calculateEndtoEndpath(int numofsubnets, int maxpath) throws IOException, ClassNotFoundException
	{
		this.maxpathnum = maxpath;
		this.nodenumused = numofsubnets;
		
		for(int i = 0; i < nodenumused; i ++)
		{
			calculatepathfrom(i);
		}
	}


	public void calculatepathfrom(int nodeind) throws IOException, ClassNotFoundException
	{
		setExperiment(nodeind);
		
		for(int i = 0; i < nodenumused; i ++)
		{
			if(i != srcnodeind)
			{
				List<GraphPath<Node, Link>> gps = ksp.getPaths(allnodes[i]);
				doonepair(i, gps);
			}
		}
		System.out.println("finish logging from node " + srcnodeind);
	}
	
	private void doonepair(int nodeind, List<GraphPath<Node, Link>> gps) throws IOException
	{
		recordlength(nodeind, gps);
		evaluatetime(nodeind, gps);
		recordtrace(nodeind, gps);
	}

	private void recordlength(int nodeind, List<GraphPath<Node, Link>> gps) throws IOException
	{
		

		PrintWriter pw = new PrintWriter(new FileWriter(lengthfiles[nodeind], false));
		// record the length of each path
		if(gps == null)
		{
			pw.close();
			return;
		}
		for(int i = 0; i < gps.size(); i ++)
		{
			pw.println(gps.get(i).getEdgeList().size());
		}
		pw.close();
	}

	/**
	 * 
	 * @param mps - given an array list of paths, 
	 * @return - record the calculation time of the reachability of all these paths (in ms).
	 * @throws IOException 
	 */
	private void evaluatetime(int nodeind, List<GraphPath<Node, Link>> gps) throws IOException
	{
		PrintWriter pw = new PrintWriter(new FileWriter(timefiles[nodeind], false));
		
		if(gps == null || gps.isEmpty())
		{
			pw.println(0);
		}else{
			pw.println(evaluatetime_I(gps, 10));
		}
		pw.close();
		
	}

	/**
	 * do it in an adaptive way
	 * @param mps
	 * @param repeat
	 * @return
	 */
	private double evaluatetime_I(List<GraphPath<Node, Link>> gps, int repeat)
	{
		long start = System.nanoTime();
		for(int i = 0; i < repeat; i ++)
		{
			for(int j = 0; j < gps.size(); j ++)
			{
				Link resl = ReachUtil.pathConcatenation(gps.get(j));
			}
		}
		long end = System.nanoTime();
		if(end - start < 10)
		{
			return evaluatetime_I(gps, repeat*2);
		}else
		{
			return (end - start)/1000000.0/repeat;
		}

	}


	private void recordtrace(int nodeind, List<GraphPath<Node, Link>> gps) throws IOException
	{
		PrintWriter pw = new PrintWriter(new FileWriter(tracefiles[nodeind], false));
		if(gps == null)
		{
			pw.close();
			return;
		}
		for(int k = 0; k < gps.size(); k ++)
		{
			List<Link> currentLinks = gps.get(k).getEdgeList();
			for(int i = 0; i < currentLinks.size(); i ++)
			{
				Link l = currentLinks.get(i);
				if(l.getACLId() != -1)
				{
					//pw.print(l.getACLId() + " ");
					pw.println(l.getACLName());
				}else{
					/*-----------------*/
					System.out.println(l + " : not have acl id");
				}
			}
			pw.println();	
		}
		pw.close();
	}


	/**
	 * 
	 * @param args[0] - number of subnets
	 *        args[1] - number of paths
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		ReachGraph rg = new ReachGraph("purdue.ser", "purdue-BDDAP.ser");
		
		EndToEndPaths e2e = new EndToEndPaths(rg);
		//e2e.calculateEndtoEndpath(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		e2e.calculateEndtoEndpath(300,1);
		/*
		for(int i = 0; i < 100; i ++)
		{
			System.out.println(e2e.allnodes[i]);
		}*/
	}
}
