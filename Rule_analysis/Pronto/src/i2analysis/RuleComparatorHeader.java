package i2analysis;

import java.util.Comparator;

public class RuleComparatorHeader implements Comparator<Rule> {

	@Override
	public int compare(Rule rule1, Rule rule2) {
		// TODO Auto-generated method stub
		int[] head1 = rule1.getHead();
		int[] head2 = rule2.getHead();
		
		// TODO: compare head1 and head2
		// if rule1 is less than rule2, return -1
		// if rule1 is equal to rule2, return 0
		// if rule1 is grater than rule2, return 1
		for(int i=32-1;i>=0;i--){
			if(head1[i]!=head2[i])
			{
				if (head1[i]==1){
					return 1;
				}else{return -1;}
			}			
		}
		return 0;
		
		

	}
		
	
}
