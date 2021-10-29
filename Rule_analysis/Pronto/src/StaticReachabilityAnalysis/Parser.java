package StaticReachabilityAnalysis;
/*
 * Parser.java
 *
 * Created : July 25, 2006, 3:20 AM
 * Last Modified : December 12, 2006
 * Author : Eric Gregory Wong
 *
 * ************ MAIN CALLING FUNCTION FOR PARSING AND PACKETSET
CREATION *************
 *
 * Functionality:
 *
 * 1. Parses Router Config files with commands:
 * a. Router hostname
 * b. Interface:
 * i. Name
 * ii. IP address and mask
 * iii.Interface access-group in and out
 * iv. ***Exception - ignores the above if keyword "remark" is used
 * c. Access-list:
 * i. Standard lists
 * ii. Extended lists with:
 * (1) ACL #
 * (2) Dynamic name
 * (3) Timeout minutes (to be added)
 * (4) Permit or deny
 * (5) Protocol
 * (6) Source + wildcard
 * (7) Destination + wildcard
 * (8) Precedence (to be added)
 * (9) TOS (to be added)
 * (10) Log (to be added)
 */

import java.io.*;
import java.util.*;

public class Parser {
	/***************
	 * Debugging file names
	 * These files will be saved in the source code or project directory when run
	 ***************/
	String testFilename1 = "Test File 1 Parsed output check direct from read.txt";
	String testFilename2 = "Test File 2 Intermediate parser check.txt";
	String testFilename3 = "Test File 3 Last ACL Output.txt";
	String testFilename4 = "Test File 4 Hashtable Output (Post).txt";
	String testFilename5 = "Test File 5 Hashtable Output (from read).txt";
	String testFilename6 = "Test File 6 Last Interface check.txt";
	String testFilename7 = "Test File 7 Files in Folder Check.txt";

	public static enum ACLType{
		standard, extend;
	}


	/*** Creation of a Parser class ***/
	public Parser(NetworkConfig network, File inputDir, File outputDir) throws
	IOException {
		/************************************************************************
		 * Parse the files in the input directory one at a time
		 ************************************************************************/
		String filenames[] = inputDir.list();
		if(network.networkName == null)
			network.networkName = inputDir.getName() ;
		// for debugging
		System.out.println("network name: " + network);
		for(int n=0;n < filenames.length;n++) {

			File inputFile = new File(inputDir,filenames[n]);

			/************************************************************************
			 * Set up a Scanner to read the file using tokens
			 ************************************************************************/
			Scanner OneLine = null;
			try {
				OneLine = new Scanner (inputFile);
				OneLine.useDelimiter("\n");
				//scanner.useDelimiter(System.getProperty("line.separator"));
				// doesn't work for .conf files
			} catch (FileNotFoundException e) {
				System.out.println ("File not found!"); // for debugging
				System.exit (0); // Stop program if no file found
			}

			RouterConfig router = new RouterConfig();
			InterfaceConfig routerInterface = new InterfaceConfig();

			/* Read line by line */
			while (OneLine.hasNext()) {
				/* Read token by token in each line */
				Scanner TokenInLine = new Scanner(OneLine.next());
				String keyword;
				if (TokenInLine.hasNext()) {
					keyword = TokenInLine.next();

					/*****************************************************************
					 * This section handles the hostname
					 *****************************************************************/
					if (keyword.equals("hostname")){
						//router.hostName = TokenInLine.next();
						// use the config file name instead
						router.hostName = inputFile.getName();
						DebugInput(System.out, new ArrayList<String>(), "++++++++parsing " + router.hostName); 
					}

					/******************************************************************
					 * This section handles the interface table creation
					 ******************************************************************/
					else if (keyword.equals("interface")) {
						/* Create a new interface object */
						routerInterface = new InterfaceConfig();
						/* Get the interface name */
						routerInterface.interfaceName = TokenInLine.next();
						if(TokenInLine.hasNext())
						{
							routerInterface.interfaceName = routerInterface.interfaceName + "-" +
									TokenInLine.next();
						}

						// for debugging
						DebugInput(System.out, null, "Parsing " + routerInterface.interfaceName);
						HandleInterface(OneLine, routerInterface, router);
						continue;
					}

					/**************************************************************
					 * This section handles ACL rules that start with "access-list"
					 ***************************************************************/
					else if (keyword.equals("access-list")) {
						HandleAccessList(OneLine, TokenInLine, router);
					}
					/* handles acl rules that start with ip access-list extend/standard*/
					else if(keyword.equals("ip"))
					{
						keyword = TokenInLine.next();
						if(keyword.equals("access-list"))
						{
							HandleAccessListGrouped(OneLine, TokenInLine, router);
						}
					}
				}
			} // end of while (oneline.hasNext())


			/************************************************************************
			 * Save the router to the tableOfRouters object
			 * in the network, using its hostname as the key
			 ************************************************************************/
			if (router.hostName!=null) network.tableOfRouters.put(router.hostName, router);
		} // end of for loop search files in folder

		/************************************************************************
		 * Determine which routers are the neighbors of which
		 ************************************************************************/
		ParseTools.ProcessNeighbors(network.tableOfRouters);
		ParseTools.ProcessNeighborsL2(network.tableOfRouters);

		/************************************************************************
		 * Create packet sets for all the routers in the network
		 ************************************************************************/
		//PacketSet.CreateAllPacketSets (network);

		/************************************************************************
		 * Send the network data to the output directory
		 ************************************************************************/
		//new NetworkDataDump(network, outputDir);

		/************************************************************************
		 * Notify user the parsing has been completed
		 ************************************************************************/
		System.out.println("\r\n\r\n----> Successful completion of parsing <----\r\n");
	} // end of Parser

	/*** this is used to handle the interface
	 * @throws IOException 
	 * the interface is routerInterface, which is stored in router
	 * the interface name has already been stored
	 */
	private void HandleInterface(Scanner OneLine, InterfaceConfig routerInterface, RouterConfig router) throws IOException
	{
		while(OneLine.hasNext()){

			Scanner TokenInLine = new Scanner(OneLine.next());
			ArrayList<String> argument = new ArrayList<String>();
			GetArgument(TokenInLine, argument);

			/* End of interface section is denoted by ! */
			if (argument.get(0).equals("!")) {
				/****************************************************
				 * Store interface objects in Router Config
				 ***************************************************/
				/* Store object in table with name as the key */
				if (routerInterface.interfaceName !=null) {
					/* there may be interface that didn't specify ip address
					 * specify here "no ip address"
					 * to prevent a null pointer error ***/
					if (routerInterface.ipAddress == null) {
						routerInterface.ipAddress = "no ip address";
					}
					router.tableOfInterfaceByNames.put(routerInterface.interfaceName, routerInterface);
				}
				/* Store object in table with ip as the key
				 * If no ip, object will not be stored */
				if (routerInterface.ipAddress != null &&
						!routerInterface.ipAddress.equals("no ip address") )
					router.tableOfInterfaceByIPs.put(routerInterface.ipAddress, routerInterface);
				//finish parsing the interface section
				DebugInput(System.out, null, "Finish parsing interface: " + routerInterface.interfaceName);
				break;
			}

			/* Handle lines "ip address address prefix" */
			if(argument.get(0).equals("ip") && argument.get(1).equals("address") &&
					(! argument.get(2).equals("dhcp")) && (! argument.get(2).equals("negotiated")))
			{
				String ipAddrStr = argument.get(2);
				int slashInd = ipAddrStr.indexOf('/');
				if(slashInd >=0)
				{
					String mask = SeperateMask(ipAddrStr, slashInd);
					routerInterface.ipAddress = ipAddrStr.substring(0, slashInd);
					routerInterface.ipMask = mask;
				}else{
					routerInterface.ipAddress = argument.get(2);
					routerInterface.ipMask = argument.get(3);
				}
				DebugInput(System.out, argument, "getting ip address");
				continue;
			}
			
			/*find layer2 neighbors. consider 'description'  */		
			if(argument.get(0).equalsIgnoreCase("description"))
			{
				if(argument.get(1).equalsIgnoreCase("REMOVED"))
				{
				}else
				{
					router.neighborsLayer2.add(argument.get(1));
				}
				continue;
			}
			
			/* Handle lines "ip access-group aclnum in/out" */
			if(argument.get(0).equals("ip") && argument.get(1).equals("access-group"))
			{
				if(argument.get(3).equals("in"))
				{
					routerInterface.inFilters.add(argument.get(2));
				}else 
					// assume the other case is 'out'
				{
					routerInterface.outFilters.add(argument.get(2));
				}
				DebugInput(System.out, argument, "getting ACL");
				continue;
			}

			/* Handles interfaces with "no ip address" */
			if (argument.get(0).equals("no") && argument.get(1).equals("ip") && 
					argument.get(2).equals("address")) {
				routerInterface.ipAddress = "no ip address";
				DebugInput(System.out, null, "no ip address");
				continue;
			} 

			/* if the interface is shutdown, quit parsing */
			if(argument.get(0).equals("shutdown"))
			{
				DebugInput(System.out, null, "it is shutdown");
				break;
			}
		}

	}

	static void HandleAccessListGrouped(Scanner oneline, Scanner tokeninline, RouterConfig router)throws IOException
	{
		ArrayList<String> argument = new ArrayList<String> ();
		GetArgument(tokeninline, argument);
		while(true){
			//"access-list" already removed		
			LinkedList<ACLRule> oneacl = new LinkedList<ACLRule>();
			ACLType thisType;
			if(argument.get(0).equals("extended"))
			{
				thisType = ACLType.extend;
			}else
			{
				thisType = ACLType.standard;
			}
			String thisNumber = argument.get(1);

			while(true){
				tokeninline = new Scanner(oneline.next());
				GetArgument(tokeninline, argument);
				if(argument.get(0).equals("ip"))
				{//a new ACL, need to add the old one 
					router.tableOfACLs.put(thisNumber, oneacl);
					argument.remove(0);// remove ip
					argument.remove(0);// remove access-list, so that it can restart
					DebugInput(System.out, null, "Add:"+thisNumber);
					break;
				}
				if((!argument.get(0).equals("permit")) && (!argument.get(0).equals("deny")))
				{//this means the end of the ACL definition
					router.tableOfACLs.put(thisNumber, oneacl);
					DebugInput(System.out, null, "Add:"+thisNumber);
					return;
				}
				ACLRule onerule = new ACLRule();
				onerule.accessList = "access-list";
				onerule.accessListNumber = thisNumber;
				CheckPermitDeny(onerule, argument);
				if(thisType == ACLType.extend)
				{
					HandleACLRuleExtend(onerule, argument);
				}else{
					HandleACLRuleStandard(onerule, argument);
				}
				oneacl.add(onerule);
			}
		}

	}

	static void HandleAccessList(Scanner oneline, Scanner tokeninline, RouterConfig router) throws IOException
	{
		ArrayList<String> argument = new ArrayList<String>();
		/**set up argument, 'access-list' has already been parsed*/
		GetArgument(tokeninline, argument);

		// Test Function 1 : output argument array into a test file
		//DebugInput(System.out, argument, "access-list");

		int currentACLNum = -1;
		int preACLNum = -1;
		int[] aclNumbers = {preACLNum, currentACLNum};// pay attention to the order

		ACLRule onerule = null;
		LinkedList<ACLRule> oneacl = new LinkedList<ACLRule>();

		if(CheckValidACL(argument, aclNumbers))
		{
			onerule = new ACLRule();
			onerule.accessList = "access-list";

			preACLNum = aclNumbers[0];
			currentACLNum = aclNumbers[1];
			onerule.accessListNumber = Integer.toString(currentACLNum);
			//
			AddDynamic(onerule, argument);
			CheckPermitDeny(onerule, argument);

			if(CheckACLType(currentACLNum) == ACLType.standard)
			{
				HandleACLRuleStandard(onerule, argument);
			}else
			{
				HandleACLRuleExtend(onerule, argument);
			}
			oneacl.add(onerule);
			//debug
			DebugTools.IntermediateACLRuleCheck(onerule, System.out);
		}


		while(oneline.hasNext())
		{
			String keyword = "";
			tokeninline = new Scanner(oneline.next());
			if(tokeninline.hasNext()){
				keyword = tokeninline.next();
			}else{
				// seems reach the end of file, finish parsing
				break;
			}
			if(keyword.equals("access-list"))
			{
				aclNumbers[0] = preACLNum; 
				aclNumbers[1] = currentACLNum;
				GetArgument(tokeninline, argument);

				if(CheckValidACL(argument, aclNumbers))
				{
					preACLNum = aclNumbers[0];
					currentACLNum = aclNumbers[1];
					if(preACLNum != currentACLNum)
					{
						// finish parsing an acl, add to the router
						router.tableOfACLs.put(Integer.toString(preACLNum), oneacl);

						//debug
						ArrayList<String> oneaclInfo = new ArrayList<String>();
						oneaclInfo.add(oneacl.get(0).accessListNumber);
						oneaclInfo.add(Integer.toString(oneacl.size()));
						DebugInput(System.out, oneaclInfo, "added access-list");

						// get a new one to store
						oneacl = new LinkedList<ACLRule>();
						preACLNum = currentACLNum;
					}

					onerule = new ACLRule();
					onerule.accessList = "access-list";

					onerule.accessListNumber = Integer.toString(currentACLNum);
					//
					AddDynamic(onerule, argument);
					CheckPermitDeny(onerule, argument);

					if(CheckACLType(currentACLNum) == ACLType.standard)
					{
						HandleACLRuleStandard(onerule, argument);
					}else
					{
						HandleACLRuleExtend(onerule, argument);
					}
					//debug
					DebugTools.IntermediateACLRuleCheck(onerule, System.out);
					oneacl.add(onerule);
				}

			}else
			{// the acl part ends
				break;
			}
		}

		// need to add the last acl
		if(currentACLNum != -1)
		{
			//debug
			ArrayList<String> oneaclInfo = new ArrayList<String>();
			oneaclInfo.add(oneacl.get(0).accessListNumber);
			oneaclInfo.add(Integer.toString(oneacl.size()));
			DebugInput(System.out, oneaclInfo, "added access-list");
			router.tableOfACLs.put(Integer.toString(currentACLNum), oneacl);
		}

	}

	public static void GetArgument(Scanner tokeninline, ArrayList<String> argument)
	{
		argument.clear();
		while(tokeninline.hasNext())
		{
			argument.add(tokeninline.next());
		}
	}

	/**
	 * aclNumbers[0] -- previous acl number
	 * aclNumbers[1] -- current acl number
	 * return true the acl is valid
	 * return false the acl is invalid
	 * */
	public static boolean CheckValidACL(ArrayList<String> argument, int[] aclNumbers)
	{
		try{
			if(aclNumbers[0] == -1)
			{
				aclNumbers[0] = Integer.valueOf(argument.get(0));
				aclNumbers[1] = aclNumbers[0];
				argument.remove(0);
			}else
			{
				aclNumbers[1] = Integer.valueOf(argument.get(0));
				argument.remove(0);
			}
		} catch(NumberFormatException e)
		{
			//the acl rule is not valid
			return false;
		}

		if(argument.get(0).equals("remark"))
		{
			return false;
		}else{
			return true;
		}
	}

	public static void DebugInput(PrintStream out, ArrayList<String> argument, String keyword) throws IOException
	{
		out.print (keyword + " | ");
		if(argument != null){
			for (int j=0; j < argument.size(); j++) {
				out.print (argument.get(j) + " | ");
			}
		}
		out.println();
	}

	// seems not useful...
	public static void AddDynamic(ACLRule onerule, ArrayList<String> argument)
	{
		if (argument.get(0).equals ("dynamic")) {
			onerule.dynamic="dynamic";
			argument.remove(0);
			onerule.dynamicName=argument.get(0);
			argument.remove(0);
		}
	}

	public static void CheckPermitDeny(ACLRule onerule, ArrayList<String> argument)
	{
		if(argument.get(0).equals("permit"))
		{
			onerule.permitDeny = "permit";
		}else
		{
			onerule.permitDeny = "deny";
		}
		argument.remove(0); 
	}

	public static void HandleACLRuleStandard(ACLRule onerule, ArrayList<String> argument)
	{
		//acl number has been removed.
		// the first argument is the ip address
		if(argument.get(0).equals("host"))
		{
			argument.remove(0);
		}

		onerule.source = argument.get(0);
		argument.remove(0);
		if(!argument.isEmpty())
		{
			if(!argument.get(0).equals("log")){
				onerule.sourceWildcard = argument.get(0);
			}
		}

	}

	public static void HandleACLRuleExtend(ACLRule onerule, ArrayList<String> argument)
	{
		//first is the protocol
		onerule.protocolLower = ParseTools.GetProtocolNumber(argument.get(0));
		if (onerule.protocolLower.equals("256")) {
			onerule.protocolLower = "0";
			onerule.protocolUpper = "255";
		}else {
			onerule.protocolUpper = onerule.protocolLower;
		}
		argument.remove(0);

		// then is the source field
		if (argument.get(0).equals("any")) {
			onerule.source = "any";
			argument.remove(0);
			ParseTools.ParsePort (onerule, argument, "source");
		}
		else if (argument.get(0).equals("host")) {
			argument.remove(0);
			onerule.source = argument.get(0);
			argument.remove(0);
			ParseTools.ParsePort (onerule, argument, "source");
		}
		else {// the last case uses wildcard
			onerule.source = argument.get(0);
			argument.remove(0);
			onerule.sourceWildcard = argument.get(0);
			argument.remove(0);
			ParseTools.ParsePort (onerule, argument, "source");
		}

		//the last is the destination field
		/*** If the destination keyword is "any" ***/
		if (argument.get(0).equals("any")) {
			onerule.destination = "any";
			argument.remove(0);
			if (!argument.isEmpty()){
				ParseTools.ParsePort (onerule, argument, "destination");
			}
		}
		else if (argument.get(0).equals("host")) {
			argument.remove(0);
			onerule.destination = argument.get(0);
			argument.remove(0);
			if (! argument.isEmpty()){
				ParseTools.ParsePort (onerule, argument, "destination");
			}
		}
		else {
			onerule.destination = argument.get(0);
			argument.remove(0);
			onerule.destinationWildcard = argument.get(0);
			argument.remove(0);
			if (! argument.isEmpty()){
				ParseTools.ParsePort (onerule, argument, "destination");
			}
		}
	}

	public static ACLType CheckACLType(int aclnum)
	{
		if (aclnum >=1 && aclnum <=99 )
			return ACLType.standard ;
		else if (aclnum >=100 && aclnum <=199 )
			return ACLType.extend;
		else if (aclnum >= 1300 && aclnum <=1999 )
			return ACLType.standard;
		else if (aclnum >=2000 && aclnum <=2699 )
			return ACLType.extend;
		//by default
		return ACLType.extend;
	}

	public static String SeperateMask(String instr, int ind)
	{
		int maskNum = Integer.parseInt(instr.substring(ind + 1));
		// maskNum = 0,1,2,...32
		int byteNum = maskNum / 8;
		if(byteNum == 4)
		{
			return "255.255.255.255";
		}
		String mask = "";
		for(int i = 0; i < byteNum; i ++)
		{
			mask = mask + "255.";
		}

		int rest = maskNum%8;
		int nextByteNum = (int) Utility.SumPower2(7 - rest + 1, 7);
		mask = mask + Integer.toString(nextByteNum);

		//fill up the rest bytes with "0"
		for(int i = 0; i < 4 - byteNum - 1; i ++)
		{
			mask = mask + ".0";
		}

		return mask;
	}


	public static void main(String args[]) throws IOException {
		// test parser
		System.out.println(System.getProperty("user.dir"));
		System.out.println(System.getProperty("os.name"));
		String ParentDir = "./";

		String networkName = "purdue";
		NetworkConfig network = new NetworkConfig(networkName);
		File inputDir = new File(ParentDir + "configure-" + networkName);
		File outputDir = new File(ParentDir + "result");
		new Parser(network, inputDir, outputDir);
		//ParseTools.SaveNetwork(network, networkName + ".ser");

	}

}
