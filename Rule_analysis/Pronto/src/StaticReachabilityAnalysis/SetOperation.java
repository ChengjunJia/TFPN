package StaticReachabilityAnalysis;

import java.util.LinkedList;
import java.util.Stack;

public class SetOperation {
	/***
	 * all are sorted, L1, L2 have no duplicate elements
	 * @param L1
	 * @param L2
	 * @param IL - should be cleared, the result of the intersection
	 * return true means IL is not empty
	 * the result
	 * L1 = L1 - L2
	 */
	public static boolean Intersection(LinkedList<Integer> L1, LinkedList<Integer> L2, 
			LinkedList<Integer> IL)
	{
		int indL2 = 0;
		Stack<Integer> todel = new Stack<Integer>();
		for(int i = 0; i < L1.size(); i ++)
		{
			while(indL2 < L2.size())
			{
				int l2val = L2.get(indL2);
				int l1val = L1.get(i);
				
				if(l2val < l1val)
				{
					indL2 ++;
				}else if(l2val == l1val)
				{
					IL.add(L1.get(i));
					//System.out.print(i + " ");
					todel.push(i);
					//L1.remove(i);
					indL2 ++;
					break;
				}else{

					break;
				}
				
			}
		}
		 //delete these
		while(!todel.isEmpty())
		{
			int i = todel.pop();
			L1.remove(i);
			// this does not work !L1.remove(todel.pop());
		}
		//System.out.println();
		
		if(IL.isEmpty())
		{
			return false;
		}else{
			return true;
		}
	}
	/*
	 */
	public static boolean Difference(LinkedList<Integer> L1, LinkedList<Integer> L2, 
			LinkedList<Integer> DL)
	{
		int indL1 = 0;
		for(int i = 0; i < L2.size(); i ++)
		{
			while(indL1 < L1.size())
			{
				int l1val = L1.get(indL1);
				int l2val = L2.get(i);
				
				if(l1val < l2val)
				{
					DL.add(L1.get(indL1));
					//
					indL1 ++;
				}else if(l1val == l2val)
				{
					indL1 ++;
					break;
				}else{
					break;
				}
				
			}
		}
		// if L1 is very long, and there are remaining elements
		if(indL1 < L1.size())
		{
			for(int i = indL1; i < L1.size(); i ++)
			{
				DL.add(L1.get(i));
			}
		}
		
		if(DL.isEmpty())
		{
			return false;
		}else{
			return true;
		}
	}


	public static void main(String[] args)
	{
		//test random
		LinkedList<Integer> universe = new LinkedList<Integer> ();
		LinkedList<Integer> expr = new LinkedList<Integer> ();
		
		int [] aclexpr = {3, 4, 5, 6};
		int total = 10;
		
		for(int i = 0; i < total; i ++)
		{
			universe.add(i);
		}
		for(int i = 0; i < aclexpr.length; i ++)
		{
			expr.add(aclexpr[i]);
		}
		
		System.out.println(expr.size());
		//System.out.println(expr);
		System.out.println(universe.size());
		//System.out.println(universe);

		LinkedList<Integer> DL = new LinkedList<Integer> ();
		Difference(universe, expr, DL);
		System.out.println(DL.size());
		
		
		LinkedList<Integer> IL = new LinkedList<Integer> ();
		Intersection(universe, expr, IL);
		System.out.println(IL.size());
		System.out.println(universe);

	}

}
