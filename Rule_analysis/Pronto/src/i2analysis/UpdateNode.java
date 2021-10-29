package i2analysis;

import java.util.ArrayList;

public class UpdateNode {
	UpdateNode parent ;
	ArrayList<UpdateNode> children;
	Rule rule;
	
	public UpdateNode(){
	children=new ArrayList<UpdateNode>();
	}
	
	public UpdateNode getParent() {	
		return parent;
	}
	public void setParent(UpdateNode parent) {
		this.parent = parent;
	}
	public ArrayList<UpdateNode> getChildren() {
		return children;
	}
	public void setChildren(ArrayList<UpdateNode> children) {
		this.children = children;
	}
	public Rule getRule() {
		return rule;
	}
	public void setRule(Rule rule) {
		this.rule = rule;
	}
	
	
	
}
