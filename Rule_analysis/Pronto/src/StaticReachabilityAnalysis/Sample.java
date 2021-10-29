package StaticReachabilityAnalysis;

/*************************************************************************
 *  Compilation:  javac Sample.java
 *  Execution:    java Sample M N
 *
 *  This program takes two command-line arguments M and N and produces
 *  a random sample of M of the integers from 0 to N-1 without replacement.
 *
 *  % java Sample 6 49
 *  10 20 0 46 40 6
 *
 *  % java Sample 10 1000
 *  656 488 298 534 811 97 813 156 424 109
 *
 *************************************************************************/

public class Sample {
	// s is of length m
	public static void GetSample(int M, int Maxsize, int[] s)
	{
		int[] perm = new int[Maxsize];
		for (int i = 0; i < Maxsize; i++)
			perm[i] = i;

		// create random sample in perm[0], perm[1], ..., perm[M-1]
		for (int i = 0; i < M; i++)  {

			// random integer between i and N-1
			int r = i + (int) (Math.random() * (Maxsize-i));

			// swap elements at indices i and r
			int t = perm[r];
			perm[r] = perm[i];
			perm[i] = t;
		}

		// print results
		for (int i = 0; i < M; i++)
			s[i] = perm[i];
	}


	public static void main(String[] args)
	{
		//test random
		//System.out.println(Math.random());
		int m = 20;
		int n = 20;
		int [] s = new int[n];
		//GetSample(n,n,s);
		GetSample(n,m,s);
		for(int i = 0; i < s.length; i ++)
		{
			System.out.println(s[i]);
		}
	}

}