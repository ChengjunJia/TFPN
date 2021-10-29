package stanalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import StaticReachabilityAnalysis.BDDACLWrapper;

/**
 * no loop detect, deadbranch detect in forwardedstates
 * @author yanghk
 *
 */

public class LoopDetect2 {

	PositionTuple destination;
	Network net;

	public LoopDetect2(Network net)
	{
		this.net = net;
	}

	/**
	 * return the head state
	 */
	public StateLoop Traverse(PositionTuple startpt)
	{
		destination = startpt;
		StateLoop startstate = new StateLoop(startpt, new FWDAPSet(BDDACLWrapper.BDDTrue));
		Traverse_recur(startstate);
		return startstate;
	}

	public void Traverse_recur(StateLoop s)
	{
		PositionTuple curpt = s.getPosition();
		HashSet<PositionTuple> nxtpts = net.LinkTransfer(curpt);

		if(nxtpts == null)
		{
			return;
		}

		for(PositionTuple nxtpt : nxtpts)
		{
			StateLoop nxts = null;

			FWDAPSet faps = s.getAPSet();
			nxts = new StateLoop(nxtpt,faps, s.getAlreadyVisited());
			s.addNextState(nxts);


			if(nxts.loopDetected(destination))
			{
				return;
			}
			if(nxts.deadBranchDetected())
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
					StateLoop fwdeds = new StateLoop(fwdedpt, oneentry.getValue(), nxts.getAlreadyVisited());
					nxts.addNextState(fwdeds);
					if(fwdeds.loopDetected(destination))
					{
						return;
					}
					if(fwdeds.deadBranchDetected())
					{
						return;
					}else
					{
						Traverse_recur(fwdeds);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param s - packet set and incoming port
	 * @return - next states
	 */
	public ArrayList<StateLoop> ForwardedStates(StateLoop s)
	{
		ArrayList<StateLoop> nxtSs = new ArrayList<StateLoop>();

		Device nxtd = net.getDevice(s.getPosition().getDeviceName());
		if(nxtd == null)
		{
			return nxtSs;
		}
		FWDAPSet fwdaps = new FWDAPSet(s.getAPSet());
		HashMap<String, FWDAPSet> fwdset =  nxtd.FowrdAction(s.getPosition().getPortName(), fwdaps);
		if(fwdset.isEmpty())
		{
			return nxtSs;
		}else
		{
			Iterator iter = fwdset.entrySet().iterator();
			//for(String portname : fwdset.keySet())
			while(iter.hasNext())
			{
				Map.Entry<String, FWDAPSet> oneentry = (Entry<String, FWDAPSet>) iter.next();
				PositionTuple fwdedpt = new PositionTuple(s.getPosition().getDeviceName(), oneentry.getKey());
				//State fwdeds = new State(fwdedpt, fwdset.get(portname), nxts.getAlreadyVisited());
				StateLoop fwdeds = new StateLoop(fwdedpt, oneentry.getValue(), s.getAlreadyVisited());
				s.addNextState(fwdeds);
				nxtSs.add(fwdeds);
			}
		}
		return nxtSs;

	}

	public ArrayList<StateLoop> linkTransfer(StateLoop s)
	{
		ArrayList<StateLoop> nxtSs = new ArrayList<StateLoop>();
		HashSet<PositionTuple> nxtpts = net.LinkTransfer(s.getPosition());
		if(nxtpts == null)
		{
			return nxtSs;
		}else
		{
			for(PositionTuple nxtpt : nxtpts)
			{
				FWDAPSet faps = s.getAPSet();
				StateLoop nxts = new StateLoop(nxtpt,faps, s.getAlreadyVisited());
				s.addNextState(nxts);

				if(nxts.loopDetected(destination))
				{
				} else if(nxts.deadBranchDetected())
				{
				}else
				{
					nxtSs.add(nxts);
				}
			}
		}
		return nxtSs;
	}

	public void DoLoop_recur(StateLoop s)
	{
		//forwarding
		ArrayList<StateLoop> nxtSf = ForwardedStates(s);
		for(StateLoop nxtsf : nxtSf)
		{
			// link transfer
			ArrayList<StateLoop> nxtSl = linkTransfer(nxtsf);
			for(StateLoop nxtsl : nxtSl)
			{
				DoLoop_recur(nxtsl);
			}
		}
	}

	public StateLoop DoLoop(PositionTuple startpt)
	{
		destination = startpt;
		StateLoop startstate = new StateLoop(startpt, new FWDAPSet(BDDACLWrapper.BDDTrue));
		DoLoop_recur(startstate);
		return startstate;
	}

	public static void main (String[] args) throws IOException
	{
		Network n = new Network("st");
		LoopDetect2 stfer = new LoopDetect2(n);

		HashSet<PositionTuple> pts = new HashSet<PositionTuple>();
        pts.add(new PositionTuple("bbra_rtr","te7/1"));
        pts.add(new PositionTuple("bbrb_rtr","te7/1"));
        pts.add(new PositionTuple("bbra_rtr","te6/3"));
        pts.add(new PositionTuple("bbrb_rtr","te7/4"));
        pts.add(new PositionTuple("bbra_rtr","te7/2"));
        pts.add(new PositionTuple("bbrb_rtr","te1/1"));
        pts.add(new PositionTuple("bbra_rtr","te6/1"));
        pts.add(new PositionTuple("bbrb_rtr","te6/3"));
        pts.add(new PositionTuple("bbra_rtr","te1/4"));
        pts.add(new PositionTuple("bbrb_rtr","te1/3"));
        pts.add(new PositionTuple("bbra_rtr","te1/3"));
        pts.add(new PositionTuple("bbrb_rtr","te7/2"));
        pts.add(new PositionTuple("bbra_rtr","te7/3"));
        pts.add(new PositionTuple("bbrb_rtr","te6/1"));
        pts.add(new PositionTuple("boza_rtr","te2/3"));
        pts.add(new PositionTuple("coza_rtr","te2/3"));
        pts.add(new PositionTuple("yozb_rtr","te1/3"));
        pts.add(new PositionTuple("yozb_rtr","te1/2"));
        pts.add(new PositionTuple("yoza_rtr","te1/1"));
        pts.add(new PositionTuple("yoza_rtr","te1/2"));
        pts.add(new PositionTuple("bozb_rtr","te2/3"));
        pts.add(new PositionTuple("cozb_rtr","te2/3"));
        pts.add(new PositionTuple("gozb_rtr","te2/3"));
        pts.add(new PositionTuple("pozb_rtr","te2/3"));
        pts.add(new PositionTuple("goza_rtr","te2/3"));
        pts.add(new PositionTuple("poza_rtr","te2/3"));
        pts.add(new PositionTuple("rozb_rtr","te2/3"));
        pts.add(new PositionTuple("sozb_rtr","te2/3"));
        pts.add(new PositionTuple("roza_rtr","te2/3"));
        pts.add(new PositionTuple("soza_rtr","te2/3"));

        /*
		for(PositionTuple pt1 : pts)
		{
			System.out.println(pt1);
			StateLoop hs = stfer.DoLoop(pt1);
			hs.printLoop();
		}*/
		StateLoop hs = stfer.DoLoop(new PositionTuple("coza_rtr", "te2/1"));
		hs.printLoop();
		hs.printState();
	}
}
