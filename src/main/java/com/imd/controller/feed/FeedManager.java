package com.imd.controller.feed;

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import com.imd.advisement.DryCowAdvisement;
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
	
	public static final int MATURITY_AGE_IN_DAYS = 270;
	public static final int HEIFER_MIN_AGE_IN_DAYS = 180;
	public static final int DRYOFF_BY_DAYS = 210;
	public static final int PREGNANCY_DAYS = 270;
	public static final int RECENT_PARTURATION_DAYS_LIMIT = 90;

	public List<Animal> getFeedCohortInformationForFarmActiveAnimals(String orgID) throws Exception{
		AnimalLoader aLoader = new AnimalLoader();
		List<Animal> farmActiveHerd = aLoader.retrieveActiveAnimals(orgID);
		if (farmActiveHerd == null || farmActiveHerd.isEmpty())
			throw new IMDException("The farm does not seem to have any active animal");
		Iterator<Animal> it = farmActiveHerd.iterator();
		while (it.hasNext()) {
			Animal animal = it.next();
			animal.setFeedCohortInformation(getAnimalFeedCohortType(animal.getOrgID(), animal.getAnimalTag()));
//			IMDLogger.log(animal.getFeedCohortInformation().toString(), Util.INFO);
		}		
		return farmActiveHerd;
	}
	
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
		String animalFeedCohortDeterminationCriteria = null;
		boolean isMale = animal.getClass().getSimpleName().equals(Sire.class.getSimpleName());
		DateTime now = DateTime.now();
		DateTime latestInseminationOrMatingEventTS = getLatestEventTimeStamp(animal,Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING);
		DateTime latestParturationEventTS = getLatestEventTimeStamp(animal,Util.LifeCycleEvents.PARTURATE);
		int ageInDays = animal.getCurrentAgeInDays().getDays();
		if (isMale && ageInDays >= MATURITY_AGE_IN_DAYS) {
			animalFeedCohortCD = Util.FeedCohortType.BULL;
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is MALE and older than " + MATURITY_AGE_IN_DAYS + " days hence its feed cohort is " + animalFeedCohortCD;
			animalFeedCohortDeterminationCriteria = "Male Animal whose age is >= " + MATURITY_AGE_IN_DAYS;
		} else if (isMale && ageInDays < MATURITY_AGE_IN_DAYS) {
			animalFeedCohortCD = Util.FeedCohortType.MALECALF;
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is MALE and younger than " + MATURITY_AGE_IN_DAYS + " days hence its feed cohort is " + animalFeedCohortCD;
			animalFeedCohortDeterminationCriteria = "Male Animal whose age is < " + MATURITY_AGE_IN_DAYS;
		} else if (animal.getAnimalTypeCD().equals(Util.AnimalTypes.FEMALECALF) && isWeanedOff(animal) && ageInDays < HEIFER_MIN_AGE_IN_DAYS) {
			animalFeedCohortCD = Util.FeedCohortType.FEMALEWEANEDOFF;
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is FEMALECALF and has been weaned off. Its feed cohort is " + animalFeedCohortCD;
			animalFeedCohortDeterminationCriteria = Util.AnimalTypes.FEMALECALF + " whose age is < " + HEIFER_MIN_AGE_IN_DAYS + " and has a weaned off event in its lifecycle events history";
		} else if (animal.getAnimalTypeCD().equals(Util.AnimalTypes.FEMALECALF) && isWeanedOff(animal) && ageInDays >= HEIFER_MIN_AGE_IN_DAYS) {
			animalFeedCohortCD = Util.FeedCohortType.HEIFER;
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is FEMALECALF and has been weaned off. Its feed cohort is " + animalFeedCohortCD + ". You MUST change its animal type to " + Util.AnimalTypes.HEIFER;
			animalFeedCohortDeterminationCriteria = Util.AnimalTypes.FEMALECALF + " whose age is >= " + HEIFER_MIN_AGE_IN_DAYS + " and has a weaned off event in its lifecycle events history. You MUST change its animal type to " + Util.AnimalTypes.HEIFER;
		} else if (animal.getAnimalTypeCD().equals(Util.AnimalTypes.FEMALECALF) && !isWeanedOff(animal) && ageInDays < HEIFER_MIN_AGE_IN_DAYS) {
			animalFeedCohortCD = Util.FeedCohortType.FEMALECALF;
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is FEMALECALF and has NOT been weaned off. Its feed cohort is " + animalFeedCohortCD;
			animalFeedCohortDeterminationCriteria = Util.AnimalTypes.FEMALECALF + " whose age is < " + HEIFER_MIN_AGE_IN_DAYS + " and does NOT have a weaned off event in its lifecycle events history";
		} else if (animal.isHeifer() && (ageInDays >= HEIFER_MIN_AGE_IN_DAYS && 
					(!animal.isPregnant() || 
					  (animal.isPregnant() && 
						(latestInseminationOrMatingEventTS == null || now.isBefore(latestInseminationOrMatingEventTS.plusDays(DRYOFF_BY_DAYS)))
					  )
					 )
					)) {
			animalFeedCohortCD = Util.FeedCohortType.HEIFER;
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is HEIFER that is not going to calve with-in next " + (PREGNANCY_DAYS - DRYOFF_BY_DAYS) + " days";
			animalFeedCohortDeterminationCriteria = "Animal whose type indicates that it is a HEIFER whose age is >= " 
			+ HEIFER_MIN_AGE_IN_DAYS + " is either NOT pregnant or its insemination event is less than " +
			DRYOFF_BY_DAYS + " days ago";
		} else if (animal.isHeifer() && (ageInDays >= HEIFER_MIN_AGE_IN_DAYS && animal.isPregnant()  && 
				   latestInseminationOrMatingEventTS != null && now.isAfter(latestInseminationOrMatingEventTS.plusDays(DRYOFF_BY_DAYS)))){
			animalFeedCohortCD = Util.FeedCohortType.HEIFERCLOSEUP;
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is HEIFERCLOSEUP that is going to calve with-in next " + (PREGNANCY_DAYS - DRYOFF_BY_DAYS) + " days";
			animalFeedCohortDeterminationCriteria = "Animal whose type indicates that it is a HEIFER whose age is >= " 
			+ HEIFER_MIN_AGE_IN_DAYS + " is pregnant has an insemination event that is older than " +
			DRYOFF_BY_DAYS + " days";
		} else if (animal.isLactating()  && 
				   latestParturationEventTS != null && now.isBefore(latestParturationEventTS.plusDays(RECENT_PARTURATION_DAYS_LIMIT))){
			animalFeedCohortCD = Util.FeedCohortType.LACTATINGEARLY;
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is LACTATINGEARLY. It calved with-in last " + RECENT_PARTURATION_DAYS_LIMIT + " days";
			animalFeedCohortDeterminationCriteria = "Animal whose type indicates that it is LACTATING and it parturated with-in the last " 
			+ RECENT_PARTURATION_DAYS_LIMIT + " days";
		}
		
		// remaining cohorts: lactating mid, lactation far, lactating dry
		// high yielder lactating early, high yielder lactating mid, high yielder lactation far, lactating dry
		FeedCohort cohort = null;
		
		if (animalFeedCohortCD == null) {
			cohort = new FeedCohort(animal.getOrgID(),"UNDETERMINED", "");
			cohort.setAnimalFeedCohortDeterminatationMessage("The animal " + animal.getAnimalTag() + " could not be mapped to any Feed Cohort.");
			cohort.setFeedCohortDeterminationCriteria("None of the determination criteria matches the profle of this animal");
		} else {
			cohort = new FeedCohort(animal.getOrgID(),animalFeedCohortCD, "");
			cohort.setAnimalFeedCohortDeterminatationMessage(animalFeedCohortDeterminatationMessage);
			cohort.setFeedCohortDeterminationCriteria(animalFeedCohortDeterminationCriteria);
		}
		//IMDLogger.log("\n" + cohort.dtoToJson(""), Util.INFO);
		return cohort;
	}

	private DateTime getLatestEventTimeStamp(Animal animal, String targetEventType1, String targetEventType2) {
		List<LifecycleEvent> sortedEvents = animal.getLifeCycleEvents();
		// events are assumed to be sorted by date with latest on top and oldest at bottom.
		if (sortedEvents == null || sortedEvents.isEmpty())
			return null;
		else {
			Iterator<LifecycleEvent> it = sortedEvents.iterator();
			while (it.hasNext()) {
				LifecycleEvent event = it.next();
				if (event.isEventOfSpecifiedType(targetEventType1) || event.isEventOfSpecifiedType(targetEventType2))
					return event.getEventTimeStamp();
			}
		}			
		return null;
	}
	private DateTime getLatestEventTimeStamp(Animal animal, String targetEventType) {
		return getLatestEventTimeStamp(animal,targetEventType,null);
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
