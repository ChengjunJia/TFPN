package StaticReachabilityAnalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeMap;

public class AtomicPredicate {
	Hashtable<String, LinkedList<ACLRule>> tableOfACLs;
	TreeMap<String, PacketSet> mapOfPacketSets;
	LinkedList<String> listOfACLNames;
	LinkedList<String> listOfConfig;
	TreeMap<String, Range> ACLsInConfig;// range - acls in the same file are stored consecutively
	LinkedList<PacketSet> listOfAPs;
	int numOfAPs;
	LinkedList<String> ACLsIncluded;
	//the linkedlist has no duplicate elements
	Hashtable<String, LinkedList<Integer>> ACLExpr;

	public AtomicPredicate()
	{
		tableOfACLs = new Hashtable<String, LinkedList<ACLRule>>();
		mapOfPacketSets = new TreeMap<String, PacketSet>();
		listOfACLNames = new LinkedList<String>();
		listOfAPs = new LinkedList<PacketSet>();
		numOfAPs = 0;
		ACLsIncluded = new LinkedList<String>();
		//ACLExpr = new Hashtable<String, HashSet<Integer>> ();
		listOfConfig = new LinkedList<String> ();
		ACLsInConfig = new TreeMap<String, Range> ();
	}
	
	public void ClearACLData()
	{
		//do not clear acl names
		tableOfACLs.clear();
		mapOfPacketSets.clear();
	}

	void ClearAPs()
	{
		numOfAPs = 0;
		listOfAPs = null;
		listOfAPs = new LinkedList<PacketSet>();
		ACLsIncluded = null;
		ACLsIncluded = new LinkedList<String>();
	}


	public void CreatPacketSet()
	{
		PacketSet permitBuffer, denyBuffer, interimBuffer ;
		String acl = new String();
		Tuple currentTuple;
		LinkedList aclList;
		ACLRule aclRule;

		/************************************
		 * Process all the elements in the ACL table, in order of the keys
		 * Only one element is processed at a time.
		 ************************************/
		for(int n = 0; n < listOfACLNames.size(); n ++) {
			/*** Get the ACL name ***/
			acl = (String) listOfACLNames.get(n);// Get the next ACL
			//System.out.println(n+": " + acl);
			/*** Get the linked list of each ACL ***/
			aclList = (LinkedList) tableOfACLs.get(acl);
			/*** Create a new deny and permit buffer ***/
			denyBuffer = new PacketSet();
			permitBuffer = new PacketSet();
			//System.out.println(permitBuffer.IsFalse());
			/*** Process the ACL rules in the ACL, one at a time ***/
			for (int counter=0; counter < aclList.size(); counter++) {
				/*** Initialization ***/
				currentTuple = new Tuple();
				interimBuffer = new PacketSet();
				aclRule = (ACLRule) aclList.get(counter);
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
					 * add currentTuple to denyBuffer straightaway
					 ********************************/
					if (denyBuffer.tupleArray.size()==0) {
					}
					/********************************
					 * Compare current permit tuple against each tuple
					 * in the deny buffer one at a time
					 ********************************/
					for (int i=0; i<denyBuffer.tupleArray.size(); i++) {
						Tuple outputTuple = new Tuple();
						boolean toDecrement = false;
						/*** Get the next tuple in the deny buffer ***/
						Tuple denyTuple = (Tuple) denyBuffer.tupleArray.get(i);
						for (int j=0; j<interimBuffer.tupleArray.size(); j++) {
							toDecrement = interimBuffer.GetPermitTuple(
									(Tuple)interimBuffer.tupleArray.get(j),
									denyTuple,interimBuffer,j);
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
				/*******************************************
				 * Store the permitBuffer in the
				 * routerConfig object Map, using acl as key
				 *******************************************/
				mapOfPacketSets.put(acl, permitBuffer);
			}else
			{// the acl is false
				mapOfPacketSets.put(acl, permitBuffer);
			}
		} // end of while(aclNumber.hasMoreElements())
		//return success;
		System.out.println("----> Successful completion of creating packetsets <----");
	} // end of CreatePacketSet
	
	void AddOneAP(PacketSet APtoAdd)
	{
		LinkedList<PacketSet> oldList = listOfAPs;
		int oldNum = numOfAPs;
		numOfAPs = 0;
		listOfAPs = new LinkedList<PacketSet>();
		for(int i = 0; i < oldNum; i ++)
		{
			//System.out.println(psToAdd);
			PacketSet tmps = new PacketSet();
			if(PacketSet.Intersection(oldList.get(i), APtoAdd, tmps))
			{
				listOfAPs.add(tmps);
				/*
					PacketSet tmpsminus = new PacketSet();
					PacketSet.Negation(tmps, tmpsminus);
					PacketSet newpsToAdd = new PacketSet();
					PacketSet.Intersection(psToAdd, 
							tmpsminus, newpsToAdd);
					psToAdd = newpsToAdd;
				 */	
				numOfAPs ++;
			}
		}
		oldList = null;

	}

	void AddOneACL(String aclName)
	{
		ACLsIncluded.add(aclName);

		PacketSet psToAdd = mapOfPacketSets.get(aclName);
		//System.out.println(aclName);
		//System.out.println(psToAdd);
		PacketSet psToAddNeg = new PacketSet();
		PacketSet.Negation(psToAdd, psToAddNeg);
		if(numOfAPs == 0)
		{
			if(!psToAdd.IsFalse())
			{
				numOfAPs ++;
				listOfAPs.add(psToAdd);
			}
			if(!psToAddNeg.IsFalse())
			{
				numOfAPs ++;
				listOfAPs.add(psToAddNeg);
			}
		}else
		{
			LinkedList<PacketSet> oldList = listOfAPs;
			int oldNum = numOfAPs;
			numOfAPs = 0;
			listOfAPs = new LinkedList<PacketSet>();
			for(int i = 0; i < oldNum; i ++)
			{
				//System.out.println(psToAdd);
				//System.out.println(psToAddNeg);
				PacketSet tmps = new PacketSet();
				/*
				 * to be optimized..
				 * if tmps = false, then get(i) is a subset of psToAddNeg
				 * if tmps = get(i), then get(i) and psToAdd are disjoint -- hard to check
				 */
				if(PacketSet.Intersection(oldList.get(i), psToAdd, tmps))
				{
					listOfAPs.add(tmps);
					/*
					PacketSet tmpsminus = new PacketSet();
					PacketSet.Negation(tmps, tmpsminus);
					PacketSet newpsToAdd = new PacketSet();
					PacketSet.Intersection(psToAdd, 
							tmpsminus, newpsToAdd);
					psToAdd = newpsToAdd;
					*/	
					numOfAPs ++;
				}
				tmps = new PacketSet();
				if(PacketSet.Intersection(oldList.get(i), psToAddNeg, tmps))
				{
					listOfAPs.add(tmps);
					/* try to shrink it..
					PacketSet tmpsminus = new PacketSet();
					PacketSet.Negation(tmps, tmpsminus);
					PacketSet newpsToAddNeg = new PacketSet();
					PacketSet.Intersection(psToAddNeg, 
							tmpsminus, newpsToAddNeg);
					psToAddNeg = newpsToAddNeg;
					*/
					numOfAPs ++;
				}
			}
			oldList = null;
		}
	}// end of add one acl

	static void ParseACLExtended(ACLRule aclRule, String[] argument)
	{
		int k = 0;
		aclRule.protocolLower = ParseTools.GetProtocolNumber(argument[k]);
		if (aclRule.protocolLower.equals("256")) {
			aclRule.protocolLower = "0";
			aclRule.protocolUpper = "255";
		}
		else aclRule.protocolUpper = aclRule.protocolLower;
		k++;
		/*********************************************
		 * Process source fields
		 *********************************************/
		if (argument[k].equals("any")) {
			aclRule.source = argument[k];
			k++;
			k = ParseTools.ParsePort (aclRule, argument, k, "source");
		}
		else if (argument[k].equals("host")) {
			k++;
			aclRule.source = argument[k];
			k++;
			k = ParseTools.ParsePort (aclRule, argument, k, "source");
		}
		else {
			aclRule.source = argument[k];
			k++;
			aclRule.sourceWildcard = argument[k];
			k++;
			k = ParseTools.ParsePort (aclRule, argument, k, "source");
		}
		/*********************************************
		 * Process destination fields
		 *********************************************/
		/*** If the destination keyword is "any" ***/
		if (argument[k].equals("any")) {
			aclRule.destination = argument[k];
			k++;
			if (argument[k]!=null)
				k = ParseTools.ParsePort (aclRule, argument, k, "destination");
		}
		else if (argument[k].equals("host")) {
			k++;
			aclRule.destination = argument[k];
			k++;
			if (argument[k]!=null)
				k = ParseTools.ParsePort (aclRule, argument, k, "destination");
		}
		else {
			aclRule.destination = argument[k];
			k++;
			aclRule.destinationWildcard = argument[k];
			k++;
			if (argument[k]!=null)
				k = ParseTools.ParsePort (aclRule, argument, k, "destination");
		}

	}
	
	public void CalAPs(int[] index)
	{
		ClearAPs();
		for(int i = 0; i < index.length; i ++)
		{
			String aclName = listOfACLNames.get(index[i]);
			AddOneACL(aclName);
			System.out.println("ACL Num: " + i + " AP Num:" + numOfAPs);
		}
	}

	public void CollectACL(File inputDir) throws
	IOException {
		/************************************************************************
		 * Parse the files in the input directory one at a time
		 ************************************************************************/
		String filenames[] = inputDir.list();
		//System.out.println("network name: " + inputDir.getName());
		for(int n = 0; n < filenames.length; n++) {
			File inputFile = new File(inputDir,filenames[n]);
			listOfConfig.add(filenames[n]);
			int oldsize = listOfACLNames.size();
			//System.out.println("in: " + filenames[n]);
			/************************************************************************
			 * Set up ACLs, Router Interfaces and Routers
			 ************************************************************************/
			String currentACLnumber = new String();
			String previousACLnumber = new String();
			LinkedList acl = new LinkedList();
			LinkedList aclExtended = new LinkedList();
			//RouterConfig router = new RouterConfig();
			//InterfaceConfig routerInterface = new InterfaceConfig();
			/************************************************************************
			 * Set up a Scanner to read the file using tokens
			 ************************************************************************/
			Scanner scanner = null;
			try {
				scanner = new Scanner (inputFile);
				scanner.useDelimiter("\n");
				//scanner.useDelimiter(System.getProperty("line.separator"));
				// doesn't work for .conf files
			} catch (FileNotFoundException e) {
				System.out.println ("File not found!"); // for debugging
				System.exit (0); // Stop program if no file found
			}
			/************************************************************************
			 * Read each token in every scanned line
			 ************************************************************************/
			/* Read line by line */
			boolean ipACLExtendedMode = false;
			while (scanner.hasNext()) {
				/* Read token by token in each line */
				Scanner lineScanner = new Scanner(scanner.next());
				String keyword;
				
				if (lineScanner.hasNext()) {
					keyword = lineScanner.next();
					//whether in the ip acl-list extended mode
					if(ipACLExtendedMode)
					{
						if(keyword.equals("permit")||keyword.equals("deny"))
						{
							ACLRule aclRule = new ACLRule();
							aclRule.accessList = "access-list";
							aclRule.accessListNumber = listOfACLNames.getLast();
							aclRule.permitDeny = keyword;
							
							String[] argument = new String[10] ;
							int i = 0;
							while (lineScanner.hasNext()) {
								argument[i] = lineScanner.next();
								//System.out.println(argument[i]);
								i++;
							}
							ParseACLExtended(aclRule,argument);
							
							aclExtended.add(aclRule);
							continue;
						}// ip acl extended mode ends
						else{
							ipACLExtendedMode = false;
							//store the acl
							tableOfACLs.put(listOfACLNames.getLast(), aclExtended);
							System.out.println("Adding " + listOfACLNames.getLast());
							//no break, should continue parsing
						}
					}
					// for debugging
					//System.out.println("keyword: "+keyword);
					/*****************************************************************
					 * This section handles the hostname
					 *****************************************************************/
					//process the ip access-list extended
					
					if (keyword.equals("hostname")){
						System.out.println("hostname: " + lineScanner.next());

					}
					
					/****************************************************************
					 * This section handles ip access-list extended
					 */
					else if(keyword.equals("ip"))
					{
						//check whether the following is "access-list" and "extended"
						if(lineScanner.hasNext())
						{
							keyword = lineScanner.next();
							if(!keyword.equals("access-list"))
							{
								continue;
							}
						}else{
							continue;
						}
						if(lineScanner.hasNext())
						{
							keyword = lineScanner.next();
							if(!keyword.equals("extended"))
							{
								continue;
							}
						}else{
							continue;
						}
						//get the name
						keyword = lineScanner.next();
						String extendedACLName = keyword + '-' + filenames[n];
						listOfACLNames.add(extendedACLName);
						ipACLExtendedMode = true;
						if(!aclExtended.isEmpty())
						{// if not empty, then it has been used before, so get a new one
							//System.out.println("new acl extended");
							aclExtended = new LinkedList();
						}
						System.out.println("enter " + extendedACLName);
					}
					
					/**************************************************************
					 * This section handles ACL rules that start with "access-list"
					 ***************************************************************/
					else if (keyword.equals("access-list")) {
						labelBreakHere: {
						int i = 0;
						ACLRule aclRule = new ACLRule();
						aclRule.accessList = keyword;
						/***************************************
						 * read access-list into argument array
						 * Although only 18 needed,
						 * allocated 12 extra to read options
						 * not handled
						 ***************************************/
						String[] argument = new String[30] ;
						while (lineScanner.hasNext()) {
							argument[i] = lineScanner.next();
							//System.out.println(argument[i]);
							if (argument[i].equals ("!")) break;
							i++;
						}
						/************************************************************************
						 * Parse argument array into correct ACL rule structure
						 ************************************************************************/
						if (argument[0].equals ("rate-limit")) break labelBreakHere;
						//Skip this section if "rate-limit" found
						aclRule.accessListNumber = argument[0];
						currentACLnumber = argument[0] + '-' + filenames[n];
						//System.out.println("ACL Number: " + currentACLnumber);
						//Initialize previousACLnumber
						if (previousACLnumber.length()==0) previousACLnumber =
								currentACLnumber;
						//Check whether this is a new ACL
						if (currentACLnumber.equals(previousACLnumber)) ; // do nothing
						else {
							//if different ACL number, store the previous ACL in router config
							tableOfACLs.put(previousACLnumber, acl);
							listOfACLNames.add(previousACLnumber);
							System.out.println("Adding " + previousACLnumber);
							//System.out.println(acl);
							//get a new acl to store
							acl = new LinkedList();
							previousACLnumber = currentACLnumber;
						}
						int k = 1;
						//k is a position marker used to check the rest of the ACL
						/************************************************************************
						 * Parse the rest of the ACL rule
						 ************************************************************************/
						if (argument[k].equals ("remark") ) { // No need to parse
							//further if ACL contains "remark"
							aclRule.remark=true;
							k=i;
						}
						while (k<i) {
							//check whether ACL is permit or deny
							boolean checkPermitDeny = false;
							//track whether this is a standard IP Access List
							boolean standardIPAccessList = false ;
							//track whether this is an extended IP Access List
							boolean extendedIPAccessList = false ;
							/************************************************************************
							 * This section handles the Dynamic part in an ACL rule
							 ************************************************************************/
							if (argument[k].equals ("dynamic")) {
								aclRule.dynamic="dynamic";
								k++;
								aclRule.dynamicName=argument[k];
							}
							/************************************************************************
							 * This section handles the Timeout part in an ACL rule
							 ************************************************************************/
							//timeout section to be added
							/************************************************************************
							 * This section handles the Permit and Deny keywords
							 * in an ACL rule
							 ************************************************************************/
							if (argument[k].equals ("permit")) {
								aclRule.permitDeny="permit";
								k++;
								checkPermitDeny = true;
							}
							else if (argument[k].startsWith("deny")) {
								//.equals doesn't work here
								aclRule.permitDeny="deny";
								k++;
								checkPermitDeny = true;
							}
							/************************************************************************
							 * This section handles the Protocol, Source
							 * and Destination parts in an ACL rule
							 * Entry into this section only after "Permit" or "Deny" found
							 ************************************************************************/
							if (checkPermitDeny) {
								//check whether ACL is standard or extended
								int valueOfACLNumber = Integer.valueOf(aclRule.accessListNumber);
								if (valueOfACLNumber>=1 &&
										valueOfACLNumber<=99 )
									standardIPAccessList=true ;
								else if (valueOfACLNumber>=100 &&
										valueOfACLNumber<=199 )
									extendedIPAccessList=true;
								else if (valueOfACLNumber>=1300 &&
										valueOfACLNumber<=1999 )
									standardIPAccessList=true ;
								else if (valueOfACLNumber>=2000 &&
										valueOfACLNumber<=2699 )
									extendedIPAccessList=true;
								/*********************************************
								 * Process standard ACL rules
								 *********************************************/
								if (standardIPAccessList) {
									if(argument[k].equals("host"))
									{
										k = k + 1;
									}
									aclRule.source = argument[k]; //store source IP
									// Process Source Mask
									if (k+1<i) {
										if (argument[k+1].contains(".") ||
												argument[k+1].contains("any")) {
											k++;
											aclRule.sourceWildcard = argument[k];
										}
									}
								} // end of one line of Standard ACL parsing
								/*********************************************
								 * Process extended ACL rules
								 *********************************************/
								else if (extendedIPAccessList) {
									aclRule.protocolLower = ParseTools.GetProtocolNumber
											(argument[k]);
									if (aclRule.protocolLower.equals("256")) {
										aclRule.protocolLower = "0";
										aclRule.protocolUpper = "255";
									}
									else aclRule.protocolUpper = aclRule.protocolLower;
									k++;
									/*********************************************
									 * Process source fields
									 *********************************************/
									if (argument[k].equals("any")) {
										aclRule.source = argument[k];
										k++;
										k = ParseTools.ParsePort (aclRule, argument, k, "source");
									}
									else if (argument[k].equals("host")) {
										k++;
										aclRule.source = argument[k];
										k++;
										k = ParseTools.ParsePort (aclRule, argument, k, "source");
									}
									else {
										aclRule.source = argument[k];
										k++;
										aclRule.sourceWildcard = argument[k];
										k++;
										k = ParseTools.ParsePort (aclRule, argument, k, "source");
									}
									/*********************************************
									 * Process destination fields
									 *********************************************/
									/*** If the destination keyword is "any" ***/
									if (argument[k].equals("any")|| argument[k].equals("cDdCSP1xK")) 
									// very ad-hoc, for uw-files
									{
										aclRule.destination = "any";//argument[k];
										k++;
										if (argument[k]!=null)
											k = ParseTools.ParsePort (aclRule, argument, k, "destination");
									}
									else if (argument[k].equals("host")) {
										k++;
										aclRule.destination = argument[k];
										k++;
										if (argument[k]!=null)
											k = ParseTools.ParsePort (aclRule, argument, k, "destination");
									}
									else {
										aclRule.destination = argument[k];
										k++;
										aclRule.destinationWildcard = argument[k];
										k++;
										if (argument[k]!=null)
											k = ParseTools.ParsePort (aclRule, argument, k, "destination");
									}
								} // end of one line of Extended ACL parsing
							} // End of protocol, source and destination parts
							/************************************************************************
							 * This section handles the precedence, tos and log parts
							 * in an ACL rule
							 ************************************************************************/
							//to be added
							//end of precedence, tos and log parts
							k++;
						}

						//Add current ACL line to the Linked List
						//if it is not meant as a remark
						if (aclRule.remark==false) acl.add(aclRule);
					} // end of labelBreakHere
					} // End of section handling ACLS that start with "access-list"
				}
			} // end of while (scanner.hasNext())
			/***************************************************
			 * Check whether this is a new ACL
			 ***************************************************/
			if (currentACLnumber.equals(previousACLnumber)) {
				if(!currentACLnumber.isEmpty()){
					tableOfACLs.put(previousACLnumber, acl);
					listOfACLNames.add(previousACLnumber);
					System.out.println("Adding " + previousACLnumber);
					//System.out.println(acl);
				}
			}
			
			int newsize = listOfACLNames.size();
			ACLsInConfig.put(filenames[n], new Range(oldsize, newsize - 1));
		} // end of for loop search files in folder
		/************************************************************************
		 * Notify user the parsing has been completed
		 ************************************************************************/
		System.out.println(listOfConfig.size() + " Configs Parsed.");
		System.out.println(listOfACLNames.size() + " ACLs Parsed.");
		System.out.println("----> Successful completion of parsing <----");
	} // end of Parser
	
	public void CombineACLs(Hashtable<String, LinkedList<ACLRule>> tofa, TreeMap<String, PacketSet> mofp, LinkedList<String> lofaname)
	{
		if(tableOfACLs.isEmpty())
		{
			tableOfACLs = tofa;
			mapOfPacketSets = mofp;
			listOfACLNames = lofaname;
			return;
		}
		//in this case, we need to merge
		listOfACLNames.addAll(lofaname);
		tableOfACLs.putAll(tofa);
		mapOfPacketSets.putAll(mofp);
	}
	
	public void CombineAP(LinkedList<PacketSet> aplist)
	{
		if(numOfAPs == 0)
		{
			//that is new
			numOfAPs = aplist.size();
			listOfAPs = aplist;
			return;
		}
		LinkedList<PacketSet> oldList = listOfAPs;
		int oldNum = numOfAPs;
		numOfAPs = 0;
		listOfAPs = new LinkedList<PacketSet>();
		for(int i = 0; i < oldNum; i ++)
		{
			for(int j = 0; j < aplist.size(); j ++)
			{
				//System.out.println(psToAdd);
				PacketSet tmps = new PacketSet();
				if(PacketSet.Intersection(oldList.get(i), aplist.get(j), tmps))
				{
					listOfAPs.add(tmps);
					numOfAPs ++;
				}
			}
			System.out.println(i+"/"+oldNum);
		}
		oldList = null;		
	}
	
	public void CombineData(AtomicPredicate newap)
	{
		CombineAP(newap.listOfAPs);
		CombineACLs(newap.tableOfACLs, newap.mapOfPacketSets, newap.listOfACLNames);
	}
	
	/**********
	 * Compute and store the expression
	 */
	public void GenerateACLExpr()
	{
		for(int i = 0; i < listOfACLNames.size(); i ++)
		{
			//generate an id set for each acl
			String aclname = listOfACLNames.get(i);
			ACLExpr.put(aclname, new LinkedList<Integer>());
			for(int j = 0; j < listOfAPs.size(); j ++)
			{
				if(PacketSet.IntersectionNotFalse(mapOfPacketSets.get(aclname), listOfAPs.get(j)))
				{
					ACLExpr.get(aclname).add(j);
				}
			}
			System.out.println(i + " " + aclname);
		}
	}
	
	/**********
	 * save the acl expr to the file
	 * @throws IOException 
	 */
	public void GenerateACLExprForSave() throws IOException
	{
		File aclexpr = new File("aclexpr.txt");
		if(!aclexpr.exists())
		{
			aclexpr.createNewFile();
		}
		FileWriter fw = new FileWriter(aclexpr);
		
		for(int i = 0; i < listOfACLNames.size(); i ++)
		{
			String aclname = listOfACLNames.get(i);
			LinkedList<Integer> ACLexprSet = new LinkedList<Integer>();
			
			for(int j = 0; j < listOfAPs.size(); j ++)
			{
				if(PacketSet.IntersectionNotFalse(mapOfPacketSets.get(aclname), listOfAPs.get(j)))
				{
					ACLexprSet.add(j);
				}
			}
			System.out.println(i + " " + aclname + ": " + ACLexprSet.size());
			Object [] aclexprarray = ACLexprSet.toArray();
			for(int j = 0; j < aclexprarray.length; j ++)
			{
				fw.write(aclexprarray[j] + " ");
			}
			fw.write("\n");
		}
		fw.close();
	}
	
	/**********
	 * Only the size of the expression is stored
	 * @throws IOException 
	 */
	public void GenerateACLExprForTest() throws IOException
	{
		File aclexpr = new File("aclexpr-count.txt");
		if(!aclexpr.exists())
		{
			aclexpr.createNewFile();
		}
		FileWriter fw = new FileWriter(aclexpr);
		
		for(int i = 0; i < listOfACLNames.size(); i ++)
		{
			String aclname = listOfACLNames.get(i);
			int idcounter = 0;
			for(int j = 0; j < listOfAPs.size(); j ++)
			{
				if(PacketSet.IntersectionNotFalse(mapOfPacketSets.get(aclname), listOfAPs.get(j)))
				{
					idcounter ++;
				}
			}
			System.out.println(i + " " + aclname + ": " + idcounter);
			fw.write(idcounter + "\n");
			//clear it for the next run.
			idcounter = 0;
		}
		fw.close();
	}
	
	

	public static void main(String args[]) throws IOException 
	{
		String inputDir = "./configure/";
		AtomicPredicate AP = new AtomicPredicate();
		try {
			AP.CollectACL(new File(inputDir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Totally " + AP.listOfACLNames.size() + " ACLs");
		AP.CreatPacketSet();
		
		int[] s = new int[AP.listOfACLNames.size()];
		Sample.GetSample(s.length, AP.listOfACLNames.size(), s);
		AP.CalAPs(s);
		
		System.out.println(s.length + " ACLs involved");
		
		APTools.DebugAPs(AP, "save", "");
		
		AP.GenerateACLExprForSave();
		//APTools.DebugACLExpr(AP);
		//APTools.CountACLExpr(AP);
	}
}
