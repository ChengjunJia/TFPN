package i2analysis;

import java.util.ArrayList;
import java.util.HashMap;

import stanalysis.FWDAPSet;
import stanalysis.PositionTuple;

public class Portpair {
	private PositionTuple souport;
    private PositionTuple desport;
    private FWDAPSet APBDDlist;
    private ArrayList<PositionTuple> arrivedport;
    private HashMap<Integer, ArrayList<Rule>> Portruleset;
    
    public Portpair(PositionTuple souport,PositionTuple desport,FWDAPSet APBDDlist, ArrayList<PositionTuple> arrivedport)
    {
    	
    	this.souport=new PositionTuple(souport.getDeviceName(),souport.getPortName());
    	this.desport=new PositionTuple(desport.getDeviceName(),desport.getPortName());
    	this.APBDDlist=new FWDAPSet(APBDDlist);
    	this.arrivedport= new ArrayList<PositionTuple>(arrivedport);
	
    }

	public PositionTuple getSouport() {
		return souport;
	}
	public void setSouport(PositionTuple souport) {
		this.souport = souport;
	}
	public PositionTuple getDesport() {
		return desport;
	}
	public void setDesport(PositionTuple desport) {
		this.desport = desport;
	}
	
	
	public FWDAPSet getAPBDDlist() {
		return APBDDlist;
	}

	public void setAPBDDlist(FWDAPSet aPBDDlist) {
		APBDDlist = aPBDDlist;
	}

	public ArrayList<PositionTuple> getArrivedport() {
		return arrivedport;
	}

	public void setArrivedport(ArrayList<PositionTuple> arrivedport) {
		this.arrivedport = arrivedport;
	}

	public HashMap<Integer, ArrayList<Rule>> getPortruleset() {
		return Portruleset;
	}

	public void setPortruleset(HashMap<Integer, ArrayList<Rule>> portruleset) {
		Portruleset = portruleset;
	}

	
	
}
