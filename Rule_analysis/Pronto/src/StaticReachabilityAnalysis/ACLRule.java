package StaticReachabilityAnalysis;

import java.io.Serializable;

/*
 * ACLrule.java
 *
 */
//import java.util.*;
public class ACLRule implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4261146103254314479L;

	public String accessList;
	public String accessListNumber;
	String dynamic;
	String dynamicName;
	String timeout;
	String timeoutMinutes;
	String permitDeny;
	String protocolLower;
	String protocolUpper;
	String source;
	String sourceWildcard;
	String sourcePortLower;
	String sourcePortUpper;
	String destination;
	String destinationWildcard;
	String destinationPortLower;
	String destinationPortUpper;
	String precedenceKeyword;
	String precedence;
	String tosKeyword;
	String tos;
	String logKeyword;
	String logInput;
	Boolean remark;

	public ACLRule(){
		accessList = null;
		accessListNumber = null;
		dynamic = null;
		dynamicName = null;
		timeout = null;
		timeoutMinutes = null;
		permitDeny = null;
		protocolLower = null ;
		protocolUpper = null ;
		source = null;
		sourceWildcard = null;
		sourcePortLower = null;
		sourcePortUpper = null;
		destination = null;
		destinationWildcard = null;
		destinationPortLower = null ;
		destinationPortUpper = null ;
		precedenceKeyword = null;
		precedence = null;
		tosKeyword = null;
		tos = null;
		logKeyword = null;
		logInput = null;
		remark = false;
	}
	public String toString() {
		return accessList + " "
				+ accessListNumber + " "
				//+ dynamic + " "
				//+ dynamicName + " "
				//+ timeout + " "
				//+ timeoutMinutes + " "
				+ permitDeny + " "
				+ protocolLower + " "
				+ protocolUpper + " "
				+ source + " "
				+ sourceWildcard + " "
				+ sourcePortLower + " "
				+ sourcePortUpper + " "
				+ destination + " "
				+ destinationWildcard + " "
				+ destinationPortLower + " "
				+ destinationPortUpper + " "
				//+ precedenceKeyword + " "
				//+ precedence + " "
				//+ tosKeyword + " "
				//+ tos + " "
				//+ logKeyword + " "
				//+ logInput + " "
				//+ remark
				;
	}
} // end class ACLrule