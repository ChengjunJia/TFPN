package i2analysis;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import stanalysis.ACLUse;
import stanalysis.APComputer;
import stanalysis.FWDAPSet;
import stanalysis.ForwardingRule;
import stanalysis.PositionTuple;
import stanalysis.Subnet;
import StaticReachabilityAnalysis.*;
import jdd.bdd.BDD;

public class Device {
	static BDDACLWrapper baw;
	//static int count=0;
	String name;
	// acl name to its id in acllib
	HashMap<String, LinkedList<ACLRule>> aclmap;
	ArrayList<ForwardingRule> fws;
	ArrayList<ForwardingRule> vlanfws;
	HashMap<String, Integer> rawacl;
	HashMap<String, ArrayList<Integer>> rawpermit;
	HashMap<String, ArrayList<Integer>> rawdeny;
	HashMap<String, HashMap<String, Integer>> Port_Rulebdds  = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, ArrayList<Rule>> Port_Rule  = new HashMap<String, ArrayList<Rule>>();
	HashMap <String, Integer> fwbdds;//port,bdd
	//HashMap <String, HashMap <String, Integer>> vlanfwbdds;
	HashMap <String, FWDAPSet> fwaps;
	HashMap <String,HashMap <String, FWDAPSet>> fwapsvlan;//<vlanid.<port,apset>>
	HashSet<Integer> rawaclinuse;
	ArrayList<ACLUse> acluses;
	HashMap <String, ArrayList<Subnet>> subnets;
	HashMap<String, HashSet<String>> vlan_ports;
	HashMap<String, Integer> inaclbdds;
	HashMap<String, Integer> outaclbdds;
	AclRuleUniverDevice aclruleuniverdevice;
	HashMap<String,AclRuleUniverDevice> vlanaclruledevice;
	HashMap<String, VlanPredicate> vlanpermap;
	ArrayList<Predicate> updatepredicate;
	HashMap<String,Predicate> updatepredicatemap=new HashMap<String,Predicate>();
	static Integer predicatename=0;
	public static int namecountac=0;
	public HashSet<UpdateNode> nofathernode=new HashSet<UpdateNode>();
	public HashMap<Integer,UpdateNode> removemap=new HashMap<Integer,UpdateNode>();
	HashMap<String,HashSet<Integer>> updateportapset=new HashMap<String,HashSet<Integer>>();
	
	
	
	public HashMap<String, HashSet<Integer>> getUpdateportapset() {
		return updateportapset;
	}

	public void setUpdateportapset(HashMap<String, HashSet<Integer>> updateportapset) {
		this.updateportapset = updateportapset;
	}

	public HashMap<String, Predicate> getUpdatepredicatemap() {
		return updatepredicatemap;
	}

	public void setUpdatepredicatemap(HashMap<String, Predicate> updatepredicatemap) {
		this.updatepredicatemap = updatepredicatemap;
	}

	public HashMap<Integer,UpdateNode> getRemovemap(){
		return removemap;
	}
	
	public HashSet<UpdateNode> getNofathernode(){
		return nofathernode;
	}
	
	
	public void addACL(String name, LinkedList<ACLRule> acl)
	{
		aclmap.put(name, acl);
	}

	public String get_name()
	{
		return name;
	}
	
	public List <Predicate> getpredicate(BDDACLWrapper bddengine)
	{
		//BDDACLWrapper bddengine;
	//	BDD thebdd = bddengine.getBDD();
		
		/*
		Set<Entry<String,Integer>> set = fwbdds.entrySet();
		Iterator<Entry<String,Integer>> iter = set.iterator();
		List <Predicate> predic = new ArrayList<Predicate>();
		while (iter.hasNext()) {
	        Entry<String,Integer> entry = iter.next();
			String portname = entry.getKey();
			Integer fwbd = entry.getValue();
			Predicate p = new Predicate(portname, fwbd, Port_Rulebdds.get(portname));
			predic.add(p);
	    }
		*/
		Set<Entry<String,Integer>> set = fwbdds.entrySet();
		Iterator<Entry<String,Integer>> iter = set.iterator();
		List <Predicate> predic = new ArrayList<Predicate>();
		while (iter.hasNext()) {
			Entry<String,Integer> entry = iter.next();
			String portname = entry.getKey();
			Integer fwbd = entry.getValue();
			Predicate p = new Predicate(portname, name, fwbd, Port_Rule.get(portname));
			p.setPredicatename(predicatename++);
			predic.add(p);
			updatepredicatemap.put(portname,p);
		}
		
		this.updatepredicate=new ArrayList<Predicate>(predic);
		
		return predic;
	}
	
	public HashMap <String, Integer> get_fwbdds_map ()
	{
		return fwbdds;
	}
	
	public HashMap<String, HashMap<String, Integer>> get_Port_Rulebdds_map()
	{
		return Port_Rulebdds;
	}
	
	public HashMap <String, FWDAPSet> get_fwaps_map()
	{
		return fwaps;
	}

	public Device(String dname)
	{
		this.name = dname;
		fws = new ArrayList<ForwardingRule>();
		acluses = new ArrayList<ACLUse>();
		aclmap = new HashMap<String, LinkedList<ACLRule>>();
		this.vlanfws=new ArrayList<ForwardingRule>();
		vlan_ports = new HashMap<String, HashSet<String>>();
	}

	public void show_fwd_bddsize()
	{
		System.out.println(name);
		int total_size = 0;
		for(Integer one_p : fwbdds.values())
		{
			total_size = total_size + baw.getNodeSize(one_p);
		}
		System.out.println(total_size);
	}


	public static void setBDDWrapper(BDDACLWrapper baw)
	{
		Device.baw = baw;
	}


	/**
	 * 
	 * @param port
	 * @param fwdaps
	 * @return <portname, ap set>
	 */
	public HashMap <String, FWDAPSet> FowrdAction(String port, FWDAPSet fwdaps)
	{
		HashMap <String, FWDAPSet> fwded = new HashMap<String, FWDAPSet>();//<port,APset>


		//System.out.println(fwaps.keySet());

		Iterator iter = fwaps.entrySet().iterator();
		while(iter.hasNext())
		{
			Map.Entry entry = (Map.Entry) iter.next();
			String otherport = (String) entry.getKey();
			//
			if(!otherport.equals(port))//select the others port of the device
			{
				FWDAPSet fwtmp1 = new FWDAPSet(fwdaps);
				fwtmp1.intersect((FWDAPSet) entry.getValue());
				if(!fwtmp1.isempty())
				{

					fwded.put(otherport, fwtmp1);			
				}
			}
		}

		return fwded;
	}
	
	public HashMap <String, FWDAPSet> FowrdActionVlan(String port, FWDAPSet fwdaps, String vlanID)
	{
		HashMap <String, FWDAPSet> fwded = new HashMap<String, FWDAPSet>();//<port,APset>


		//System.out.println(fwaps.keySet());
        if(!fwapsvlan.containsKey(vlanID)){
        	return fwded;
        }
		
		Iterator iter = fwapsvlan.get(vlanID).entrySet().iterator();
//		HashSet<Integer> APset=fwapsvlan.get(vlanID).get("gi4/16").getApset();
		while(iter.hasNext())
		{
			Map.Entry entry = (Map.Entry) iter.next();
			String otherport = (String) entry.getKey();
			//
			if(!otherport.equals(port))//select the others port of the device
			{
				FWDAPSet fwtmp1 = new FWDAPSet(fwdaps);
				fwtmp1.intersect((FWDAPSet) entry.getValue());
				if(!fwtmp1.isempty())
				{
					fwded.put(otherport, fwtmp1);			
				}
			}
		}

		return fwded;//<port,the apset after intersect>
	}

	public FWDAPSet FowrdAction(String inport, String outport, FWDAPSet fwdaps)
	{
		if(fwaps.containsKey(outport))
		{
			FWDAPSet fwtmp = new FWDAPSet(fwdaps);
			fwtmp.intersect(fwaps.get(outport));
			if(fwtmp.isempty())
			{
				return null;
			}else
			{
				return fwtmp;
			}
		}else
		{
			return null;
		}
	}

	/**
	 * forwarding, acls
	 * @param apc
	 */
	public void setaps(APComputer apc)
	{
		fwaps = new HashMap <String, FWDAPSet>();
		setaps_1(fwaps, fwbdds, apc);//fwbdds（单个设备所对应）的key是端口名字，value是端口对应的bdd
		//fwbdds is the HashMap <String, Integer>, portname and BDD
	}
	
	public void setapsupdate(APComputer apc)
	{
		fwaps = new HashMap <String, FWDAPSet>();
		setaps_1_update(fwaps, fwbdds, apc);//fwbdds（单个设备所对应）的key是端口名字，value是端口对应的bdd
		//fwbdds is the HashMap <String, Integer>, portname and BDD
	}
	
	public void setapsupdateremove(APComputer apc)
	{
		fwaps = new HashMap <String, FWDAPSet>();
		setaps_1_updateremove(fwaps, fwbdds, apc);//fwbdds（单个设备所对应）的key是端口名字，value是端口对应的bdd
		//fwbdds is the HashMap <String, Integer>, portname and BDD
	}
	
	public void setvlanaps(APComputer apc)
	{
		fwapsvlan = new HashMap <String,HashMap <String, FWDAPSet>>();
		setaps_1_vlan(fwapsvlan, vlanpermap, apc);//fwbdds（单个设备所对应）的key是端口名字，value是端口对应的bdd
		//fwbdds is the HashMap <String, Integer>, portname and BDD

	}

	private void setaps_1(HashMap<String, FWDAPSet> filteraps, 
			HashMap<String, Integer> filterbdds, APComputer apc)
	{
		for(String portname : filterbdds.keySet())
		{
			HashSet<Integer> rawset = apc.getAPExpComputed(filterbdds.get(portname));////output all the Ap of the port
			if(rawset == null)
			{
				System.err.println("bdd expression not found!");
				System.exit(1);
			}else
			{
				FWDAPSet faps = new FWDAPSet(rawset);//put the output ap in to the FWDAPSet, if the ap size is over the half of the total size of ap, it needs complement.
				filteraps.put(portname, faps);//the portname is the device's portname
			}
		}
	}
	
	private void setaps_1_update(HashMap<String, FWDAPSet> filteraps, 
			HashMap<String, Integer> filterbdds, APComputer apc)
	{
		for(String portname : filterbdds.keySet())
		{
			//HashSet<Integer> rawset = apc.getAPExpComputed(filterbdds.get(portname));////output all the Ap of the port
			HashSet<Integer> rawset = updateportapset.get(portname);
			
			
			if(rawset == null)
			{
			//	System.err.println("bdd expression not found!");
			//	System.exit(1);
			}else
			{
				FWDAPSet faps = new FWDAPSet(rawset);//put the output ap in to the FWDAPSet, if the ap size is over the half of the total size of ap, it needs complement.
				filteraps.put(portname, faps);//the portname is the device's portname
				
			}
		}
	}
	
	private void setaps_1_updateremove(HashMap<String, FWDAPSet> filteraps, 
			HashMap<String, Integer> filterbdds, APComputer apc)
	{
		for(String portname : filterbdds.keySet())
		{
			//HashSet<Integer> rawset = apc.getAPExpComputed(filterbdds.get(portname));////output all the Ap of the port
			HashSet<Integer> rawset = updateportapset.get(portname);
			
		//	if(rawset.contains(792329)){
		//		System.out.println("bingo");
		//	}
			
			if(rawset == null)
			{
				//System.err.println("bdd expression not found!");
			//	System.exit(1);
			}else
			{
				FWDAPSet faps = new FWDAPSet(rawset);//put the output ap in to the FWDAPSet, if the ap size is over the half of the total size of ap, it needs complement.
				filteraps.put(portname, faps);//the portname is the device's portname
				
			}
		}
	}
	
	private void setaps_1_vlan(HashMap <String,HashMap <String, FWDAPSet>> filteraps, 
			HashMap<String, VlanPredicate> filterbdds, APComputer apc)
	{
		for(String VlanID:filterbdds.keySet())
		{
			
			HashMap <String, FWDAPSet> hp=new HashMap <String, FWDAPSet>();
			filteraps.put(VlanID, hp);
			
			for(String portname: vlan_ports.get(VlanID))
			{
				HashSet<Integer> rawset = apc.getvlanAPExpComputed(filterbdds.get(VlanID).getFwbdds(),VlanID);////output all the Ap of the port
				if(rawset == null)
				{
					System.err.println("bdd expression not found!");
					//System.exit(1);
				}else
				{
					FWDAPSet faps = new FWDAPSet(rawset);//put the output ap in to the FWDAPSet, if the ap size is over the half of the total size of ap, it needs complement.
					hp.put(portname, faps);//the portname is the device's portname
				}
				
			}
		}
		

	}

	public Collection<Integer> getfwbdds()
	{
		return fwbdds.values();
	}
	
	

	public Collection<FWDAPSet> getfwaps()
	{
		return fwaps.values();
	}
	
	

	public void computeFWBDDs(HashMap<Integer,Rule> univerhashmap,ACLForTree aclfortree,HashMap<String ,ACLForTree> aclfortreevlan, Integer ruleid, ForwardingRule remaintoupdate,int init) throws IOException
	{
		Collections.sort(fws);//对fws 的len进行排序了，第一个device的fws一共有8000多条rule
		//this.ruleset= new ArrayList<Rule>();
		//this.fwbdds = Device.baw.getfwdbdds_sorted_no_store(fws,this.Port_Rulebdds,this.name,ruleset);

		this.fwbdds = Device.baw.getfwdbdds_sorted_no_store(fws,this.Port_Rule,this.name,univerhashmap,aclruleuniverdevice,aclfortree,nofathernode,removemap,ruleid,remaintoupdate);
		this.vlanpermap=Device.baw.getfwdbdds_sorted_no_store_vlan(vlanfws,this.Port_Rule,this.name,univerhashmap, vlanaclruledevice, aclfortreevlan,vlan_ports);
		//this.fwbdds = Device.baw.getfwdbdds_sorted_no_store(fws,this.Port_Rule,this.name,univerhashmap);
		
	//	System.out.println("device"+name);
		System.out.println("fwssize"+fws.size());
	//	if (this.name=="newy32aoa"){
		//	System.out.println("bingo");
		//}
		//this.fwbdds = Device.baw.getfwdbdds(fws);
		
		//System.out.println(fwbdds.size());
		//for(String iname : fwbdds.keySet())
		//{
		//	System.out.println(iname + ": " + fwbdds.get(iname));
		//}
	}

	public void addFW(ForwardingRule fw)
	{
		fws.add(fw);
	}

	public void computeACLBDDs(ACLForTree aclfortree, HashMap<String ,ACLForTree> aclfortreevlan)//fix by yu zhao 6/1/2016
	{
		this.aclruleuniverdevice=new AclRuleUniverDevice("fwd");
		this.vlanaclruledevice=new HashMap<String,AclRuleUniverDevice>();
		computeRawACL();//compute the ACLBDD, do not consider the override
		rawaclinuse = new HashSet<Integer>();
		for(int i = 0; i < acluses.size(); i ++)
		{
			ACLUse oneacluse = acluses.get(i);
			if(oneacluse.getinterface().startsWith("vlan")){	
				String port=oneacluse.getinterface();
				ACLForTree aclforvlan;
				if(aclfortreevlan.containsKey(port)){//port is the vlanID//check the aclfortreevlan has vlanID or not, the vlanID are filled into it in the network
				aclforvlan=aclfortreevlan.get(port);
				}else{
					continue;
				}
				
				HashSet<String> phyportset=vlan_ports.get(port);//find all the phyports
				if(oneacluse.isin()){//oneacluse is the aclrule as vlanused:199 vlan710 in,number is 199
					
					if(!rawpermit.containsKey(oneacluse.getnumber())){//HashMap<String, ArrayList<Integer>> rawpermit;
						continue;
					}
					
					if(!rawpermit.get(oneacluse.getnumber()).isEmpty()){//permitin do not void,this for is permit
						AclRuleUniverDevice vlandevice;//set a ACLrule object in vlanaclruledevice
						
						    if(vlanaclruledevice.containsKey(port)){
						    	vlandevice=vlanaclruledevice.get(port);
						    }else{
						    	vlandevice=new AclRuleUniverDevice("vlan");
						    	vlanaclruledevice.put(port, vlandevice);//build a new vlanaclruledevice with new vlanid, and new vlandevice
						    }
						    ArrayList<Integer> rawpiList =rawpermit.get(oneacluse.getnumber());
						    for(String phyport:phyportset){//every port in the vlan					    	
							//	ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
								    for(int j=0;j<rawpiList.size()-1;j++){
									    	ACLrule aclrule=new ACLrule();
									
									    	aclrule.setRulename(namecountac++);//set rule name,build aclrule
									    	/*
									    	if(namecountac==612)
									    	{
									    		System.out.println("test");
									    	}
									    	*/
									    	aclrule.setFwdBDD(rawpiList.get(j));//set rule's BDD
									    	aclrule.setPortname(phyport);//set rule's port
									    //	newlist.add(aclrule);
									    	/////////////////
									    	if(!vlandevice.getVlandeaclinper().containsKey(rawpiList.get(j))){
									    		HashMap<String, ACLrule> newHM=new HashMap<String, ACLrule>();
									    		vlandevice.getVlandeaclinper().put(rawpiList.get(j), newHM);//build
									    	}
									    	vlandevice.getVlandeaclinper().get(rawpiList.get(j)).put(phyport, aclrule);//put
									    	//////////////////
									    	aclforvlan.getPermitunr().put(aclrule.getRulename(),aclrule);//put the aclrule into the aclfortreevlan's univer
									  }
								            //multi- used acl table
									    if(!vlandevice.getPortinpermit().containsKey(phyport)){//first time of this port, there more than one acl rule on this device
									    	 vlandevice.getPortinpermit().put(phyport,rawpiList.get(rawpiList.size()-1));//port's bdd
									    	 String newport=new String(name+phyport);//the univer tree need univer name
									    	 aclforvlan.getPortinpermit().put(newport, rawpiList.get(rawpiList.size()-1));//port's bdd
									    //	 vlandevice.getDeviceaclinpermit().put(phyport,newlist);//put aclrulelist in to the list			//getDeviceaclinpermit()!!	     
									     }else{
									    //	 vlandevice.getDeviceaclinpermit().get(phyport).addAll(newlist);
									    	 int andresult=baw.getBDD().and( vlandevice.getPortinpermit().get(phyport),rawpiList.get(rawpiList.size()-1));
									    	 baw.getBDD().ref(andresult);//may have two deref
									    	 vlandevice.getPortinpermit().put(phyport,andresult);
									    	 String newport=new String(name+phyport);
									    	 aclforvlan.getPortinpermit().put(newport, andresult);
										     }
								    
								    }
						    
							  }
					////////////////////////////////////////
						if(!rawdeny.get(oneacluse.getnumber()).isEmpty()){//permitin do not void,this for is deny
							
							AclRuleUniverDevice vlandevice;//set a ACLrule object in vlanaclruledevice
							
						    if(vlanaclruledevice.containsKey(port)){//port means vlanid
						    	vlandevice=vlanaclruledevice.get(port);
						    }else{
						    	vlandevice=new AclRuleUniverDevice("vlan");
						    	vlanaclruledevice.put(port, vlandevice);
						    }
						    
						    ArrayList<Integer> rawdiList =rawdeny.get(oneacluse.getnumber());
						    
						    for(String phyport:phyportset){
						
						//		ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
								    for(int j=0;j<rawdiList.size()-1;j++){
								    	ACLrule aclrule=new ACLrule();
								    	aclrule.setRulename(namecountac++);
								    	
								    	aclrule.setFwdBDD(rawdiList.get(j));
								    	aclrule.setPortname(phyport);
								    	///////////////
								    	if(!vlandevice.getVlandeaclindeny().containsKey(rawdiList.get(j))){
								    		HashMap<String, ACLrule> newHM=new HashMap<String, ACLrule>();
								    		vlandevice.getVlandeaclindeny().put(rawdiList.get(j), newHM);//build
								    	}
								    	vlandevice.getVlandeaclindeny().get(rawdiList.get(j)).put(phyport, aclrule);//put
								    	//////////////////
								    	//aclforvlan.getDenyunr().put(aclrule.getRulename(),aclrule);//put the aclrule into the aclfortreevlan's univer
								    	
							//	    	newlist.add(aclrule);
								    	aclforvlan.getDenyunr().put(aclrule.getRulename(),aclrule);
								    	}

							     if(!vlandevice.getPortindeny().containsKey(phyport)){//first time of this port, there more than one acl rule on this device
							    	 vlandevice.getPortindeny().put(phyport,rawdiList.get(rawdiList.size()-1));
							    	 String newport=new String(name+phyport);
							    	 aclforvlan.getPortindeny().put(newport, rawdiList.get(rawdiList.size()-1));
							    	// vlandevice.getDeviceaclindeny().put(phyport,newlist);
							    	 
							     }else{
							    //	 vlandevice.getDeviceaclindeny().get(phyport).addAll(newlist);
							    	 int andresult=baw.getBDD().and(vlandevice.getPortindeny().get(phyport),rawdiList.get(rawdiList.size()-1));
							    	 baw.getBDD().ref(andresult);//may have two deref
							    	 vlandevice.getPortindeny().put(phyport,andresult);
							    	 String newport=new String(name+phyport);
							    	 aclforvlan.getPortindeny().put(newport, andresult);
								     } 
						  }
						}

				}else{
					if(!rawpermit.containsKey(oneacluse.getnumber())){
						continue;
					}
					
					
					if(!rawpermit.get(oneacluse.getnumber()).isEmpty()){//permitout do not void
						AclRuleUniverDevice vlandevice;//set a ACLrule object in vlanaclruledevice
						
					    if(vlanaclruledevice.containsKey(port)){
					    	vlandevice=vlanaclruledevice.get(port);
					    }else{
					    	vlandevice=new AclRuleUniverDevice("vlan");
					    	vlanaclruledevice.put(port, vlandevice);
					    }
					    
					    ArrayList<Integer> rawpoList =rawpermit.get(oneacluse.getnumber());
					    
					    for(String phyport:phyportset){
					    
						//	ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
							    for(int j=0;j<rawpoList.size()-1;j++){
							    	ACLrule aclrule=new ACLrule();
							    	aclrule.setRulename(namecountac++);
						//	     	if(namecountac==612)
						//	    	{
						//	    		System.out.println("test");
					//		    	}
							    	aclrule.setFwdBDD(rawpoList.get(j));
							    	aclrule.setPortname(phyport);
							    	////////////////////
							    	if(!vlandevice.getVlandeacloutper().containsKey(rawpoList.get(j))){
							    		HashMap<String, ACLrule> newHM=new HashMap<String, ACLrule>();
							    		vlandevice.getVlandeacloutper().put(rawpoList.get(j), newHM);//build
							    	}
							    	vlandevice.getVlandeacloutper().get(rawpoList.get(j)).put(phyport, aclrule);//put
							    	//////////////////
							    	aclforvlan.getPermitunr().put(aclrule.getRulename(),aclrule);//put the aclrule into the aclfortreevlan's univer
							    	}

					     if(!vlandevice.getPortoutpermit().containsKey(phyport)){//first time of this port, there more than one acl rule on this device
					    	 vlandevice.getPortoutpermit().put(phyport,rawpoList.get(rawpoList.size()-1));
					    	 String newport=new String(name+phyport);
					    	 aclforvlan.getPortoutpermit().put(newport, rawpoList.get(rawpoList.size()-1));
					    //	 vlandevice.getDeviceacloutpermit().put(phyport,newlist);				     
					     }else{
					    //	 vlandevice.getDeviceacloutpermit().get(phyport).addAll(newlist);
					    	 int andresult=baw.getBDD().and( vlandevice.getPortoutpermit().get(phyport),rawpoList.get(rawpoList.size()-1));
					    	 baw.getBDD().ref(andresult);//may have two deref
					    	 vlandevice.getPortoutpermit().put(phyport,andresult);
					    	 String newport=new String(name+phyport);
					    	 aclforvlan.getPortoutpermit().put(newport, andresult);
						     } 
						  }
					}
					//
						if(!rawdeny.get(oneacluse.getnumber()).isEmpty()){//permitin do not void
							
                            AclRuleUniverDevice vlandevice;//set a ACLrule object in vlanaclruledevice
							
						    if(vlanaclruledevice.containsKey(port)){
						    	vlandevice=vlanaclruledevice.get(port);
						    }else{
						    	vlandevice=new AclRuleUniverDevice("vlan");
						    	vlanaclruledevice.put(port, vlandevice);
						    }
						    
						    ArrayList<Integer> rawdoList =rawdeny.get(oneacluse.getnumber());
						    
						    for(String phyport:phyportset){
							//	ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
								    for(int j=0;j<rawdoList.size()-1;j++){
								    	ACLrule aclrule=new ACLrule();
								    	aclrule.setRulename(namecountac++);
						//		    	if(namecountac==612)
							//	    	{
							//	    		System.out.println("test");
							//	    	}
								    	aclrule.setFwdBDD(rawdoList.get(j));
								    	aclrule.setPortname(phyport);
								    	///////////
								    	if(!vlandevice.getVlandeacloutdeny().containsKey(rawdoList.get(j))){
								    		HashMap<String, ACLrule> newHM=new HashMap<String, ACLrule>();
								    		vlandevice.getVlandeacloutdeny().put(rawdoList.get(j), newHM);//build
								    	}
								    	vlandevice.getVlandeacloutdeny().get(rawdoList.get(j)).put(phyport, aclrule);//put
								    	//////////////////
								    	aclforvlan.getDenyunr().put(aclrule.getRulename(),aclrule);
								    	}
			
						     if(!vlandevice.getPortoutdeny().containsKey(phyport)){//first time of this port, there more than one acl rule on this device
						    	 vlandevice.getPortoutdeny().put(phyport,rawdoList.get(rawdoList.size()-1));
						    	 String newport=new String(name+phyport);
						    	 aclforvlan.getPortoutdeny().put(newport, rawdoList.get(rawdoList.size()-1));
						//    	 vlandevice.getDeviceacloutdeny().put(phyport,newlist);				     
						     }else{
						  //  	 vlandevice.getDeviceacloutdeny().get(phyport).addAll(newlist);
						    	 int andresult=baw.getBDD().and(vlandevice.getPortoutdeny().get(phyport),rawdoList.get(rawdoList.size()-1));
						    	 baw.getBDD().ref(andresult);//may have two deref
						    	 vlandevice.getPortoutdeny().put(phyport,andresult);
						    	 String newport=new String(name+phyport);
						    	 aclforvlan.getPortoutdeny().put(newport, andresult);
							     } 
					  }
						}		
		     		}

			}else{
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////no vlan
			String port=oneacluse.getinterface();
			if(oneacluse.isin()){
				if(!rawpermit.get(oneacluse.getnumber()).isEmpty()){//permitin do not void
					    ArrayList<Integer> rawpiList =rawpermit.get(oneacluse.getnumber());
							ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
							    for(int j=0;j<rawpiList.size()-1;j++){
							    	ACLrule aclrule=new ACLrule();
							    	aclrule.setRulename(namecountac++);
							    	aclrule.setFwdBDD(rawpiList.get(j));
							    	aclrule.setPortname(port);
							    	newlist.add(aclrule);
							    	aclfortree.getPermitunr().put(aclrule.getRulename(),aclrule);
							    	}
	
					     if(!aclruleuniverdevice.getDeviceaclinpermit().containsKey(port)){//first time of this port, there more than one acl rule on this device
					    	 aclruleuniverdevice.getPortinpermit().put(port,rawpiList.get(rawpiList.size()-1));
					    	 String newport=new String(name+port);
					    	 aclfortree.getPortinpermit().put(newport, rawpiList.get(rawpiList.size()-1));
					    	 aclruleuniverdevice.getDeviceaclinpermit().put(port,newlist);				     
					     }else{
					    	 aclruleuniverdevice.getDeviceaclinpermit().get(port).addAll(newlist);
					    	 int andresult=baw.getBDD().and( aclruleuniverdevice.getPortinpermit().get(port),rawpiList.get(rawpiList.size()-1));
					    	 baw.getBDD().ref(andresult);//may have two deref
					    	 aclruleuniverdevice.getPortinpermit().put(port,andresult);
					    	 String newport=new String(name+port);
					    	 aclfortree.getPortinpermit().put(newport, andresult);
						     }

						  }
					if(!rawdeny.get(oneacluse.getnumber()).isEmpty()){//permitin do not void
					    ArrayList<Integer> rawdiList =rawdeny.get(oneacluse.getnumber());
							ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
							    for(int j=0;j<rawdiList.size()-1;j++){
							    	ACLrule aclrule=new ACLrule();
							    	aclrule.setRulename(namecountac++);
							    	aclrule.setFwdBDD(rawdiList.get(j));
							    	aclrule.setPortname(port);
							    	newlist.add(aclrule);
							    	aclfortree.getDenyunr().put(aclrule.getRulename(),aclrule);
							    	}

						     if(!aclruleuniverdevice.getDeviceaclindeny().containsKey(port)){//first time of this port, there more than one acl rule on this device
						    	 aclruleuniverdevice.getPortindeny().put(port,rawdiList.get(rawdiList.size()-1));
						    	 String newport=new String(name+port);
						    	 aclfortree.getPortindeny().put(newport, rawdiList.get(rawdiList.size()-1));
						    	 aclruleuniverdevice.getDeviceaclindeny().put(port,newlist);
						    	 
						     }else{
						    	 aclruleuniverdevice.getDeviceaclindeny().get(port).addAll(newlist);
						    	 int andresult=baw.getBDD().and(aclruleuniverdevice.getPortindeny().get(port),rawdiList.get(rawdiList.size()-1));
						    	 baw.getBDD().ref(andresult);//may have two deref
						    	 aclruleuniverdevice.getPortindeny().put(port,andresult);
						    	 String newport=new String(name+port);
						    	 aclfortree.getPortindeny().put(newport, andresult);
							     } 
					  }

			}else{
				if(!rawpermit.get(oneacluse.getnumber()).isEmpty()){//permitin do not void
				    ArrayList<Integer> rawpoList =rawpermit.get(oneacluse.getnumber());
						ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
						    for(int j=0;j<rawpoList.size()-1;j++){
						    	ACLrule aclrule=new ACLrule();
						    	aclrule.setRulename(namecountac++);
						    	aclrule.setFwdBDD(rawpoList.get(j));
						    	aclrule.setPortname(port);
						    	newlist.add(aclrule);
						    	aclfortree.getPermitunr().put(aclrule.getRulename(),aclrule);
						    	}

				     if(!aclruleuniverdevice.getDeviceacloutpermit().containsKey(port)){//first time of this port, there more than one acl rule on this device
				    	 aclruleuniverdevice.getPortoutpermit().put(port,rawpoList.get(rawpoList.size()-1));
				    	 String newport=new String(name+port);
				    	 aclfortree.getPortoutpermit().put(newport, rawpoList.get(rawpoList.size()-1));
				    	 aclruleuniverdevice.getDeviceacloutpermit().put(port,newlist);				     
				     }else{
				    	 aclruleuniverdevice.getDeviceacloutpermit().get(port).addAll(newlist);
				    	 int andresult=baw.getBDD().and( aclruleuniverdevice.getPortoutpermit().get(port),rawpoList.get(rawpoList.size()-1));
				    	 baw.getBDD().ref(andresult);//may have two deref
				    	 aclruleuniverdevice.getPortoutpermit().put(port,andresult);
				    	 String newport=new String(name+port);
				    	 aclfortree.getPortoutpermit().put(newport, andresult);
					     } 
					  }
					if(!rawdeny.get(oneacluse.getnumber()).isEmpty()){//permitin do not void
					    ArrayList<Integer> rawdoList =rawdeny.get(oneacluse.getnumber());
							ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
							    for(int j=0;j<rawdoList.size()-1;j++){
							    	ACLrule aclrule=new ACLrule();
							    	aclrule.setRulename(namecountac++);
							    	aclrule.setFwdBDD(rawdoList.get(j));
							    	aclrule.setPortname(port);
							    	newlist.add(aclrule);
							    	aclfortree.getDenyunr().put(aclrule.getRulename(),aclrule);
							    	}
		
					     if(!aclruleuniverdevice.getDeviceacloutdeny().containsKey(port)){//first time of this port, there more than one acl rule on this device
					    	 aclruleuniverdevice.getPortoutdeny().put(port,rawdoList.get(rawdoList.size()-1));
					    	 String newport=new String(name+port);
					    	 aclfortree.getPortoutdeny().put(newport, rawdoList.get(rawdoList.size()-1));
					    	 aclruleuniverdevice.getDeviceacloutdeny().put(port,newlist);				     
					     }else{
					    	 aclruleuniverdevice.getDeviceacloutdeny().get(port).addAll(newlist);
					    	 int andresult=baw.getBDD().and(aclruleuniverdevice.getPortoutdeny().get(port),rawdoList.get(rawdoList.size()-1));
					    	 baw.getBDD().ref(andresult);//may have two deref
					    	 aclruleuniverdevice.getPortoutdeny().put(port,andresult);
					    	 String newport=new String(name+port);
					    	 aclfortree.getPortoutdeny().put(newport, andresult);
						     } 
				  }
		   				
	     		}
			}
			
		 }//end for
	//	boolean aaa=aclfortreevlan.get("vlan488").getPermitunr().containsKey(612);		    
	//	System.out.println(aaa);
		}

			
			
			
			/*
			
			
			String port=null;
			if(!oneacluse.getinterface().substring(0,4).equals("vlan")){//need vlan 6/1/2016				
					port=oneacluse.getinterface();
							
					if(oneacluse.isin())
					{
						if(!rawpermit.get(oneacluse.getnumber()).isEmpty()){
							ArrayList<Integer> rawperlist =rawpermit.get(oneacluse.getnumber());
			
							//	ArrayList<Integer> rawperlist =rawpermit.get(oneacluse.getnumber());
								PositionTuple pt= new PositionTuple(name,port);
								if (!aclruleuniver.getUniveraclinpermit().containsKey(pt))//if the pt isn't existing
								{aclruleuniver.getUniveraclinpermit().put(pt,rawperlist);
								
								}else{//if the pt is existing
									int size0=aclruleuniver.getUniveraclinpermit().get(pt).size();
									int size1=rawperlist.size();
									int lastbdd=aclruleuniver.getUniveraclinpermit().get(pt).get(size0-1);
									int newbdd=baw.getBDD().and(rawperlist.get(size1-1),lastbdd);
									baw.getBDD().ref(newbdd);
									aclruleuniver.getUniveraclinpermit().get(pt).remove(size0-1);
									rawperlist.remove(size1-1);
									aclruleuniver.getUniveraclinpermit().get(pt).addAll(rawperlist);//remove the end and merge the list
									aclruleuniver.getUniveraclinpermit().get(pt).add(newbdd);//add the end		
								}
								
					
						}
						if(!rawdeny.get(oneacluse.getnumber()).isEmpty()){
							ArrayList<Integer> rawdelist =rawdeny.get(oneacluse.getnumber());
			
							//	ArrayList<Integer> rawperlist =rawpermit.get(oneacluse.getnumber());
								PositionTuple pt= new PositionTuple(name,port);
								if (!aclruleuniver.getUniveraclindeny().containsKey(pt))//if the pt isn't existing
								{aclruleuniver.getUniveraclindeny().put(pt,rawdelist);
	
								}else{//if the pt is existing
									int size0=aclruleuniver.getUniveraclindeny().get(pt).size();
									int size1=rawdelist.size();
									int lastbdd=aclruleuniver.getUniveraclindeny().get(pt).get(size0-1);
									int newbdd=baw.getBDD().and(rawdelist.get(size1-1),lastbdd);
									baw.getBDD().ref(newbdd);
									aclruleuniver.getUniveraclindeny().get(pt).remove(size0-1);
									rawdelist.remove(size1-1);
									aclruleuniver.getUniveraclindeny().get(pt).addAll(rawdelist);//remove the end and merge the list
									aclruleuniver.getUniveraclindeny().get(pt).add(newbdd);//add the end		
								}
								
							
						}
						
					}else
					{
						if(!rawpermit.get(oneacluse.getnumber()).isEmpty()){
							ArrayList<Integer> rawperlist =rawpermit.get(oneacluse.getnumber());
						    
							//	ArrayList<Integer> rawperlist =rawpermit.get(oneacluse.getnumber());
								PositionTuple pt= new PositionTuple(name,port);
								if (!aclruleuniver.getUniveracloutpermit().containsKey(pt))//if the pt isn't existing
								{aclruleuniver.getUniveracloutpermit().put(pt,rawperlist);
		
								}else{//if the pt is existing
									int size0=aclruleuniver.getUniveracloutpermit().get(pt).size();
									int size1=rawperlist.size();
									int lastbdd=aclruleuniver.getUniveracloutpermit().get(pt).get(size0-1);
									int newbdd=baw.getBDD().and(rawperlist.get(size1-1),lastbdd);
									baw.getBDD().ref(newbdd);
									aclruleuniver.getUniveracloutpermit().get(pt).remove(size0-1);
									rawperlist.remove(size1-1);
									aclruleuniver.getUniveracloutpermit().get(pt).addAll(rawperlist);//remove the end and merge the list
									aclruleuniver.getUniveracloutpermit().get(pt).add(newbdd);//add the end		
								}
								
							
						}
						if(!rawdeny.get(oneacluse.getnumber()).isEmpty()){
							ArrayList<Integer> rawdelist =rawdeny.get(oneacluse.getnumber());
						
							//	ArrayList<Integer> rawperlist =rawpermit.get(oneacluse.getnumber());
								PositionTuple pt= new PositionTuple(name,port);
								if (!aclruleuniver.getUniveracloutdeny().containsKey(pt))//if the pt isn't existing
								{aclruleuniver.getUniveracloutdeny().put(pt,rawdelist);
			
								}else{//if the pt is existing
									int size0=aclruleuniver.getUniveracloutdeny().get(pt).size();
									int size1=rawdelist.size();
									int lastbdd=aclruleuniver.getUniveracloutdeny().get(pt).get(size0-1);
									int newbdd=baw.getBDD().and(rawdelist.get(size1-1),lastbdd);
									baw.getBDD().ref(newbdd);
									aclruleuniver.getUniveracloutdeny().get(pt).remove(size0-1);
									rawdelist.remove(size1-1);
									aclruleuniver.getUniveracloutdeny().get(pt).addAll(rawdelist);//remove the end and merge the list
									aclruleuniver.getUniveracloutdeny().get(pt).add(newbdd);//add the end		
								}
						}
					}
			}
		}*/

/*      
			if(vlan_ports.containsKey(oneacluse.getinterface()))
			{
				ports = vlan_ports.get(oneacluse.getinterface());
			}else
			{
				ports = new HashSet<String> ();
				ports.add(oneacluse.getinterface());
			}
			*/
		/*	if(oneacluse.isin())
			{
				int aclbdd = baw.encodeACLin(subs, rawaclbdd, ports.size());
				//System.out.println(oneacluse);

				for(String pport : ports)
				{
					if(inaclbddset.containsKey(pport))
					{
						inaclbddset.get(pport).add(aclbdd);
					}else
					{
						ArrayList<Integer> newset = new ArrayList<Integer>();
						newset.add(aclbdd);
						inaclbddset.put(pport, newset);
					}
				}
			}else
			{
				int aclbdd = baw.encodeACLout(subs, rawaclbdd, ports.size());
				for(String pport : ports)
				{
					if(outaclbddset.containsKey(pport))
					{
						outaclbddset.get(pport).add(aclbdd);
					}else
					{
						ArrayList<Integer> newset = new ArrayList<Integer>();
						newset.add(aclbdd);
						outaclbddset.put(pport, newset);
					}
				}
			}
		}
		inaclbdds = new HashMap<String, Integer>();
		outaclbdds = new HashMap<String, Integer>();
		for(String pport : inaclbddset.keySet())
		{
			ArrayList<Integer> bddset = inaclbddset.get(pport);
			int [] bdds = Utility.ArrayListToArray(bddset);
			//System.out.println(bdds.length);
			int andbdd = baw.AndInBatch(bdds);
			baw.DerefInBatch(bdds);
			inaclbdds.put(pport, andbdd);
		}
		for(String pport : outaclbddset.keySet())
		{
			ArrayList<Integer> bddset = outaclbddset.get(pport);
			int [] bdds = Utility.ArrayListToArray(bddset);
			int andbdd = baw.AndInBatch(bdds);
			baw.DerefInBatch(bdds);
			outaclbdds.put(pport, andbdd);
		}*/
		

	
	
	public void computeRawACL()
	{

		    rawpermit=new HashMap<String,ArrayList<Integer>>();
			rawdeny=new HashMap<String,ArrayList<Integer>>();
		    rawacl = new HashMap<String, Integer> ();
		    ArrayList<ArrayList<Integer>> result;
		    
			for(String aclname : aclmap.keySet())//aclname is not port name, they  are table name
			{
				result=baw.ConvertACLs(aclmap.get(aclname));
				rawpermit.put(aclname, result.get(0));
				rawdeny.put(aclname,result.get(1));
				rawacl.put(aclname, result.get(2).get(0));
				
				
		//		rawacl.put(aclname, baw.ConvertACLs(aclmap.get(aclname)));
				
		//		rawacl.put(aclname, baw.ConvertACLs(aclmap.get(aclname)));//HashMap<String, Integer> rawacl;
			}
			
			//System.out.println(rawacl);
	}
	public void addACLUse(ACLUse oneuse)
	{
		acluses.add(oneuse);//ArrayList<ACLUse> acluses;
	}
	
	public void addVlanPorts(String vlan, HashSet<String> ports)
	{
		vlan_ports.put(vlan, ports);
	}

	public void getvlanpredicate(HashMap<String, ArrayList<VlanPredicate>> vlanmap) {
		// TODO Auto-generated method stub
		
		
		for (String vlanID: vlanpermap.keySet()){
			if(vlanmap.containsKey(vlanID)){
				vlanmap.get(vlanID).add(vlanpermap.get(vlanID));
			}else{
				ArrayList<VlanPredicate> alist=new ArrayList<VlanPredicate>();
				alist.add(vlanpermap.get(vlanID));
				vlanmap.put(vlanID, alist);
				
			}
			
		}
		
		
	}


}
/*
this.aclruleuniverdevice=new AclRuleUniverDevice("fwd");
this.vlanaclruledevice=new HashMap<String,AclRuleUniverDevice>();
computeRawACL();//compute the ACLBDD, do not consider the override
rawaclinuse = new HashSet<Integer>();
for(int i = 0; i < acluses.size(); i ++)
{
	ACLUse oneacluse = acluses.get(i);
	if(oneacluse.getinterface().startsWith("vlan")){	
		String port=oneacluse.getinterface();
		ACLForTree aclforvlan;
		if(aclfortreevlan.containsKey(port)){
		aclforvlan=aclfortreevlan.get(port);
		}else{
		//	aclforvlan=new ACLForTree();
		//	aclfortreevlan.put(port, aclforvlan);
			continue;
		}
		
		HashSet<String> phyportset=vlan_ports.get(port);//find all the phyports
		if(oneacluse.isin()){
			
			if(!rawpermit.containsKey(oneacluse.getnumber())){
				continue;
			}
			
			if(!rawpermit.get(oneacluse.getnumber()).isEmpty()){//permitin do not void,this for is permit
				AclRuleUniverDevice vlandevice;//set a ACLrule object in vlanaclruledevice
				
				    if(vlanaclruledevice.containsKey(port)){
				    	vlandevice=vlanaclruledevice.get(port);
				    }else{
				    	vlandevice=new AclRuleUniverDevice("vlan");
				    	vlanaclruledevice.put(port, vlandevice);
				    }
				
				
				    ArrayList<Integer> rawpiList =rawpermit.get(oneacluse.getnumber());
				       
				    
				    for(String phyport:phyportset){//every port in the vlan
				    
						ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
						    for(int j=0;j<rawpiList.size()-1;j++){
						    	ACLrule aclrule=new ACLrule();
						    	aclrule.setRulename(namecountac++);//set rule name
						    	aclrule.setFwdBDD(rawpiList.get(j));//set rule's BDD
						    	aclrule.setPortname(phyport);//set rule's port
						    	newlist.add(aclrule);
						    	aclforvlan.getPermitunr().put(aclrule.getRulename(),aclrule);//put the aclrule into the aclfortreevlan's univer
						    	}

				     if(!vlandevice.getDeviceaclinpermit().containsKey(phyport)){//first time of this port, there more than one acl rule on this device
				    	 vlandevice.getPortinpermit().put(phyport,rawpiList.get(rawpiList.size()-1));//port's bdd
				    	 String newport=new String(name+phyport);//the univer tree need univer name
				    	 aclforvlan.getPortinpermit().put(newport, rawpiList.get(rawpiList.size()-1));//port's bdd
				    	 vlandevice.getDeviceaclinpermit().put(phyport,newlist);//put aclrulelist in to the list				     
				     }else{
				    	 vlandevice.getDeviceaclinpermit().get(phyport).addAll(newlist);
				    	 int andresult=baw.getBDD().and( vlandevice.getPortinpermit().get(phyport),rawpiList.get(rawpiList.size()-1));
				    	 baw.getBDD().ref(andresult);//may have two deref
				    	 vlandevice.getPortinpermit().put(phyport,andresult);
				    	 String newport=new String(name+phyport);
				    	 aclforvlan.getPortinpermit().put(newport, andresult);
					     }
				    }
					  }
			//
				if(!rawdeny.get(oneacluse.getnumber()).isEmpty()){//permitin do not void,this for is deny
					
					AclRuleUniverDevice vlandevice;//set a ACLrule object in vlanaclruledevice
					
				    if(vlanaclruledevice.containsKey(port)){
				    	vlandevice=vlanaclruledevice.get(port);
				    }else{
				    	vlandevice=new AclRuleUniverDevice("vlan");
				    	vlanaclruledevice.put(port, vlandevice);
				    }
				    
				    ArrayList<Integer> rawdiList =rawdeny.get(oneacluse.getnumber());
				    
				    for(String phyport:phyportset){
				
						ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
						    for(int j=0;j<rawdiList.size()-1;j++){
						    	ACLrule aclrule=new ACLrule();
						    	aclrule.setRulename(namecountac++);
						    	aclrule.setFwdBDD(rawdiList.get(j));
						    	aclrule.setPortname(phyport);
						    	newlist.add(aclrule);
						    	aclforvlan.getDenyunr().put(aclrule.getRulename(),aclrule);
						    	}

					     if(!vlandevice.getDeviceaclindeny().containsKey(phyport)){//first time of this port, there more than one acl rule on this device
					    	 vlandevice.getPortindeny().put(phyport,rawdiList.get(rawdiList.size()-1));
					    	 String newport=new String(name+phyport);
					    	 aclforvlan.getPortindeny().put(newport, rawdiList.get(rawdiList.size()-1));
					    	 vlandevice.getDeviceaclindeny().put(phyport,newlist);
					    	 
					     }else{
					    	 vlandevice.getDeviceaclindeny().get(phyport).addAll(newlist);
					    	 int andresult=baw.getBDD().and(vlandevice.getPortindeny().get(phyport),rawdiList.get(rawdiList.size()-1));
					    	 baw.getBDD().ref(andresult);//may have two deref
					    	 vlandevice.getPortindeny().put(phyport,andresult);
					    	 String newport=new String(name+phyport);
					    	 aclforvlan.getPortindeny().put(newport, andresult);
						     } 
				  }
				}

		}else{
			if(!rawpermit.containsKey(oneacluse.getnumber())){
				continue;
			}
			
			
			if(!rawpermit.get(oneacluse.getnumber()).isEmpty()){//permitout do not void
				AclRuleUniverDevice vlandevice;//set a ACLrule object in vlanaclruledevice
				
			    if(vlanaclruledevice.containsKey(port)){
			    	vlandevice=vlanaclruledevice.get(port);
			    }else{
			    	vlandevice=new AclRuleUniverDevice("vlan");
			    	vlanaclruledevice.put(port, vlandevice);
			    }
			    
			    ArrayList<Integer> rawpoList =rawpermit.get(oneacluse.getnumber());
			    
			    for(String phyport:phyportset){
			    
					ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
					    for(int j=0;j<rawpoList.size()-1;j++){
					    	ACLrule aclrule=new ACLrule();
					    	aclrule.setRulename(namecountac++);
					    	aclrule.setFwdBDD(rawpoList.get(j));
					    	aclrule.setPortname(phyport);
					    	newlist.add(aclrule);
					    	aclforvlan.getPermitunr().put(aclrule.getRulename(),aclrule);
					    	}

			     if(!vlandevice.getDeviceacloutpermit().containsKey(phyport)){//first time of this port, there more than one acl rule on this device
			    	 vlandevice.getPortoutpermit().put(phyport,rawpoList.get(rawpoList.size()-1));
			    	 String newport=new String(name+phyport);
			    	 aclforvlan.getPortoutpermit().put(newport, rawpoList.get(rawpoList.size()-1));
			    	 vlandevice.getDeviceacloutpermit().put(phyport,newlist);				     
			     }else{
			    	 vlandevice.getDeviceacloutpermit().get(phyport).addAll(newlist);
			    	 int andresult=baw.getBDD().and( vlandevice.getPortoutpermit().get(phyport),rawpoList.get(rawpoList.size()-1));
			    	 baw.getBDD().ref(andresult);//may have two deref
			    	 vlandevice.getPortoutpermit().put(phyport,andresult);
			    	 String newport=new String(name+phyport);
			    	 aclforvlan.getPortoutpermit().put(newport, andresult);
				     } 
				  }
			}
			//
				if(!rawdeny.get(oneacluse.getnumber()).isEmpty()){//permitin do not void
					
                    AclRuleUniverDevice vlandevice;//set a ACLrule object in vlanaclruledevice
					
				    if(vlanaclruledevice.containsKey(port)){
				    	vlandevice=vlanaclruledevice.get(port);
				    }else{
				    	vlandevice=new AclRuleUniverDevice("vlan");
				    	vlanaclruledevice.put(port, vlandevice);
				    }
				    
				    ArrayList<Integer> rawdoList =rawdeny.get(oneacluse.getnumber());
				    
				    for(String phyport:phyportset){
						ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
						    for(int j=0;j<rawdoList.size()-1;j++){
						    	ACLrule aclrule=new ACLrule();
						    	aclrule.setRulename(namecountac++);
						    	aclrule.setFwdBDD(rawdoList.get(j));
						    	aclrule.setPortname(phyport);
						    	newlist.add(aclrule);
						    	aclforvlan.getDenyunr().put(aclrule.getRulename(),aclrule);
						    	}
	
				     if(!vlandevice.getDeviceacloutdeny().containsKey(phyport)){//first time of this port, there more than one acl rule on this device
				    	 vlandevice.getPortoutdeny().put(phyport,rawdoList.get(rawdoList.size()-1));
				    	 String newport=new String(name+phyport);
				    	 aclforvlan.getPortoutdeny().put(newport, rawdoList.get(rawdoList.size()-1));
				    	 vlandevice.getDeviceacloutdeny().put(phyport,newlist);				     
				     }else{
				    	 vlandevice.getDeviceacloutdeny().get(phyport).addAll(newlist);
				    	 int andresult=baw.getBDD().and(vlandevice.getPortoutdeny().get(phyport),rawdoList.get(rawdoList.size()-1));
				    	 baw.getBDD().ref(andresult);//may have two deref
				    	 vlandevice.getPortoutdeny().put(phyport,andresult);
				    	 String newport=new String(name+phyport);
				    	 aclforvlan.getPortoutdeny().put(newport, andresult);
					     } 
			  }
				}		
     		}

	}else{

////////////////////////////////////////////////////////////////////////////////////////////////////////no vlan
	String port=oneacluse.getinterface();
	if(oneacluse.isin()){
		if(!rawpermit.get(oneacluse.getnumber()).isEmpty()){//permitin do not void
			    ArrayList<Integer> rawpiList =rawpermit.get(oneacluse.getnumber());
					ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
					    for(int j=0;j<rawpiList.size()-1;j++){
					    	ACLrule aclrule=new ACLrule();
					    	aclrule.setRulename(namecountac++);
					    	aclrule.setFwdBDD(rawpiList.get(j));
					    	aclrule.setPortname(port);
					    	newlist.add(aclrule);
					    	aclfortree.getPermitunr().put(aclrule.getRulename(),aclrule);
					    	}

			     if(!aclruleuniverdevice.getDeviceaclinpermit().containsKey(port)){//first time of this port, there more than one acl rule on this device
			    	 aclruleuniverdevice.getPortinpermit().put(port,rawpiList.get(rawpiList.size()-1));
			    	 String newport=new String(name+port);
			    	 aclfortree.getPortinpermit().put(newport, rawpiList.get(rawpiList.size()-1));
			    	 aclruleuniverdevice.getDeviceaclinpermit().put(port,newlist);				     
			     }else{
			    	 aclruleuniverdevice.getDeviceaclinpermit().get(port).addAll(newlist);
			    	 int andresult=baw.getBDD().and( aclruleuniverdevice.getPortinpermit().get(port),rawpiList.get(rawpiList.size()-1));
			    	 baw.getBDD().ref(andresult);//may have two deref
			    	 aclruleuniverdevice.getPortinpermit().put(port,andresult);
			    	 String newport=new String(name+port);
			    	 aclfortree.getPortinpermit().put(newport, andresult);
				     }

				  }
			if(!rawdeny.get(oneacluse.getnumber()).isEmpty()){//permitin do not void
			    ArrayList<Integer> rawdiList =rawdeny.get(oneacluse.getnumber());
					ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
					    for(int j=0;j<rawdiList.size()-1;j++){
					    	ACLrule aclrule=new ACLrule();
					    	aclrule.setRulename(namecountac++);
					    	aclrule.setFwdBDD(rawdiList.get(j));
					    	aclrule.setPortname(port);
					    	newlist.add(aclrule);
					    	aclfortree.getDenyunr().put(aclrule.getRulename(),aclrule);
					    	}

				     if(!aclruleuniverdevice.getDeviceaclindeny().containsKey(port)){//first time of this port, there more than one acl rule on this device
				    	 aclruleuniverdevice.getPortindeny().put(port,rawdiList.get(rawdiList.size()-1));
				    	 String newport=new String(name+port);
				    	 aclfortree.getPortindeny().put(newport, rawdiList.get(rawdiList.size()-1));
				    	 aclruleuniverdevice.getDeviceaclindeny().put(port,newlist);
				    	 
				     }else{
				    	 aclruleuniverdevice.getDeviceaclindeny().get(port).addAll(newlist);
				    	 int andresult=baw.getBDD().and(aclruleuniverdevice.getPortindeny().get(port),rawdiList.get(rawdiList.size()-1));
				    	 baw.getBDD().ref(andresult);//may have two deref
				    	 aclruleuniverdevice.getPortindeny().put(port,andresult);
				    	 String newport=new String(name+port);
				    	 aclfortree.getPortindeny().put(newport, andresult);
					     } 
			  }

	}else{
		if(!rawpermit.get(oneacluse.getnumber()).isEmpty()){//permitin do not void
		    ArrayList<Integer> rawpoList =rawpermit.get(oneacluse.getnumber());
				ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
				    for(int j=0;j<rawpoList.size()-1;j++){
				    	ACLrule aclrule=new ACLrule();
				    	aclrule.setRulename(namecountac++);
				    	aclrule.setFwdBDD(rawpoList.get(j));
				    	aclrule.setPortname(port);
				    	newlist.add(aclrule);
				    	aclfortree.getPermitunr().put(aclrule.getRulename(),aclrule);
				    	}

		     if(!aclruleuniverdevice.getDeviceacloutpermit().containsKey(port)){//first time of this port, there more than one acl rule on this device
		    	 aclruleuniverdevice.getPortoutpermit().put(port,rawpoList.get(rawpoList.size()-1));
		    	 String newport=new String(name+port);
		    	 aclfortree.getPortoutpermit().put(newport, rawpoList.get(rawpoList.size()-1));
		    	 aclruleuniverdevice.getDeviceacloutpermit().put(port,newlist);				     
		     }else{
		    	 aclruleuniverdevice.getDeviceacloutpermit().get(port).addAll(newlist);
		    	 int andresult=baw.getBDD().and( aclruleuniverdevice.getPortoutpermit().get(port),rawpoList.get(rawpoList.size()-1));
		    	 baw.getBDD().ref(andresult);//may have two deref
		    	 aclruleuniverdevice.getPortoutpermit().put(port,andresult);
		    	 String newport=new String(name+port);
		    	 aclfortree.getPortoutpermit().put(newport, andresult);
			     } 
			  }
			if(!rawdeny.get(oneacluse.getnumber()).isEmpty()){//permitin do not void
			    ArrayList<Integer> rawdoList =rawdeny.get(oneacluse.getnumber());
					ArrayList<ACLrule> newlist=new ArrayList<ACLrule>();
					    for(int j=0;j<rawdoList.size()-1;j++){
					    	ACLrule aclrule=new ACLrule();
					    	aclrule.setRulename(namecountac++);
					    	aclrule.setFwdBDD(rawdoList.get(j));
					    	aclrule.setPortname(port);
					    	newlist.add(aclrule);
					    	aclfortree.getDenyunr().put(aclrule.getRulename(),aclrule);
					    	}

			     if(!aclruleuniverdevice.getDeviceacloutdeny().containsKey(port)){//first time of this port, there more than one acl rule on this device
			    	 aclruleuniverdevice.getPortoutdeny().put(port,rawdoList.get(rawdoList.size()-1));
			    	 String newport=new String(name+port);
			    	 aclfortree.getPortoutdeny().put(newport, rawdoList.get(rawdoList.size()-1));
			    	 aclruleuniverdevice.getDeviceacloutdeny().put(port,newlist);				     
			     }else{
			    	 aclruleuniverdevice.getDeviceacloutdeny().get(port).addAll(newlist);
			    	 int andresult=baw.getBDD().and(aclruleuniverdevice.getPortoutdeny().get(port),rawdoList.get(rawdoList.size()-1));
			    	 baw.getBDD().ref(andresult);//may have two deref
			    	 aclruleuniverdevice.getPortoutdeny().put(port,andresult);
			    	 String newport=new String(name+port);
			    	 aclfortree.getPortoutdeny().put(newport, andresult);
				     } 
		  }
   				
 		}
	}
	
 }//end for
			    
}*/