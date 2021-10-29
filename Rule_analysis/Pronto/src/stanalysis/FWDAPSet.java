package stanalysis;

import java.util.HashSet;
import reachmatrix.APSet1;

public class FWDAPSet extends APSet1{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6682820011054707611L;

	public FWDAPSet(HashSet<Integer> hs) {
		super(hs);//hs 是device里面的所有相关APP
		// TODO Auto-generated constructor stub
	}
	public FWDAPSet(int settype) {
		super(settype);
	}
	
	public FWDAPSet(FWDAPSet aps2)
	{
		super(aps2);
	}
	
}
