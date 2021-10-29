package StaticReachabilityAnalysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;


import jdd.bdd.BDD;

/**
 * basically adapted from PacketSetAP.java
 * @author carmo
 *
 */
public class BDDAP implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8496681632964454915L;
	StoreACL sa;
	// each integer is a BDD node, denotes the acl
	// aclid -> aclbdd
	ArrayList<Integer> ACLBDDREP;
	// aclid -> {apbdd1, apbdd2, ...}
	ArrayList<HashSet<Integer>> ACLAPREP;
	// apid -> apbdd
	ArrayList<Integer> AP;
	// for each ap, store the set of acls whose expression has this ap, use the index in ACLAPREP to identify the acl
	// apid -> {aclid1, aclid2, ...}
	ArrayList<HashSet<Integer>> APREF;
	int emptyapid;
	// apbdd -> apid
	HashMap<Integer, Integer> APMAP;
	BDDACLWrapper bddengine;


	public BDDAP(String filename)
	{
		//the file is for sa
		sa = StoreACL.LoadNetwork(filename);

		ACLBDDREP = new ArrayList<Integer>();
		AP = new ArrayList<Integer>();

		bddengine = new BDDACLWrapper();
	}
	
	public BDDACLWrapper getBDDEngine()
	{
		return bddengine;
	}
	
	public ArrayList<Integer> getPredicates()
	{
		return ACLBDDREP;
	}

	/**
	 * get number of aps
	 */
	public long getAPNum()
	{
		return AP.size();
	}

	public StoreACL getACLInfo()
	{
		return sa;
	}

	/**
	 * calculate the bdd representation for each acl
	 */
	public void CalBDDForACL()
	{
		
		for(int i = 0; i < sa.ACLList.size(); i ++)
		{
			//System.out.println(i);
			
			long t1 = System.nanoTime();
			int bddnode = bddengine.ConvertACLs(sa.ACLList.get(i));
			long t2 = System.nanoTime();
			
			System.out.println(sa.ACLList.get(i).size()+" " + (t2 - t1));
			
			ACLBDDREP.add(bddnode);
		}
		System.out.println("BDD for ACLs Computed.");
	}

	/**
	 * add one acl and recompute aps 
	 * @param ind - the bddnode for the ind-th ACL 
	 */
	private void AddOneACL(int ind)
	{

		BDD thebdd = bddengine.aclBDD;

		int bddToAdd = ACLBDDREP.get(ind);
		int bddToAddNeg = thebdd.not(bddToAdd);
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
			ArrayList<Integer> oldList = AP;
			int oldNum = AP.size();
			// set up a new list
			AP = new ArrayList<Integer>();
			for(int i = 0; i < oldNum; i ++)
			{
				int oldap = oldList.get(i);

				int tmps = thebdd.and(bddToAdd, oldap);
				thebdd.ref(tmps);
				if(tmps != BDDACLWrapper.BDDFalse)
				{
					AP.add(tmps);
				}

				tmps = thebdd.and(bddToAddNeg, oldap);
				thebdd.ref(tmps);
				if(tmps != BDDACLWrapper.BDDFalse)
				{
					AP.add(tmps);
				}
			}
			/**
			 * in this case, we need to de-ref useless nodes.
			 * we still keep bddToAdd, since it is the bdd node for an acl
			 * we will de-ref:
			 * bddToAddNeg, the whole list of oldList.
			 */
			int [] toDeRef = new int[oldList.size() + 1];
			for(int i = 0; i < oldList.size(); i ++)
			{
				toDeRef[i] = oldList.get(i);
			}
			toDeRef[oldList.size()] = bddToAddNeg;
			bddengine.DerefInBatch(toDeRef);
		}

	}// end of add one acl

	/**
	 * Adapt from AtomicPredicate.CalAPs
	 * @param index - the order of combining ACLs
	 */
	public void CalAPs(int[] index)
	{
		AP.clear();
		if(index.length != ACLBDDREP.size())
		{
			System.err.println("The size of indexes does not match" +
					"the number of ACLs!");
			return;
		}
		for(int i = 0; i < index.length; i ++)
		{
			AddOneACL(index[i]);
			System.out.println(AP.size());
		}
	}
	
	public void CalAPs(String orderfile) throws FileNotFoundException
	{
		AP.clear();
		int[] order = new int[sa.getACLNum()];
		System.out.println("read in " + orderfile);
		APTools.ReadOrder(new File(orderfile), order);
		
		if(order.length != ACLBDDREP.size())
		{
			System.err.println("The size of indexes does not match" +
					"the number of ACLs!");
			return;
		}
		long start = System.nanoTime();
		for(int i = 0; i < order.length; i ++)
		{
			//System.out.println("add " + order[i]);
			AddOneACL(order[i]);
			System.out.println(AP.size());
		}
		long end = System.nanoTime();
		System.out.println(end - start);
	}

	/**
	 * Adapt from AtomicPredicate.CalAPs
	 * @param index - the order of combining ACLs
	 * measure the time of combining the last acl and return
	 */
	public double CalAPsTest(int[] index)
	{
		AP.clear();
		if(index.length != ACLBDDREP.size())
		{
			System.err.println("The size of indexes does not match" +
					"the number of ACLs!");
			return -1;
		}
		long start = 0;
		long end = 0;
		for(int i = 0; i < index.length; i ++)
		{
			if(i == index.length - 1)
			{
				start = System.nanoTime();
				AddOneACL(index[i]);
				end = System.nanoTime();
			}else
			{
				AddOneACL(index[i]);
			}
			//System.out.println("ACL Num: " + i + " AP Num:" + AP.size());
		}
		return (end - start)/1000000.0;
	}

	/**
	 * 
	 * @return an index list, the last one is lastind
	 */
	public int[] getIndexes(int lastind)
	{
		int [] index = new int[ACLBDDREP.size()];
		// no special order
		for(int i = 0; i < index.length; i ++)
		{
			index[i] = i;
		}

		index[lastind] = index.length - 1;
		index[index.length - 1] = lastind;

		return index;
	}

	/**
	 * test time to add an acl
	 */
	public void AddTest()
	{
		for(int i = 0; i < ACLBDDREP.size(); i ++)
		{
			int [] index = getIndexes(i);
			double t = CalAPsTest(index);
			System.out.println(t);
		}
	}

	/**
	 * just add ACLs following the order of ACL storage
	 */
	public long SimpleTest()
	{
		int [] index = new int[ACLBDDREP.size()];
		// no special order
		Sample.GetSample(ACLBDDREP.size(), ACLBDDREP.size(), index);
		long start = System.nanoTime();
		CalAPs(index);
		long end = System.nanoTime();
		System.out.println("# of ACLs " + index.length);
		System.out.println("# of APs " + AP.size());
		System.out.println("Time: " + (end - start)/1000000.0 + "ms");
		// memory (in bytes)
		long memUsage = bddengine.BDDSize();
		System.out.println("Memory Use for ACL and Atomic Predicates: " + 
				memUsage + " bytes");
		return end - start;
	}

	/**
	 * randomly choose index and find the quickest way to 
	 * @return
	 */
	public long findfastestorder(int tries)
	{

		long besttime = 0;
		for(int j = 0; j < tries; j ++)
		{
			int [] index = new int[ACLBDDREP.size()];
			Sample.GetSample(ACLBDDREP.size(), ACLBDDREP.size(), index);
			long start = System.nanoTime();
			CalAPs(index);
			long end = System.nanoTime();
			if(j == 0)
			{
				besttime = end - start;
			}else
			{
				if((end - start) < besttime)
				{
					besttime = end - start;
				}
			}
		}

		return besttime;

	}

	public boolean SaveBDDAP()
	{

		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			fos = new FileOutputStream(sa.NetworkName + "-BDDAP.ser");
			out = new ObjectOutputStream(fos);
			out.writeObject(this);
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			return false;
		}

		System.out.println("BDD Atomic Predicates from Network " + sa.NetworkName + " is saved.");
		return true;
	}

	public static BDDAP LoadBDDAP(String filename)
	{

		FileInputStream fis = null;
		ObjectInputStream in = null;
		BDDAP bap = null;
		try
		{
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			bap = (BDDAP)in.readObject();
			in.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		catch(ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		System.out.println("BDD Atomic Predicates from " + filename + " is loaded.");
		return bap;
	}

	/**
	 * 
	 * @param routername
	 * @param aclname
	 * @return if the acl is true or force, return the set containing the acl itself;
	 *         otherwise, return an ap expression
	 */
	public HashSet<Integer> getAPExp(String routername, String aclname)
	{
		HashSet<Integer> apexp = new HashSet<Integer> ();
		//assume that the acl is used in some interface
		int apid =  sa.ACLMap.get(routername).get(aclname);
		int aclbdd = ACLBDDREP.get(apid);
		// get the expression
		if(aclbdd == BDDACLWrapper.BDDFalse || aclbdd == BDDACLWrapper.BDDTrue)
		{
			apexp.add(aclbdd);
			return apexp;
		}

		for(int i = 0; i < AP.size(); i ++)
		{
			int oneap = AP.get(i);

			if(bddengine.aclBDD.and(oneap, aclbdd) != BDDACLWrapper.BDDFalse)
			{
				apexp.add(oneap);
			}
		}
		return apexp;
	}

	public HashSet<Integer> getAPExp(int aclid)
	{
		HashSet<Integer> apexp = new HashSet<Integer> ();

		int aclbdd = ACLBDDREP.get(aclid);
		// get the expression
		if(aclbdd == BDDACLWrapper.BDDFalse || aclbdd == BDDACLWrapper.BDDTrue)
		{
			apexp.add(aclbdd);
			return apexp;
		}

		for(int i = 0; i < AP.size(); i ++)
		{
			int oneap = AP.get(i);

			if(bddengine.aclBDD.and(oneap, aclbdd) != BDDACLWrapper.BDDFalse)
			{
				apexp.add(oneap);
			}
		}
		return apexp;
	}

	/**
	 * calculate ACLAPREP
	 * for true or false, we do not assign the expression
	 */
	public void CalAPExp()
	{
		ACLAPREP = new ArrayList<HashSet<Integer>>();
		for(int i = 0; i < ACLBDDREP.size(); i ++)
		{
			int aclbdd = ACLBDDREP.get(i);
			if(aclbdd == BDDACLWrapper.BDDFalse || aclbdd == BDDACLWrapper.BDDTrue)
			{
				ACLAPREP.add(null);
			}else
			{
				ACLAPREP.add(getAPExp(i));
			}
		}
		System.out.println("AP expression for each acl is computed.");
	}

	/**
	 * calculate APREF
	 * should be called after CalAPExp
	 */
	public void CalAPREF()
	{
		APREF = new ArrayList<HashSet<Integer>> ();
		APMAP = new HashMap<Integer, Integer>();
		for(int i = 0; i < AP.size(); i ++)
		{
			APMAP.put(AP.get(i), i);
			APREF.add(new HashSet<Integer>());
		}

		for(int i = 0; i < ACLBDDREP.size(); i ++)
		{
			// i is aclid
			if(ACLAPREP.get(i) != null)
				for(int apbdd : ACLAPREP.get(i))
				{
					int apid = APMAP.get(apbdd);
					APREF.get(apid).add(i);
				}
		}
		for(int i = 0; i < APREF.size(); i ++)
		{
			if(APREF.get(i).isEmpty())
			{
				emptyapid = i;
				break;
			}
		}
		System.out.println("AP Reference is computed.");
		System.out.println("empty AP id is " + emptyapid);

	}

	/**
	 * measure the time for preparation
	 * @return tries - number of attempts to search for the best
	 *                  way to calculate atomic predicates
	 *          repbdd - number of times to convert ACLs to BDDs
	 * @throws FileNotFoundException 
	 */
	public void preparetime(int triesap, int repbdd) throws FileNotFoundException
	{
		PrintStream ps = new PrintStream((new FileOutputStream("aclaptime", true)));

		CalBDDForACL();
		long bt = findfastestorder(triesap);
		System.out.println("AP Computing:" + bt);
		for(int i = 0; i < repbdd; i ++)
		{
			long start = System.nanoTime();
			CalBDDForACL();
			long end = System.nanoTime();
			ps.println(end - start + bt);
			System.out.println(end - start + bt);
		}

	}

	/**
	 * test the time to re-calculate ap when an acl is removed.
	 * @param aclid
	 * @return
	 * TO DO: only can run once
	 */
	public double remove(int aclid)
	{
		// clone all things
		ArrayList<HashSet<Integer>> APREFclone = new ArrayList<HashSet<Integer>>(APREF);

		long start = System.nanoTime();

		int mergeemptyid = -1;
		HashSet<Integer> mergeidset = new HashSet<Integer> ();
		// first step, APREFclone is updated
		if(ACLAPREP.get(aclid) == null)
		{
			long end = System.nanoTime();
			return (end - start)/1000000.0;
		}
		for(int apbdd : ACLAPREP.get(aclid))
		{
			int apid = APMAP.get(apbdd);
			APREFclone.get(apid).remove(aclid);
			if(APREFclone.get(apid).isEmpty())
			{
				mergeemptyid = apid;
			}else
			{
				mergeidset.add(apid);
			}
		}

		ArrayList<Integer> tomerge = new ArrayList<Integer> ();
		for(int apid : mergeidset)
		{
			int refcount = APREFclone.get(apid).size();
			int apbdd = AP.get(apid);
			boolean firstpass = false;
			HashSet<Integer> CandidateBDD = new HashSet<Integer> ();
			for(int refaclid : APREFclone.get(apid))
			{
				if(firstpass)
				{
					if(CandidateBDD.size() <= 1)
					{
						break;
					}else
					{
						CandidateBDD.retainAll(ACLAPREP.get(refaclid));
					}
				}else
				{
					firstpass = true;
					for(int apbddcandidate : ACLAPREP.get(refaclid))
					{
						if(apbddcandidate != apbdd)
						{
							int apidcandidate = APMAP.get(apbddcandidate);
							if(APREFclone.get(apidcandidate).size() == refcount)
							{
								CandidateBDD.add(apbddcandidate);
							}
						}
					}
				}
			}

			if(CandidateBDD.isEmpty())
			{
				continue;
			}else
			{
				if(CandidateBDD.size() > 1)
				{
					System.err.println("internel error. candidate set should not have more than 1 ap.");
				}else
				{
					for(int apbddcandidate : CandidateBDD)
					{
						int apidcandidate = APMAP.get(apbddcandidate);
						if(APREFclone.get(apidcandidate).equals(APREFclone.get(apid)))
						{
							// find a pair of aps to be merged
							tomerge.add(apid);
							tomerge.add(apidcandidate);
						}
					}
				}
			}
		}

		/*
		System.out.println("Atomic Predicates to be merged");
		if(mergeemptyid >= 0)
		{
			System.out.println(mergeemptyid + "to be merged with " + emptyapid);
		}
		System.out.println("merge every two: ");
		for(int i = 0; i < tomerge.size(); i ++)
		{
			System.out.println(tomerge.get(i));
		}*/
		long end = System.nanoTime();
		return (end - start)/1000000.0;
	}

	public double remove2(int aclid)
	{
		// clone all things
		ArrayList<HashSet<Integer>> APREFclone = new ArrayList<HashSet<Integer>>(APREF);

		long start = System.nanoTime();

		int mergeemptyid = -1;
		HashSet<Integer> mergeidset = new HashSet<Integer> ();
		// first step, APREFclone is updated
		if(ACLAPREP.get(aclid) == null)
		{
			long end = System.nanoTime();
			return (end - start)/1000000.0;
		}
		for(int apbdd : ACLAPREP.get(aclid))
		{
			int apid = APMAP.get(apbdd);
			APREFclone.get(apid).remove(aclid);
			if(APREFclone.get(apid).isEmpty())
			{
				mergeemptyid = apid;
			}else
			{
				mergeidset.add(apid);
			}
		}

		ArrayList<Integer> tomerge = new ArrayList<Integer> ();
		for(int apid : mergeidset)
		{
			int refcount = APREFclone.get(apid).size();
			int apbdd = AP.get(apid);
			boolean firstpass = false;
			HashSet<Integer> CandidateBDD = new HashSet<Integer> ();
			int minid = -1;
			int minsize = -1;
			for(int refaclid : APREFclone.get(apid))
			{
				if(firstpass)
				{
					if(ACLAPREP.get(refaclid).size() < minsize)
					{
						minid = refaclid;
						minsize = ACLAPREP.get(minid).size();
					}
				}else
				{
					firstpass = true;
					minid = refaclid;
					minsize = ACLAPREP.get(minid).size();

				}
			}

			for(int apbddcandidate : ACLAPREP.get(minid))
			{
				if(apbddcandidate != apbdd)
				{
					int apidcandidate = APMAP.get(apbddcandidate);
					if(APREFclone.get(apidcandidate).size() == refcount)
					{
						CandidateBDD.add(apbddcandidate);
					}
				}
			}

			if(CandidateBDD.isEmpty())
			{
				continue;
			}else
			{
				for(int apbddcandidate : CandidateBDD)
				{
					int apidcandidate = APMAP.get(apbddcandidate);
					if(APREFclone.get(apidcandidate).equals(APREFclone.get(apid)))
					{
						// find a pair of aps to be merged
						tomerge.add(apid);
						tomerge.add(apidcandidate);
						break;
					}
				}

			}
		}

		/*
		System.out.println("Atomic Predicates to be merged");
		if(mergeemptyid >= 0)
		{
			System.out.println(mergeemptyid + "to be merged with " + emptyapid);
		}
		System.out.println("merge every two: ");
		for(int i = 0; i < tomerge.size(); i ++)
		{
			System.out.println(tomerge.get(i));
		}*/
		long end = System.nanoTime();
		return (end - start)/1000000.0;
	}


	public static void RemoveTest()
	{
		BDDAP bap = new BDDAP("purdue-ACLs.ser");
		int totalacl = bap.getACLInfo().getACLNum();
		bap.CalBDDForACL();
		bap.SimpleTest();
		bap.CalAPExp();

		for(int i = 0; i < totalacl; i ++)
		{
			bap.CalAPREF();
			double timepass = bap.remove2(i);
			System.out.println(timepass);
		}
	}
	
	public HashSet<Integer> getAllAP()
	{
		HashSet<Integer> unv = new HashSet<Integer>();
		for(int i = 0; i < AP.size(); i ++)
		{
			unv.add(AP.get(i));
		}
		return unv;
	}

	public static void main(String[] args) throws FileNotFoundException
	{
		
		BDDAP bap = BDDAP.LoadBDDAP("purdue-BDDAP.ser");
		
		bap.CalBDDForACL();
		
		
		
		StoreACL sa = bap.sa;
		for(LinkedList<ACLRule> one_acl : sa.ACLList)
		{
			if(one_acl.size() >= 40)
			{
				System.out.println(one_acl.size());
			}
		}
		
		// RemoveTest();
		//BDDAP bap = new BDDAP("uw-ACLs.ser");
		//bap.preparetime(100, 20);
		//bap.CalBDDForACL();
		//bap.CalAPs("uworderrandomrouter");
		//bap.SimpleTest();

		/**
		 * debug which one is correct? bdd or packetset
		 */
		/*
		LinkedList<LinkedList<ACLRule>> tmp = bap.sa.ACLList;
		bap.sa.ACLList = new LinkedList<LinkedList<ACLRule>> ();
		for(int i = 0; i < 100; i ++)
		{
			bap.sa.ACLList.add(tmp.get(i));
		}

		int addnum = 446;
		LinkedList<ACLRule> tmpacl = tmp.get(addnum);
		LinkedList<ACLRule> toaddacl = new LinkedList<ACLRule>();
		for(int i = 0; i < 20; i ++)
		{
			toaddacl.add(tmpacl.get(i));
		}
		//toaddacl.add(tmpacl.get(39));
		//toaddacl.add(tmpacl.get(46));
		toaddacl.add(tmpacl.get(51));

		bap.sa.ACLList.add(toaddacl);
		System.out.println(toaddacl.size());
		 */
		/*
		for(int i = 0; i < toaddacl.size(); i ++)
		{
			System.out.println(toaddacl.get(i));
		}
		 */	

		//HashSet<Integer> apexp = bap.getAPExp("config822", "5");
		//System.out.println("size of the expression: " + apexp.size());

		//BDDAP testload = LoadBDDAP("purdue-BDDAP.ser");

		/*
		for(int i = 0; i < bap.ACLBDDREP.size(); i ++)
		{
			System.out.println(bap.ACLBDDREP.get(i));
		}
		 */
	}
}
