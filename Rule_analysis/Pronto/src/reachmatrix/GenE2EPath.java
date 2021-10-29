package reachmatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;

public class GenE2EPath {

	ReachGraph rg;
	Node[] allnodes; // all subnets.
	HashMap<Node, Integer> reversemap;
	HashSet<Integer> nodeselected;

	int srcnodeind; // node index used to generate all paths from this node

	// has the numbers of these paths
	int maxpathnum = 50;
	KShortestPaths<Node, Link> ksp;

	public GenE2EPath(ReachGraph rg) throws IOException
	{
		this.rg = rg;
		allnodes = (Node[]) rg.subnets.values().toArray(new Node[0]);
		reversemap = new HashMap<Node, Integer>();
		for(int i = 0; i < allnodes.length; i ++)
		{
			reversemap.put(allnodes[i], i);
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

	}


	/**
	 * 
	 * @param numofsubnets - number of subnets involved;
	 * @param maxpath - number of paths calculated for each pair of src and dest
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void calculateEndtoEndpath(int startnode, int endnode, int maxpath) throws IOException, ClassNotFoundException
	{
		this.maxpathnum = maxpath;
		
		File file = new File ("end2endpath" + startnode + "-" + endnode);
	    PrintWriter printWriter = new PrintWriter (file);
		
		for(int i: nodeselected)
		{
			genpathfrom(i,printWriter);
			printWriter.flush();
		}
		
		printWriter.close();
	}
	
	public void calculateEndtoEndpath(Set<Node> ns, int maxpath) throws IOException, ClassNotFoundException
	{
		this.maxpathnum = maxpath;
		nodeselected = new HashSet<Integer>();
		for(Node n : ns)
		{
			nodeselected.add(reversemap.get(n));
		}
		
		File file = new File ("end2endpath" + ns.size());
	    PrintWriter printWriter = new PrintWriter (file);
		
		for(int i: nodeselected)
		{
			genpathfrom(i,printWriter);
			printWriter.flush();
		}
		
		printWriter.close();
	}
	


	public void genpathfrom(int nodeind, PrintWriter pw) throws IOException, ClassNotFoundException
	{
		setExperiment(nodeind);
		
		for(int i : nodeselected)
		{
			if(i != srcnodeind)
			{
				List<GraphPath<Node, Link>> gps = ksp.getPaths(allnodes[i]);
				recordtrace(gps, pw);
			}
			System.out.println(srcnodeind + "-" + i);
		}
		
	}
	

	private void recordtrace(List<GraphPath<Node, Link>> gps, PrintWriter pw) throws IOException
	{
		if(gps == null)
		{
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
					pw.print(l.getACLName() + " ");
				}else{
					/*-----------------*/
					System.out.println(l + " : not have acl id");
				}
			}
			pw.println();	
		}
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
		
		GenE2EPath e2e = new GenE2EPath(rg);
		e2e.calculateEndtoEndpath(Integer.parseInt(args[0]), Integer.parseInt(args[1]),1);
		
	}
}
