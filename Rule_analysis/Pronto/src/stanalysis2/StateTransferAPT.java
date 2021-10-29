package stanalysis2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import stanalysis.PositionTuple;
import StaticReachabilityAnalysis.BDDACLWrapper;

public class StateTransferAPT {

	PositionTuple destination;
	NetworkAPT net;

	public StateTransferAPT(NetworkAPT net)
	{
		this.net = net;
	}

	/**
	 * return the head state
	 */
	public StateAPT Traverse(PositionTuple startpt, PositionTuple endpt)
	{
		destination = endpt;
		StateAPT startstate = new StateAPT(startpt, BDDACLWrapper.BDDTrue);
		Traverse_recur(startstate);
		return startstate;
	}

	public void Traverse_recur(StateAPT s)
	{
		PositionTuple curpt = s.getPosition();
		PositionTuple nxtpt = net.LinkTransfer(curpt);
		StateAPT nxts = null;
		if(nxtpt == null)
		{
			return;
		}else
		{
			int apt = s.getAPTuple();
			nxts = new StateAPT(nxtpt,apt, s.getAlreadyVisited());
			s.addNextState(nxts);
		}

		if(nxts.loopDetected())
		{
			return;
		}
		if(nxts.destDetected(destination))
		{
			return;
		}

		// next one is the switch forwarding
		DeviceAPT nxtd = net.getDevice(nxtpt.getDeviceName());
		if(nxtd == null)
		{
			return;
		}
		int apt2 = nxts.getAPTuple();
		HashMap<String, Integer> fwdset =  nxtd.FowrdAction(nxtpt.getPortName(), apt2);
		if(fwdset.isEmpty())
		{
			return;
		}else
		{
			Iterator iter = fwdset.entrySet().iterator();
			//for(String portname : fwdset.keySet())
			while(iter.hasNext())
			{
				Map.Entry<String, Integer> oneentry = (Entry<String, Integer>) iter.next();
				PositionTuple fwdedpt = new PositionTuple(nxtpt.getDeviceName(), oneentry.getKey());
				//PositionTuple fwdedpt = new PositionTuple(nxtpt.getDeviceName(), portname);
				//StateAPT fwdeds = new StateAPT(fwdedpt, fwdset.get(portname), nxts.getAlreadyVisited());
				StateAPT fwdeds = new StateAPT(fwdedpt, oneentry.getValue(), nxts.getAlreadyVisited());
				nxts.addNextState(fwdeds);
				//if(!fwdeds.loopDetected() && !fwdeds.destDetected(destination))
				Traverse_recur(fwdeds);
			}
		}
	}

	public static void main (String[] args) throws IOException
	{
		NetworkAPT n = new NetworkAPT("st");
		StateTransferAPT stfer = new StateTransferAPT(n);
		StateAPT hs = null;
		long start = System.nanoTime();
		for(int i = 0; i < 10000; i++)
		{
			hs = stfer.Traverse(new PositionTuple("gozb_rtr","te2/1"), 
					new PositionTuple("gozb_rtr","te2/3"));
		}
		long end = System.nanoTime();
		hs.printState();
		System.out.println((end - start)/1000000.0);
	}
}

