package com.imd.advisement;

import java.util.List;


import com.imd.dto.Animal;

public abstract class AdvisementRule {
	private String advisementID;
	
	public abstract List<Animal> applyAdvisementRule(String orgID, String languageCd);
	
	public void setAdvisementID(String advisementID) {
		this.advisementID = advisementID;
	}
	
	public String getAdvisementID() {
		return this.advisementID;
	}
}
