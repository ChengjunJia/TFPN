package reachmatrix;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.graph.DirectedSubgraph;

public class GenPathRun {

	/**
	 * 
	 * @param args args[0] - only accept 1,2,3
	 * @throws FileNotFoundException
	 */
	static LinkedList<int[]> levels = new LinkedList<int[]>();
	static int[] level1 = {2,6};
	static int[] level2 = {2,6};
	static int[] level3 = {3,4,5,6,7,10};
	static ReachGraph rg = new ReachGraph("purdue.ser", "purdue-BDDAP.ser");
	static NetworkFactory netf;
	// store lists of subnet sets to be tested
	static List<Set<Node>> subnetsl = new LinkedList<Set<Node>>();

	
	public static void main(String[] args) throws ClassNotFoundException, IOException
	{	
		levels.add(level1);
		levels.add(level2);
		levels.add(level3);
		
		//int level = Integer.parseInt(args[0]);
		int level = 1;
		netf = new NetworkFactory(rg,level);
		
		
		Set<Node> ns1 = netf.expandselection(5);
		Set<Node> ns2 = netf.expandselection(6);
		
		ns2.removeAll(ns1);
		
		Set<Node> toadd; 
		int maxnodenum = 25;
		if(ns2.size() > maxnodenum)
		{
			toadd = new HashSet<Node>();
			Random generator = new Random(System.currentTimeMillis());
			double prob = maxnodenum/(ns2.size() + 0.0);
			
			for(Node n : ns2)
			{
				double roll = generator.nextDouble();
				if(roll < prob)
				{
					toadd.add(n);
				}
			}
		}else
		{
			toadd = ns2;
		}
		System.out.println(toadd.size() + " nodes");
		
		GenE2EPath gep = new GenE2EPath(rg);
		gep.calculateEndtoEndpath(toadd, 1);
		
		
		

	}
}
