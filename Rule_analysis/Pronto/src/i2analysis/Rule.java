package i2analysis;


public class Rule {
	private String portname;
    private String devicename;
	private int[] head;
	private int[] tail;
	private Integer ruleBDD;
	private String rulename;
	private int prefixlen;
	
	public Rule(String portname,String devicename,int[] head, int[] tail, Integer ruleBDD, String rulename){
		setPortname(portname);
		setDevicename(devicename);
		this.head=new int[32];
		for(int i=0;i<31;i++){
			this.head[i]=head[i];
		}
		
		this.tail=new int[32];
		for(int i=0;i<31;i++){
			this.tail[i]=tail[i];
		}
		
		setRuleBDD(ruleBDD);
		setRulename(rulename);	
	}

	public String getPortname() {
		return portname;
	}

	public void setPortname(String portname) {
		this.portname = portname;
	}

	public String getDevicename() {
		return devicename;
	}

	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}

	public int[] getHead() {
		return head;
	}

	public void setHead(int[] head) {
		this.head = head;
	}

	public int[] getTail() {
		return tail;
	}

	public void setTail(int[] tail) {
		this.tail = tail;
	}

	public Integer getRuleBDD() {
		return ruleBDD;
	}

	public void setRuleBDD(Integer ruleBDD) {
		this.ruleBDD = ruleBDD;
	}

	public String getRulename() {
		return rulename;
	}

	public void setRulename(String rulename) {
		this.rulename = rulename;
	}

	public int getPrefixlen() {
		return prefixlen;
	}

	public void setPrefixlen(int prefixlen) {
		this.prefixlen = prefixlen;
	}
	
    
}
