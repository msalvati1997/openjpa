package org.apache.openjpa.util;


public class Foo1 {
	public String string;
	public Double Dbl;
	public double dbl;
    public Foo1() {}
    public Foo1(double d) {
    	this.dbl = d;
    	}
    public Foo1(String s, Double i) {
    	this.string = s; this.Dbl = i;
    	}
  
}