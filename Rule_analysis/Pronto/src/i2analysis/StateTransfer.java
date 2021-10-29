package i2analysis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import stanalysis.FWDAPSet;
import stanalysis.PositionTuple;
import stanalysis.State;
import StaticReachabilityAnalysis.BDDACLWrapper;

public class StateTransfer {

	PositionTuple destination;
	Network net;

	public StateTransfer(Network net)
	{
		this.net = net;
	}

	/**
	 * return the head state
	 */
	public State Traverse(PositionTuple startpt, PositionTuple endpt)
	{
		destination = endpt;
		State startstate = new State(startpt, new FWDAPSet(BDDACLWrapper.BDDTrue));
		Traverse_recur(startstate);
		return startstate;
	}

	public void Traverse_recur(State s)
	{
		PositionTuple curpt = s.getPosition();
		PositionTuple nxtpt = net.LinkTransfer(curpt);
		State nxts = null;
		if(nxtpt == null)
		{
			return;
		}else
		{
			FWDAPSet faps = s.getAPSet();
			nxts = new State(nxtpt,faps, s.getAlreadyVisited());
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
		Device nxtd = net.getDevice(nxtpt.getDeviceName());
		if(nxtd == null)
		{
			return;
		}
		FWDAPSet fwdaps = new FWDAPSet(nxts.getAPSet());
		HashMap<String, FWDAPSet> fwdset =  nxtd.FowrdAction(nxtpt.getPortName(), fwdaps);
		if(fwdset.isEmpty())
		{
			return;
		}else
		{
			Iterator iter = fwdset.entrySet().iterator();
			//for(String portname : fwdset.keySet())
			while(iter.hasNext())
			{
				Map.Entry<String, FWDAPSet> oneentry = (Entry<String, FWDAPSet>) iter.next();
				PositionTuple fwdedpt = new PositionTuple(nxtpt.getDeviceName(), oneentry.getKey());
				//State fwdeds = new State(fwdedpt, fwdset.get(portname), nxts.getAlreadyVisited());
				State fwdeds = new State(fwdedpt, oneentry.getValue(), nxts.getAlreadyVisited());
				nxts.addNextState(fwdeds);
				//if(!fwdeds.loopDetected() && !fwdeds.destDetected(destination))
				Traverse_recur(fwdeds);
			}
		}
	}

	public static void main (String[] args) throws IOException
	{
		Network n = new Network("st");
		StateTransfer stfer = new StateTransfer(n);
		long start = System.nanoTime();
		State hs = null;
		//for(int i = 0; i < 10000; i ++)
		{
			hs = stfer.Traverse(new PositionTuple("kans","xe-0/1/1"), new PositionTuple("salt","ge-6/0/0"));
		}
		long end = System.nanoTime();
		hs.printState();
		System.out.println((end - start)/1000000.0);
	}
}
