package i2analysis;
import java.util.ArrayList;
import java.util.HashMap;

import stanalysis.PositionTuple;

public class AclRuleUniverDevice {

	
	private HashMap<String, ArrayList<ACLrule>> deviceaclinpermit;//every port has many acl rules
	private HashMap<String, ArrayList<ACLrule>> deviceacloutpermit;
	private HashMap<String, ArrayList<ACLrule>> deviceaclindeny;
    private HashMap<String, ArrayList<ACLrule>> deviceacloutdeny;//<port,ACLrule set>
    private HashMap<String,Integer> portinpermit;//<port,BDD>
    private HashMap<String,Integer> portoutpermit;
    private HashMap<String,Integer> portindeny;
    private HashMap<String,Integer> portoutdeny;
    private HashMap<Integer,HashMap<String, ACLrule>> vlandeaclinper;//<aclBDD,<port,ACLrule>>
    private HashMap<Integer,HashMap<String, ACLrule>> vlandeacloutper;
    private HashMap<Integer,HashMap<String, ACLrule>> vlandeaclindeny;
    private HashMap<Integer,HashMap<String, ACLrule>> vlandeacloutdeny;
    
    
    public AclRuleUniverDevice(String type){
    	if(type.equals("fwd")){
			this.deviceaclinpermit=new HashMap<String, ArrayList<ACLrule>>();
			this.deviceacloutpermit=new HashMap<String, ArrayList<ACLrule>>();
			this.deviceaclindeny=new HashMap<String, ArrayList<ACLrule>>();
			this.deviceacloutdeny=new HashMap<String, ArrayList<ACLrule>>();
    	}else{//vlan
    		this.vlandeaclinper=new HashMap<Integer,HashMap<String, ACLrule>>();
    		this.vlandeacloutper=new HashMap<Integer,HashMap<String, ACLrule>>();
    		this.vlandeaclindeny=new HashMap<Integer,HashMap<String, ACLrule>>();
    		this.vlandeacloutdeny=new HashMap<Integer,HashMap<String, ACLrule>>();
    	}
    	
    	this.portinpermit=new HashMap<String,Integer>();
		this.portoutpermit=new HashMap<String,Integer>();
		this.portindeny=new HashMap<String,Integer>();
		this.portoutdeny=new HashMap<String,Integer>();
    }

	public HashMap<String, ArrayList<ACLrule>> getDeviceaclinpermit() {
		return deviceaclinpermit;
	}

	public void setDeviceaclinpermit(HashMap<String, ArrayList<ACLrule>> deviceaclinpermit) {
		this.deviceaclinpermit = deviceaclinpermit;
	}

	public HashMap<String, ArrayList<ACLrule>> getDeviceacloutpermit() {
		return deviceacloutpermit;
	}

	public void setDeviceacloutpermit(HashMap<String, ArrayList<ACLrule>> deviceacloutpermit) {
		this.deviceacloutpermit = deviceacloutpermit;
	}

	public HashMap<String, ArrayList<ACLrule>> getDeviceaclindeny() {
		return deviceaclindeny;
	}

	public void setDeviceaclindeny(HashMap<String, ArrayList<ACLrule>> deviceaclindeny) {
		this.deviceaclindeny = deviceaclindeny;
	}

	public HashMap<String, ArrayList<ACLrule>> getDeviceacloutdeny() {
		return deviceacloutdeny;
	}

	public void setDeviceacloutdeny(HashMap<String, ArrayList<ACLrule>> deviceacloutdeny) {
		this.deviceacloutdeny = deviceacloutdeny;
	}

	public HashMap<String, Integer> getPortinpermit() {
		return portinpermit;
	}

	public void setPortinpermit(HashMap<String, Integer> portinpermit) {
		this.portinpermit = portinpermit;
	}

	public HashMap<String, Integer> getPortoutpermit() {
		return portoutpermit;
	}

	public void setPortoutpermit(HashMap<String, Integer> portoutpermit) {
		this.portoutpermit = portoutpermit;
	}

	public HashMap<String, Integer> getPortindeny() {
		return portindeny;
	}

	public void setPortindeny(HashMap<String, Integer> portindeny) {
		this.portindeny = portindeny;
	}

	public HashMap<String, Integer> getPortoutdeny() {
		return portoutdeny;
	}

	public void setPortoutdeny(HashMap<String, Integer> portoutdeny) {
		this.portoutdeny = portoutdeny;
	}

	public HashMap<Integer, HashMap<String, ACLrule>> getVlandeaclinper() {
		return vlandeaclinper;
	}

	public void setVlandeaclinper(HashMap<Integer, HashMap<String, ACLrule>> vlandeaclinper) {
		this.vlandeaclinper = vlandeaclinper;
	}

	public HashMap<Integer, HashMap<String, ACLrule>> getVlandeacloutper() {
		return vlandeacloutper;
	}

	public void setVlandeacloutper(HashMap<Integer, HashMap<String, ACLrule>> vlandeacloutper) {
		this.vlandeacloutper = vlandeacloutper;
	}

	public HashMap<Integer, HashMap<String, ACLrule>> getVlandeaclindeny() {
		return vlandeaclindeny;
	}

	public void setVlandeaclindeny(HashMap<Integer, HashMap<String, ACLrule>> vlandeaclindeny) {
		this.vlandeaclindeny = vlandeaclindeny;
	}

	public HashMap<Integer, HashMap<String, ACLrule>> getVlandeacloutdeny() {
		return vlandeacloutdeny;
	}

	public void setVlandeacloutdeny(HashMap<Integer, HashMap<String, ACLrule>> vlandeacloutdeny) {
		this.vlandeacloutdeny = vlandeacloutdeny;
	}

	
    
	
}
