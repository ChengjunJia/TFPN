package reachmatrix;

public class APSet2 {

}

/**
 *
 * package reachmatrix;

import java.util.HashSet;

import org.jgrapht.graph.*;


public class Link extends DefaultEdge{


	*
	 * 
	 *
	private static final long serialVersionUID = 1979946993006965280L;
	
	// used to check whether a set is allowall
	static long universenumber = -1;
	// implement functions to operate universenumber and use universenumber
    // set unviersenumber	
	static public void setUniversenumber(long i)
	{
		universenumber = i;
	}
	
	// is unviersenumber valid
	static public boolean isUniversenumberValid()
	{
		if(universenumber > 0)
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	/**
	 *  simplify union result if it becomes allowall
	 *  if univerenumber is not set, then do nothing
	 * @return
	 *
	public boolean simplifyAP()
	{
		if(isUniversenumberValid())
		{
			return false;
		}else
		{
			if(ak == apkind.other)
			{
				if(ap.size() == universenumber)
				{
					//only now it can be simplified
					this.setap(apkind.allowall, null);
					return true;
				}else
				{
					return false;
				}
			}else{
				return false;
			}
		}
	}

	// n1-->n2
	Node n1;
	Node n2;


	public static enum apkind{
		allowall, denyall, other; 
	}

	HashSet<Integer> ap;
	apkind ak;
	// the interface name
	String iname;
	// this stores the index of id in the acllist in StoreACL class
	int aclid;

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

	/**
	 * when this is called, the new link is used to calculate the union of aps along a path, so iname is not needed
	 * @param l - an existing link
	 *
	public Link(Link l)
	{
		this.n1 = l.n1;
		this.n2 = l.n2;

		if(l.ak == apkind.allowall || l.ak == apkind.denyall)
		{
			this.setap(l.ak, null);
		}else
		{
			this.ak = l.ak;
			this.ap = new HashSet<Integer>(l.getap());
		}
		this.aclid = l.aclid;
		
	}

	/**
	 * 
	 * @param k when apkind is denyall or allowall, the hashset is null
	 * @param a
	 *
	public void setap(apkind k, HashSet<Integer> a)
	{
		ak = k;
		ap = a;
	}

	/**
	 * intersection
	 * @param l - is not changed
	 *
	public void intersect(Link l)
	{
		switch (l.ak)
		{
		case allowall: //do nothing
			break;
		case denyall: 
			this.setap(apkind.denyall, null);
			break;
		case other: 
			switch (this.ak)
			{
			case allowall: 
				this.ak = apkind.other;
				this.ap = new HashSet<Integer> (l.getap());
				break;
			case denyall: // do nothing
				break;
			case other:
				this.ap.retainAll(l.getap());
				if(this.ap.isEmpty())
				{
					this.setap(apkind.denyall, null);
				}else
				{
					this.ak = apkind.other;
				}
				break;
			}
		break;

		}
	}
	
	/**
	 * concatenate a link 
	 * current link is a->b, ap1, l is b->c, ap2
	 * the result is a->c, intersect(ap1, ap2)
	 * @param l - is not changed
	 *
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
	 *
	public void union(Link l)
	{
		switch(l.ak)
		{
		case allowall:
			this.setap(apkind.allowall, null);
			break;
		case denyall: // do nothing
			break;
		case other: 
			switch(this.ak)
			{
			case allowall: // do nothing
				break;
			case denyall:
				this.ak = apkind.other;
				this.ap = new HashSet<Integer> (l.ap);
				break;
			case other:
				this.ap.addAll(l.ap);
				this.simplifyAP();
				break;
			}
			break;
		}
	}
	
	/**
	 * combine two links together
	 * @param l - l is not changed.
	 *
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
	

	public apkind getkind()
	{
		return ak;
	}

	public HashSet<Integer> getap()
	{
		return ap;
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
		return n1.toString() + "-" + n2.toString() + ":" + ak;
	}
	
	public String printAP()
	{
		if(ak == apkind.other)
		{
			return "other: " + ap.size();
		}else{
			return ak.toString();
		}
			
	}

}

 * 
 */
