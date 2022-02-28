package bean.basis;

import java.util.ArrayList;

import interfaces.Header;

public class Node {
	Header hdr = null;
	String deviceName;
	int port = -1;
	boolean isOut;
	ArrayList<Integer> visits = new ArrayList<Integer>();
	ArrayList<Header> hsHistory = new ArrayList<Header>();
	ArrayList<Rule> ruleHistory = new ArrayList<Rule>();

	public Node() {
	}

	public Node(Node node) {
		if (node.getHdr() != null) {
			this.hdr = node.getHdr().copy();
		}
		this.port = node.getPort();
		// for (Rule r : node.getRuleHistory()) {
		// this.ruleHistory.add(r);
		// }
		this.ruleHistory.addAll(node.getRuleHistory());
		// when copying the node, copy the rule history
	}

	public Node(Header hdr, int port) {
		if (hdr != null) {
			this.hdr = hdr.copy();
		}
		this.port = port;
	}

	public Header getHdr() {
		return hdr;
	}

	public void setHdr(Header hdr) {
		this.hdr = hdr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ArrayList<Integer> getVisits() {
		return visits;
	}

	public void setVisits(ArrayList<Integer> visits) {
		this.visits = visits;
	}

	public ArrayList<Header> getHsHistory() {
		return hsHistory;
	}

	public void setHsHistory(ArrayList<Header> hsHistory) {
		this.hsHistory = hsHistory;
	}

	public ArrayList<Rule> getRuleHistory() {
		return this.ruleHistory;
	}

	public void addRuleHistory(Rule r) {
		this.ruleHistory.add(r);
	}

	public void addRuleHistory(ArrayList<Rule> ruleInput) {
		this.ruleHistory.addAll(ruleInput);
	}

	public void addRuleHistory(Node n) {
		this.ruleHistory.addAll(n.getRuleHistory());
	}

	public void setRuleHistory(ArrayList<Rule> ruleHistory) {
		this.ruleHistory = ruleHistory;
	}

	@Override
	public String toString() {
		String result = "";
		System.out.println("Start to get the visited result");
		if (this.hdr != null) {
			result += this.hdr.toString() + "\n";
		}
		System.out.println("End the get visited result");
		result += this.port + "\n";
		result += this.visits.toString() + "\n";
		return result;
	}

	public void printSelf() {
		System.out.println("-----");
		if (this.hdr != null) {
			System.out.println(this.hdr.toString());
		}
		System.out.println(this.port);
		System.out.println(this.visits);
		System.out.println("-----");
	}
}