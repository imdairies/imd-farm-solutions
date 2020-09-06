package com.imd.services.bean;

public class InputDelimitedFileBean {
	private boolean shouldAdd;
	private String inputDelimitedFileContents;
	private String loginToken;
	private boolean donotOverwrite;

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

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
				"\"donotOverwrite\":" + donotOverwrite + "\n" +
			   "\"inputDelimitedFileContents\":" + inputDelimitedFileContents;
	
	}

	public boolean isDonotOverwrite() {
		return donotOverwrite;
	}

	public void setDonotOverwrite(boolean donotOverwrite) {
		this.donotOverwrite = donotOverwrite;
	}

}
