package reachmatrix;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

import StaticReachabilityAnalysis.BDDACLWrapper;
import StaticReachabilityAnalysis.BDDAP;

public class PathRun {

	static BDDAP ap = BDDAP.LoadBDDAP("purdue-BDDAP.ser");

	public static void main(String[] args)
	{
		APSet.setUniverse(ap.getAllAP());
		String [] data_files = {"end2endpath74", "end2endpath161", "end2endpath224", "end2endpath351", "end2endpath519", "end2endpath603", "end2endpath647"
				,"end2endpath880", "end2endpath904", "end2endpath941"};

		for(String data_file : data_files)
		try{
			System.out.println(data_file);
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(data_file);
			PrintWriter pw = new PrintWriter(data_file + "apv");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				String [] aclnames = parseline(strLine);
				if(aclnames.length > 2)
				{
					APSet[] aps = getAPSets(aclnames);
					double time1 = testtime(aps);
					pw.println(time1);
				}

			}
			//Close the input stream
			in.close();
			pw.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

	public static String[] parseline(String aline)
	{
		String alinestrip = aline.trim();
		String[] tokens = alinestrip.split(" ");

		return tokens;
	}

	public static APSet[] getAPSets(String[] acls)
	{
		APSet[] apsets = new APSet[acls.length];
		for(int i = 0; i < acls.length; i++)
		{
			String acl = acls[i];
			if(acl.equals("null"))
			{
				apsets[i] = new APSet(BDDACLWrapper.BDDTrue);
			}else
			{
				String[] tokens = acl.split(",");
				HashSet<Integer> apexp = ap.getAPExp(tokens[0], tokens[1]);

				if(apexp.contains(BDDACLWrapper.BDDFalse))
				{
					apsets[i] = new APSet(BDDACLWrapper.BDDFalse);
				}else if(apexp.contains(BDDACLWrapper.BDDTrue))
				{
					apsets[i] = new APSet(BDDACLWrapper.BDDTrue);
				}else
				{
					apsets[i] = new APSet(apexp);
				}

			}
		}
		return apsets;
	}

	public static double testtime(APSet[] apsets)
	{
		int rep = 100;

		long start = System.nanoTime();
		for(int i = 0; i < rep; i ++)
		{
			APSet res = new APSet(BDDACLWrapper.BDDTrue);

			for(APSet aset : apsets)
			{
				res.intersect(aset);
			}
		}
		long end = System.nanoTime();
		return (end - start)/1000000.0/rep;
	}

}
