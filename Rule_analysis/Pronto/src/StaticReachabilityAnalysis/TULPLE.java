package StaticReachabilityAnalysis;

import java.io.Serializable;

/*
 * Tuple.java
 *
 */
//import java.util.*;
class Tuple implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6478598777851377838L;
	/*
	 * the memory size of a range (in byte)
	 * long is 4 bytes
	 */
	public final static int size = Range.size*5;
	
	Range sourceIP;
	Range sourcePort;
	Range destinationIP;
	Range destinationPort;
	Range protocol;
	/** Creates a new instance of Tuple */
	Tuple() {
		sourceIP = new Range();
		sourcePort = new Range();
		destinationIP = new Range();
		destinationPort = new Range();
		protocol = new Range();
	}
	/*creates a new instance of Tuple given 5 ranges*/
	Tuple(Range r1, Range r2, Range r3, Range r4, Range r5){
		sourceIP = r1;
		sourcePort = r2;
		destinationIP = r3;
		destinationPort = r4;
		protocol = r5;
	}
	
	public String toString () {
		PacketSet pS = new PacketSet();
		return "[ " + pS.convertIntegertoIP(sourceIP.lower) + " , " +
		pS.convertIntegertoIP(sourceIP.upper) + " ] ; " +
		"[ " + sourcePort.lower + " , " + sourcePort.upper + " ] ; " +
		"[ " + pS.convertIntegertoIP(destinationIP.lower) + " , " +
		pS.convertIntegertoIP(destinationIP.upper) + " ] ; " + "[ " +
		destinationPort.lower + " , " + destinationPort.upper + " ] ; " +
		"[ " + protocol.lower + " , " + protocol.upper + " ] ";
	}
}
