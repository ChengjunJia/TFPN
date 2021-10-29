package reachmatrix;

import java.util.HashSet;

import StaticReachabilityAnalysis.Sample;

public class APSet extends APSet1{

	public APSet(HashSet<Integer> hs) {
		super(hs);
		// TODO Auto-generated constructor stub
	}
	public APSet(int settype) {
		super(settype);
	}
	
	public APSet(APSet aps2)
	{
		super(aps2);
	}
	
	/**
	 * used for test
	 */
	public static HashSet<Integer> arrytoset(int[] arry)
	{
		HashSet<Integer> s1 = new HashSet<Integer>();
		for(int i = 0; i < arry.length; i ++)
		{
			s1.add(arry[i]);
		}
		return s1;
	}
	
	/**
	 * used for test
	 */
	public static HashSet<Integer> genset(int n, int Msize)
	{
		int [] arry = new int[n];
		Sample.GetSample(n, Msize, arry);
		return arrytoset(arry);
	}
	
	/**
	 * test it
	 */
	public static void main(String[] args)
	{
		HashSet<Integer> unv = new HashSet<Integer>();
		int Msize = 10;
		for(int i = 0; i < Msize; i ++)
		{
			unv.add(i);
		}
		APSet.setUniverse(unv);
		APSet.setcompThreshold(0.4);
		
		HashSet<Integer> anu = new HashSet<Integer>(unv);
		APSet aps0 = new APSet(anu);
		System.out.println(aps0);
		
		int[] sa1 = {0,1,2};
		HashSet<Integer> s1 = APSet.arrytoset(sa1);
		APSet aps1 = new APSet(s1);
		System.out.println(aps1);
		
		int[] sa2 = {0,1,3,6,9,8};
		HashSet<Integer> s2 = APSet.arrytoset(sa2);
		APSet aps2 = new APSet(s2);
		System.out.println(aps2);
		
		int[] sa3 = {2,6,7};
		HashSet<Integer> s3 = APSet.arrytoset(sa3);
		APSet aps3 = new APSet(s3);
		// NN intersect
		aps3.intersect(aps1);
		System.out.println(aps3);
		aps3 = new APSet(APSet.arrytoset(sa3));
		// NN union
		aps3.union(aps1);
		System.out.println(aps3);
		// CN union
		int[] sa4 = {2,5};
		aps3.union(new APSet(APSet.arrytoset(sa4)));
		System.out.println(aps3);
		// CN intersect
		int[] sa5 = {7,9};
		aps3.intersect(new APSet(APSet.arrytoset(sa5)));
		System.out.println(aps3);
		// NC union
		aps3.union(new APSet(APSet.arrytoset(sa2)));
		System.out.println(aps3);
		// NC intersect
		aps1.intersect(aps3);
		System.out.println(aps1);
		// CC union
		aps2 = new APSet(APSet.arrytoset(sa2));
		int [] sa6 = {2,5,6,7,8};
		APSet aps4 = new APSet(APSet.arrytoset(sa6));
		aps4.union(aps2);
		System.out.println(aps4);
		aps4 = new APSet(APSet.arrytoset(sa6));
		aps4.intersect(aps2);
		System.out.println(aps4);
	}

}
