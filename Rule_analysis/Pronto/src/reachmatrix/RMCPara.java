package reachmatrix;

import java.io.*;
import java.util.concurrent.*;

import org.jgrapht.graph.*;

import StaticReachabilityAnalysis.BDDACLWrapper;
import StaticReachabilityAnalysis.Sample;

/**
 * implement Xie's algorithm using parallelization
 * @author hongkun
 *
 */
public class RMCPara {

	int size; // how large is the reachability matrix
	int roundnumber; // should be size-2 for full computation
	//generated from rg
	DirectedSubgraph<Node, Link> subnetwork;
	Node[] subnets;
	int subnetid;
	// reach vector from subnetid to other subnets
	Link[] reachvector;
	Link[] newvector;
	int numsubnetused = -1;
	int wayness = 2;




	public RMCPara(DirectedSubgraph<Node, Link> subn, int wayness)
	{
		subnetwork = subn;
		size = subn.vertexSet().size();

		//this.roundnumber = size - 2;
		this.roundnumber = 32;
		//set Node[] subnets
		subnets = subn.vertexSet().toArray(new Node[0]);

		this.wayness = wayness;
	}

	public RMCPara(DirectedSubgraph<Node, Link> subn, int wayness, int numsub)
	{
		this(subn, wayness);
		numsubnetused = numsub;
	}


	private void setexperiment(int id)
	{
		subnetid = id;
		reachvector = new Link[size];
		//initialize reachvector
		for(int i = 0; i < reachvector.length; i ++)
		{
			if(i == subnetid)
			{
				Link l = new Link(subnets[i], subnets[i], null);
				l.setap(BDDACLWrapper.BDDTrue);
				reachvector[i] = l;
			}else{
				if(subnetwork.containsEdge(subnets[subnetid], subnets[i]))
				{
					Link l = new Link(subnetwork.getEdge(subnets[subnetid], subnets[i]));
					reachvector[i] = l;

					/*
					if(l.getkind() == Link.apkind.other)
					{
						System.out.println(i);
					}*/

				}else{
					Link l = new Link(subnets[subnetid], subnets[i], null);
					l.setap(BDDACLWrapper.BDDFalse);
					reachvector[i] = l;
				}
			}
		}
	}

	private void setexperimentto(int id)
	{
		subnetid = id;
		reachvector = new Link[size];
		//initialize reachvector
		for(int i = 0; i < reachvector.length; i ++)
		{
			if(i == subnetid)
			{
				Link l = new Link(subnets[i], subnets[i], null);
				l.setap(BDDACLWrapper.BDDTrue);
				reachvector[i] = l;
			}else{
				if(subnetwork.containsEdge(subnets[i], subnets[subnetid]))
				{
					Link l = new Link(subnetwork.getEdge(subnets[i], subnets[subnetid]));
					reachvector[i] = l;

					/*
					if(l.getkind() == Link.apkind.other)
					{
						System.out.println(i);
					}*/

				}else{
					Link l = new Link(subnets[i], subnets[subnetid], null);
					l.setap(BDDACLWrapper.BDDFalse);
					reachvector[i] = l;
				}
			}
		}
	}

	class CalculateOne implements Runnable{
		int ind;

		public CalculateOne(int ind){
			this.ind = ind;
		}

		@Override
		public void run() {
			Link riprime = new Link(reachvector[ind]);

			for(int k = 0; k < size; k ++)
			{
				if(k != ind && k!= subnetid)
				{
					// consider subnetid -> k -> i
					if(subnetwork.containsEdge(subnets[k], subnets[ind]))
					{
						Link fki = new Link(subnetwork.getEdge(subnets[k], subnets[ind]));
						Link fk = new Link(reachvector[k]);
						fk.concatenateLink(fki);
						riprime.combineLink(fk);
					}
				}

			}
			newvector[ind] = riprime;

		}

	}

	public double calculatevector(int id) throws InterruptedException
	{
		setexperiment(id);

		long start = System.nanoTime();
		// do the algorithm in Xie's paper
		for(int m = 0; m < roundnumber; m ++)
		{
			newvector = new Link[size];
			ExecutorService executor = Executors.newFixedThreadPool(wayness);
			for(int i = 0; i < size; i ++)
			{
				if(i != subnetid)
				{
					CalculateOne co = new CalculateOne(i);
					executor.execute(co);
				}
			}
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.HOURS);

			reachvector = newvector;
		}

		long end = System.nanoTime();

		double timepast = (end - start)/1000000.0;
		//System.out.println(timepast + "ms");
		return timepast;

	}

	public double calculatevectorto(int id)
	{
		setexperimentto(id);

		long start = System.nanoTime();
		// do the algorithm in Xie's paper
		for(int m = 0; m < roundnumber; m ++)
		{
			Link [] newvector = new Link[size];
			for(int i = 0; i < size; i ++)
			{
				Link riprime = new Link(reachvector[i]);

				if(i != subnetid){
					for(int k = 0; k < size; k ++)
					{
						if(k != i && k!= subnetid)
						{
							// consider i->k->subnetidi
							if(subnetwork.containsEdge(subnets[i], subnets[k]))
							{
								Link fik = new Link(subnetwork.getEdge(subnets[i], subnets[k]));
								Link fk = new Link(reachvector[k]);
								fik.concatenateLink(fk);
								riprime.combineLink(fik);
							}
						}

					}
				}
				newvector[i] = riprime;
			}

			reachvector = newvector;
		}

		long end = System.nanoTime();

		double timepast = (end - start)/1000000.0;
		//System.out.println(timepast + "ms");
		return timepast;

	}

	public void calculateall() throws FileNotFoundException, InterruptedException
	{
		System.out.print("calculating reachability for network of size " + size);
		//set an output file
		PrintStream outps = new PrintStream(new FileOutputStream("reachmatrixtime-para-" + size + "-" + wayness + ".txt"));

		int sepnum = 50;
		for(int i = 0; i < size; i ++)
		{
			double timepast = this.calculatevector(i);
			outps.println(timepast);

			if(i%sepnum == 0)
			{
				System.out.println();
				System.out.print(i/sepnum + ": ");
			}
			System.out.print("*");
		}
		outps.close();
		System.out.println();
	}

	public void calculatetoall() throws FileNotFoundException
	{
		System.out.print("calculating reachability for network of size " + size);
		//set an output file
		PrintStream outps = new PrintStream(new FileOutputStream("reachmatrixtimeto-" + size + ".txt"));

		int sepnum = 50;
		for(int i = 0; i < size; i ++)
		{
			double timepast = this.calculatevectorto(i);
			outps.println(timepast);

			if(i%sepnum == 0)
			{
				System.out.println();
				System.out.print(i/sepnum + ": ");
			}
			System.out.print("*");
		}
		outps.close();
		System.out.println();
	}

	public void calculateselected() throws FileNotFoundException, InterruptedException
	{
		System.out.print("calculating reachability for network of size " + size);
		System.out.print("select " + numsubnetused + " nodes");
		int[] subnetselected = new int[numsubnetused];
		Sample.GetSample(numsubnetused, size, subnetselected);
		//set an output file
		// different selection have different hash code
		PrintStream outps = new PrintStream(new FileOutputStream("reachmatrixtime-" + size 
				+ "-" + subnetselected.hashCode() + ".txt"));

		int sepnum = 50;
		for(int i = 0; i < subnetselected.length; i ++)
		{
			double timepast = this.calculatevector(subnetselected[i]);
			outps.println(timepast);

			if(i%sepnum == 0)
			{
				System.out.println();
				System.out.print(i/sepnum + ": ");
			}
			System.out.print("*");
		}
		outps.close();
		System.out.println();
	}

}
