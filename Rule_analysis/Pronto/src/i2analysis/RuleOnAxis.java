package i2analysis;

public class RuleOnAxis {
	private Rule rule;
	private String headortail;
	private int[] Space;
	private int portid;
	
	 
    public RuleOnAxis(Rule rule, String headortail, int[] Space, int portid) {
		
    	this.rule= rule;
		this.headortail = headortail;
	//	this.subAPset = subAPset;
		this.Space=Space;
		this.portid=portid;
		}
	
		
		
	public Rule getRule() {
		return rule;
	}
		
	public void setRule(Rule rule) {
		this.rule = rule;
	}
    	
	public String getHeadortail() {
		return headortail;
	}

	public void setHeadortail(String headortail) {
		this.headortail = headortail;
	}

	public int[] getSpace() {
		return Space;
	}
	
	public void setSpace(int[] space) {
		Space = space;
	}
	
	public int getPortid() {
		return portid;
	}
	
	public void setPortid(int portid) {
		this.portid = portid;
	}
	
	
	
}
