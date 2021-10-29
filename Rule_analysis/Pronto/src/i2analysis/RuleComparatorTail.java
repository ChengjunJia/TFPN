package i2analysis;

import java.util.Comparator;

public class RuleComparatorTail implements Comparator<Rule> {

	@Override
	public int compare(Rule rule1, Rule rule2) {
		// TODO Auto-generated method stub
		int[] tail1 = rule1.getTail();
		int[] tail2 = rule2.getTail();
		
		// TODO: compare head1 and head2
		// if rule1 is less than rule2, return -1
		// if rule1 is equal to rule2, return 0
		// if rule1 is grater than rule2, return 1
		for(int i=32-1;i>=0;i--){
			if(tail1[i]!=tail2[i])
			{
				if (tail1[i]==1){
					return 1;
				}else{return -1;}
			}			
		}
		return 0;
		
		

	}
		
	
}
