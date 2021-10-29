package stanalysis;

import i2analysis.StateNode;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * a state is (a place in the network, packet set)
 * @author nrlab
 *
 */

public class State {

	public static final int contflag = 0;
	public static final int destflag = 1;
	public static final int loopflag = 2;
	public static final int deadflag = 3;

	PositionTuple pt;
	FWDAPSet fwdaps;
	int flag;
	ArrayList<State> nextState;
	/*
	 * ports have traveled prior to the current position
	 */
	//HashSet<PositionTuple> visited;
	/*
	 * device have traveled prior to the current position
	 */
	HashSet<String> dvisited;

	public State(PositionTuple pt, FWDAPSet fwdaps)
	{
		this.pt = pt;
		this.fwdaps = fwdaps;
		dvisited = new HashSet<String>();
		flag = deadflag;
		nextState = null;
	}

	public State(PositionTuple pt, FWDAPSet fwdaps, HashSet<String> visitedset)
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

	public void addNextState(State s)
	{
		if(nextState == null)
		{
			nextState = new ArrayList<State>();
			flag = contflag;
		}
		nextState.add(s);

	}

	public HashSet<String> getVisited()
	{
		return dvisited;
	}

	public HashSet<String> getAlreadyVisited()
	{
		HashSet<String> alv = new HashSet<String> (dvisited);
		alv.add(pt.getDeviceName());
		return alv;
	}

	public boolean loopDetected()
	{
		if(dvisited.contains(pt.getDeviceName()))
		{
			flag = loopflag;
			return true;
		}else
		{
			return false;
		}
	}

	public boolean destDetected(PositionTuple destpt)
	{
		if(pt.equals(destpt))
		{
			flag = destflag;
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
		case destflag: 
			return "dest";
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
				State nxtS = nextState.get(i);
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
			for(State nxtS : nextState)
			{
				nxtS.printLoop_recur(newheadstr);
			}
		}
	}

}
