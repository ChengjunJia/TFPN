package i2analysis;

import java.io.*;
import java.util.*;

import stanalysis.APComputer;
import stanalysis.FWDAPSet;
import stanalysis.ForwardingRule;
import stanalysis.PositionTuple;
import StaticReachabilityAnalysis.*;
import jdd.bdd.BDD;



public class Network {
	BDDACLWrapper bddengine;
	String name;
	HashMap<String, Device> devices;
	//HashSet<Rule> universet;
	HashMap<Integer,Rule> univerhashmap;
	ACLForTree aclfortree;
	HashMap<String,HashSet<String>> univervlan;//vlanid,list<divcename>
	HashMap<String ,ACLForTree> aclfortreevlan;
	public HashSet<String> percentport=new HashSet<String>();//port after percent
	public HashSet<UpdateNode> nofathernode=new HashSet<UpdateNode>();
	public HashMap<Integer,UpdateNode> removemap=new HashMap<Integer,UpdateNode>();
	ForwardingRule remaintoupdate=new ForwardingRule();
    
	// device|port - device|port
	HashMap<PositionTuple, PositionTuple> topology;//key和value都是PositionTuple的对象
	//ArrayList<ArrayList<ACLRule>> acllib;
	APComputer fwdapc;
	int init=0;


	
	public PositionTuple LinkTransfer(PositionTuple pt)
	{
		//System.out.println();
		return topology.get(pt);
	}
	
	public HashMap<PositionTuple, PositionTuple> get_topology()
	{
		return topology;
	}

	public Device getDevice(String dname)
	{
		return devices.get(dname);
	}
	
	public Collection<Device> getAllDevices()
	{
		return devices.values();
	}
	
	public Set<PositionTuple> getallactiveports()
	{
		return topology.keySet();
	}
	
	public APComputer getFWDAPC()
	{
		return fwdapc;
	}
	


	public Network(String name, int ruleid) throws IOException
	{
		this.univerhashmap= new HashMap<Integer,Rule>();
		this.aclfortree=new ACLForTree();
		this.name = name;
		this.univervlan=new HashMap<String,HashSet<String>>();
		this.aclfortreevlan=new HashMap<String ,ACLForTree>();
		
		devices = new HashMap<String, Device>();
	
	String [] devicenames = null;
		
	if(name.equals("i2")){
		String foldername2 = "i2/";
		String foldername1 = "i2/";
		devicenames = new String[]{"atla","chic","hous","kans","losa","newy32aoa","salt","seat","wash"};
		for( int i = 0; i < devicenames.length; i ++)
		{
			Device d = BuildNetwork.parseDevice(devicenames[i], foldername1 + devicenames[i] + "ap"
					, foldername2 + devicenames[i] + "ap",univervlan);

			////把device里面的每一行都加到ArrayList<ForwardingRule> fws里面; ForwardingRule(long destip, int prefixlen, String outinterface)
			System.out.println(d.name);
			 
			devices.put(d.name, d);//HashMap<String, Device> devices;
		}
	}
	
    if(name.equals("st")){
		String foldername2 = "st/";
		String foldername1 = "stconfig/";
		devicenames = new String[]{"bbra_rtr", "bbrb_rtr", "boza_rtr", "bozb_rtr", "coza_rtr", "cozb_rtr", "goza_rtr",
				"gozb_rtr", "poza_rtr", "pozb_rtr", "roza_rtr", "rozb_rtr", "soza_rtr", "sozb_rtr", "yoza_rtr", "yozb_rtr"};
		for( int i = 0; i < devicenames.length; i ++)
		{
			Device d = BuildNetwork.parseDevice(devicenames[i], foldername1 + devicenames[i] + "_config.txt"
					, foldername2 + devicenames[i] + "ap",univervlan);

			////把device里面的每一行都加到ArrayList<ForwardingRule> fws里面; ForwardingRule(long destip, int prefixlen, String outinterface)
			System.out.println(d.name);
			 
			devices.put(d.name, d);//HashMap<String, Device> devices;
		}
	}
		
		
		
		for(String vlanID: univervlan.keySet()){
			ACLForTree aclfortree=new ACLForTree();
			aclfortreevlan.put(vlanID,aclfortree);
			
		}
		
	//	Runtime r = Runtime.getRuntime();
	//	r.gc();
	//	r.gc();
	//	long m1 = r.totalMemory() - r.freeMemory();
		
		bddengine = new BDDACLWrapper();
		bddengine.rulename=0;//update

		Device.setBDDWrapper(bddengine);

		for(Device d : devices.values())
		{
			d.computeACLBDDs(aclfortree,aclfortreevlan);
		}	
		
		for(String d_name : devicenames)
		{
			devices.get(d_name).computeFWBDDs(univerhashmap,aclfortree,aclfortreevlan,ruleid,remaintoupdate,init);
		}
		
		//ArrayList<Integer> fwdbddary = new ArrayList<Integer> ();yu zhao
		ArrayList<Predicate> fwdpreidcatary = new ArrayList<Predicate> ();
		HashMap<String, ArrayList<VlanPredicate>> vlanmap=new HashMap<String, ArrayList<VlanPredicate>> ();
		
		/*
		for(Device d : devices.values())
		{
			Collection<Integer> bdds = d.getRawACLinUse();
			for(int bdd : bdds)
			{
				fwdbddary.add(bdd);
			}
		}*/
		//BDD thebdd = bddengine.getBDD();
		for(Device d : devices.values())//把所有求得的bdd都扔到一个数组fwdbddary内
		{
			//Collection<Integer> bdds = d.getfwbdds();yu zhao
			List <Predicate> predic=d.getpredicate(bddengine);
			
			for(Predicate eachpredic: predic)
			{
				fwdpreidcatary.add(eachpredic);
			}	
			
			d.getvlanpredicate(vlanmap);
			
			
		}
		//fwdapc = new APComputer(fwdbddary, bddengine);
		
		fwdapc= new APComputer(fwdpreidcatary,vlanmap,bddengine);
		FWDAPSet.setUniverse(fwdapc.getAllAP());//yu zhao
		//FWDAPSet.setUniverse(fwdapc.getAllAP());
		
	
		for(Device d : devices.values())
		{
			d.setaps(fwdapc);//八成是把每一个设备的ap分出来
			d.setvlanaps(fwdapc);
		}//yu zhao

		/*
		 * topology information
		 */
		topology = new HashMap<PositionTuple, PositionTuple>();
		if(name.equals("i2")){
		addTopology("chic","xe-0/1/0","newy32aoa","xe-0/1/3");
		addTopology("chic","xe-1/0/1","kans","xe-0/1/0");
		addTopology("chic","xe-1/1/3","wash","xe-6/3/0");
		addTopology("hous","xe-3/1/0","losa","ge-6/0/0");
		addTopology("kans","ge-6/0/0","salt","ge-6/1/0");
		addTopology("chic","xe-1/1/2","atla","xe-0/1/3");
		addTopology("seat","xe-0/0/0","salt","xe-0/1/1");
		addTopology("chic","xe-1/0/2","kans","xe-0/0/3");
		addTopology("hous","xe-1/1/0","kans","xe-1/0/0");
		addTopology("seat","xe-0/1/0","losa","xe-0/0/0");
		addTopology("salt","xe-0/0/1","losa","xe-0/1/3");
		addTopology("seat","xe-1/0/0","salt","xe-0/1/3");
		addTopology("newy32aoa","et-3/0/0-0","wash","et-3/0/0-0");
		addTopology("newy32aoa","et-3/0/0-1","wash","et-3/0/0-1");
		addTopology("chic","xe-1/1/1","atla","xe-0/0/0");
		addTopology("losa","xe-0/1/0","seat","xe-2/1/0");
		addTopology("hous","xe-0/1/0","losa","ge-6/1/0");
		addTopology("atla","xe-0/0/3","wash","xe-1/1/3");
		addTopology("hous","xe-3/1/0","kans","ge-6/2/0");
		addTopology("atla","ge-6/0/0","hous","xe-0/0/0");
		addTopology("chic","xe-1/0/3","kans","xe-1/0/3");
		addTopology("losa","xe-0/0/3","salt","xe-0/1/0");
		addTopology("atla","ge-6/1/0","hous","xe-1/0/0");
		addTopology("atla","xe-1/0/3","wash","xe-0/0/0");
		addTopology("chic","xe-2/1/3","wash","xe-0/1/3");
		addTopology("atla","xe-1/0/1","wash","xe-0/0/3");
		addTopology("kans","xe-0/1/1","salt","ge-6/0/0");
		addTopology("chic","xe-1/1/0","newy32aoa","xe-0/0/0");
		}
		
		if(name.equals("st")){
			addTopology("bbra_rtr","te7/3","goza_rtr","te2/1");
			addTopology("bbra_rtr","te7/3","pozb_rtr","te3/1");
			addTopology("bbra_rtr","te1/3","bozb_rtr","te3/1");
			addTopology("bbra_rtr","te1/3","yozb_rtr","te2/1");
			addTopology("bbra_rtr","te1/3","roza_rtr","te2/1");
			addTopology("bbra_rtr","te1/4","boza_rtr","te2/1");
			addTopology("bbra_rtr","te1/4","rozb_rtr","te3/1");
			addTopology("bbra_rtr","te6/1","gozb_rtr","te3/1");
			addTopology("bbra_rtr","te6/1","cozb_rtr","te3/1");
			addTopology("bbra_rtr","te6/1","poza_rtr","te2/1");
			addTopology("bbra_rtr","te6/1","soza_rtr","te2/1");
			addTopology("bbra_rtr","te7/2","coza_rtr","te2/1");
			addTopology("bbra_rtr","te7/2","sozb_rtr","te3/1");
			addTopology("bbra_rtr","te6/3","yoza_rtr","te1/3");
			addTopology("bbra_rtr","te7/1","bbrb_rtr","te7/1");
			addTopology("bbrb_rtr","te7/4","yoza_rtr","te7/1");
			addTopology("bbrb_rtr","te1/1","goza_rtr","te3/1");
			addTopology("bbrb_rtr","te1/1","pozb_rtr","te2/1");
			addTopology("bbrb_rtr","te6/3","bozb_rtr","te2/1");
			addTopology("bbrb_rtr","te6/3","roza_rtr","te3/1");
			addTopology("bbrb_rtr","te6/3","yozb_rtr","te1/1");
			addTopology("bbrb_rtr","te1/3","boza_rtr","te3/1");
			addTopology("bbrb_rtr","te1/3","rozb_rtr","te2/1");
			addTopology("bbrb_rtr","te7/2","gozb_rtr","te2/1");
			addTopology("bbrb_rtr","te7/2","cozb_rtr","te2/1");
			addTopology("bbrb_rtr","te7/2","poza_rtr","te3/1");
			addTopology("bbrb_rtr","te7/2","soza_rtr","te3/1");
			addTopology("bbrb_rtr","te6/1","coza_rtr","te3/1");
			addTopology("bbrb_rtr","te6/1","sozb_rtr","te2/1");
			addTopology("boza_rtr","te2/3","bozb_rtr","te2/3");
			addTopology("coza_rtr","te2/3","cozb_rtr","te2/3");
			addTopology("goza_rtr","te2/3","gozb_rtr","te2/3");
			addTopology("poza_rtr","te2/3","pozb_rtr","te2/3");
			addTopology("roza_rtr","te2/3","rozb_rtr","te2/3");
			addTopology("soza_rtr","te2/3","sozb_rtr","te2/3");	
			addTopology("yoza_rtr","te1/1","yozb_rtr","te1/3");
			addTopology("yoza_rtr","te1/2","yozb_rtr","te1/2");
		}
		
	}
	
	public void addTopology(String d1, String p1, String d2, String p2)
	{
		PositionTuple pt1 = new PositionTuple(d1, p1);//link的前两列"chic","xe-0/1/0"
		PositionTuple pt2 = new PositionTuple(d2, p2);//link的后两列"newy32aoa","xe-0/1/3"
		topology.put(pt1, pt2);
		topology.put(pt2, pt1);
	}

	public HashMap<Integer,Integer> addfwdrule(String devicename, String portname, long ipaddr, int prefixlen, int bddreturn){
		int[] head=new int[BDDACLWrapper.getipBits()];
		int[] tail=new int[BDDACLWrapper.getipBits()];	
		Integer entrybdd =bddengine.encodeDstIPPrefix1(ipaddr, prefixlen, head, tail);
		HashMap<Integer,Integer> rulechangelist= new HashMap<Integer,Integer>();
		
		for(Predicate pred:devices.get(devicename).updatepredicate){
			if(pred.getPortname().equals(portname)){
				String s=String.valueOf(BDDACLWrapper.rulename++);
				Rule rule=new Rule(portname,devicename,head,tail,entrybdd,s);
				rule.setPrefixlen(prefixlen);
				pred.getRuleset().add(rule);
			}
			for(Rule rule:pred.getRuleset()){
				if(rule.getPrefixlen()>=prefixlen)
				{continue;}
				int[] refhead=rule.getHead();
				int[] reftail=rule.getTail();
				int headjudge=bddengine.firstisbigger(head, reftail);
				int tailjudge=bddengine.firstisbigger(refhead, tail);
				if((headjudge==1)&&(tailjudge==1)){
					int nonrule=bddengine.getBDD().not(entrybdd);
					int result=bddengine.getBDD().and(rule.getRuleBDD(),nonrule);
					if(result!=rule.getRuleBDD()){
						rule.setRuleBDD(result);
						bddengine.getBDD().ref(result);
						int rulechange=Integer.valueOf(rule.getRulename()).intValue();
						rulechangelist.put(rulechange,result);//<rulename,afterand's bdd>
					}
				}
				
			}
		}
		return rulechangelist;
	}
	/////////////////perfect update//////////////////////////////
	public void perfectupdateadd(String devicename, String portname, long ipaddr, int prefixlen) {
		HashSet<Predicate> changedpredicate=new HashSet<Predicate>();
		HashMap<String,Predicate> updatepredicatemap=devices.get(devicename).getUpdatepredicatemap();
		int[] head=new int[BDDACLWrapper.getipBits()];
		int[] tail=new int[BDDACLWrapper.getipBits()];	
		Integer entrybdd =bddengine.encodeDstIPPrefix1(ipaddr, prefixlen, head, tail);
		bddengine.getBDD().ref(entrybdd);
		////update predicate///////////
		Predicate predicate=updatepredicatemap.get(portname);
		Integer newpredicbdd=bddengine.getBDD().or(predicate.getFwbdds(),entrybdd);
		if(predicate.getFwbdds()!=newpredicbdd){
		bddengine.getBDD().ref(newpredicbdd);
		predicate.setFwbdds(newpredicbdd);
		changedpredicate.add(predicate);
		}
		////////////////////////////////
		
		String s=String.valueOf(BDDACLWrapper.rulename++);
		Rule rule=new Rule(portname,devicename,head,tail,entrybdd,s);
		HashSet<UpdateNode> nofathernode=devices.get(devicename).getNofathernode();
		for(UpdateNode updatenode: nofathernode){
			if(bddengine.contain(updatenode.getRule(),rule)){
				traverse(rule,updatenode.getChildren(),updatenode,changedpredicate,entrybdd);//travse		}
			}
		}
		//////////////////AP update/////////////////
		
		fwdapc.computeUpdate(changedpredicate,devices);
		FWDAPSet.setUniverse(fwdapc.getAllAP());//yu zhao
		for(Device d : devices.values())
		{
			d.setapsupdate(fwdapc);//八成是把每一个设备的ap分出来
		}//yu zhao
		
		
	}
	
	private void traverse(Rule rule, ArrayList<UpdateNode> childrenlist,UpdateNode fathernode, HashSet<Predicate> changedpredicate, Integer entrybdd) {
		for(UpdateNode updatenode: childrenlist){
			if(bddengine.contain(updatenode.getRule(),rule)){
				traverse(rule,updatenode.getChildren(),updatenode,changedpredicate,entrybdd);
				break;
			}	
		}
		UpdateNode newnode=new UpdateNode();
		newnode.setRule(rule);
		newnode.setParent(fathernode);
		
		///////////////////////updatefathernode's predicate///////
		HashMap<String,Predicate> updatepredicatemap=devices.get(rule.getDevicename()).getUpdatepredicatemap();
		Predicate fatherpredic=updatepredicatemap.get(fathernode.rule.getPortname());
		Integer notbdd=bddengine.getBDD().not(entrybdd);
		Integer fathernewbdd=bddengine.getBDD().and(notbdd,fatherpredic.getFwbdds());
		if(fathernewbdd!=fatherpredic.getFwbdds()){
		bddengine.getBDD().ref(fathernewbdd);
		fatherpredic.setFwbdds(fathernewbdd);
		}
		Integer fatherbdd=bddengine.getBDD().and(fathernode.rule.getRuleBDD(),notbdd);
		bddengine.getBDD().ref(fatherbdd);
		fathernode.rule.setRuleBDD(fatherbdd);
		changedpredicate.add(fatherpredic);
		/////////////////////////////////
		
		for(UpdateNode updatenode:childrenlist){//set children
			if(bddengine.contain(rule,updatenode.getRule())){
				updatenode.setParent(newnode);
				newnode.getChildren().add(updatenode);
				fathernode.getChildren().remove(updatenode);
			
			}
		}
		fathernode.getChildren().add(newnode);
		
		
		// TODO Auto-generated method stub
		
	}
	
	public void perfectupdateremove(Integer ruleid){
		HashSet<Predicate> changedpredicate=new HashSet<Predicate>();
		Rule rule=univerhashmap.get(ruleid);
		/////preidcate update
		Predicate rulepredicate=devices.get(rule.getDevicename()).getUpdatepredicatemap().get(rule.getPortname());
		Integer notrule=bddengine.getBDD().not(rule.getRuleBDD());
		Integer newpredicbdd=bddengine.getBDD().and(rulepredicate.getFwbdds(),notrule);
		if(rulepredicate.getFwbdds()!=newpredicbdd){
		bddengine.getBDD().ref(newpredicbdd);
		rulepredicate.setFwbdds(newpredicbdd);
		changedpredicate.add(rulepredicate);
		}
		rulepredicate.getRuleset().remove(rule);//may be need to mod
		////
		
		UpdateNode objectnode=devices.get(rule.getDevicename()).getRemovemap().get(Integer.parseInt(rule.getRulename()));
		if(objectnode.getParent()!=null){
			/////////////////update father's predicate/////////////
			HashMap<String,Predicate> updatepredicatemap=devices.get(rule.getDevicename()).getUpdatepredicatemap();
			Predicate fatherpredic=updatepredicatemap.get(objectnode.getParent().rule.getPortname());
			Integer fathernewbdd=bddengine.getBDD().or(rule.getRuleBDD(),fatherpredic.getFwbdds());
			if(fathernewbdd!=fatherpredic.getFwbdds()){
				fatherpredic.setFwbdds(fathernewbdd);
				bddengine.getBDD().ref(fathernewbdd);
			}
			
		    /////////////////////////////////////////////////
			objectnode.getParent().getChildren().remove(objectnode);
			objectnode.getParent().getChildren().addAll(objectnode.getChildren());
		    for(UpdateNode object:objectnode.getChildren())
		    {
		    	object.setParent(objectnode.getParent());
		    }
		}else{
			devices.get(rule.getDevicename()).nofathernode.remove(objectnode);
			for(UpdateNode object:objectnode.getChildren()){
			//	UpdateNode aaa;
		//		UpdateNode bbb=object.getParent();
				object.setParent(null);
				devices.get(rule.getDevicename()).nofathernode.add(object);
			}
		}
		//////////////////AP update/////////////////
		for(Device d:devices.values()){
			d.setUpdateportapset(new HashMap<String, HashSet<Integer>>());
		}
		
		fwdapc.computeUpdateremove(changedpredicate,devices);
	
		
		FWDAPSet.setUniverse(fwdapc.getAllAP());//yu zhao
	
		for(Device d : devices.values())
		{
			d.setapsupdateremove(fwdapc);//八成是把每一个设备的ap分出来
		}//yu zhao
		
	}
	//////////////////////////////////////////////////////////////////
	public static void main (String[] args) throws IOException
	{
		Network n = new Network("st",826);
		n.remaintoupdate.getPortname();
		System.out.println(n.topology.size());
	}

}
