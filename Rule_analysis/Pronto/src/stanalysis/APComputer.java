package stanalysis;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import jdd.bdd.BDD;
import StaticReachabilityAnalysis.*;
import i2analysis.AP;
import i2analysis.Device;
import i2analysis.Predicate;
import i2analysis.Rule;
import i2analysis.SubAP;
import i2analysis.VlanPredicate;

public class APComputer {
	
	// predicateBDD -> count
	HashMap<Integer, Integer> PredicateBDD;//<BDD,出现次数>
	HashMap<String,HashMap<Integer, Integer>> PredicateBDDvlan=new HashMap<String,HashMap<Integer, Integer>>();//<Vlan,<pre bdd,integer 1>>
	// aclbdd -> {apbdd1, apbdd2, ...}
	HashMap<Integer, HashSet<Integer>> PredicateREP;//<predicate's BDD,APset>
	
	HashMap<String, HashMap<Integer, HashSet<Integer>>> PredicateREPvlan=new HashMap<String, HashMap<Integer, HashSet<Integer>>>(); //<vlanid,<predicate's BDD,APset>>
	HashSet<Integer> APold; //yu zhao 杨宏坤的ap
	//HashSet<AP> APset;
	ArrayList<AP> APset;
	HashMap<String,ArrayList<AP>> APmapvlan=new HashMap<String,ArrayList<AP>>();//<Vlanid,apset>
	// for each ap, store the set of acls whose expression has this ap, use the index in ACLAPREP to identify the acl
	// ap -> {aclbdd1, aclbdd2, ...}
	HashMap<Integer, HashSet<Integer>> APREF;
    Integer apname=0;
    HashMap<Integer,ArrayList<Integer>> APchanged=new HashMap<Integer,ArrayList<Integer>>();
	static BDDACLWrapper bddengine;
	
	//static int updateSubAPCount=0;
	
	
	
	
	
	public Set<Integer> getPredicates()
	{
		return PredicateBDD.keySet();
	}
	
	public HashMap<Integer, ArrayList<Integer>> getAPchanged() {
		return APchanged;
	}

	public void setAPchanged(HashMap<Integer, ArrayList<Integer>> aPchanged) {
		APchanged = aPchanged;
	}

	public ArrayList<AP> getAPset() {
		return APset;
	}

	public void setAPset(ArrayList<AP> aPset) {
		APset = aPset;
	}

	/**
	 * 
	 * @param p
	 * @return a set of atomic predicates that cover the predicate p
	 */
	public HashSet<Integer> find_cover(int p)
	{
		HashSet<Integer> new_set = new HashSet<Integer>();
		BDD theBDD = bddengine.getBDD();
		for (int ap : APold)
		{
			if(theBDD.and(p, ap) != BDDACLWrapper.BDDFalse)
			{
				new_set.add(ap);
			}
		}
		return new_set;
	}


	public void AddPredicateOnly(int pbdd)
	{
		if(PredicateBDD.containsKey(pbdd))
		{
			PredicateBDD.put(pbdd, PredicateBDD.get(pbdd) + 1);
		}else
		{
			PredicateBDD.put(pbdd, 1);
		}
	}

//	public APComputer(ArrayList<Integer> PredicateBDDREP, BDDACLWrapper bddengine)
//	{

//		APComputer.bddengine = bddengine;
//		ComputeAP(PredicateBDDREP);
//	}Yu Zhao
	
	public APComputer(ArrayList<Predicate> PredicateBDDREP,HashMap<String, ArrayList<VlanPredicate>> vlanmap, BDDACLWrapper bddengine)
	{

		APComputer.bddengine = bddengine;
		ComputeAP(PredicateBDDREP);
		System.out.println("apsize:"+APset.size());
		for(String vlanid: vlanmap.keySet()){
			//HashMap<String,ArrayList<AP>> APmapvlan=new HashMap<String,ArrayList<AP>>();	
			ArrayList<AP> alist=new ArrayList<AP>();
			APmapvlan.put(vlanid, alist);
			ComputeAPvlan(vlanmap.get(vlanid),vlanid);
		/*	if(vlanid.equals("vlan249")){
				System.out.println("tesxt");
			}
			*/
		}
	}
	
	
	

	private void iniPredicateBDD(ArrayList<Predicate> PredicateBDDList)
	{
		PredicateBDD = new HashMap<Integer, Integer> ();//BDD计数<BDD名字，出现了多少次>
		for(Predicate preidcate : PredicateBDDList)
		{
			PredicateBDD.put(preidcate.getFwbdds(),1);
			//AddPredicateOnly(abdd);
		}
	}
	
	private void iniPredicateBDDvlan(ArrayList<VlanPredicate> PredicateBDDList, String Vlanid)
	{
		HashMap<Integer, Integer> PredicateBDDv = new HashMap<Integer, Integer> ();//BDD计数<BDD名字，出现了多少次>
		for(VlanPredicate preidcate : PredicateBDDList)
		{
			PredicateBDDv.put(preidcate.getFwbdds(),1);
			//AddPredicateOnly(abdd);
		}
		PredicateBDDvlan.put(Vlanid,PredicateBDDv);
	}
	

	public APComputer(BDDACLWrapper bddengine)
	{
		APComputer.bddengine = bddengine;
	}

	public void ComputeAP(ArrayList<Predicate> PredicateBDDREP)
	{
		iniPredicateBDD(PredicateBDDREP);
		int[] index = new int[PredicateBDDREP.size()];
		for(int i = 0; i < index.length; i ++)//做出一个index来
	//	for(int i=index.length-1;i>0;i--)
		{
			index[i] = i;
		}
		//Sample.GetSample(index.length, index.length, index);
		CalAPs(PredicateBDDREP, index);
		CalAPExp();

			
		}
	
	public void ComputeAPvlan(ArrayList<VlanPredicate> PredicateBDDREP,String vlanid)
	{
		iniPredicateBDDvlan(PredicateBDDREP,vlanid);
		int[] index = new int[PredicateBDDREP.size()];
		for(int i = 0; i < index.length; i ++)//做出一个index来
	//	for(int i=index.length-1;i>0;i--)
		{
			index[i] = i;
		}
		//Sample.GetSample(index.length, index.length, index);
		CalAPsvlan(PredicateBDDREP, index,vlanid);////
		CalAPExpvlan(vlanid);

			
		}
	
		//CalAPREF();//in order to test
/*	public void ComputeAP(ArrayList<Integer> PredicateBDDREP)
	{
		iniPredicateBDD(PredicateBDDREP);
		int[] index = new int[PredicateBDDREP.size()];
		for(int i = 0; i < index.length; i ++)//做出一个index来
		{
			index[i] = i;
		}
		//Sample.GetSample(index.length, index.length, index);
		CalAPs(PredicateBDDREP, index);
		CalAPExp();
		CalAPREF();
	}yu zhao*/

	static public BDDACLWrapper getBDDEngine()
	{
		return bddengine;
	}


	/**
	 * get number of aps
	 */
	public long getAPNum()
	{
		return APold.size();
	}
/*
	public List<SubAP> generateSubAPList(Set<Entry<String,Integer>> predicateRulesSet) {
		List<SubAP> listSubAP=new ArrayList<SubAP>();
				
		// get the rules from predicate
		Iterator<Entry<String,Integer>> iter = predicateRulesSet.iterator();
		
		// loop through the rules in predicate
		while (iter.hasNext()) {
	        Entry<String,Integer> entry = iter.next();
			String rulename = entry.getKey();//拿到predicate的一个rulename
			Integer fwbd = entry.getValue();//拿到predicate的一个rule的BDD

			SubAP subap=new SubAP();//生成一个新的subap对象
			subap.setSubapbdds(fwbd.intValue());//把fwbd放入subap的BDD
			
			// add rule from predicate to SubAP rules
			HashMap<String,Boolean> rule_subap=new HashMap<String,Boolean>();//subap的rule，HashMap<String,Boolean>
			rule_subap.put(rulename, true);//subap中的rule，要加入<rulename,ture>
			subap.setRule(rule_subap);//把
			
			listSubAP.add(subap);
	    }
		
		return listSubAP;
	}

	public void updateSubAPList(Set<Entry<String,Integer>> predicateRulesSet, AP ap, int tmps, int iscut)
	{   
		BDD thebdd = bddengine.getBDD();
	//	List<SubAP> subAPsetcut=new ArrayList<SubAP>();
		List<SubAP> oldsubAPset=ap.getSubAPset();
		//for (int i=0;i<subAPset.size();i++)//原ap中的subap，由于切了，需要减少
	//	{
		List<SubAP> subAPset=new ArrayList<SubAP>();
		
		if(iscut!=3){
		Iterator<SubAP> iter1 = oldsubAPset.iterator();	//原ap中的subap，由于切了，需要减少
		while(iter1.hasNext()){
			SubAP subap=iter1.next();
			int newsubap=thebdd.and(subap.getSubapbdds(),tmps);
			thebdd.ref(newsubap);
			thebdd.deref(subap.getSubapbdds());//在此过程中，释放subap的bdd，因为已经没有用了
			if(newsubap!=BDDACLWrapper.BDDFalse)
			{
				//在此过程中subap会发生变化，但是后面用的都是subAPset，所以没关系
				subap.setSubapbdds(newsubap);//非空的话就改一下		
				subAPset.add(subap);
			}else{
				//不非空的话就删掉
			}
		}
		}else{subAPset=oldsubAPset;}
		
		if(iscut==0){ap.setSubAPset(subAPset); return;}
		//oldsubAPset=subAPset;
			//	new HashSet<AP>();
		Iterator<Entry<String,Integer>> iter = predicateRulesSet.iterator();
		
		while (iter.hasNext())
		{
			Entry<String,Integer> entry = iter.next();
			String rulename = entry.getKey();//拿到predicate的一个rulename
			Integer fwbd = entry.getValue();//拿到predicate的一个rule的BDD		
			int fwbdcut=thebdd.and(fwbd, tmps);
			if(fwbdcut!=BDDACLWrapper.BDDFalse){
			subAPset=computeSubAP(fwbdcut,subAPset,rulename);}
		}
		ap.setSubAPset(subAPset);
	}
	
//	private int computerSubApCallCount = 0;
	public List<SubAP> computeSubAP(Integer fwbd, List<SubAP> subAPset, String rulename) {
//		computerSubApCallCount++;
		//for test
	//	updateSubAPCount++;
		BDD thebdd = bddengine.getBDD();
		List<SubAP> oldsubAPset=subAPset;//为了防止迭代的时候，list发生变化
		List<SubAP> newsubAPset=new ArrayList<SubAP>();
		if(!oldsubAPset.isEmpty()){
		   Iterator<SubAP> iter1 = oldsubAPset.iterator();
	//	int count =0;
		   while(iter1.hasNext()){
			//try {
				SubAP subap=iter1.next();
				//System.out.println("fwbd="+fwbd+" , subap.getSubapbdds()="+subap.getSubapbdds());
			    int subapbdd=thebdd.and(fwbd,subap.getSubapbdds());
			    thebdd.ref(subapbdd);
			//thebdd.ref(subfwbd);
			    int subapbddneg=thebdd.and(thebdd.not(fwbd), subap.getSubapbdds());
			    thebdd.ref(subapbddneg);
			    if ((subapbdd!=BDDACLWrapper.BDDFalse)&(subapbddneg!=BDDACLWrapper.BDDFalse))
			    {
					SubAP subap1 = new SubAP(subap);
					SubAP subap2 = new SubAP(subap);
					subap1.setSubapbdds(subapbdd);
					subap1.getRule().put(rulename, true);
					subap2.setSubapbdds(subapbddneg);
					//	subap2.getRule().put(rulename, true);
					newsubAPset.add(subap1);
					newsubAPset.add(subap2);
			    }else if((subapbdd!=BDDACLWrapper.BDDFalse)&(subapbddneg==BDDACLWrapper.BDDFalse))
			    {
				    SubAP subap1 = new SubAP(subap);	
				    subap1.setSubapbdds(subapbdd);
				    newsubAPset.add(subap1);
			    }else if((subapbdd==BDDACLWrapper.BDDFalse)&(subapbddneg!=BDDACLWrapper.BDDFalse))
			    {
			    	newsubAPset.add(subap);
			    }
			    
			    int [] toDeRef = new int[oldsubAPset.size() + 1];
				int cntr = 0;
				for(SubAP sub : oldsubAPset)//把以前的ap都存在toDeRef里面
				{
					toDeRef[cntr] = sub.getSubapbdds();
					cntr ++;
				}
			//	toDeRef[oldList.size()] = bddToAddNeg;//把最后一个predict的bdd的非删掉。
				bddengine.DerefInBatch(toDeRef);//通通删掉？这里有问题会不会把新生成的也删掉？
//			} catch(Exception e) {
//				e.printStackTrace();
//			}
		  }

		}else{
			
			SubAP subap1= new SubAP();
			subap1.setSubapbdds(fwbd);
			HashMap<String,Boolean> rule1=new HashMap<String,Boolean>();
			subap1.setRule(rule1);
			subap1.getRule().put(rulename, true);
			newsubAPset.add(subap1);	
			
			int fwbdneg=thebdd.not(fwbd);
			thebdd.ref(fwbdneg);
			SubAP subap2 = new SubAP();
			subap2.setSubapbdds(fwbdneg);
			HashMap<String,Boolean> rule2=new HashMap<String,Boolean>();
			subap2.setRule(rule2);
			newsubAPset.add(subap2);}

		return newsubAPset;
	 }
	
	/**
	 * add one acl and recompute aps 
	 * @param ind - the bddnode for the ind-th Predicate in PredicateBDDREP
	 */
	/*
	private void AddOnePredicateForAP(Predicate predicatetoadd)
	{

		BDD thebdd = bddengine.getBDD();

		int pbdd=predicatetoadd.getFwbdds();
		int bddToAddNeg = thebdd.not(pbdd);//equation 2 求逆
		thebdd.ref(bddToAddNeg);

		if(APset.size() == 0)
		{
			// initialize...
			if(predicatetoadd.getFwbdds() != BDDACLWrapper.BDDFalse)
			{
				thebdd.ref(pbdd);
				// List of AP portname, to be stored in AP
				List<String> listPortname = new ArrayList<String>();
				// AP portname
				String portname = predicatetoadd.getPortname();
				// add the portname into AP
				listPortname.add(portname);
				// AP fwbdds
				//int pbdds = predicatetoadd.getFwbdds();
				// List of sub AP, to be stored in AP
				// generate SubAP list
				List<SubAP> listSubAP = generateSubAPList(predicatetoadd.getRule().entrySet());
				// create the AP
				AP ap = new AP(pbdd, listSubAP, listPortname);
				APset.add(ap);
			//	List<SubAP> listSubAPnull =new ArrayList<SubAP>();
			//	List<String> listPortnamenull = new ArrayList<String>();
			//	AP apneg = new AP(bddToAddNeg, listSubAPnull,listPortnamenull);
			//	APset.add(apneg);
			}
			if(bddToAddNeg != BDDACLWrapper.BDDFalse)
			{	
			    List<SubAP> listSubAPnull =new ArrayList<SubAP>();
			    List<String> listPortnamenull = new ArrayList<String>();
			    AP apneg = new AP(bddToAddNeg, listSubAPnull,listPortnamenull);
			    APset.add(apneg);
			}
		}else
		{    
			// old list
			//List<AP> oldList = cloneList(APset);jsting
		    
		    //ArrayList newArrayList = (ArrayList) oldArrayList.clone();
			List<AP> oldList=new ArrayList<AP>();
			oldList=APset;
		    
			//HashSet<AP> oldList=APset;
			//HashSet<Integer> oldList = APold;
			// set up a new list
			APset = new ArrayList<AP>();
			//APset=new HashSet<AP>();
			//APold = new HashSet<Integer>();
			Iterator<AP> iterold = oldList.iterator();//这是java里面的一个迭代器，主要用来取集合容器里面的值
			//Iterator<Integer> iterold = oldList.iterator();//这是java里面的一个迭代器，主要用来取集合容器里面的值
			while(iterold.hasNext())//开始做笛卡尔积
			{
				AP oldap = iterold.next();
                //int pbdd=predicatetoadd.getFwbdds();
				int tmps=thebdd.and(pbdd, oldap.getApbdds());//新来的predicate和旧的ap逐条做与
				int negtmps=thebdd.and(bddToAddNeg, oldap.getApbdds());//新来的predicate的逆与旧的ap做与
									
				//int tmps = thebdd.and(predicatetoadd.getFwbdds(), oldap);//新来的predicate和旧的ap逐条做与
				thebdd.ref(tmps);
				thebdd.ref(negtmps);
				if((tmps != BDDACLWrapper.BDDFalse)&&((negtmps != BDDACLWrapper.BDDFalse)))//把ap切成了2半
				{
					//two new ap are built by the oldap
					
					AP ap1=new AP(oldap);
					AP ap2=new AP(oldap);
					//AP ap1=new AP();
				//	ap1=oldap;
					//AP ap2=new AP();
				//	ap2=oldap;
					//ap1 is the cover part of the old ap, apbdd is the bdd of ap1
					//int ap1bdd=tmps;
					String portname = predicatetoadd.getPortname();
					// add the portname into AP1
					ap1.getPortname().add(portname);
					ap1.setApbdds(tmps);
					// compute the subap
					updateSubAPList(predicatetoadd.getRule().entrySet(),ap1,tmps,1);
					APset.add(ap1);				
					ap2.setApbdds(negtmps);
	    			updateSubAPList(predicatetoadd.getRule().entrySet(),ap2,negtmps,0);
					APset.add(ap2);
					
					
				} else if((tmps != BDDACLWrapper.BDDFalse)&((negtmps == BDDACLWrapper.BDDFalse)))//覆盖了原ap
				{
					
					updateSubAPList(predicatetoadd.getRule().entrySet(),oldap,tmps,2);
					APset.add(oldap);
				} else if((tmps == BDDACLWrapper.BDDFalse)&((negtmps != BDDACLWrapper.BDDFalse)))//与原ap无关
				{
					APset.add(oldap);
				}	
			}
			
			/**
			 * in this case, we need to de-ref useless nodes.
			 * we still keep bddToAdd, since it is the bdd node for an acl
			 * we will de-ref:
			 * bddToAddNeg, the whole list of oldList.*/
			 
		/*	int [] toDeRef = new int[oldList.size() + 1];
			int cntr = 0;
			for(AP ap : oldList)//把以前的ap都存在toDeRef里面
			{
				toDeRef[cntr] = ap.getApbdds();
				cntr ++;
			}
			toDeRef[oldList.size()] = bddToAddNeg;//把最后一个predict的bdd的非删掉。
			bddengine.DerefInBatch(toDeRef);//通通删掉？这里有问题会不会把新生成的也删掉？
		}

	}
	*///yu zhao 2016/4/16
	
	private void AddOnePredicateForAP(Predicate predicatetoadd)
	{

		BDD thebdd = bddengine.getBDD();
		
		int pbdd=predicatetoadd.getFwbdds();
		int bddToAddNeg = thebdd.not(pbdd);//equation 2 求逆
		thebdd.ref(bddToAddNeg);
		if(APset.size()==0)
			
		{
			// initialize...
				if(predicatetoadd.getFwbdds() != BDDACLWrapper.BDDFalse)
				{
					thebdd.ref(pbdd);
					// List of AP portname, to be stored in AP
					ArrayList<Predicate> listpredicate = new ArrayList<Predicate>();
					listpredicate.add(predicatetoadd);
					// AP fwbdds
					//int pbdds = predicatetoadd.getFwbdds();
					// List of sub AP, to be stored in AP
					// create the AP
					AP ap = new AP(pbdd, listpredicate);
					APset.add(ap);
				//	List<SubAP> listSubAPnull =new ArrayList<SubAP>();
				//	List<String> listPortnamenull = new ArrayList<String>();
				//	AP apneg = new AP(bddToAddNeg, listSubAPnull,listPortnamenull);
				//	APset.add(apneg);
				}
				if(bddToAddNeg != BDDACLWrapper.BDDFalse)
				{	
				    //List<SubAP> listSubAPnull =new ArrayList<SubAP>();
				    ArrayList<Predicate> listPredicatenull = new ArrayList<Predicate>();
				    AP apneg = new AP(bddToAddNeg, listPredicatenull);
				    APset.add(apneg);
				}
		}else{
			List<AP> oldList=new ArrayList<AP>();
			oldList=APset;
		    
			//HashSet<AP> oldList=APset;
			//HashSet<Integer> oldList = APold;
			// set up a new list
			APset = new ArrayList<AP>();
			//APset=new HashSet<AP>();
			//APold = new HashSet<Integer>();
			Iterator<AP> iterold = oldList.iterator();//这是java里面的一个迭代器，主要用来取集合容器里面的值
			while(iterold.hasNext())//开始做笛卡尔积
			{
				AP oldap = iterold.next();
		//		ArrayList<Predicate> oldlistpredicate=oldap.getListpredicate();
				int tmps = thebdd.and(predicatetoadd.getFwbdds(), oldap.getApbdds());//新来的predicate和旧的ap逐条做与
				thebdd.ref(tmps);
				
				if(tmps != BDDACLWrapper.BDDFalse)//不等于0的话就加到新的APset里面
				{
					//oldap.getListpredicate().add(predicatetoadd);
					AP ap=new AP(tmps,oldap.getListpredicate());
					ap.getListpredicate().add(predicatetoadd);
					APset.add(ap);
				}
				
				tmps = thebdd.and(bddToAddNeg, oldap.getApbdds());//新来的非predicate和旧的ap逐条做与
				thebdd.ref(tmps);
				if(tmps != BDDACLWrapper.BDDFalse)//不等于0的话就加到新的APset里面
				{
					AP ap=new AP(tmps,oldap.getListpredicate());
					APset.add(ap);
				}
				
			}
			/**
			 * in this case, we need to de-ref useless nodes.
			 * we still keep bddToAdd, since it is the bdd node for an acl
			 * we will de-ref:
			 * bddToAddNeg, the whole list of oldList.
			  */
			int [] toDeRef = new int[oldList.size() + 1];
			int cntr = 0;
			for(AP oldap : oldList)//把以前的ap都存在toDeRef里面
			{
				toDeRef[cntr] = oldap.getApbdds();
				cntr ++;
			}
			toDeRef[oldList.size()] = bddToAddNeg;//把最后一个predict的bdd的非删掉。
			bddengine.DerefInBatch(toDeRef);//通通删掉？这里有问题会不会把新生成的也删掉？
		}
	}
	
	private void AddOnePredicateForAPvlan(VlanPredicate predicatetoadd, String vlanID)
	{

		
		BDD thebdd = bddengine.getBDD();
		
		ArrayList<AP> vlanAPset=APmapvlan.get(vlanID);
		
		//test
		
		int pbdd=predicatetoadd.getFwbdds();
		int bddToAddNeg = thebdd.not(pbdd);//equation 2 求逆
		thebdd.ref(bddToAddNeg);
		if(vlanAPset.size()==0)
		{
			// initialize...
				if(predicatetoadd.getFwbdds() != BDDACLWrapper.BDDFalse)
				{
					thebdd.ref(pbdd);
					// List of AP portname, to be stored in AP
					ArrayList<VlanPredicate> listpredicate = new ArrayList<VlanPredicate>();
					listpredicate.add(predicatetoadd);
					// AP fwbdds
					//int pbdds = predicatetoadd.getFwbdds();
					// List of sub AP, to be stored in AP
					// create the AP
					AP ap = new AP(pbdd, listpredicate,0);//zero is the different with AP(,)
					vlanAPset.add(ap);
				//	List<SubAP> listSubAPnull =new ArrayList<SubAP>();
				//	List<String> listPortnamenull = new ArrayList<String>();
				//	AP apneg = new AP(bddToAddNeg, listSubAPnull,listPortnamenull);
				//	APset.add(apneg);
				}
				if(bddToAddNeg != BDDACLWrapper.BDDFalse)
				{	
				    //List<SubAP> listSubAPnull =new ArrayList<SubAP>();
				    ArrayList<VlanPredicate> listPredicatenull = new ArrayList<VlanPredicate>();
				    AP apneg = new AP(bddToAddNeg, listPredicatenull,0);
				    vlanAPset.add(apneg);
				}
		}else{
			List<AP> oldList=new ArrayList<AP>();
			oldList=vlanAPset;//ArrayList<AP>
		    
			//HashSet<AP> oldList=APset;
			//HashSet<Integer> oldList = APold;
			// set up a new list
			vlanAPset = new ArrayList<AP>();//set to empty
			//APset=new HashSet<AP>();
			//APold = new HashSet<Integer>();
			Iterator<AP> iterold = oldList.iterator();//这是java里面的一个迭代器，主要用来取集合容器里面的值
			while(iterold.hasNext())//开始做笛卡尔积
			{
				AP oldap = iterold.next();
		//		ArrayList<Predicate> oldlistpredicate=oldap.getListpredicate();
				int tmps = thebdd.and(predicatetoadd.getFwbdds(), oldap.getApbdds());//新来的predicate和旧的ap逐条做与
				thebdd.ref(tmps);
				
				if(tmps != BDDACLWrapper.BDDFalse)//不等于0的话就加到新的APset里面
				{
					//oldap.getListpredicate().add(predicatetoadd);
					AP ap=new AP(tmps,oldap.getVlanpredicatelist(),0);
					ap.getVlanpredicatelist().add(predicatetoadd);
					vlanAPset.add(ap);
				}
				
				tmps = thebdd.and(bddToAddNeg, oldap.getApbdds());//新来的非predicate和旧的ap逐条做与
				thebdd.ref(tmps);
				if(tmps != BDDACLWrapper.BDDFalse)//不等于0的话就加到新的APset里面
				{
					AP ap=new AP(tmps,oldap.getVlanpredicatelist(),0);
					vlanAPset.add(ap);
				}
				
			}
			/**
			 * in this case, we need to de-ref useless nodes.
			 * we still keep bddToAdd, since it is the bdd node for an acl
			 * we will de-ref:
			 * bddToAddNeg, the whole list of oldList.
			  */
			APmapvlan.put(vlanID,vlanAPset);
			int [] toDeRef = new int[oldList.size() + 1];
			int cntr = 0;
			for(AP oldap : oldList)//把以前的ap都存在toDeRef里面
			{
				toDeRef[cntr] = oldap.getApbdds();
				cntr ++;
			}
			toDeRef[oldList.size()] = bddToAddNeg;//把最后一个predict的bdd的非删掉。
			bddengine.DerefInBatch(toDeRef);//通通删掉？这里有问题会不会把新生成的也删掉？
		}

	}
	
	
	/*
	private void AddOnePredicateForAP(int bddToAdd)
	{

		BDD thebdd = bddengine.getBDD();

		int bddToAddNeg = thebdd.not(bddToAdd);//equation 2 求逆
		thebdd.ref(bddToAddNeg);

		if(AP.size() == 0)
		{
			// initialize...
			if(bddToAdd != BDDACLWrapper.BDDFalse)
			{
				thebdd.ref(bddToAdd);
				AP.add(bddToAdd);
			}
			if(bddToAddNeg != BDDACLWrapper.BDDFalse)
			{
				AP.add(bddToAddNeg);
			}
		}else
		{
			// old list
			HashSet<Integer> oldList = AP;
			// set up a new list
			AP = new HashSet<Integer>();
			Iterator<Integer> iterold = oldList.iterator();//这是java里面的一个迭代器，主要用来取集合容器里面的值
			while(iterold.hasNext())//开始做笛卡尔积
			{
				int oldap = iterold.next();

				int tmps = thebdd.and(bddToAdd, oldap);//新来的predicate和旧的ap逐条做与
				thebdd.ref(tmps);
				if(tmps != BDDACLWrapper.BDDFalse)//不等于0的话就加到新的APset里面
				{
					AP.add(tmps);
				}

				tmps = thebdd.and(bddToAddNeg, oldap);//新来的非predicate和旧的ap逐条做与
				thebdd.ref(tmps);
				if(tmps != BDDACLWrapper.BDDFalse)//不等于0的话就加到新的APset里面
				{
					AP.add(tmps);
				}
			}
			
			/**
			 * in this case, we need to de-ref useless nodes.
			 * we still keep bddToAdd, since it is the bdd node for an acl
			 * we will de-ref:
			 * bddToAddNeg, the whole list of oldList.
			  
			int [] toDeRef = new int[oldList.size() + 1];
			int cntr = 0;
			for(int oldbdd : oldList)//把以前的ap都存在toDeRef里面
			{
				toDeRef[cntr] = oldbdd;
				cntr ++;
			}
			toDeRef[oldList.size()] = bddToAddNeg;//把最后一个predict的bdd的非删掉。
			bddengine.DerefInBatch(toDeRef);//通通删掉？这里有问题会不会把新生成的也删掉？
		}

	}
   
			*///yu zhao 2016.4.16
			


	/**
	 * Adapt from AtomicPredicate.CalAPs
	 * @param index - the order of combining ACLs
	 * to compute ap, need to specify index and predicatebddrep
	 */
	public void CalAPs(ArrayList<Predicate> PredicateBDDList, int[] index)
	{
		
	//	BDD thebdd = bddengine.getBDD();
		//thebdd.and(6668, 7109);
	//	HashSet<Integer> done = new HashSet<Integer> ();
		//APold = new HashSet<Integer>();//构造AP
		//APset = new HashSet<AP>();
		APset = new ArrayList<AP>();
		if(index.length != PredicateBDDList.size())
		{
			System.err.println("The size of indexes does not match" +
					"the number of ACLs!");
			return;
		}
		long start = System.nanoTime();
		//for(int i = 0; i < index.length; i ++)//挨个生成AP
		for(int i = 0;i<index.length;i++)
		//for(int i=index.length-1;i>0;i--)
		{   
			//count
			//System.out.println("APID:"+i+"APset number"+APset.size());
			Predicate predicatetoadd = PredicateBDDList.get(index[i]);
	//		if(done.contains(bddtoadd))
		//	{
			//}else
			//{
			AddOnePredicateForAP(predicatetoadd);
			
		//	done.add(bddtoadd);
				//System.out.println(AP.size());
			//}
		}
		
/*
		for (AP ap : APset){
			int aa=0;
			for(Predicate PP :PredicateBDDList){
				if(thebdd.and(PP.getFwbdds(),ap.getApbdds())!=BDDACLWrapper.BDDFalse){
					aa=aa+1;
				}
			}
			System.out.println(aa);
		}
		*/
		    APset=DetermineRule(APset);
		    /*
			for (AP ap : APset){
				int aa=0;
				for(Predicate PP :PredicateBDDList){
					if(thebdd.and(PP.getFwbdds(),ap.getApbdds())!=BDDACLWrapper.BDDFalse){
						aa=aa+1;
					}
				}
				System.out.println(aa);
			}
		    */
		    
		long end = System.nanoTime();
		System.out.println("number of predicates: " + index.length);
		System.out.println("number of AP: " + APset.size());
		System.out.println("takes " + (end - start)/1000000.0 + " ms");
		
	}
	
	public void CalAPsvlan(ArrayList<VlanPredicate> PredicateBDDREP, int[] index, String vlanid)
	{

	//	BDD thebdd = bddengine.getBDD();
		if(index.length != PredicateBDDREP.size())
		{
			System.err.println("The size of indexes does not match" +
					"the number of ACLs!");
			return;
		}
		long start = System.nanoTime();
		for(int i = 0;i<index.length;i++)
		{   
			VlanPredicate predicatetoadd = PredicateBDDREP.get(index[i]);

			AddOnePredicateForAPvlan(predicatetoadd,vlanid);

		}

		
		    ArrayList<AP> vlanAPset=DetermineRulevlan(APmapvlan.get(vlanid));
		    APmapvlan.put(vlanid,vlanAPset);

		long end = System.nanoTime();
		/*
		System.out.println("number of predicates: " + index.length);
		System.out.println("number of AP: " + vlanAPset.size());
		System.out.println("takes vlanap" + (end - start)/1000000.0 + " ms");
		*/
	}
	
	
	public void Test(ArrayList<AP> APset, ArrayList<Predicate> PredicateBDDList){		
		BDD thebdd = bddengine.getBDD();
		
		for(Predicate predicate: PredicateBDDList){
		    for (Rule rule:predicate.getRuleset()){
		    	int result4=thebdd.and(rule.getRuleBDD(),predicate.getFwbdds());
		    	/*
		    	if(rule.getRulename().equals("78")){
		    		System.out.println("bingo5");
		    	}
		    	
		    	*/
		    	if(result4==0){
		    		System.out.println("bingo 4");
		    	}
		    }
			
		}
		
		
		for(AP ap:APset){//for each ap
	         for(Predicate predicate1 :PredicateBDDList){//for each predicate in the whole set
	        	 int i=0;
	        	 for(Predicate predicateap: ap.getListpredicate()){//for ap's all pradicate
	        		 if(predicate1.getFwbdds()==predicateap.getFwbdds()){//is the predicate in the ap
	        			 int result1=thebdd.and(ap.getApbdds(), predicate1.getFwbdds());
	        			 if(result1==0){
	        				 System.out.println("bingo3");//the ap's predicate proble
	        			 }
	        			 for(Rule rule:predicate1.getRuleset()){//for every rule in the predicate in the whole set
	        				 int result=thebdd.and(rule.getRuleBDD(),ap.getApbdds());//rule and with the ap's bdd
	        				      int mm1=0;
	        				      for(Rule ruletest:predicateap.getRuleset()){
	        				    	  if(ruletest.getRulename().equals(rule.getRulename())){mm1=1;break;}
	        				      }
	        				 
	        				 if((result!=0)&(mm1==1)){//if no-null set result and ap has this rule, that is ok	 
	        				 }else if((result==0)&!(mm1==1)){//if null set result and ap do not has this rule, that is ok
	        				 }else{
	        					 System.out.println("bingo1"+result);}//else alarm!!!
	        			 }
	        			 i=1;
	        			 break;
	        		 }//end if(predicate1.g...	 
	        	 }
	        	 if (i==0){//means the predicate is not in the ap
	        		 int result2=thebdd.and(ap.getApbdds(), predicate1.getFwbdds());
	        		 if(result2!=0){
	        			 System.out.println("bingo4");//the ap's predicate proble
	        		 }
	        		 
	        		 for(Rule rule:predicate1.getRuleset())
	        		 {
	        			 int result=thebdd.and(rule.getRuleBDD(),ap.getApbdds());
	        			 if (result!=0){
	        				 System.out.println("bingo2");
	        			 }
	        		 }
	        		 
	        	 }
	         }
	        	 
		} 
	         }
			    	

	
	
	/*public void CalAPs(ArrayList<Integer> PredicateBDDList, int[] index)
	{
		HashSet<Integer> done = new HashSet<Integer> ();
		AP = new HashSet<Integer>();//构造AP
		if(index.length != PredicateBDDList.size())
		{
			System.err.println("The size of indexes does not match" +
					"the number of ACLs!");
			return;
		}
		long start = System.nanoTime();
		for(int i = 0; i < index.length; i ++)//挨个生成AP
		{   
			int bddtoadd = PredicateBDDList.get(index[i]);
			if(done.contains(bddtoadd))
			{

			}else
			{
				AddOnePredicateForAP(bddtoadd);
				done.add(bddtoadd);
				//System.out.println(AP.size());
			}
		}
		long end = System.nanoTime();
		System.out.println("number of predicates: " + index.length);
		System.out.println("number of AP: " + AP.size());
		System.out.println("takes " + (end - start)/1000000.0 + " ms");
	}yu zhao
    */

	public ArrayList<AP> DetermineRule(ArrayList<AP> APset)
	{
		BDD thebdd = bddengine.getBDD();
	//	int kk=0;//test
	//	ArrayList<AP> newAPset=new ArrayList<AP>();
		    for(int i=0;i<APset.size();i++)//for every AP
		    {
		    	
		    	ArrayList<Predicate> newpredicateset=new ArrayList<Predicate>();//build a new preidcateset
		    	HashSet<Integer> perdicatename=APset.get(i).getPredicatename();
		    	ArrayList<Predicate> predicateset=APset.get(i).getListpredicate();//old predicateset
		    	
			    int predicatesize=predicateset.size();
		//	    System.out.println("APID"+i+"predicatenumber"+predicatesize);
			    
			    
		    	for(int j=0;j<predicatesize;j++){
		    	    Predicate predicate=predicateset.get(j);//select a old predicate from the old set
		    	   ArrayList<Rule> newruleset=new ArrayList<Rule>();//build a new rule set
		    	   ArrayList<Rule> ruleset= predicate.getRuleset();   //get old predicate's ruleset
		    	   int rulesize=ruleset.size();
		    	       for(int k=0; k<rulesize;k++){
		    	    	   Rule rule=ruleset.get(k); 	    	   
		    	    	   int ruleBDD=rule.getRuleBDD();
		    	    	   int newrulebdd=thebdd.and(APset.get(i).getApbdds(), ruleBDD);//rule's bdd join with ap's bdd
		    	    	  // thebdd.ref(newrulebdd);
		    	    	   if (newrulebdd !=BDDACLWrapper.BDDFalse){
		    	    		 //  thebdd.deref(newrulebdd);//in order to consider the JDD
		    	    		   thebdd.ref(newrulebdd);
		    	    		   Rule newrule=new Rule(rule.getPortname(),rule.getDevicename(),rule.getHead(),rule.getTail(),ruleBDD, rule.getRulename());
		    	    		   newruleset.add(newrule);	   //build a new rule's rule set
		    	    	   }  
		    	       }
		    	    Predicate newpreidcate=new Predicate(predicate.getPortname(), predicate.getDevicename(),predicate.getFwbdds(),newruleset);
		    	    newpreidcate.setPredicatename(predicate.getPredicatename());
		    	    newpredicateset.add(newpreidcate);    //build a new predicate's set
		    	    perdicatename.add(predicate.getPredicatename());
		    	}
		    	APset.get(i).setListpredicate(newpredicateset);
		    	APset.get(i).setApname(apname++);
		    }
		 //   System.out.println("123");//test
		 //   System.out.println("final="+kk);//test
		return APset;
	}
	
	public ArrayList<AP> DetermineRulevlan(ArrayList<AP> vlanAPset)
	{
		BDD thebdd = bddengine.getBDD();
	//	int kk=0;//test
	//	ArrayList<AP> newAPset=new ArrayList<AP>();
		    for(int i=0;i<vlanAPset.size();i++){//
		    	ArrayList<VlanPredicate> newpredicateset=new ArrayList<VlanPredicate>();//build a new preidcateset
		    	ArrayList<VlanPredicate> predicateset=vlanAPset.get(i).getVlanpredicatelist();//old predicateset
		    	int predicatesize=predicateset.size();
		    	for(int j=0;j<predicatesize;j++){
		    		VlanPredicate predicate=predicateset.get(j);//select a old predicate from the old set
		    		HashMap<Integer,ArrayList<Rule>> rulemap= predicate.getRuleset();//
		    		HashMap<Integer,ArrayList<Rule>> newmap=new HashMap<Integer,ArrayList<Rule>>();
		    		HashMap<String,ArrayList<Rule>> portrulelist=new HashMap<String,ArrayList<Rule>>();
		    		for(Integer rulebdd:rulemap.keySet()){
		    			int newrulebdd=thebdd.and(vlanAPset.get(i).getApbdds(), rulebdd);//rule's bdd join with ap's bdd
		    			  if (newrulebdd !=BDDACLWrapper.BDDFalse){
		    				  thebdd.ref(newrulebdd);
		    				  ArrayList<Rule> newrulelist=new ArrayList<Rule>();
		    				  for(Rule rule:rulemap.get(rulebdd)){
		    					  Rule newrule=new Rule(rule.getPortname(),rule.getDevicename(),rule.getHead(),rule.getTail(),newrulebdd, rule.getRulename());
		    				      newrulelist.add(newrule);
		    				      if(portrulelist.containsKey(newrule.getPortname())){
		    				    	  portrulelist.get(newrule.getPortname()).add(newrule);
		    				      }else{
		    				    	  ArrayList<Rule> rlist=new ArrayList<Rule>();
		    				    	  rlist.add(newrule);
		    				    	  portrulelist.put(newrule.getPortname(), rlist);
		    				      }
		    				  }
		    				  newmap.put(newrulebdd, newrulelist);
		    			  }
		    			
		    		}
		    		VlanPredicate newpreidcate=new VlanPredicate(predicate.getPortname(), predicate.getDevicename(),predicate.getFwbdds(),newmap,portrulelist);
		    		newpredicateset.add(newpreidcate);

			    	   
		    	}
		    	vlanAPset.get(i).setVlanpredicatelist(newpredicateset);
		    	}
		    	
		    return vlanAPset;
		
	}
	
	/**
	 * 
	 * @param predicatebdd
	 * @return if the acl is true or force, return the set containing the acl itself;
	 *         otherwise, return an ap expression
	 */
	public HashSet<Integer> getAPExp(int PredicateBDD)
	{
		HashSet<Integer> apexp = new HashSet<Integer> ();

		for(AP oneap: APset)
		{
			if(bddengine.getBDD().and(oneap.getApbdds(), PredicateBDD) != BDDACLWrapper.BDDFalse)
            {
            	apexp.add(oneap.getApbdds());
            }
		}
		return apexp;
	}
	
	public HashSet<Integer> getAPExpvlan(int PredicateBDD,String VlanID)
	{
		HashSet<Integer> apexp = new HashSet<Integer> ();
		/*
		if(VlanID.equals("vlan249")){
			System.out.println("test");
		}
		*/
		//HashMap<String,ArrayList<AP>> APmapvlan
		ArrayList<AP> APsetvlan=APmapvlan.get(VlanID);
		for(AP oneap: APsetvlan)
		{
			if(bddengine.getBDD().and(oneap.getApbdds(), PredicateBDD) != BDDACLWrapper.BDDFalse)
            {
            	apexp.add(oneap.getApbdds());
            }
		}
		return apexp;
	}

	/**
	 * it is already computed
	 * @param PredicateBDD
	 * @return the expression
	 */
	public HashSet<Integer> getAPExpComputed(int PredicateBDD)
	{			
		//return new HashSet<Integer>(PredicateAPREP.get(PredicateBDD));
		return PredicateREP.get(PredicateBDD);	
	}
	
	
    
	public HashSet<Integer> getvlanAPExpComputed(int PredicateBDD, String VlanID)
	{			
		//return new HashSet<Integer>(PredicateAPREP.get(PredicateBDD));
		return PredicateREPvlan.get(VlanID).get(PredicateBDD);	
	}
	/**
	 * calculate ACLAPREP
	 * for true or false, we do not assign the expression
	 */
	public void CalAPExp()
	{
		PredicateREP = new HashMap<Integer, HashSet<Integer>>();//
		for(int abdd : PredicateBDD.keySet())//PredicateBDD:predicate's BDD, 1
		{
			PredicateREP.put(abdd, getAPExp(abdd));//<predicate's BDD,get which AP>
		}
	//	System.out.println("AP expression for each predicate is computed.");
	}

	public void CalAPExpvlan(String vlanID)
	{
		HashMap<Integer, HashSet<Integer>> PredicateREPv = new HashMap<Integer, HashSet<Integer>>();//
		for(int abdd : PredicateBDDvlan.get(vlanID).keySet())//PredicateBDD:predicate's BDD, 1
		{
			PredicateREPv.put(abdd, getAPExpvlan(abdd,vlanID));//<predicate's BDD,get which AP>
		}
		PredicateREPvlan.put(vlanID, PredicateREPv);
	//	System.out.println("AP expression for each predicate is computed.");
	}
	
	/**
	 * calculate APREF
	 * should be called after CalAPExp
	 */

	public void CalAPREF()
	{
		APREF = new HashMap<Integer,HashSet<Integer>> ();//<ap,predicate's BDD set>
		for(AP ap : APset)
		{
			APREF.put(ap.getApbdds(), new HashSet<Integer>());//{163344=[], 163329=[], 162313=[]}
		}
		
		/*
		for(int apbdd : APold)
		{
			APREF.put(apbdd, new HashSet<Integer>());//{163344=[], 163329=[], 162313=[]}
		}
        */
		for(int predicatebdd : PredicateREP.keySet())//key is predicate's all BDD set.PredicateREP;//<BDD,APset>
		{	
			for(int apbdd : PredicateREP.get(predicatebdd))//get a predicate to bianli all the ap in it
			{

				APREF.get(apbdd).add(predicatebdd);//ap find the predicate then add it.
				
			}
		}

		System.out.println("AP Reference is computed.");
        //test
		for (AP ap:APset){
			for(int bdd:APREF.get(ap.getApbdds())){
				int i=0;
			    for (Predicate predicate:ap.getListpredicate())	{
			    	if (predicate.getFwbdds()==bdd)
			    	{ i=1;
			    		break; 		
			    	}  				    
			    }
			    if(i==0){
			    	System.out.println("bingo");}
			}
		}
		
		
		
	}
	
	/**
	 * 
	 * @param predicates
	 * @return true: ap not changed, false: ap changed
	 */
	public boolean update_a(Collection<Integer> predicates)
	{
		boolean nochange = true;
		for(int onep : predicates)
		{
			if(!update_a(onep))
			{
				nochange = false;
			}
		}
		return nochange;
	}
	
	/**
	 * @return - true: ap not changed, false: ap changed
	 * @param predicatebdd
	 */
	public boolean update_a(int predicatebdd)
	{
		if(PredicateBDD.containsKey(predicatebdd))
		{
			// already has the predicate
			AddPredicateOnly(predicatebdd);
			return true;
		}else
		{
			int oldsize = APold.size();
			update_a_i(predicatebdd);
			int newsize = APold.size();
			if(oldsize == newsize)
			{
				return true;
			}else
			{
				return false;
			}
		}
	}

	/**
	 * 
	 * @param predicatebdd - the predicate bdd to add
	 * 1. update ap set, generate change list oldap -> {newap1, newap2}, generate the expression 
	 * for the new predicatebdd
	 * 2. apply change list to predicateaprep
	 * 3. update apref
	 */
	private HashMap<Integer, ArrayList<Integer>> update_a_i(int predicatebdd)
	{
		// update ap set, get change list, get the expression for the new predicate
		HashMap<Integer, ArrayList<Integer>> changelist = AddOnePredicate(predicatebdd);
		// update exp for old predicates
		ApplyChangelistExp(changelist);
		// update ap ref
		ApplyChangelistRef(predicatebdd, changelist);
		
		return changelist;
	}

	/**
	 * add one predicate to the current predicate sets,
	 * change AP, PredicateBDD,get the expression for the new predicate, return change list
	 */
	private HashMap<Integer, ArrayList<Integer>> AddOnePredicate(int bddToAdd)
	{

		HashMap<Integer, ArrayList<Integer>> changelist = new HashMap<Integer, ArrayList<Integer>>();
		BDD thebdd = bddengine.getBDD();
		
		AddPredicateOnly(bddToAdd);

		int bddToAddNeg = thebdd.not(bddToAdd);
		thebdd.ref(bddToAddNeg);

		// old list
		HashSet<Integer> oldList = APold;
		// set up a new list
		APold = new HashSet<Integer>();
		Iterator<Integer> iterold = oldList.iterator();
		HashSet<Integer> newexp = new HashSet<Integer>();
		while(iterold.hasNext())
		{
			int oldap = iterold.next();
			
			int tmps1 = thebdd.and(bddToAdd, oldap);
			if(tmps1 != BDDACLWrapper.BDDFalse)
			{
				thebdd.ref(tmps1);
				APold.add(tmps1);
				newexp.add(tmps1);
			}

			int tmps2 = thebdd.and(bddToAddNeg, oldap);
			if(tmps2 != BDDACLWrapper.BDDFalse)
			{
				thebdd.ref(tmps2);
				APold.add(tmps2);
			}
			
			if(tmps1 != oldap && tmps2 != oldap)
			{
				ArrayList<Integer> newary = new ArrayList<Integer>();
				newary.add(tmps1);
				newary.add(tmps2);
				changelist.put(oldap, newary);
			}
		}
		/**
		 * in this case, we need to de-ref useless nodes.
		 * we still keep bddToAdd, since it is the bdd node for an acl
		 * we will de-ref:
		 * bddToAddNeg, the whole list of oldList.
		 */
		int [] toDeRef = new int[oldList.size() + 1];
		int cntr = 0;
		for(int oldbdd : oldList)
		{
			toDeRef[cntr] = oldbdd;
			cntr ++;
		}
		toDeRef[oldList.size()] = bddToAddNeg;
		bddengine.DerefInBatch(toDeRef);
		
		PredicateREP.put(bddToAdd, newexp);

		return changelist;
	}
	
	private void ApplyChangelistExp(HashMap<Integer, ArrayList<Integer>> changelist)
	{
		for(int oldap : changelist.keySet())
		{
			HashSet<Integer> predicates = APREF.get(oldap);
			for(int onep : predicates)
			{
				PredicateREP.get(onep).remove(oldap);
				ArrayList<Integer> toadd = changelist.get(oldap);
				PredicateREP.get(onep).add(toadd.get(0));
				PredicateREP.get(onep).add(toadd.get(1));
			}
		}
	}
	
	private void ApplyChangelistRef(int bddtoadd, HashMap<Integer, ArrayList<Integer>> changelist)
	{
		for(int oldap : changelist.keySet())
		{
			HashSet<Integer> oldref = APREF.get(oldap);
			ArrayList<Integer> newaps = changelist.get(oldap);
			HashSet<Integer> newref = new HashSet<Integer> (oldref);
			APREF.put(newaps.get(0), newref);
			APREF.put(newaps.get(1), oldref);
			APREF.remove(oldap);
		}
		
		for(int oneap : PredicateREP.get(bddtoadd))
		{
			APREF.get(oneap).add(bddtoadd);
		}
	}
	

	public HashSet<Integer> getAllAP()
	{
		HashSet<Integer> allAP=new HashSet<Integer>();
		for (AP oneap:APset)
		{
			allAP.add(oneap.getApbdds());
		}			
		return allAP;
	}

	public static void main(String[] args) throws FileNotFoundException
	{

	}
	
	// preserve all the list elements
	private List<AP> cloneList(List<AP> ls) {
		List<AP> newLs = new ArrayList<AP>();
		if (ls!=null) {
			for (AP ap : ls) {
				newLs.add(new AP(ap));
			}
		}
		return newLs;
	}

	public HashMap<String, ArrayList<AP>> getAPmapvlan() {
		return APmapvlan;
	}

	public void setAPmapvlan(HashMap<String, ArrayList<AP>> aPmapvlan) {
		APmapvlan = aPmapvlan;
	}

	public void computeUpdate(HashSet<Predicate> changedpredicate, HashMap<String, Device> devices) {
		APchanged=new HashMap<Integer,ArrayList<Integer>>();//<apname,<predicname>>
		
		
	
		
		for(Predicate predicate: changedpredicate){
			for(AP ap: APset){
				if(ap.getListpredicate()!=null){
					if(ap.getPredicatename().contains(predicate.getPredicatename()))
					{
						ap.getPredicatename().remove(predicate.getPredicatename());
						APchanged.put(ap.getApname(),new ArrayList<Integer>());
						HashSet<Predicate> needtoremove=new HashSet<Predicate>();//=new Predicate();
						for(Predicate oldpredicate: ap.getListpredicate()){
							if(oldpredicate.getPredicatename()==predicate.getPredicatename()){
								needtoremove.add(oldpredicate);
							}
						}
						if(needtoremove!=null){
						ap.getListpredicate().removeAll(needtoremove);
					//	System.out.println("bingo");
					    }
					}
				}
			}
		}
		
		ArrayList<AP> APsetnew=new ArrayList<AP>();
		for(int i=0; i<APset.size();i++){
		//	if(i==214){
			//	System.out.println("bingo");
		//	}
			
			int tag=0;
			for(int j=i+1; j<APset.size();j++){
				AP api=APset.get(i);
				AP apj=APset.get(j);
				if(!api.getPredicatename().equals(apj.getPredicatename())){
					tag=1;
				}else{
					APchanged.remove(api.getApname());
				}
			}
			if(i==APset.size()-1||tag==1){
			APsetnew.add(APset.get(i));
			}
		}
		
		APset=APsetnew;
		
	    for(Predicate predicate:changedpredicate){
	    	AddOnePredicateForAPupdate(predicate,APchanged);
	    }
	    /*
		for(AP ap:APset){
		for(Predicate predicate: ap.getListpredicate()){
			if(ap.getApname()==182){
			if(predicate.getPortname().equals("xe-1/0/3")){
				if(predicate.getDevicename().equals("atla")){
				System.out.println("bingo");
				predicate.getRuleset().size();
				}
			}
		}
		}
		}
		*/
	 
	    DetermineRuleUpdate(APset,APchanged);
		CalAPExpupdate(devices);
		
	

		///
		//HashMap<devicename,HashMap<port,AP>>
		//CalAPExp();
		// TODO Auto-generated method stub	
	}
	
	public void computeUpdateremove(HashSet<Predicate> changedpredicate, HashMap<String, Device> devices) {
		
		
		APchanged=new HashMap<Integer,ArrayList<Integer>>();//<apname,<predicname>>
		

		
		
		for(Predicate predicate: changedpredicate){
			for(AP ap: APset){
				if(ap.getListpredicate()!=null){
					if(ap.getPredicatename().contains(predicate.getPredicatename()))
					{
						ap.getPredicatename().remove(predicate.getPredicatename());
					   
						
						APchanged.put(ap.getApname(),new ArrayList<Integer>());
						HashSet<Predicate> needtoremove=new HashSet<Predicate>();//=new Predicate();
						for(Predicate oldpredicate: ap.getListpredicate()){
							if(oldpredicate.getPredicatename()==predicate.getPredicatename()){
								needtoremove.add(oldpredicate);
							}
						}
						if(needtoremove!=null){
						ap.getListpredicate().removeAll(needtoremove);
					//	System.out.println("bingo");
					    }
					}
				}
			}
		}

		ArrayList<AP> APsetnew=new ArrayList<AP>();
		for(int i=0; i<APset.size();i++){
		//	if(i==214){
			//	System.out.println("bingo");
		//	}
			
			int tag=0;
			for(int j=i+1; j<APset.size();j++){
				AP api=APset.get(i);
				AP apj=APset.get(j);
				if(!api.getPredicatename().equals(apj.getPredicatename())){
					tag=1;
				}else{
					APchanged.remove(api.getApname());
				}
			}
			if(i==APset.size()-1||tag==1){
			APsetnew.add(APset.get(i));
			}
		}
		
		APset=APsetnew;
		

		
	    for(Predicate predicate:changedpredicate){
	    	AddOnePredicateForAPupdate(predicate,APchanged);
	    }


	    
	    DetermineRuleUpdate(APset,APchanged);

		CalAPExpupdateremove(devices);


		

		///
		//HashMap<devicename,HashMap<port,AP>>
		//CalAPExp();
		// TODO Auto-generated method stub	
	}
	
	private void CalAPExpupdate(HashMap<String, Device> devices){
		for(AP ap: APset){
			for(Predicate predicate: ap.getListpredicate()){
				String devicename=predicate.getDevicename();
				String portname=predicate.getPortname();
				HashMap<String, HashSet<Integer>> portapset=devices.get(devicename).getUpdateportapset();
				if(portapset.containsKey(portname)){
					portapset.get(portname).add(ap.getApbdds());
				}else{
					HashSet<Integer> set=new HashSet<Integer>();
					set.add(ap.getApbdds());
					portapset.put(portname,set);
				}
			}
		}
	}
	
	private void CalAPExpupdateremove(HashMap<String, Device> devices){
		
		for(AP ap: APset){
			for(Predicate predicate: ap.getListpredicate()){
				String devicename=predicate.getDevicename();
				String portname=predicate.getPortname();
				/*
				if(devicename.equals("seat")){
					if(devicename.equals("xe-2/0/0")){
						System.out.println("bingo");
					}
				}
				*/
				
				HashMap<String, HashSet<Integer>> portapset=devices.get(devicename).getUpdateportapset();
				if(portapset.containsKey(portname)){
					portapset.get(portname).add(ap.getApbdds());
				}else{
					HashSet<Integer> set=new HashSet<Integer>();
					set.add(ap.getApbdds());
					portapset.put(portname,set);
				}
			}
		}
	}
	
	private void AddOnePredicateForAPupdate(Predicate predicatetoadd, HashMap<Integer, ArrayList<Integer>> APchanged)
	{

		BDD thebdd = bddengine.getBDD();
		
		int pbdd=predicatetoadd.getFwbdds();
		int bddToAddNeg = thebdd.not(pbdd);//equation 2 求逆
		thebdd.ref(bddToAddNeg);
		if(APset.size()==0)
			
		{
			// initialize...
				if(predicatetoadd.getFwbdds() != BDDACLWrapper.BDDFalse)
				{
					thebdd.ref(pbdd);
					// List of AP portname, to be stored in AP
					ArrayList<Predicate> listpredicate = new ArrayList<Predicate>();
					listpredicate.add(predicatetoadd);
					// AP fwbdds
					//int pbdds = predicatetoadd.getFwbdds();
					// List of sub AP, to be stored in AP
					// create the AP
					AP ap = new AP(pbdd, listpredicate);
					APset.add(ap);
				//	List<SubAP> listSubAPnull =new ArrayList<SubAP>();
				//	List<String> listPortnamenull = new ArrayList<String>();
				//	AP apneg = new AP(bddToAddNeg, listSubAPnull,listPortnamenull);
				//	APset.add(apneg);
				}
				if(bddToAddNeg != BDDACLWrapper.BDDFalse)
				{	
				    //List<SubAP> listSubAPnull =new ArrayList<SubAP>();
				    ArrayList<Predicate> listPredicatenull = new ArrayList<Predicate>();
				    AP apneg = new AP(bddToAddNeg, listPredicatenull);
				    APset.add(apneg);
				}
		}else{
			List<AP> oldList=new ArrayList<AP>();
			oldList=APset;
		    
			//HashSet<AP> oldList=APset;
			//HashSet<Integer> oldList = APold;
			// set up a new list
			APset = new ArrayList<AP>();
			//APset=new HashSet<AP>();
			//APold = new HashSet<Integer>();
			Iterator<AP> iterold = oldList.iterator();//这是java里面的一个迭代器，主要用来取集合容器里面的值
			while(iterold.hasNext())//开始做笛卡尔积
			{
				AP oldap = iterold.next();
			
		//		ArrayList<Predicate> oldlistpredicate=oldap.getListpredicate();
				int tmps = thebdd.and(predicatetoadd.getFwbdds(), oldap.getApbdds());//新来的predicate和旧的ap逐条做与
				thebdd.ref(tmps);
				
				if(tmps != BDDACLWrapper.BDDFalse)//不等于0的话就加到新的APset里面
				{
					AP ap=new AP(tmps,oldap.getListpredicate());
					ap.getListpredicate().add(predicatetoadd);
					APset.add(ap);
					
					if(tmps!=oldap.getApbdds()){
						ap.setApname(apname);
						ArrayList<Integer> list=new ArrayList<Integer>();
						list.add(1000000);//1000000 is a unpossible predicate name in this test
						APchanged.put(apname++,list);
						HashSet<Integer> hs=new HashSet<Integer>(oldap.getPredicatename());
						hs.add(predicatetoadd.getPredicatename());
						ap.setPredicatename(hs);
						
						
					}else{
						ap.setApname(oldap.getApname());
						ArrayList<Integer> list=new ArrayList<Integer>();
						list.add(predicatetoadd.getPredicatename());
						APchanged.put(oldap.getApname(),list);
						HashSet<Integer> hs=new HashSet<Integer>(oldap.getPredicatename());
						hs.add(predicatetoadd.getPredicatename());
						ap.setPredicatename(hs);
					}
					//oldap.getListpredicate().add(predicatetoadd);
					
				}
				
				tmps = thebdd.and(bddToAddNeg, oldap.getApbdds());//新来的非predicate和旧的ap逐条做与
				thebdd.ref(tmps);
				if(tmps != BDDACLWrapper.BDDFalse)//不等于0的话就加到新的APset里面
				{
					AP ap=new AP(tmps,oldap.getListpredicate());
					APset.add(ap);
					if(tmps!=oldap.getApbdds()){
						ap.setApname(apname);
						ArrayList<Integer> list=new ArrayList<Integer>();
						list.add(1000000);
						APchanged.put(apname++,list);
						ap.setPredicatename(new HashSet<Integer>(oldap.getPredicatename()));
					}else{
						ap.setApname(oldap.getApname());
						ap.setPredicatename(new HashSet<Integer>(oldap.getPredicatename()));
					
					}
				}
				
			}
			/**
			 * in this case, we need to de-ref useless nodes.
			 * we still keep bddToAdd, since it is the bdd node for an acl
			 * we will de-ref:
			 * bddToAddNeg, the whole list of oldList.
			  */
			int [] toDeRef = new int[oldList.size() + 1];
			int cntr = 0;
			for(AP oldap : oldList)//把以前的ap都存在toDeRef里面
			{
				toDeRef[cntr] = oldap.getApbdds();
				cntr ++;
			}
			toDeRef[oldList.size()] = bddToAddNeg;//把最后一个predict的bdd的非删掉。
			bddengine.DerefInBatch(toDeRef);//通通删掉？这里有问题会不会把新生成的也删掉？
		}
	}
	public ArrayList<AP> DetermineRuleUpdate(ArrayList<AP> APset, HashMap<Integer, ArrayList<Integer>> APchanged)//3 different case, ap is changed, ap has a unchanged bdd but has new perdicate,ap has changed bdd 
	{
		BDD thebdd = bddengine.getBDD();
	//	int kk=0;//test
	//	ArrayList<AP> newAPset=new ArrayList<AP>();
		
		    for(int i=0;i<APset.size();i++){//for every AP
	
		    	
		    	if(APchanged.containsKey(APset.get(i).getApname())){
		    	
				    	ArrayList<Predicate> newpredicateset=new ArrayList<Predicate>();//build a new preidcateset
				    	HashSet<Integer> perdicatename=APset.get(i).getPredicatename();
				    	ArrayList<Predicate> predicateset=APset.get(i).getListpredicate();//old predicateset
				    	
					    int predicatesize=predicateset.size();
				//	    System.out.println("APID"+i+"predicatenumber"+predicatesize);
					    
					    if(APchanged.get(APset.get(i).getApname()).contains(1000000)){
						
					    	for(int j=0;j<predicatesize;j++){
								    	    Predicate predicate=predicateset.get(j);//select a old predicate from the old set
								    	   ArrayList<Rule> newruleset=new ArrayList<Rule>();//build a new rule set
								    	   ArrayList<Rule> ruleset= predicate.getRuleset();   //get old predicate's ruleset
								    	   int rulesize=ruleset.size();
								    	       for(int k=0; k<rulesize;k++){
								    	    	   Rule rule=ruleset.get(k); 	    	   
								    	    	   int ruleBDD=rule.getRuleBDD();
								    	    	   int newrulebdd=thebdd.and(APset.get(i).getApbdds(), ruleBDD);//rule's bdd join with ap's bdd
								    	    	  // thebdd.ref(newrulebdd);
								    	    	   if (newrulebdd !=BDDACLWrapper.BDDFalse){
								    	    		 //  thebdd.deref(newrulebdd);//in order to consider the JDD
								    	    		   thebdd.ref(newrulebdd);
								    	    		   Rule newrule=new Rule(rule.getPortname(),rule.getDevicename(),rule.getHead(),rule.getTail(),ruleBDD, rule.getRulename());
								    	    		   newruleset.add(newrule);	   //build a new rule's rule set
								    	    	   }  
								    	       }
								    	    Predicate newpreidcate=new Predicate(predicate.getPortname(), predicate.getDevicename(),predicate.getFwbdds(),newruleset);
								    	    newpreidcate.setPredicatename(predicate.getPredicatename());
								    	    newpredicateset.add(newpreidcate);    //build a new predicate's set
								    	    perdicatename.add(predicate.getPredicatename());
				                    }
						    	
						    	APset.get(i).setListpredicate(newpredicateset);
					    }else{
					    	HashSet<Predicate> needtoremove=new HashSet<Predicate>();
					    	for(int j=0;j<predicatesize;j++){
			                    if(APchanged.get(APset.get(i).getApname()).contains(predicateset.get(j).getPredicatename())){
							    	    Predicate predicate=predicateset.get(j);//select a old predicate from the old set
							    	   ArrayList<Rule> newruleset=new ArrayList<Rule>();//build a new rule set
							    	   ArrayList<Rule> ruleset= predicate.getRuleset();   //get old predicate's ruleset
							    	   int rulesize=ruleset.size();
							    	       for(int k=0; k<rulesize;k++){
							    	    	   Rule rule=ruleset.get(k); 	    	   
							    	    	   int ruleBDD=rule.getRuleBDD();
							    	    	 
							    	    	   
							    	    	   int newrulebdd=thebdd.and(APset.get(i).getApbdds(), ruleBDD);//rule's bdd join with ap's bdd
							    	    	  // thebdd.ref(newrulebdd);
							    	    	   if (newrulebdd !=BDDACLWrapper.BDDFalse){
							    	    		 //  thebdd.deref(newrulebdd);//in order to consider the JDD
							    	    		   thebdd.ref(newrulebdd);
							    	    		   Rule newrule=new Rule(rule.getPortname(),rule.getDevicename(),rule.getHead(),rule.getTail(),ruleBDD, rule.getRulename());
							    	    		   newruleset.add(newrule);	   //build a new rule's rule set
							    	    	   }  
							    	       }
							    	    Predicate newpreidcate=new Predicate(predicate.getPortname(), predicate.getDevicename(),predicate.getFwbdds(),newruleset);
							    	    newpreidcate.setPredicatename(predicate.getPredicatename());
							    	    newpredicateset.add(newpreidcate);    //build a new predicate's set
							    	    perdicatename.add(predicate.getPredicatename());
							    	    needtoremove.add(predicate);
			                    }
					    	}
					    	APset.get(i).getListpredicate().removeAll(needtoremove);
					    	APset.get(i).getListpredicate().addAll(newpredicateset);
					    }
				 //   	APset.get(i).setApname(apname++);
		       }
		    }
		 //   System.out.println("123");//test
		 //   System.out.println("final="+kk);//test
		return APset;
	}
	
}


