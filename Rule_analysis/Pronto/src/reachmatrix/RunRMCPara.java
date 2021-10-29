package reachmatrix;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * use this class to run the experiment of reachability matrix computation
 * only static methods
 * 
 * @author hongkun
 *
 */
public class RunRMCPara {

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

	/**
	 * 
	 * @param args[0] - level, args[1] - wayness
	 * @throws FileNotFoundException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws FileNotFoundException, InterruptedException
	{	
		levels.add(level1);
		levels.add(level2);
		levels.add(level3);
		
		int level = Integer.parseInt(args[0]);
		netf = new NetworkFactory(rg,level);
		
		int levelid = level - 1;
		for(int i = 0; i < levels.get(levelid).length; i ++)
		{
			Set<Node> ns = netf.expandselection(levels.get(levelid)[i]);
			subnetsl.add(ns);
		}
		
		int wayness = Integer.parseInt(args[1]);
		
		System.out.println(subnetsl.size() + " sets generated.");

		GenerateExpNetwork gen = new GenerateExpNetwork(rg);
		/*
		ReachMatrixComputation rmc = new ReachMatrixComputation(gen.generatesubnetwork(subnetsl.get(0)));
		rmc.calculateall();*/
		
		for(int i = 0; i < subnetsl.size(); i ++)
		{
			RMCPara rmcp = new RMCPara(gen.generatesubnetwork(subnetsl.get(i)), wayness);
			rmcp.calculateall();
		}

	}
}
