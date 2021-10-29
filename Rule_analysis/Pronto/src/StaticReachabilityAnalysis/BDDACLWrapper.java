package StaticReachabilityAnalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;

import i2analysis.ACLForTree;
import i2analysis.ACLrule;
import i2analysis.AclRuleUniver;
import i2analysis.AclRuleUniverDevice;
import i2analysis.Headtail;
import i2analysis.PortPairACL;
import i2analysis.Rule;
import i2analysis.UpdateNode;
import i2analysis.VlanPredicate;
import stanalysis.ForwardingRule;
import stanalysis.Subnet;
import jdd.bdd.*;
//import jdd.bdd.debug.DebugBDD;

/**
 * The true, false, bdd variables, the negation of bdd variables:
 * their reference count are already set to maximal, so they will never be 
 * garbage collected. And no need to worry about the reference count for them.
 * @author carmo
 *
 */
public class BDDACLWrapper implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7284490986562707221L;

	BDD aclBDD;

	/**
	 * the arrays store BDD variables.
	 */
	final static int protocolBits = 8;
	int[] protocol; //protocol[0] is the lowest bits
	final static int portBits = 16;
	int[] srcPort;
	int[] dstPort;
	final static int ipBits = 32;
	int[] srcIP;
	int[] dstIP;
   // static int counttest=0;
	/**
	 * for readability. In bdd:
	 * 0 is the false node
	 * 1 is the true node
	 */
	public final static int BDDFalse = 0;
	public final static int BDDTrue = 1;
    public static int rulename=0;
    

	
    public final static int getipBits(){
    	return ipBits;
    }//yu zhao
    
	public BDDACLWrapper()
	{
	//	aclBDD = new BDD(100000, 1000000);
		aclBDD = new BDD(1000000, 1000000);
		protocol = new int[protocolBits];
		srcPort = new int[portBits];
		dstPort = new int[portBits];
		srcIP = new int[ipBits];
		dstIP = new int[ipBits];

		/**
		 * will try more orders of variables
		 */
		DeclareSrcIP();
		DeclareDstIP();
		DeclareSrcPort();
		DeclareDstPort();
		DeclareProtocol();

	}
	

	public BDD getBDD()
	{
		return aclBDD;
	}

	public HashMap<String, Integer> getfwdbdds(ArrayList<ForwardingRule> fws)
	{
		int alreadyfwded = BDDFalse;
		HashMap<String, Integer> fwdbdds = new HashMap<String, Integer>();
		int longestmatch = 32;

		//int prefixchk = encodeDstIPPrefix(2148270417L, 32);

		for(int i = longestmatch; i >=0; i --)
		{
			for(int j = 0; j < fws.size(); j ++)
			{
				ForwardingRule onefw = fws.get(j);
				if(onefw.getprefixlen() == i)
				{

					String iname = onefw.getiname();
					//int[] ipbin = Utility.CalBinRep(onefw.getdestip(), ipBits);
					//int[] ipbinprefix = new int[onefw.getprefixlen()];
					//for(int k = 0; k < onefw.getprefixlen(); k ++)
					//{
					//	ipbinprefix[k] = ipbin[k + ipBits - onefw.getprefixlen()];
					//}
					//int entrybdd = EncodePrefix(ipbinprefix, dstIP, ipBits);
					int[] head=new int[ipBits];
					int[] tail=new int[ipBits];
					int entrybdd = encodeDstIPPrefix1(onefw.getdestip(), onefw.getprefixlen(),head,tail);

					int notalreadyfwded = aclBDD.not(alreadyfwded);
					aclBDD.ref(notalreadyfwded);
					int toadd = aclBDD.and(entrybdd, notalreadyfwded);
					aclBDD.ref(toadd);
					aclBDD.deref(notalreadyfwded);
					int altmp = aclBDD.or(alreadyfwded, entrybdd);
					aclBDD.ref(altmp);
					aclBDD.deref(alreadyfwded);
					alreadyfwded = altmp;
					onefw.setBDDRep(entrybdd);
					//aclBDD.deref(entrybdd);

					/*
					if(aclBDD.and(prefixchk, toadd) > 0)
					{
						System.out.println(onefw);
					}*/

					if(fwdbdds.containsKey(iname))
					{
						int oldkey = fwdbdds.get(iname);
						int newkey = aclBDD.or(toadd, oldkey);
						aclBDD.ref(newkey);
						aclBDD.deref(toadd);
						aclBDD.deref(oldkey);
						fwdbdds.put(iname, newkey);
					}else
					{
						fwdbdds.put(iname, toadd);
					}
				}
			}
		}
		aclBDD.deref(alreadyfwded);
		return fwdbdds;
	}

	public HashMap<String, Integer> getfwdbdds_no_store(ArrayList<ForwardingRule> fws)
	{
		int alreadyfwded = BDDFalse;
		HashMap<String, Integer> fwdbdds = new HashMap<String, Integer>();
		int longestmatch = 32;

		//int prefixchk = encodeDstIPPrefix(2148270417L, 32);

		for(int i = longestmatch; i >=0; i --)
		{
			for(int j = 0; j < fws.size(); j ++)
			{
				ForwardingRule onefw = fws.get(j);
				if(onefw.getprefixlen() == i)
				{

					String iname = onefw.getiname();
					//int[] ipbin = Utility.CalBinRep(onefw.getdestip(), ipBits);
					//int[] ipbinprefix = new int[onefw.getprefixlen()];
					//for(int k = 0; k < onefw.getprefixlen(); k ++)
					//{
					//	ipbinprefix[k] = ipbin[k + ipBits - onefw.getprefixlen()];
					//}
					//int entrybdd = EncodePrefix(ipbinprefix, dstIP, ipBits);
					int[] head=new int[ipBits];
					int[] tail=new int[ipBits];
					int entrybdd = encodeDstIPPrefix1(onefw.getdestip(), onefw.getprefixlen(),head,tail);

					int notalreadyfwded = aclBDD.not(alreadyfwded);
					aclBDD.ref(notalreadyfwded);
					int toadd = aclBDD.and(entrybdd, notalreadyfwded);
					aclBDD.ref(toadd);
					aclBDD.deref(notalreadyfwded);
					int altmp = aclBDD.or(alreadyfwded, entrybdd);
					aclBDD.ref(altmp);
					aclBDD.deref(alreadyfwded);
					alreadyfwded = altmp;
					//onefw.setBDDRep(entrybdd);
					aclBDD.deref(entrybdd);

					/*
					if(aclBDD.and(prefixchk, toadd) > 0)
					{
						System.out.println(onefw);
					}*/

					if(fwdbdds.containsKey(iname))
					{
						int oldkey = fwdbdds.get(iname);
						int newkey = aclBDD.or(toadd, oldkey);
						aclBDD.ref(newkey);
						aclBDD.deref(toadd);
						aclBDD.deref(oldkey);
						fwdbdds.put(iname, newkey);
					}else
					{
						fwdbdds.put(iname, toadd);
					}
				}
			}
		}
		aclBDD.deref(alreadyfwded);
		return fwdbdds;
	}

	public ArrayList<Headtail> transfer(int[] head,int[] tail, ArrayList<Headtail> rangeset){
		int result1;
		int result2;
		int result;
		ArrayList<Headtail> newrangeset= new ArrayList<Headtail>();
		//int[] newhead=new int[ipBits];
		//int[] newtail=new int[ipBits];
		int[] newrangehead=new int[ipBits];
		int[] newrangetail=new int[ipBits];
		int k,sign=0;
		for(int i=0;i<rangeset.size();i++){
			
		  if (sign==0){
			result1=firstisbigger(rangeset.get(i).getHead(),head);
			result2=firstisbigger(head,rangeset.get(i+1).getHead());
			if((result1 != 1)&&(result2 !=1)){//the game begin,1-2  3-4  5-6  7-8,where the new rule locate
				sign=1;
				result=firstisbigger(rangeset.get(i).getTail(),head);
				if(result != 1){//head locate in the 2-3
					//head=head;head not change
					Headtail headtail=new Headtail(rangeset.get(i).getHead(),rangeset.get(i).getTail());
				    newrangeset.add(headtail);//the first range do not change
				    k=i;
				    while(true){
				    	k=k+1;
				    result1=firstisbigger(rangeset.get(k).getHead(),tail);
				    result2=firstisbigger(rangeset.get(k).getTail(),tail);
				    if(result1 == 1){////3>tail
				    	//tail not change
				    	for(int j=0;j<ipBits;j++){
				    		newrangehead[j]=head[j];
				    		newrangetail[j]=tail[j];
				    	}
				    	//
				    	Headtail headtail1=new Headtail(newrangehead,newrangetail);
				    	newrangeset.add(headtail1);
				    	i=k-1;
				    	break;
				    	
				    }else if(result2==1){////4>tail
				   // 	System.out.println("bingo");
				    	//
				    	for(int j=0;j<ipBits;j++){
				    		newrangehead[j]=head[j];
				    		newrangetail[j]=rangeset.get(k).getTail()[j];
				    	}
				    	//
				    	Headtail headtail1=new Headtail(newrangehead,newrangetail);
				    	newrangeset.add(headtail1);
				    	i=k;
				    	break;
				    }
				    }//end while
					
				}else{//head locate in the 1-2
					//
					for(int j=0;j<ipBits;j++){head[j]=rangeset.get(i).getTail()[j];}//head change to 2
					//
					result=firstisbigger(rangeset.get(i).getTail(),tail);
					if(result != 1){//tail > 2
						k=i;
						while(true){
							k=k+1;
							result1=firstisbigger(rangeset.get(k).getHead(),tail);
							result2=firstisbigger(rangeset.get(k).getTail(),tail);
							if (result1 ==1){//3>tail
								//tail=tail;tail not change
								//
								for(int j=0;j<ipBits;j++){
									newrangehead[j]=rangeset.get(i).getHead()[j];
									newrangetail[j]=tail[j];
								}
								//
								Headtail headtail=new Headtail(newrangehead,newrangetail);
						    	newrangeset.add(headtail);
								//
								i=k-1;
								break;	
							}else if(result ==1)//4>tail
							{
								//tail has been changed to 3
								for(int j=0;j<ipBits;j++){head[j]=rangeset.get(k).getHead()[j];}
								//
								for (int j=0;j<ipBits;j++){//range head is 1,range tail is 4
									newrangehead[j]=rangeset.get(i).getHead()[j];
									newrangetail[j]=rangeset.get(k).getTail()[j];
								}
								//
								i=k;
								break;	
							}
							}//end while
						}else{//tail in 1-2
							//head and tail, I set it to be not changed
						System.out.println("the rule can not be probing");
						ArrayList<Headtail> newrangesetcovered= new ArrayList<Headtail>();
						newrangesetcovered=rangeset;
						return newrangesetcovered;//the rule is covered set a null arraylist back		
					    }	
			   	  }	
			}else{
				Headtail headtail=new Headtail(rangeset.get(i).getHead(),rangeset.get(i).getTail());
			    newrangeset.add(headtail);
			}
		    }else{
		    	Headtail headtail=new Headtail(rangeset.get(i).getHead(),rangeset.get(i).getTail());
			    newrangeset.add(headtail);
		    }
		}//end for
			return newrangeset;
		}
			
    /*
    //test program
      int[] testhead1=new int[ipBits];
      int[] testtail1=new int[ipBits];
      int prefixlen=4;
      int[] ipbin=new int[4];
      ipbin[0]=0;ipbin[1]=0;ipbin[2]=0;ipbin[3]=1;//0001
      int q=0;
      for(int k=0;k<ipBits;k++){
	    	if(k<(ipBits-prefixlen)){
	    		testhead1[k]=0;
	    		testtail1[k]=1;
	    	}else{	
	    		testhead1[k]=ipbin[q];
	    		testtail1[k]=ipbin[q];
	    		q=q+1;
	    	}
	    }
      Headtail testheadtail=new Headtail(testhead1,testtail1);
      rangset.add(testheadtail);
      /////
      int[] testhead2=new int[ipBits];
      int[] testtail2=new int[ipBits];
      prefixlen=4;
      ipbin=new int[4];
      ipbin[0]=0;ipbin[1]=1;ipbin[2]=0;ipbin[3]=1;//0101
      q=0;
      for(int k=0;k<ipBits;k++){
	    	if(k<(ipBits-prefixlen)){
	    		testhead2[k]=0;
	    		testtail2[k]=1;
	    	}else{	
	    		testhead2[k]=ipbin[q];
	    		testtail2[k]=ipbin[q];
	    		q=q+1;
	    	}
	    }
      Headtail testheadtails=new Headtail(testhead2,testtail2);
      rangset.add(testheadtails);
      //////
      int[] testhead3=new int[ipBits];
      int[] testtail3=new int[ipBits];
     
      prefixlen=4;
      ipbin=new int[4];
      ipbin[0]=0;ipbin[1]=0;ipbin[2]=1;ipbin[3]=1;//0011
      q=0;
      for(int k=0;k<ipBits;k++){
	    	if(k<(ipBits-prefixlen)){
	    		testhead3[k]=0;
	    		testtail3[k]=1;
	    	}else{	
	    		testhead3[k]=ipbin[q];
	    		testtail3[k]=ipbin[q];
	    		q=q+1;
	    	}
	    }
      Headtail testheadtailss=new Headtail(testhead3,testtail3);
      rangset.add(testheadtailss);
      rangset.add(headtail2);
      ////rule's head and tail
      prefixlen=3;
      ipbin=new int[3];
      ipbin[0]=1;ipbin[1]=0;ipbin[2]=1;//ipbin[3]=1;//101
      q=0;
 /*     for(int k=0;k<ipBits;k++){
	    	if(k<(ipBits-prefixlen)){
	    		head[k]=0;
	    		tail[k]=1;
	    	}else{	
	    		head[k]=ipbin[q];
	    		tail[k]=ipbin[q];
	    		q=q+1;
	    	}
      }                            
  */
  /*    head=testhead1;
      tail=testtail3;
      rangset=transfer(head,tail,rangset);
      */
    //test end;ip is 01 is ok,ip is 001 is ok,ip is 101 is ok,head of ip is 0001 tail of ip is 0011;  
	/**
	 * from shorted to longest
	 * @param fws
	 * @return
	 * @throws IOException 
	 */
	//public HashMap<String, Integer> getfwdbdds_sorted_no_store(ArrayList<ForwardingRule> fws, HashMap<String, HashMap<String, Integer>> Port_Rulebdds,String name, ArrayList<Rule> ruleset)
	//test
	public HashMap<String, Integer> getfwdbdds_sorted_no_store1(ArrayList<ForwardingRule> fws, HashMap<String, ArrayList<Rule>> Port_Rule,String name, HashMap<Integer,Rule> univerhashmap) throws IOException
	{
		int alreadyfwded = BDDFalse;
		HashMap<String, Integer> fwdbdds = new HashMap<String, Integer>();

		//int prefixchk = encodeDstIPPrefix(2148270417L, 32);
		//System.out.println(fws);
		long test=0;
		BufferedWriter out1=null;
		if(name.equals("coza_rtr")){
		File writename1 = new File("C:/text3.txt"); 
		writename1.createNewFile(); 
		out1 = new BufferedWriter(new FileWriter(writename1));
		}
		for(int j = fws.size() - 1; j >= 0; j --)
		{
			
			long t1=0;
			long t2=0;
			if(name.equals("coza_rtr")){
				t1 = System.nanoTime();
			}
			ForwardingRule onefw = fws.get(j);
			//System.out.println(j);
             

			String iname = onefw.getiname();
			//int[] ipbin = Utility.CalBinRep(onefw.getdestip(), ipBits);
			//int[] ipbinprefix = new int[onefw.getprefixlen()];
			//for(int k = 0; k < onefw.getprefixlen(); k ++)
			//{
			//	ipbinprefix[k] = ipbin[k + ipBits - onefw.getprefixlen()];
			//}
			//int entrybdd = EncodePrefix(ipbinprefix, dstIP, ipBits);
			int entrybdd = encodeDstIPPrefix(onefw.getdestip(), onefw.getprefixlen());//把prefix换成BDD

			int notalreadyfwded = aclBDD.not(alreadyfwded);///
			aclBDD.ref(notalreadyfwded);
			int toadd = aclBDD.and(entrybdd, notalreadyfwded);
			/*
			if(toadd==BDDFalse){
				continue;
			}
			*/
			aclBDD.ref(toadd);
			aclBDD.deref(notalreadyfwded);
			int altmp = aclBDD.or(alreadyfwded, entrybdd);
			aclBDD.ref(altmp);
			aclBDD.deref(alreadyfwded);
			alreadyfwded = altmp;
			//onefw.setBDDRep(entrybdd);
			aclBDD.deref(entrybdd);

			/*
					if(aclBDD.and(prefixchk, toadd) > 0)
					{
						System.out.println(onefw);
					}*/

			if(fwdbdds.containsKey(iname))
			{
				int oldkey = fwdbdds.get(iname);
				int newkey = aclBDD.or(toadd, oldkey);
				aclBDD.ref(newkey);
				aclBDD.deref(toadd);
				aclBDD.deref(oldkey);
				fwdbdds.put(iname, newkey);
			}else
			{
				fwdbdds.put(iname, toadd);
			}
			if(name.equals("coza_rtr")){
            t2 = System.nanoTime();
			test+=t2-t1;
			out1.write(String.valueOf(test));
			out1.newLine();
			}

		}
		if(name.equals("coza_rtr")){
		out1.flush(); 
			
	    out1.close();
		}
		aclBDD.deref(alreadyfwded);
		return fwdbdds;
	}
	
	public HashMap<String, Integer> getfwdbdds_sorted_no_store(ArrayList<ForwardingRule> fws, HashMap<String, ArrayList<Rule>> Port_Rule,String name, HashMap<Integer,Rule> univerhashmap,AclRuleUniverDevice aclruleuniverdevice,ACLForTree aclfortree, HashSet<UpdateNode> nofathernode, HashMap<Integer, UpdateNode> removemap, Integer ruleid, ForwardingRule remain)
	{
		ArrayList<ACLrule> pilist=new ArrayList<ACLrule>();
		ArrayList<ACLrule> polist=new ArrayList<ACLrule>();
		ArrayList<ACLrule> dilist=new ArrayList<ACLrule>();
		ArrayList<ACLrule> dolist=new ArrayList<ACLrule>();
        
		
		if(!aclruleuniverdevice.getDeviceaclinpermit().isEmpty()){			
			for(String port: aclruleuniverdevice.getDeviceaclinpermit().keySet()){
				pilist.addAll(aclruleuniverdevice.getDeviceaclinpermit().get(port));
			}
		}
		
		if(!aclruleuniverdevice.getDeviceacloutpermit().isEmpty()){			
			for(String port: aclruleuniverdevice.getDeviceacloutpermit().keySet()){
				polist.addAll(aclruleuniverdevice.getDeviceacloutpermit().get(port));
			}
		}
		
		if(!aclruleuniverdevice.getDeviceaclindeny().isEmpty()){			
			for(String port: aclruleuniverdevice.getDeviceaclindeny().keySet()){
				dilist.addAll(aclruleuniverdevice.getDeviceaclindeny().get(port));
			}
		}
		
		if(!aclruleuniverdevice.getDeviceacloutdeny().isEmpty()){	
			for(String port: aclruleuniverdevice.getDeviceacloutdeny().keySet()){
				dolist.addAll(aclruleuniverdevice.getDeviceacloutdeny().get(port));
			}
		}
		
		
		
		int alreadyfwded = BDDFalse;
		HashMap<String, Integer> fwdbdds = new HashMap<String, Integer>();//portname,predict
		
		ArrayList<Headtail> rangset=new ArrayList<Headtail>();
		int entrybdd;
		String iname;//portname
		int[] head;
		int[] tail;
		ArrayList<Integer> ruleuid=new ArrayList<Integer>();
		HashMap<Integer,Rule> rulemap=new HashMap<Integer,Rule>();
		for(int j = fws.size() - 1; j >= 0; j --)
		{   //System.out.println(counttest++);		
			ForwardingRule onefw = fws.get(j);//提取第j条rule，并且倒着提取
			//System.out.println(j);
			iname = onefw.getiname();//xe-2/2/0/提取的是portname
		   // System.out.println(rulename);
			//if(rulename==826){
			//	System.out.println("bingo");
			//}
			
		//	System.out.println(rulename);
			if((rulename+1)!=ruleid||ruleid==10000000){
			head=new int[ipBits];
			tail=new int[ipBits];			
			entrybdd = encodeDstIPPrefix1(onefw.getdestip(), onefw.getprefixlen(),head,tail);//把prefix换成BDD
	//		entrybdd = encodeDstIPPrefix(onefw.getdestip(), onefw.getprefixlen());
			int notalreadyfwded = aclBDD.not(alreadyfwded);///alreadyfwded取非
			aclBDD.ref(notalreadyfwded);
			int toadd = aclBDD.and(entrybdd, notalreadyfwded);///////////
			if(toadd==BDDFalse){
				continue;
			}
			aclBDD.ref(toadd);
			//permit in
			if(!pilist.isEmpty()){
				for(ACLrule aclrule:pilist){
					   if(aclBDD.and(toadd, aclrule.getFwdBDD())!=BDDFalse){
						     if(!iname.equals(aclrule.getPortname())){//input port is not same to the output's port
									     String s=new String(name+aclrule.getPortname()+iname);//device name+ portnamein+portnameout
									     String s1=new String(name+iname);//device name+portnameout
									     if(!aclfortree.getPermitin().containsKey(s)){//hashmap do not have such s ports
									    	  HashMap<Integer,ArrayList<ACLrule>> map=new HashMap<Integer,ArrayList<ACLrule>>();
									    	  ArrayList<ACLrule> nlist=new ArrayList<ACLrule>();
									    	  nlist.add(aclrule);
									    	  map.put(rulename+1,nlist);
									    	  aclfortree.getPermitin().put(s, map);
									    	  aclfortree.getPermitinup().put(s1, map);
									     }else{//hashmap already has such s ports
									    	 HashMap<Integer,ArrayList<ACLrule>> hmap=aclfortree.getPermitin().get(s);
									    	 HashMap<Integer,ArrayList<ACLrule>> hmap2=aclfortree.getPermitinup().get(s1);
									    	 if(!hmap.containsKey(rulename+1)){//if the rule's name is aready in the port
									    		  ArrayList<ACLrule> nlist=new ArrayList<ACLrule>();
										    	  nlist.add(aclrule); 
										    	  hmap.put(rulename+1,nlist);
										    	  hmap2.put(rulename+1,nlist);
									    	 }else{
									    		 hmap.get(rulename+1).add(aclrule);
									    		 hmap2.get(rulename+1).add(aclrule);
									    	 }
									     }
						     }
					   }	
				}
			}
			//deny in
			if(!dilist.isEmpty()){
				for(ACLrule aclrule:dilist){
					   if(aclBDD.and(toadd, aclrule.getFwdBDD())!=BDDFalse){
						     if(!iname.equals(aclrule.getPortname())){//input port is not same to the output's port
									     String s=new String(name+aclrule.getPortname()+iname);//device name+ portnamein+portnameout
									     if(!aclfortree.getDenyin().containsKey(s)){//hashmap do not have such s ports
									    	  HashMap<Integer,ArrayList<ACLrule>> map=new HashMap<Integer,ArrayList<ACLrule>>();
									    	  ArrayList<ACLrule> nlist=new ArrayList<ACLrule>();
									    	  nlist.add(aclrule);
									    	  map.put(rulename+1,nlist);
									    	  aclfortree.getDenyin().put(s, map);
									     }else{//hashmap already has such s ports
									    	 HashMap<Integer,ArrayList<ACLrule>> hmap=aclfortree.getDenyin().get(s);
									    	 if(!hmap.containsKey(rulename+1)){//if the rule's name is aready in the port
									    		  ArrayList<ACLrule> nlist=new ArrayList<ACLrule>();
										    	  nlist.add(aclrule); 
										    	  hmap.put(rulename+1,nlist);
									    	 }else{
									    		 hmap.get(rulename+1).add(aclrule);
									    	 }
									     }
						     }
					   }	
				}
			}
			//permit out
			if(!polist.isEmpty()){
				for(ACLrule aclrule:polist){
					 if(aclrule.getPortname().equals(iname)){
					   if(aclBDD.and(toadd, aclrule.getFwdBDD())!=BDDFalse){			   
									     String s=new String(name+iname);//device name+ portnameout
									     if(!aclfortree.getPermitout().containsKey(s)){//hashmap do not have such s ports
									    	  HashMap<Integer,ArrayList<ACLrule>> map=new HashMap<Integer,ArrayList<ACLrule>>();
									    	  ArrayList<ACLrule> nlist=new ArrayList<ACLrule>();
									    	  nlist.add(aclrule);
									    	  map.put(rulename+1,nlist);
									    	  aclfortree.getPermitout().put(s, map);
									     }else{//hashmap already has such s ports
									    	 HashMap<Integer,ArrayList<ACLrule>> hmap=aclfortree.getPermitout().get(s);
									    	 if(!hmap.containsKey(rulename+1)){//if the rule's name is aready in the port
									    		  ArrayList<ACLrule> nlist=new ArrayList<ACLrule>();
										    	  nlist.add(aclrule); 
										    	  hmap.put(rulename+1,nlist);
									    	 }else{
									    		 hmap.get(rulename+1).add(aclrule);
									    	 }
									     }
						     
					   }	
				    }
				}
			}
			//deny out
			if(!dolist.isEmpty()){
				for(ACLrule aclrule:dolist){
					 if(aclrule.getPortname().equals(iname)){
					   if(aclBDD.and(toadd, aclrule.getFwdBDD())!=BDDFalse){
						    
									     String s=new String(name+iname);//device name+ portnameout
									     if(!aclfortree.getDenyout().containsKey(s)){//hashmap do not have such s ports
									    	  HashMap<Integer,ArrayList<ACLrule>> map=new HashMap<Integer,ArrayList<ACLrule>>();
									    	  ArrayList<ACLrule> nlist=new ArrayList<ACLrule>();
									    	  nlist.add(aclrule);
									    	  map.put(rulename+1,nlist);
									    	  aclfortree.getDenyout().put(s, map);
									     }else{//hashmap already has such s ports
									    	 HashMap<Integer,ArrayList<ACLrule>> hmap=aclfortree.getDenyout().get(s);
									    	 if(!hmap.containsKey(rulename+1)){//if the rule's name is aready in the port
									    		  ArrayList<ACLrule> nlist=new ArrayList<ACLrule>();
										    	  nlist.add(aclrule); 
										    	  hmap.put(rulename+1,nlist);
									    	 }else{
									    		 hmap.get(rulename+1).add(aclrule);
									    	 }
									     }
						     
					   }	
				    }
				}
			}
			
			
			aclBDD.deref(notalreadyfwded);
			int altmp = aclBDD.or(alreadyfwded, entrybdd);//new rule's uncovered bdd or alreadyfwded
			aclBDD.ref(altmp);
			aclBDD.deref(alreadyfwded);
			alreadyfwded = altmp;
			aclBDD.deref(entrybdd);

	
			if(fwdbdds.containsKey(iname))
			{
				
				rulename++;
				String s=String.valueOf(rulename);
				Rule rule=new Rule(iname,name,head,tail,toadd,s);
				rule.setPrefixlen(onefw.getprefixlen());
				Port_Rule.get(iname).add(rule);
				ruleuid.add(rulename);//update
				rulemap.put(rulename,rule);//update
				univerhashmap.put(rulename,rule);
                
                ///////		
				int oldkey = fwdbdds.get(iname);
				int newkey = aclBDD.or(toadd, oldkey);
				aclBDD.ref(newkey);
				
			//	if( newkey!=oldkey){
			//	aclBDD.deref(oldkey);//r
			//	}
			
				fwdbdds.put(iname, newkey);
			
			}else
			{
				fwdbdds.put(iname, toadd);
				
				
				//i=0;
				rulename++;
				String s=String.valueOf(rulename);
				//String s=String.valueOf(i+name);
				Rule rule=new Rule(iname,name,head,tail,toadd,s);
				ArrayList<Rule> ruleset =new ArrayList<Rule>();
				ruleset.add(rule);
				ruleuid.add(rulename);//update
				rulemap.put(rulename,rule);//update
				Port_Rule.put(iname,ruleset);
				//universet.add(rule);//compute all rules
				univerhashmap.put(rulename,rule);
				
			}
			}else{
				//	remain=new ForwardingRule(onefw.getdestip(), onefw.getprefixlen(), iname);
		        remain.setDestip(onefw.getdestip());
		        remain.setPrefixlen(onefw.getprefixlen());
						//onefw;
				remain.setDevicename(name);
				remain.setPortname(iname);
				rulename++;
			}
		}
		System.out.println("rangset.size()"+rangset.size());
		System.out.println("fws.size()"+fws.size());
		aclBDD.deref(alreadyfwded);
		//////////////update//////
		HashMap<Integer,UpdateNode> already=new HashMap<Integer,UpdateNode>();
		UpdateNode newnode;
		UpdateNode oldnode;
		for (Integer id: ruleuid){
			if(already.containsKey(id)){
				already.get(id);
				break;
			}else{
				oldnode=new UpdateNode();
				oldnode.setRule(rulemap.get(id));
				removemap.put(id,oldnode);//update remove
				//if(id==1234){
				//	System.out.println("bingo");
				//}
				nofathernode.add(oldnode);
			}
			
			Rule rule=rulemap.get(id);
			for(int i=id+1;i<ruleuid.size();i++){
			
				if(!rulemap.containsKey(i)){
					break;
				}
			    boolean iscon=contain(rulemap.get(i),rule);//first contain the last
				if(iscon==true){
					if(!already.containsKey(i)){
						newnode=new UpdateNode();
						oldnode.setParent(newnode);
						newnode.setRule(rulemap.get(id));
						newnode.getChildren().add(oldnode);
						nofathernode.remove(oldnode);
						nofathernode.add(newnode);
						oldnode=newnode;//point
						removemap.put(i,newnode);//update remove
					
					}else{
						oldnode.setParent(already.get(i));
						already.get(i).getChildren().add(oldnode);
						oldnode=already.get(i);
						break;
					}
					
				}
			    //UpdateNode
			}
		}
		
		
		////
		return fwdbdds;
	}
	
	public HashMap<String, VlanPredicate> getfwdbdds_sorted_no_store_vlan(ArrayList<ForwardingRule> fws, HashMap<String, ArrayList<Rule>> Port_Rule,String name, HashMap<Integer,Rule> univerhashmap,HashMap<String,AclRuleUniverDevice> vlanaclruledevice,HashMap<String ,ACLForTree> aclfortreevlan, HashMap<String, HashSet<String>> vlan_ports)
	{
		
		HashMap<String,HashMap<Integer,HashMap<String, ACLrule>>> pilist=new HashMap<String,HashMap<Integer,HashMap<String, ACLrule>>>();//<vlanid,<perbdd,<portname,aclrule>>>
		HashMap<String,HashMap<Integer,HashMap<String, ACLrule>>> polist=new HashMap<String,HashMap<Integer,HashMap<String, ACLrule>>>();
		HashMap<String,HashMap<Integer,HashMap<String, ACLrule>>> dilist=new HashMap<String,HashMap<Integer,HashMap<String, ACLrule>>>();
		HashMap<String,HashMap<Integer,HashMap<String, ACLrule>>> dolist=new HashMap<String,HashMap<Integer,HashMap<String, ACLrule>>>();
		for(String vlanid: vlanaclruledevice.keySet()){
			pilist.put(vlanid, vlanaclruledevice.get(vlanid).getVlandeaclinper());
			polist.put(vlanid, vlanaclruledevice.get(vlanid).getVlandeacloutper());
			dilist.put(vlanid, vlanaclruledevice.get(vlanid).getVlandeaclindeny());
			dolist.put(vlanid, vlanaclruledevice.get(vlanid).getVlandeacloutdeny());
		}
		
		HashMap<String, VlanPredicate> vlanpremap=new HashMap<String, VlanPredicate>();//<vlandid,vlanpre>
		HashMap<String, Integer> fwdbdds = new HashMap<String, Integer>();//portname,predict
		//HashMap<String, HashMap<String, Integer>> Port_Rulebdds=new HashMap<String,HashMap<String, Integer>>();
		//<portname,<rulename,BDD>>
		//int i=0;
		ArrayList<Headtail> rangset=new ArrayList<Headtail>();
	//	HashMap<int [],int []> alreadysetold=new HashMap<int [],int []>();
		//ArrayList<><>
		int entrybdd;
		String iname;//portname
		int[] head;
		int[] tail;
		
		for(int j = fws.size() - 1; j >= 0; j --)
		{   //System.out.println(counttest++);
			ForwardingRule onefw = fws.get(j);//提取第j条rule，并且倒着提取
			iname = onefw.getiname();//at here vlanname is the portname
			head=new int[ipBits];
			tail=new int[ipBits];			
			entrybdd = encodeDstIPPrefix1(onefw.getdestip(), onefw.getprefixlen(),head,tail);//把prefix换成BDD
			int alreadyfwded;
			if(vlanpremap.containsKey(iname)){
				alreadyfwded=vlanpremap.get(iname).getFwbdds();
			}else{
				alreadyfwded = BDDFalse;
				VlanPredicate vlanpre=new VlanPredicate();
				vlanpremap.put(iname,vlanpre);
			}
			
			int notalreadyfwded = aclBDD.not(alreadyfwded);///alreadyfwded取非
			aclBDD.ref(notalreadyfwded);
			int toadd = aclBDD.and(entrybdd, notalreadyfwded);///////////
			if(toadd==0){
				continue;
			}
			aclBDD.ref(toadd);
			aclBDD.deref(notalreadyfwded);
			int altmp = aclBDD.or(alreadyfwded, entrybdd);//new rule's uncovered bdd or alreadyfwded
			aclBDD.ref(altmp);
			aclBDD.deref(alreadyfwded);
			vlanpremap.get(iname).setFwbdds(altmp);
			//onefw.setBDDRep(entrybdd);
			aclBDD.deref(entrybdd);
			
			
			ArrayList<Rule> vlanrule=new ArrayList<Rule>();
			for(String portname: vlan_ports.get(iname)){//every port of this vlan
				
				
				///////////////////////////////////////////////////////////////
				if(pilist.containsKey(iname)){
				if(!pilist.get(iname).isEmpty()){
					for(Integer perbdd:pilist.get(iname).keySet()){
						if(aclBDD.and(toadd, perbdd)!=BDDFalse){
							for(String pname: vlan_ports.get(iname)){//every port of vlan
								if(pname.equals(portname)){
									continue;
								}
								ACLrule aclrule=pilist.get(iname).get(perbdd).get(portname);
								String s=new String(name+pname+portname);//device name+ portnamein+portnameout
							     String s1=new String(name+portname);//device name+portnameout
								if(!aclfortreevlan.get(iname).getPermitin().containsKey(s)){
									HashMap<Integer,ArrayList<ACLrule>> map=new HashMap<Integer,ArrayList<ACLrule>>();
									ArrayList<ACLrule> nlist=new ArrayList<ACLrule>();
									nlist.add(aclrule);//pilist.get(iname).get(perbdd).get(portname) is a aclrule
									map.put(rulename+1,nlist);
									aclfortreevlan.get(iname).getPermitin().put(s, map);
									aclfortreevlan.get(iname).getPermitinup().put(s1, map);
								}else{
									 HashMap<Integer,ArrayList<ACLrule>> hmap=aclfortreevlan.get(iname).getPermitin().get(s);
									 HashMap<Integer,ArrayList<ACLrule>> hmap2=aclfortreevlan.get(iname).getPermitinup().get(s1);
							    	 if(!hmap.containsKey(rulename+1)){//if the rule's name is aready in the port
							    		  ArrayList<ACLrule> nlist=new ArrayList<ACLrule>();
								    	  nlist.add(aclrule); 
								    	  hmap.put(rulename+1,nlist);
								    	  hmap2.put(rulename+1,nlist);
							    	 }else{
							    		 hmap.get(rulename+1).add(aclrule);
							    		 hmap2.get(rulename+1).add(aclrule);
							    	 }				
								}	
							}	
						}
					}		
				}
				}
				
				if(polist.containsKey(iname)){
                 if(!polist.containsKey(iname)){
					for(Integer perbdd:polist.get(iname).keySet()){
						if(aclBDD.and(toadd, perbdd)!=BDDFalse){
							for(String pname: vlan_ports.get(iname)){//every port of vlan
								if(pname.equals(portname)){
									continue;
								}
								ACLrule aclrule=polist.get(iname).get(perbdd).get(portname);
							     String s=new String(name+portname);//device name+portnameout
								if(!aclfortreevlan.get(iname).getPermitout().containsKey(s)){
									HashMap<Integer,ArrayList<ACLrule>> map=new HashMap<Integer,ArrayList<ACLrule>>();
									ArrayList<ACLrule> nlist=new ArrayList<ACLrule>();
									nlist.add(aclrule);//pilist.get(iname).get(perbdd).get(portname) is a aclrule
									map.put(rulename+1,nlist);
									aclfortreevlan.get(iname).getPermitout().put(s, map);
								}else{
									 HashMap<Integer,ArrayList<ACLrule>> hmap=aclfortreevlan.get(iname).getPermitout().get(s);
							    	 if(!hmap.containsKey(rulename+1)){//if the rule's name is aready in the port
							    		  ArrayList<ACLrule> nlist=new ArrayList<ACLrule>();
								    	  nlist.add(aclrule); 
								    	  hmap.put(rulename+1,nlist);
							    	 }else{
							    		 hmap.get(rulename+1).add(aclrule);
							    	 }				
								}	
							}	
						}
					}		
				}
				}
				rulename++;
				String s=String.valueOf(rulename);
				Rule rule=new Rule(portname,name,head,tail,toadd,s);//build a new rule under all the ports
				aclfortreevlan.get(iname).getUniverhashmapvlan().put(rulename,rule);
					
				
				vlanrule.add(rule);
				}
			    vlanpremap.get(iname).getRuleset().put(toadd, vlanrule);
			    vlanpremap.get(iname).setDevicename(name);
			    ArrayList<String> alist=new ArrayList<String>(vlan_ports.get(iname));
			    vlanpremap.get(iname).setPortname(alist);
			}
			
			return vlanpremap;
	
	}

	/**
	 * bdd for each rule is computed
	 * @param fws
	 * @return
	 */
	public HashMap<String, Integer> getfwdbdds2(ArrayList<ForwardingRule> fws)
	{
		int alreadyfwded = BDDFalse;

		HashMap<String, Integer> fwdbdds = new HashMap<String, Integer>();
		int longestmatch = 32;
		for(int i = longestmatch; i >=0; i --)
		{
			for(int j = 0; j < fws.size(); j ++)
			{
				ForwardingRule onefw = fws.get(j);
				if(onefw.getprefixlen() == i)
				{
					String iname = onefw.getiname();
					//int[] ipbin = Utility.CalBinRep(onefw.getdestip(), ipBits);
					//int[] ipbinprefix = new int[onefw.getprefixlen()];
					//for(int k = 0; k < onefw.getprefixlen(); k ++)
					//{
					//	ipbinprefix[k] = ipbin[k + ipBits - onefw.getprefixlen()];
					//}
					//int entrybdd = EncodePrefix(ipbinprefix, dstIP, ipBits);
					int entrybdd = onefw.getBDDRep();
					int notalreadyfwded = aclBDD.not(alreadyfwded);
					aclBDD.ref(notalreadyfwded);
					int toadd = aclBDD.and(entrybdd, notalreadyfwded);
					aclBDD.ref(toadd);
					aclBDD.deref(notalreadyfwded);
					int altmp = aclBDD.or(alreadyfwded, entrybdd);
					aclBDD.ref(altmp);
					aclBDD.deref(alreadyfwded);
					alreadyfwded = altmp;

					if(fwdbdds.containsKey(iname))
					{
						int oldkey = fwdbdds.get(iname);
						int newkey = aclBDD.or(toadd, oldkey);
						aclBDD.ref(newkey);
						aclBDD.deref(toadd);
						aclBDD.deref(oldkey);
						fwdbdds.put(iname, newkey);
					}else
					{
						fwdbdds.put(iname, toadd);
					}
				}
			}
		}
		aclBDD.deref(alreadyfwded);
		return fwdbdds;
	}

	public int encodeSrcIPPrefix(long ipaddr, int prefixlen)
	{
		int[] ipbin = Utility.CalBinRep(ipaddr, ipBits);
		int[] ipbinprefix = new int[prefixlen];
		for(int k = 0; k < prefixlen; k ++)
		{
			ipbinprefix[k] = ipbin[k + ipBits - prefixlen];
		}
		int entrybdd = EncodePrefix(ipbinprefix, srcIP, ipBits);
		return entrybdd;
	}

	//1 means bigger,0 means smaller,2 means equal 
	public int firstisbigger(int[] first,int[] second)
	{
		for(int i=ipBits-1;i>=0;i--){
			if(first[i]!=second[i])
			{
				if (first[i]==1){
					return 1;
				}else{return 0;}
			}			
		}
		return 2;
	}
	
	public boolean contain(Rule rule1,Rule rule2){//rule1 contains rule2
		
		int head=firstisbigger(rule1.getHead(),rule2.getHead());
		int tail=firstisbigger(rule1.getTail(),rule2.getTail());
		if(head==0&&tail==1){
			return true;
		}
		return false;
	}

	
	public int encodeDstIPPrefix(long ipaddr, int prefixlen)
	{
		int[] ipbin = Utility.CalBinRep(ipaddr, ipBits);//转换为2进制数组,ipbin[0]为2进制数的最低位
		int[] ipbinprefix = new int[prefixlen];
		for(int k = 0; k < prefixlen; k ++)
		{
			ipbinprefix[k] = ipbin[k + ipBits - prefixlen];//取出只取prefixlen那么长
		}
		int entrybdd = EncodePrefix(ipbinprefix, dstIP, ipBits);
		return entrybdd;
	}
	
	
	public int encodeDstIPPrefix1(long ipaddr, int prefixlen, int[] head, int[] tail)
	{
		int[] ipbin = Utility.CalBinRep(ipaddr, ipBits);//转换为2进制数组,ipbin[0]为2进制数的最低位
	    for(int i=0;i<ipBits;i++){
	    	if(i<(ipBits-prefixlen)){
	    		head[i]=0;
	    		tail[i]=1;
	    	}else{	
	    	head[i]=ipbin[i];
	    	tail[i]=ipbin[i];
	    	}
	    }
		
		
		int[] ipbinprefix = new int[prefixlen];
		for(int k = 0; k < prefixlen; k ++)
		{
			ipbinprefix[k] = ipbin[k + ipBits - prefixlen];//取出只取prefixlen那么长
		}
		int entrybdd = EncodePrefix(ipbinprefix, dstIP, ipBits);
		return entrybdd;
	}

	public void multipleref(int bddnode, int reftimes)
	{
		for(int i = 0; i < reftimes; i ++)
		{
			aclBDD.ref(bddnode);
		}
	}

	/**
	 * 
	 * @param entrybdd
	 * @return the set of fwdbdds which might be changed
	 */
	public HashSet<String> getDependencySet(ForwardingRule fwdr, HashMap<String, Integer> fwdbdds)
	{
		HashSet<String> ports = new HashSet<String>();
		int entrybdd = fwdr.getBDDRep();
		if(fwdbdds.keySet().contains(fwdr.getiname()))
		{
			int onebdd = fwdbdds.get(fwdr.getiname());
			if(entrybdd == aclBDD.and(entrybdd, onebdd))
			{
				return ports;
			}else
			{
				ports.add(fwdr.getiname());
				for(String port : fwdbdds.keySet())
				{
					if(!port.equals(fwdr.getiname()))
					{
						onebdd = fwdbdds.get(port);
						if(BDDFalse != aclBDD.and(entrybdd, onebdd))
						{
							ports.add(port);
						}
					}
				}
			}
		}else
		{
			ports.add(fwdr.getiname());
			for(String port : fwdbdds.keySet())
			{
				int onebdd = fwdbdds.get(port);
				if(BDDFalse != aclBDD.and(entrybdd, onebdd))
				{
					ports.add(port);
				}
			}
		}

		return ports;
	}

	public int getlongP(ForwardingRule onefw, ArrayList<ForwardingRule> fws)
	{
		int longP = BDDFalse;

		for(ForwardingRule of : fws)
		{
			if(onefw.getprefixlen() <= of.getprefixlen())
			{
				int tmp = aclBDD.or(longP, of.getBDDRep());
				aclBDD.deref(longP);
				aclBDD.ref(tmp);
				longP = tmp;
			}
		}

		return longP;
	}

	/**
	 * 
	 * @param subs - has ip information
	 * @param rawBDD
	 * @param reftimes -  the res need to be referenced for several times
	 * @return
	 */
	public int encodeACLin(ArrayList<Subnet> subs, int rawBDD, int reftimes)
	{
		// dest ip
		if(subs == null)
		{
			multipleref(rawBDD, reftimes);
			return rawBDD;
		}
		int destipbdd = encodeDstIPPrefixs(subs);
		int notdestip = aclBDD.not(destipbdd);
		aclBDD.ref(notdestip);
		int res = aclBDD.or(notdestip, rawBDD);

		multipleref(res, reftimes);
		aclBDD.deref(destipbdd);
		aclBDD.deref(notdestip);
		return res;
	}


	public int encodeACLout(ArrayList<Subnet> subs, int rawBDD, int reftimes)
	{
		if(subs == null)
		{
			multipleref(rawBDD, reftimes);
			return rawBDD;
		}
		// src ip
		int srcipbdd = encodeSrcIPPrefixs(subs);
		int notsrctip = aclBDD.not(srcipbdd);
		aclBDD.ref(notsrctip);
		int res = aclBDD.or(notsrctip, rawBDD);

		multipleref(res, reftimes);
		aclBDD.deref(srcipbdd);
		aclBDD.deref(notsrctip);
		return res;
	}

	public int encodeDstIPPrefixs(ArrayList<Subnet> subs)
	{
		int res = BDDFalse;
		for(int i = 0; i < subs.size(); i ++)
		{
			int[] head=new int[ipBits];
			int[] tail=new int[ipBits];
			Subnet onesub = subs.get(i);
			int dstipbdd = encodeDstIPPrefix(onesub.getipaddr(), onesub.getprefixlen());
			int tmp = aclBDD.or(res, dstipbdd);
			aclBDD.ref(tmp);
			aclBDD.deref(res);
			aclBDD.deref(dstipbdd);
			res = tmp;
		}
		return res;
	}

	public int encodeSrcIPPrefixs(ArrayList<Subnet> subs)
	{
		int res = BDDFalse;
		for(int i = 0; i < subs.size(); i ++)
		{
			Subnet onesub = subs.get(i);
			int srcipbdd = encodeSrcIPPrefix(onesub.getipaddr(), onesub.getprefixlen());
			int tmp = aclBDD.or(res, srcipbdd);
			aclBDD.ref(tmp);
			aclBDD.deref(res);
			aclBDD.deref(srcipbdd);
			res = tmp;
		}
		return res;
	}
	/**
	 * 
	 * @return the size of bdd (in bytes)
	 */
	public long BDDSize()
	{
		return aclBDD.getMemoryUsage();
	}

	private void DeclareVars(int[] vars, int bits)
	{
		for(int i = bits - 1; i >=0; i --)
		{
			vars[i] = aclBDD.createVar();
		}
	}

	//protocol is 8 bits
	private void DeclareProtocol()
	{
		DeclareVars(protocol, protocolBits);
	}

	private void DeclareSrcPort()
	{
		DeclareVars(srcPort, portBits);
	}

	private void DeclareDstPort()
	{
		DeclareVars(dstPort, portBits);
	}

	private void DeclareSrcIP()
	{
		DeclareVars(srcIP, ipBits);
	}

	private void DeclareDstIP()
	{
		DeclareVars(dstIP, ipBits);
	}

	/**
	 * @param vars - a list of bdd nodes that we do not need anymore
	 */
	public void DerefInBatch(int[] vars)
	{
		for(int i = 0; i < vars.length; i ++)
		{
			aclBDD.deref(vars[i]);
		}
	}

	/**
	 * 
	 * @param acls - the acl that needs to be transformed to bdd
	 * @return a bdd node that represents the acl
	 */
	public ArrayList<ArrayList<Integer>> ConvertACLs(LinkedList<ACLRule> acls)
	{   
		ArrayList<ArrayList<Integer>> result=new ArrayList<ArrayList<Integer>>();//3 element, 0 is permit, 1 is deny, 2 is the old
		/*
		if(acls.size() == 0)
		{
			// no need to ref the false node
			return BDDFalse;
		}
		*/
		int res = BDDFalse;
		int denyBuffer = BDDFalse;
		int denyBufferNot = BDDTrue;
		
		for(int i=0;i<3;i++){
		ArrayList<Integer> initlist=new ArrayList<Integer>();
		result.add(initlist);
		}//initial
		
		for(int i = 0; i < acls.size(); i ++)
		{   
			ACLRule acl = acls.get(i);
			// g has been referenced
			int g = ConvertACLRule(acl);
           
			if(PacketSet.CheckPermit(acl))
			{
				if(res == BDDFalse)
				{
					if(denyBuffer == BDDFalse)
					{   
						res = g;
						result.get(0).add(g);
						aclBDD.ref(g);
					}else
					{
						int tempnode = aclBDD.and(g, denyBufferNot);
						//int res=aclBDD.and(g, denyBufferNot);
						result.get(0).add(tempnode);
						aclBDD.ref(tempnode);
						res = tempnode;
						aclBDD.deref(g);			
					}
				}else
				{
					if(denyBuffer == BDDFalse)
					{
						// just combine the current res and g
						int tempnode = aclBDD.or(res, g);
						int output=aclBDD.and(g, aclBDD.not(res));
						
						aclBDD.ref(tempnode);
						result.get(0).add(output);
						aclBDD.ref(output);
						
						DerefInBatch(new int[]{res, g});
						res = tempnode;
					}else
					{
						//the general case
						int tempnode = aclBDD.and(g, denyBufferNot);									
						aclBDD.ref(tempnode);				
						aclBDD.deref(g);				
						int output=aclBDD.and(tempnode, aclBDD.not(res));
						result.get(0).add(output);
						aclBDD.ref(output);
						int tempnode2 = aclBDD.or(res, tempnode);
						aclBDD.ref(tempnode2);
						DerefInBatch(new int[]{res, tempnode});
						res = tempnode2;
				
				
		
					}
				}

			}else
			{
				/**
				 * combine to the denyBuffer
				 */
				if(denyBuffer == BDDFalse)
				{
					denyBuffer = g;
					denyBufferNot = aclBDD.not(g);
					aclBDD.ref(denyBufferNot);
					
					int output=aclBDD.and(g, aclBDD.not(res));
					result.get(1).add(output);
				    aclBDD.ref(output);
					
				//	aclBDD.numberOfVariables();
					
				}else
				{
					int tempnode = aclBDD.or(denyBuffer, g);
					aclBDD.ref(tempnode);
					int output0=aclBDD.and(g,denyBufferNot);
					int output1=aclBDD.and(output0,aclBDD.not(res));
					aclBDD.ref(output1);
					result.get(1).add(output1);
					
					DerefInBatch(new int[]{denyBuffer, g});
					denyBuffer = tempnode;

					aclBDD.deref(denyBufferNot);
					denyBufferNot = aclBDD.not(denyBuffer);
					aclBDD.ref(denyBufferNot);
				}
			}
			//System.out.println(acl);
			//System.out.println(res);
		}
		/**
		 * we need to de-ref denyBuffer, denyBufferNot
		 */
		//DerefInBatch(new int[]{denyBuffer, denyBufferNot});
		aclBDD.deref(denyBufferNot);
	    result.get(0).add(res);//permit
		result.get(1).add(aclBDD.and(denyBuffer, aclBDD.not(res)));//deny
		//aclBDD.deref(denyBuffer);
		result.get(2).add(res);
		return result;
	}


	/**
	 * 
	 * @param aclr - an acl rule
	 * @return a bdd node representing this rule
	 */
	public int ConvertACLRule(ACLRule aclr)
	{	
		/**
		 *  protocol
		 */
		// no need to ref the true node
		int protocolNode = BDDTrue;
		if(aclr.protocolLower == null || 
				aclr.protocolLower.equalsIgnoreCase("any"))
		{
			//do nothing, just a shortcut
		}else{
			Range r = PacketSet.convertProtocolToRange
					(aclr.protocolLower, aclr.protocolUpper);
			protocolNode = ConvertProtocol(r);
		}

		/**
		 * src port
		 */
		int srcPortNode = BDDTrue;
		if(aclr.sourcePortLower == null ||
				aclr.sourcePortLower.equalsIgnoreCase("any"))
		{
			//do nothing, just a shortcut
		}else{
			Range r = PacketSet.convertPortToRange(aclr.sourcePortLower, 
					aclr.sourcePortUpper);
			srcPortNode = ConvertSrcPort(r);
		}

		/**
		 * dst port
		 */
		int dstPortNode = BDDTrue;
		if(aclr.destinationPortLower == null ||
				aclr.destinationPortLower.equalsIgnoreCase("any"))
		{
			// do nothing, just a shortcut
		}else{
			Range r = PacketSet.convertPortToRange(aclr.destinationPortLower, 
					aclr.destinationPortUpper);
			dstPortNode = ConvertDstPort(r);
		}

		/**
		 * src IP
		 */
		int srcIPNode = ConvertIPAddress(aclr.source, aclr.sourceWildcard, srcIP);

		/**
		 * dst IP
		 */
		int dstIPNode = ConvertIPAddress(aclr.destination, 
				aclr.destinationWildcard, dstIP);

		//put them together
		int [] fiveFields = {protocolNode,srcPortNode,dstPortNode,
				srcIPNode,dstIPNode};
		int tempnode = AndInBatch(fiveFields);
		//clean up internal nodes
		DerefInBatch(fiveFields);

		return tempnode;
	}

	/**
	 * @param bddnodes - an array of bdd nodes
	 * @return - the bdd node which is the AND of all input nodes
	 * all temporary nodes are de-referenced. 
	 * the input nodes are not de-referenced.
	 */
	public int AndInBatch(int [] bddnodes)
	{
		int tempnode = BDDTrue;
		for(int i = 0; i < bddnodes.length; i ++)
		{
			if(i == 0)
			{
				tempnode = bddnodes[i];
				aclBDD.ref(tempnode);
			}else
			{
				if(bddnodes[i] == BDDTrue)
				{
					// short cut, TRUE does not affect anything
					continue;
				}
				if(bddnodes[i] == BDDFalse)
				{
					// short cut, once FALSE, the result is false
					// the current tempnode is useless now
					aclBDD.deref(tempnode);
					tempnode = BDDFalse; 
					break;
				}
				int tempnode2 = aclBDD.and(tempnode, bddnodes[i]);
				aclBDD.ref(tempnode2);
				// do not need current tempnode 
				aclBDD.deref(tempnode);
				//refresh
				tempnode = tempnode2;
			}
		}
		return tempnode;
	}

	/**
	 * @param bddnodes - an array of bdd nodes
	 * @return - the bdd node which is the OR of all input nodes
	 * all temporary nodes are de-referenced. 
	 * the input nodes are not de-referenced.
	 */
	public int OrInBatch(int [] bddnodes)
	{
		int tempnode = BDDFalse;
		for(int i = 0; i < bddnodes.length; i ++)
		{
			if(i == 0)
			{
				tempnode = bddnodes[i];
				aclBDD.ref(tempnode);
			}else
			{
				if(bddnodes[i] == BDDFalse)
				{
					// short cut, FALSE does not affect anything
					continue;
				}
				if(bddnodes[i] == BDDTrue)
				{
					// short cut, once TRUE, the result is true
					// the current tempnode is useless now
					aclBDD.deref(tempnode);
					tempnode = BDDTrue; 
					break;
				}
				int tempnode2 = aclBDD.or(tempnode, bddnodes[i]);
				aclBDD.ref(tempnode2);
				// do not need current tempnode 
				aclBDD.deref(tempnode);
				//refresh
				tempnode = tempnode2;
			}
		}
		return tempnode;
	}


	/**
	 * @param ip address and mask
	 * @return the corresponding bdd node
	 */
	protected int ConvertIPAddress(String IP, String Mask, int[] vars)
	{
		int tempnode = BDDTrue;
		// case 1 IP = any
		if(IP == null || IP.equalsIgnoreCase("any"))
		{
			// return TRUE node
			return tempnode;
		}

		// binary representation of IP address
		int[] ipbin = Utility.IPBinRep(IP);
		// case 2 Mask = null
		if(Mask == null)
		{
			// no mask is working
			return EncodePrefix(ipbin, vars, ipBits);
		}else{
			int [] maskbin = Utility.IPBinRep(Mask);
			int numMasked = Utility.NumofNonZeros(maskbin);

			int [] prefix = new int[maskbin.length - numMasked];
			int [] varsUsed = new int[prefix.length];
			int ind = 0;
			for(int i = 0; i < maskbin.length; i ++)
			{
				if(maskbin[i] == 0)
				{
					prefix[ind] = ipbin[i];
					varsUsed[ind] = vars[i];
					ind ++;
				}
			}

			return EncodePrefix(prefix, varsUsed, prefix.length);
		}

	}

	/***
	 * convert a range of protocol numbers to a bdd representation
	 */
	protected int ConvertProtocol(Range r)
	{
		return ConvertRange(r, protocol, protocolBits);

	}

	/**
	 * convert a range of source port numbers to a bdd representation
	 */
	protected int ConvertSrcPort(Range r)
	{
		return ConvertRange(r, srcPort, portBits);
	}

	/**
	 * convert a range of destination port numbers to a bdd representation
	 */
	protected int ConvertDstPort(Range r)
	{
		return ConvertRange(r, dstPort, portBits);
	}

	/**
	 * 
	 * @param r - the range
	 * @param vars - bdd variables used
	 * @param bits - number of bits in the representation
	 * @return the corresponding bdd node
	 */
	private int ConvertRange(Range r, int [] vars, int bits)
	{

		LinkedList<int []> prefix = Utility.DecomposeInterval(r, bits);
		//System.out.println(vars.length);
		if(prefix.size() == 0)
		{
			return BDDTrue;
		}

		int tempnode = BDDTrue;
		for(int i = 0; i < prefix.size(); i ++)
		{
			if(i == 0)
			{
				tempnode = EncodePrefix(prefix.get(i), vars, bits);
			}else
			{
				int tempnode2 = EncodePrefix(prefix.get(i), vars, bits);
				int tempnode3 = aclBDD.or(tempnode, tempnode2);
				aclBDD.ref(tempnode3);
				DerefInBatch(new int[]{tempnode, tempnode2});
				tempnode = tempnode3;
			}
		}
		return tempnode;
	}

	/**
	 * 
	 * @param prefix - 
	 * @param vars - bdd variables used
	 * @param bits - number of bits in the representation
	 * @return a bdd node representing the predicate
	 * e.g. for protocl, bits = 8, prefix = {1,0,1,0}, so the predicate is protocol[4] 
	 * and (not protocol[5]) and protocol[6] and (not protocol[7])
	 */
	private int EncodePrefix(int [] prefix, int[] vars, int bits)
	{
		if(prefix.length == 0)
		{
			return BDDTrue;
		}

		int tempnode = BDDTrue;
		for(int i = 0; i < prefix.length; i ++)
		{
			if(i == 0){
				tempnode = EncodingVar(vars[bits - prefix.length + i], prefix[i]);
			}else
			{
				int tempnode2 = EncodingVar(vars[bits - prefix.length + i], prefix[i]);
				int tempnode3 = aclBDD.and(tempnode, tempnode2);
				aclBDD.ref(tempnode3);
				//do not need tempnode2, tempnode now
				//aclBDD.deref(tempnode2);
				//aclBDD.deref(tempnode);
				DerefInBatch(new int[]{tempnode, tempnode2});
				//refresh tempnode 3
				tempnode = tempnode3;
			}
		}
		return tempnode;
	}

	/***
	 * return a bdd node representing the predicate on the protocol field
	 */
	private int EncodeProtocolPrefix(int [] prefix)
	{
		return EncodePrefix(prefix, protocol, protocolBits);
	}

	/**
	 * print out a graph for the bdd node var
	 */
	public void PrintVar(int var)
	{
		if(aclBDD.isValid(var))
		{
			aclBDD.printDot(Integer.toString(var), var);
			System.out.println("BDD node " + var + " printed.");
		}else
		{
			System.err.println(var + " is not a valid BDD node!");
		}
	}

	/**
	 * return the size of the bdd tree
	 */
	public int getNodeSize(int bddnode)
	{
		int size = aclBDD.nodeCount(bddnode);
		if(size == 0)
		{// this means that it is only a terminal node
			size ++;
		}
		return size;
	}

	/*
	 * cleanup the bdd after usage
	 */
	public void CleanUp()
	{
		aclBDD.cleanup();
	}

	/***
	 * var is a BDD variable
	 * if flag == 1, return var
	 * if flag == 0, return not var, the new bdd node is referenced.
	 */
	private int EncodingVar(int var, int flag)
	{
		if (flag == 0)
		{
			int tempnode = aclBDD.not(var);
			// no need to ref the negation of a variable.
			// the ref count is already set to maximal
			//aclBDD.ref(tempnode);
			return tempnode;
		}
		if (flag == 1)
		{
			return var;
		}

		//should not reach here
		System.err.println("flag can only be 0 or 1!");
		return -1;
	}

	public static void main(String[] args) throws IOException
	{

		/****
		 * it is to test the translation from an interval of port numbers to bdd
		 */
		/*
		BDDPacketSet bps = new BDDPacketSet();
		int var = bps.ConvertProtocol(new Range(16,31));
		bps.PrintVar(var);
		bps.CleanUp();
		 */

		/**
		 * test whether translate IP address correctly
		 */

		/*
		BDDPacketSet bps = new BDDPacketSet();
		int var = bps.ConvertIPAddress("192.192.200.2", "0.0.255.255", bps.srcIP);
		bps.PrintVar(var);
		bps.CleanUp();
		 */

		/**
		 * test conversion of an inverval
		 */
		/*
		BDDACLWrapper bps = new BDDACLWrapper();
		Range r = new Range(123,123);
		int bddnode = bps.ConvertDstPort(r);
		bps.PrintVar(bddnode);
		 */

		/**
		 * compare packet set conversion and bdd conversion
		 * this is the problem. actually you cannot convert the following things to a single range.
		 */

		/*
		Range r1 = PacketSet.convertIPtoIntegerRange("0.0.0.0", "255.255.255.0");
		System.out.println(r1);

		Range r2 = PacketSet.convertIPtoIntegerRange("0.0.0.255", "255.255.255.0");
		System.out.println(r2);
		 */

		/******************
		 * test conversion for one rule
		 */		

		/*
		NetworkConfig net = ParseTools.LoadNetwork("purdue.ser");
		Hashtable<String, RouterConfig> routers = net.tableOfRouters;
		RouterConfig aRouter = routers.get("config822");
		Hashtable<String, InterfaceConfig> interfaces = aRouter.tableOfInterfaceByNames;
		InterfaceConfig aInterface = interfaces.get("Vlan1000");
		String aclName = aInterface.outFilters.get(0);
		System.out.println(aclName);

		Hashtable<String, LinkedList<ACLRule>> tACLs = aRouter.tableOfACLs;
		LinkedList<ACLRule> acl = tACLs.get(aclName);
		acl.get(0).permitDeny = "deny";
		acl.get(3).permitDeny = "deny";
		acl.get(8).permitDeny = "deny";
		for(int i = 0; i < acl.size(); i ++)
		{
			System.out.println(acl.get(i));
		}

		BDDACLWrapper bps = new BDDACLWrapper();
		int var = bps.ConvertACLs(acl);
		bps.PrintVar(var);
		bps.CleanUp();
		 */

		/***
		 * check the size of a single acl rule
		 */
		BDDACLWrapper bps = new BDDACLWrapper();
		
	//	System.out.println(bps.encodeDstIPPrefix(0, 0));

		/*
		StoreACL sa = StoreACL.LoadNetwork("purdue-ACLs.ser");
		LinkedList<LinkedList<ACLRule>> acllists = sa.ACLList;


		for(int i = 0; i < acllists.size(); i ++)
		{
			LinkedList<ACLRule> acls = acllists.get(i);
			for(int j = 0; j < acls.size(); j ++)
			{
				ACLRule aclr = acls.get(j);
				int aclrnode = bps.ConvertACLRule(aclr);
				System.out.println(bps.getNodeSize(aclrnode));
			}
		}
		 */

		/**
		 * test group function
		 */
		/*
		ACLRule acl0 = new ACLRule();
		acl0.permitDeny = "deny";
		ACLRule acl1 = new ACLRule();
		acl1.permitDeny = "deny";
		ACLRule acl2 = new ACLRule();
		acl2.permitDeny = "deny";
		ACLRule acl3 = new ACLRule();
		acl3.permitDeny = "deny";
		ACLRule acl4 = new ACLRule();
		acl4.permitDeny = "deny";

		LinkedList<ACLRule> acls = new LinkedList<ACLRule>();
		acls.add(acl0);
		acls.add(acl1);
		acls.add(acl2);
		acls.add(acl3);
		acls.add(acl4);

		LinkedList<int[]> grouped = BDDPacketSet.GroupACLRules(acls);

		for(int i = 0; i < grouped.size(); i ++)
		{
			System.out.println(grouped.get(i)[0] + " " + grouped.get(i)[1]);
		}
		 */

		/**
		 * integer division...
		 */
		//System.out.println(3/2);

		/**
		 * test andinbatch and orinbatch
		 */
		/*
		BDDPacketSet bps = new BDDPacketSet();
		int var1 = bps.OrInBatch(new int[]{0,1,0});
		bps.PrintVar(var1);
		int var2 = bps.AndInBatch(new int[]{1,1,1});
		bps.PrintVar(var2);
		bps.CleanUp();
		 */
	}
}
