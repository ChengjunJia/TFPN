package stanalysis2;

import StaticReachabilityAnalysis.BDDACLWrapper;
import stanalysis.FWDAPSet;

public class APTuple {

	FWDAPSet fwdaps;
	ACLAPSet aclaps;
	
	/**
	 * 
	 * @param type 0,1
	 */
	public APTuple(int type)
	{
		if(type == BDDACLWrapper.BDDTrue)
		{
			fwdaps = new FWDAPSet(BDDACLWrapper.BDDTrue);
			aclaps = new ACLAPSet(BDDACLWrapper.BDDTrue);
		}else
		{
			fwdaps = new FWDAPSet(BDDACLWrapper.BDDFalse);
			aclaps = new ACLAPSet(BDDACLWrapper.BDDFalse);
		}
	}
	
	public APTuple(FWDAPSet fwdaps, ACLAPSet aclaps)
	{
		this.fwdaps = fwdaps;
		this.aclaps = aclaps;
	}
	
	public APTuple(APTuple anotherapt)
	{
		this.fwdaps = new FWDAPSet(anotherapt.fwdaps);
		this.aclaps = new ACLAPSet(anotherapt.aclaps);
	}
	
	public void union(APTuple anotherapt)
	{
		fwdaps.union(anotherapt.fwdaps);
		aclaps.union(anotherapt.aclaps);
	}
	
	public void union(FWDAPSet faps)
	{
		fwdaps.union(faps);
	}
	
	public void union(ACLAPSet aaps)
	{
		aclaps.union(aaps);
	}
	
	public void intersect(APTuple anotherapt)
	{
		fwdaps.intersect(anotherapt.fwdaps);
		aclaps.intersect(anotherapt.aclaps);
	}
	
	public void intersect(FWDAPSet faps)
	{
		fwdaps.intersect(faps);
	}
	
	public void intersect(ACLAPSet aaps)
	{
		aclaps.intersect(aaps);
	}
	
	public boolean isempty()
	{
		return fwdaps.isempty() || aclaps.isempty();
	}
	
	public boolean isfull()
	{
		return fwdaps.isfull() && aclaps.isfull();
	}
	
	public String toString()
	{
		return "(" + fwdaps + "," + aclaps + ")";
	}
}
