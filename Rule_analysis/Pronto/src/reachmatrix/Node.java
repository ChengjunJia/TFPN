package reachmatrix;

import java.io.*;

/**
 * This class abstract routers and subnets.
 * @author carmo
 *
 */
public class Node implements Serializable{

	String name;
	long hashval; // just a very simple one
	
	public static enum type {
		router, subnet;
	}
	private static final long serialVersionUID = -5699294613624126225L;
	
	type nodetype;
	
	/**
	 * 
	 * @param n - name of the node, if it is a subnet, then its name is the IP prefix. if it is a router, its name is the host name.
	 * @param t - type
	 */
	public Node(String n, type t)
	{
		name = n;
		nodetype = t;
		//calculate hash value
		hashval = (name.hashCode() << 1) + nodetype.ordinal();
	}
	
	public String toString()
	{
		return name;
	}
	
	/**
	 * 
	 * @return true if it is a router
	 */
	public boolean isRouter()
	{
		if(nodetype == type.router)
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	/**
	 * check whether two nodes are identical
	 * use hash value calculated in the node
	 * @param n
	 * @return
	 */
	public boolean equals(Object n)
	{
		if(this.hashval == ((Node) n).hashval)
		{
			return true;
		}else
		{
			return false;
		}
		
		/* too slow...
		if(this.nodetype != n.nodetype)
		{
			return false;
		}
		if(!this.name.equals(n.name))
		{
			return false;
		}
		return true;
		*/
	}
	
	public int hashCode()
	{
		return (int)hashval;
	}
}
