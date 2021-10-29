package reachmatrix;

import java.io.*;
import java.util.*;

public class MyPath implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3149170678363143617L;
	ArrayList<Link> path;
	//maintained by update
	int length;
	Node start;
	Node end;

	/**
	 * maintaining length, start, end
	 */
	void update()
	{
		length = path.size();
		if(length == 0)
		{
			start = null;
			end = null;
		}else
		{
			start = path.get(0).getn1();
			end = path.get(length - 1).getn2();
		}
	}

	public ArrayList<Link> getpath()
	{
		return this.path;
	}

	/**
	 * whether links in path are organized correctly 
	 * @return
	 */
	public boolean validate()
	{
		for(int i = 1; i < path.size(); i ++)
		{
			if(! path.get(i-1).getn2().equals( path.get(i).getn1()))
			{
				return false;
			}
		}
		return true;
	}

	public MyPath(ArrayList<Link> path)
	{
		this.path = path;
		update();
	}
	
	public MyPath(MyPath mp)
	{
		this.path = (ArrayList<Link>) mp.path.clone();
		update();
	}

	public MyPath(Link l)
	{
		path = new ArrayList<Link> ();
		path.add(l);
		update();
	}

	/**
	 * the added link is at the end of the path
	 */
	public void addLink(Link l)
	{
		path.add(l);
		update();
	}

	/**
	 * the concatenated path is at the end of the path
	 * before concatenation, need to check that there is no node overlap 
     * use canConcatenate
	 * @param mp
	 */
	public void concatenatePath(MyPath mp)
	{
		ArrayList<Link> mppath = mp.path;
		for(int i = 0; i < mppath.size(); i ++)
		{
			this.path.add(mppath.get(i));
		}
		update();
	}

	/**
	 * check whether a given node is on the path
	 * if yes, return true
	 * otherwise, return false
	 */
	public boolean onPath(Node n)
	{

		for(int i = 0; i < this.path.size(); i ++)
		{
			Link l = this.path.get(i);
			if(l.getn1().equals(n))
			{
				return true;
			}
		}
		if(this.end.equals(n))
		{
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param mp mp will be appended to the current path
	 * @return -true, meaning that no nodes overlap in the two paths, can concatenate
	 *         -false, meaning otherwise 
	 */
	public boolean canConcatenate(MyPath mp)
	{
		//first check whether the end of the current path equals to the start of mp
		if(!this.end.equals(mp.start))
		{
			System.out.println("h1");
			return false;
		}
		
		for(int i = 0; i < mp.length; i ++)
		{
			Node n = mp.path.get(i).getn2();
			if(this.onPath(n))
			{
				return false;
			}
		}
		
		return true;
		
	}
	

	public String toString()
	{
		String pathString = new String();
		for(int i = 0; i < length; i ++)
		{
			pathString  = pathString + " " + path.get(i).getn1();
		}
		if(length > 0)
		{
			pathString = pathString + " " + end;
		}
		return pathString;
	}


}
