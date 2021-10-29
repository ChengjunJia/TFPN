/**
 * @author carmo
 * test the time to calculate the reachability over a path, and the number of 
 * atomic predicates 
 */
package reachmatrix;

import java.io.*;
import java.util.*;

import StaticReachabilityAnalysis.*;

public class PathComputation {
	ReachGraph rg;
	Node src;
	Node dst;
	Node[] nodetable;
	int nodenum;
	boolean[] visited;
	HashMap<Node,Integer> nodeToIndex;

	MyPath sdpath;
	ArrayList<Link> currentLinks;

	PrintStream tracestream;
	String timestamp;
	
	int repeat = 1;

	public void SetTrace() throws FileNotFoundException
	{
		tracestream = new PrintStream(new File("pctrace"));
	}
	
	public void SetRepeat(int repeat)
	{
		this.repeat = repeat;
	}

	public PathComputation(ReachGraph rg)
	{
		this.rg = rg;
		nodenum = rg.getGraph().vertexSet().size();
		nodetable = new Node[nodenum];
		visited = new boolean[nodenum];
		nodeToIndex = new HashMap<Node, Integer>();

		int index = 0;
		Iterator<Node> nodeiter = rg.getGraph().vertexSet().iterator();
		while(nodeiter.hasNext())
		{
			nodetable[index] = nodeiter.next();
			nodeToIndex.put(nodetable[index], index);
			index ++;
		}
	}

	/**
	 * randomly select a pair of source and destination
	 */
	public void SetSrcDst()
	{
		int[] sd = new int[2];
		Sample.GetSample(2, nodenum, sd);

		src = nodetable[sd[0]];
		dst = nodetable[sd[1]];

	}

	/**
	 * use randomized DFS to find a path from source to destination
	 * @return true, find a path from source to destination, stored in sdpath
	 *         false, source and destination is not connected 
	 */
	public boolean FindPath()
	{
		if(src == null || dst == null)
		{
			return false;
		}
		// clear visited
		for(int i = 0; i < nodenum; i ++)
		{
			visited[i] = false;
		}
		// clear currnet Links
		currentLinks = new ArrayList<Link>();

		//begin
		//set visited
		int nodeind = nodeToIndex.get(src);
		visited[nodeind] = true;

		//local storage
		Set<Link> outedgesset = rg.getGraph().outgoingEdgesOf(src);
		Link[] outedges = new Link[outedgesset.size()];
		Iterator<Link> edgesiter = outedgesset.iterator();
		int edgeInd = 0;
		while(edgesiter.hasNext())
		{
			outedges[edgeInd] = edgesiter.next();
			edgeInd ++;
		}
		int[] visitorder = new int[edgeInd];
		Sample.GetSample(edgeInd, edgeInd, visitorder);

		for(int i = 0; i < edgeInd; i ++)
		{
			Node tovisit = outedges[visitorder[i]].getn2();
			int tovisitId = nodeToIndex.get(tovisit);
			if(visited[tovisitId])
			{
				continue;
			}else{
				//try to add a new link
				currentLinks.add(rg.getGraph().getEdge(src, tovisit));
				if(FindPath_R(tovisitId, 1)){
					sdpath = new MyPath(currentLinks);
					return true;
				}else
				{
					//fail, need to remove it
					currentLinks.remove(0);
				}
			}
		}

		return false;
	}

	/**
	 * 
	 * @return
	 */
	private boolean FindPath_R(int root, int depth)
	{
		visited[root] = true;
		//local storage
		Node rootnode = nodetable[root];

		// we are done
		if(rootnode == dst)
		{
			return true;
		}
		Set<Link> outedgesset = rg.getGraph().outgoingEdgesOf(rootnode);
		Link[] outedges = new Link[outedgesset.size()];
		Iterator<Link> edgesiter = outedgesset.iterator();
		int edgeInd = 0;
		while(edgesiter.hasNext())
		{
			outedges[edgeInd] = edgesiter.next();
			edgeInd ++;
		}
		int[] visitorder = new int[edgeInd];
		Sample.GetSample(edgeInd, edgeInd, visitorder);
		for(int i = 0; i < edgeInd; i ++)
		{
			Node tovisit = outedges[visitorder[i]].getn2();
			int tovisitId = nodeToIndex.get(tovisit);
			if(visited[tovisitId])
			{
				continue;
			}else{
				//try to add a new link
				currentLinks.add(rg.getGraph().getEdge(rootnode, tovisit));
				if(FindPath_R(tovisitId, depth + 1)){
					return true;
				}else
				{
					//fail, need to remove it
					currentLinks.remove(depth);
				}
			}
		}
		return false;
	}

	/**
	 * src, dst should have been chosen, and a path (stored in sdpath) should haven been found
	 * called by PathReach()
	 * @return false - no path found, just return
	 *         true - find one path, and calculate
	 */
	public boolean OnePathReach(PrintStream outpr)
	{
		SetSrcDst();
		boolean findp = FindPath();
		if(!findp)
		{
			return false;
		}

		Link res = null;
		long start = System.nanoTime();
		//long start = System.currentTimeMillis();

		for(int i = 0; i < repeat; i ++)
			res = ReachUtil.pathConcatenation(currentLinks);

		//long end = System.currentTimeMillis();
		long end = System.nanoTime();
		outpr.print(currentLinks.size() + " ");
		
		outpr.print(res.getAPSize() +" ");
		
		//convert time in ms
		//outpr.println((end - start)/(repeat + 0.0));
		outpr.println((end - start)/1000000.0/repeat);

		//record trace: id of the acl involved
		recordtrace();
		return true;
	}

	/**
	 * write down ids of acls in currentLinks
	 */
	private void recordtrace()
	{
		for(int i = 0; i < currentLinks.size(); i ++)
		{
			Link l = currentLinks.get(i);
			if(l.getACLId() != -1)
			{
				tracestream.print(l.getACLId() + " ");
			}else{
				/*-----------------*/
				System.out.println(l + " : not have acl id");
			}
		}
		tracestream.println();
		return;
	}


	/**
	 * 
	 * @param round - how many data collected
	 * @param logged - if true, store result in a file, if false, shown in the console
	 * @throws FileNotFoundException
	 */
	public void PathReach(int round, boolean logged) throws FileNotFoundException {
		//show time
		int day, month, year;
		int minute, hour;
		GregorianCalendar date = new GregorianCalendar();
		day = date.get(Calendar.DAY_OF_MONTH);
		month = date.get(Calendar.MONTH);
		year = date.get(Calendar.YEAR);
		minute = date.get(Calendar.MINUTE);
		hour = date.get(Calendar.HOUR);
		timestamp = hour + "-" + minute + "-" + month + "-" + day + "-" + year;

		SetTrace();

		PrintStream outpr;
		File outf;
		if(logged)
		{
			outf = new File("pc"+timestamp);
			outpr = new PrintStream(outf);		
		}else{
			System.out.println(timestamp);
			outpr = System.out;
		}

		int count = 0;
		while(count < round)
		{
			if(OnePathReach(outpr))
			{
				count ++;
			}
		}

	}

	/**
	 * 
	 * @param args[0] - number of paths generated
	 * @param args[1] - repeat
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		ReachGraph rg = new ReachGraph("purdue.ser", "purdue-BDDAP.ser");
		PathComputation pc = new PathComputation(rg);
		
		pc.SetRepeat(Integer.parseInt(args[1]));
		pc.PathReach(Integer.parseInt(args[0]), true);


		/**
		pc.OnePathReach(System.out);
		 */

		/**
		 * set source and destination 
		pc.SetSrcDst();
		System.out.println(pc.src);
		System.out.println(pc.dst);
		 */
	}


}
