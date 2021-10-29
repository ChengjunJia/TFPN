package StaticReachabilityAnalysis;

/*
 * DebugTools.java
 *
 */
import java.util.*;
import java.io.*;
public class DebugTools {
	/** Creates a new instance of DebugTools */
	DebugTools() {
	}
	/******************************************************
	 *
	 * Writes the last ACL parsed into an output file
	 *
	 ******************************************************/
	static void CheckLastACLOutput(LinkedList a, PrintWriter out) {
		try {
			out.println("Parsed Output");
			out.println("access-list|" + " # |" + "dyn & #|"
					+ "timeout & #|" + "permorden|"
					+ " prot|" + " source & wc|"
					+ " dest & wc|" + " prcd & #|" + " tos & #|"
					+ " log & input ");
			Iterator itr = a.iterator();
			while (itr.hasNext()) {
				out.println (itr.next());
			}
		} catch (Exception e) { System.out.println ("Error - " + e); }
	}
	/******************************************************
	 *
	 * Writes the ACL rule parsed into an output file
	 *
	 ******************************************************/
	static void IntermediateParserCheck(ACLRule aclRule, FileWriter out){
		try {
			out.write (aclRule.accessList + " | ");
			out.write (aclRule.accessListNumber + " | ");
			out.write (aclRule.permitDeny + " | ");
			out.write (aclRule.protocolLower + " | ");
			out.write (aclRule.source + " | ");
			out.write (aclRule.sourceWildcard + " | ");
			out.write (aclRule.destination + " | ");
			out.write (aclRule.destinationWildcard + " | ");
			out.write ("\r\n" );
		} catch (Exception e) { System.out.println ("Error - " + e); }
	}
	
	public static void IntermediateACLRuleCheck(ACLRule aclRule, PrintStream out){
		try {
			out.print (aclRule.accessList + " | ");
			out.print (aclRule.accessListNumber + " | ");
			out.print (aclRule.permitDeny + " | ");
			out.print (aclRule.protocolLower + " | ");
			out.print (aclRule.source + " | ");
			out.print (aclRule.sourceWildcard + " | ");
			out.print (aclRule.destination + " | ");
			out.print (aclRule.destinationWildcard + " | ");
			out.println ( );
		} catch (Exception e) { System.out.println ("Error - " + e); }
	}
}