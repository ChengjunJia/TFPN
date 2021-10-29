package i2analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import StaticReachabilityAnalysis.ACLRule;

public class ACLForTree {
	private HashMap<String,HashMap<Integer,ArrayList<ACLrule>>> permitin;//<porttoport,hashmap<fwdrulename,list<aclrule>>>
	private HashMap<String,HashMap<Integer,ArrayList<ACLrule>>> permitinup;//<unique output port,>//this is due to the random case of the first one of the reachibility
	private HashMap<String,HashMap<Integer,ArrayList<ACLrule>>> denyin;//unique port
	private HashMap<String,HashMap<Integer,ArrayList<ACLrule>>> permitout;//unique port
	private HashMap<String,HashMap<Integer,ArrayList<ACLrule>>> denyout;//unique port
	
	
    private HashMap<String,Integer> portinpermit;//<port,BDD>
    private HashMap<String,Integer> portoutpermit;
    private HashMap<String,Integer> portindeny;
    private HashMap<String,Integer> portoutdeny;
    
    private HashMap<Integer,ACLrule> permitunr;//<aclrulename,aclrule>
    private HashMap<Integer,ACLrule> denyunr;
    
    private HashMap<Integer,Rule> univerhashmapvlan;//all fwding rule
	
	private HashMap<String,ArrayList<ACLrule>> peroutportacl;
	private HashMap<String,ArrayList<ACLrule>> deoutportacl;
    
    
    
	public ACLForTree(){
		this.permitin=new HashMap<String,HashMap<Integer,ArrayList<ACLrule>>>();
		this.permitinup=new HashMap<String,HashMap<Integer,ArrayList<ACLrule>>>();
		this.denyin=new HashMap<String,HashMap<Integer,ArrayList<ACLrule>>>();
		this.permitout=new HashMap<String,HashMap<Integer,ArrayList<ACLrule>>>();
		this.denyout=new HashMap<String,HashMap<Integer,ArrayList<ACLrule>>>();		
		
		this.portinpermit=new HashMap<String,Integer>();
		this.portoutpermit=new HashMap<String,Integer>();
		this.portindeny=new HashMap<String,Integer>();
		this.portoutdeny=new HashMap<String,Integer>();
		
		this.permitunr=new HashMap<Integer,ACLrule>();
		this.denyunr=new HashMap<Integer,ACLrule>();
		this.univerhashmapvlan=new HashMap<Integer,Rule>();
	}

	public HashMap<String, HashMap<Integer, ArrayList<ACLrule>>> getPermitin() {
		return permitin;
	}

	public void setPermitin(HashMap<String, HashMap<Integer, ArrayList<ACLrule>>> permitin) {
		this.permitin = permitin;
	}

	public HashMap<String, HashMap<Integer, ArrayList<ACLrule>>> getDenyin() {
		return denyin;
	}

	public void setDenyin(HashMap<String, HashMap<Integer, ArrayList<ACLrule>>> denyin) {
		this.denyin = denyin;
	}

	public HashMap<String, HashMap<Integer, ArrayList<ACLrule>>> getPermitout() {
		return permitout;
	}

	public void setPermitout(HashMap<String, HashMap<Integer, ArrayList<ACLrule>>> permitout) {
		this.permitout = permitout;
	}

	public HashMap<String, HashMap<Integer, ArrayList<ACLrule>>> getDenyout() {
		return denyout;
	}

	public void setDenyout(HashMap<String, HashMap<Integer, ArrayList<ACLrule>>> denyout) {
		this.denyout = denyout;
	}

	public HashMap<String, HashMap<Integer, ArrayList<ACLrule>>> getPermitinup() {
		return permitinup;
	}

	public void setPermitinup(HashMap<String, HashMap<Integer, ArrayList<ACLrule>>> permitinup) {
		this.permitinup = permitinup;
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

	public HashMap<Integer, ACLrule> getPermitunr() {
		return permitunr;
	}

	public void setPermitunr(HashMap<Integer, ACLrule> permitunr) {
		this.permitunr = permitunr;
	}

	public HashMap<Integer, ACLrule> getDenyunr() {
		return denyunr;
	}

	public void setDenyunr(HashMap<Integer, ACLrule> denyunr) {
		this.denyunr = denyunr;
	}

	public HashMap<Integer, Rule> getUniverhashmapvlan() {
		return univerhashmapvlan;
	}

	public void setUniverhashmapvlan(HashMap<Integer, Rule> univerhashmapvlan) {
		this.univerhashmapvlan = univerhashmapvlan;
	}

	public HashMap<String, ArrayList<ACLrule>> getPeroutportacl() {
		return peroutportacl;
	}

	public void setPeroutportacl(HashMap<String, ArrayList<ACLrule>> peroutportacl) {
		this.peroutportacl = peroutportacl;
	}

	public HashMap<String, ArrayList<ACLrule>> getDeoutportacl() {
		return deoutportacl;
	}

	public void setDeoutportacl(HashMap<String, ArrayList<ACLrule>> deoutportacl) {
		this.deoutportacl = deoutportacl;
	}

    

    
	
	
	
}
	/*
	private HashMap<PortPairACL,HashMap<Integer,ArrayList<ACLrule>>> permitin;
	private HashMap<PortPairACL,HashMap<Integer,ArrayList<ACLrule>>> denyin;
	private HashMap<PositionTuple,HashMap<Integer,ArrayList<ACLrule>>> permitout;
	private HashMap<PositionTuple,HashMap<Integer,ArrayList<ACLrule>>> denyout;
	
	public ACLForTree(){
		permitin=new HashMap<PortPairACL,HashMap<Integer,ArrayList<ACLrule>>>();
		denyin=new HashMap<PortPairACL,HashMap<Integer,ArrayList<ACLrule>>>();
		permitout=new HashMap<PositionTuple,HashMap<Integer,ArrayList<ACLrule>>>();
		denyout=new HashMap<PositionTuple,HashMap<Integer,ArrayList<ACLrule>>>();		
	}

	public HashMap<PortPairACL, HashMap<Integer, ArrayList<ACLrule>>> getPermitin() {
		return permitin;
	}

	public void setPermitin(HashMap<PortPairACL, HashMap<Integer, ArrayList<ACLrule>>> permitin) {
		this.permitin = permitin;
	}

	public HashMap<PortPairACL, HashMap<Integer, ArrayList<ACLrule>>> getDenyin() {
		return denyin;
	}

	public void setDenyin(HashMap<PortPairACL, HashMap<Integer, ArrayList<ACLrule>>> denyin) {
		this.denyin = denyin;
	}

	public HashMap<PositionTuple, HashMap<Integer, ArrayList<ACLrule>>> getPermitout() {
		return permitout;
	}

	public void setPermitout(HashMap<PositionTuple, HashMap<Integer, ArrayList<ACLrule>>> permitout) {
		this.permitout = permitout;
	}

	public HashMap<PositionTuple, HashMap<Integer, ArrayList<ACLrule>>> getDenyout() {
		return denyout;
	}

	public void setDenyout(HashMap<PositionTuple, HashMap<Integer, ArrayList<ACLrule>>> denyout) {
		this.denyout = denyout;
	}
	
}
*/
