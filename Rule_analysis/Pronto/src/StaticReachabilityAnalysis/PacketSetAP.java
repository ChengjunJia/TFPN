package StaticReachabilityAnalysis;

import java.util.LinkedList;

public class PacketSetAP {

	StoreACL sa;
	LinkedList<PacketSet> ACLpacketset;
	LinkedList<PacketSet> AP;
	
	/**
	 * 
	 * @param filename - store an instance of sa
	 */
	public PacketSetAP(String filename)
	{
		sa = StoreACL.LoadNetwork(filename);
		ACLpacketset = new LinkedList<PacketSet>();
		AP = new LinkedList<PacketSet>();
	}
	
	/**
	 * calculate the packetset representation for each acl
	 */
	public void CalPacketSet()
	{
		for(int i = 0; i < sa.ACLList.size(); i ++)
		{
			PacketSet ps = ConvertToPacketSet(sa.ACLList.get(i));
			ACLpacketset.add(ps);
		}
		System.out.println("Packet Sets for ACLs Computed.");
	}
	
	/**
	 * just add ACLs following the order of ACL storage
	 */
	public void SimpleTest()
	{
		int [] index = new int[ACLpacketset.size()];
		// no special order
		for(int i = 0; i < index.length; i ++)
		{
			index[i] = i;
		}
		long start = System.currentTimeMillis();
		CalAPs(index);
		long end = System.currentTimeMillis();
		System.out.println("# of ACLs " + index.length);
		System.out.println("# of APs " + AP.size());
		System.out.println("Time: " + (end - start) + "ms");
		// memory
		long ACLSize = 0;
		for(int i = 0; i < ACLpacketset.size(); i ++)
		{
			ACLSize = ACLSize + ACLpacketset.get(i).SizeOf();
		}
		System.out.println("Memory Use for ACL representation: " + ACLSize
		+ " bytes");
		long APSize = 0;
		for(int i = 0; i < AP.size(); i ++)
		{
			APSize = APSize + AP.get(i).SizeOf();
		}
		System.out.println("Memory Use for Atomic Predicates: " 
		+ APSize + " bytes");
	}
	
	public static void main(String[] args)
	{
		PacketSetAP psap = new PacketSetAP("uw-ACLs.ser");
		
		/**
		 * debug which one is correct? bdd or packetset
		 */
		/*
		LinkedList<LinkedList<ACLRule>> tmp = psap.sa.ACLList;
		psap.sa.ACLList = new LinkedList<LinkedList<ACLRule>> ();
		for(int i = 0; i < 100; i ++)
		{
			psap.sa.ACLList.add(tmp.get(i));
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
		
		psap.sa.ACLList.add(toaddacl);
		System.out.println(toaddacl.size());
		*/
		
		psap.CalPacketSet();
		psap.SimpleTest();
	}
	
	/**
	 * Adapt from AtomicPredicate.CalAPs
	 * @param index - the order of combining ACLs
	 */
	public void CalAPs(int[] index)
	{
		AP.clear();
		if(index.length != ACLpacketset.size())
		{
			System.err.println("The size of indexes does not match" +
					"the number of Packet Sets!");
			return;
		}
		for(int i = 0; i < index.length; i ++)
		{
			AddOneACL(index[i]);
			//System.out.println("ACL Num: " + i + " AP Num:" + AP.size());
		}
	}
	
	/**Copy from AtomicPredicate.AddOneACL
	 * add one acl and recompute aps 
	 * @param ind - the packetset for the ind-th ACL 
	 */
	void AddOneACL(int ind)
	{

		PacketSet psToAdd = ACLpacketset.get(ind);
		PacketSet psToAddNeg = new PacketSet();
		PacketSet.Negation(psToAdd, psToAddNeg);
		if(AP.size() == 0)
		{
			// initialize...
			if(!psToAdd.IsFalse())
			{
				AP.add(psToAdd);
			}
			if(!psToAddNeg.IsFalse())
			{
				AP.add(psToAddNeg);
			}
		}else
		{
			// old list
			LinkedList<PacketSet> oldList = AP;
			int oldNum = AP.size();
			// set up a new list
			AP = new LinkedList<PacketSet>();
			for(int i = 0; i < oldNum; i ++)
			{
				PacketSet tmps = new PacketSet();
				/**
				 * to be optimized..
				 * if tmps = false, then get(i) is a subset of psToAddNeg
				 * if tmps = get(i), then get(i) and psToAdd are disjoint -- hard to check
				 */
				if(PacketSet.Intersection(oldList.get(i), psToAdd, tmps))
				{
					AP.add(tmps);
					/*
					PacketSet tmpsminus = new PacketSet();
					PacketSet.Negation(tmps, tmpsminus);
					PacketSet newpsToAdd = new PacketSet();
					PacketSet.Intersection(psToAdd, 
							tmpsminus, newpsToAdd);
					psToAdd = newpsToAdd;
					*/	
				}
				tmps = new PacketSet();
				if(PacketSet.Intersection(oldList.get(i), psToAddNeg, tmps))
				{
					AP.add(tmps);
					/* try to shrink it..
					PacketSet tmpsminus = new PacketSet();
					PacketSet.Negation(tmps, tmpsminus);
					PacketSet newpsToAddNeg = new PacketSet();
					PacketSet.Intersection(psToAddNeg, 
							tmpsminus, newpsToAddNeg);
					psToAddNeg = newpsToAddNeg;
					*/
				}
			}
		}
	}// end of add one acl
	
	public static PacketSet ConvertToPacketSet(LinkedList<ACLRule> aclList)
	{
		PacketSet denyBuffer = new PacketSet();
		PacketSet denyBufferNot = PacketSet.NoFilters();
		PacketSet res = new PacketSet();
		
		for(int i = 0; i < aclList.size(); i ++)
		{
			Tuple aclTuple = PacketSet.convertACLRuletoTuple(aclList.get(i));
			PacketSet oneTuple = new PacketSet();
			oneTuple.AddOneTuple(aclTuple);
			
			if(PacketSet.CheckPermit(aclList.get(i)))
			{
				if(denyBuffer.IsFalse())
				{
					PacketSet tmps = PacketSet.UnionOpt(oneTuple, res);
					res = tmps;
				}else
				{
					PacketSet tmps = new PacketSet();
					PacketSet.Intersection(denyBufferNot, oneTuple, tmps);
					res = PacketSet.UnionOpt(res, tmps);
				}
			}else
			{
				PacketSet tmps = PacketSet.UnionOpt(denyBuffer, oneTuple);
				denyBuffer = tmps;
				denyBufferNot.Clean();
				PacketSet.Negation(denyBuffer, denyBufferNot);
			}
		}
		
		return res;
	}
	
	/**
	 * copy from PacketSet.CreatePacketSet
	 * @param acl an aclList
	 * @return the packetset representation of the acl
	 */
	public static PacketSet ConvertToPacketSetZ(LinkedList<ACLRule> aclList)
	{
		/*** Create a new deny and permit buffer ***/
		PacketSet denyBuffer = new PacketSet();
		PacketSet permitBuffer = new PacketSet();
		
		boolean success = false;
		
		/*** Process the ACL rules in the ACL, one at a time ***/
		for (int counter=0; counter < aclList.size(); counter++) {
			/*** Initialization ***/
			Tuple currentTuple = new Tuple();
			PacketSet interimBuffer = new PacketSet();
			ACLRule aclRule = aclList.get(counter);
			//System.out.println(aclRule);
			
			/*****************************************
			 * Process current ACL rule into a tuple
			 * Extract the 5 ranges from the ACL rule
			 * and store in currentTuple
			 *****************************************/
			currentTuple.sourceIP = PacketSet.convertIPtoIntegerRange
			(aclRule.source, aclRule.sourceWildcard);
			currentTuple.sourcePort = PacketSet.convertPortToRange
			(aclRule.sourcePortLower, aclRule.sourcePortUpper);
			currentTuple.destinationIP = PacketSet.convertIPtoIntegerRange
			(aclRule.destination, aclRule.destinationWildcard);
			currentTuple.destinationPort = PacketSet.convertPortToRange
			(aclRule.destinationPortLower, aclRule.destinationPortUpper);
			currentTuple.protocol = PacketSet.convertProtocolToRange
			(aclRule.protocolLower, aclRule.protocolUpper);
			
			/*** If ACL rule is a deny, add the
			 * current tuple to the deny buffer ***/
			if (aclRule.permitDeny.equalsIgnoreCase("deny"))
				denyBuffer.tupleArray.add(currentTuple);
			/*** Otherwise if ACL rule is a permit, do the following ***/
			else if (aclRule.permitDeny.equalsIgnoreCase("permit")) {
				interimBuffer.tupleArray.add(currentTuple);
				
				/********************************
				 * If there are no deny rules,
				 * add currentTuple to denyBuffer straightway
				 ********************************/
				if (denyBuffer.tupleArray.size()==0) {
					//permitBuffer.tupleArray.add(currentTuple);
					success = true;
				}
				
				/********************************
				 * Compare current permit tuple against each tuple
				 * in the deny buffer one at a time
				 ********************************/
				for (int i=0; i<denyBuffer.tupleArray.size(); i++) {
					boolean toDecrement = false;
					/*** Get the next tuple in the deny buffer ***/
					Tuple denyTuple = (Tuple) denyBuffer.tupleArray.get(i);
					for (int j=0; j<interimBuffer.tupleArray.size(); j++) {
						toDecrement = PacketSet.GetPermitTuple(
								(Tuple)interimBuffer.tupleArray.get(j),
								denyTuple,interimBuffer,j);
						success = true;
						/*** if object at position j was changed,
						 * decrement j by 1
						 * so as to process the new object ***/
						if (toDecrement) j-- ;
					}
				}
			} // end of else if (aclRule.permitDeny.equalsIgnoreCase("permit"))
			/*** Add the interim buffer contents to the permit buffer ***/
			permitBuffer.tupleArray.addAll(interimBuffer.tupleArray);
		} // end of for (int counter=0; counter < aclList.size(); counter++)
		
		/*******************************************
		 * If there are ACLs in the Permit Buffer,
		 * - Run the optimizer
		 * - Add the permit buffer to the router object
		 *******************************************/
		if (permitBuffer.tupleArray.size()>0) {
			// Optimize the Permit Buffer
			boolean isSuccess=false;
			isSuccess = PacketSet.OptimizePacketSet(permitBuffer);
			
			return permitBuffer;
		}else
		{
			//return an empty packetset, i.e. false
			return new PacketSet();
		}
	}
}
