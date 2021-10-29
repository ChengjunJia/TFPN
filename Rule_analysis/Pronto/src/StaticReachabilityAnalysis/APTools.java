package StaticReachabilityAnalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

public class APTools {

	static void RealTimeStat(int[]s)
	{
		// to be done
		Arrays.sort(s);
		double avg = 0.0;
		for(int i = 0; i < s.length; i ++)
		{
			avg = avg + s[i];
		}
		avg = avg/s.length;
		int q25 = (int)Math.rint(s.length/4.0) - 1; 
		int q50 = (int)Math.rint(s.length/2.0) - 1;
		int q75 = (int)Math.rint(s.length/4.0*3) - 1;
		System.out.println("Quantiles: " + s[q25]+" "+s[q50]+" "+s[q75]);
		System.out.println("Average: " + avg);
	}

	public static void DebugACLs(AtomicPredicate ap, int [] ind)
	{
		System.out.println("Select " + ind.length +" ACLs:");
		System.out.println();
		for(int i = 0; i < ind.length; i ++)
		{
			String aclName = ap.listOfACLNames.get(ind[i]);
			System.out.println(aclName);
			System.out.println(ap.mapOfPacketSets.get(aclName));
			System.out.println("-------------------------------------------------");
		}
	}

	//option = detail / simple / save
	public static void DebugAPs(AtomicPredicate ap, String option, String suffix) throws IOException
	{
		if(option.equals("detail"))
		{		
			System.out.println("ALL " + ap.numOfAPs +" APs:");
			System.out.println();
			for(int i = 0; i < ap.numOfAPs; i ++)
			{
				System.out.println(ap.listOfAPs.get(i));
				System.out.println("-------------------------------------------------");
			}
		}else if(option.equals("simple"))
		{
			System.out.println("Totally " + ap.numOfAPs + "APs.");
		}else if(option.equals("save"))
		{
			System.out.println("Totally " + ap.numOfAPs + "APs.");
			File outputFile = new File("APsaved" +suffix + ".txt");
			if(!outputFile.exists())
			{
				outputFile.createNewFile();
			}

			FileWriter fw = new FileWriter(outputFile);

			String del = "#";

			for(int i = 0; i < ap.numOfAPs; i ++)
			{
				PacketSet curAP = ap.listOfAPs.get(i);
				ArrayList<Tuple> al = curAP.tupleArray;
				for(int j = 0; j < al.size(); j ++)
				{
					Tuple t = (Tuple) al.get(j);

					fw.write(t.sourceIP.lower + " ");
					fw.write(t.sourceIP.upper + " ");
					fw.write(t.sourcePort.lower + " ");
					fw.write(t.sourcePort.upper + " ");

					fw.write(t.destinationIP.lower + " ");
					fw.write(t.destinationIP.upper + " ");
					fw.write(t.destinationPort.lower + " ");
					fw.write(t.destinationPort.upper + " ");

					fw.write(t.protocol.lower + " ");
					fw.write(t.protocol.upper + " ");
					fw.write("\n");
				}
				fw.write(del + "\n");
			}
			fw.close();

		}
	}

	static public void MergeAP(File indir) throws IOException
	{
		String filenames[] = indir.list();
		File outfile = new File(indir.getName() + "merged");
		if(!outfile.exists())
		{
			outfile.createNewFile();
		}
		FileWriter fw = new FileWriter(outfile);

		for(int i = 0; i < filenames.length; i ++)
		{
			Scanner sc = new Scanner(new File(indir, filenames[i]));
			sc.useDelimiter("\n");
			// read line by line
			while(sc.hasNext())
			{
				fw.write(sc.next() + "\n");
			}
			sc.close();
			System.out.println(i+" / " + filenames.length);
		}
		fw.close();
	}

	static public void SplitAP(int numperfile, File fileAP) throws IOException
	{
		int apcounter = 0;
		int filecounter = 0;
		Scanner sc = new Scanner(fileAP);
		sc.useDelimiter("\n");
		File filepart = new File(fileAP.getName() + "p" + filecounter);
		if(!filepart.exists())
		{
			filepart.createNewFile();
		}
		FileWriter fw = new FileWriter(filepart);
		while(sc.hasNext())
		{
			String tuplestr = sc.next();
			fw.write(tuplestr + "\n");
			if(tuplestr.length() <= 2)
			{
				apcounter = apcounter + 1;
				if(apcounter == numperfile)
				{// need to get a new file
					fw.close();
					filecounter = filecounter + 1;

					filepart = new File(fileAP.getName() + "p" + filecounter);
					fw = new FileWriter(filepart);
					System.out.println("write " + apcounter);
					apcounter = 0;
				}
			}
		}
		fw.close();
		if(apcounter == 0){filepart.delete();}
		else{
			System.out.println("write " + apcounter);
		}
	}

	static public void SplitAPOnDemand(int[] numperfile, File fileAP) throws IOException
	{
		int apcounter = 0;
		int filecounter = 0;
		int numind = 0;
		Scanner sc = new Scanner(fileAP);
		sc.useDelimiter("\n");
		File filepart = new File(fileAP.getName() + "p" + filecounter);
		if(!filepart.exists())
		{
			filepart.createNewFile();
		}
		FileWriter fw = new FileWriter(filepart);
		while(sc.hasNext())
		{
			String tuplestr = sc.next();
			fw.write(tuplestr + "\n");
			if(tuplestr.length() <= 2)
			{
				apcounter = apcounter + 1;
				if(numind == numperfile.length)
				{// right the rest to a file
					continue;
				}
				if(apcounter == numperfile[numind])
				{// need to get a new file
					fw.close();
					filecounter = filecounter + 1;

					filepart = new File(fileAP.getName() + "p" + filecounter);
					fw = new FileWriter(filepart);
					numind ++;
					System.out.println("write " + apcounter);
					apcounter = 0;
				}
			}
		}
		fw.close();
		if(apcounter == 0)
		{filepart.delete();}else
		{
			System.out.println("write " + apcounter);
		}
	}

	static public void ReadAP(LinkedList<PacketSet> listAP, File fileAP) throws FileNotFoundException
	{
		Scanner sc = new Scanner(fileAP);
		sc.useDelimiter("\n");
		PacketSet ps = new PacketSet();
		while(sc.hasNext())
		{
			String tuplestr = sc.next();
			if(tuplestr.length() <=2)
			{
				listAP.add(ps);
				ps = new PacketSet();
			}else
			{
				Scanner linesc = new Scanner(tuplestr);
				linesc.useDelimiter(" ");
				long [] tupleval = new long[10];
				int i = 0;
				while(linesc.hasNext())
				{
					tupleval[i] = Long.parseLong(linesc.next());
					i ++;
				}
				Tuple newt = new Tuple();
				newt.sourceIP.lower = tupleval[0];
				newt.sourceIP.upper = tupleval[1];
				newt.sourcePort.lower = tupleval[2];
				newt.sourcePort.upper = tupleval[3];

				newt.destinationIP.lower = tupleval[4];
				newt.destinationIP.upper = tupleval[5];
				newt.destinationPort.lower = tupleval[6];
				newt.destinationPort.upper = tupleval[7];

				newt.protocol.lower = tupleval[8];
				newt.protocol.upper = tupleval[9];
				ps.tupleArray.add(newt);
			}
		}
		System.out.println(listAP.size()+" APs readed.");
	}

	static public void TestNumOfAPs(AtomicPredicate ap, File outputFile, int totalrun, int[] scale) throws IOException
	{//acls should be parsed, packetset of acls should be generated
		if(!outputFile.exists())
		{
			outputFile.createNewFile();
		}

		FileWriter fw = new FileWriter(outputFile);


		int poolsize = ap.listOfACLNames.size();
		int[] oneScaleRes = new int[totalrun];

		for(int i = 0; i < scale.length; i++)
		{
			int[] s = new int[scale[i]];
			fw.write(scale[i] + "\t");
			System.out.print("size = " + scale[i] + ", runtime = ");

			for(int runtime = 0; runtime < totalrun; runtime ++)
			{
				System.out.print(runtime + " ");
				Sample.GetSample(s.length, poolsize, s);
				ap.CalAPs(s);
				fw.write(ap.numOfAPs + "\t");
				oneScaleRes[runtime] = ap.numOfAPs;
			}
			System.out.println();
			fw.write("\n");
			fw.flush();
			APTools.RealTimeStat(oneScaleRes);
			s = null;
		}
		fw.close();

	}

	static public void CountACLTuples(AtomicPredicate ap) throws IOException
	{
		File acltuple = new File("ACLTuples.txt");
		if(!acltuple.exists())
		{
			acltuple.createNewFile();
		}
		FileWriter fw = new FileWriter(acltuple);
		for(int i = 0; i < ap.listOfACLNames.size(); i++)
		{
			String aclname = ap.listOfACLNames.get(i);
			PacketSet ps = ap.mapOfPacketSets.get(aclname);
			fw.write(ps.Size() + "\n");
		}
		fw.close();
	}

	static public void CountAPTuples(AtomicPredicate ap) throws IOException
	{
		File aptuple = new File("aptuple.txt");
		if(!aptuple.exists())
		{
			aptuple.createNewFile();
		}
		FileWriter fw = new FileWriter(aptuple);
		for(int i = 0; i < ap.numOfAPs; i ++)
		{
			PacketSet ps = ap.listOfAPs.get(i);
			fw.write(ps.Size() + "\n");
		}

		fw.close();
	}

	static public void DebugACLExpr(AtomicPredicate ap)
	{
		//show the ap-based expression
		for(int i = 0; i < ap.listOfACLNames.size(); i ++)
		{
			String aclname = ap.listOfACLNames.get(i);
			System.out.print(aclname + ": ");
			System.out.println(ap.ACLExpr.get(aclname));
		}
	}

	static public void CountACLExpr(AtomicPredicate ap) throws IOException
	{
		File aclexpr = new File("aclexpr.txt");
		if(!aclexpr.exists())
		{
			aclexpr.createNewFile();
		}
		FileWriter fw = new FileWriter(aclexpr);
		for(int i = 0; i < ap.listOfACLNames.size(); i ++)
		{
			String aclname = ap.listOfACLNames.get(i);
			fw.write(ap.ACLExpr.get(aclname).size() + "\n");
		}

		fw.close();
	}

	static public int ACLNum(AtomicPredicate ap)
	{
		return ap.listOfACLNames.size();
	}

	static public int RuleNum(AtomicPredicate ap)
	{
		int rulecounter = 0;
		//count the number of rules
		for (int i = 0; i < ap.listOfACLNames.size(); i ++)
		{
			String aclname = ap.listOfACLNames.get(i);
			rulecounter = rulecounter + ap.tableOfACLs.get(aclname).size();
		}

		return rulecounter;
	}

	/*************
	 * 
	 * @param apexpr
	 * @param order - how to select ap expr
	 * @throws IOException 
	 */

	static public void APGrowsGivenOrder(LinkedList<LinkedList<Integer>> aclexpr, int [] order, 
			LinkedList<Integer> universe, FileWriter fw ) throws IOException
			{
		LinkedList<LinkedList<Integer>> aplist = new LinkedList<LinkedList<Integer>>();
		aplist.add((LinkedList<Integer>)universe.clone());
		//System.out.println(aplist.get(0).size());

		for(int i = 0; i < order.length; i ++)
		{
			LinkedList<Integer> toadd = (LinkedList<Integer>) aclexpr.get(order[i]).clone();
			// add acl i
			if(toadd.isEmpty()||toadd.size() == universe.size())
			{
				//just not change

			}else
			{
				LinkedList<LinkedList<Integer>> aplisttemp = new LinkedList<LinkedList<Integer>>();
				//try to use randomization to average the performance
				int [] r = new int[aplist.size()];
				Sample.GetSample(aplist.size(), aplist.size(), r);
				
				int j = 0;
				
				for( j = 0; j < aplist.size(); j ++)
				{
					//System.out.println(j);
					
					LinkedList<Integer> randj = aplist.get(r[j]);
					//LinkedList<Integer> randj = aplist.get(j);
					LinkedList<Integer> curap1 = new LinkedList<Integer> ();
					LinkedList<Integer> curap2 = new LinkedList<Integer> ();

					//curap1.retainAll(toadd);
					//System.out.print(toadd.size() + " ");
					boolean notempty = SetOperation.Intersection(toadd, randj, curap1);
					//toadd.removeAll(curap1);
					//System.out.print(toadd.size() + " ");
					//System.out.println(curap1.size());
					if(notempty)
					{
						aplisttemp.add(curap1);
						if(curap1.size() == randj.size())
						{// in this case, get(j) is a subset of toadd,
							// so the intersection of toaddcomp and get(j) is empty 
							continue;
						}else{
							// in this case, get(j) is spliced... curap1 should < get(j)
							SetOperation.Difference(randj, curap1, curap2);
							aplisttemp.add(curap2);
						}
					}else
					{
						// in this case, get(j) is a subset of toaddcomp
						aplisttemp.add((LinkedList<Integer>) randj.clone());
						continue;
					}
					if(toadd.isEmpty())
					{
						//no need to do the rest
						break;
					}
				}
				
				for(int k = j + 1; k < aplist.size(); k ++)
				{
					aplisttemp.add((LinkedList<Integer>) aplist.get(r[k]).clone());
					//aplisttemp.add((LinkedList<Integer>) aplist.get(k).clone());
				}
				aplist = null;
				aplist = aplisttemp;
			}

			fw.write(aplist.size() + " ");

			//System.out.println("["+ i + " ACLs Added, Number of AP: " + aplist.size()+"]");
		}

		fw.write("\n");
		fw.flush();
		System.out.println("\n");
		//System.out.println(aplist);

			}

	static public void APGrowsRandom(int numofap, LinkedList<LinkedList<Integer>> aclexpr, 
			int totalrun, String filesuffix) throws IOException
	{
		File outputFile = new File("apgrowsrandom" + filesuffix + ".txt");
		if(!outputFile.exists())
		{
			outputFile.createNewFile();
		}

		FileWriter fw = new FileWriter(outputFile);

		LinkedList<Integer> universe = new LinkedList<Integer>();
		for(int i = 0; i < numofap; i ++)
		{
			universe.add(i);
		}

		int [] order = new int[aclexpr.size()];
		for(int runtime = 0; runtime < totalrun; runtime ++)
		{
			System.out.print(runtime + " ");
			Sample.GetSample(order.length, order.length, order);
			APGrowsGivenOrder(aclexpr, order, universe, fw);
		}

		fw.close();

	}
	
	static public void APGrowsFromFile(int numofap, LinkedList<LinkedList<Integer>> aclexpr, 
			String outsuffix, File orderfile) throws IOException
	{
		File outputFile = new File("apgrows" + outsuffix + ".txt");
		if(!outputFile.exists())
		{
			outputFile.createNewFile();
		}

		FileWriter fw = new FileWriter(outputFile);

		LinkedList<Integer> universe = new LinkedList<Integer>();
		for(int i = 0; i < numofap; i ++)
		{
			universe.add(i);
		}

		int [] order = new int[aclexpr.size()];
		ReadOrder(orderfile, order);
		APGrowsGivenOrder(aclexpr, order, universe, fw);

		fw.close();

	}

	static public void ReadACLExpr(LinkedList<LinkedList<Integer>> aclexpr, File infile) 
			throws FileNotFoundException
			{
		//aclexpr should be cleared
		Scanner sc = new Scanner(infile);
		sc.useDelimiter("\n");
		LinkedList<Integer> oneexpr; 
		while(sc.hasNext())
		{
			oneexpr = new LinkedList<Integer>();

			String oneexprstr = sc.next();

			Scanner linesc = new Scanner(oneexprstr);
			linesc.useDelimiter(" ");
			while(linesc.hasNext())
			{
				String apid = linesc.next();
				oneexpr.add(Integer.parseInt(apid));
			}

			aclexpr.add(oneexpr);
		}
		System.out.println(aclexpr.size()+" ACL expressions readed.");
			}
	
	static public void ACLComplexity(AtomicPredicate ap) throws IOException
	{
		File aclnum = new File("aclnum.txt");
		if(!aclnum.exists())
		{
			aclnum.createNewFile();
		}
		
		FileWriter fw = new FileWriter(aclnum);
		for(int i = 0; i < ap.listOfACLNames.size(); i ++)
		{
			LinkedList acl = ap.tableOfACLs.get(ap.listOfACLNames.get(i));
			fw.write(acl.size() + "\n");
		}
		fw.close();
		
		File aclnumbyrouter = new File("aclnumbyrouter.txt");
		if(!aclnumbyrouter.exists())
		{
			aclnumbyrouter.createNewFile();
		}
		File aclsinrouter = new File("aclsinrouter.txt");
		if(!aclsinrouter.exists())
		{
			aclsinrouter.createNewFile();
		}
		FileWriter fwnum = new FileWriter(aclnumbyrouter);
		FileWriter fwrouter = new FileWriter(aclsinrouter);
		for(int i = 0; i < ap.listOfConfig.size(); i ++)
		{
			Range r = ap.ACLsInConfig.get(ap.listOfConfig.get(i));
			fwrouter.write(r.lower + " " + r.upper + "\n");
			int aclcounter=0;
			for(long j = r.lower; j <= r.upper; j ++)
			{
				String aclname = ap.listOfACLNames.get((int) j);
				aclcounter = aclcounter + ap.tableOfACLs.get(aclname).size();
			}
			fwnum.write(aclcounter + "\n");
		}
		fwrouter.close();
		fwnum.close();
	}
	
	static public void ReadOrder(File infile, int[] order ) throws FileNotFoundException
	{
		Scanner scanner = new Scanner(infile);
		scanner.useDelimiter("\n");

		int i = 0;
		while(scanner.hasNextInt()){
		   int newind = scanner.nextInt();
		   order[i++] = newind;
		   //System.out.println(newind);
		}
	}

	static public void APGrowsByConfig(int numofap, LinkedList<LinkedList<Integer>> aclexpr, 
			int totalrun, AtomicPredicate ap, String filesuffix) throws IOException
			{
		File outputFile = new File("apgrowsbyconfig" + filesuffix + ".txt");
		if(!outputFile.exists())
		{
			outputFile.createNewFile();
		}

		FileWriter fw = new FileWriter(outputFile);

		LinkedList<Integer> universe = new LinkedList<Integer>();
		for(int i = 0; i < numofap; i ++)
		{
			universe.add(i);
		}

		int [] configOrder = new int[ap.listOfConfig.size()];
		int [] aclOrder = new int[ap.listOfACLNames.size()];

		for(int runtime = 0; runtime < totalrun; runtime ++)
		{
			System.out.print(runtime + " ");
			Sample.GetSample(configOrder.length, configOrder.length, configOrder);
			int apOrderCounter = 0;
			for(int i = 0; i < configOrder.length; i ++)
			{			
				Range r = ap.ACLsInConfig.get(ap.listOfConfig.get(configOrder[i]));
				//System.out.println(r);
				for(long j = r.lower; j <= r.upper; j ++)
				{
					aclOrder[apOrderCounter] = (int) j;
					//System.out.println(j +" " + aclOrder[apOrderCounter]);
					apOrderCounter ++;
				}
			}
			APGrowsGivenOrder(aclexpr, aclOrder, universe, fw);
		}

		fw.close();

			}
	

}
