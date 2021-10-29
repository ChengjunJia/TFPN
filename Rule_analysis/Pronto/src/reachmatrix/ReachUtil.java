package reachmatrix;

import java.util.ArrayList;

import org.jgrapht.GraphPath;


public class ReachUtil {

	/**
	 * return a link from the source of the path to the sink of the path
	 * ap is the intersection of all aps of links along the path
	 * @param path
	 * @return
	 */
	public static Link pathConcatenation(GraphPath<Node,Link> path)
	{
		ArrayList<Link> edges = (ArrayList<Link>) path.getEdgeList();
		return pathConcatenation(edges);

	}
	
	public static Link pathConcatenation(ArrayList<Link> edges)
	{
		if(edges.size() == 0)
			return null;
		else
		{
			Link resLink = null;
			for(int i = 0; i < edges.size(); i ++)
			{
				if(i == 0)
				{
					resLink = new Link((Link) edges.get(i));
				}else
				{
					resLink.concatenateLink((Link) edges.get(i));
				}
			}
			return resLink;
		}

	}
	

}
