package i2analysis;

import java.util.ArrayList;
import java.util.HashMap;

public class VlanPredicate {

	    private String devicename;
	    private Integer fwbdds;
	    private HashMap<Integer,ArrayList<Rule>> ruleset;//BDD,rule
	    private ArrayList<String> portname;
	    private HashMap<String,ArrayList<Rule>> portrule;//<port,ruleset> easy to the ruleset cover

	    public VlanPredicate(ArrayList<String> portname, String devicename, Integer fwbdds, HashMap<Integer,ArrayList<Rule>> ruleset,HashMap<String,ArrayList<Rule>> portrule) {
	    	this.portname = portname;
	    	this.devicename=devicename;
	    	this.fwbdds = fwbdds;
	    	this.ruleset = ruleset;
	    	this.portrule= portrule;
	   // 	this.ruleset.sort(new RuleComparatorHeader());
	    }

	    public VlanPredicate(){
	    	ruleset=new HashMap<Integer,ArrayList<Rule>>();
	    }
	    
		public String getDevicename() {
			return devicename;
		}

		public void setDevicename(String devicename) {
			this.devicename = devicename;
		}

		public Integer getFwbdds() {
			return fwbdds;
		}

		public void setFwbdds(Integer fwbdds) {
			this.fwbdds = fwbdds;
		}

		public HashMap<Integer, ArrayList<Rule>> getRuleset() {
			return ruleset;
		}

		public void setRuleset(HashMap<Integer, ArrayList<Rule>> ruleset) {
			this.ruleset = ruleset;
		}

		public ArrayList<String> getPortname() {
			return portname;
		}

		public void setPortname(ArrayList<String> portname) {
			this.portname = portname;
		}

		public HashMap<String, ArrayList<Rule>> getPortrule() {
			return portrule;
		}

		public void setPortrule(HashMap<String, ArrayList<Rule>> portrule) {
			this.portrule = portrule;
		}

        

        
	    
	    
}
