package i2analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Random;

import stanalysis.FWDAPSet;
import stanalysis.PositionTuple;
import StaticReachabilityAnalysis.BDDACLWrapper;
import jdd.bdd.BDD;

public class ReachabilityGraph {

	static int count=0;
	StateNode startstate;
	Network net;

	HashMap<String, HashSet<PositionTuple>> devicemap;
	HashMap<PositionTuple, HashSet<StateNode>> reachsets;

	HashMap<String, HashSet<PositionTuple>> changeddevicemap;
	HashMap<PositionTuple, HashSet<StateNode>> changedreachsets;

	HashMap<PositionTuple, PositionTuple> topology;
	
	HashMap<Integer,Finalresult> updateref=new HashMap<Integer,Finalresult>();//<rulename,RefSet>
	
	HashMap<Integer,ArrayList<Finalresult>> updateref2=new HashMap<Integer,ArrayList<Finalresult>>();
	
	ArrayList<Finalresult> finalresultlist;
	
	ArrayList<Portpair> portpairlist;
  //  HashMap<Integer,ArrayList<HashSet<Rule>>> orignalruleset;//k,ruleset in the k level
	//HashMap<Integer,ArrayList<HashSet<Integer>>> orignalruleset;//k,rulenameset in the k level
	HashMap<Integer,ArrayList<RefSet>> orignalruleset;//k,rulenameset in the k level
	HashMap<Integer,ArrayList<RefSet>> aclorignalruleset;//with acl rule
	HashMap<Integer,HashMap<Integer,ArrayList<RefSet>>> apupadteorignalruleset=new HashMap<Integer,HashMap<Integer,ArrayList<RefSet>>>();//<apname,...>
	HashMap<Integer,HashMap<Integer,ArrayList<RefSet>>> apupadteaclorignalruleset=new HashMap<Integer,HashMap<Integer,ArrayList<RefSet>>>();
	static Integer remainfwd=0;
	static Integer remainacl=0;
	static Integer total=0;
    double p=1.005; //setcover al's parameter
    int maxk=0;
	
	public ReachabilityGraph(Network net)
	{
		this.net = net;
		topology = net.topology;
	}
	
	public void setTopology(HashMap<PositionTuple, PositionTuple> top)
	{
		topology = top;
	}

	public PositionTuple LinkTransfer(PositionTuple pt)
	{
		//System.out.println();
		return topology.get(pt);
	}

	public void setStartstate(PositionTuple startpt)
	{
		startstate = new StateNode(startpt, new FWDAPSet(BDDACLWrapper.BDDTrue));////create object,put the first (device,port) into the startstate
		//set as all apset,compliment is 1 in the FWDAPset 
		devicemap = new HashMap<String, HashSet<PositionTuple>>();
		reachsets = new HashMap<PositionTuple, HashSet<StateNode>>();
	}
	
	public void updatedevicemap_d()
	{
		for(String dname : changeddevicemap.keySet())
		{
			if(devicemap.containsKey(dname))
			{
				devicemap.get(dname).removeAll(changeddevicemap.get(dname));
			}else
			{
				System.err.println("not consistent in devicemap.");
				System.exit(1);
			}
		}
	}
	
	public void updatedevicemap()
	{
		for(String dname : changeddevicemap.keySet())
		{
			if(devicemap.containsKey(dname))
			{
				devicemap.get(dname).addAll(changeddevicemap.get(dname));
			}else
			{
				devicemap.put(dname, changeddevicemap.get(dname));
			}
		}
	}

	public void addtodevicemap(PositionTuple pt)
	{
		if(devicemap.containsKey(pt.getDeviceName()))
		{
			devicemap.get(pt.getDeviceName()).add(pt);//devicemap:HashMap<String, HashSet<PositionTuple>>//devicename,
		}else
		{
			HashSet<PositionTuple> newset = new HashSet<PositionTuple>();
			newset.add(pt);
			devicemap.put(pt.getDeviceName(), newset);//½«linkµÄÉè±¸Ãû×ÖºÍ<Éè±¸Ãû×Ö,port¶Ë¿Ú>´æÈë
		}
	}
	

	public void addtodevicemap_u(PositionTuple pt)
	{
		if(changeddevicemap.containsKey(pt.getDeviceName()))
		{
			changeddevicemap.get(pt.getDeviceName()).add(pt);
		}else
		{
			HashSet<PositionTuple> newset = new HashSet<PositionTuple>();
			newset.add(pt);
			changeddevicemap.put(pt.getDeviceName(), newset);
		}
	}
	
	public void updatereachsets_d()
	{
		for(PositionTuple pt : changedreachsets.keySet())
		{
			if(reachsets.containsKey(pt))
			{
				reachsets.get(pt).removeAll(changedreachsets.get(pt));
			}else
			{
				System.err.println("not consistent in reachsets.");
				System.exit(1);
			}
		}
	}
	
	public void updatereachsets()
	{
		for(PositionTuple pt : changedreachsets.keySet())
		{
			if(reachsets.containsKey(pt))
			{
				reachsets.get(pt).addAll(changedreachsets.get(pt));
			}else
			{
				reachsets.put(pt, changedreachsets.get(pt));
			}
		}
	}

	public void addtoreachsets(StateNode sn)
	{
		if(reachsets.containsKey(sn.getPosition()))
		{
			reachsets.get(sn.getPosition()).add(sn);
		}else
		{
			HashSet<StateNode> newset = new HashSet<StateNode>();
			newset.add(sn);
			reachsets.put(sn.getPosition(), newset);
		}
	}

	public void addtoreachsets_u(StateNode sn)
	{
		if(changedreachsets.containsKey(sn.getPosition()))
		{
			changedreachsets.get(sn.getPosition()).add(sn);
		}else
		{
			HashSet<StateNode> newset = new HashSet<StateNode>();
			newset.add(sn);
			changedreachsets.put(sn.getPosition(), newset);
		}
	}

	public void addlink(PositionTuple pt1, PositionTuple pt2)
	{
		changeddevicemap = new HashMap<String, HashSet<PositionTuple>>();
		changedreachsets = new HashMap<PositionTuple, HashSet<StateNode>>();
		
		addlink_onedirect(pt1, pt2);
		addlink_onedirect(pt2, pt1);
		
		updatedevicemap();
		updatereachsets();
		
		topology.put(pt1, pt2);
		topology.put(pt2, pt1);
	}
	
	public void deletelink(PositionTuple pt1, PositionTuple pt2)
	{
		changeddevicemap = new HashMap<String, HashSet<PositionTuple>>();
		changedreachsets = new HashMap<PositionTuple, HashSet<StateNode>>();
		
		topology.remove(pt1);
		topology.remove(pt2);
		
		deletelink_onedirect(pt1, pt2);
		deletelink_onedirect(pt2, pt1);
		
		updatedevicemap_d();
		updatereachsets_d();
		
	}
	
	public void addlink_onedirect(PositionTuple pt1, PositionTuple pt2)
	{
		if(devicemap.containsKey(pt1.getDeviceName()))
		{
			HashSet<PositionTuple> pts1 = devicemap.get(pt1.getDeviceName());
			for(PositionTuple pttmp : pts1)
			{
				for(StateNode sntmp: reachsets.get(pttmp))
				{

					StateNode stpt1 = sntmp.findNextState(pt1);
					if(stpt1 != null)
					{
						// link transfer
						StateNode newsn2 = new StateNode(pt2, stpt1.getAPSet());
						stpt1.addNextState(newsn2);
						Traverse_recur_u(newsn2);
					}
				}
			}
		}
	}
	
	public void deletelink_onedirect(PositionTuple pt1, PositionTuple pt2)
	{
		if(devicemap.containsKey(pt1.getDeviceName()))
		{
			HashSet<PositionTuple> pts1 = devicemap.get(pt1.getDeviceName());
			for(PositionTuple pttmp : pts1)
			{
				for(StateNode sntmp : reachsets.get(pttmp))
				{
					StateNode stpt1 = sntmp.findNextState(pt1);
					if(stpt1 != null)
					{
						// link transfer
						StateNode stpt2 = stpt1.findNextState(pt2);
						if(stpt2 != null)
						{
							stpt1.removeNextState(pt2);
							Traverse_recur_d(stpt2);
						}
					}
				}
			}
		}
	}

	public StateNode ForwardedOnePort(StateNode sn, PositionTuple outpt)
	{
		Device d = net.getDevice(outpt.getDeviceName());
		FWDAPSet aps = d.FowrdAction(sn.getPosition().getPortName(), outpt.getPortName(), sn.getAPSet());
		if(aps == null)
		{
			return null;
		}else
		{
			StateNode newsn = new StateNode(outpt, aps);
			sn.addNextState(newsn);
			return newsn;
		}
	}


	public void Traverse( )
	{
		Traverse_recur(startstate,startstate.getPosition());
	}
	
	
	public void Traversevlan(String vlanId)
	{
		Traverse_recurvlan(startstate,startstate.getPosition(),vlanId);
	}

	/**
	 * 
	 * @param s - packet set and incoming port
	 * @return - next states
	 */
	public ArrayList<StateNode> ForwardedStates(StateNode s, PositionTuple pt)
	{
		ArrayList<StateNode> nxtSs = new ArrayList<StateNode>();

		Device nxtd = net.getDevice(s.getPosition().getDeviceName());
		if(nxtd == null)
		{
			return nxtSs;
		}
		HashMap<String, FWDAPSet> fwdset =  nxtd.FowrdAction(s.getPosition().getPortName(), s.getAPSet());//<portname,APset>of the same device
		//put the all the others' port and ap into the fwdset
		//current port's APset intersect with the same device's other port's APset
		if(fwdset.isEmpty())
		{
			return nxtSs;
		}else
		{
			Iterator iter = fwdset.entrySet().iterator();
			while(iter.hasNext())
			{
				Map.Entry<String, FWDAPSet> oneentry = (Entry<String, FWDAPSet>) iter.next();
				PositionTuple fwdedpt = new PositionTuple(s.getPosition().getDeviceName(), oneentry.getKey());
				//State fwdeds = new State(fwdedpt, fwdset.get(portname), nxts.getAlreadyVisited());
				StateNode fwdeds = new StateNode(fwdedpt, oneentry.getValue(), s.getAlreadyVisited(), s.getAlreadyVisited2());//when init's time, alreadyvisited is null
				s.addNextState(fwdeds);//set NextState//when init's time,nextStateNodes = null
				if(fwdeds.loopDetected())
				{
				}else
				{
					nxtSs.add(fwdeds);

					ArrayList<PositionTuple> arrivedport=new ArrayList<PositionTuple>(fwdeds.getAlreadyVisited2());
					ArrayList<PositionTuple> arrivedporttidy= new ArrayList<PositionTuple>(arrivedport);
					PositionTuple souport=arrivedporttidy.get(1);
				    PositionTuple desport=arrivedporttidy.get(arrivedporttidy.size()-1);
				    Portpair portpair=new Portpair(souport,desport,oneentry.getValue(),arrivedporttidy);//oneentry.getValue()is the fwd ap set
					portpairlist.add(portpair);
				}
			}
		}
		return nxtSs;
	}
	
	public ArrayList<StateNode> ForwardedStatesvlan(StateNode s, PositionTuple pt,String VlanID)
	{
		ArrayList<StateNode> nxtSs = new ArrayList<StateNode>();

		Device nxtd = net.getDevice(s.getPosition().getDeviceName());
		if(nxtd == null)
		{
			return nxtSs;
		}
		HashMap<String, FWDAPSet> fwdset =  nxtd.FowrdActionVlan(s.getPosition().getPortName(), s.getAPSet(), VlanID);//<portname,APset>of the same device
		//put the all the others' port and ap into the fwdset
		//current port's APset intersect with the same device's other port's APset
		if(fwdset.isEmpty())//
		{
			return nxtSs;
		}else
		{
			Iterator iter = fwdset.entrySet().iterator();
			while(iter.hasNext())
			{
				Map.Entry<String, FWDAPSet> oneentry = (Entry<String, FWDAPSet>) iter.next();
				PositionTuple fwdedpt = new PositionTuple(s.getPosition().getDeviceName(), oneentry.getKey());
				//State fwdeds = new State(fwdedpt, fwdset.get(portname), nxts.getAlreadyVisited());
				StateNode fwdeds = new StateNode(fwdedpt, oneentry.getValue(), s.getAlreadyVisited(), s.getAlreadyVisited2());//when init's time, alreadyvisited is null
				s.addNextState(fwdeds);//set NextState//when init's time,nextStateNodes = null
				if(fwdeds.loopDetected())
				{
				}else
				{
					nxtSs.add(fwdeds);

					ArrayList<PositionTuple> arrivedport=new ArrayList<PositionTuple>(fwdeds.getAlreadyVisited2());
					ArrayList<PositionTuple> arrivedporttidy= new ArrayList<PositionTuple>(arrivedport);
					PositionTuple souport=arrivedporttidy.get(1);
				    PositionTuple desport=arrivedporttidy.get(arrivedporttidy.size()-1);
				    Portpair portpair=new Portpair(souport,desport,oneentry.getValue(),arrivedporttidy);//oneentry.getValue()is the fwd ap set
					portpairlist.add(portpair);
				}
			}
		}
		return nxtSs;
	}
	

	public ArrayList<StateNode> linkTransfer(StateNode s)
	{
		ArrayList<StateNode> nxtSs = new ArrayList<StateNode>();
		PositionTuple nxtpt = LinkTransfer(s.getPosition());//find port in the topogly 
		if(nxtpt == null)
		{
			return nxtSs;
		}else
		{		
			FWDAPSet faps = s.getAPSet();//apset before into this port
			StateNode nxts = new StateNode(nxtpt,faps, s.getAlreadyVisited(), s.getAlreadyVisited2());
			//let the linked port device, this port's ap, and alreadyvisited to construct a statenode 
			s.addNextState(nxts);

			if(nxts.loopDetected())
			{
			} else
			{
				nxtSs.add(nxts);
			}

		}
		return nxtSs;
	}

	public void Traverse_recur(StateNode s, PositionTuple pt)
	{
		//at the first step, the s is intied by the first element of the tolopy
		
		//forwarding
		addtodevicemap(s.getPosition());//construct devicemap£¬remember which node can be arrived.//devicemap.get(pt.getDeviceName()).add(pt);
		addtoreachsets(s);//construct reachset//reachsets.get(sn.getPosition()).add(sn);//
		ArrayList<StateNode> nxtSf = ForwardedStates(s, pt);//put the all the other port information of the same device into the nxtSf//the nxtSF is the List of the statenode of the outputport of the device
		for(StateNode nxtsf : nxtSf)
		{
			// link transfer
			ArrayList<StateNode> nxtSl = linkTransfer(nxtsf);//try the other devices' ports which are connected with these ports 
			for(StateNode nxtsl : nxtSl)
			{
				
				Traverse_recur(nxtsl, pt);
			}
		}
	}

	public void Traverse_recurvlan(StateNode s, PositionTuple pt,String vlanID)
	{

		//at the first step, the s is intied by the first element of the tolopy
		
		//forwarding
		addtodevicemap(s.getPosition());//construct devicemap£¬remember which node can be arrived.//devicemap.get(pt.getDeviceName()).add(pt);
		addtoreachsets(s);//construct reachset//reachsets.get(sn.getPosition()).add(sn);//
		ArrayList<StateNode> nxtSf = ForwardedStatesvlan(s, pt,vlanID);//put the all the other port information of the same device into the nxtSf//the nxtSF is the List of the statenode of the outputport of the device
		for(StateNode nxtsf : nxtSf)
		{
			// link transfer
			ArrayList<StateNode> nxtSl = linkTransfer(nxtsf);//try the other devices' ports which are connected with these ports 
			for(StateNode nxtsl : nxtSl)
			{
				
				Traverse_recurvlan(nxtsl, pt,vlanID);
			}
		}
	}
	
	public void Traverse_recur_u(StateNode s)
	{
		//forwarding
		addtodevicemap_u(s.getPosition());
		addtoreachsets_u(s);
		//ArrayList<StateNode> nxtSf = ForwardedStates(s);//yuzhao, can not be processed
		ArrayList<StateNode> nxtSf=new ArrayList<StateNode>();//yuzhao
		
		for(StateNode nxtsf : nxtSf)
		{
			// link transfer
			ArrayList<StateNode> nxtSl = linkTransfer(nxtsf);
		//	ArrayList<StateNode> nxtSl=new ArrayList<StateNode>();//yuzhao, can not be processed
			for(StateNode nxtsl : nxtSl)
			{

				Traverse_recur_u(nxtsl);
			}
		}
	}
	
	// no need to compute reachability
	public void Traverse_recur_d(StateNode s)
	{
		addtodevicemap_u(s.getPosition());
		addtoreachsets_u(s);
		Collection<StateNode> nstates = s.getNextStates();
		if(nstates == null)
		{
			return;
		}else
		{
			for(StateNode ns : nstates)
			{
				Collection<StateNode> nnstates = ns.getNextStates();
				if(nnstates == null)
				{
					return;
				}else
				{
					for(StateNode nns : nnstates)
					{
						Traverse_recur_d(nns);
					}
				}
			}
		}
		
	}
	
	
	public void computeSetcovervlan(ACLForTree aclfortreevlan,String vlanid){
		BDD thebdd = net.bddengine.getBDD();
		HashSet<Integer> unipermitaclset=new HashSet<Integer>(aclfortreevlan.getPermitunr().keySet());//permitaclset
		HashSet<Integer> universernameset=new HashSet<Integer>(aclfortreevlan.getUniverhashmapvlan().keySet());//fwdruleset
	//	HashSet<HashSet<Integer>> refruleset=new HashSet<HashSet<Integer>>();
		//HashSet<HashSet<Integer>> resultrulesetlist=new HashSet<HashSet<Integer>>();
		finalresultlist=new ArrayList<Finalresult>();

		for(int i=maxk;i>=0; i--){//for every k level
			if (universernameset==null){
				System.out.println("finished");
			}
		    ////////fwding rule with the acl rule//////////////////
			if(aclorignalruleset.containsKey(i)){//check the set has k or not
						ArrayList<RefSet> refsetlistwacl=aclorignalruleset.get(i);
						refsetlistwacl.sort(new RefSetComparator());//sort the aclisize 
		                for(RefSet refset:refsetlistwacl){//every refset in the list
		                	refset.getFwdruleset().retainAll(universernameset);//fwdruleset retainAll with left-no-check
		                	if(refset.getFwdruleset().size()>=Math.pow(p, i)){//if fwd size is good
		                		refset.getAclrulelist().retainAll(unipermitaclset);//aclruleset retainAll with left-no-check
		                		if(!refset.getAclrulelist().isEmpty()){//has aclrule
		                			Finalresult finalresult=new Finalresult();//
		                			finalresult.setRuleset(refset.getFwdruleset());//put ruleset into the finalresult
		                			for(int j=0;j<refset.getAclrulelist().size();j++){
		                			Integer aclname=refset.getAclrulelist().get(j);//put  aclname into the finalresult
		                			Integer aclBDD=aclfortreevlan.getPermitunr().get(aclname).getFwdBDD();
		                			Integer afterpermit=thebdd.and(aclBDD,refset.getBddafterdeny());
		                			if(afterpermit==0){
		                			//	System.out.println("afterpermit==0");
		                			}else{
		                				finalresult.setAclrule(aclname); 
			                			unipermitaclset.remove(aclname);//remove the aclname in all
			                			universernameset.removeAll(refset.getFwdruleset());//remove the fwd rulename in all
			                			break;
		                			}
		                			}
		                		//	finalresult.setAclrule(aclname); 
		                		//	unipermitaclset.remove(aclname);//remove the aclname in all
		                			finalresultlist.add(finalresult);
		                			//finalresultlist.
		                		}else{
		                			if(orignalruleset.containsKey(i)){
		                			orignalruleset.get(i).add(refset);}else{
		                				ArrayList<RefSet> newlist=new ArrayList<RefSet>();
		                				newlist.add(refset);
		                				orignalruleset.put(i, newlist);
		                			}
		                		}
		                	}else{
		                		int k=(int) Math.floor(Math.log(refset.getFwdruleset().size()) / Math.log(p));
		                		
		                		if(aclorignalruleset.containsKey(k)){
		                			aclorignalruleset.get(k).add(refset);
		                		}else{
		                			ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
					    			rulelist.add(refset);
					    			aclorignalruleset.put(k,rulelist);	
		                		}
		                		
		                	}
		                	
		                }
					}
			/////////fwding rule without the acl rule///////////////////////////////
				
			
			        if(orignalruleset.containsKey(i)){
						ArrayList<RefSet> refsetlist=orignalruleset.get(i);
						for(RefSet refset:refsetlist){
					
							
							
							refset.getFwdruleset().retainAll(universernameset);
						//	refset.getFwdruleset().retainAll(universernameset);
						//	refset.getFwdruleset().retainAll(universernameset);
							if(refset.getFwdruleset().size()>=Math.pow(p, i)){
								Finalresult finalresult=new Finalresult();//
								finalresult.setRuleset(refset.getFwdruleset());//put ruleset into the finalresult
								finalresultlist.add(finalresult);
								universernameset.removeAll(refset.getFwdruleset());//remove the fwd rulename in all
								//universernameset.removeAll(refset.getFwdruleset());//remove the fwd rulename in all
							}else{
								int k=(int) Math.floor(Math.log(refset.getFwdruleset().size()) / Math.log(p));
								if(orignalruleset.containsKey(k)){
		                		   orignalruleset.get(k).add(refset);
		                		   
		                		   
		                		   
		                		   
		                		}else{
		                			ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
		                			rulelist.add(refset);
		                			orignalruleset.put(k,rulelist);
		                		//	ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
					    		//	rulelist.add(refset);
					    		//	orignalruleset.put(k,rulelist);	
		                			
		                		}
								
							}
						}
					}
		}
	      if(vlanid.equals("vlan492")){
	        	System.out.println("test");
	        }
		this.total=this.total+finalresultlist.size()+aclfortreevlan.getDenyunr().size();
		this.remainfwd=this.remainfwd+universernameset.size();
		this.remainacl=this.remainacl+unipermitaclset.size();
		//System.out.println("vlanid"+vlanid);
		//System.out.println("finalsize"+this.total);
		//System.out.println("remainfwdsize"+this.remainfwd);
		//System.out.println("remainaclsize"+this.remainacl);
		//System.out.println("finished");
		
		//HashSet<Integer> asd ;
		//for(Finalresult final1:finalresultlist){
			//System.out.println(final1.getRuleset());
			
		//}
		
	}
	
	
	
	public void computeSetcover(HashMap<Integer,Rule> univerhashmap,ACLForTree aclfortree){
		BDD thebdd = net.bddengine.getBDD();
		int count=0;
		HashSet<Integer> unipermitaclset=new HashSet<Integer>(aclfortree.getPermitunr().keySet());//permitaclset
		HashSet<Integer> universernameset=new HashSet<Integer>(univerhashmap.keySet());//fwdruleset
	//	HashSet<HashSet<Integer>> refruleset=new HashSet<HashSet<Integer>>();
		//HashSet<HashSet<Integer>> resultrulesetlist=new HashSet<HashSet<Integer>>();
		finalresultlist=new ArrayList<Finalresult>();

		for(int i=maxk;i>=0; i--){//for every k level
			if (universernameset==null){
				System.out.println("finished");
			}
		    ////////fwding rule with the acl rule//////////////////
			if(aclorignalruleset.containsKey(i)){//check the set has k or not
						ArrayList<RefSet> refsetlistwacl=aclorignalruleset.get(i);
						refsetlistwacl.sort(new RefSetComparator());//sort the aclisize 
		                for(RefSet refset:refsetlistwacl){//every refset in the list
		                	refset.getFwdruleset().retainAll(universernameset);//fwdruleset retainAll with left-no-check
		                	if(refset.getFwdruleset().size()>=Math.pow(p, i)){//if fwd size is good
		                	
		                		refset.getAclrulelist().retainAll(unipermitaclset);//aclruleset retainAll with left-no-check
		                		if(!refset.getAclrulelist().isEmpty()){//has aclrule
		                			Finalresult finalresult=new Finalresult();//
		                			finalresult.setRuleset(refset.getFwdruleset());//put ruleset into the finalresult
		                			finalresult.setRefset(refset);
		                			////update///
			                		for(Integer rulename:refset.getFwdruleset2())
			                		{
			                			if(updateref.containsKey(rulename)){
			                				updateref2.get(rulename).add(finalresult);
			                			}else{
			                				ArrayList<Finalresult> hs=new ArrayList<Finalresult>();
			                				hs.add(finalresult);
			                				updateref2.put(rulename,hs);
			                			}
			                			//updateref.put(rulename, finalresult);
			                			//updateref2.put(rulename,hs);
			                		}
			                		
			                		
			                		///end update///
		                			for(int j=0;j<refset.getAclrulelist().size();j++){
		                			Integer aclname=refset.getAclrulelist().get(j);//put  aclname into the finalresult
		                			Integer aclBDD=aclfortree.getPermitunr().get(aclname).getFwdBDD();
		                			Integer afterpermit=thebdd.and(aclBDD,refset.getBddafterdeny());
		                			if(afterpermit==0){
		                				System.out.println("afterpermit==0");
		                			}else{
		                				finalresult.setAclrule(aclname); 
			                			unipermitaclset.remove(aclname);//remove the aclname in all
			                			universernameset.removeAll(refset.getFwdruleset());//remove the fwd rulename in all
			                			break;
		                			}
		                			}
		                		//	finalresult.setAclrule(aclname); 
		                		//	unipermitaclset.remove(aclname);//remove the aclname in all
		                			finalresultlist.add(finalresult);
		                			//finalresultlist.
		                		}else{
		                			orignalruleset.get(i).add(refset);
		                		}
		                	}else{
		                		int k=(int) Math.floor(Math.log(refset.getFwdruleset().size()) / Math.log(p));
		                		if(aclorignalruleset.containsKey(k)){
		                			aclorignalruleset.get(k).add(refset);
		                		}else{
		                			ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
					    			rulelist.add(refset);
					    			aclorignalruleset.put(k,rulelist);	
		                		}
		                		
		                	}
		                	
		                }
					}
			/////////fwding rule without the acl rule///////////////////////////////
					if(orignalruleset.containsKey(i)){
						ArrayList<RefSet> refsetlist=orignalruleset.get(i);
						for(RefSet refset:refsetlist){
					//		count++;
							
							
							refset.getFwdruleset().retainAll(universernameset);
						//	refset.getFwdruleset().retainAll(universernameset);
						//	refset.getFwdruleset().retainAll(universernameset);
							if(refset.getFwdruleset().size()>=Math.pow(p, i)){
								Finalresult finalresult=new Finalresult();//
								finalresult.setRuleset(refset.getFwdruleset());//put ruleset into the finalresult
								finalresult.setRefset(refset);
								finalresultlist.add(finalresult);
							////update///
								/*
		                		for(Integer rulename:refset.getFwdruleset())
		                		{
		                			updateref.put(rulename, finalresult);
		                		}*/
								for(Integer rulename:refset.getFwdruleset2())
		                		{
		                			if(updateref.containsKey(rulename)){
		                				updateref2.get(rulename).add(finalresult);
		                			}else{
		                				ArrayList<Finalresult> hs=new ArrayList<Finalresult>();
		                				hs.add(finalresult);
		                				updateref2.put(rulename,hs);
		                			}
		                			//updateref.put(rulename, finalresult);
		                			//updateref2.put(rulename,hs);
		                		}
		                		///end update///
								universernameset.removeAll(refset.getFwdruleset());//remove the fwd rulename in all
								//universernameset.removeAll(refset.getFwdruleset());//remove the fwd rulename in all
							}else{
								int k=(int) Math.floor(Math.log(refset.getFwdruleset().size()) / Math.log(p));
								if(orignalruleset.containsKey(k)){
		                		   orignalruleset.get(k).add(refset);
		                		   
		                		   
		                		   
		                		   
		                		}else{
		                			ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
		                			rulelist.add(refset);
		                			orignalruleset.put(k,rulelist);
		                		//	ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
					    		//	rulelist.add(refset);
					    		//	orignalruleset.put(k,rulelist);	
		                			
		                		}
								
							}
						}
					}
		}
		System.out.println("finish");
		this.total=this.total+finalresultlist.size()+aclfortree.getDenyunr().size();
		this.remainfwd=this.remainfwd+universernameset.size();
		this.remainacl=this.remainacl+unipermitaclset.size();
		
		System.out.println("finalsize"+this.total);
		System.out.println("remainfwdsize"+this.remainfwd);
		System.out.println("remainaclsize"+this.remainacl);
		/*
		System.out.println("finalsize"+finalresultlist.size());
		System.out.println("remainfwdsize"+universernameset.size());
		System.out.println("remainaclsize"+unipermitaclset.size());
		*/
		//System.out.println("finished");
		
		//HashSet<Integer> asd ;
		//for(Finalresult final1:finalresultlist){
			//System.out.println(final1.getRuleset());
			
		//}
		
	}
	
	public void computeSetcoverupdate(HashMap<Integer,Rule> univerhashmap,ACLForTree aclfortree){
		
	
		
		int count=0;
		BDD thebdd = net.bddengine.getBDD();
		HashSet<Integer> unipermitaclset=new HashSet<Integer>(aclfortree.getPermitunr().keySet());//permitaclset
		HashSet<Integer> universernameset=new HashSet<Integer>(univerhashmap.keySet());//fwdruleset
	//	HashSet<HashSet<Integer>> refruleset=new HashSet<HashSet<Integer>>();
		//HashSet<HashSet<Integer>> resultrulesetlist=new HashSet<HashSet<Integer>>();
		ArrayList<Finalresult> finalresultlist=new ArrayList<Finalresult>();

		for(int i=maxk;i>=0; i--){//for every k level
			if (universernameset==null){
				System.out.println("finished");
			}
		    ////////fwding rule with the acl rule//////////////////
			if(aclorignalruleset.containsKey(i)){//check the set has k or not
						ArrayList<RefSet> refsetlistwacl=aclorignalruleset.get(i);
						refsetlistwacl.sort(new RefSetComparator());//sort the aclisize 
		                for(RefSet refset:refsetlistwacl){//every refset in the list
		            
		                	refset.getFwdruleset2().retainAll(universernameset);//fwdruleset retainAll with left-no-check
		                	if(refset.getFwdruleset2().size()>=Math.pow(p, i)){//if fwd size is good
		                	
		                		refset.getAclrulelist().retainAll(unipermitaclset);//aclruleset retainAll with left-no-check
		                		if(!refset.getAclrulelist().isEmpty()){//has aclrule
		                			Finalresult finalresult=new Finalresult();//
		                			finalresult.setRuleset(new HashSet<Integer>(refset.getFwdruleset2()));//put ruleset into the finalresult
		                			finalresult.setRefset(refset);
		                			////update///
			                		for(Integer rulename:refset.getFwdruleset2())
			                		{
			                			updateref.put(rulename, finalresult);
			                		}
			                		///end update///
		                			for(int j=0;j<refset.getAclrulelist().size();j++){
		                			Integer aclname=refset.getAclrulelist().get(j);//put  aclname into the finalresult
		                			Integer aclBDD=aclfortree.getPermitunr().get(aclname).getFwdBDD();
		                			Integer afterpermit=thebdd.and(aclBDD,refset.getBddafterdeny());
		                			if(afterpermit==0){
		                				System.out.println("afterpermit==0");
		                			}else{
		                				finalresult.setAclrule(aclname); 
			                			unipermitaclset.remove(aclname);//remove the aclname in all
			                			universernameset.removeAll(refset.getFwdruleset2());//remove the fwd rulename in all
			                			break;
		                			}
		                			}
		                		//	finalresult.setAclrule(aclname); 
		                		//	unipermitaclset.remove(aclname);//remove the aclname in all
		                			finalresultlist.add(finalresult);
		                			//finalresultlist.
		                		}else{
		                			orignalruleset.get(i).add(refset);
		                		}
		                	}else{
		                		int k=(int) Math.floor(Math.log(refset.getFwdruleset2().size()) / Math.log(p));
		                		if(aclorignalruleset.containsKey(k)){
		                			aclorignalruleset.get(k).add(refset);
		                		}else{
		                			ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
					    			rulelist.add(refset);
					    			aclorignalruleset.put(k,rulelist);	
		                		}
		                		
		                	}
		                	
		                }
					}
			/////////fwding rule without the acl rule///////////////////////////////
					if(orignalruleset.containsKey(i)){
						ArrayList<RefSet> refsetlist=orignalruleset.get(i);
						for(RefSet refset:refsetlist){
							count++;
							
						//	orignalruleset.keySet()
							refset.getFwdruleset2().retainAll(universernameset);
						//	refset.getFwdruleset().retainAll(universernameset);
						//	refset.getFwdruleset().retainAll(universernameset);
							if(refset.getFwdruleset2().size()>=Math.pow(p, i)){
								Finalresult finalresult=new Finalresult();//
								finalresult.setRuleset(new HashSet<Integer>(refset.getFwdruleset2()));//put ruleset into the finalresult
								finalresult.setRefset(refset);
								finalresultlist.add(finalresult);
							////update///
		                		for(Integer rulename:refset.getFwdruleset2())
		                		{
		                			updateref.put(rulename, finalresult);
		                		}
		                		///end update///
								universernameset.removeAll(refset.getFwdruleset2());//remove the fwd rulename in all
								//universernameset.removeAll(refset.getFwdruleset());//remove the fwd rulename in all
							}else{
								int k=(int) Math.floor(Math.log(refset.getFwdruleset2().size()) / Math.log(p));
								if(orignalruleset.containsKey(k)){
		                		   orignalruleset.get(k).add(refset);
		                		   
		                		   
		                		   
		                		   
		                		}else{
		                			ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
		                			rulelist.add(refset);
		                			orignalruleset.put(k,rulelist);
		                		//	ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
					    		//	rulelist.add(refset);
					    		//	orignalruleset.put(k,rulelist);	
		                			
		                		}
								
							}
						}
					}
		}
		System.out.println("finish");
		this.total=this.total+finalresultlist.size()+aclfortree.getDenyunr().size();
		this.remainfwd=this.remainfwd+universernameset.size();
		this.remainacl=this.remainacl+unipermitaclset.size();
		
		System.out.println("finalsize"+this.total);
		System.out.println("remainfwdsize"+this.remainfwd);
		System.out.println("remainaclsize"+this.remainacl);
		/*
		System.out.println("finalsize"+finalresultlist.size());
		System.out.println("remainfwdsize"+universernameset.size());
		System.out.println("remainaclsize"+unipermitaclset.size());
		*/
		//System.out.println("finished");
		
		//HashSet<Integer> asd ;
		//for(Finalresult final1:finalresultlist){
			//System.out.println(final1.getRuleset());
			
		//}
		
	}
	
	/*
	//full greedy
	HashSet<Integer> universernameset=new HashSet<Integer>(univerhashmap.keySet());
	HashSet<HashSet<Integer>> refruleset=new HashSet<HashSet<Integer>>();
	HashSet<HashSet<Integer>> resultrulesetlist=new HashSet<HashSet<Integer>>();

	
	for(int test: orignalruleset.keySet()){
		for(HashSet<Integer> ruleset: orignalruleset.get(test))
		refruleset.add(ruleset);
	}
	int ii=0;
	while(true){
		ii++;
		System.out.println(ii);
		System.out.println(universernameset.size());
		System.out.println(resultrulesetlist.size());
	   int	maxmax=0;
	   HashSet<Integer> maxruleset=new HashSet<Integer>();
	   for(HashSet<Integer> ruleset: refruleset){
		   ruleset.retainAll(universernameset);//remove all result
		   if  (ruleset.size()>maxmax){
			  maxmax=ruleset.size();  
			  maxruleset=ruleset;
		  }				  
	   }
//	   if(maxmax==1){
//			  System.out.println("bingo");
//		  }
	   refruleset.remove(maxruleset);
	   refruleset.contains(maxruleset);
	   universernameset.removeAll(maxruleset);
	   resultrulesetlist.add(maxruleset);
	   if(universernameset.isEmpty()){break;}
	}
	
	*/
	
	
	
	//compute the ruleset list of each portpair
	public void computePortpair(ACLForTree aclfortree)
	{
		ArrayList<AP> APlist=net.fwdapc.getAPset();
		HashMap<Integer,AP> APhashmap=new HashMap<Integer,AP>();
	//	orignalruleset=new HashMap<Integer,ArrayList<HashSet<Integer>>>();
		this.orignalruleset=new HashMap<Integer,ArrayList<RefSet>>();
		this.aclorignalruleset=new HashMap<Integer,ArrayList<RefSet>>();
		for(int i=0; i<APlist.size(); i++){
			APhashmap.put(APlist.get(i).getApbdds(), APlist.get(i));
			
			//build a hashmap in the AP,in oder to check faster
			HashMap<PositionTuple,ArrayList<Rule>> ptruleset=new HashMap<PositionTuple,ArrayList<Rule>>();
			for (Predicate predicate: APlist.get(i).getListpredicate()){//build a hashmap
				PositionTuple pt=new PositionTuple(predicate.getDevicename(),predicate.getPortname());
				ptruleset.put(pt,predicate.getRuleset());	//build a HashMap<PositionTuple,ArrayList<Rule>> which can search the ruleset easily		
			}//one AP's all predicate build the hashMap as pt,arraylist
			APlist.get(i).setPtruleset(ptruleset);//add the ptruleset into the AP
			// The ruleset of the predicate in the AP
			
		}
		//compute HashMap<Integer,AP>
		int kkk=0;
		///////////////////////////////////
		for(Portpair portpair: portpairlist)//for every portpair			
		{   
			/*
			System.out.println("portpairID"+kkk++);
		    System.out.println("portpairsize"+portpairlist.size());
			
		    */
		    HashSet<Integer> APset;
			FWDAPSet APBDDlist=portpair.getAPBDDlist();//APBDDlist is the portpait's AP list. And it may be a complement style
			//find the APset of the Portpair
			if(APBDDlist.isIscomplement()){
				HashSet<Integer> universeset=new HashSet<Integer>(FWDAPSet.getUniverseset());//static method need to be operate in static
				
				universeset.removeAll(APBDDlist.getApset());//if it is complement, the ap list should be got by complement at universet
				APset=universeset;
			}else{
			    APset=APBDDlist.getApset();
			}	
	        int kk1=0;
		    for (Integer apbdd: APset){//find the AP ap in the portair's APset
		    	
		    	kk1++;
		    	
		    	AP ap=APhashmap.get(apbdd);//find the corresponding ap from the portpair apset
		    	/*
		    	for(Predicate predicate: ap.getListpredicate()){
	    			if(predicate.getPortname().equals("xe-1/0/3")){
	    				if(predicate.getDevicename().equals("atla")){
	    				System.out.println("bingo");
	    				predicate.getRuleset().size();
	    				}
	    			}
	    		}
	    		*/
		    	ArrayList<PositionTuple> oldarrivedportlist=portpair.getArrivedport();//find the arrivedport from the portpair port
		    	
		    	ArrayList<PositionTuple> arrivedportlist= new ArrayList<PositionTuple>();
				
			    for (int i=0; i<oldarrivedportlist.size(); i++){
			    	if(i%2==1){
			    		arrivedportlist.add(oldarrivedportlist.get(i));
			    	}
			    }//select ports of the output
		    	
		    	ArrayList<ArrayList<Rule>> rulesetlist=new ArrayList<ArrayList<Rule>>();
		    	for(PositionTuple pt: arrivedportlist){//find every pt in the  arrivedportlist
		    		
		    		ArrayList<Rule> rulesethead=new ArrayList<Rule>(ap.getPtruleset().get(pt));//find the pt's rule set in AP
		    		//ArrayList<Rule> rulesetail=new ArrayList<Rule>(ap.getPtruleset().get(pt));
		    		
		    		//rulesethead.sort(new RuleComparatorHeader());
		    		rulesetlist.add(rulesethead);
		    	//	rulesetail.sort(new RuleComparatorTail());
		    	//	rulesetlist.add(rulesetail);
		    		//System.out.println("bingo");
		    	}
		    	ArrayList<Ruleoutput> ruleoutputlist=computeruleset(rulesetlist);//compute the all possible ruleset in this ap of this portpair
		    	
		    	//////////////////////
		    	HashMap<String,Integer> hp=new HashMap<String,Integer>();
                int countnum=0;
		    	for(PositionTuple pt: arrivedportlist){
		    		hp.put(pt.getDeviceName(),countnum++);
		    	}
		    		
		    	for(Ruleoutput ruleoutput:ruleoutputlist){
		    		ArrayList<Rule> newrulelist=new ArrayList<Rule>();
		    		for(int countq=0; countq<ruleoutput.getRuleset().size();countq++){
		    			newrulelist.add(null);
		    		}
		    		
		    		for(Rule rule:ruleoutput.getRuleset()){
		    			newrulelist.set(hp.get(rule.getDevicename()),rule);
		    		}
		    		ruleoutput.setRuleset(newrulelist);
		    		
		    	}//sort the rule with the port sort, this is for the updat

		    	computePortruleset(ruleoutputlist,portpair);
		    //	System.out.println(ruleoutputlist.size());
		    	for(Ruleoutput ruleoutput: ruleoutputlist){
		    		int rulesetsize=ruleoutput.getRuleset().size();
		    		int k=(int) Math.floor(Math.log(rulesetsize) / Math.log(p));//Math.log(value) / Math.log(base);
		    		
		    		if(k>maxk){
		    			maxk=k;//at here maxk is for the count of how many k
		    		}
		    		
		    		//ArrayList<PositionTuple> portlist=ruleoutput.getArrivedportlist();
		    		ArrayList<PositionTuple> portlist=oldarrivedportlist;
		    		//ArrayList<HashMap<Integer,ArrayList<ACLrule>>> permitin=new ArrayList<HashMap<Integer,ArrayList<ACLrule>>>();
		    		//ArrayList<HashMap<Integer,ArrayList<ACLrule>>> permitout=new ArrayList<HashMap<Integer,ArrayList<ACLrule>>>();
		    		//HashMap<Integer,ArrayList<ACLrule>> permitin=new HashMap<Integer,ArrayList<ACLrule>>();
		    		HashMap<Integer,HashSet<ACLrule>> permitin=new HashMap<Integer,HashSet<ACLrule>>();
		    		HashMap<Integer,HashSet<ACLrule>> permitout=new HashMap<Integer,HashSet<ACLrule>>();
		    		//HashMap<Integer,ArrayList<ACLrule>> permitout=new HashMap<Integer,ArrayList<ACLrule>>();
		    		RefSet refset=new RefSet();
		    		 for(int j=0; j<portlist.size(); j++){//get the possible aclrule set duing to the portlist
		    			if(j%2==1){
				    				String uniport=new String(portlist.get(j).getDeviceName()+portlist.get(j).getPortName());
				    				
						    		if(aclfortree.getPermitout().containsKey(uniport)){//permitout
						    		    for(Integer rn: aclfortree.getPermitout().get(uniport).keySet())
						    		    {
						    		    	if(permitout.containsKey(rn)){
						    		    		permitout.get(rn).addAll(new HashSet<ACLrule>(aclfortree.getPermitout().get(uniport).get(rn)));//can be improved
						    		    	}else{
						    		    		permitout.put(rn,new HashSet<ACLrule>(aclfortree.getPermitout().get(uniport).get(rn)));//rn is the rulename
						    		    	}
						    		    	
						    		    }		
						    	//		permitout.add(aclfortree.getPermitout().get(uniport));	
						    		}
						    		
						    		if(j==1){//permitin at the random port at the first port
										    String pairsporth=new String(portlist.get(j).getDeviceName()+portlist.get(j).getPortName());
										    		if(aclfortree.getPermitinup().containsKey(pairsporth)){
										    			for(Integer rn: aclfortree.getPermitinup().get(pairsporth).keySet())
										    		    {
										    		    	if(permitin.containsKey(rn)){
										    		    		permitin.get(rn).addAll(new HashSet<ACLrule>(aclfortree.getPermitinup().get(pairsporth).get(rn)));
										    		    	}else{
										    		    		permitin.put(rn,new HashSet<ACLrule>(aclfortree.getPermitinup().get(pairsporth).get(rn)));
										    		    	}
										    		    	
										    		    }		
										    	//		permitout.add(aclfortree.getPermitout().get(uniport));	
								    		        }
						    		}else{
								    		String pairsport=new String(portlist.get(j).getDeviceName()+portlist.get(j-1).getPortName()+portlist.get(j).getPortName());
								    		if(aclfortree.getPermitin().containsKey(pairsport)){
								    			for(Integer rn: aclfortree.getPermitin().get(pairsport).keySet())
								    		    {
								    		    	if(permitin.containsKey(rn)){
								    		    		permitin.get(rn).addAll(new HashSet<ACLrule>(aclfortree.getPermitin().get(pairsport).get(rn)));
								    		    	}else{
								    		    		permitin.put(rn,new HashSet<ACLrule>(aclfortree.getPermitin().get(pairsport).get(rn)));
								    		    	}
								    		    	
								    		    }			
								    		//	 permitin.add(aclfortree.getPermitin().get(pairsport));
								    		   }
						    		}
				    		
				            //this part is deny part
						    		//HashMap<String,Integer> 
						    		//String pairsport=new String(portlist.get(j).getDeviceName()+portlist.get(j-1).getPortName()+portlist.get(j).getPortName());
						    		if(j!=1){
								    		if(aclfortree.getPortindeny().containsKey(new String(portlist.get(j).getDeviceName()+portlist.get(j-1)))){
								    			refset.getDenyBDD().add(aclfortree.getPortindeny().get(new String(portlist.get(j).getDeviceName()+portlist.get(j-1))));
								    		}
						    		}
						    		if(aclfortree.getPortoutdeny().containsKey(new String(portlist.get(j).getDeviceName()+portlist.get(j)))){
						    			refset.getDenyBDD().add(aclfortree.getPortoutdeny().get(new String(portlist.get(j).getDeviceName()+portlist.get(j))));
						    		}
						    		
		    			  }
		    		  }
		    		 
		    		 BDD thebdd = net.bddengine.getBDD();
		    		 Integer rulesetbdd=ruleoutput.getRulesetBDD();
		    		 for(Integer denybdd:refset.getDenyBDD())//deny rule effect
		    		 {
		    			 rulesetbdd=thebdd.and(denybdd,rulesetbdd);
		    		 }
		    		 thebdd.ref(rulesetbdd);
		    		 if(rulesetbdd==0){
		    			 System.out.println("rulesetbdd==0");
		    			 continue;
		    		 }else{
		    			// ruleoutput.setRulesetBDD(rulesetbdd);
		    			 refset.setBddafterdeny(rulesetbdd);
		    		 }
		    		 
		    		for(Rule eachrule:ruleoutput.getRuleset()){
		    			
		    			String rulename=eachrule.getRulename();
		    			refset.getFwdruleset().add(Integer.valueOf(rulename));
		    			refset.getFwdruleset2().add(Integer.valueOf(rulename));
		                if(permitin.containsKey(Integer.valueOf(rulename))){
		                	for(ACLrule aclrule:permitin.get(Integer.valueOf(rulename))){
		                		refset.getAclrulelist().add(aclrule.getRulename());
		                	}
		                }
		                if(permitout.containsKey(Integer.valueOf(rulename))){
		                	for(ACLrule aclrule:permitout.get(Integer.valueOf(rulename))){
		                		refset.getAclrulelist().add(aclrule.getRulename());
		                	}
	
		                }
		    			//eachrule.getRuleBDD();
		    		}
		    		//////////////////////final to orignalruleset///////////////////////////////////////
		    	//	updateaclorignalrule.
		    	//	apupadteorignalruleset
		    		
		    		if(!refset.getAclrulelist().isEmpty()){//if refset can measure the acl rule
		    			if(aclorignalruleset.containsKey(k)){
		    				/*
		    				aclorignalruleset.get(k).add(refset);
		    				//update
		    				if(apupadteaclorignalruleset.containsKey(ap.getApname())){
		    					apupadteaclorignalruleset.get(ap.getApname()).get(k).add(refset);//can be modify
		    				}else{
		    					HashMap<Integer,ArrayList<RefSet>> newmap=new HashMap<Integer,ArrayList<RefSet>>();
		    					ArrayList<RefSet> newlist=new ArrayList<RefSet>();
		    					newlist.add(refset);
		    					newmap.put(k, newlist);
		    					apupadteaclorignalruleset.put(ap.getApname(), newmap);
		    					
		    				}
		    				*/
		    				aclorignalruleset.get(k).add(refset);
		    				//update
		    				if(apupadteaclorignalruleset.containsKey(ap.getApname())){
		    					if(apupadteaclorignalruleset.get(ap.getApname()).containsKey(k)){
		    					apupadteaclorignalruleset.get(ap.getApname()).get(k).add(refset);//can be modify
		    					}else{
		    						ArrayList<RefSet> newlist=new ArrayList<RefSet>();
			    					newlist.add(refset);
			    					apupadteaclorignalruleset.get(ap.getApname()).put(k, newlist);
		    					}
		    				
		    				
		    				}else{
		    					HashMap<Integer,ArrayList<RefSet>> newmap=new HashMap<Integer,ArrayList<RefSet>>();
		    					ArrayList<RefSet> newlist=new ArrayList<RefSet>();
		    					newlist.add(refset);
		    					newmap.put(k, newlist);
		    					apupadteaclorignalruleset.put(ap.getApname(), newmap);
		    					
		    				}

		    			}else{
		    				ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
		    				rulelist.add(refset);
		    				aclorignalruleset.put(k,rulelist);
		    				//update
		    				if(apupadteaclorignalruleset.containsKey(ap.getApname())){		
		    					if(apupadteaclorignalruleset.get(ap.getApname()).containsKey(k)){
			    					apupadteaclorignalruleset.get(ap.getApname()).get(k).add(refset);//can be modify
			    					}else{
			    						ArrayList<RefSet> newlist=new ArrayList<RefSet>();
				    					newlist.add(refset);
				    					apupadteaclorignalruleset.get(ap.getApname()).put(k, newlist);
			    					}
		    				}else{
		    					HashMap<Integer,ArrayList<RefSet>> newmap=new HashMap<Integer,ArrayList<RefSet>>();
		    					ArrayList<RefSet> newlist=new ArrayList<RefSet>();
		    					newlist.add(refset);
		    					newmap.put(k, newlist);
		    					apupadteaclorignalruleset.put(ap.getApname(), newmap);
		    					
		    				}/*
		    				ArrayList<RefSet> aclrulelist=new ArrayList<RefSet>();
		    				aclrulelist.add(refset);
		    				aclorignalruleset.put(k,aclrulelist);
		    				//update
		    				if(apupadteaclorignalruleset.containsKey(ap.getApname())){		
		    					ArrayList<RefSet> newlist=new ArrayList<RefSet>(aclrulelist);
		    					apupadteaclorignalruleset.get(ap.getApname()).put(k,newlist);//can be modify
		    				}else{
		    					HashMap<Integer,ArrayList<RefSet>> newmap=new HashMap<Integer,ArrayList<RefSet>>();
		    					ArrayList<RefSet> newlist=new ArrayList<RefSet>();
		    					newlist.add(refset);
		    					newmap.put(k, newlist);
		    					apupadteaclorignalruleset.put(ap.getApname(), newmap);	
		    				}*/
		    			}
		
		    		}else{//if refset can't measure the acl rule
		    			if(orignalruleset.containsKey(k)){
		    				orignalruleset.get(k).add(refset);
		    				//update
		    				if(apupadteorignalruleset.containsKey(ap.getApname())){
		    					if(apupadteorignalruleset.get(ap.getApname()).containsKey(k)){
		    					apupadteorignalruleset.get(ap.getApname()).get(k).add(refset);//can be modify
		    					}else{
		    						ArrayList<RefSet> newlist=new ArrayList<RefSet>();
			    					newlist.add(refset);
			    					apupadteorignalruleset.get(ap.getApname()).put(k, newlist);
		    					}
		    				
		    				
		    				}else{
		    					HashMap<Integer,ArrayList<RefSet>> newmap=new HashMap<Integer,ArrayList<RefSet>>();
		    					ArrayList<RefSet> newlist=new ArrayList<RefSet>();
		    					newlist.add(refset);
		    					newmap.put(k, newlist);
		    					apupadteorignalruleset.put(ap.getApname(), newmap);
		    					
		    				}
		    				
		    			}else{
		    				ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
		    				rulelist.add(refset);
		    				orignalruleset.put(k,rulelist);
		    				//update
		    				if(apupadteorignalruleset.containsKey(ap.getApname())){		
		    					if(apupadteorignalruleset.get(ap.getApname()).containsKey(k)){
			    					apupadteorignalruleset.get(ap.getApname()).get(k).add(refset);//can be modify
			    					}else{
			    						ArrayList<RefSet> newlist=new ArrayList<RefSet>();
				    					newlist.add(refset);
				    					apupadteorignalruleset.get(ap.getApname()).put(k, newlist);
			    					}
		    				}else{
		    					HashMap<Integer,ArrayList<RefSet>> newmap=new HashMap<Integer,ArrayList<RefSet>>();
		    					ArrayList<RefSet> newlist=new ArrayList<RefSet>();
		    					newlist.add(refset);
		    					newmap.put(k, newlist);
		    					apupadteorignalruleset.put(ap.getApname(), newmap);
		    					
		    				}
		    				
		    			}
		    			
		    			
		    		}
		    		
		    		/*
		    		
		    		
		    		//ruleonaxislist.sort(new RuleComparator());
		    		
		    		
		    		if(orignalruleset.containsKey(k)){
		    			
		    			HashSet<Integer> rulenameoutputSet=new HashSet<Integer>();// for all no acl ruleset	
		    			HashSet<Integer> rulenameoutputSetACL=new HashSet<Integer>();//for all acl ruleset
		    			for(Rule eachrule: ruleoutput.getRuleset()){
		    				
		    				rulenameoutputSet.add(Integer.valueOf(eachrule.getRulename()));
		    			    //rulenameoutputList.add(0,Integer.valueOf(eachrule.getRulename()));
		    			}
		    			
		    			orignalruleset.get(k).add(rulenameoutputSet);
		    			
		    		}else {
		    			//HashSet<Rule> ruleoutputSet =new HashSet<Rule>(ruleoutput.getRuleset());
		    			//ArrayList<HashSet<Rule>> rulelist=new ArrayList<HashSet<Rule>>();
		    			//rulelist.add(ruleoutputSet);
		    			//orignalruleset.put(k,rulelist);
		    			HashSet<Integer> rulenameoutputSet=new HashSet<Integer>();
		    			ArrayList<HashSet<Integer>> rulelist=new ArrayList<HashSet<Integer>>();
		    			for(Rule eachrule: ruleoutput.getRuleset()){
		    				rulenameoutputSet.add(Integer.valueOf(eachrule.getRulename()));}
		    			rulelist.add(rulenameoutputSet);
		    			orignalruleset.put(k,rulelist);
		    			
		    			
		    		}
		    		*/
		    	//	orignalruleset
		    	}
		    	
		    }
		
		}
		/*
		int count1=0;
		
			for(Integer bb:orignalruleset.keySet()){
				count1=count1+orignalruleset.get(bb).size();
				
			}
		
		System.out.println(count1);
		*/
	}//end public void computePortpair()
	
	public void computePortpairupdate(ACLForTree aclfortree)
	{
		
		
		///update
		aclorignalruleset=new HashMap<Integer,ArrayList<RefSet>>();
		orignalruleset=new HashMap<Integer,ArrayList<RefSet>>();
		for (Integer apchangedname: net.fwdapc.getAPchanged().keySet()){
			if(apupadteorignalruleset.containsKey(apchangedname)){//can be modify
				apupadteorignalruleset.remove(apchangedname);
			}
			if(apupadteaclorignalruleset.containsKey(apchangedname)){
				apupadteaclorignalruleset.remove(apchangedname);
			}
		}/*
		int count=0;
		for(Integer aa: apupadteorignalruleset.keySet()){
			for(Integer bb:apupadteorignalruleset.get(aa).keySet()){
				count=count+apupadteorignalruleset.get(aa).get(bb).size();
				
			}
		}
		*/
		for(Integer apname: apupadteorignalruleset.keySet()){

			for(Integer k: apupadteorignalruleset.get(apname).keySet()){
					if(orignalruleset.containsKey(k)){
						orignalruleset.get(k).addAll(apupadteorignalruleset.get(apname).get(k));
					//orignalruleset.putAll(apupadteorignalruleset.get(apname));
					}else{
						ArrayList<RefSet> newlist=new ArrayList<RefSet>();
						orignalruleset.put(k,newlist);
						orignalruleset.get(k).addAll(apupadteorignalruleset.get(apname).get(k));
					}
			}
		}
		
		for(Integer apname: apupadteaclorignalruleset.keySet()){

			for(Integer k: apupadteaclorignalruleset.get(apname).keySet()){
					if(aclorignalruleset.containsKey(k)){
						aclorignalruleset.get(k).addAll(apupadteaclorignalruleset.get(apname).get(k));
					//orignalruleset.putAll(apupadteorignalruleset.get(apname));
					}else{
						ArrayList<RefSet> newlist=new ArrayList<RefSet>();
						aclorignalruleset.put(k,newlist);
						aclorignalruleset.get(k).addAll(apupadteaclorignalruleset.get(apname).get(k));
					}
			}
		}
		/*
		int count1=0;
		
		for(Integer bb:orignalruleset.keySet()){
			count1=count1+orignalruleset.get(bb).size();
			
		}
	
	   System.out.println(count1);
	   */
		///update
		
		
		
		ArrayList<AP> APlist=net.fwdapc.getAPset();


		
		HashMap<Integer,AP> APhashmap=new HashMap<Integer,AP>();
	//	orignalruleset=new HashMap<Integer,ArrayList<HashSet<Integer>>>();
		//this.orignalruleset=new HashMap<Integer,ArrayList<RefSet>>();
		//this.aclorignalruleset=new HashMap<Integer,ArrayList<RefSet>>();
		//PositionTuple pt1=new PositionTuple("atla","xe-1/0/3");//test
		for(int i=0; i<APlist.size(); i++){
			APhashmap.put(APlist.get(i).getApbdds(), APlist.get(i));
			//build a hashmap in the AP,in oder to check faster
			
			HashMap<PositionTuple,ArrayList<Rule>> ptruleset=new HashMap<PositionTuple,ArrayList<Rule>>();
			for (Predicate predicate: APlist.get(i).getListpredicate()){//build a hashmap
				PositionTuple pt=new PositionTuple(predicate.getDevicename(),predicate.getPortname());
				ptruleset.put(pt,predicate.getRuleset());	//build a HashMap<PositionTuple,ArrayList<Rule>> which can search the ruleset easily	
			
				
				
				
			}//one AP's all predicate build the hashMap as pt,arraylist
			APlist.get(i).setPtruleset(ptruleset);//add the ptruleset into the AP
			// The ruleset of the predicate in the AP
		}
	

		
		//compute HashMap<Integer,AP>
		int kkk=0;
		///////////////////////////////////
		for(Portpair portpair: portpairlist)//for every portpair			
		{   
		    HashSet<Integer> APset;
			FWDAPSet APBDDlist=portpair.getAPBDDlist();//APBDDlist is the portpait's AP list. And it may be a complement style
			//find the APset of the Portpair
			if(APBDDlist.isIscomplement()){
				HashSet<Integer> universeset=new HashSet<Integer>(FWDAPSet.getUniverseset());//static method need to be operate in static
				
				universeset.removeAll(APBDDlist.getApset());//if it is complement, the ap list should be got by complement at universet
				APset=universeset;
			}else{
			    APset=APBDDlist.getApset();
			}	
		//	System.out.println("portpairID"+kkk++);
	//	    System.out.println("portpairsize"+portpairlist.size());
		//    System.out.println("APsize"+APset.size());
	        int kk1=0;
		    for (Integer apbdd: APset){//find the AP ap in the portair's APset
		  
		    	kk1++;
		    	
		    	AP ap=APhashmap.get(apbdd);//find the corresponding ap from the portpair apset
		    	/*
		        if(ap.getApname()==10){
		        	System.out.println("bingo");
		        }
				for(Predicate predicate: ap.getListpredicate()){
					  for(Rule rule: predicate.getRuleset()){
						  if(rule.getRulename().equals("296")){
							  System.out.println("bingo");
						  }
					  }
						
				}
		        */
		    if(net.fwdapc.getAPchanged().containsKey(ap.getApname())){
		    	ArrayList<PositionTuple> oldarrivedportlist=portpair.getArrivedport();//find the arrivedport from the portpair port
		    	
		    	ArrayList<PositionTuple> arrivedportlist= new ArrayList<PositionTuple>();
				
			    for (int i=0; i<oldarrivedportlist.size(); i++){
			    	if(i%2==1){
			    		arrivedportlist.add(oldarrivedportlist.get(i));
			    	}
			    }//select ports of the output
		    	
		    	ArrayList<ArrayList<Rule>> rulesetlist=new ArrayList<ArrayList<Rule>>();
		    	for(PositionTuple pt: arrivedportlist){//find every pt in the  arrivedportlist
		    		
		    		ArrayList<Rule> rulesethead=new ArrayList<Rule>(ap.getPtruleset().get(pt));//find the pt's rule set in AP
		    		//ArrayList<Rule> rulesetail=new ArrayList<Rule>(ap.getPtruleset().get(pt));
		    		
		    		//rulesethead.sort(new RuleComparatorHeader());
		    		rulesetlist.add(rulesethead);
		    	//	rulesetail.sort(new RuleComparatorTail());
		    	//	rulesetlist.add(rulesetail);
		    		//System.out.println("bingo");
		    
		    		/*
		    		for(Predicate predicate: ap.getListpredicate()){
		  			  for(Rule rule: predicate.getRuleset()){
		  				  if(rule.getRulename().equals("296")){
		  					  System.out.println("bingo");
		  				  }
		  			  }
		  				
		  			}
		  			*/
		    		
		    	}
		    	ArrayList<Ruleoutput> ruleoutputlist=computeruleset(rulesetlist);//compute the all possible ruleset in this ap of this portpair
             //   System.out.println(ruleoutputlist.size());
		
		    	//////////////////////
		    	HashMap<String,Integer> hp=new HashMap<String,Integer>();
                int countnum=0;
		    	for(PositionTuple pt: arrivedportlist){
		    		hp.put(pt.getDeviceName(),countnum++);
		    	}
		    		
		    	for(Ruleoutput ruleoutput:ruleoutputlist){
		    		ArrayList<Rule> newrulelist=new ArrayList<Rule>();
		    		for(int countq=0; countq<ruleoutput.getRuleset().size();countq++){
		    			newrulelist.add(null);
		    		}
		    		
		    		for(Rule rule:ruleoutput.getRuleset()){
		    			newrulelist.set(hp.get(rule.getDevicename()),rule);
		    		}
		    		ruleoutput.setRuleset(newrulelist);
		    		
		    	}//sort the rule with the port sort, this is for the updat

		    	computePortruleset(ruleoutputlist,portpair);
		    	
		    	for(Ruleoutput ruleoutput: ruleoutputlist){
		    		int rulesetsize=ruleoutput.getRuleset().size();
		    		int k=(int) Math.floor(Math.log(rulesetsize) / Math.log(p));//Math.log(value) / Math.log(base);
		    		
		    		if(k>maxk){
		    			maxk=k;//at here maxk is for the count of how many k
		    		}
		    		
		    		//ArrayList<PositionTuple> portlist=ruleoutput.getArrivedportlist();
		    		ArrayList<PositionTuple> portlist=oldarrivedportlist;
		    		//ArrayList<HashMap<Integer,ArrayList<ACLrule>>> permitin=new ArrayList<HashMap<Integer,ArrayList<ACLrule>>>();
		    		//ArrayList<HashMap<Integer,ArrayList<ACLrule>>> permitout=new ArrayList<HashMap<Integer,ArrayList<ACLrule>>>();
		    		//HashMap<Integer,ArrayList<ACLrule>> permitin=new HashMap<Integer,ArrayList<ACLrule>>();
		    		HashMap<Integer,HashSet<ACLrule>> permitin=new HashMap<Integer,HashSet<ACLrule>>();
		    		HashMap<Integer,HashSet<ACLrule>> permitout=new HashMap<Integer,HashSet<ACLrule>>();
		    		//HashMap<Integer,ArrayList<ACLrule>> permitout=new HashMap<Integer,ArrayList<ACLrule>>();
		    		RefSet refset=new RefSet();
		    		 for(int j=0; j<portlist.size(); j++){//get the possible aclrule set duing to the portlist
		    			if(j%2==1){
				    				String uniport=new String(portlist.get(j).getDeviceName()+portlist.get(j).getPortName());
				    				
						    		if(aclfortree.getPermitout().containsKey(uniport)){//permitout
						    		    for(Integer rn: aclfortree.getPermitout().get(uniport).keySet())
						    		    {
						    		    	if(permitout.containsKey(rn)){
						    		    		permitout.get(rn).addAll(new HashSet<ACLrule>(aclfortree.getPermitout().get(uniport).get(rn)));//can be improved
						    		    	}else{
						    		    		permitout.put(rn,new HashSet<ACLrule>(aclfortree.getPermitout().get(uniport).get(rn)));//rn is the rulename
						    		    	}
						    		    	
						    		    }		
						    	//		permitout.add(aclfortree.getPermitout().get(uniport));	
						    		}
						    		
						    		if(j==1){//permitin at the random port at the first port
										    String pairsporth=new String(portlist.get(j).getDeviceName()+portlist.get(j).getPortName());
										    		if(aclfortree.getPermitinup().containsKey(pairsporth)){
										    			for(Integer rn: aclfortree.getPermitinup().get(pairsporth).keySet())
										    		    {
										    		    	if(permitin.containsKey(rn)){
										    		    		permitin.get(rn).addAll(new HashSet<ACLrule>(aclfortree.getPermitinup().get(pairsporth).get(rn)));
										    		    	}else{
										    		    		permitin.put(rn,new HashSet<ACLrule>(aclfortree.getPermitinup().get(pairsporth).get(rn)));
										    		    	}
										    		    	
										    		    }		
										    	//		permitout.add(aclfortree.getPermitout().get(uniport));	
								    		        }
						    		}else{
								    		String pairsport=new String(portlist.get(j).getDeviceName()+portlist.get(j-1).getPortName()+portlist.get(j).getPortName());
								    		if(aclfortree.getPermitin().containsKey(pairsport)){
								    			for(Integer rn: aclfortree.getPermitin().get(pairsport).keySet())
								    		    {
								    		    	if(permitin.containsKey(rn)){
								    		    		permitin.get(rn).addAll(new HashSet<ACLrule>(aclfortree.getPermitin().get(pairsport).get(rn)));
								    		    	}else{
								    		    		permitin.put(rn,new HashSet<ACLrule>(aclfortree.getPermitin().get(pairsport).get(rn)));
								    		    	}
								    		    	
								    		    }			
								    		//	 permitin.add(aclfortree.getPermitin().get(pairsport));
								    		   }
						    		}
				    		
				            //this part is deny part
						    		//HashMap<String,Integer> 
						    		//String pairsport=new String(portlist.get(j).getDeviceName()+portlist.get(j-1).getPortName()+portlist.get(j).getPortName());
						    		if(j!=1){
								    		if(aclfortree.getPortindeny().containsKey(new String(portlist.get(j).getDeviceName()+portlist.get(j-1)))){
								    			refset.getDenyBDD().add(aclfortree.getPortindeny().get(new String(portlist.get(j).getDeviceName()+portlist.get(j-1))));
								    		}
						    		}
						    		if(aclfortree.getPortoutdeny().containsKey(new String(portlist.get(j).getDeviceName()+portlist.get(j)))){
						    			refset.getDenyBDD().add(aclfortree.getPortoutdeny().get(new String(portlist.get(j).getDeviceName()+portlist.get(j))));
						    		}
						    		
		    			  }
		    		  }
		    		 
		    		 BDD thebdd = net.bddengine.getBDD();
		    		 Integer rulesetbdd=ruleoutput.getRulesetBDD();
		    		 for(Integer denybdd:refset.getDenyBDD())//deny rule effect
		    		 {
		    			 rulesetbdd=thebdd.and(denybdd,rulesetbdd);
		    		 }
		    		 thebdd.ref(rulesetbdd);
		    		 if(rulesetbdd==0){
		    			 System.out.println("rulesetbdd==0");
		    			 continue;
		    		 }else{
		    			// ruleoutput.setRulesetBDD(rulesetbdd);
		    			 refset.setBddafterdeny(rulesetbdd);
		    		 }
		    		 
		    		for(Rule eachrule:ruleoutput.getRuleset()){
		    			
		    			String rulename=eachrule.getRulename();
		    			refset.getFwdruleset().add(Integer.valueOf(rulename));
		    			refset.getFwdruleset2().add(Integer.valueOf(rulename));
		                if(permitin.containsKey(Integer.valueOf(rulename))){
		                	for(ACLrule aclrule:permitin.get(Integer.valueOf(rulename))){
		                		refset.getAclrulelist().add(aclrule.getRulename());
		                	}
		                }
		                if(permitout.containsKey(Integer.valueOf(rulename))){
		                	for(ACLrule aclrule:permitout.get(Integer.valueOf(rulename))){
		                		refset.getAclrulelist().add(aclrule.getRulename());
		                	}
	
		                }
		    			//eachrule.getRuleBDD();
		    		}
		    		//////////////////////final to orignalruleset///////////////////////////////////////
		    	//	updateaclorignalrule.
		    	//	apupadteorignalruleset
		    		
		    		
		    		if(!refset.getAclrulelist().isEmpty()){//if refset can measure the acl rule
		    			if(aclorignalruleset.containsKey(k)){
		    				aclorignalruleset.get(k).add(refset);
		    				//update
		    				if(apupadteaclorignalruleset.containsKey(ap.getApname())){
		    					if(apupadteaclorignalruleset.get(ap.getApname()).containsKey(k)){
		    					apupadteaclorignalruleset.get(ap.getApname()).get(k).add(refset);//can be modify
		    					}else{
		    						ArrayList<RefSet> newlist=new ArrayList<RefSet>();
			    					newlist.add(refset);
			    					apupadteaclorignalruleset.get(ap.getApname()).put(k, newlist);
		    					}
		    				
		    				
		    				}else{
		    					HashMap<Integer,ArrayList<RefSet>> newmap=new HashMap<Integer,ArrayList<RefSet>>();
		    					ArrayList<RefSet> newlist=new ArrayList<RefSet>();
		    					newlist.add(refset);
		    					newmap.put(k, newlist);
		    					apupadteaclorignalruleset.put(ap.getApname(), newmap);
		    					
		    				}

		    			}else{
		    				ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
		    				rulelist.add(refset);
		    				aclorignalruleset.put(k,rulelist);
		    				//update
		    				if(apupadteaclorignalruleset.containsKey(ap.getApname())){		
		    					if(apupadteaclorignalruleset.get(ap.getApname()).containsKey(k)){
			    					apupadteaclorignalruleset.get(ap.getApname()).get(k).add(refset);//can be modify
			    					}else{
			    						ArrayList<RefSet> newlist=new ArrayList<RefSet>();
				    					newlist.add(refset);
				    					apupadteaclorignalruleset.get(ap.getApname()).put(k, newlist);
			    					}
		    				}else{
		    					HashMap<Integer,ArrayList<RefSet>> newmap=new HashMap<Integer,ArrayList<RefSet>>();
		    					ArrayList<RefSet> newlist=new ArrayList<RefSet>();
		    					newlist.add(refset);
		    					newmap.put(k, newlist);
		    					apupadteaclorignalruleset.put(ap.getApname(), newmap);
		    					
		    				}
		    			}
		    		}else{//if refset can't measure the acl rule
		    			if(orignalruleset.containsKey(k)){
		    				orignalruleset.get(k).add(refset);
		    				//update
		    				if(apupadteorignalruleset.containsKey(ap.getApname())){
		    					if(apupadteorignalruleset.get(ap.getApname()).containsKey(k)){
		    					apupadteorignalruleset.get(ap.getApname()).get(k).add(refset);//can be modify
		    					}else{
		    						ArrayList<RefSet> newlist=new ArrayList<RefSet>();
			    					newlist.add(refset);
			    					apupadteorignalruleset.get(ap.getApname()).put(k, newlist);
		    					}
		    				
		    				
		    				}else{
		    					HashMap<Integer,ArrayList<RefSet>> newmap=new HashMap<Integer,ArrayList<RefSet>>();
		    					ArrayList<RefSet> newlist=new ArrayList<RefSet>();
		    					newlist.add(refset);
		    					newmap.put(k, newlist);
		    					apupadteorignalruleset.put(ap.getApname(), newmap);
		    					
		    				}
		    				
		    			}else{
		    				ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
		    				rulelist.add(refset);
		    				orignalruleset.put(k,rulelist);
		    				//update
		    				if(apupadteorignalruleset.containsKey(ap.getApname())){		
		    					if(apupadteorignalruleset.get(ap.getApname()).containsKey(k)){
			    					apupadteorignalruleset.get(ap.getApname()).get(k).add(refset);//can be modify
			    					}else{
			    						ArrayList<RefSet> newlist=new ArrayList<RefSet>();
				    					newlist.add(refset);
				    					apupadteorignalruleset.get(ap.getApname()).put(k, newlist);
			    					}
		    				}else{
		    					HashMap<Integer,ArrayList<RefSet>> newmap=new HashMap<Integer,ArrayList<RefSet>>();
		    					ArrayList<RefSet> newlist=new ArrayList<RefSet>();
		    					newlist.add(refset);
		    					newmap.put(k, newlist);
		    					apupadteorignalruleset.put(ap.getApname(), newmap);
		    					
		    				}
		    				
		    			}
		    			/*
		    			int count=0;
		    			
		    				for(Integer bb:orignalruleset.keySet()){
		    					count=count+orignalruleset.get(bb).size();
		    					
		    				}
		    			
		    			System.out.println(count);
		    			*/
		    		}
		    		
		    		/*
		    		
		    		
		    		//ruleonaxislist.sort(new RuleComparator());
		    		
		    		
		    		if(orignalruleset.containsKey(k)){
		    			
		    			HashSet<Integer> rulenameoutputSet=new HashSet<Integer>();// for all no acl ruleset	
		    			HashSet<Integer> rulenameoutputSetACL=new HashSet<Integer>();//for all acl ruleset
		    			for(Rule eachrule: ruleoutput.getRuleset()){
		    				
		    				rulenameoutputSet.add(Integer.valueOf(eachrule.getRulename()));
		    			    //rulenameoutputList.add(0,Integer.valueOf(eachrule.getRulename()));
		    			}
		    			
		    			orignalruleset.get(k).add(rulenameoutputSet);
		    			
		    		}else {
		    			//HashSet<Rule> ruleoutputSet =new HashSet<Rule>(ruleoutput.getRuleset());
		    			//ArrayList<HashSet<Rule>> rulelist=new ArrayList<HashSet<Rule>>();
		    			//rulelist.add(ruleoutputSet);
		    			//orignalruleset.put(k,rulelist);
		    			HashSet<Integer> rulenameoutputSet=new HashSet<Integer>();
		    			ArrayList<HashSet<Integer>> rulelist=new ArrayList<HashSet<Integer>>();
		    			for(Rule eachrule: ruleoutput.getRuleset()){
		    				rulenameoutputSet.add(Integer.valueOf(eachrule.getRulename()));}
		    			rulelist.add(rulenameoutputSet);
		    			orignalruleset.put(k,rulelist);
		    			
		    			
		    		}
		    		*/
		    	//	orignalruleset
		    	}
		    }//end update if
		    }
		
		}
		/*
		int count1=0;
		
			for(Integer bb:orignalruleset.keySet()){
				count1=count1+orignalruleset.get(bb).size();
				
			}
		
		System.out.println(count1);
		*/
	}//end public void computePortpair()
	
	public void computePortpairvlan(ACLForTree aclfortree, String VlanID)
	{
		

		ArrayList<AP> APlist=net.fwdapc.getAPmapvlan().get(VlanID);
		HashMap<Integer,AP> APhashmap=new HashMap<Integer,AP>();//<apbdd,ap>
	//	orignalruleset=new HashMap<Integer,ArrayList<HashSet<Integer>>>();
		this.orignalruleset=new HashMap<Integer,ArrayList<RefSet>>();
		this.aclorignalruleset=new HashMap<Integer,ArrayList<RefSet>>();
		for(int i=0; i<APlist.size(); i++){
			APhashmap.put(APlist.get(i).getApbdds(), APlist.get(i));
			
			//build a hashmap in the AP,in oder to check faster
			HashMap<PositionTuple,ArrayList<Rule>> ptruleset=new HashMap<PositionTuple,ArrayList<Rule>>();
			for (VlanPredicate vlanpredicate: APlist.get(i).getVlanpredicatelist()){//build a hashmap
				for (String portname:vlanpredicate.getPortname()){
					PositionTuple pt=new PositionTuple(vlanpredicate.getDevicename(),portname);
					ptruleset.put(pt, vlanpredicate.getPortrule().get(portname));
				}
					//build a HashMap<PositionTuple,ArrayList<Rule>> which can search the ruleset easily		
			}//one AP's all predicate build the hashMap as pt,arraylist
			APlist.get(i).setPtruleset(ptruleset);//add the ptruleset into the AP
			// The ruleset of the predicate in the AP
			
		}
		//compute HashMap<Integer,AP>
		int kkk=0;
		///////////////////////////////////
		for(Portpair portpair: portpairlist)//for every portpair			
		{   
			/*
			System.out.println("portpairID"+kkk++);
		    System.out.println("portpairsize"+portpairlist.size());
			*/
		    
		    HashSet<Integer> APset;
			FWDAPSet APBDDlist=portpair.getAPBDDlist();//APBDDlist is the portpait's AP list. And it may be a complement style
			//find the APset of the Portpair
			if(APBDDlist.isIscomplement()){
				//notice notice, it the isIscomplement happens the FWDAPSet fnction need to be rewrite!!!!!!!
				HashSet<Integer> universeset=new HashSet<Integer>(FWDAPSet.getUniverseset());//static method need to be operate in static
				universeset.removeAll(APBDDlist.getApset());//if it is complement, the ap list should be got by complement at universet
				APset=universeset;
			}else{
			    APset=APBDDlist.getApset();
			}	
	        int kk1=0;
		    for (Integer apbdd: APset){//find the AP ap in the portair's APset
		    	
		    	kk1++;
		    	
		    	AP ap=APhashmap.get(apbdd);//find the corresponding ap from the portpair apset
		    	ArrayList<PositionTuple> oldarrivedportlist=portpair.getArrivedport();//find the arrivedport from the portpair port
		    	
		    	ArrayList<PositionTuple> arrivedportlist= new ArrayList<PositionTuple>();
				
			    for (int i=0; i<oldarrivedportlist.size(); i++){
			    	if(i%2==1){
			    		arrivedportlist.add(oldarrivedportlist.get(i));
			    	}
			    }//select ports of the output
		    	
		    	ArrayList<ArrayList<Rule>> rulesetlist=new ArrayList<ArrayList<Rule>>();
		    	for(PositionTuple pt: arrivedportlist){//find every pt in the  arrivedportlist
		    		//ap.getPtruleset().keySet();
		    		

		    		
		    		ArrayList<Rule> rulesethead=new ArrayList<Rule>(ap.getPtruleset().get(pt));//find the pt's rule set in AP
		    		rulesetlist.add(rulesethead);
		    	}
		    	ArrayList<Ruleoutput> ruleoutputlist=computeruleset(rulesetlist);//compute the all possible ruleset in this ap of this portpair
		    	computePortruleset(ruleoutputlist,portpair);
		    	
		    	for(Ruleoutput ruleoutput: ruleoutputlist){
		    		int rulesetsize=ruleoutput.getRuleset().size();
		    		int k=(int) Math.floor(Math.log(rulesetsize) / Math.log(p));//Math.log(value) / Math.log(base);
		    		
		    		if(k>maxk){
		    			maxk=k;//at here maxk is for the count of how many k
		    		}
		    		
		    		
		    	
		    		ArrayList<PositionTuple> portlist=oldarrivedportlist;
		    		HashMap<Integer,HashSet<ACLrule>> permitin=new HashMap<Integer,HashSet<ACLrule>>();
		    		HashMap<Integer,HashSet<ACLrule>> permitout=new HashMap<Integer,HashSet<ACLrule>>();//<fwdrulename,set<aclrule>>
		    		RefSet refset=new RefSet();
		    		 for(int j=0; j<portlist.size(); j++){//get the possible aclrule set duing to the portlist
		    			if(j%2==1){
				    				String uniport=new String(portlist.get(j).getDeviceName()+portlist.get(j).getPortName());//get the already port information
				    				
						    		if(aclfortree.getPermitout().containsKey(uniport)){//permitout
						    		    for(Integer rn: aclfortree.getPermitout().get(uniport).keySet())
						    		    {
						    		    	if(permitout.containsKey(rn)){//if the permitout has a fwd rn
						    		    		permitout.get(rn).addAll(new HashSet<ACLrule>(aclfortree.getPermitout().get(uniport).get(rn)));//can be improved
						    		    	}else{
						    		    		permitout.put(rn,new HashSet<ACLrule>(aclfortree.getPermitout().get(uniport).get(rn)));//rn is the rulename//collect all of uniport's aclrule into it
						    		    	}
						    		    	
						    		    }		
	
						    		}
						    		
						    		if(j==1){//permitin at the random port at the first port
										    String pairsporth=new String(portlist.get(j).getDeviceName()+portlist.get(j).getPortName());
										    		if(aclfortree.getPermitinup().containsKey(pairsporth)){
										    			for(Integer rn: aclfortree.getPermitinup().get(pairsporth).keySet())
										    		    {
										    		    	if(permitin.containsKey(rn)){
										    		    		permitin.get(rn).addAll(new HashSet<ACLrule>(aclfortree.getPermitinup().get(pairsporth).get(rn)));
										    		    	}else{
										    		    		permitin.put(rn,new HashSet<ACLrule>(aclfortree.getPermitinup().get(pairsporth).get(rn)));//put pairsporth's the fwrulename and acl into the permit
										    		    	}
										    		    	
										    		    }		
										    	//		permitout.add(aclfortree.getPermitout().get(uniport));	
								    		        }
						    		}else{
								    		String pairsport=new String(portlist.get(j).getDeviceName()+portlist.get(j-1).getPortName()+portlist.get(j).getPortName());
								    		if(aclfortree.getPermitin().containsKey(pairsport)){
								    			for(Integer rn: aclfortree.getPermitin().get(pairsport).keySet())
								    		    {
								    		    	if(permitin.containsKey(rn)){
								    		    		permitin.get(rn).addAll(new HashSet<ACLrule>(aclfortree.getPermitin().get(pairsport).get(rn)));
								    		    	}else{
								    		    		permitin.put(rn,new HashSet<ACLrule>(aclfortree.getPermitin().get(pairsport).get(rn)));
								    		    	}
								    		    	
								    		    }			
								    		//	 permitin.add(aclfortree.getPermitin().get(pairsport));
								    		   }
						    		}
				    		
				            //this part is deny part
						    		//HashMap<String,Integer> 
						    		//String pairsport=new String(portlist.get(j).getDeviceName()+portlist.get(j-1).getPortName()+portlist.get(j).getPortName());
						    		if(j!=1){
								    		if(aclfortree.getPortindeny().containsKey(new String(portlist.get(j).getDeviceName()+portlist.get(j-1)))){
								    			refset.getDenyBDD().add(aclfortree.getPortindeny().get(new String(portlist.get(j).getDeviceName()+portlist.get(j-1))));
								    		}
						    		}
						    		if(aclfortree.getPortoutdeny().containsKey(new String(portlist.get(j).getDeviceName()+portlist.get(j)))){
						    			refset.getDenyBDD().add(aclfortree.getPortoutdeny().get(new String(portlist.get(j).getDeviceName()+portlist.get(j))));
						    		}
						    		
		    			  }
		    		  }
		    		 BDD thebdd = net.bddengine.getBDD();
		    		 Integer rulesetbdd=ruleoutput.getRulesetBDD();
		    		 for(Integer denybdd:refset.getDenyBDD())
		    		 {
		    			 rulesetbdd=thebdd.and(denybdd,rulesetbdd);
		    		 }
		    		 thebdd.ref(rulesetbdd);
		    		 if(rulesetbdd==0){
		    			 System.out.println("rulesetbdd==0");
		    			 continue;
		    		 }else{
		    			 refset.setBddafterdeny(rulesetbdd);
		    		 }
		    		 
		    
		    		 
		    		for(Rule eachrule:ruleoutput.getRuleset()){
		    			
		    			String rulename=eachrule.getRulename();
		    			
		    			refset.getFwdruleset().add(Integer.valueOf(rulename));
		                if(permitin.containsKey(Integer.valueOf(rulename))){
		                	for(ACLrule aclrule:permitin.get(Integer.valueOf(rulename))){//every fwd's rulename to match the permitin's acl rule
		                		refset.getAclrulelist().add(aclrule.getRulename());//add the acl rule into the refset
		                		refset.getAclmap().put(aclrule.getRulename(),Integer.valueOf(rulename));
		                	}
		                }
		                if(permitout.containsKey(Integer.valueOf(rulename))){
		                	for(ACLrule aclrule:permitout.get(Integer.valueOf(rulename))){
		                		refset.getAclrulelist().add(aclrule.getRulename());
		                		refset.getAclmap().put(aclrule.getRulename(), Integer.valueOf(rulename));
		                	}
	
		                }
		    			//eachrule.getRuleBDD();
		    		}
		    		
		    		if(!refset.getAclrulelist().isEmpty()){//if refset can measure the acl rule
		    			if(aclorignalruleset.containsKey(k)){
		    				aclorignalruleset.get(k).add(refset);
		    			}else{
		    				ArrayList<RefSet> aclrulelist=new ArrayList<RefSet>();
		    				aclrulelist.add(refset);
		    				aclorignalruleset.put(k,aclrulelist);
		    			}
		
		    		}else{//if refset can't measure the acl rule
		    			if(orignalruleset.containsKey(k)){
		    				orignalruleset.get(k).add(refset);
		    			}else{
		    				ArrayList<RefSet> rulelist=new ArrayList<RefSet>();
		    				rulelist.add(refset);
		    				orignalruleset.put(k,rulelist);
		    			}
		    			
		    			
		    		}
		    		
		    	}
		    	
		    }
		
		}
		
	}
	
	public void computePortruleset(ArrayList<Ruleoutput> ruleoutputlist,Portpair portpair){
		
		BDD thebdd = net.bddengine.getBDD();
		//net.bddengine;
		HashMap<Integer, ArrayList<Rule>> portruleset=new HashMap<Integer, ArrayList<Rule>>();
		ArrayList<Ruleoutput> rulelistremove=new ArrayList<Ruleoutput>();
		for(Ruleoutput ruleoutput:ruleoutputlist){
			//System.out.println(count++);
			int i=0;
			int BDD=0;
			int newBDD=0;
			for(Rule rule:ruleoutput.getRuleset()){//
				if(i==0){
				BDD=rule.getRuleBDD();
				thebdd.ref(BDD);
				i=1;
				}else{
			    newBDD=thebdd.and(BDD,rule.getRuleBDD());
				thebdd.ref(newBDD);
				thebdd.deref(BDD);
				BDD=newBDD;
				}
				
			}
			if(BDD==0){
			//	System.out.println("bingo");
				rulelistremove.add(ruleoutput);
			}else{
			portruleset.put(BDD, ruleoutput.getRuleset());		
			ruleoutput.setRulesetBDD(BDD);
			
			}
			//HashMap<Integer, ArrayList<Rule>> portruleset=new
			portpair.setPortruleset(portruleset);
			//private HashMap<Integer, ArrayList<Rule>> Portruleset;
		}
		ruleoutputlist.removeAll(rulelistremove);
	}
	
	public ArrayList<Ruleoutput> computeruleset(ArrayList<ArrayList<Rule>> rulesetlist)
	{
		ArrayList<Ruleoutput> ruleoutputlist=new ArrayList<Ruleoutput>();//output
		ArrayList<RuleOnAxis> ruleonaxislist=new ArrayList<RuleOnAxis>();
		for (int i=0;i<rulesetlist.size();i=i+1){
			for(Rule rule: rulesetlist.get(i)){
				RuleOnAxis ruleonaxis0=new RuleOnAxis(rule,"head",rule.getHead(),i);
				ruleonaxislist.add(ruleonaxis0);
				RuleOnAxis ruleonaxis1=new RuleOnAxis(rule,"tail",rule.getTail(),i);
				ruleonaxislist.add(ruleonaxis1);
			}	
		}//get the head space and tail space on the axis
		ruleonaxislist.sort(new RuleComparator());
		
		
		HashMap<Integer,ArrayList<Rule>> recordhashmap=new HashMap<Integer,ArrayList<Rule>>();
		for (int i=0;i<rulesetlist.size();i++){
			ArrayList<Rule> rulelist=new ArrayList<Rule>();
			recordhashmap.put(i, rulelist);
		}
		
		int[] oldspace=null;
		ArrayList<RuleOnAxis> recordlist=new ArrayList<RuleOnAxis>();
		
		
		for (int i=0; i<ruleonaxislist.size()-1;i++){
		//	boolean spaceequal=false;
			RuleOnAxis ruleonaxis=ruleonaxislist.get(i);
			Rule rule=ruleonaxis.getRule();
			
			if(!Arrays.equals(ruleonaxis.getSpace(), oldspace)){
			//	spaceequal=true;
		       if(!recordlist.isEmpty()){
						for(RuleOnAxis recordruleonaxis: recordlist){					
							recordhashmap.get(recordruleonaxis.getPortid()).remove(recordruleonaxis.getRule());	
						}
						recordlist.clear();		
		       }
				oldspace=ruleonaxis.getSpace();//record the last oldspace's name to compare with a new one
			}
				
			if(ruleonaxis.getHeadortail().equals("head") ){
				
				boolean tag=false;
				for(int j=0;j<rulesetlist.size();j++){//check whether there is a combination ranging space
					if(j!=ruleonaxis.getPortid()){
					  if (recordhashmap.get(j).isEmpty()){
						  tag=true;
						  break;
					  }
					} 
				}
				
				if(tag==false){
					ArrayList<ArrayList<Rule>> result=new ArrayList<ArrayList<Rule>>();
					ArrayList<Rule> curList=new ArrayList<Rule>();
					recursive(recordhashmap, result, 0, curList,ruleonaxis);//search all combination ranging space and insert them into the ruleoutputlist
					for(ArrayList<Rule> rulelistreuslt: result){
						Ruleoutput ruleoutput=new Ruleoutput(null,null,rulelistreuslt);
						ruleoutputlist.add(ruleoutput);
					}
					
					
				}
		
			recordhashmap.get(ruleonaxis.getPortid()).add(rule);
			}else{
				recordlist.add(ruleonaxis);
			}
			
		}
		
		return ruleoutputlist;
	}
	public void recursive(HashMap<Integer,ArrayList<Rule>> recordhashmap,ArrayList<ArrayList<Rule>> result, int layer, ArrayList<Rule> curList, RuleOnAxis ruleonaxis){
		
		
			if (layer < recordhashmap.size() - 1){
				if(layer==ruleonaxis.getPortid()) {
					ArrayList<Rule> list=new ArrayList<Rule>(curList);
					list.add(ruleonaxis.getRule());
					recursive(recordhashmap,result,layer+1,list, ruleonaxis);
				}
				else{
					for (int i = 0; i < recordhashmap.get(layer).size(); i++) { 
						ArrayList<Rule> list=new ArrayList<Rule>(curList);
						list.add(recordhashmap.get(layer).get(i));
						recursive(recordhashmap,result,layer+1,list, ruleonaxis);
	                    }
					}
				
			}else if(layer == recordhashmap.size() - 1){
				if(layer!=ruleonaxis.getPortid()) {
					for (int i = 0; i < recordhashmap.get(layer).size(); i++) {  
		                ArrayList<Rule> list = new ArrayList<Rule>(curList);  
		                list.add(recordhashmap.get(layer).get(i));  
		                result.add(list);  
					}
				}else{
					ArrayList<Rule> list = new ArrayList<Rule>(curList);  
	                list.add(ruleonaxis.getRule());  
	                result.add(list);
				}//
			}
	}
	
	
	public int compare(int[] head1, int[] head2) {//rule1's head compare rule2's tail
		// TODO Auto-generated method stub
	//	int[] head1 = ruleoutput.getHead();
	//	int[] head2 = rule2.getTail();
		
		// TODO: compare head1 and head2
		// if rule1 is less than rule2, return -1
		// if rule1 is equal to rule2, return 0
		// if rule1 is grater than rule2, return 1
		for(int i=32-1;i>=0;i--){
			if(head1[i]!=head2[i])
			{
				if (head1[i]==1){
					return 1;
				}else{return -1;}
			}			
		}
		return 0;
		
	}
	
	public void updateaddfwd(HashMap<Integer,Integer> rulechangelist,int newrulebdd){
		BDD thebdd = net.bddengine.getBDD();
		for(Integer rulename:rulechangelist.keySet()){
			RefSet refset=updateref2.get(rulename).get(0).getRefset();
			int afterdeny=refset.getBddafterdeny();
			int newnot=thebdd.not(newrulebdd);
			int result=thebdd.and(newnot, afterdeny);
			if(result!=0){
				refset.setBddafterdeny(result);
				continue;
			}else{
			    Integer aclrulename=updateref2.get(0).get(rulename).getAclrule();
				Integer fwdrulename=updateref2.get(0).get(rulename).getRefset().getAclmap().get(aclrulename);//acl need be modified
				if(fwdrulename==rulename){
					System.out.println("add an acl rule");
				}
				updateremovefwd(rulename,1);
			    Finalresult finalresult=new Finalresult();
			    RefSet refset0=new RefSet();
			    ArrayList<Integer> ru=new  ArrayList<Integer>();
			    ru.add(rulename);
			    finalresult.setRefset(refset0);
			    
			}
		}
	}
	
	public void updateremovefwd(Integer rulename,int tag){
		for(Finalresult finalresult:updateref2.get(rulename)){
		
			
		int type=0;
		Integer aclrulename=finalresult.getAclrule();
		Integer fwdrulename=finalresult.getRefset().getAclmap().get(aclrulename);
		//Integer aclrulename=updateref.get(rulename).getAclrule();
		//Integer fwdrulename=updateref.get(rulename).getRefset().getAclmap().get(aclrulename);//acl need be modified
		if(fwdrulename==rulename){
			System.out.println("add an acl rule");
			type=1;
		}
		//Finalresult finalresult=updateref.get(rulename);
		//updateref.remove(rulename);
		ArrayList<Integer> newlist=finalresult.getRefset().getFwdruleset2();//fwd rulename list

		ArrayList<Integer> headlist=new ArrayList<Integer>();
		ArrayList<Integer> tailist=new ArrayList<Integer>();
		int i=0;
		for(Integer runame:newlist){//every rule in the newlist add into the headlist and tail list separatly
			
			if(runame==rulename){
				i=1;
			}else {
				if(i==0){
					headlist.add(runame);
				}else{
					tailist.add(runame);
				}
			}
		}
			finalresultlist.remove(finalresult);
			if(!headlist.isEmpty()){//build two new finalresult, the setFwdruleset2 is the due
					Finalresult finalresult1=new Finalresult();
					finalresult1.setAclrule(finalresult.getAclrule());
		            RefSet refset=new RefSet(finalresult.getRefset());
		            refset.setFwdruleset2(headlist);
					finalresult1.setRefset(refset);
					finalresultlist.add(finalresult1);
			}
			if(!tailist.isEmpty()){
					Finalresult finalresult2=new Finalresult();
					finalresult2.setAclrule(finalresult.getAclrule());
		            RefSet refset=new RefSet(finalresult.getRefset());
		            refset.setFwdruleset2(tailist);
					finalresult2.setRefset(refset);
					finalresultlist.add(finalresult2);
			}
			if(tag==1){
				break;
			}
		}
		/*
		Integer aclrulename=updateref.get(rulename).getAclrule();
		Integer fwdrulename=updateref.get(rulename).getRefset().getAclmap().get(aclrulename);//acl need be modified
		if(fwdrulename==rulename){
			System.out.println("add an acl rule");
			type=1;
		}
		Finalresult finalresult=updateref.get(rulename);
		updateref.remove(rulename);
		ArrayList<Integer> newlist=finalresult.getRefset().getFwdruleset2();//fwd rulename list

		ArrayList<Integer> headlist=new ArrayList<Integer>();
		ArrayList<Integer> tailist=new ArrayList<Integer>();
		int i=0;
		for(Integer runame:newlist){//every rule in the newlist add into the headlist and tail list separatly
			
			if(runame==rulename){
				i=1;
			}else {
				if(i==0){
					headlist.add(runame);
				}else{
					tailist.add(runame);
				}
			}
		}
			finalresultlist.remove(finalresult);
			if(!headlist.isEmpty()){//build two new finalresult, the setFwdruleset2 is the due
					Finalresult finalresult1=new Finalresult();
					finalresult1.setAclrule(finalresult.getAclrule());
		            RefSet refset=new RefSet(finalresult.getRefset());
		            refset.setFwdruleset2(headlist);
					finalresult1.setRefset(refset);
					finalresultlist.add(finalresult1);
			}
			if(!tailist.isEmpty()){
					Finalresult finalresult2=new Finalresult();
					finalresult2.setAclrule(finalresult.getAclrule());
		            RefSet refset=new RefSet(finalresult.getRefset());
		            refset.setFwdruleset2(tailist);
					finalresult2.setRefset(refset);
					finalresultlist.add(finalresult2);
			}
			*/
			}
	

	@SuppressWarnings("finally")
	public static void main (String[] args) throws IOException
	{
		/*
		File filetest = new File("C:/test.txt");
		 filetest.createNewFile();
		   FileWriter fwtest = new FileWriter(filetest.getAbsoluteFile());
		   BufferedWriter bwtest = new BufferedWriter(fwtest);
		   fwtest.write(123);
		   bwtest.close();
		*/
		
		
		File file = new File("C:/pai2.txt");
		// file.createNewFile();
		   FileWriter fw = new FileWriter(file.getAbsoluteFile());
		   BufferedWriter bw = new BufferedWriter(fw);
		   
		File file2=    new File("C:/pri2.txt");
		FileWriter fw2 = new FileWriter(file2.getAbsoluteFile());
		BufferedWriter bw2 = new BufferedWriter(fw2);
		
		File file3=    new File("C:/gai2.txt");
		FileWriter fw3 = new FileWriter(file3.getAbsoluteFile());
		BufferedWriter bw3 = new BufferedWriter(fw3);
		
		File file4=    new File("C:/gri2.txt");
		FileWriter fw4 = new FileWriter(file4.getAbsoluteFile());
		BufferedWriter bw4 = new BufferedWriter(fw4);
		
		File file5=    new File("C:/past.txt");
		FileWriter fw5 = new FileWriter(file5.getAbsoluteFile());
		BufferedWriter bw5 = new BufferedWriter(fw5);
	
		File file6=    new File("C:/prst.txt");
		FileWriter fw6 = new FileWriter(file6.getAbsoluteFile());
		BufferedWriter bw6 = new BufferedWriter(fw6);
		
		File file7=    new File("C:/gast.txt");
		FileWriter fw7 = new FileWriter(file7.getAbsoluteFile());
		BufferedWriter bw7 = new BufferedWriter(fw7);
		
		File file8=    new File("C:/grst.txt");
		FileWriter fw8 = new FileWriter(file8.getAbsoluteFile());
		BufferedWriter bw8 = new BufferedWriter(fw8);
		
		
		if(1==1){
		   for(int ii=1;ii<=1000;ii++){   
			        Random random = new Random();
			        int iii = random.nextInt(70000);
					long t1 = System.nanoTime();
					//System.out.println(d.fws);
					Network n = new Network("i2",iii);
					if((n.remaintoupdate.getPortname()==null)){
						break;
					}		
					ReachabilityGraph rg = new ReachabilityGraph(n);////put the net information to this class as net and topology
					ACLForTree aclfortree=n.aclfortree;
					HashMap<String ,ACLForTree> aclfortreevlan=n.aclfortreevlan;
					//Set<PositionTuple> pts = n.getallactiveports();//return topology.keySet(),这个keyset‘s content is <devicename，portname>
					ArrayList<PositionTuple> pts=new ArrayList<PositionTuple>();
					for(String devicename : n.devices.keySet())
					{
						PositionTuple pt=new PositionTuple(devicename,"randomport");
						pts.add(pt);
					}
					//all of the source port
			        rg.portpairlist= new ArrayList<Portpair>();
					int rep = 100;
					for(PositionTuple pt1 : pts)//bianli every port in the keyset
					{
						System.out.println(pt1);
						rg.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
			
							rg.Traverse();
							rg.startstate.printLoop();
					}
					long t9 = System.nanoTime();
					rg.computePortpair(aclfortree);
					long t10 = System.nanoTime();
					System.out.println(" testtakes" + (t10 - t9) + "s");
					rg.computeSetcover(n.univerhashmap,aclfortree);
					///////////////////////////////////////////////////////////////////////////vlan////
					HashSet<ReachabilityGraph> rgvlanset=new HashSet<ReachabilityGraph>();
					
					for(String vlanid: n.univervlan.keySet()){//every vlanid
						//ACLForTree aclfortreevlan=n.aclfortreevlan.get(vlanid);
						ReachabilityGraph rgvlan = new ReachabilityGraph(n);
						rgvlan.portpairlist= new ArrayList<Portpair>();
						for(PositionTuple pt1 : pts)//bianli every port in the keyset//the same pts as above
						{
							if(!n.univervlan.get(vlanid).contains(pt1.getDeviceName())){//if the vlan do not have such device, continue
								continue;
							}
											
							    rgvlan.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
			
								rgvlan.Traversevlan(vlanid);
							//	rgvlan.startstate.printLoop();
						}
					//	aclfortreevlan
						rgvlan.computePortpairvlan(aclfortreevlan.get(vlanid),vlanid);
						rgvlan.computeSetcovervlan(aclfortreevlan.get(vlanid),vlanid);
						
						rgvlanset.add(rgvlan);
					}
					long t2 = System.nanoTime();
					System.out.println(" alltakes" + (t2 - t1)/1000000000 + "s");
					///////////////////perfect update///////////////////////
					try{
						long t3;
						long t4;
						t3 = System.nanoTime();
						//fw 2873102336 24 te2/3.10
						//String devicename="atla";
						//String portname="xe-0/0/1";
						//long ipaddr=69274592L;
						//int prefixlen=27;
						long ipaddr=n.remaintoupdate.getdestip();
						int prefixlen=n.remaintoupdate.getprefixlen();
						String devicename=n.remaintoupdate.getDevicename();
						String portname=n.remaintoupdate.getPortname();
						n.perfectupdateadd(devicename, portname, ipaddr, prefixlen);
						///set cover
						
						rg.portpairlist= new ArrayList<Portpair>();
						for(PositionTuple pt1 : pts)//bianli every port in the keyset
						{
							System.out.println(pt1);
							rg.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
				
								rg.Traverse();
								rg.startstate.printLoop();
						}
						rg.computePortpairupdate(aclfortree);
						rg.computeSetcoverupdate(n.univerhashmap,aclfortree);
						
						
						t4 = System.nanoTime();
						System.out.println(" addtakes" + (t4 - t3) + "ns");
						 bw.write(Long.toString((t4-t3)));
						 bw.newLine();
					}finally{
						continue;}
		   }
	}
	
		bw.close();
				////////////////////////////////////perfect remove///////////////////////
		
		if(1==1){
			for(int ii=1;ii<=1000;ii++){   
		        Random random = new Random();
		        int iii = random.nextInt(70000);
							long t1 = System.nanoTime();
							//System.out.println(d.fws);
							Network n = new Network("i2",10000000);
							//if((n.remaintoupdate.getPortname()==null)){
							//	break;
							//}		
							ReachabilityGraph rg = new ReachabilityGraph(n);////put the net information to this class as net and topology
							ACLForTree aclfortree=n.aclfortree;
							HashMap<String ,ACLForTree> aclfortreevlan=n.aclfortreevlan;
							//Set<PositionTuple> pts = n.getallactiveports();//return topology.keySet(),这个keyset‘s content is <devicename，portname>
							ArrayList<PositionTuple> pts=new ArrayList<PositionTuple>();
							for(String devicename : n.devices.keySet())
							{
								PositionTuple pt=new PositionTuple(devicename,"randomport");
								pts.add(pt);
							}
							//all of the source port
					        rg.portpairlist= new ArrayList<Portpair>();
							int rep = 100;
							for(PositionTuple pt1 : pts)//bianli every port in the keyset
							{
								System.out.println(pt1);
								rg.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
					
									rg.Traverse();
								//	rg.startstate.printLoop();
							}
							long t9 = System.nanoTime();
							rg.computePortpair(aclfortree);
							long t10 = System.nanoTime();
						//	System.out.println(" testtakes" + (t10 - t9) + "s");
							rg.computeSetcover(n.univerhashmap,aclfortree);
							
							///////////////////////////////////////////////////////////////////////////vlan////
							HashSet<ReachabilityGraph> rgvlanset=new HashSet<ReachabilityGraph>();
							
							for(String vlanid: n.univervlan.keySet()){//every vlanid
								//ACLForTree aclfortreevlan=n.aclfortreevlan.get(vlanid);
								ReachabilityGraph rgvlan = new ReachabilityGraph(n);
								rgvlan.portpairlist= new ArrayList<Portpair>();
								for(PositionTuple pt1 : pts)//bianli every port in the keyset//the same pts as above
								{
									if(!n.univervlan.get(vlanid).contains(pt1.getDeviceName())){//if the vlan do not have such device, continue
										continue;
									}
													
									    rgvlan.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
					
										rgvlan.Traversevlan(vlanid);
									//	rgvlan.startstate.printLoop();
								}
							//	aclfortreevlan
								rgvlan.computePortpairvlan(aclfortreevlan.get(vlanid),vlanid);
								rgvlan.computeSetcovervlan(aclfortreevlan.get(vlanid),vlanid);
								
								rgvlanset.add(rgvlan);
							}
							long t2 = System.nanoTime();
							System.out.println(" alltakes" + (t2 - t1)/1000000000 + "s");
							///////////////////perfect update///////////////////////
							try{
							long t3=System.nanoTime();
							n.perfectupdateremove(iii);
					
							rg.portpairlist= new ArrayList<Portpair>();
						//	t4=System.nanoTime();
							for(PositionTuple pt1 : pts)//bianli every port in the keyset
							{
							//	System.out.println(pt1);
								rg.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
					
									rg.Traverse();
									rg.startstate.printLoop();
							}
							rg.computePortpairupdate(aclfortree);
							rg.computeSetcoverupdate(n.univerhashmap,aclfortree);
							long t4 = System.nanoTime();
						//	System.out.println(" addtakes" + (t4 - t3) + "ns");
							 bw2.write(Long.toString((t4-t3)));
							 bw2.newLine();
							System.out.println(" removetakes" + (t4 - t3) + "ns"); 
							}finally{
								continue;}
							
					   }
		}
		
		bw2.close();
		
		if(1==1){
			for(int ii=1;ii<=1000;ii++){   
		        Random random = new Random();
		        int iii = random.nextInt(70000);
						long t1 = System.nanoTime();
						//System.out.println(d.fws);
						Network n = new Network("i2",iii);
						if((n.remaintoupdate.getPortname()==null)){
							break;
						}		
						ReachabilityGraph rg = new ReachabilityGraph(n);////put the net information to this class as net and topology
						ACLForTree aclfortree=n.aclfortree;
						HashMap<String ,ACLForTree> aclfortreevlan=n.aclfortreevlan;
						//Set<PositionTuple> pts = n.getallactiveports();//return topology.keySet(),这个keyset‘s content is <devicename，portname>
						ArrayList<PositionTuple> pts=new ArrayList<PositionTuple>();
						for(String devicename : n.devices.keySet())
						{
							PositionTuple pt=new PositionTuple(devicename,"randomport");
							pts.add(pt);
						}
						//all of the source port
				        rg.portpairlist= new ArrayList<Portpair>();
						int rep = 100;
						for(PositionTuple pt1 : pts)//bianli every port in the keyset
						{
							System.out.println(pt1);
							rg.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
				
								rg.Traverse();
								rg.startstate.printLoop();
						}
						long t9 = System.nanoTime();
						rg.computePortpair(aclfortree);
						long t10 = System.nanoTime();
						System.out.println(" testtakes" + (t10 - t9) + "s");
						rg.computeSetcover(n.univerhashmap,aclfortree);
						///////////////////////////////////////////////////////////////////////////vlan////
						HashSet<ReachabilityGraph> rgvlanset=new HashSet<ReachabilityGraph>();
						
						for(String vlanid: n.univervlan.keySet()){//every vlanid
							//ACLForTree aclfortreevlan=n.aclfortreevlan.get(vlanid);
							ReachabilityGraph rgvlan = new ReachabilityGraph(n);
							rgvlan.portpairlist= new ArrayList<Portpair>();
							for(PositionTuple pt1 : pts)//bianli every port in the keyset//the same pts as above
							{
								if(!n.univervlan.get(vlanid).contains(pt1.getDeviceName())){//if the vlan do not have such device, continue
									continue;
								}
												
								    rgvlan.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
				
									rgvlan.Traversevlan(vlanid);
								//	rgvlan.startstate.printLoop();
							}
						//	aclfortreevlan
							rgvlan.computePortpairvlan(aclfortreevlan.get(vlanid),vlanid);
							rgvlan.computeSetcovervlan(aclfortreevlan.get(vlanid),vlanid);
							
							rgvlanset.add(rgvlan);
						}
						long t2 = System.nanoTime();
						System.out.println(" alltakes" + (t2 - t1)/1000000000 + "s");
						///////////////////perfect update///////////////////////
						try{
							
							//fw 2873102336 24 te2/3.10
							//String devicename="atla";
							//String portname="xe-0/0/1";
							//long ipaddr=69274592L;
							//int prefixlen=27;
							long ipaddr=n.remaintoupdate.getdestip();
							int prefixlen=n.remaintoupdate.getprefixlen();
							String devicename=n.remaintoupdate.getDevicename();
							String portname=n.remaintoupdate.getPortname();
							
							///////update//////////////////////////////////////
						
							
							long t3 = System.nanoTime();
							//fw 2873102336 24 te2/3.10
							int bddreturn=0;
							HashMap<Integer,Integer> rulechangelist=rg.net.addfwdrule(devicename, portname, ipaddr, prefixlen, bddreturn);
							rg.updateaddfwd(rulechangelist,bddreturn);
							long t4 = System.nanoTime();
							System.out.println(" updatetakes" + (t4 - t3) + "ns");
						
							   bw3.write(Long.toString((t4-t3)));
							   bw3.newLine();
						}finally{
							continue;}
			   }
		}			
		
		bw3.close();
		
		if(1==1){
			for(int ii=1;ii<=1000;ii++){   
		        Random random = new Random();
		        int iii = random.nextInt(70000);  
						long t1 = System.nanoTime();
						//System.out.println(d.fws);
						Network n = new Network("i2",10000000);
					//	if((n.remaintoupdate.getPortname()==null)){
					//		break;
					//	}		
						ReachabilityGraph rg = new ReachabilityGraph(n);////put the net information to this class as net and topology
						ACLForTree aclfortree=n.aclfortree;
						HashMap<String ,ACLForTree> aclfortreevlan=n.aclfortreevlan;
						//Set<PositionTuple> pts = n.getallactiveports();//return topology.keySet(),这个keyset‘s content is <devicename，portname>
						ArrayList<PositionTuple> pts=new ArrayList<PositionTuple>();
						for(String devicename : n.devices.keySet())
						{
							PositionTuple pt=new PositionTuple(devicename,"randomport");
							pts.add(pt);
						}
						//all of the source port
				        rg.portpairlist= new ArrayList<Portpair>();
						int rep = 100;
						for(PositionTuple pt1 : pts)//bianli every port in the keyset
						{
							System.out.println(pt1);
							rg.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
				
								rg.Traverse();
								rg.startstate.printLoop();
						}
						long t9 = System.nanoTime();
						rg.computePortpair(aclfortree);
						long t10 = System.nanoTime();
						rg.computeSetcover(n.univerhashmap,aclfortree);
						///////////////////////////////////////////////////////////////////////////vlan////
						HashSet<ReachabilityGraph> rgvlanset=new HashSet<ReachabilityGraph>();
						
						for(String vlanid: n.univervlan.keySet()){//every vlanid
							//ACLForTree aclfortreevlan=n.aclfortreevlan.get(vlanid);
							ReachabilityGraph rgvlan = new ReachabilityGraph(n);
							rgvlan.portpairlist= new ArrayList<Portpair>();
							for(PositionTuple pt1 : pts)//bianli every port in the keyset//the same pts as above
							{
								if(!n.univervlan.get(vlanid).contains(pt1.getDeviceName())){//if the vlan do not have such device, continue
									continue;
								}
												
								    rgvlan.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
				
									rgvlan.Traversevlan(vlanid);
								//	rgvlan.startstate.printLoop();
							}
						//	aclfortreevlan
							rgvlan.computePortpairvlan(aclfortreevlan.get(vlanid),vlanid);
							rgvlan.computeSetcovervlan(aclfortreevlan.get(vlanid),vlanid);
							
							rgvlanset.add(rgvlan);
						}
						long t2 = System.nanoTime();
						System.out.println(" alltakes" + (t2 - t1)/1000000000 + "s");
						///////////////////perfect update///////////////////////
			
							
							//fw 2873102336 24 te2/3.10
							//String devicename="atla";
							//String portname="xe-0/0/1";
							//long ipaddr=69274592L;
							//int prefixlen=27;
						//	long ipaddr=n.remaintoupdate.getdestip();
						//	int prefixlen=n.remaintoupdate.getprefixlen();
						//	String devicename=n.remaintoupdate.getDevicename();
						//	String portname=n.remaintoupdate.getPortname();
							
							///////update//////////////////////////////////////
						
						try{
							long t3 = System.nanoTime();
							//fw 2873102336 24 te2/3.10
							int bddreturn=0;
						//	HashMap<Integer,Integer> rulechangelist=rg.net.addfwdrule(devicename, portname, ipaddr, prefixlen, bddreturn);
							rg.updateremovefwd(iii,0);
							///rg.updateaddfwd(rulechangelist,bddreturn);
							long t4 = System.nanoTime();
							System.out.println(" updatetakes" + (t4 - t3) + "ns");
						
							   bw4.write(Long.toString((t4-t3)));
							   bw4.newLine();
			           }finally{
					          continue;}
			   }
		}			
							
		bw4.close();
		
		
		if(1==1){
			for(int ii=1;ii<=1000;ii++){   
		        Random random = new Random();
		        int iii = random.nextInt(600);
						long t1 = System.nanoTime();
						//System.out.println(d.fws);
						Network n = new Network("st",iii);
						if((n.remaintoupdate.getPortname()==null)){
							break;
						}		
						ReachabilityGraph rg = new ReachabilityGraph(n);////put the net information to this class as net and topology
						ACLForTree aclfortree=n.aclfortree;
						HashMap<String ,ACLForTree> aclfortreevlan=n.aclfortreevlan;
						//Set<PositionTuple> pts = n.getallactiveports();//return topology.keySet(),这个keyset‘s content is <devicename，portname>
						ArrayList<PositionTuple> pts=new ArrayList<PositionTuple>();
						for(String devicename : n.devices.keySet())
						{
							PositionTuple pt=new PositionTuple(devicename,"randomport");
							pts.add(pt);
						}
						//all of the source port
				        rg.portpairlist= new ArrayList<Portpair>();
						int rep = 100;
						for(PositionTuple pt1 : pts)//bianli every port in the keyset
						{
							System.out.println(pt1);
							rg.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
				
								rg.Traverse();
							//	rg.startstate.printLoop();
						}
						long t9 = System.nanoTime();
						rg.computePortpair(aclfortree);
						long t10 = System.nanoTime();
						System.out.println(" testtakes" + (t10 - t9) + "s");
						rg.computeSetcover(n.univerhashmap,aclfortree);
						///////////////////////////////////////////////////////////////////////////vlan////
						HashSet<ReachabilityGraph> rgvlanset=new HashSet<ReachabilityGraph>();
						
						for(String vlanid: n.univervlan.keySet()){//every vlanid
							//ACLForTree aclfortreevlan=n.aclfortreevlan.get(vlanid);
							ReachabilityGraph rgvlan = new ReachabilityGraph(n);
							rgvlan.portpairlist= new ArrayList<Portpair>();
							for(PositionTuple pt1 : pts)//bianli every port in the keyset//the same pts as above
							{
								if(!n.univervlan.get(vlanid).contains(pt1.getDeviceName())){//if the vlan do not have such device, continue
									continue;
								}
												
								    rgvlan.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
				
									rgvlan.Traversevlan(vlanid);
								//	rgvlan.startstate.printLoop();
							}
						//	aclfortreevlan
							rgvlan.computePortpairvlan(aclfortreevlan.get(vlanid),vlanid);
							rgvlan.computeSetcovervlan(aclfortreevlan.get(vlanid),vlanid);
							
							rgvlanset.add(rgvlan);
						}
						long t2 = System.nanoTime();
						System.out.println(" alltakes" + (t2 - t1)/1000000000 + "s");
						///////////////////perfect update///////////////////////
						try{
							long t3;
							long t4;
							t3 = System.nanoTime();
							//fw 2873102336 24 te2/3.10
							//String devicename="atla";
							//String portname="xe-0/0/1";
							//long ipaddr=69274592L;
							//int prefixlen=27;
							long ipaddr=n.remaintoupdate.getdestip();
							int prefixlen=n.remaintoupdate.getprefixlen();
							String devicename=n.remaintoupdate.getDevicename();
							String portname=n.remaintoupdate.getPortname();
							n.perfectupdateadd(devicename, portname, ipaddr, prefixlen);
							///set cover
							
							rg.portpairlist= new ArrayList<Portpair>();
							for(PositionTuple pt1 : pts)//bianli every port in the keyset
							{
								System.out.println(pt1);
								rg.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
					
									rg.Traverse();
					//				rg.startstate.printLoop();
							}
							rg.computePortpairupdate(aclfortree);
							rg.computeSetcoverupdate(n.univerhashmap,aclfortree);
							
							
							t4 = System.nanoTime();
							System.out.println(" addtakes" + (t4 - t3) + "ns");
							 bw5.write(Long.toString((t4-t3)));
							 bw5.newLine();
						}finally{
							continue;}
			   }
		}	
	    bw5.close();
		
		
		
		if(1==1){
			for(int ii=1;ii<=1000;ii++){   
		        Random random = new Random();
		        int iii = random.nextInt(600);
							long t1 = System.nanoTime();
							//System.out.println(d.fws);
							Network n = new Network("st",10000000);
							//if((n.remaintoupdate.getPortname()==null)){
							//	break;
							//}		
							ReachabilityGraph rg = new ReachabilityGraph(n);////put the net information to this class as net and topology
							ACLForTree aclfortree=n.aclfortree;
							HashMap<String ,ACLForTree> aclfortreevlan=n.aclfortreevlan;
							//Set<PositionTuple> pts = n.getallactiveports();//return topology.keySet(),这个keyset‘s content is <devicename，portname>
							ArrayList<PositionTuple> pts=new ArrayList<PositionTuple>();
							for(String devicename : n.devices.keySet())
							{
								PositionTuple pt=new PositionTuple(devicename,"randomport");
								pts.add(pt);
							}
							//all of the source port
					        rg.portpairlist= new ArrayList<Portpair>();
							int rep = 100;
							for(PositionTuple pt1 : pts)//bianli every port in the keyset
							{
								System.out.println(pt1);
								rg.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
					
									rg.Traverse();
								//	rg.startstate.printLoop();
							}
							long t9 = System.nanoTime();
							rg.computePortpair(aclfortree);
							long t10 = System.nanoTime();
						//	System.out.println(" testtakes" + (t10 - t9) + "s");
							rg.computeSetcover(n.univerhashmap,aclfortree);
							
							///////////////////////////////////////////////////////////////////////////vlan////
							HashSet<ReachabilityGraph> rgvlanset=new HashSet<ReachabilityGraph>();
							
							for(String vlanid: n.univervlan.keySet()){//every vlanid
								//ACLForTree aclfortreevlan=n.aclfortreevlan.get(vlanid);
								ReachabilityGraph rgvlan = new ReachabilityGraph(n);
								rgvlan.portpairlist= new ArrayList<Portpair>();
								for(PositionTuple pt1 : pts)//bianli every port in the keyset//the same pts as above
								{
									if(!n.univervlan.get(vlanid).contains(pt1.getDeviceName())){//if the vlan do not have such device, continue
										continue;
									}
													
									    rgvlan.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
					
										rgvlan.Traversevlan(vlanid);
									//	rgvlan.startstate.printLoop();
								}
							//	aclfortreevlan
								rgvlan.computePortpairvlan(aclfortreevlan.get(vlanid),vlanid);
								rgvlan.computeSetcovervlan(aclfortreevlan.get(vlanid),vlanid);
								
								rgvlanset.add(rgvlan);
							}
							long t2 = System.nanoTime();
							System.out.println(" alltakes" + (t2 - t1)/1000000000 + "s");
							///////////////////perfect update///////////////////////
							try{
							long t3=System.nanoTime();
							n.perfectupdateremove(iii);
					
							rg.portpairlist= new ArrayList<Portpair>();
						//	t4=System.nanoTime();
							for(PositionTuple pt1 : pts)//bianli every port in the keyset
							{
							//	System.out.println(pt1);
								rg.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
					
									rg.Traverse();
									rg.startstate.printLoop();
							}
							rg.computePortpairupdate(aclfortree);
							rg.computeSetcoverupdate(n.univerhashmap,aclfortree);
							long t4 = System.nanoTime();
						//	System.out.println(" addtakes" + (t4 - t3) + "ns");
							 bw6.write(Long.toString((t4-t3)));
							 bw6.newLine();
							System.out.println(" removetakes" + (t4 - t3) + "ns"); 
							}finally{
								continue;}
							
					   }
		}
		bw6.close();
		
		
		if(1==1){
			for(int ii=1;ii<=1000;ii++){   
		        Random random = new Random();
		        int iii = random.nextInt(3000);  
						long t1 = System.nanoTime();
						//System.out.println(d.fws);
						Network n = new Network("i2",iii);
						if((n.remaintoupdate.getPortname()==null)){
							break;
						}		
						ReachabilityGraph rg = new ReachabilityGraph(n);////put the net information to this class as net and topology
						ACLForTree aclfortree=n.aclfortree;
						HashMap<String ,ACLForTree> aclfortreevlan=n.aclfortreevlan;
						//Set<PositionTuple> pts = n.getallactiveports();//return topology.keySet(),这个keyset‘s content is <devicename，portname>
						ArrayList<PositionTuple> pts=new ArrayList<PositionTuple>();
						for(String devicename : n.devices.keySet())
						{
							PositionTuple pt=new PositionTuple(devicename,"randomport");
							pts.add(pt);
						}
						//all of the source port
				        rg.portpairlist= new ArrayList<Portpair>();
						int rep = 100;
						for(PositionTuple pt1 : pts)//bianli every port in the keyset
						{
							System.out.println(pt1);
							rg.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
				
								rg.Traverse();
								rg.startstate.printLoop();
						}
						long t9 = System.nanoTime();
						rg.computePortpair(aclfortree);
						long t10 = System.nanoTime();
						System.out.println(" testtakes" + (t10 - t9) + "s");
						rg.computeSetcover(n.univerhashmap,aclfortree);
						///////////////////////////////////////////////////////////////////////////vlan////
						HashSet<ReachabilityGraph> rgvlanset=new HashSet<ReachabilityGraph>();
						
						for(String vlanid: n.univervlan.keySet()){//every vlanid
							//ACLForTree aclfortreevlan=n.aclfortreevlan.get(vlanid);
							ReachabilityGraph rgvlan = new ReachabilityGraph(n);
							rgvlan.portpairlist= new ArrayList<Portpair>();
							for(PositionTuple pt1 : pts)//bianli every port in the keyset//the same pts as above
							{
								if(!n.univervlan.get(vlanid).contains(pt1.getDeviceName())){//if the vlan do not have such device, continue
									continue;
								}
												
								    rgvlan.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
				
									rgvlan.Traversevlan(vlanid);
								//	rgvlan.startstate.printLoop();
							}
						//	aclfortreevlan
							rgvlan.computePortpairvlan(aclfortreevlan.get(vlanid),vlanid);
							rgvlan.computeSetcovervlan(aclfortreevlan.get(vlanid),vlanid);
							
							rgvlanset.add(rgvlan);
						}
						long t2 = System.nanoTime();
						System.out.println(" alltakes" + (t2 - t1)/1000000000 + "s");
						///////////////////perfect update///////////////////////
						try{
							
							//fw 2873102336 24 te2/3.10
							//String devicename="atla";
							//String portname="xe-0/0/1";
							//long ipaddr=69274592L;
							//int prefixlen=27;
							long ipaddr=n.remaintoupdate.getdestip();
							int prefixlen=n.remaintoupdate.getprefixlen();
							String devicename=n.remaintoupdate.getDevicename();
							String portname=n.remaintoupdate.getPortname();
							
							///////update//////////////////////////////////////
						
							
							long t3 = System.nanoTime();
							//fw 2873102336 24 te2/3.10
							int bddreturn=0;
							HashMap<Integer,Integer> rulechangelist=rg.net.addfwdrule(devicename, portname, ipaddr, prefixlen, bddreturn);
							rg.updateaddfwd(rulechangelist,bddreturn);
							long t4 = System.nanoTime();
							System.out.println(" updatetakes" + (t4 - t3) + "ns");
						
							   bw7.write(Long.toString((t4-t3)));
							   bw7.newLine();
						}finally{
							continue;}
			   }
		}			
		
		bw7.close();
		
		
		
		if(1==1){
			for(int ii=1;ii<=1000;ii++){   
		        Random random = new Random();
		        int iii = random.nextInt(600);
						long t1 = System.nanoTime();
						//System.out.println(d.fws);
						Network n = new Network("st",10000000);
					//	if((n.remaintoupdate.getPortname()==null)){
					//		break;
					//	}		
						ReachabilityGraph rg = new ReachabilityGraph(n);////put the net information to this class as net and topology
						ACLForTree aclfortree=n.aclfortree;
						HashMap<String ,ACLForTree> aclfortreevlan=n.aclfortreevlan;
						//Set<PositionTuple> pts = n.getallactiveports();//return topology.keySet(),这个keyset‘s content is <devicename，portname>
						ArrayList<PositionTuple> pts=new ArrayList<PositionTuple>();
						for(String devicename : n.devices.keySet())
						{
							PositionTuple pt=new PositionTuple(devicename,"randomport");
							pts.add(pt);
						}
						//all of the source port
				        rg.portpairlist= new ArrayList<Portpair>();
						int rep = 100;
						for(PositionTuple pt1 : pts)//bianli every port in the keyset
						{
							System.out.println(pt1);
							rg.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
				
								rg.Traverse();
								rg.startstate.printLoop();
						}
						long t9 = System.nanoTime();
						rg.computePortpair(aclfortree);
						long t10 = System.nanoTime();
						rg.computeSetcover(n.univerhashmap,aclfortree);
						///////////////////////////////////////////////////////////////////////////vlan////
						HashSet<ReachabilityGraph> rgvlanset=new HashSet<ReachabilityGraph>();
						
						for(String vlanid: n.univervlan.keySet()){//every vlanid
							//ACLForTree aclfortreevlan=n.aclfortreevlan.get(vlanid);
							ReachabilityGraph rgvlan = new ReachabilityGraph(n);
							rgvlan.portpairlist= new ArrayList<Portpair>();
							for(PositionTuple pt1 : pts)//bianli every port in the keyset//the same pts as above
							{
								if(!n.univervlan.get(vlanid).contains(pt1.getDeviceName())){//if the vlan do not have such device, continue
									continue;
								}
												
								    rgvlan.setStartstate(pt1);//ÀïÃæÓÐ¸östratstate////every start port//init
				
									rgvlan.Traversevlan(vlanid);
								//	rgvlan.startstate.printLoop();
							}
						//	aclfortreevlan
							rgvlan.computePortpairvlan(aclfortreevlan.get(vlanid),vlanid);
							rgvlan.computeSetcovervlan(aclfortreevlan.get(vlanid),vlanid);
							
							rgvlanset.add(rgvlan);
						}
						long t2 = System.nanoTime();
						System.out.println(" alltakes" + (t2 - t1)/1000000000 + "s");
						///////////////////perfect update///////////////////////
			
							
							//fw 2873102336 24 te2/3.10
							//String devicename="atla";
							//String portname="xe-0/0/1";
							//long ipaddr=69274592L;
							//int prefixlen=27;
						//	long ipaddr=n.remaintoupdate.getdestip();
						//	int prefixlen=n.remaintoupdate.getprefixlen();
						//	String devicename=n.remaintoupdate.getDevicename();
						//	String portname=n.remaintoupdate.getPortname();
							
							///////update//////////////////////////////////////
						
						try{
							long t3 = System.nanoTime();
							//fw 2873102336 24 te2/3.10
							int bddreturn=0;
						//	HashMap<Integer,Integer> rulechangelist=rg.net.addfwdrule(devicename, portname, ipaddr, prefixlen, bddreturn);
							rg.updateremovefwd(iii,0);
							///rg.updateaddfwd(rulechangelist,bddreturn);
							long t4 = System.nanoTime();
							System.out.println(" updatetakes" + (t4 - t3) + "ns");
						
							   bw8.write(Long.toString((t4-t3)));
							   bw8.newLine();
			           }finally{
					          continue;}
			   }
		}			
							
		bw8.close();
		
	}//end main
}


class Ruleoutput{
	private int[] head;
	private int[] tail;
	private ArrayList<Rule> ruleset;
	private ArrayList<PositionTuple> arrivedportlist;
	private int rulesetBDD;
	
	public Ruleoutput(int[] head,int[] tail, ArrayList<Rule> ruleset){
		this.head=head;
		this.tail=tail;
		this.ruleset=ruleset;
	}
	
	public int[] getHead() {
		return head;
	}
	public void setHead(int[] head) {
		this.head = head;
	}
	public int[] getTail() {
		return tail;
	}
	public void setTail(int[] tail) {
		this.tail = tail;
	}
	public ArrayList<Rule> getRuleset() {
		return ruleset;
	}
	public void setRuleset(ArrayList<Rule> ruleset) {
		this.ruleset = ruleset;
	}

	public ArrayList<PositionTuple> getArrivedportlist() {
		return arrivedportlist;
	}

	public void setArrivedportlist(ArrayList<PositionTuple> arrivedportlist) {
		this.arrivedportlist = arrivedportlist;
	}

	public int getRulesetBDD() {
		return rulesetBDD;
	}

	public void setRulesetBDD(int rulesetBDD) {
		this.rulesetBDD = rulesetBDD;
	}
	
}

class ComparatorHeader implements Comparator<Ruleoutput> {

	@Override
	public int compare(Ruleoutput rule1, Ruleoutput rule2) {
		// TODO Auto-generated method stub
		int[] head1 = rule1.getHead();
		int[] head2 = rule2.getHead();
		
		// TODO: compare head1 and head2
		// if rule1 is less than rule2, return -1
		// if rule1 is equal to rule2, return 0
		// if rule1 is grater than rule2, return 1
		for(int i=32-1;i>=0;i--){
			if(head1[i]!=head2[i])
			{
				if (head1[i]==1){
					return 1;
				}else{return -1;}
			}			
		}
		return 0;
		
		

	}
	
}

class RuleComparator implements Comparator<RuleOnAxis> {

	@Override
	public int compare(RuleOnAxis ruleonaxis0, RuleOnAxis ruleonaxis1) {
		// TODO Auto-generated method stub
		int[] space1 = ruleonaxis0.getSpace();
		int[] space2 = ruleonaxis1.getSpace();
		
		// TODO: compare head1 and head2
		// if rule1 is less than rule2, return -1
		// if rule1 is equal to rule2, return 0
		// if rule1 is grater than rule2, return 1
		for(int i=32-1;i>=0;i--){
			if(space1[i]!=space2[i])
			{
				if (space1[i]==1){
					return 1;
				}else{return -1;}
			}			
		}
		return 0;
		
		

	}
		
	
}

class RefSetComparator implements Comparator<RefSet> {

	@Override
	public int compare(RefSet refset0, RefSet refset1) {
		// TODO Auto-generated method stub
		int size0=refset0.getAclrulelist().size();
		int size1=refset1.getAclrulelist().size();
		// TODO: compare head1 and head2
		// if rule1 is less than rule2, return -1
		// if rule1 is equal to rule2, return 0
		// if rule1 is grater than rule2, return 1
		if(size0>size1){
			return 1;
		}else if(size0<size1){
			return -1;
		}else{
			return 0;
		}
		
		}
}

