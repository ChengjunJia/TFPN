package StaticReachabilityAnalysis;
/*
 * InterfaceConfig.java
 *
 */
import java.io.Serializable;
import java.util.*;

public class InterfaceConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2583144319511876935L;
	String interfaceName; // Interface name
	String ipAddress; // IP address - yet to handle secondary case!
	String ipMask; // IP mask
	ArrayList<String> neighbors; // Array of neighbors --> make this array of objects
	// First check whether IP mask is the same length or same?
	// If same, check whether same network prefix (ip address + ip mask)
	// If not same --> not neighbors
	// If same network prefix --> neighbors
	ArrayList<String> inFilters; // Array of incoming filters, list of Strings - acl number
	PacketSet inPacketSet;
	ArrayList<String> outFilters; // Array of outgoing filters, list of Strings - acl number
	PacketSet outPacketSet;
	ArrayList<String> outAPExpr;
	/** Creates a new instance of InterfaceConfig */
	InterfaceConfig() {
		interfaceName = null;
		ipAddress = null;
		ipMask = null; // Process_Neighbors
		// [if (currentInterface.ipMask.equals(interfaceToCompare.ipMask))] does not work if set to null
		neighbors = new ArrayList<String>();	
		inFilters = new ArrayList<String>();
		outFilters = new ArrayList<String>();
		outAPExpr = new ArrayList<String>();
	}
	
	public String getName(){
		return interfaceName;
	}
	
	public ArrayList<String> getinFilters()
	{
		return inFilters;
	}
	
	public ArrayList<String> getoutFilters()
	{
		return outFilters;
	}
	/**
	 * 
	 * @return use ip address and ip mask to calculate the prefix
	 */
	public String getInterfacePrefix()
	{
		return ParseTools.GetPrefix(ipAddress, ipMask);
	}
	
	public int getInterfacePrefixLength()
	{
		return ParseTools.GetPrefixLength(ipMask);
	}
	
	public String toString() {
		return interfaceName + " " + ipAddress + " " + ipMask + " " + neighbors + " "
		+ inFilters + " " + outFilters;
	}
}