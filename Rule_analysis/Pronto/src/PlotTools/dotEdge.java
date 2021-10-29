package PlotTools;

/*
 * for the getXX method, if the field is not set, then return an empty string
 */
public class dotEdge {

	private final String edgeOp = "->";

	private boolean directed = true;

	private String src;

	private String dst;

	private String label;
	
	private String ltail;
	private String lhead;

	public dotEdge(String s, String d)
	{
		src = s;
		dst = d;
	}
	
	public void setLTail(String l)
	{
		ltail = l;
	}
	
	public void setLHead(String l)
	{
		lhead = l;
	}
	
	public String getLHead()
	{
		if(lhead == null)
		{
			return "";
		}else
		{
			return "lhead=" + lhead;
		}
	}
	
	public String getLTail()
	{
		if(ltail == null)
		{
			return "";
		}else
		{
			return "ltail=" + ltail;
		}
	}

	public void setLabel(String l)
	{
		label = l;
	}

	public String getLabel()
	{
		if(label != null)
			return "label=\"" + label + "\"";
		else
			return "";
	}

	public String getDirection()
	{
		if(directed)
		{
			return "";
		}else
		{
			return "dir = none";
		}
	}

	public void Directed()
	{
		directed = true;
	}

	public void Undirected()
	{
		directed = false;
	}

	public String toString()
	{
		return edgeFromTo() + edgeAttr();
	}

	private String edgeAttr()
	{
		String edgeA = "";
		String labelStr = getLabel();
		String dirStr = getDirection();
		String lheadStr = getLHead();
		String ltailStr = getLTail();
		
		edgeA = AddAttr(edgeA, labelStr);
		edgeA = AddAttr(edgeA, dirStr);
		edgeA = AddAttr(edgeA, lheadStr);
		edgeA = AddAttr(edgeA, ltailStr);

		if(edgeA.isEmpty())
		return edgeA;
		else
			return "["+edgeA+"]";
	}
	
	private String AddAttr(String attr, String feature)
	{
		String added = "";
		if(!feature.isEmpty())
		{
			if(attr.isEmpty())
			{
				return feature;
			}else
			{
				added = attr + "," + feature;
			}
		}else
		{
			return attr;
		}
		return added;
	}

	private String edgeFromTo()
	{
		return src + edgeOp + dst;
	}

	public static void main(String[] args)
	{
		dotEdge edge = new dotEdge("A", "B");
		System.out.println(edge);
		
		edge.Undirected();
		System.out.println(edge);
		
		edge.setLabel("a2");
		System.out.println(edge);
		
		edge.setLHead("cluster0");
		System.out.println(edge);
		
		edge.setLTail("cluster1");
		System.out.println(edge);

	}
}
