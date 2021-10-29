package stanalysis;

public class ForwardingRule implements Comparable<ForwardingRule>{
	long destip;
	int prefixlen;
	String outinterface;
	int bddrep;
	boolean visible;
	String devicename;
	String portname;
	
	public ForwardingRule(long destip, int prefixlen, String outinterface)
	{
		this.destip = destip;
		this.prefixlen = prefixlen;
		this.outinterface = outinterface;
		this.visible = true;
	}
	
	
	
	public long getDestip() {
		return destip;
	}



	public void setDestip(long destip) {
		this.destip = destip;
	}



	public int getPrefixlen() {
		return prefixlen;
	}



	public void setPrefixlen(int prefixlen) {
		this.prefixlen = prefixlen;
	}



	public String getOutinterface() {
		return outinterface;
	}



	public void setOutinterface(String outinterface) {
		this.outinterface = outinterface;
	}



	public int getBddrep() {
		return bddrep;
	}



	public void setBddrep(int bddrep) {
		this.bddrep = bddrep;
	}



	public boolean isVisible() {
		return visible;
	}



	public void setVisible(boolean visible) {
		this.visible = visible;
	}



	public String getDevicename() {
		return devicename;
	}



	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}



	public String getPortname() {
		return portname;
	}



	public void setPortname(String portname) {
		this.portname = portname;
	}



	public ForwardingRule()
	{

	}
	
	public int compareTo(ForwardingRule another_rule) {
        return this.prefixlen - another_rule.prefixlen;
    }
	
	public void setVisible()
	{
		visible = true;
	}
	
	public void setInvisible()
	{
		visible = false;
	}
	
	public boolean isvisible()
	{
		return visible;
	}
	
	public long getdestip()
	{
		return destip;
	}
	
	public int getprefixlen()
	{
		return prefixlen;
	}
	
	public String getiname()
	{
		return outinterface;
	}
	
	public void setBDDRep(int bddentry)
	{
		bddrep = bddentry;
	}
	
	public int getBDDRep()
	{
		return bddrep;
	}

	public String toString()
	{
		return destip + " " + prefixlen + " " + outinterface;
	}
}
