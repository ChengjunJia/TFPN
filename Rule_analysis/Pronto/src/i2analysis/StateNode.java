package i2analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import stanalysis.FWDAPSet;
import stanalysis.PositionTuple;

/**
 * a state is (a place in the network, packet set)
 * @author nrlab
 *
 */

public class StateNode {

	public static final int contflag = 0;
	public static final int loopflag = 2;
	public static final int deadflag = 3;
	// dead branch means that we find a loop not originated from source port

	PositionTuple pt;
	FWDAPSet fwdaps;
	int flag;

	HashMap<PositionTuple, StateNode> nextStateNodes;	

	HashSet<PositionTuple> dvisited;
	ArrayList<PositionTuple> dvisitedlist; 

	public StateNode(PositionTuple pt, FWDAPSet fwdaps)
	{
		this.pt = pt;
		this.fwdaps = fwdaps;
		dvisited = new HashSet<PositionTuple>();
		this.dvisitedlist= new ArrayList<PositionTuple>();
		flag = deadflag;
		nextStateNodes = null;
	}
	
	public Collection<StateNode> getNextStates()
	{
		if(nextStateNodes == null)
		{
			return null;
		}else
		{
			return nextStateNodes.values();
		}
	}

	public StateNode(PositionTuple pt, FWDAPSet fwdaps, HashSet<PositionTuple> visitedset, ArrayList<PositionTuple> dvisitedlist)
	{
		this.pt = pt;
		this.fwdaps = fwdaps;
		dvisited = visitedset;
		this.dvisitedlist=dvisitedlist;
		flag = deadflag;
		nextStateNodes = null;
	}
	
	public StateNode(PositionTuple pt, FWDAPSet fwdaps, HashSet<PositionTuple> visitedset)
	{
		this.pt = pt;
		this.fwdaps = fwdaps;
		dvisited = visitedset;
		flag = deadflag;
		nextStateNodes = null;
	}

	public StateNode findNextState(PositionTuple pt)
	{	
		if(nextStateNodes == null)
		{
			return null;
		}
		return nextStateNodes.get(pt);
	}
	
	public void removeNextState(PositionTuple pt)
	{
		nextStateNodes.remove(pt);
		if(nextStateNodes.isEmpty())
		{
			flag = deadflag;
			nextStateNodes = null;
		}
	}

	public FWDAPSet getAPSet()
	{
		return fwdaps;
	}

	public PositionTuple getPosition()
	{
		return pt;
	}

	public void addNextState(StateNode s)
	{
		if(nextStateNodes == null)
		{
			nextStateNodes = new HashMap<PositionTuple, StateNode>();
			flag = contflag;
		}
		nextStateNodes.put(s.getPosition(), s);

	}

	public HashSet<PositionTuple> getVisited()
	{
		return dvisited;
	}

	public HashSet<PositionTuple> getAlreadyVisited()
	{
		HashSet<PositionTuple> alv = new HashSet<PositionTuple> (dvisited);
		alv.add(pt);
		return alv;
	}
	
	public ArrayList<PositionTuple> getAlreadyVisited2()
	{
		ArrayList<PositionTuple> alv = new ArrayList<PositionTuple> (dvisitedlist);
		alv.add(pt);
		return alv;
	}
	

	public boolean loopDetected()
	{
		if(dvisited.contains(pt))
		{
			flag = loopflag;
			return true;
		}else
		{
			return false;
		}
	}

	public String printFlag()
	{
		switch (flag) {
		case contflag: 
			return "cont";
		case loopflag:
			return "loop";
		case deadflag:
			return "dead";
		default: 
			System.err.println("unknown flag " + flag);
			System.exit(1);
		}
		return null;
	}

	/**
	 * depth first print
	 */
	public void printState()
	{
		printState_recur("");
	}

	public void printState_recur(String headstr)
	{
		String newheadstr = headstr + pt + " " + fwdaps + " ";
		if(nextStateNodes == null)
		{
			System.out.println(newheadstr + printFlag());
		}else
		{
			for(StateNode nxtS : nextStateNodes.values())
			{
				nxtS.printState_recur(newheadstr);
			}
		}

	}
	
	public void printLoop()
	{
		printLoop_recur("");
	}
	
	public void printLoop_recur(String headstr)
	{
		String newheadstr = headstr + pt + " " + fwdaps + " ";
		if(flag == loopflag)
		{
			System.out.println(newheadstr + printFlag());
			return;
		}else if(nextStateNodes == null)
		{
			return;
		}
		else
		{
			for(StateNode nxtS : nextStateNodes.values())
			{
				nxtS.printLoop_recur(newheadstr);
			}
		}
	}

	public ArrayList<PositionTuple> getDvisitedlist() {
		return dvisitedlist;
	}

	public void setDvisitedlist(ArrayList<PositionTuple> dvisitedlist) {
		this.dvisitedlist = dvisitedlist;
	}
    


}
