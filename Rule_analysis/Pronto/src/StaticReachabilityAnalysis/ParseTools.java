package StaticReachabilityAnalysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import PlotTools.GraphViz;
import PlotTools.dotEdge;

public class ParseTools {

	/************************************************************************************
	 *
	 * Function to calculate the network prefix of an IP address.
	 * Input : IP address (string), IP mask (string)
	 * Output : Network prefix (string)
	 *
	 ************************************************************************************/
	static String GetPrefix (String ip, String mask) {
		String Prefix;
		int ipOctet1=0, ipOctet2=0, ipOctet3=0, ipOctet4=0;
		int maskOctet1=0, maskOctet2=0, maskOctet3=0, maskOctet4=0;
		/*** Break up ip string into octets ***/
		Scanner s = new Scanner(ip).useDelimiter("\\.");
		ipOctet1 = Integer.parseInt(s.next());
		ipOctet2 = Integer.parseInt(s.next());
		ipOctet3 = Integer.parseInt(s.next());
		ipOctet4 = Integer.parseInt(s.next());
		/*** Break up mask string into octets ***/
		s = new Scanner(mask).useDelimiter("\\.");
		maskOctet1 = Integer.parseInt(s.next());
		maskOctet2 = Integer.parseInt(s.next());
		maskOctet3 = Integer.parseInt(s.next());
		maskOctet4 = Integer.parseInt(s.next());
		/*** Determine the network prefix based on the ip and mask ***/
		Prefix = Integer.toString(ipOctet1 & maskOctet1) + "." +
				Integer.toString(ipOctet2 & maskOctet2) + "." +
				Integer.toString(ipOctet3 & maskOctet3) + "." +
				Integer.toString(ipOctet4 & maskOctet4);
		return Prefix;
	}

	/**
	 * 
	 * @param mask
	 * @return the length of the prefix
	 * 255.255.255.0 -> 24
	 */
	static int GetPrefixLength(String mask)
	{
		String [] masks = mask.split("\\.");
		int numofzeros = 0;
		for(int i = masks.length - 1; i >=0; i --)
		{
			int offset = 1;
			int maskInt = Integer.parseInt(masks[i]);

			for(int j = 0; j < 8; j ++)
			{
				offset = offset << j;
				if((maskInt & offset )>0)
				{
					return masks.length * 8 - numofzeros;
				}
				numofzeros ++;
			}
		}
		return masks.length * 8 - numofzeros;
	}

	/************************************************************************************
	 *
	 * Function to convert ports in an ACL rule to its IANA number assignment.
	 * Input : ACL rule (ACLrule), parsed argument (string), position k (int),
	 * port name or number (string)
	 * Output : position k, which is one position after reading the port arguments
	 * The upper and lower port numbers are stored in the ACL rule that was
			input.
	 * Limitation: Does not handle neq operator yet
	 *
	 ************************************************************************************/
	static int ParsePort (ACLRule aclRule, String[] argument, int k, String port) {
		// handles eq, gt, lt, range
		// to include handling for neq
		if (argument[k].equalsIgnoreCase("eq")) { // handles eq
			k++;
			if (port.equals("source")) {
				aclRule.sourcePortLower = GetPortNumber (argument[k]);
				aclRule.sourcePortUpper = aclRule.sourcePortLower;
				k++;
			}
			else if (port.equals("destination")) {
				aclRule.destinationPortLower = GetPortNumber (argument[k]);
				aclRule.destinationPortUpper = aclRule.destinationPortLower;
			}
		}
		else if (argument[k].equalsIgnoreCase("gt")) {
			k++;
			if (port.equals("source")) {
				aclRule.sourcePortLower = GetPortNumber (argument[k]);
				k++;
			}
			else if (port.equals("destination"))
				aclRule.destinationPortLower = GetPortNumber (argument[k]);
		}
		else if (argument[k].equalsIgnoreCase("lt")) {
			k++;
			if (port.equals("source")) {
				aclRule.sourcePortUpper = GetPortNumber (argument[k]);
				k++;
			}
			else if (port.equals("destination"))
				aclRule.destinationPortUpper = GetPortNumber (argument[k]);
		}
		else if (argument[k].equalsIgnoreCase("range")) {
			k++;
			if (port.equals("source")) {
				aclRule.sourcePortLower = GetPortNumber (argument[k]);
				k++;
				aclRule.sourcePortUpper = GetPortNumber (argument[k]);
				k++;
			}
			else if (port.equals("destination")) {
				aclRule.destinationPortLower = GetPortNumber (argument[k]);
				k++;
				aclRule.destinationPortUpper = GetPortNumber (argument[k]);
			}
		}
		return k;
	}

	static void ParsePort (ACLRule aclRule, ArrayList<String> argument, String port) {
		// handles eq, gt, lt, range
		// to include handling for neq
		if (argument.get(0).equalsIgnoreCase("eq")) { // handles eq
			argument.remove(0);
			if (port.equals("source")) {
				aclRule.sourcePortLower = GetPortNumber (argument.get(0));
				aclRule.sourcePortUpper = aclRule.sourcePortLower;
				argument.remove(0);
			}
			else if (port.equals("destination")) {
				aclRule.destinationPortLower = GetPortNumber (argument.get(0));
				aclRule.destinationPortUpper = aclRule.destinationPortLower;
			}
		}
		else if (argument.get(0).equalsIgnoreCase("gt")) {
			argument.remove(0);
			if (port.equals("source")) {
				aclRule.sourcePortLower = GetPortNumber (argument.get(0));
				argument.remove(0);
			}
			else if (port.equals("destination"))
				aclRule.destinationPortLower = GetPortNumber (argument.get(0));
		}
		else if (argument.get(0).equalsIgnoreCase("lt")) {
			argument.remove(0);
			if (port.equals("source")) {
				aclRule.sourcePortUpper = GetPortNumber (argument.get(0));
				argument.remove(0);
			}
			else if (port.equals("destination"))
				aclRule.destinationPortUpper = GetPortNumber (argument.get(0));
		}
		else if (argument.get(0).equalsIgnoreCase("range")) {
			argument.remove(0);
			if (port.equals("source")) {
				aclRule.sourcePortLower = GetPortNumber (argument.get(0));
				argument.remove(0);
				aclRule.sourcePortUpper = GetPortNumber (argument.get(0));
				argument.remove(0);
			}
			else if (port.equals("destination")) {
				aclRule.destinationPortLower = GetPortNumber (argument.get(0));
				argument.remove(0);
				aclRule.destinationPortUpper = GetPortNumber (argument.get(0));
			}
		}
	}

	/************************************************************************************
	 *
	 * Function to lookup the IANA number assignment of a port.
	 * Input : port name or number (string)
	 * Output : port number (string)
	 *
	 ************************************************************************************/
	static String GetPortNumber (String port) {
		String portNumber;
		// Note switch statement does not work with strings
		if (port.equalsIgnoreCase("tcpmux")) portNumber = "1";
		else if (port.equalsIgnoreCase("ftp-data")) portNumber = "20";
		else if (port.equalsIgnoreCase("ftp")) portNumber = "21";
		else if (port.equalsIgnoreCase("ssh")) portNumber = "22";
		else if (port.equalsIgnoreCase("telnet")) portNumber = "23";
		else if (port.equalsIgnoreCase("smtp")) portNumber= "25";
		else if (port.equalsIgnoreCase("dsp")) portNumber= "33";
		else if (port.equalsIgnoreCase("time")) portNumber= "37";
		else if (port.equalsIgnoreCase("rap")) portNumber= "38";
		else if (port.equalsIgnoreCase("rlp")) portNumber= "39";
		else if (port.equalsIgnoreCase("name")) portNumber= "42";
		else if (port.equalsIgnoreCase("nameserver")) portNumber= "42";
		else if (port.equalsIgnoreCase("nicname")) portNumber= "43";
		else if (port.equalsIgnoreCase("dns")) portNumber = "53";
		else if (port.equalsIgnoreCase("domain")) portNumber = "53";
		else if (port.equalsIgnoreCase("bootps")) portNumber = "67";
		else if (port.equalsIgnoreCase("bootpc")) portNumber = "68";
		else if (port.equalsIgnoreCase("tftp")) portNumber = "69";
		else if (port.equalsIgnoreCase("gopher")) portNumber = "70";
		else if (port.equalsIgnoreCase("finger")) portNumber = "79";
		else if (port.equalsIgnoreCase("http")) portNumber = "80";
		else if (port.equalsIgnoreCase("www")) portNumber = "80";
		else if (port.equalsIgnoreCase("kerberos")) portNumber = "88";
		else if (port.equalsIgnoreCase("pop2")) portNumber = "109";
		else if (port.equalsIgnoreCase("pop3")) portNumber = "110";
		else if (port.equalsIgnoreCase("sunrpc")) portNumber = "111";
		else if (port.equalsIgnoreCase("ident")) portNumber = "113";
		else if (port.equalsIgnoreCase("auth")) portNumber = "113";
		else if (port.equalsIgnoreCase("sftp")) portNumber = "115";
		else if (port.equalsIgnoreCase("nntp")) portNumber = "119";
		else if (port.equalsIgnoreCase("ntp")) portNumber = "123";
		else if (port.equalsIgnoreCase("netbios-ns")) portNumber = "137";
		else if (port.toLowerCase().startsWith("netbios-dg")) portNumber = "138";
		else if (port.toLowerCase().startsWith("netbios-ss")) portNumber = "139";
		else if (port.equalsIgnoreCase("sqlsrv")) portNumber = "156";
		else if (port.equalsIgnoreCase("snmp")) portNumber = "161";
		else if (port.equalsIgnoreCase("snmptrap")) portNumber = "162";
		else if (port.equalsIgnoreCase("bgp")) portNumber = "179";
		else if (port.equalsIgnoreCase("exec")) portNumber = "512";
		else if (port.equalsIgnoreCase("shell")) portNumber = "514";
		else if (port.equalsIgnoreCase("isakmp")) portNumber = "500";
		else if (port.equalsIgnoreCase("biff")) portNumber = "512";
		else if (port.equalsIgnoreCase("lpd")) portNumber = "515";
		else if (port.equalsIgnoreCase("cmd")) portNumber = "514";
		else if (port.equalsIgnoreCase("syslog")) portNumber = "514";
		else if (port.equalsIgnoreCase("whois")) portNumber = "43";
		else if (port.equals("lLth9012b03GJ")) portNumber = "390"; //this is ad hoc
		else portNumber = port;
		return portNumber;
	}

	/*
	 * plot the connection of routers, ignore multiple links between the same pair of routers
	 */
	static boolean PlotTopo(Hashtable<String, RouterConfig> RTable, String figName, String figType)
	{

		//store routers already considered
		HashSet<RouterConfig> plotted = new HashSet<RouterConfig> ();
		//plot the topology
		GraphViz gv = new GraphViz(figName,figType);
		gv.start_graph();

		Collection<RouterConfig> rList = RTable.values();

		for(RouterConfig rc : rList)
		{
			plotted.add(rc);
			Collection<InterfaceConfig> iList = rc.tableOfInterfaceByIPs.values();
			for(InterfaceConfig ic : iList)
			{
				for(int i = 0; i < ic.neighbors.size(); i ++)
				{
					String neighborStr = ic.neighbors.get(i);
					int routerInd = neighborStr.indexOf("-");
					String neighborRouterStr = neighborStr.substring(0, routerInd);
					RouterConfig neighborRouter = RTable.get(neighborRouterStr);
					if(neighborRouter == null)
					{
						System.err.println("find bad router name: " + neighborRouterStr);
						return false;
					}
					if(!plotted.contains(neighborRouter))
					{
						//generate one edge
						dotEdge edge = new dotEdge(rc.hostName, neighborRouter.hostName);
						edge.Undirected();
						gv.addln(edge.toString());
					}

				}
			}
		}

		gv.end_graph();
		gv.writeDotSourceToFile();

		return true;
	}

	/************************************************************************************
	 *
	 * Function to determine the neighbors of routers in a network.
	 * Checks the interface on each router against the interface on every other
	 * router.
	 * Store the IPs of each router's neighbors in its respective interface objects.
	 * @throws IOException 
	 *
	 ************************************************************************************/
	static boolean ProcessNeighbors (Hashtable<String, RouterConfig> RTable) throws IOException {
		boolean success=false;

		int linkCounter = 0;
		HashSet<RouterConfig> involvedRouter = new HashSet<RouterConfig>(); 

		Collection<RouterConfig> rList1 = RTable.values();
		Collection<RouterConfig> rList2 = RTable.values();

		for(RouterConfig rc1 : rList1)
		{
			Parser.DebugInput(System.out, null, "---processing " + rc1.hostName);
			for(RouterConfig rc2 : rList2)
			{
				if(!rc1.hostName.equals(rc2.hostName)){
					linkCounter = linkCounter + ProcessRouterPair(rc1, rc2, involvedRouter);
				}
			}
		}

		System.out.println("number of links: " + linkCounter);
		System.out.println("number of routers: " + involvedRouter.size());
		return success;
	}

	/************************************************************************************
	 *
	 * make layer2 neighbors are consistent. This means:
	 * if A and B are neighbors, then A is in B's neighbor list and vice versa.
	 *
	 ************************************************************************************/
	static boolean ProcessNeighborsL2 (Hashtable<String, RouterConfig> RTable) {

		Collection<RouterConfig> rList1 = RTable.values();
		for(RouterConfig rc1 : rList1)
		{
			Iterator<String> neighborsL2 = rc1.neighborsLayer2.iterator();
			while(neighborsL2.hasNext())
			{
				String nL2 = neighborsL2.next();
				RouterConfig neighborRouter = RTable.get(nL2);
				neighborRouter.neighborsLayer2.add(rc1.hostName);
				//System.out.println(rc1.hostName + "-" + nL2);
			}

		}

		System.out.println("Layer 2 neighbors are processed");
		return true;
	}

	/*
	 * Input: rc1, rc2 - two router configure
	 * check all the interfaces in rc1 and rc2 and find neighbors. 
	 * if rc1 and rc2 are neighbors, then add them in checkedList.
	 * return: the number of neighbor links between rc1 and rc2
	 */
	static int ProcessRouterPair(RouterConfig rc1, RouterConfig rc2, HashSet<RouterConfig> checkedList)
	{
		int linkCtr = 0;
		Collection<InterfaceConfig> interfaceList1 = rc1.tableOfInterfaceByIPs.values(); 
		Collection<InterfaceConfig> interfaceList2 = rc2.tableOfInterfaceByIPs.values();
		//System.out.println("[" + interfaceList1.size() +" "+ interfaceList2.size() + "]");
		//System.out.println(rc1.hostName + " vs " + rc2.hostName);
		for(InterfaceConfig iConfig1 : interfaceList1)
		{
			for(InterfaceConfig iConfig2 : interfaceList2)
			{
				if(ComparePrefix(iConfig1, iConfig2))
				{
					iConfig1.neighbors.add(rc2.hostName + "-" + iConfig2.interfaceName);
					//iConfig2.neighbors.add(rc1.hostName + "-" + iConfig1.interfaceName);
					if(!(iConfig1.interfaceName.contains("Vlan") && iConfig2.interfaceName.contains("Vlan")))
						System.out.println("find neighbour:" + rc1.hostName + "-" + 
								iConfig1.interfaceName + "-" + rc2.hostName + "-" + 
								iConfig2.interfaceName);
					linkCtr ++;
					checkedList.add(rc1);
				}
			}
		}
		//System.out.println("link couter = " + linkCtr);
		return linkCtr;
	}

	static boolean ComparePrefix(InterfaceConfig currentInterface, InterfaceConfig interfaceToCompare)
	{
		//System.out.println(currentInterface.interfaceName + " vs " + interfaceToCompare.interfaceName);
		if(currentInterface.ipMask.equals(interfaceToCompare.ipMask))
		{
			String prefix1 = ParseTools.GetPrefix (currentInterface.ipAddress,
					currentInterface.ipMask);
			String prefix2 = ParseTools.GetPrefix (interfaceToCompare.ipAddress,
					interfaceToCompare.ipMask);
			if(prefix1.equals(prefix2))
			{
				return true;
			}
		}
		return false;
	}

	/************************************************************************************
	 *
	 * Function to lookup the IANA number assignment of a protocol.
	 * Input : protocol name or number (string)
	 * Output : protocol number (string)
	 *
	 ************************************************************************************/
	static String GetProtocolNumber (String protocol) {
		String protocolNumber;
		if(protocol.equalsIgnoreCase("icmp")) protocolNumber = "1" ;
		else if(protocol.equalsIgnoreCase("igmp")) protocolNumber = "2" ;
		//else if(protocol.equalsIgnoreCase("ip")) protocolNumber = "4" ;
		else if(protocol.equalsIgnoreCase("ip")) protocolNumber = "256" ;
		// special case to indicate all protocols. No actual protocol number 256.
		else if(protocol.equalsIgnoreCase("tcp")) protocolNumber = "6" ;
		else if(protocol.equalsIgnoreCase("egp")) protocolNumber = "8" ;
		else if(protocol.equalsIgnoreCase("igp")) protocolNumber = "9" ;
		else if(protocol.equalsIgnoreCase("udp")) protocolNumber = "17" ;
		else if(protocol.equalsIgnoreCase("rdp")) protocolNumber = "27" ;
		else if(protocol.equalsIgnoreCase("ipv6")) protocolNumber = "41" ;
		else if(protocol.equalsIgnoreCase("rsvp")) protocolNumber = "46" ;
		else if(protocol.equalsIgnoreCase("eigrp")) protocolNumber = "88" ;
		else if(protocol.equalsIgnoreCase("l2tp")) protocolNumber = "115" ;
		else if(protocol.equalsIgnoreCase("esp")) protocolNumber = "50";
		else if(protocol.equalsIgnoreCase("ahp")) protocolNumber = "51";
		else if(protocol.equalsIgnoreCase("gre")) protocolNumber = "47";
		else if(protocol.equalsIgnoreCase("ospf")) protocolNumber = "89";
		else {
			System.out.println("Unknown Protocol: " + protocol + "----------------------------");
			protocolNumber = protocol ;
		}
		return protocolNumber;
	}

	/*
	 * parse one router config file, new router config is stored in router
	 */
	static boolean ParseRouter(RouterConfig router, File inputFile) throws IOException{
		boolean success = false;
		/************************************************************************
		 * Set up ACLs, Router Interfaces
		 ************************************************************************/
		String currentACLnumber = new String();
		String previousACLnumber = new String();
		LinkedList<ACLRule> acl = new LinkedList<ACLRule>();
		//RouterConfig router = new RouterConfig();
		InterfaceConfig routerInterface = new InterfaceConfig();
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
		boolean interfaceFlag = false; // to check if interface is being processed
		/* Read line by line */
		while (scanner.hasNext()) {
			/* Read token by token in each line */
			Scanner lineScanner = new Scanner(scanner.next());
			String keyword;
			if (lineScanner.hasNext()) {
				keyword = lineScanner.next();
				// for debugging
				//System.out.println("keyword: " + keyword);
				/*****************************************************************
				 * This section handles the hostname
				 *****************************************************************/
				if (keyword.equals("hostname")){
					router.hostName=lineScanner.next();
					//outputTestFile1.write(router.hostName + "\r\n"); // for debugging
					//for debugging
					//System.out.println(router);

				}
				/******************************************************************
				 * This section handles the interface table creation
				 ******************************************************************/
				else if (keyword.equals("interface")) {
					/* Create a new interface object */
					routerInterface = new InterfaceConfig();
					/* Get the interface name */
					routerInterface.interfaceName = lineScanner.next();
					//outputTestFile1.write(routerInterface.interfaceName + "\r\n"); // for debugging
					// for debugging
					System.out.println(routerInterface);
					interfaceFlag = true;
					continue;
				}
				/************************************************************************
				 * This section handles the interface configuration section
				 ************************************************************************/
				else if (interfaceFlag) {
					/* End of interface section is denoted by ! */
					if (keyword.equals("!")) {
						interfaceFlag = false;
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
						continue;
					}
					/* Handle lines that begin with "ip" */
					else if (keyword.equals("ip")) {
						//outputTestFile1.write (keyword + " | "); // for debugging
						String IP_argument_1, IP_argument_2, IP_argument_3;
						IP_argument_1 = lineScanner.next();
						if (IP_argument_1.equals("address") ||
								IP_argument_1.equals("access-group")){
							IP_argument_2 = lineScanner.next();
							IP_argument_3 = lineScanner.next();
							if (IP_argument_1.equals ("address")){
								routerInterface.ipAddress = IP_argument_2;
								routerInterface.ipMask = IP_argument_3;
							}
							else if (IP_argument_1.equals ("access-group")) {
								if (IP_argument_3.equals("in"))
									routerInterface.inFilters.add(IP_argument_2);
								else if (IP_argument_3.equals("out"))
									routerInterface.outFilters.add(IP_argument_2);
							}
							//outputTestFile1.write (keyword + " | " + IP_argument_1 + " | " + IP_argument_2 + " | " + IP_argument_3 + "\r\n" );
							// for debugging
						}
					} // End of else if (keyword.equals("ip"))
					/* Handles interfaces with no ip specified, keyword "no" */
					else if (keyword.equals("no")) {
						String nextArgument = lineScanner.next();
						if (nextArgument.equals("ip")) {
							nextArgument = lineScanner.next();
							if (nextArgument.equals("address")) {
								routerInterface.ipAddress = "no ip address";
								//outputTestFile1.write (keyword + " | no ip address + \r\n");
							}
						}
					} // End of else if (keyword.equals("no"))
				} // end of else if (interfaceFlag)
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
						if (argument[i].equals ("!")) break;
						i++;
					}
					// Test Function 1 : output argument array into a test file
					//for checking
					//outputTestFile1.write (keyword + " | ");
					//for (int j=0; j<i; j++) outputTestFile1.write (argument[j] + " | ");
					//end of Test Function 1
					/************************************************************************
					 * Parse argument array into correct ACL rule structure
					 ************************************************************************/
					if (argument[0].equals ("rate-limit")) break labelBreakHere;
					//Skip this section if "rate-limit" found
					aclRule.accessListNumber = argument[0];
					currentACLnumber = argument[0];
					//Initialize previousACLnumber
					if (previousACLnumber.length()==0) previousACLnumber =
							currentACLnumber;
					//Check whether this is a new ACL
					if (currentACLnumber.equals(previousACLnumber)) ; // do nothing
					else {
						//if different ACL number, store the previous ACL in router config
						router.tableOfACLs.put(previousACLnumber, acl);
						//outputTestFile5.write("\r\nPrevious ACL #" + previousACLnumber
						//		+ "\r\nCurrent ACL #" + currentACLnumber + "\r\n");
						//outputTestFile5.write(acl + "\r\n");
						acl = new LinkedList<ACLRule>();
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
							if (Integer.valueOf(currentACLnumber)>=1 &&
									Integer.valueOf(currentACLnumber)<=99 )
								standardIPAccessList=true ;
							else if (Integer.valueOf(currentACLnumber)>=100 &&
									Integer.valueOf(currentACLnumber)<=199 )
								extendedIPAccessList=true;
							else if (Integer.valueOf(currentACLnumber)>=1300 &&
									Integer.valueOf(currentACLnumber)<=1999 )
								standardIPAccessList=true ;
							else if (Integer.valueOf(currentACLnumber)>=2000 &&
									Integer.valueOf(currentACLnumber)<=2699 )
								extendedIPAccessList=true;
							/*********************************************
							 * Process standard ACL rules
							 *********************************************/
							if (standardIPAccessList) {
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
								aclRule.protocolLower = GetProtocolNumber
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
					//new DebugTools().IntermediateParserCheck(aclRule, outputTestFile2); // for debugging
					//outputTestFile1.write ("\r\n" ); // for debugging
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
			router.tableOfACLs.put(previousACLnumber, acl);
			//outputTestFile5.write("\r\n" + previousACLnumber + "\r\n" + currentACLnumber + "\r\n"); // for debugging
			//outputTestFile5.write(acl + "\r\n"); // for debugging
		}
		//outputTestFile4.println(router);
		//outputTestFile6.println(routerInterface);
		//new DebugTools().CheckLastACLOutput(acl,outputTestFile3);
		//System.out.println(router.hostName); // for debugging
		/************************************************************************
		 * Save the router to the tableOfRouters object
		 * in the network, using its hostname as the key
		 ************************************************************************/
		//if (router.hostName!=null) network.tableOfRouters.put(router.hostName, router);

		/*** Close all the debugging/test files ***/
		//outputTestFile1.close();
		//outputTestFile2.close();
		//outputTestFile3.close();
		//outputTestFile4.close();
		//outputTestFile5.close();
		//outputTestFile6.close();
		//outputTestFile7.close();
		/************************************************************************
		 * Determine which routers are the neighbors of which
		 ************************************************************************/
		//boolean successProcessNeighbors = ProcessNeighbors(network.tableOfRouters);
		/************************************************************************
		 * Create packet sets for all the routers in the network
		 ************************************************************************/
		PacketSet.CreatePacketSet(router);
		/************************************************************************
		 * Send the network data to the output directory
		 ************************************************************************/
		//new NetworkDataDump(network, outputDir);
		/************************************************************************
		 * Notify user the parsing has been completed
		 ************************************************************************/
		System.out.println("\r\n\r\n----> Successful completion of parsing <----\r\n");
		return success;	
	}

	static boolean SaveNetwork(NetworkConfig net, String filename)
	{

		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(net);
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	public static NetworkConfig LoadNetwork(String filename)
	{

		FileInputStream fis = null;
		ObjectInputStream in = null;
		NetworkConfig net = null;
		try
		{
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			net = (NetworkConfig)in.readObject();
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
		System.out.println("Network " + net.networkName + " is loaded.");
		return net;
	}

	public static void main(String args[]) {
		// test get prefix
		String ipaddr = "172.140.23.14";
		String prefix = "255.255.255.252";
		System.out.println(GetPrefix(ipaddr, prefix));

		System.out.println(Parser.SeperateMask("/32", 0));
		System.out.println(Parser.SeperateMask("/30", 0));
		System.out.println(Parser.SeperateMask("/16", 0));
		System.out.println(Parser.SeperateMask("/17", 0));

		System.out.println(GetPrefixLength("255.255.255.255"));
		System.out.println(GetPrefixLength("255.255.252.0"));
		System.out.println(GetPrefixLength("255.254.0.0"));
	}

}
