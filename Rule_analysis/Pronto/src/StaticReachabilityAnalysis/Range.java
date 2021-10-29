package StaticReachabilityAnalysis;

import java.io.Serializable;

/*
 * Range.java
 *
 */
//import java.util.*;
public class Range implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8711856102321897279L;
	/*
	 * the memory size of a range (in byte)
	 * long is 4 bytes
	 */
	public final static int size = 8;
	
	/*String lower;
String upper;*/
	long lower;
	long upper;
	/** Creates a new instance of Range */
	public Range() {
		/*lower = "";
upper = "";*/
		lower = -1L;
		upper = -1L;
	}
	
	/**
	 * initialize a range with a,b
	 * */
	public Range(long l, long u)
	{
		lower = l;
		upper = u;
		/*
		if(l < u)
		{
			lower = l;
			upper = u;
		}else{
			lower = l;
			upper = u;
		}
		*/
		
	}
	
	/*get a copy of inputRange*/
	public Range (Range inputRange)
	{
		lower = inputRange.lower; 
		upper = inputRange.upper;
	}
	
	public long Length()
	{
		return upper - lower + 1;
	}

	
	public String toString () {
		return "[ " + lower + " , " + upper + " ] " ;
	}
}
