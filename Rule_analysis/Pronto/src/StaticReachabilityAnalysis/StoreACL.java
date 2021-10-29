package StaticReachabilityAnalysis;

import java.io.*;
import java.util.*;

public class StoreACL implements Serializable {

	/**
	 * generated version UID
	 */
	private static final long serialVersionUID = -3157208412973965542L;
	String NetworkName;
	//store all the acls in the network, use indexes to access
	// only consider acls that are used in some interfaces
	LinkedList<LinkedList<ACLRule>> ACLList;
	//router names
	LinkedList<String> RouterList;

	// map(routername, (aclname, aclind))
	HashMap<String, HashMap<String, Integer>> ACLMap;

	/**
	 * filename - the file that stores the network
	 */
	
	public LinkedList<LinkedList<ACLRule>> get_all_acls()
	{
		return ACLList;
	}
	
	public StoreACL(String filename)
	{
		ACLList = new LinkedList<LinkedList<ACLRule>>();
		RouterList = new LinkedList<String> ();
		ACLMap = new HashMap<String, HashMap<String, Integer>> ();

		LoadData(filename);
	}
	
	/** 
	 * aclid, aclsize, router
	 * @throws IOException 
	 */
	public void outputinfo() throws IOException
	{
		FileOutputStream out = new FileOutputStream(NetworkName + "-acl.out");
		PrintStream ps = new PrintStream(out);
		for(int i = 0; i < RouterList.size(); i ++)
		{
			String rname = RouterList.get(i);
			HashMap<String, Integer> amap = ACLMap.get(rname);
			if(amap != null)
			{
				int aclrulesum = 0;
				for(Integer aclid : amap.values())
				{
					aclrulesum = aclrulesum + ACLList.get(aclid).size();
				}
				for(Integer aclid : amap.values())
				{
					ps.println(aclid + "\t" + ACLList.get(aclid).size() + "\t" + rname + "\t" + aclrulesum);
				}
			}
		}
		ps.close();
		out.close();
	}
	
	/**
	 * 
	 * @return
	 */
	public void getrandomorderbyrouter()
	{
		int[] routerorder = new int[RouterList.size()];
		Sample.GetSample(RouterList.size(), RouterList.size(), routerorder);
		int[] aclorder = new int[ACLList.size()];
		int curaclid = 0;
		for(int i = 0; i < RouterList.size(); i ++)
		{
			String rname = RouterList.get(i);
			HashMap<String, Integer> amap = ACLMap.get(rname);
			if(amap != null)
			{
				for(Integer aclid : amap.values())
				{
					aclorder[curaclid] = aclid;
					curaclid ++;
				}
			}
		}
		for(int i = 0; i < aclorder.length; i ++)
		{
			System.out.println(aclorder[i]);
		}
	}

	/**
	 * 
	 * @param router
	 * @param acl
	 * @return - # of rules in the acl
	 */
	public int getACLSize(String router, String acl)
	{
		int aclid = getACLId(router, acl);
		return ACLList.get(aclid).size();
	}

	/**
	 * 
	 * @param router
	 * @param acl
	 * @return - the id of the acl
	 */
	public int getACLId(String router, String acl)
	{
		HashMap<String, Integer> localmap = ACLMap.get(router);
		int aclid = localmap.get(acl);
		return aclid;
	}

	private void LoadData(String filename)
	{
		NetworkConfig network = ParseTools.LoadNetwork(filename);
		NetworkName = network.getName();

		Enumeration<String> routernames = network.tableOfRouters.keys();
		while(routernames.hasMoreElements())
		{
			RouterList.add(routernames.nextElement());
		}

		// store acls that are not defined in the same router.
		LinkedList<String> MissingACLName = new LinkedList<String> ();
		LinkedList<Integer> MissingACLInd = new LinkedList<Integer> ();

		for(int i = 0; i < RouterList.size(); i ++)
		{
			String currentRouterName = RouterList.get(i);
			RouterConfig currentRouter = network.tableOfRouters.get(currentRouterName);

			// the interfaces that are assigned IP addresses, and hence are installed with ACLs
			Enumeration<InterfaceConfig> interfaceEnum = currentRouter.tableOfInterfaceByIPs.elements();
			//-----------------------------------------------------------------------------------------------
			//Enumeration<InterfaceConfig> interfaceEnum = currentRouter.tableOfInterfaceByNames.elements();
			// map(aclname, aclind) for the current router
			HashMap<String, Integer> localaclmap = new HashMap<String, Integer> ();

			while(interfaceEnum.hasMoreElements())
			{
				InterfaceConfig currentInterface = interfaceEnum.nextElement();

				//insert filters that are installed on the router
				for(int j = 0; j < currentInterface.inFilters.size(); j ++)
				{
					String aclName = currentInterface.inFilters.get(j);
					LinkedList<ACLRule> acl = currentRouter.tableOfACLs.get(aclName);
					ACLList.add(acl);
					localaclmap.put(aclName + ACLList.size(), ACLList.size() - 1);
					// the acl is not defined in the same router
					if(acl == null)
					{
						MissingACLName.add(aclName);
						MissingACLInd.add(ACLList.size() - 1);
					}
				}

				for(int j = 0; j < currentInterface.outFilters.size(); j ++)
				{
					String aclName = currentInterface.outFilters.get(j) ;
					LinkedList<ACLRule> acl = currentRouter.tableOfACLs.get(aclName);
					ACLList.add(acl);
					localaclmap.put(aclName + ACLList.size(), ACLList.size() - 1);
					// find a missing acl
					if(acl == null)
					{
						MissingACLName.add(aclName);
						MissingACLInd.add(ACLList.size() - 1);
					}
				}

			}// each router, check all its interfaces with IP addresses.

			ACLMap.put(currentRouterName, localaclmap);
			System.out.println(localaclmap.size() + " acls in " + currentRouterName);
		}

		// try to find missing acls
		System.out.println(MissingACLInd + " missing");
		for(int i = 0; i < RouterList.size(); i ++)
		{
			String currentRouterName = RouterList.get(i);
			RouterConfig currentRouter = network.tableOfRouters.get(currentRouterName);

			int found = 0;
			if(found == MissingACLInd.size())
			{
				// when all missing acls are found, no need to do the loop
				break;
			}
			for(int j = 0; j < MissingACLInd.size(); j ++)
			{
				// -1 means that already find the missing acl
				if(MissingACLInd.get(j) >=0)
					if(currentRouter.tableOfACLs.containsKey(MissingACLName.get(j)))
					{
						// get a hit!
						LinkedList<ACLRule> acl = 
								currentRouter.tableOfACLs.get(MissingACLName.get(j));
						ACLList.set(MissingACLInd.get(j), acl);
						MissingACLInd.set(j, -1);
						found ++;
						System.out.println(MissingACLName.get(j));
					}
			}
		}


		System.out.println("# of routers loaded: " + RouterList.size());
		System.out.println("# of ACLs: " + ACLList.size());

	}

	public void Stat()
	{
		System.out.println("# of routers loaded: " + RouterList.size());
		System.out.println("# of ACLs: " + ACLList.size());

		int sum = 0;
		for(int i = 0; i < ACLList.size(); i ++)
		{
			sum = sum + ACLList.get(i).size();
			//System.out.println(i);
		}
		System.out.println("# of ACL Rules: " + sum);
	}

	public boolean SaveACLs()
	{

		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			fos = new FileOutputStream(NetworkName + "-ACLs.ser");
			out = new ObjectOutputStream(fos);
			out.writeObject(this);
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			return false;
		}

		System.out.println("ACLs from Network " + NetworkName + " is saved.");
		return true;
	}

	public static StoreACL LoadNetwork(String filename)
	{

		FileInputStream fis = null;
		ObjectInputStream in = null;
		StoreACL sa = null;
		try
		{
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			sa = (StoreACL)in.readObject();
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
		System.out.println("ACLs from network " + sa.NetworkName + " is loaded.");
		return sa;
	}

	public void PrepareOutputFDD()
	{
		FileOutputStream out;
		PrintStream ps;
		try {
			out = new FileOutputStream(NetworkName + "-acls");
			ps = new PrintStream(out);

			for(int i = 0; i < ACLList.size(); i ++)
			{
				LinkedList<ACLRule> acl = ACLList.get(i);
				//System.out.println(acl.size());
				for(int j = 0; j < acl.size(); j ++)
				{
					ACLRule aclr = acl.get(j);
					String aline = "";
					// src ip
					String[] ips = convertIPtoRange
							(aclr.source, aclr.sourceWildcard);
					aline = aline + ips[0] + " " + ips[1] + " ";


					// dst ip
					ips = convertIPtoRange
							(aclr.destination, aclr.destinationWildcard);
					aline = aline + ips[0] + " " + ips[1] + " ";

					// src port
					Range r = PacketSet.convertPortToRange(
							aclr.sourcePortLower, aclr.sourcePortUpper);
					aline = aline + r.lower + " " + r.upper + " ";

					// dst port
					r = PacketSet.convertPortToRange(
							aclr.destinationPortLower, aclr.destinationPortUpper);
					aline = aline + r.lower + " " + r.upper + " ";

					// protocol 
					r = PacketSet.convertProtocolToRange(
							aclr.protocolLower, aclr.protocolUpper);
					aline = aline + r.lower + " " + r.upper + " ";

					if(PacketSet.CheckPermit(aclr))
					{
						aline = aline + 1;
					}else
					{
						aline = aline + 0;
					}
					ps.println(aline);

				}
				ps.println(denyAll());
				ps.println("#");
			}
			// we append one special rule: allow all, to deal with links that have no acls
			ps.println(allowAll());
			ps.println("#");
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
		System.out.println(ACLList.size() + " ACLs written." + " FDD input data is prepared.");
		System.out.println("at the end, a special rule (allowall) are appended.");
	}

	/**
	 * adapted from PacketSet.convertIPtoIntegerRange()
	 * @param inputIP
	 * @param inputMask
	 * @return a string of lower ip, a another string of upper ip
	 */
	private static String[] convertIPtoRange 
	(String inputIP, String inputMask) 
	{
		String[] output = new String[2];
		int ipOctet1, ipOctet2, ipOctet3, ipOctet4;
		int maskOctet1, maskOctet2, maskOctet3, maskOctet4;
		String upperOctetString1, upperOctetString2,
		upperOctetString3, upperOctetString4 ;

		/*******************************************************
		 * If there are no IP addresses, the range is set as the max
		 *******************************************************/
		if (inputIP==null || inputIP.equalsIgnoreCase("any")) {
			output[0] = "0.0.0.0" ;
			output[1] = "255.255.255.255" ;
		}
		else {
			output[0] = inputIP;
			/*******************************************************
			 * Process wildcard mask next
			 * If there is no mask, single point in range
			 *******************************************************/
			if (inputMask==null) {
				output[1] = output[0];
			}
			else {

				Scanner s = new Scanner(inputIP).useDelimiter("\\.");
				//System.out.println(inputIP);
				ipOctet1 = Integer.parseInt(s.next());
				ipOctet2 = Integer.parseInt(s.next());
				ipOctet3 = Integer.parseInt(s.next());
				ipOctet4 = Integer.parseInt(s.next());
				/*******************************************************
				 * Break up wildcard mask into octets
				 *******************************************************/
				s = new Scanner(inputMask).useDelimiter("\\.");
				maskOctet1 = Integer.parseInt(s.next());
				maskOctet2 = Integer.parseInt(s.next());
				maskOctet3 = Integer.parseInt(s.next());
				maskOctet4 = Integer.parseInt(s.next());
				/*** Calculate upper value of IP range in string ***/
				upperOctetString1 = Integer.toString(ipOctet1 | maskOctet1) ;
				upperOctetString2 = Integer.toString(ipOctet2 | maskOctet2) ;
				upperOctetString3 = Integer.toString(ipOctet3 | maskOctet3) ;
				upperOctetString4 = Integer.toString(ipOctet4 | maskOctet4) ;
				output[1] = upperOctetString1 + "." + upperOctetString2 + 
						"." + upperOctetString3 + "." + upperOctetString4;
			} // end of else statement on inputMask
		} // end of else statement on inputIP
		return output;
	}

	private static String denyAll()
	{
		return "0.0.0.0 255.255.255.255 0.0.0.0 255.255.255.255 " +
				"0 65535 0 65535 0 255 0";
	}
	
	/**
	 * this is used to deal with links with no acls
	 * @return
	 */
	private static String allowAll()
	{
		return "0.0.0.0 255.255.255.255 0.0.0.0 255.255.255.255 " +
				"0 65535 0 65535 0 255 1";
	}
	
	/**
	 * 
	 * @return - num of acls
	 */
	public int getACLNum()
	{
		return ACLList.size();
	}


	public static void main(String[] args) throws IOException
	{

		
		StoreACL sa = new StoreACL("purdue.ser");
		sa.Stat();
		sa.getrandomorderbyrouter();
		//sa.SaveACLs();
		

		/*
		StoreACL sa = StoreACL.LoadNetwork("purdue-ACLs.ser");
		sa.Stat();
		*/

		/*
		String [] res = convertIPtoRange("192.168.2.1", "0.0.255.255");
		System.out.println(res[0] + " " + res[1]);
		 */

		/*
		StoreACL sa = StoreACL.LoadNetwork("purdue-ACLs.ser");
		System.out.println(sa.NetworkName);
		sa.PrepareOutputFDD();
		*/

	}

}
