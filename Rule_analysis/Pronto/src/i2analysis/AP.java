package i2analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import stanalysis.PositionTuple;

public class AP {
	
	private Integer apbdds;
//	private List<SubAP> subAPset;
//	private List<String> portname;
	private ArrayList<Predicate> listpredicate;
	private HashMap<PositionTuple,ArrayList<Rule>> ptruleset;
	private ArrayList<VlanPredicate> vlanpredicatelist;
	private HashSet<Integer> predicatename=new HashSet<Integer>();
	private Integer apname;
	
	public AP(){
		
	}

	public AP(AP ap) {
		//this.apbdds = ap.getApbdds();
		//this.subAPset = ap.getSubAPset();
		/*
		int subApSet_len = ap.getSubAPset()!=null ? ap.getSubAPset().size() : 0;
		//System.out.println("subApSet_len="+subApSet_len);
		if (subApSet_len>0) {
			SubAP[] arr = new SubAP[subApSet_len];
			System.arraycopy(ap.getSubAPset().toArray(), 0, arr, 0, subApSet_len);
			this.subAPset = new ArrayList<SubAP>(Arrays.asList(arr));
		} else {
			this.subAPset = new ArrayList<SubAP>();
		}
		
		//this.portname = ap.getPortname();
		if (ap.getPortname()!=null) {
			this.portname = new ArrayList<String>();
			for (String port : ap.getPortname()) {
				this.portname.add(port);
			}
		}
		*///yu zhao 2016/4/16
	}
	public AP(Integer apbdds, ArrayList<Predicate> listpredicate) {
		
		this.apbdds = apbdds;
	//	this.subAPset = subAPset;
		this.listpredicate=new ArrayList<Predicate>();
		for (int i=0;i<listpredicate.size();i++){		
		this.listpredicate.add(listpredicate.get(i));
		}
	}
	
	public AP(Integer apbdds, ArrayList<VlanPredicate> Vlanlistpredicate, int aa) {
		
		this.apbdds = apbdds;
	//	this.subAPset = subAPset;
		this.vlanpredicatelist=new ArrayList<VlanPredicate>();
		for (int i=0;i<Vlanlistpredicate.size();i++){		
		this.vlanpredicatelist.add(Vlanlistpredicate.get(i));
		}
	}
	
	
	/*
	public AP(Integer apbdds, List<SubAP> subAPset, List<String> portname) {
		this.apbdds = apbdds;
	//	this.subAPset = subAPset;
		this.portname = portname;
	}
*/
	public Integer getApbdds() {
		return apbdds;
	}

	public void setApbdds(Integer apbdds) {
		this.apbdds = apbdds;
	}

	public ArrayList<Predicate> getListpredicate() {
		return listpredicate;
	}

	public void setListpredicate(ArrayList<Predicate> listpredicate) {
		this.listpredicate = listpredicate;
	}

	public HashMap<PositionTuple, ArrayList<Rule>> getPtruleset() {
		return ptruleset;
	}

	public void setPtruleset(HashMap<PositionTuple, ArrayList<Rule>> ptruleset) {
		this.ptruleset = ptruleset;
	}

	public ArrayList<VlanPredicate> getVlanpredicatelist() {
		return vlanpredicatelist;
	}

	public void setVlanpredicatelist(ArrayList<VlanPredicate> vlanpredicatelist) {
		this.vlanpredicatelist = vlanpredicatelist;
	}

	public HashSet<Integer> getPredicatename() {
		return predicatename;
	}

	public void setPredicatename(HashSet<Integer> predicatename) {
		this.predicatename = predicatename;
	}

	public Integer getApname() {
		return apname;
	}

	public void setApname(Integer apname) {
		this.apname = apname;
	}

	
   
	


	/*
	public static List<SubAP> generateSubAPList(Set<Entry<String,Integer>> predicateRulesSet) {
		List<SubAP> listSubAP=new ArrayList<SubAP>();
				
		// get the rules from predicate
		Iterator<Entry<String,Integer>> iter = predicateRulesSet.iterator();
		
		// loop through the rules in predicate
		while (iter.hasNext()) {
	        Entry<String,Integer> entry = iter.next();
			String rulename = entry.getKey();//拿到predicate的一个rulename
			Integer fwbd = entry.getValue();//拿到predicate的一个rule的BDD

			SubAP subap = new SubAP();//生成一个新的subap对象
			subap.setSubapbdds(fwbd.intValue());//把fwbd放入subap的BDD
			
			// add rule from predicate to SubAP rules
			HashMap<String,Boolean> rule_subap=new HashMap<String,Boolean>();//subap的rule，HashMap<String,Boolean>
			rule_subap.put(rulename, true);//subap中的rule，要加入<rulename,ture>
			subap.setRule(rule_subap);//把
			
			listSubAP.add(subap);
	    }
		
		return listSubAP;
	}
*/
	
}