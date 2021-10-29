package stanalysis;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * a state is (a place in the network, packet set)
 * @author nrlab
 *
 */

public class StateLoop {

	public static final int contflag = 0;
	//public static final int destflag = 1;
	public static final int loopflag = 2;
	public static final int deadflag = 3;
	public static final int dbraflag = 4;
	// dead branch means that we find a loop not originated from source port

	PositionTuple pt;
	FWDAPSet fwdaps;
	int flag;
	ArrayList<StateLoop> nextState;
	/*
	 * ports have traveled prior to the current position
	 */
	//HashSet<PositionTuple> visited;
	/*
	 * device have traveled prior to the current position
	 */
	HashSet<PositionTuple> dvisited;

	public StateLoop(PositionTuple pt, FWDAPSet fwdaps)
	{
		this.pt = pt;
		this.fwdaps = fwdaps;
		dvisited = new HashSet<PositionTuple>();
		flag = deadflag;
		nextState = null;
	}

	public StateLoop(PositionTuple pt, FWDAPSet fwdaps, HashSet<PositionTuple> visitedset)
	{
		this.pt = pt;
		this.fwdaps = fwdaps;
		dvisited = visitedset;
		flag = deadflag;
		nextState = null;
	}

	public FWDAPSet getAPSet()
	{
		return fwdaps;
	}

	public PositionTuple getPosition()
	{
		return pt;
	}

	public void addNextState(StateLoop s)
	{
		if(nextState == null)
		{
			nextState = new ArrayList<StateLoop>();
			flag = contflag;
		}
		nextState.add(s);

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

	public boolean deadBranchDetected()
	{
		if(dvisited.contains(pt))
		{
			flag = dbraflag;
			return true;
		}else
		{
			return false;
		}
	}

	public boolean loopDetected(PositionTuple srcpt)
	{
		if(pt.equals(srcpt))
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
		case dbraflag: 
			return "dead branch";
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
		if(nextState == null)
		{
			System.out.println(newheadstr + printFlag());
		}else
		{
			for(int i = 0; i < nextState.size(); i ++)
			{
				StateLoop nxtS = nextState.get(i);
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
		}else if(nextState == null)
		{
			return;
		}
		else
		{
			for(StateLoop nxtS : nextState)
			{
				nxtS.printLoop_recur(newheadstr);
			}
		}
	}

}
