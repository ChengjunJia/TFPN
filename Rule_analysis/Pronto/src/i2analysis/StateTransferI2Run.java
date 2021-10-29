package i2analysis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import stanalysis.PositionTuple;
import stanalysis.State;


public class StateTransferI2Run {

	/**
	 * 
	 * @param args - 1,2,3
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		Network n = new Network("i2");
		StateTransfer stfer = new StateTransfer(n);
		Set<PositionTuple> pts = n.getallactiveports();
		Iterator<PositionTuple> ptiter = pts.iterator();
		int cntr = 0;
		HashSet<PositionTuple> ptsh1 = new HashSet<PositionTuple>();
		HashSet<PositionTuple> ptsh2 = new HashSet<PositionTuple>();
		HashSet<PositionTuple> ptsh3 = new HashSet<PositionTuple>();
		while(cntr < pts.size()/3)
		{
			ptsh1.add(ptiter.next());
			cntr ++;
		}
		while(cntr < pts.size() /3 *2)
		{
			ptsh2.add(ptiter.next());
			cntr ++;
		}
		while(ptiter.hasNext())
		{
			ptsh3.add(ptiter.next());
		}
		
		int choice = Integer.parseInt(args[0]);
		HashSet<PositionTuple> apts = null;
		if(choice == 1)
		{
			apts = ptsh1;
		}else if(choice == 2)
		{
			apts = ptsh2;
		}else
		{
			apts = ptsh3;
		}
		
		System.out.println(apts.size());
		
		for(PositionTuple pt1 : apts)
			for(PositionTuple pt2 : pts)
			{
				if(!pt1.equals(pt2))
				{
					long start = System.nanoTime();
					int rep = 10000;
					for(int i = 0; i < rep; i ++)
					{
						State hs = stfer.Traverse(pt1, pt2);
						//State hs = stfer.Traverse(new PositionTuple("gozb_rtr","te2/3"), new PositionTuple("bbra_rtr","te7/3"));
					}
					long end = System.nanoTime();
					System.out.println((end - start)/1000000.0/rep);
				}
			}
		

	}
}
