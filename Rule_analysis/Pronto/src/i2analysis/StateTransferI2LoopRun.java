package i2analysis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import stanalysis.PositionTuple;
import stanalysis.State;


public class StateTransferI2LoopRun {

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

		System.out.println(pts.size());

		for(PositionTuple pt1 : pts)
		{

			//long start = System.nanoTime();
			int rep = 1;
			for(int i = 0; i < rep; i ++)
			{
				State hs = stfer.Traverse(pt1, pt1);
				hs.printLoop();
				//State hs = stfer.Traverse(new PositionTuple("gozb_rtr","te2/3"), new PositionTuple("bbra_rtr","te7/3"));
			}
			//long end = System.nanoTime();
			//System.out.println((end - start)/1000000.0/rep);

		}


	}
}
