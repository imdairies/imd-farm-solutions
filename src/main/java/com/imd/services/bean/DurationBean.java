package com.imd.services.bean;


public class DurationBean {
	private String start;
	private String end;
	private int steps;
	
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
	public String getEnd() {
		return end;
	}
	public void setEnd(String end) {
		this.end = end;
	}
	public int getSteps() {
		return steps;
	}
	public void setSteps(int steps) {
		this.steps = steps;
	}
	public String toString() {
		return 	"\n start:" + start + 
				"\n end:" + end + 
				"\n steps:" + steps;
	}
}
