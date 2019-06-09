package com.imd.controller.feed;

import java.util.Iterator;
import java.util.List;


import com.imd.dto.Animal;
import com.imd.dto.CohortNutritionalNeeds;
import com.imd.dto.FeedCohort;
import com.imd.dto.FeedItem;
import com.imd.dto.FeedPlan;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.Sire;
import com.imd.loader.AnimalLoader;
import com.imd.loader.FeedLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class FeedManager {
	
	private static final int MATURITY_AGE_IN_DAYS = 270;
//	private static final int WEANED_OFF_AGE_LIMIT_IN_DAYS = 90;

	public FeedCohort getAnimalFeedCohortType(String orgID, String animalTag) throws Exception {
		AnimalLoader animalLoader = new AnimalLoader();
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
		List<Animal> animals = animalLoader.getAnimalRawInfo(orgID, animalTag);		
		if (animals == null || animals.isEmpty()) {
			throw new IMDException("Animal not found [" + orgID + "-" + animalTag + "]");
		}
		if (animals.size() > 1) {
			throw new IMDException("Multiple animals found for the ORG-TAG [" + orgID + "-" + animalTag + "]. This indicates that the data has been corrupted. Please remove the dubplicate record.");			
		}
		Animal animal = animals.get(0);
		animal.setLifeCycleEvents(eventLoader.retrieveAllLifeCycleEventsForAnimal(orgID, animalTag));

		return findAnimalFeedCohort(animal);
	}
	
	public List<CohortNutritionalNeeds> getAnimalNutritionalNeeds(FeedCohort cohort, Float start, Float end) {
		FeedLoader loader = new FeedLoader();
		// Get the nutritional needs of this animal from FEED_COHORT_NUTRITIONAL_NEEDS table.
		return loader.retrieveCohortNutritionalNeeds(cohort, start, end);
	}

	public FeedPlan getPersonalizedFeedPlan(FeedCohort cohort, Float start, Float end, Animal animal) throws IMDException {
		FeedLoader loader = new FeedLoader();
		if (cohort.getFeedCohortTypeCD().equals(Util.FeedCohortType.FEMALECALF)) {
			return getFEMALECALFPlan(loader.retrieveFeedPlan(cohort.getOrgID(), cohort.getFeedCohortTypeCD()), animal);
		} else 
			return null;
	}
	
	
	private FeedPlan getFEMALECALFPlan(FeedPlan plan, Animal animal) {
		if (plan == null) {
			plan = new FeedPlan();
		}
		if (plan.getFeedPlan() == null || plan.getFeedPlan().isEmpty()) {
			plan.setPlanAnalysisComments("The cohort " + plan.getFeedCohort().getFeedCohortTypeCD() + " does not have any feed plan specified. Therefore, we can not create a personalized feed for " + animal.getAnimalTag());
			return plan;
		} else {
			Iterator<FeedItem> it = plan.getFeedPlan().iterator();
			while (it.hasNext()) {
				FeedItem item = it.next();
				if (item.getFulFillmentTypeCD().equals(Util.FulfillmentType.ABSOLUTE)) {
					item.setPersonalizedFeedMessage("Give the animal " + item.getFulfillmentPct() + " " + item.getUnits() + " of " + item.getFeedItemCD());
				} else if (item.getFulFillmentTypeCD().equals(Util.FulfillmentType.FREEFLOW)) {
					item.setPersonalizedFeedMessage("Put " + item.getFeedItemCD() + " infront of the animal and let is consume it freely.");
				} else if (item.getFulFillmentTypeCD().equals(Util.FulfillmentType.BODYWEIGHT)) {
					item.setPersonalizedFeedMessage("Give the animal " + item.getFeedItemCD() + " " + item.getFulfillmentPct()*100 + " " + item.getUnits() + " of body weight");
				} else {
					item.setPersonalizedFeedMessage("Unable to determine the usage of " + item.getFeedItemCD());
				} 
			}
		}
		return plan;
	}

	private FeedCohort findAnimalFeedCohort(Animal animal) throws IMDException{
		String animalFeedCohortCD = null;
		String animalFeedCohortDeterminatationMessage = null;
		boolean isMale = animal.getClass().getSimpleName().equals(Sire.class.getSimpleName());
		if (isMale && animal.getCurrentAgeInDays().getDays() >= MATURITY_AGE_IN_DAYS) {
			animalFeedCohortCD = Util.FeedCohortType.BULL;
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is MALE and older than " + MATURITY_AGE_IN_DAYS + " days hence its feed cohort is " + animalFeedCohortCD;
		} else if (isMale && animal.getCurrentAgeInDays().getDays() < MATURITY_AGE_IN_DAYS) {
			animalFeedCohortCD = Util.FeedCohortType.MALECALF;
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is MALE and younger than " + MATURITY_AGE_IN_DAYS + " days hence its feed cohort is " + animalFeedCohortCD;
		} else if (animal.getAnimalTypeCD().equals(Util.AnimalTypes.FEMALECALF) && isWeanedOff(animal)) {
			animalFeedCohortCD = Util.FeedCohortType.FEMALEWEANEDOFF;
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is FEMALECALF and has been weaned off. Its feed cohort is " + animalFeedCohortCD;
		} else if (animal.getAnimalTypeCD().equals(Util.AnimalTypes.FEMALECALF) && !isWeanedOff(animal)) {
			animalFeedCohortCD = Util.FeedCohortType.FEMALECALF;
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is FEMALECALF and has NOT been weaned off. Its feed cohort is " + animalFeedCohortCD;
		}
		if (animalFeedCohortCD == null) {
			throw new IMDException("The animal " + animal.getAnimalTag() + " could not be mapped to any Feed Cohort.");
		} 
		FeedCohort cohort = new FeedCohort(animal.getOrgID(),animalFeedCohortCD, "");
		cohort.setAnimalFeedCohortDeterminatationMessage(animalFeedCohortDeterminatationMessage);
		//IMDLogger.log("\n" + cohort.dtoToJson(""), Util.INFO);
		return cohort;
	}

	private boolean isWeanedOff(Animal animal) throws IMDException{
		List<LifecycleEvent> events = animal.getLifeCycleEvents();
		boolean weanedOff = false;
		if (events == null || events.isEmpty()) {
//			throw new IMDException("Could not determine whether the animal " + animal.getAnimalTag() + " has been weaned off or not because no life cycle events were found for this animal.");
			IMDLogger.log("Could not determine whether the animal " + animal.getAnimalTag() + " has been weaned off or not because no life cycle events were found for this animal.", Util.INFO);
			return false; 
		}
		try {
			Iterator<LifecycleEvent> it = events.iterator();
			while (it.hasNext()) {
				if (it.next().getEventType().getEventCode().equals(Util.LifeCycleEvents.WEANEDOFF)) {
					weanedOff = true;
					break;				
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IMDException("Could not determine whether the animal " + animal.getAnimalTag() + " has been weaned off or not because of the following exception: " + ex.getClass().getSimpleName() + " " + ex.getMessage());
		}
		return weanedOff;
	}
}
