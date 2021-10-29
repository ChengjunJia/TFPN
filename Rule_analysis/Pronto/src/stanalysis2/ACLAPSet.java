package stanalysis2;

import java.util.HashSet;
import StaticReachabilityAnalysis.BDDACLWrapper;

/**
 * APSet1 uses complement optimization
 * @author hongkun
 *
 */

public class ACLAPSet {
	static int totalap;
	static HashSet<Integer> universeset;
	static double compoptThreshold = 0.5;
	boolean iscomplement;
	HashSet<Integer> apset;
	
	static void setcompThreshold(double thr)
	{
		compoptThreshold = thr;
	}
	
	public boolean isempty()
	{
		if(!iscomplement && apset.isEmpty())
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	public boolean isfull()
	{
		if(iscomplement && apset.isEmpty())
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	// directly set
	public static void setUniverse(HashSet<Integer> unv)
	{
		universeset = unv;
		totalap = universeset.size();
	}


	private void tocomplement()
	{
		HashSet<Integer> news = new HashSet<Integer>(universeset);
		news.removeAll(apset);
		apset = news;
		iscomplement = !iscomplement;
	}
	
	public void union(ACLAPSet apset2){
		if(!iscomplement && !apset2.iscomplement)
		{
			unionNN(apset2.apset);
			return;
		}
		if(!iscomplement && apset2.iscomplement)
		{
			unionNC(apset2.apset);
			return;
		}
		if(iscomplement && !apset2.iscomplement)
		{
			unionCN(apset2.apset);
			return;
		}
		if(iscomplement && apset2.iscomplement)
		{
			unionCC(apset2.apset);
			return;
		}
	}
	
	public void intersect(ACLAPSet apset2){
		if(!iscomplement && !apset2.iscomplement)
		{
			intersectNN(apset2.apset);
			return;
		}
		if(!iscomplement && apset2.iscomplement)
		{
			intersectNC(apset2.apset);
			return;
		}
		if(iscomplement && !apset2.iscomplement)
		{
			intersectCN(apset2.apset);
			return;
		}
		if(iscomplement && apset2.iscomplement)
		{
			intersectCC(apset2.apset);
			return;
		}
	}
	
	/**
	 * copy an ap set
	 */
	public ACLAPSet(ACLAPSet aps2)
	{
		iscomplement = aps2.iscomplement;
		apset = new HashSet<Integer>(aps2.apset);
	}

	public ACLAPSet(HashSet<Integer> hs)
	{
		if(hs.size() == totalap)
		{
			SetAPSet(BDDACLWrapper.BDDTrue);
		}else if(hs.size() == 0)
		{
			SetAPSet(BDDACLWrapper.BDDFalse);
		}else{
			SetAPSet(hs);
		}
	}

	public ACLAPSet(int settype)
	{
		SetAPSet(settype);
	}

	// make sure hs is not allowall or denyall
	// directly set it
	private void SetAPSet(HashSet<Integer> hs)
	{
		apset = hs;
		iscomplement = false;
		complementopt();
	}


	private void complementopt()
	{
		if(apset.size() > compoptThreshold * totalap )
		{
			tocomplement();
		}
	}

	/**
	 * 1 - bddtrue
	 * 0 - bddfalse
	 * @param settype
	 */
	private void SetAPSet(int settype)
	{
		apset = new HashSet<Integer>();
		if(settype == BDDACLWrapper.BDDTrue)
		{
			iscomplement = true;
		}else if(settype == BDDACLWrapper.BDDFalse)
		{
			iscomplement = false;
		}else{
			System.err.println("unsupported ap kind!");
		}
	}
	
	/** 
	 * not complement
	 */
	private void unionNN(HashSet<Integer> hs)
	{
		apset.addAll(hs);
		complementopt();
	}
	
	private void intersectNN(HashSet<Integer> hs)
	{
		apset.retainAll(hs);
		// no need to do complementopt, since it is shrinking
	}
	
	/**
	 * comp(union(s1, comp(s2))) = comp(s2) - s1
	 * @param hs
	 */
	private void unionNC(HashSet<Integer> comphs)
	{
		iscomplement = true;
		HashSet<Integer> news = new HashSet<Integer>(comphs);
		news.removeAll(apset);
		apset = news;
		// no need to do complementopt, it is shrinking
	}
	
	/**
	 * intersect(s1, s2) = s1 - comp(s2)
	 * @param hs
	 */
	private void intersectNC(HashSet<Integer> comphs)
	{
		apset.removeAll(comphs);
		// no need to do complemntopt, since it is shrinking
	}
	
	/**
	 * comp(union(s1, s2)) = comp(s1) - s2
	 * @param hs
	 */
	private void unionCN(HashSet<Integer> hs)
	{
		apset.removeAll(hs);
		// no need to do complementopt, it is shrinking
	}
	
	/**
	 * intersect(s1, s2) = s2 - comp(s1)
	 * @param hs
	 */
	private void intersectCN(HashSet<Integer> hs)
	{
		iscomplement = false;
		HashSet<Integer> news = new HashSet<Integer>(hs);
		news.removeAll(apset);
		apset = news;
	}
	
	/**
	 * union(s1, s2) = comp(intersect(comp(s1), comp(s2)))
	 * @param comphs
	 */
	private void unionCC(HashSet<Integer> comphs)
	{
		apset.retainAll(comphs);
	}
	
	/**
	 * comp(intersect(s1, s2)) = union(comp(s1), comp(s2))
	 * @param comphs
	 */
	private void intersectCC(HashSet<Integer> comphs)
	{
		apset.addAll(comphs);
		complementopt();
	}
	
	public String toString()
	{
		String tos = "size: " + apset.size();
		//String tos = apset.toString();
		if(iscomplement)
		{
			tos = tos + " complement";
		}
		return tos;
	}
	
	/**
	 * 
	 * @return size of apset
	 */
	public int getSize()
	{
		return apset.size();
	}

}

