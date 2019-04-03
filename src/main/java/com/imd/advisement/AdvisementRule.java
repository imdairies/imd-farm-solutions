package com.imd.advisement;

import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import com.imd.dto.Animal;

public abstract class AdvisementRule {
	private String advisementID;
	
	/**
	 * applies the advisement rule specific logic to identify the animals on whom this rule should be applied
	 * @return
	 */
	public abstract List<Animal> getAdvisementRuleAddressablePopulation(String orgID);
	
	/**
	 * Executes the rule specific logic on the addressablePopulation and returns the result of applying the rule and
	 * returns the outcome of the rule application on each of the animals in the addressablePopulation.
	 * @return
	 */
	public abstract HashMap<Animal, String> applyAdvisementRule(List<Animal> addressablePopulation);
	
	/**
	 * Socializes the rule outcome through the specific channel e.g. show the outcome in Alerts window and/or SMS the outcome etc.
	 * @param addressablePopulation
	 * @return
	 */
	public abstract HashMap<Animal, Integer> channelAdvisementRuleOutcome(List<Animal> addressablePopulation);

	public String getAdvisementID() {
		return advisementID;
	}

	public void setAdvisementID(String advisementID) {
		this.advisementID = advisementID;
	}
	
}
