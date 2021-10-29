package StaticReachabilityAnalysis;
/*
 * AComp.java
 *
 */
import java.util.*;
class AComp implements Comparator {
	// This Comparator overrides the default compare function
	// Used for the ArrayList sorting in OptimizePacketSet
	// Enables correct sorting of the tuples

	public int compare (Object a, Object b) {
		int result=0;
		Tuple aTuple, bTuple;
		aTuple = (Tuple) a;
		bTuple = (Tuple) b;
		// if a is smaller than b, then a will come first
		// 1st Check : sourceIP.lower
		if (aTuple.sourceIP.lower == bTuple.sourceIP.lower ) {
			// If tied, do 2nd Check : souceIP.upper
			if (aTuple.sourceIP.upper == bTuple.sourceIP.upper) {
				// If tied again, do 3rd Check : sourcePort.lower
				if (aTuple.sourcePort.lower ==
						bTuple.sourcePort.lower) {
					// If tied again, do 4th Check :sourcePort.upper
					if (aTuple.sourcePort.upper ==
							bTuple.sourcePort.upper) {
						// If tied again, do 5th Check : distinationIP.lower
						if (aTuple.destinationIP.lower ==
								bTuple.destinationIP.lower){
							// If tied again, do 6th Check : distinationIP.upper
							if (aTuple.destinationIP.upper ==
									bTuple.destinationIP.upper){
								// If tied again, do 7th Check : distinationPort.lower
								if (aTuple.destinationPort.lower ==
										bTuple.destinationPort.lower){
									// If tied again, do 8th Check : distinationPort.upper
									if (aTuple.destinationPort.upper ==
											bTuple.destinationPort.upper){
										// If tied again, do 9th Check : protocol.upper
										if (aTuple.protocol.lower ==
												bTuple.protocol.lower) {
											// If tied again, do 10th Check : protocol.upper
											if (aTuple.protocol.upper ==
													bTuple.protocol.upper) result = 0;
											else if (aTuple.protocol.upper <
													bTuple.protocol.upper) result = -1;
											else result = 1;
										}
										else if (aTuple.protocol.lower <
												bTuple.protocol.lower) result = -1;
										else result = 1;
									}
									else if (aTuple.destinationPort.upper <
											bTuple.destinationPort.upper) result = -1;
									else result = 1;
								}
								else if (aTuple.destinationPort.lower <
										bTuple.destinationPort.lower) result = -1;
								else result = 1 ;
							}
							else if (aTuple.destinationIP.upper <
									bTuple.destinationIP.upper) result = -1;
							else result = 1 ;
						}
						else if (aTuple.destinationIP.lower <
								bTuple.destinationIP.lower) result = -1;
						else result = 1;
					} // end of 3rd Check
					else if (aTuple.sourcePort.upper <
							bTuple.sourcePort.upper) result = -1;
					else result = 1 ;
				} // end of 2nd Check
				else if (aTuple.sourcePort.lower <
						bTuple.sourcePort.lower) result = -1;
				else result = 1 ;
			}
			else if (aTuple.sourceIP.upper <
					bTuple.sourceIP.upper) result = -1 ;
			else result = 1;
		}
		else if (aTuple.sourceIP.lower <
				bTuple.sourceIP.lower) result = -1 ;
		else result = 1;
		return result;
	}
	/** Creates a new instance of AComp */
	AComp() {
	}
}