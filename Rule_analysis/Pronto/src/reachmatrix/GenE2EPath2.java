package reachmatrix;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DirectedSubgraph;
import org.jgrapht.GraphPath;

public class GenE2EPath2 {

	static GenerateExpNetwork gen;
	
	DirectedSubgraph<Node, Link> sgraph;
	HashSet<Node> leaves;

	Node srcnode; // node index used to generate all paths from this node

	// has the numbers of these paths
	int maxpathnum = 50;
	KShortestPaths<Node, Link> ksp;

	public GenE2EPath2(DirectedSubgraph<Node, Link> sgraph) 
	{
		this.sgraph = sgraph;
		findLeaves();

	}
	
	public static void SetGEN(GenerateExpNetwork genet)
	{
		gen = genet;
	}
	
	private void findLeaves()
	{
		Set<Node> nodes = sgraph.vertexSet();
		leaves = new HashSet<Node>();
		for(Node n : nodes)
		{
			if(sgraph.inDegreeOf(n) == 1 || sgraph.outDegreeOf(n) == 1)
			{
				leaves.add(n);
			}
		}
		System.out.println(leaves.size() + " leaves");
	}

	/**
	 * 
	 * @param ind - to generate all paths from ind
	 * @throws FileNotFoundException 
	 */
	private void setExperiment(Node n)
	{
		srcnode = n;
		ksp = new KShortestPaths<Node, Link> (sgraph, srcnode, maxpathnum);

	}


	/**
	 * 
	 * @param numofsubnets - number of subnets involved;
	 * @param maxpath - number of paths calculated for each pair of src and dest
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void calculateEndtoEndpath(int maxpath) throws IOException, ClassNotFoundException
	{
		this.maxpathnum = maxpath;

		File file = new File ("end2endpath" + sgraph.vertexSet().size());
		PrintWriter printWriter = new PrintWriter (file);

		for(Node l : leaves)
		{
			genpathfrom(l,printWriter);
			printWriter.flush();
		}

		printWriter.close();
	}


	public void genpathfrom(Node n, PrintWriter pw) throws IOException, ClassNotFoundException
	{
		setExperiment(n);

		for(Node l : leaves)
		{
			if(!l.equals(srcnode))
			{
				List<GraphPath<Node, Link>> gps = ksp.getPaths(l);
				recordtrace(gps, pw);
			}
			System.out.println(srcnode + "-" + l);
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
				Node n1 = l.getn1();
				Node n2 = l.getn2();
				int id1 = gen.getreversemapID(n1);
				int id2 = gen.getreversemapID(n2);
				pw.print(gen.getACLNamegraph1(id1, id2) + " ");
				pw.print(gen.getACLNamegraph2(id2, id1) + " ");
				System.out.println(gen.getACLNamegraph1(id1, id2) + " " + gen.getACLNamegraph2(id2, id1));
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
