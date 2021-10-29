package i2analysis;
import java.util.*;

public class Predicate {
    private String portname;
    private String devicename;
    private Integer fwbdds;
   // private HashMap<String, Integer> rule;
    private ArrayList<Rule> ruleset;
    private Integer Predicatename;//in order to update
    
    
    public Predicate(String portname, String devicename, Integer fwbdds, ArrayList<Rule> ruleset) {
    	this.portname = portname;
    	this.devicename=devicename;
    	this.fwbdds = fwbdds;
    	this.ruleset = ruleset;
   // 	this.ruleset.sort(new RuleComparatorHeader());
    }
    
	public String getPortname() {
		return portname;
	}
	public void setPortname(String portname) {
		this.portname = portname;
	}
	public Integer getFwbdds() {
		return fwbdds;
	}
	public void setFwbdds(Integer fwbdds) {
		this.fwbdds = fwbdds;
	}

	public ArrayList<Rule> getRuleset() {
		return ruleset;
	}

	public void setRuleset(ArrayList<Rule> ruleset) {
		this.ruleset = ruleset;
	}

	public String getDevicename() {
		return devicename;
	}

	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}

	public Integer getPredicatename() {
		return Predicatename;
	}

	public void setPredicatename(Integer predicatename) {
		Predicatename = predicatename;
	}
    
    
}