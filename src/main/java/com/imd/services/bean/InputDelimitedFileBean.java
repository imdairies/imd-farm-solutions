package com.imd.services.bean;

public class InputDelimitedFileBean {
	private boolean shouldAdd;
	private String inputDelimitedFileContents;

	public String getInputDelimitedFileContents() {
		return inputDelimitedFileContents;
	}

	public void setInputDelimitedFileContents(String inputContents) {
		this.inputDelimitedFileContents = inputContents;
	}

	public boolean getShouldAdd() {
		return shouldAdd;
	}

	public void setShouldAdd(boolean add) {
		this.shouldAdd = add;
	}
	
	public String toString() {
		return "\"shouldAdd\":" + shouldAdd + "\n" +
			   "\"inputDelimitedFileContents\":" + inputDelimitedFileContents;
	
	}

}
