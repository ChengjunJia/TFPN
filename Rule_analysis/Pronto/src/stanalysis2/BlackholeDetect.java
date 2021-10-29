package stanalysis2;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import stanalysis.FWDAPSet;

public class BlackholeDetect {
	NetworkAPT net;

	public BlackholeDetect(NetworkAPT n)
	{
		net = n;
	}

	
	public void FindBlackhole()
	{
		for(DeviceAPT d : net.getAllDevices())
		{
			System.out.println(d.name);
			Collection<FWDAPSet> aps = d.getfwaps();
			Iterator<FWDAPSet> iter = aps.iterator();
			FWDAPSet aps1 = new FWDAPSet(iter.next());
			while(iter.hasNext())
			{
				aps1.union(iter.next());
				//System.out.println(aps1);
			}
			System.out.println(aps1.isfull());
			System.out.println(aps1);
		}
	}
	
	public void FindBlackholeBench()
	{
		for(DeviceAPT d : net.getAllDevices())
		{
			System.out.println(d.name);
			double ti = FindBlackholeOne(d);
			System.out.println(ti);
		}
	}

	public double FindBlackholeOne(DeviceAPT d)
	{
		Collection<FWDAPSet> aps = d.getfwaps();
		int rep = 5000;
		long start = System.nanoTime();
		for(int i = 0; i < rep; i ++)
		{
			Iterator<FWDAPSet> iter = aps.iterator();
			FWDAPSet aps1 = new FWDAPSet(iter.next());
			while(iter.hasNext())
			{
				aps1.union(iter.next());
			}
		}
		long end = System.nanoTime();
		return (end - start)/1000000.0/rep;
	}

	public static void main(String[] args) throws IOException
	{
		NetworkAPT n = new NetworkAPT("st");
		BlackholeDetect bhd = new BlackholeDetect(n);
		bhd.FindBlackholeBench();
		//bhd.FindBlackhole();
	}
}
