package i2analysis;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;  
import java.util.Comparator;  
import java.util.List;

//import jdd.util.Array;  
  
public class test3 {  
      
      
      
    public static void main(String[] args) {  
    	
    	int[] aaa={1,2,3,4,5};
    	int[] bbb={1,2,3,4,5};
    	boolean a12a=Arrays.equals(aaa,bbb);
    	System.out.println(a12a);
    	
        List<String> lists = new ArrayList<String>();  
        List<A> list = new ArrayList<A>();  
        List<B> listB = new ArrayList<B>();  
        lists.add("5");  
        lists.add("2");  
        lists.add("9");  
        //lists中的对象String 本身含有compareTo方法，所以可以直接调用sort方法，按自然顺序排序，即升序排序  
        Collections.sort(lists);  
          
        A aa = new A();  
        aa.setName("aa");  
        aa.setOrder(1);  
        A bb = new A();  
        bb.setName("bb");  
        bb.setOrder(2);  
        list.add(bb);  
        list.add(aa);  
        //list中的对象A实现Comparable接口  
        Collections.sort(list);  
          
        B ab = new B();  
        ab.setName("ab");  
        ab.setOrder("1");  
        B ba = new B();  
        ba.setName("ba");  
        ba.setOrder("2");  
        listB.add(ba);  
        listB.add(ab);  
        //根据Collections.sort重载方法来实现  
        Collections.sort(listB,new Comparator<B>(){  
            @Override  
            public int compare(B b1, B b2) {  
                return b1.getOrder().compareTo(b2.getOrder());  
            }  
              
        });  
          
        System.out.println(lists);  
        System.out.println(list);  
        System.out.println(listB);  
          
    }  
  
}  
  
class A implements Comparable<A>{  
    private String name;  
    private Integer order;  
    public String getName() {  
        return name;  
    }  
    public void setName(String name) {  
        this.name = name;  
    }  
      
    public Integer getOrder() {  
        return order;  
    }  
    public void setOrder(Integer order) {  
        this.order = order;  
    }  
    @Override  
    public String toString() {  
        return "name is "+name+" order is "+order;  
    }  
    @Override  
    public int compareTo(A a) {  
        return this.order.compareTo(a.getOrder());  
    }  
      
}  
  
class B{  
    private String name;  
    private String order;  
    public String getName() {  
        return name;  
    }  
    public void setName(String name) {  
        this.name = name;  
    }  
    public String getOrder() {  
        return order;  
    }  
    public void setOrder(String order) {  
        this.order = order;  
    }  
    @Override  
    public String toString() {  
        return "name is "+name+" order is "+order;  
    }  
}  