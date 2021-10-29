package reachmatrix;

import java.util.HashSet;

import org.jgrapht.graph.*;


public class Link extends DefaultEdge{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1979946993006965280L;
	
	// n1-->n2
	Node n1;
	Node n2;

	APSet aps;
	// the interface name
	String iname;
	// this stores the index of id in the acllist in StoreACL class
	int aclid;
	String aclname;

	public Link(Node n1, Node n2, String iname)
	{
		this.n1 = n1;
		this.n2 = n2;
		this.iname = iname;
		this.aclid = -1;
	}
	
	public void setACLId(int id)
	{
		aclid = id;
	}
	
	public int getACLId( )
	{
		return aclid;
	}
	
	public void setACLName()
	{
		aclname = "null";
	}
	
	public void setACLName(String rn, String acln)
	{
		aclname = rn + "," + acln;
	}
	
	public String getACLName()
	{
		return aclname;
	}

	/**
	 * when this is called, the new link is used to calculate the union of aps along a path, so iname is not needed
	 * @param l - an existing link
	 */
	public Link(Link l)
	{
		this.n1 = l.n1;
		this.n2 = l.n2;

		this.aps = new APSet(l.aps);
		this.aclid = l.aclid;
		this.aclname = l.aclname;
		
	}

	/**
	 * 
	 * @param k when apkind is denyall or allowall, the hashset is null
	 * @param a
	 */
	public void setap(HashSet<Integer> a)
	{
		aps = new APSet(a);
	}
	
	/**
	 * 0 - bddfalse, 1 - bddtrue
	 * @param aptype
	 */
	public void setap(int aptype)
	{
		aps = new APSet(aptype);
	}

	/**
	 * intersection
	 * @param l - is not changed
	 */
	public void intersect(Link l)
	{
		aps.intersect(l.aps);
	}
	
	/**
	 * concatenate a link 
	 * current link is a->b, ap1, l is b->c, ap2
	 * the result is a->c, intersect(ap1, ap2)
	 * @param l - is not changed
	 */
	public void concatenateLink(Link l)
	{
		// should point to the same node
		if(! this.getn2().equals(l.getn1()))
		{
			System.err.println("cannot link " + this + " and " + l);
			System.exit(1);
		}
		
		this.n2 = l.n2;
		this.intersect(l);
	}
	
	/**
	 * union
	 * @param l
	 */
	public void union(Link l)
	{
		aps.union(l.aps);
	}
	
	/**
	 * combine two links together
	 * @param l - l is not changed.
	 */
	public void combineLink(Link l)
	{
		if(this.n1 .equals( l.n1) && this.n2 .equals( l.n2))
		{
			this.union(l);
		}else
		{
			System.err.println("cannot combine links" + this + " and " + l);
			System.exit(1);
		}
	}
	


	public APSet getap()
	{
		return aps;
	}

	public Node getn1()
	{
		return n1;
	}

	public Node getn2()
	{
		return n2;
	}

	public String getname()
	{
		return iname;
	}

	public String toString()
	{
		return n1.toString() + "-" + n2.toString() + ":" + aps.toString();
	}
	
	public int getAPSize()
	{
		return aps.getSize();
	}

}
