package StaticReachabilityAnalysis;
/*
 * RouterConfig.java
 *
 * Created on August 16, 2006, 1:20 AM
 *
 * Last modified August 22, 2006
 *
 * Contains the router configuration class
 */
import java.util.*;
import java.io.*;

public class RouterConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1379022203094263446L;
	String hostName; // Router name
	Hashtable<String, InterfaceConfig> tableOfInterfaceByNames; // Table of pointers to Interface objects
	// keyed by the interface names
	Hashtable<String, InterfaceConfig> tableOfInterfaceByIPs; // Table of pointers to Interface objects
	// keyed by the interface IPs
	Hashtable<String, LinkedList<ACLRule>> tableOfACLs; // Table of pointers to ACLs
	//string acl numbers : packetset 
	TreeMap mapOfPacketSets; // Map of packet filters in tuple-form
	// Use the ACL numbers as keys to point to the packetfilter objects (tuples)
	//integer is used to express the atomic predicate is expressed by which link predicates 
	Hashtable<String, Integer> APWithBitmap;
	//store the packet set expression of the atomic predicate
	Hashtable<String, PacketSet> APWithPS;
	HashSet<String> neighborsLayer2; // layer 2 neighbors
	//number of Atomic Predicates 
	int APCount;
	
	/** Creates a new instance of RouterConfig */
	RouterConfig() {
		hostName = null;
		tableOfInterfaceByNames = new Hashtable<String, InterfaceConfig>();
		tableOfInterfaceByIPs = new Hashtable<String, InterfaceConfig>();
		tableOfACLs = new Hashtable<String, LinkedList<ACLRule>>();
		mapOfPacketSets = new TreeMap(new TComp());
		APCount = 0;
		APWithBitmap = new Hashtable<String, Integer>();
		APWithPS = new Hashtable<String, PacketSet>();
		neighborsLayer2 = new HashSet<String>();
		//Interface = null; // doesn't work if set to null --> it's a pointer!
	}
	
	public String printLayer2Neighbors()
	{
		String neighbors = "";
		for (String an : neighborsLayer2)
		{
			neighbors = neighbors + " " + an;
		}
		return neighbors;
	}
	
	public Collection<InterfaceConfig> getInterfaceWithIP()
	{
		return tableOfInterfaceByIPs.values();
	}
	
	// not used???
	public String toString() {
		return hostName + " "
				+ tableOfInterfaceByNames + " "
				+ tableOfInterfaceByIPs + " "
				+ tableOfACLs + " "
				+ mapOfPacketSets + " ";
	}
	
	public String getname()
	{
		return hostName;
	}
	
	public Hashtable<String, LinkedList<ACLRule>> getACLs()
	{
		return tableOfACLs;
	}
	
	//create PacketSet for each interface
	static void InterfacePacketSet(RouterConfig router)
	{
		Enumeration<String> interfacelist = router.tableOfInterfaceByIPs.keys();
		while(interfacelist.hasMoreElements()) {
			String Interface_IP = (String) interfacelist.nextElement();
			System.out.println("Interface IP : " + Interface_IP);
			InterfaceConfig oneInterface = (InterfaceConfig) 
					router.tableOfInterfaceByIPs.get(Interface_IP);
			PacketSet filter = PacketSet.NoFilters();
			for (int i = 0; i < oneInterface.outFilters.size(); i++)
			{
				PacketSet filterTMP = new PacketSet();
				PacketSet ps1 = (PacketSet)
						router.mapOfPacketSets.get(oneInterface.outFilters.get(i));

				PacketSet.Intersection(ps1, filter, filterTMP);
				filter = filterTMP;
				//filterTMP will be cleared in Intersection...
			}
			oneInterface.outPacketSet = filter;
			filter = PacketSet.NoFilters();
			for (int i = 0; i < oneInterface.inFilters.size(); i ++)
			{
				PacketSet filterTMP = new PacketSet();
				PacketSet ps1 = (PacketSet) 
						router.mapOfPacketSets.get(oneInterface.inFilters.get(i));
				PacketSet.Intersection(ps1, filter, filterTMP);
				filter = filterTMP;
			}
			oneInterface.inPacketSet = filter;
			//System.out.println(oneInterface.outPacketSet);
			//System.out.println(oneInterface.inPacketSet);
		}
	}
	
	// atomic predicate of a router
	static void AtomicPredicateOfRouter(RouterConfig router)
	{
		int interfaceNum = router.tableOfInterfaceByIPs.size();
		router.APCount = 0;
		//System.out.println(interfaceNum);
		//cannot have too many interfaces...
		if(interfaceNum > 30)
		{
			try {
				throw new Exception("cannot have too many interfaces now...");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < (int)Math.pow(2, interfaceNum); i++)
		{
			PacketSet filter = PacketSet.NoFilters();

			Enumeration interfacelist = router.tableOfInterfaceByIPs.keys();
			boolean addFlag = true;
			for(int j = 0; j < interfaceNum; j ++) {
				String Interface_IP = (String) interfacelist.nextElement();
				//System.out.println("Interface IP : " + Interface_IP);
				InterfaceConfig oneInterface = (InterfaceConfig) 
						router.tableOfInterfaceByIPs.get(Interface_IP);
				// 1 - not change; 0 - negate
				int action = (i>>j) & 1;
				//System.out.println(i + " " + j);
				if(action == 1)
				{
					PacketSet filterTMP = new PacketSet();
					PacketSet.Intersection(oneInterface.outPacketSet, filter, filterTMP);
					filter = filterTMP;
				}else{
					//then we need to negate it
					PacketSet filterTMP = new PacketSet();
					PacketSet nps = new PacketSet();
					PacketSet.Negation(oneInterface.outPacketSet, nps);
					//System.out.println("nps");
					//System.out.println(nps);
					if(nps.IsFalse())
					{// shortcut...
						addFlag = false;
						break;
					}
					PacketSet.Intersection(nps, filter, filterTMP);
					filter = filterTMP;
				}
				if(filter.IsFalse())
				{
					//short cut... hope this will be the most cases...s
					addFlag = false;
					break;
				}

			}
			//System.out.println(filter);
			if(addFlag)
			{
				String APName = "AP" + router.APCount;
				router.APCount = router.APCount + 1;
				//record i
				System.out.println(APName + " " + i);
				router.APWithBitmap.put(APName, i);
				//record packetset
				router.APWithPS.put(APName, filter);
				//generate expression
				interfacelist = router.tableOfInterfaceByIPs.keys();
				for(int j = 0; j <  interfaceNum; j ++)
				{
					String Interface_IP = (String) interfacelist.nextElement();
					//System.out.println("Interface IP : " + Interface_IP);
					InterfaceConfig oneInterface = (InterfaceConfig) 
							router.tableOfInterfaceByIPs.get(Interface_IP);
					// 1 - add; 0 - not add
					int action = (i>>j) & 1;
					if(action == 1)
					{
						oneInterface.outAPExpr.add(APName);
					}
				}
			}
		}
	}
	
	//Do a dump of the RouterConfig object for debugging/checking purposes
	void DebugAP()
	{
		Enumeration APlist = APWithPS.keys();
		while(APlist.hasMoreElements())
		{
			String APName = (String)APlist.nextElement();
			System.out.println(APName + ":");
			PacketSet ps = APWithPS.get(APName);
			System.out.println(ps);
		}
		Enumeration interfacelist = tableOfInterfaceByIPs.keys();
		while(interfacelist.hasMoreElements()) {
			String Interface_IP = (String) interfacelist.nextElement();
			//System.out.println("Interface IP : " + Interface_IP);
			InterfaceConfig oneInterface = (InterfaceConfig) tableOfInterfaceByIPs.get(Interface_IP);
			System.out.println(Interface_IP + ":");
			for(int i = 0; i < oneInterface.outAPExpr.size(); i ++)
			{
				System.out.print(oneInterface.outAPExpr.get(i) + " ");
			}
			System.out.println();
		}
	}
	
	static void Debug(RouterConfig a, File outputDir) {
		try {
			String outputFileName = "Router Dump - ".concat(a.hostName.concat(".txt"));
			File Output = new File (outputDir, outputFileName);
			PrintWriter out = new PrintWriter (new BufferedWriter
					(new FileWriter(Output)));
			//Set up debugger object exactly like RouterConfig class
			//This eases checking that the debugger handles everything in the class
			String debugHostName = a.hostName;
			Hashtable debugTableOfInterfaceByNames = a.tableOfInterfaceByNames;
			Hashtable debugTableOfInterfaceByIPs = a.tableOfInterfaceByIPs;
			Hashtable debugTableOfACLs = a.tableOfACLs;
			TreeMap debugMapOfPacketSets = a.mapOfPacketSets ;
			ACLRule debugACLrule;
			LinkedList debugACL;
			InterfaceConfig debugInterfaces;
			out.println("Router Config Debugger");
			out.println("----------------------\r\n");
			out.println("Host name : " + debugHostName + "\r\n");
			out.println("-----------------------------------------------" +"\r\n");
			String acl;
			Set set = debugMapOfPacketSets.entrySet();
			Iterator aclSet = set.iterator();
			while (aclSet.hasNext()) {
				Map.Entry mapPS = (Map.Entry) aclSet.next();
				acl = (String) mapPS.getKey();
				out.println("ACL number : " + acl);
				debugACL = (LinkedList) debugTableOfACLs.get(acl);
				int LL_size = debugACL.size();
				out.println("# of ACL rules : " + LL_size);
				out.println("*************************");
				out.println(debugMapOfPacketSets.get(acl));
				int counter = 0;
				while(counter < LL_size) {
					debugACLrule = (ACLRule) debugACL.get(counter);
					out.println(" Keyword / ACL# : " + debugACLrule.accessList
							+ " / " + debugACLrule.accessListNumber);
					out.println(" Keyword / Dyn# : " + debugACLrule.dynamic
							+ " / " + debugACLrule.dynamicName);
					out.println(" Keyword / TOmin : " + debugACLrule.timeout
							+ " / " + debugACLrule.timeoutMinutes);
					out.println(" Permit or Deny : " + debugACLrule.permitDeny);
					out.println(" Protocol Range : " + debugACLrule.protocolLower +
							" / " + debugACLrule.protocolUpper);
					out.println(" Src / Wildcard : " + debugACLrule.source
							+ " / " + debugACLrule.sourceWildcard);
					out.println(" Src Port Range : " + debugACLrule.sourcePortLower +
							" / " + debugACLrule.sourcePortUpper);
					out.println(" Dest / Wildcard : " + debugACLrule.destination
							+ " / " + debugACLrule.destinationWildcard );
					out.println(" Dest Port Range : "+ debugACLrule.destinationPortLower
							+ " / " + debugACLrule.destinationPortUpper);
					out.println(" Keyword / Prec# : " + debugACLrule.precedenceKeyword
							+ " / " + debugACLrule.precedence);
					out.println(" Keyword / TOS : " + debugACLrule.tosKeyword
							+ " / " + debugACLrule.tos);
					out.println(" Keyword / Log : " + debugACLrule.logKeyword
							+ " / " + debugACLrule.logInput);
					out.println(" ********************************");
					counter++;
				}
				//if (ACL_number.hasMoreElements())
				if (aclSet.hasNext())
					out.println("+++++++++++++++++++++++++++++++++++++++++\r\n");
				else out.println();
			}
			out.println("-----------------------------------------------" +"\r\n");
			// Objects within become a set?!!
			String interfaceName;
			Enumeration interfaceProperties = debugTableOfInterfaceByNames.keys();
			while(interfaceProperties.hasMoreElements()) {
				interfaceName = (String) interfaceProperties.nextElement();
				out.println("Interface name : " + interfaceName);
				debugInterfaces = (InterfaceConfig)
						debugTableOfInterfaceByNames.get(interfaceName);
				out.println("IP address : " + debugInterfaces.ipAddress);
				out.println("IP mask : " + debugInterfaces.ipMask);
				out.println("Neighbors : " + debugInterfaces.neighbors);
				out.println("In Filters : " + debugInterfaces.inFilters);
				out.println("Out Filters : " + debugInterfaces.outFilters);
				out.println();
			}
			out.println("-----------------------------------------------" +"\r\n");
			String Interface_IP;
			Enumeration interfaceProperties_2 = debugTableOfInterfaceByIPs.keys();
			while(interfaceProperties_2.hasMoreElements()) {
				Interface_IP = (String) interfaceProperties_2.nextElement();
				out.println("Interface IP : " + Interface_IP);
				debugInterfaces = (InterfaceConfig)
						debugTableOfInterfaceByIPs.get(Interface_IP);
				out.println("Interface name : " + debugInterfaces.interfaceName);
				out.println("IP mask : " + debugInterfaces.ipMask);
				out.println("Neighbors : " + debugInterfaces.neighbors);
				out.println("In Filters : " + debugInterfaces.inFilters);
				out.println("Out Filters : " + debugInterfaces.outFilters);
				out.println();
			}
			out.close();
		} catch (Exception e) { System.out.println ("Error - " + e); }
	}
}
