package stanalysis;

import java.io.Serializable;

/**
 * tuple (switch, port)
 * @author nrlab
 *
 */
public class PositionTuple implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5901751895887589638L;
	
	String devicename;
	String portname;
	
	public PositionTuple(String dname, String pname)
	{
		devicename = dname;
		portname = pname;
	}
	
	public String getDeviceName()
	{
		return devicename;
	}
	
	public String getPortName()
	{
		return portname;
	}
	
	public boolean equals(Object pt)
	{
		return devicename.equals(((PositionTuple)pt).getDeviceName()) 
				&& portname.equals(((PositionTuple)pt).getPortName());
	}
	
	
	public int hashCode()
	{
		return devicename.hashCode() * 13 + portname.hashCode();
	}
	
	public String toString()
	{
		return devicename + "," + portname;
	}
	
	
	public static void main(String[] args)
	{
		System.out.println(new PositionTuple("a","b").equals(new PositionTuple("a", "1")));
	}
	
}
