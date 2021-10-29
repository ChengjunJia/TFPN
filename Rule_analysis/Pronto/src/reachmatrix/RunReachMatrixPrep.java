package reachmatrix;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * use this class to measure the preprocessing time of  the experiment of reachability matrix computation
 * only static methods
 * 
 * @author hongkun
 *
 */
public class RunReachMatrixPrep {

	/**
	 * 
	 * @param args args[0] - only accept 1,2,3
	 * @throws FileNotFoundException
	 */
	static LinkedList<int[]> levels = new LinkedList<int[]>();
	static int[] level1 = {2,4,6,8,10};
	static int[] level2 = {1,2,3,4,6};
	static int[] level3 = {1,2,3,4,5,6,7,8,10};
	static ReachGraph rg = new ReachGraph("purdue.ser", "purdue-BDDAP.ser");
	static NetworkFactory netf;
	// store lists of subnet sets to be tested
	static List<Set<Node>> subnetsl = new LinkedList<Set<Node>>();


	public static void main(String[] args) throws FileNotFoundException
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

		System.out.println(subnetsl.size() + " sets generated.");

		GenerateExpNetwork gen = new GenerateExpNetwork(rg);

		PrintStream ps = new PrintStream((new FileOutputStream("appreptime", true)));
		for(int i = 0; i < subnetsl.size(); i ++)
		{
			long timepast = gen.generatesubnetwork_sim(subnetsl.get(i), "purdue-BDDAP.ser");
			ps.println(subnetsl.get(i).size() + " " + timepast);
			System.out.println(subnetsl.get(i).size() + ":" + timepast);
		}

	}
}
