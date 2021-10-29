package StaticReachabilityAnalysis;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

public class StudyBDDSizeForACL {

	BDDACLWrapper bps;
	StoreACL sa;
	
	public StudyBDDSizeForACL(String filename)
	{
		sa = StoreACL.LoadNetwork(filename);
		bps = new BDDACLWrapper();
	}
	
	/**
	 * for acls
	 */
	public void BDDSizeForACLs(PrintWriter outf)
	{
		LinkedList<LinkedList<ACLRule>> acllists = sa.ACLList;
		
		for(int i =0; i < acllists.size(); i ++)
		{
			LinkedList<ACLRule> acls = acllists.get(i);
			int aclsnode = bps.ConvertACLs(acls);
			outf.println(acls.size() + " " + bps.getNodeSize(aclsnode));
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		StudyBDDSizeForACL sbs = new StudyBDDSizeForACL("uw-ACLs.ser");
		PrintWriter outf = new PrintWriter(new FileWriter("uw.txt"));
		sbs.BDDSizeForACLs(outf);
		outf.close();
	}
}
