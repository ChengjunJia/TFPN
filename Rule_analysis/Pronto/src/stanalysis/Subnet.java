package stanalysis;

public class Subnet {

	long ipaddr;
	int prefixlen;
	String name;
	String iname;
	
	public Subnet(long ip, int masklen, String name)
	{
		this.ipaddr = ip;
		this.prefixlen = 32 - masklen;
		this.name = name;
		this.iname = null;
	}
	
	public Subnet(long ip, int masklen, String name, String iname)
	{
		this(ip, masklen, name);
		this.iname = iname;
	}
	
	public long getipaddr()
	{
		return ipaddr;
	}
	
	public int getprefixlen()
	{
		return prefixlen;
	}
	
	public String getname()
	{
		return name;
	}
	
	public ForwardingRule convertoFW()
	{
		return new ForwardingRule(ipaddr,prefixlen, iname);
	}

	public String toString()
	{
		if(iname == null)
			return name + ":" + ipaddr + " " + prefixlen;
		else
			return name + ":" + ipaddr + " " + prefixlen + " " + iname;
	}
}
