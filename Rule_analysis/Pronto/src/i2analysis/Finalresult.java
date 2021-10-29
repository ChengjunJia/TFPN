package i2analysis;

import java.util.HashSet;

public class Finalresult {
    private HashSet<Integer> ruleset;
	private Integer aclrule;
	private RefSet refset;
	
	public HashSet<Integer> getRuleset() {
		return ruleset;
	}
	public void setRuleset(HashSet<Integer> ruleset) {
		this.ruleset = ruleset;
	}
	public Integer getAclrule() {
		return aclrule;
	}
	public void setAclrule(Integer aclrule) {
		this.aclrule = aclrule;
	}
	public RefSet getRefset() {
		return refset;
	}
	public void setRefset(RefSet refset) {
		this.refset = refset;
	}
	
	
}
