package i2analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RefSet {
   private HashSet<Integer> fwdruleset;
   private ArrayList<Integer> fwdruleset2;
   private ArrayList<Integer> aclrulelist;
   private HashSet<Integer> denyBDD;
   private Integer bddafterdeny;
   private HashMap<Integer,Integer> Aclmap;//<aclname,fwdrulename> in order to update 
   
	
   public RefSet(){
	   this.fwdruleset=new HashSet<Integer>();
	   this.fwdruleset2=new ArrayList<Integer>();
	   this.aclrulelist=new ArrayList<Integer>();
	   this.denyBDD=new HashSet<Integer>();
	   this.Aclmap=new HashMap<Integer,Integer>();
   }

   public RefSet(RefSet refset){
	   //this.fwdruleset=refset.getFwdruleset();
	   this.fwdruleset2=refset.getFwdruleset2();
	   //this.aclrulelist=refset.getAclrulelist();
	   this.bddafterdeny=refset.getBddafterdeny();
	   this.Aclmap=refset.getAclmap();
   }
   
public HashSet<Integer> getFwdruleset() {
	return fwdruleset;
}

public void setFwdruleset(HashSet<Integer> fwdruleset) {
	this.fwdruleset = fwdruleset;
}



public ArrayList<Integer> getAclrulelist() {
	return aclrulelist;
}

public void setAclrulelist(ArrayList<Integer> aclrulelist) {
	this.aclrulelist = aclrulelist;
}

public HashSet<Integer> getDenyBDD() {
	return denyBDD;
}

public void setDenyBDD(HashSet<Integer> denyBDD) {
	this.denyBDD = denyBDD;
}

public Integer getBddafterdeny() {
	return bddafterdeny;
}

public void setBddafterdeny(Integer bddafterdeny) {
	this.bddafterdeny = bddafterdeny;
}

public ArrayList<Integer> getFwdruleset2() {
	return fwdruleset2;
}

public void setFwdruleset2(ArrayList<Integer> fwdruleset2) {
	this.fwdruleset2 = fwdruleset2;
}

public HashMap<Integer, Integer> getAclmap() {
	return Aclmap;
}

public void setAclmap(HashMap<Integer, Integer> aclmap) {
	Aclmap = aclmap;
}




   
   
}
