package stanalysis2;

import java.util.ArrayList;
import java.util.HashSet;

import stanalysis.PositionTuple;

/**
 * a state is (a place in the network, packet set)
 * @author nrlab
 *
 */

public class StateAPT {

	public static final int contflag = 0;
	public static final int destflag = 1;
	public static final int loopflag = 2;
	public static final int deadflag = 3;

	PositionTuple pt;
	int ps;
	int flag;
	ArrayList<StateAPT> nextState;
	/*
	 * ports have traveled prior to the current position
	 */
	//HashSet<PositionTuple> visited;
	/*
	 * device have traveled prior to the current position
	 */
	HashSet<String> dvisited;

	public StateAPT(PositionTuple pt, int ps)
	{
		this.pt = pt;
		this.ps = ps;
		dvisited = new HashSet<String>();
		flag = deadflag;
		nextState = null;
	}

	public StateAPT(PositionTuple pt, int apt, HashSet<String> visitedset)
	{
		this.pt = pt;
		this.ps = apt;
		dvisited = visitedset;
		flag = deadflag;
		nextState = null;
	}

	public int getAPTuple()
	{
		return ps;
	}

	public PositionTuple getPosition()
	{
		return pt;
	}

	public void addNextState(StateAPT s)
	{
		if(nextState == null)
		{
			nextState = new ArrayList<StateAPT>();
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
		String newheadstr = headstr + pt + " " + ps + " ";
		if(nextState == null)
		{
			System.out.println(newheadstr + printFlag());
		}else
		{
			for(int i = 0; i < nextState.size(); i ++)
			{
				StateAPT nxtS = nextState.get(i);
				nxtS.printState_recur(newheadstr);
			}
		}
		
	}

}
