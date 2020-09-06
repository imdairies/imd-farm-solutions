package com.imd.advisement;

import java.util.List;

import com.imd.dto.Animal;

public class LifecycleStatusCorrectnessAdvisement extends AdvisementRule {

	@Override
	public List<Animal> applyAdvisementRule(String orgID, String languageCd) {
		/**
		 * 1) Check the animal gender and see if the lifecycle status is according to the gender e.g. FEMALECALF, LACTATION, PREGNANT etc. are only applicable to Female gender etc.
		 * 2) FEMALECALF, MALECALF, FEMALENEWBORN, MALENEWBORN are only applicable to calves less than 3 months of age.
		 * 3) HEIFER is only applicable to non-lactating animal whose age is greater than 6 months. 
		 * 4) LACTATING animal should have some milk record.
		 * 5) DRY=Y cow should not have any milk record.
		 * 6) Parturitied recently but hasn't been set to lactating is still in Post Lactation.
		 */

		return null;
	}



}
