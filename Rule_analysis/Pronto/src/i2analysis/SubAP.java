package i2analysis;

import java.util.ArrayList;
import java.util.HashMap;

public class SubAP {
	private int subapbdds;
	private HashMap<String,Boolean> rule;
	
	public SubAP() {}
	
	public SubAP(SubAP subap) {
		this.subapbdds = subap.getSubapbdds();
		/*if (ap.getSubAPset()!=null) {
			this.subAPset = new ArrayList<SubAP>();
			for (SubAP sap : ap.getSubAPset()) {
				this.subAPset.add(sap);
			}
		}
		*/
		
		
		//this.rule = subap.getRule();
		this.rule= new HashMap<String,Boolean>(subap.getRule());
	}
	
	public int getSubapbdds() {
		return subapbdds;
	}
	public void setSubapbdds(int subapbdds) {
		this.subapbdds = subapbdds;
	}
	public HashMap<String, Boolean> getRule() {
		return rule;
	}
	public void setRule(HashMap<String, Boolean> rule) {
		this.rule = rule;
	}
	
}