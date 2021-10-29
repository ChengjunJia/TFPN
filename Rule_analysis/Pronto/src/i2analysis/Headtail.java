package i2analysis;

public class Headtail {

	private int[] head;
	private int[] tail;
	
	public Headtail(int[] head, int[] tail){
	    this.head=new int[32];
	    this.tail=new int[32];
		for (int i=0;i<32;i++)
		{
			this.head[i]=head[i];
		}
	
		for (int i=0;i<32;i++)
		{
			this.tail[i]=tail[i];
		}
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
}
