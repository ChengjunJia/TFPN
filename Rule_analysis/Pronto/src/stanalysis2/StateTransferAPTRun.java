package stanalysis2;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import stanalysis.PositionTuple;

public class StateTransferAPTRun {

	public static void main(String[] args) throws IOException
	{
		NetworkAPT n = new NetworkAPT("st");
		StateTransferAPT stfer = new StateTransferAPT(n);
		Set<PositionTuple> pts = n.getallactiveports();
		
		System.out.println(pts.size()+" ports.");
		for(PositionTuple pt1 : pts)
			for(PositionTuple pt2 : pts)
			{
				if(!pt1.equals(pt2))
				{
					long start = System.nanoTime();
					int rep = 1;
					//System.out.println(pt1+","+pt2);
					for(int i = 0; i < rep; i ++)
					{
						StateAPT hs = stfer.Traverse(pt1, pt2);
						//State hs = stfer.Traverse(new PositionTuple("gozb_rtr","te2/3"), new PositionTuple("bbra_rtr","te7/3"));
					}
					long end = System.nanoTime();
					System.out.println((end - start)/1000000.0/rep);
				}
			}
		

	}
}