package org.apache.openjpa.util;

public class Foo2 {
	
	public String string;
	public int i;
    public short shrt;
    public Foo1 b;
    public Foo2() {}
    public Foo2(String s, int i) {
    	this.string = s; this.i = i;
    	}
    public Foo2(short s, Foo1 b){
    	this.shrt = s; 
    	this.b = b;
    	}
    public Foo2(short s){
    	this.shrt = s; 
    	}
    public Foo2(short shrt,String string) {
		super();
		this.string = string;
		this.shrt = shrt;
	}
   
}

