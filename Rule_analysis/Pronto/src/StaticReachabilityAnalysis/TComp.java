package StaticReachabilityAnalysis;
/*
 * TComp.java
 *
 */
import java.io.Serializable;
import java.util.*;
class TComp implements Comparator, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 836849734215230940L;
	// This Comparator overrides the default compare function
	// Used for the TreeMap in RouterConfig
	// Enables correct sorting of the ACL number string
	//do not make factor^compareLen too much...
	static int factor = 5;
	static int compareLen = 3;
	int factorPower(int expo)
	{
		int m = 1;
		for (int i = 0; i < expo; i ++)
		{
			m = m*factor;
		}
		return m;
	}
	
	int effectLen(int l1, int l2)
	{
		int el;
		if(l1 > l2)
		{
			el = l2;
		}else{
			el = l1;
		}
		if(el > compareLen)
		{
			return compareLen;
		}else{
			return el;
		}
	}
	public int compare (Object a, Object b) {
		//int aValue, bValue;
		//aValue = Integer.valueOf((String) a) ;
		//bValue = Integer.valueOf((String) b);
		// if a is smaller than b, then a will come first
		//return (aValue-bValue);
		String sa = (String) a;
		String sb = (String) b;
		int diff = 0;
		int el = effectLen(sa.length(), sb.length());
		for(int i = 0; i < el; i ++)
		{
			diff = diff + (sa.charAt(i) - sb.charAt(i))*factorPower(el - i);
		}
		return diff;
		//only compare the first three digits, hope that will work
		
	}
	/** Creates a new instance of TComp */
	TComp() {
	}
}
