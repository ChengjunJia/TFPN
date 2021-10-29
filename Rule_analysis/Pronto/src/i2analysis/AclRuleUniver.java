package i2analysis;

import java.util.ArrayList;
import java.util.HashMap;

import stanalysis.PositionTuple;

public class AclRuleUniver {

	private HashMap<PositionTuple, ArrayList<Integer>> univeraclinpermit;//every port has many acl rules
	private HashMap<PositionTuple, ArrayList<Integer>> univeracloutpermit;
	private HashMap<PositionTuple, ArrayList<Integer>> univeraclindeny;
    private HashMap<PositionTuple, ArrayList<Integer>> univeracloutdeny;
    
    public AclRuleUniver(){
		this.univeracloutpermit=new HashMap<PositionTuple, ArrayList<Integer>>();
		this.univeraclindeny=new HashMap<PositionTuple, ArrayList<Integer>>();
		this.univeracloutdeny=new HashMap<PositionTuple, ArrayList<Integer>>();
		this.univeraclinpermit=new HashMap<PositionTuple, ArrayList<Integer>>();
    }

	public HashMap<PositionTuple, ArrayList<Integer>> getUniveraclinpermit() {
		return univeraclinpermit;
	}

	public void setUniveraclinpermit(HashMap<PositionTuple, ArrayList<Integer>> univeraclinpermit) {
		this.univeraclinpermit = univeraclinpermit;
	}

	public HashMap<PositionTuple, ArrayList<Integer>> getUniveracloutpermit() {
		return univeracloutpermit;
	}

	public void setUniveracloutpermit(HashMap<PositionTuple, ArrayList<Integer>> univeracloutpermit) {
		this.univeracloutpermit = univeracloutpermit;
	}

	public HashMap<PositionTuple, ArrayList<Integer>> getUniveraclindeny() {
		return univeraclindeny;
	}

	public void setUniveraclindeny(HashMap<PositionTuple, ArrayList<Integer>> univeraclindeny) {
		this.univeraclindeny = univeraclindeny;
	}

	public HashMap<PositionTuple, ArrayList<Integer>> getUniveracloutdeny() {
		return univeracloutdeny;
	}

	public void setUniveracloutdeny(HashMap<PositionTuple, ArrayList<Integer>> univeracloutdeny) {
		this.univeracloutdeny = univeracloutdeny;
	}
	
    
    
    
    
	
}
