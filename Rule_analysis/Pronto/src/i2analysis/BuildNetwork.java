package i2analysis;

import java.io.*;
import java.util.*;

import stanalysis.ACLUse;
import stanalysis.ForwardingRule;
import stanalysis2.DeviceAPT;
import StaticReachabilityAnalysis.*;
import StaticReachabilityAnalysis.Parser.ACLType;

/**
 * only FIB is considered.
 * @author yanghk
 *
 */

public class BuildNetwork {
	
	public static void main (String[] args) throws IOException
	{
		BDDACLWrapper baw = new BDDACLWrapper();
		Device.setBDDWrapper(baw);
	//	Device d = parseDevice("atla", "i2/atlaap");
		//System.out.println(d.subnets.size());
	//	d.computeFWBDDs();yuzhao 5.16.2016
	}

	public static Device parseDevice(String dname, String filename, String filenameparsed, HashMap<String,HashSet<String>> univervlan) throws IOException
	{
		Device d = new Device(dname);
		
		File inputFile = new File(filename);
		extractACLs(d, inputFile);
		inputFile = new File(filenameparsed);
		readParsed(d, inputFile, univervlan);
		return d;
		
	}
	
	public static ForwardingRule assembleFW(String[] entries)
	{
		//use short name for physical port
		String portname = entries[3];
		
		if(entries[3].startsWith("vlan"))
		{
			portname = entries[3];
		//	System.out.println(portname);
		}else
		{
			//System.out.println(entries[3]);
			portname = entries[3].split("\\.")[0];
		}
		return new ForwardingRule(Long.parseLong(entries[1]), Integer.parseInt(entries[2]), portname);//long destip, int prefixlen, String outinterface
	}
	
	public static void extractACLs(Device d, File inputFile) throws IOException
	{

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

		/* Read line by line */
		while (OneLine.hasNext()) {
			/* Read token by token in each line */
			Scanner TokenInLine = new Scanner(OneLine.next());
			String keyword;
			if (TokenInLine.hasNext()) {
				keyword = TokenInLine.next();
				/**************************************************************
				 * This section handles ACL rules that start with "access-list"
				 ***************************************************************/
				if (keyword.equals("access-list")) {
					HandleAccessList(OneLine, TokenInLine, d);
				}
				/* handles acl rules that start with ip access-list extend/standard*/
				else if(keyword.equals("ip"))
				{
					keyword = TokenInLine.next();
					if(keyword.equals("access-list"))
					{
						HandleAccessListGrouped(OneLine, TokenInLine, d);
					}
				}
			}
		} // end of while (oneline.hasNext())
	}
	
	
	static void HandleAccessListGrouped(Scanner oneline, Scanner tokeninline, Device d)throws IOException
	{
		ArrayList<String> argument = new ArrayList<String> ();
		Parser.GetArgument(tokeninline, argument);
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
				Parser.GetArgument(tokeninline, argument);
				if(argument.get(0).equals("ip"))
				{//a new ACL, need to add the old one 
					d.addACL(thisNumber, oneacl);
					argument.remove(0);// remove ip
					argument.remove(0);// remove access-list, so that it can restart
					//Parser.DebugInput(System.out, null, "Add:"+thisNumber);
					break;
				}
				if((!argument.get(0).equals("permit")) && (!argument.get(0).equals("deny")))
				{//this means the end of the ACL definition
					d.addACL(thisNumber, oneacl);
					//Parser.DebugInput(System.out, null, "Add:"+thisNumber);
					return;
				}
				ACLRule onerule = new ACLRule();
				onerule.accessList = "access-list";
				onerule.accessListNumber = thisNumber;
				Parser.CheckPermitDeny(onerule, argument);
				if(thisType == ACLType.extend)
				{
					Parser.HandleACLRuleExtend(onerule, argument);
				}else{
					Parser.HandleACLRuleStandard(onerule, argument);
				}
				oneacl.add(onerule);
			}
		}

	}

	
	public static void readParsed(Device d, File inputFile, HashMap<String,HashSet<String>> univervlan) throws IOException
	{
		Scanner OneLine = null;
		try {
			OneLine = new Scanner (inputFile);
			OneLine.useDelimiter("\n");
			//System.out.println ("lunar "+OneLine);
			//scanner.useDelimiter(System.getProperty("line.separator"));
			// doesn't work for .conf files
		} catch (FileNotFoundException e) {
			System.out.println ("File not found!"); // for debugging
			System.exit (0); // Stop program if no file found
		}
		
		while(OneLine.hasNext())
		{
			String linestr = OneLine.next();
			String[] tokens = linestr.split(" ");
			if(tokens[0].equals( "fw")) 
			{   
				if(!tokens[3].startsWith("vlan")&&!tokens[3].startsWith("self")){
				ForwardingRule onerule = assembleFW(tokens);
				
				d.addFW(onerule);//把device里面的每一行都加到ArrayList<ForwardingRule> fws里面; ForwardingRule(long destip, int prefixlen, String outinterface)
				//System.out.println("add: " + onerule);
				}else if(tokens[3].startsWith("vlan")){
					ForwardingRule onerule = assembleFW(tokens);
					d.vlanfws.add(onerule);
					if(univervlan.containsKey(tokens[3])){
						univervlan.get(tokens[3]).add(d.name);
					}else{
						HashSet<String> nlist=new HashSet<String>();
						nlist.add(d.name);
						univervlan.put(tokens[3], nlist);
					}

				}
				
				
			}else if(tokens[0].equals("vlanport"))
			{
				String vlanname = tokens[1];
				HashSet<String> ports = new HashSet<String>();
				for(int i = 2; i < tokens.length; i ++)
				{
					ports.add(tokens[i]);
				}
				d.addVlanPorts(vlanname, ports);
				//System.out.println("add: " + vlanname + " " + ports);
			}
			else if(tokens[0].equals("acl"))//tokens:acl 120 vlan316 in vlan320 in vlan322 in....
			{
				for (int i = 0; i < (tokens.length - 2)/2; i ++)
				{
					d.addACLUse(new ACLUse(tokens[1], tokens[(i+1)*2], tokens[(i+1)*2+1]));//acluses.add(oneuse);//ArrayList<ACLUse> acluses;
					//System.out.println(tokens[1] + " " + tokens[(i+1)*2] + " " + tokens[(i+1)*2+1]);
				}
			}	
		}
	}
	
	static void HandleAccessList(Scanner oneline, Scanner tokeninline, Device d) throws IOException
	{
		ArrayList<String> argument = new ArrayList<String>();
		/**set up argument, 'access-list' has already been parsed*/
		Parser.GetArgument(tokeninline, argument);

		// Test Function 1 : output argument array into a test file
		//DebugInput(System.out, argument, "access-list");

		int currentACLNum = -1;
		int preACLNum = -1;
		int[] aclNumbers = {preACLNum, currentACLNum};// pay attention to the order

		ACLRule onerule = null;
		LinkedList<ACLRule> oneacl = new LinkedList<ACLRule>();

		if(Parser.CheckValidACL(argument, aclNumbers))
		{
			onerule = new ACLRule();
			onerule.accessList = "access-list";

			preACLNum = aclNumbers[0];
			currentACLNum = aclNumbers[1];
			onerule.accessListNumber = Integer.toString(currentACLNum);
			//
			Parser.AddDynamic(onerule, argument);
			Parser.CheckPermitDeny(onerule, argument);

			if(Parser.CheckACLType(currentACLNum) == ACLType.standard)
			{
				Parser.HandleACLRuleStandard(onerule, argument);
			}else
			{
				Parser.HandleACLRuleExtend(onerule, argument);
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
				Parser.GetArgument(tokeninline, argument);

				if(Parser.CheckValidACL(argument, aclNumbers))
				{
					preACLNum = aclNumbers[0];
					currentACLNum = aclNumbers[1];
					if(preACLNum != currentACLNum)
					{
						// finish parsing an acl, add to the router
						d.addACL(Integer.toString(preACLNum), oneacl);

						//debug
						ArrayList<String> oneaclInfo = new ArrayList<String>();
						oneaclInfo.add(oneacl.get(0).accessListNumber);
						oneaclInfo.add(Integer.toString(oneacl.size()));
						//Parser.DebugInput(System.out, oneaclInfo, "added access-list");

						// get a new one to store
						oneacl = new LinkedList<ACLRule>();
						preACLNum = currentACLNum;
					}

					onerule = new ACLRule();
					onerule.accessList = "access-list";

					onerule.accessListNumber = Integer.toString(currentACLNum);
					//
					Parser.AddDynamic(onerule, argument);
					Parser.CheckPermitDeny(onerule, argument);

					if(Parser.CheckACLType(currentACLNum) == ACLType.standard)
					{
						Parser.HandleACLRuleStandard(onerule, argument);
					}else
					{
						Parser.HandleACLRuleExtend(onerule, argument);
					}
					//debug
					//DebugTools.IntermediateACLRuleCheck(onerule, System.out);
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
			//Parser.DebugInput(System.out, oneaclInfo, "added access-list");
			d.addACL(Integer.toString(currentACLNum), oneacl);
		}

	}
	
	
}


