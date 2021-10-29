package i2analysis;

import java.io.IOException;
import java.util.HashMap;

import stanalysis.PositionTuple;

public class AddLinkRun {
	Network net;
	HashMap<PositionTuple, PositionTuple> topology;


	public AddLinkRun(Network n)
	{
		net = n;
		topology = n.topology;
	}

	public double TestAdd(HashMap<PositionTuple, PositionTuple> top, PositionTuple startpt, PositionTuple ta1, PositionTuple ta2)
	{
		int rep = 2000;

		long start = System.nanoTime();
		for(int i = 0; i < rep; i ++)
		{
			ReachabilityGraph rg = new ReachabilityGraph(net);
			rg.setTopology((HashMap<PositionTuple, PositionTuple>) top.clone());
			rg.setStartstate(startpt);
			rg.Traverse();
			rg.addlink(ta1, ta2);
		}
		long middle = System.nanoTime();

		for(int i = 0; i < rep; i ++)
		{
			ReachabilityGraph rg = new ReachabilityGraph(net);
			rg.setTopology((HashMap<PositionTuple, PositionTuple>) top.clone());
			rg.setStartstate(startpt);
			rg.Traverse();
		}
		long end = System.nanoTime();
		long t1 = middle - start;
		long t2 = end - middle;
		
		return (t1 - t2)/1000000.0/rep;
		
	}
	
	public void TestAdd(HashMap<PositionTuple, PositionTuple> top, PositionTuple ta1, PositionTuple ta2)
	{
		for(PositionTuple startpt : top.keySet())
		{
			double t = TestAdd(top, startpt, ta1, ta2);
			System.out.println(t);
		}
	}
	
	public void TestAdd()
	{
		for(PositionTuple pt1 : topology.keySet())
		{
			PositionTuple pt2 = topology.get(pt1);
			HashMap<PositionTuple, PositionTuple> newtop = (HashMap<PositionTuple, PositionTuple>) topology.clone();
			newtop.remove(pt1);
			newtop.remove(pt2);
			TestAdd(newtop, pt1, pt2);
		}
	}
	
	public static void main(String [] args) throws IOException
	{
		Network n = new Network("i2");
		AddLinkRun alr = new AddLinkRun(n);
		alr.TestAdd();
	}

}
