package com.imd.services.bean;

public class AdvisementBean {
	private String animalTag;
	private String advisementID;
	private boolean threshold1Violated;
	private boolean threshold2Violated;
	private boolean threshold3Violated;

	public String getAdvisementID() {
		return advisementID;
	}

	public void setAdvisementID(String advisementID) {
		this.advisementID = advisementID;
	}
	

	public boolean isThreshold1Violated() {
		return threshold1Violated;
	}

	public void setThreshold1Violated(boolean threshold1Violated) {
		this.threshold1Violated = threshold1Violated;
	}

	public boolean isThreshold2Violated() {
		return threshold2Violated;
	}

	public void setThreshold2Violated(boolean threshold2Violated) {
		this.threshold2Violated = threshold2Violated;
	}

	public boolean isThreshold3Violated() {
		return threshold3Violated;
	}

	public void setThreshold3Violated(boolean threshold3Violated) {
		this.threshold3Violated = threshold3Violated;
	}
	public String toString() {
		return  "animalTag: " + animalTag + "\n" +
				"advisementID: " + advisementID + "\n" +
				"threshold1Violated: " + threshold1Violated + "\n" +
				"threshold2Violated: " + threshold2Violated + "\n" +
				"threshold3Violated: " + threshold3Violated + "\n";
	}

	public String getAnimalTag() {
		return animalTag;
	}

	public void setAnimalTag(String animalTag) {
		this.animalTag = animalTag;
	}
}
