package com.imd.controller.feed;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.imd.advisement.DryCowAdvisement;
import com.imd.dto.Animal;
import com.imd.dto.CohortNutritionalNeeds;
import com.imd.dto.FeedCohort;
import com.imd.dto.FeedItem;
import com.imd.dto.FeedPlan;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.MilkingDetail;
import com.imd.dto.Sire;
import com.imd.loader.AnimalLoader;
import com.imd.loader.FeedLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.MilkingDetailLoader;
import com.imd.services.MilkingInformationSrvc;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

public class FeedManager {
	
	public static final int MATURITY_AGE_IN_DAYS = 270;
	public static final int HEIFER_MIN_AGE_IN_DAYS = 180;
	public static final int DRYOFF_BY_DAYS = 210;
	public static final int RECENT_PARTURATION_DAYS_LIMIT = 90;
	public static final int MID_PARTURATION_DAYS_START = 90;
	public static final int MID_PARTURATION_DAYS_END = 180;
	public static final int PREGNANCY_DURATION_DAYS = 270;
	public static final int NEAR_PARTURATION_THRESHOLD_DAYS = 15;

	public List<Animal> getFeedCohortInformationForFarmActiveAnimals(String orgID) throws Exception{
		AnimalLoader aLoader = new AnimalLoader();
		List<Animal> farmActiveHerd = aLoader.retrieveActiveAnimals(orgID);
		if (farmActiveHerd == null || farmActiveHerd.isEmpty())
			throw new IMDException("The farm does not seem to have any active animal");
		Iterator<Animal> it = farmActiveHerd.iterator();
		while (it.hasNext()) {
			Animal animal = it.next();
			animal.setWeight(getLatestEventWeight(animal.getOrgID(), animal.getAnimalTag()));
			animal.setFeedCohortInformation(getAnimalFeedCohortType(animal.getOrgID(), animal.getAnimalTag()));
			animal.setAnimalNutritionalNeeds(getAnimalNutritionalNeeds(animal),animal.getWeight());
//			IMDLogger.log(animal.getFeedCohortInformation().toString(), Util.INFO);
		}		
		return farmActiveHerd;
	}

	private CohortNutritionalNeeds getAnimalNutritionalNeeds(Animal animal) {
		FeedLoader feedLoader = new FeedLoader();
		List<CohortNutritionalNeeds> needs = feedLoader.retrieveCohortNutritionalNeeds(animal.getFeedCohortInformation(), 0f, 9999f);
		CohortNutritionalNeeds need = null;
		if (needs == null || needs.isEmpty()) {
			IMDLogger.log("Nutritional Needs for the animal " + animal.getAnimalTag() + " could not be found. This indicates that nutritioanl needs for the cohort " + animal.getFeedCohortInformation().getFeedCohortTypeCD() + " has not been configured", Util.ERROR);
			return null;
		} else if (needs.size() >1) {
			IMDLogger.log(needs.size() + " nutritional needs records were found for the animal " + animal.getAnimalTag() + " belonging to the cohort " + animal.getFeedCohortInformation().getFeedCohortTypeCD() + ". We will use the record with the smallest start and end values.", Util.ERROR);
		} 
		return needs.get(0);
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
	
	public FeedPlan getPersonalizedFeedPlan(FeedCohort cohort, String animalTag) throws IMDException {
		FeedLoader loader = new FeedLoader();
//		if (cohort.getFeedCohortTypeCD().equals(Util.FeedCohortType.FEMALECALF)) {
			return getFeedCohortPersonalizedPlan(cohort,loader.retrieveFeedPlan(cohort.getOrgID(), cohort.getFeedCohortTypeCD()), animalTag);
//		} else 	if (cohort.getFeedCohortTypeCD().equals(Util.FeedCohortType.BULL)) {
//			return getFeedCohortPersonalizedPlan(loader.retrieveFeedPlan(cohort.getOrgID(), cohort.getFeedCohortTypeCD()), animal);
//		} else 
//
//			return null;
	}
	
	private FeedPlan getFeedCohortPersonalizedPlan(FeedCohort cohort, FeedPlan plan, String animalTag) {
		if (plan == null || plan.getFeedPlan() == null) {
			plan = new FeedPlan();
			plan.setFeedPlan(new ArrayList<FeedItem>());
			plan.setPlanAnalysisComments("The cohort " + cohort.getFeedCohortTypeCD() + " does not have any feed plan specified. Therefore, we can not determine a personalized feed for " + animalTag);
			return plan;
		} else {
			Iterator<FeedItem> it = plan.getFeedPlan().iterator();
			while (it.hasNext()) {
				FeedItem item = it.next();
				if (item.getFulFillmentTypeCD().equals(Util.FulfillmentType.ABSOLUTE)) {
					item.setPersonalizedFeedMessage("Give the animal " + item.getFulfillmentPct() + " " + item.getUnits() + " of " + item.getFeedItemCD());
				} else if (item.getFulFillmentTypeCD().equals(Util.FulfillmentType.FREEFLOW)) {
					item.setPersonalizedFeedMessage("Put " + item.getFeedItemCD() + " infront of the animal and let is consume "  + item.getFeedItemCD() + " freely.");
				} else if (item.getFulFillmentTypeCD().equals(Util.FulfillmentType.BODYWEIGHT)) {
					Float animalWeight = getLatestEventWeight(cohort.getOrgID(), animalTag);
					if (animalWeight == null) {
						item.setPersonalizedFeedMessage("In order to determine the personalized feed value for the animal, we need its weight. We could not find a valid weight event in the animal's record. Please weigh the animal and add the weight event to the animal record and try again");
					}
					else {
						item.setPersonalizedFeedMessage("Give the animal " + Util.formatToSpecifiedDecimalPlaces((float)(item.getFulfillmentPct()* animalWeight),1) + " " + item.getUnits() + " " + item.getFeedItemCD() + " (last measured weight of the animal was: " + animalWeight + " Kgs.)");
					}
				} else if (item.getFulFillmentTypeCD().equals(Util.FulfillmentType.MILKPROD)) {
					LocalDate startAverageCalculationFrom = LocalDate.now().minusDays(1);
					int numOfAdditionalDaysToAverage = 2;
					Float milkAverage = getMilkAverage(cohort.getOrgID(), animalTag,startAverageCalculationFrom, numOfAdditionalDaysToAverage);
					if (milkAverage == null) {
						item.setPersonalizedFeedMessage("In order to determine the " + item.getFeedItemCD() + " requirements of the animal, we need its daily milk record. We could not find the milk record. Please add the animal's milk record and try again");
					}
					else {
						item.setPersonalizedFeedMessage("Give the animal " + Util.formatToSpecifiedDecimalPlaces((float)(milkAverage/item.getFulfillmentPct()),1) + " " + item.getUnits() + " " + item.getFeedItemCD() + " (animal's last " + (numOfAdditionalDaysToAverage+1) + " days' milk average was: " + Util.formatToSpecifiedDecimalPlaces(milkAverage,1) + " Liters)");
					}
				} else {
					item.setPersonalizedFeedMessage("Unable to determine the usage of " + item.getFeedItemCD());
				}
			}
		}
		return plan;
	}

	private Float getMilkAverage(String orgID, String animalTag, LocalDate startAverageCalculationFrom, int numOfDaysToAverage) {
		MilkingDetailLoader loader = new MilkingDetailLoader();
		Float milkAverage = null;
		try {
			List<MilkingDetail> milkInfo = loader.retrieveFarmMilkVolumeForSpecifiedDateRangeForSpecificAnimal(orgID, animalTag, startAverageCalculationFrom.minusDays(numOfDaysToAverage), startAverageCalculationFrom, false);
			if (milkInfo != null) {
				Iterator<MilkingDetail> it = milkInfo.iterator();
				while (it.hasNext()) {
					if (milkAverage == null)
						milkAverage = new Float(it.next().getMilkVolume().floatValue());
					else
						milkAverage = new Float(milkAverage.floatValue() + it.next().getMilkVolume().floatValue());
				}
				if (milkAverage != null)
					milkAverage = new Float((float)milkAverage.floatValue() / (float)milkInfo.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//		IMDLogger.log(milkAverage.toString(), Util.INFO);
		return milkAverage;
	}

	private Float getLatestEventWeight(String orgID, String animalTag) {
		Animal animal;
		try {
			animal = new Animal(animalTag);
			animal.setOrgID(orgID);
			LifeCycleEventsLoader   eventLoader = new LifeCycleEventsLoader();
			animal.setLifeCycleEvents(eventLoader.retrieveAllLifeCycleEventsForAnimal(orgID, animalTag));
			LifecycleEvent weightEvent = getLatestEvent(animal, Util.LifeCycleEvents.WEIGHT);
			if (weightEvent == null || weightEvent.getAuxField1Value() == null || weightEvent.getAuxField1Value().isEmpty()) {
				IMDLogger.log("Could not find a weight event for the animal : " + 
								animalTag + ", would not be able to determine the personalized feed plan of this animal.", Util.ERROR);
				return null;
			} else if (weightEvent.getAuxField1Value() == null || weightEvent.getAuxField1Value().isEmpty()) {
				IMDLogger.log("Could not find a valid weight valus for the animal : " + 
						animalTag + ", would not be able to determine the personalized feed plan of this animal.", Util.ERROR);
				return null;
			} else {
				return Float.parseFloat(weightEvent.getAuxField1Value());
			}
		} catch (IMDException e) {
			e.printStackTrace();
			IMDLogger.log("Exception Occured in FeedManager.getLatestEventWeight(...) "  +  e.getMessage(),  Util.ERROR);
			return null;
		}		
	}

	private FeedCohort findAnimalFeedCohort(Animal animal) throws IMDException{
		String animalFeedCohortCD = null;
		String duplicateCheck = "";
		String animalFeedCohortDeterminatationMessage = null;
		String animalFeedCohortDeterminationCriteria = null;
		boolean isMale = animal.getClass().getSimpleName().equals(Sire.class.getSimpleName());
		DateTime now = DateTime.now();
		now = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),0,0);
		DateTime latestInseminationOrMatingEventTS = getLatestEventTimeStamp(animal,Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING);
		DateTime latestParturationEventTS = getLatestEventTimeStamp(animal,Util.LifeCycleEvents.PARTURATE,Util.LifeCycleEvents.ABORTION);
		int ageInDays = animal.getCurrentAgeInDays().getDays();
		if (isMale && ageInDays >= MATURITY_AGE_IN_DAYS) {
			animalFeedCohortCD = Util.FeedCohortType.BULL;
			duplicateCheck += animalFeedCohortCD + " ";
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is MALE and older than " + MATURITY_AGE_IN_DAYS + " days hence its feed cohort is " + animalFeedCohortCD;
			animalFeedCohortDeterminationCriteria = "Male Animal whose age is >= " + MATURITY_AGE_IN_DAYS;
		}
		if (isMale && ageInDays < MATURITY_AGE_IN_DAYS) {
			animalFeedCohortCD = Util.FeedCohortType.MALECALF;
			duplicateCheck += animalFeedCohortCD + " ";
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is MALE and younger than " + MATURITY_AGE_IN_DAYS + " days hence its feed cohort is " + animalFeedCohortCD;
			animalFeedCohortDeterminationCriteria = "Male Animal whose age is < " + MATURITY_AGE_IN_DAYS;
		}
		if (animal.getAnimalTypeCD().equals(Util.AnimalTypes.FEMALECALF) && isWeanedOff(animal) && ageInDays < HEIFER_MIN_AGE_IN_DAYS) {
			animalFeedCohortCD = Util.FeedCohortType.FMLWNDOFF;
			duplicateCheck += animalFeedCohortCD + " ";
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is FEMALEWEANEDOFF and has been weaned off. Its feed cohort is " + animalFeedCohortCD;
			animalFeedCohortDeterminationCriteria = Util.AnimalTypes.FEMALECALF + " whose age is < " + HEIFER_MIN_AGE_IN_DAYS + " and has a weaned off event in its lifecycle events history";
		}
		if (animal.getAnimalTypeCD().equals(Util.AnimalTypes.FEMALECALF) && isWeanedOff(animal) && ageInDays >= HEIFER_MIN_AGE_IN_DAYS) {
			animalFeedCohortCD = Util.FeedCohortType.HEIFER;
			duplicateCheck += animalFeedCohortCD + " ";
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is HEIFER and has been weaned off. Its feed cohort is " + animalFeedCohortCD + ". You MUST change its animal type to " + Util.AnimalTypes.HEIFER;
			animalFeedCohortDeterminationCriteria = Util.AnimalTypes.FEMALECALF + " whose age is >= " + HEIFER_MIN_AGE_IN_DAYS + " and has a weaned off event in its lifecycle events history. You MUST change its animal type to " + Util.AnimalTypes.HEIFER;
		}
		if (animal.getAnimalTypeCD().equals(Util.AnimalTypes.FEMALECALF) && !isWeanedOff(animal) && ageInDays < HEIFER_MIN_AGE_IN_DAYS) {
			animalFeedCohortCD = Util.FeedCohortType.FEMALECALF;
			duplicateCheck += animalFeedCohortCD + " ";
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is FEMALECALF and has NOT been weaned off. Its feed cohort is " + animalFeedCohortCD;
			animalFeedCohortDeterminationCriteria = Util.AnimalTypes.FEMALECALF + " whose age is < " + HEIFER_MIN_AGE_IN_DAYS + " and does NOT have a weaned off event in its lifecycle events history";
		}
		if (animal.isHeifer() && ageInDays >= HEIFER_MIN_AGE_IN_DAYS && !animal.isPregnant()) {
			animalFeedCohortCD = Util.FeedCohortType.HEIFER;
			duplicateCheck += animalFeedCohortCD + " ";
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is HEIFER which is not pregnant and is " + ageInDays + " days old";
			animalFeedCohortDeterminationCriteria = "Animal whose type indicates that it is a HEIFER whose age is >= " 
			+ HEIFER_MIN_AGE_IN_DAYS + " and is NOT pregnant";
		}
		if (animal.isHeifer() && ageInDays >= HEIFER_MIN_AGE_IN_DAYS && 
				animal.isPregnant() && 
				latestInseminationOrMatingEventTS != null && 
			    now.isBefore(latestInseminationOrMatingEventTS.plusDays(PREGNANCY_DURATION_DAYS - NEAR_PARTURATION_THRESHOLD_DAYS))) {
			animalFeedCohortCD = Util.FeedCohortType.PREGHFR;
			duplicateCheck += animalFeedCohortCD + " ";
			int daysToParturate = Util.getDaysBetween(latestInseminationOrMatingEventTS.plusDays(PREGNANCY_DURATION_DAYS),now);
			if (daysToParturate < 0)
				animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is " + animalFeedCohortCD + ", is pregnant and its calving is " + (-1 * daysToParturate)+ " days over due. It will calve any moment now, In Sha Allah";
			else if (daysToParturate == 0)
				animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is " + animalFeedCohortCD + ", is pregnant and its calving is expected to be today, In Sha Allah.";
			else 
				animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is " + animalFeedCohortCD + ", is pregnant and is going to calve after " + daysToParturate + " days, In Sha Allah";
			animalFeedCohortDeterminationCriteria = "Animal whose type indicates that it is a HEIFER, whose age is >= " 
			+ HEIFER_MIN_AGE_IN_DAYS + " days, is pregnant and has an insemination event that is earlier than " + 
			(PREGNANCY_DURATION_DAYS - NEAR_PARTURATION_THRESHOLD_DAYS) + " days i.e. its calving is not anytime soon";
		}
		if (animal.isHeifer() && ageInDays >= HEIFER_MIN_AGE_IN_DAYS && animal.isPregnant()  && 
				   latestInseminationOrMatingEventTS != null && 
				   !now.isBefore(latestInseminationOrMatingEventTS.plusDays(PREGNANCY_DURATION_DAYS - NEAR_PARTURATION_THRESHOLD_DAYS))) {
			animalFeedCohortCD = Util.FeedCohortType.HFRCLOSEUP;
			duplicateCheck += animalFeedCohortCD + " ";
			int daysToParturate = Util.getDaysBetween(latestInseminationOrMatingEventTS.plusDays(PREGNANCY_DURATION_DAYS),now);
			if (daysToParturate < 0)
				animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is " + animalFeedCohortCD + ", is pregnant and its calving is " + (-1 * daysToParturate)+ " days over due. It will calve any moment now, In Sha Allah";
			else if (daysToParturate == 0)
				animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is " + animalFeedCohortCD + ", is pregnant and its calving is expected to be today, In Sha Allah.";
			else 
				animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is "  + animalFeedCohortCD + " that is going to calve after " + Util.getDaysBetween(latestInseminationOrMatingEventTS.plusDays(PREGNANCY_DURATION_DAYS),now) + " days, In Sha Allah";
			animalFeedCohortDeterminationCriteria = "Animal whose type indicates that it is a HEIFER whose age is >= " 
			+ HEIFER_MIN_AGE_IN_DAYS + " is pregnant has an insemination event that is older than " +
			(PREGNANCY_DURATION_DAYS - NEAR_PARTURATION_THRESHOLD_DAYS) + " days";
		}
		if (animal.isLactating()  && 
				latestParturationEventTS != null && now.isBefore(latestParturationEventTS.plusDays(RECENT_PARTURATION_DAYS_LIMIT))){
			animalFeedCohortCD = Util.FeedCohortType.LCTEARLY;
			duplicateCheck += animalFeedCohortCD + " ";
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is " + animalFeedCohortCD + ". It calved " + Util.getDaysBetween(DateTime.now(), latestParturationEventTS)+ " days ago";
			animalFeedCohortDeterminationCriteria = "Animal whose type indicates that it is LACTATING and it parturated/aborted with-in the last " 
			+ RECENT_PARTURATION_DAYS_LIMIT + " days";
		}
		if (animal.isLactating()  && 
				   latestParturationEventTS != null && 
				   now.isAfter(latestParturationEventTS.plusDays(MID_PARTURATION_DAYS_START)) && 
				   now.isBefore(latestParturationEventTS.plusDays(MID_PARTURATION_DAYS_END))){
			animalFeedCohortCD = Util.FeedCohortType.LCTMID;
			duplicateCheck += animalFeedCohortCD + " ";
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is "  + animalFeedCohortCD + ". It calved " + Util.getDaysBetween(DateTime.now(), latestParturationEventTS)+ " days ago";
			animalFeedCohortDeterminationCriteria = "Animal whose type indicates that it is LACTATING and it parturated/aborted with-in the last " 
					+ MID_PARTURATION_DAYS_START + " to " + MID_PARTURATION_DAYS_END + " days";
		} 
		if (animal.isLactating()  && 
				   latestParturationEventTS != null && 
				   !now.isBefore(latestParturationEventTS.plusDays(MID_PARTURATION_DAYS_END))){
			animalFeedCohortCD = Util.FeedCohortType.LCTOLD;
			duplicateCheck += animalFeedCohortCD + " ";
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is "  + animalFeedCohortCD + ". It calved " + Util.getDaysBetween(DateTime.now(), latestParturationEventTS)+ " days ago";
			animalFeedCohortDeterminationCriteria = "Animal whose type indicates that it is " + animalFeedCohortCD + " and it parturated/aborted more than " + MID_PARTURATION_DAYS_END + " days ago";
		} 
		if (!animal.isHeifer() && animal.isDry()  && animal.isPregnant() &&
				   latestInseminationOrMatingEventTS != null && 
				   !now.isBefore(latestInseminationOrMatingEventTS.plusDays(PREGNANCY_DURATION_DAYS - NEAR_PARTURATION_THRESHOLD_DAYS))){
			animalFeedCohortCD = Util.FeedCohortType.NEARPRTRT;
			duplicateCheck += animalFeedCohortCD + " ";
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is " + animalFeedCohortCD + ". It is expected to calve after " +  Util.getDaysBetween(latestInseminationOrMatingEventTS.plusDays(PREGNANCY_DURATION_DAYS),now) + " days, In Sha Allah";
			animalFeedCohortDeterminationCriteria = "Animal whose type indicates that it is Dry and Pregnant and it will calve with-in next " + NEAR_PARTURATION_THRESHOLD_DAYS + " days";			
		} 
		if (!animal.isHeifer() && animal.isDry() && animal.isPregnant() &&
				   latestInseminationOrMatingEventTS != null && 
				   now.isBefore(latestInseminationOrMatingEventTS.plusDays(PREGNANCY_DURATION_DAYS - NEAR_PARTURATION_THRESHOLD_DAYS)) /*&&
				   !now.isBefore(latestInseminationOrMatingEventTS.plusDays(DRYOFF_BY_DAYS))*/){
			animalFeedCohortCD = Util.FeedCohortType.FARPRTRT;
			duplicateCheck += animalFeedCohortCD + " ";
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is " + animalFeedCohortCD + ". It is expected to calve after " + Util.getDaysBetween(latestInseminationOrMatingEventTS.plusDays(PREGNANCY_DURATION_DAYS),now) + " days, In Sha Allah";
			animalFeedCohortDeterminationCriteria = "Animal whose type indicates that it is Dry and Pregnant and it will calve after more than " + NEAR_PARTURATION_THRESHOLD_DAYS + " days";			
		}
		
		// remaining cohorts: high yielder lactating early, high yielder lactating mid, high yielder lactation far, lactating dry
		FeedCohort cohort = null;
		
		if (animalFeedCohortCD == null) {
			cohort = new FeedCohort(animal.getOrgID(),"UNDETERMINED", "");
			cohort.setAnimalFeedCohortDeterminatationMessage("The animal " + animal.getAnimalTag() + " could not be mapped to any Feed Cohort.");
			cohort.setFeedCohortDeterminationCriteria("None of the determination criteria matches the profle of this animal");
		} else {
			if (duplicateCheck.trim().equals(animalFeedCohortCD)) {
				cohort = new FeedCohort(animal.getOrgID(),animalFeedCohortCD, "");
				cohort.setAnimalFeedCohortDeterminatationMessage(animalFeedCohortDeterminatationMessage);
				cohort.setFeedCohortDeterminationCriteria(animalFeedCohortDeterminationCriteria);
			} else {
				// multiple rules were fired by this animal.
				cohort = new FeedCohort(animal.getOrgID(),animalFeedCohortCD, "");
				cohort.setAnimalFeedCohortDeterminatationMessage(animalFeedCohortDeterminatationMessage + ". ⛔️Please note that this animal was found to belong to multiple feed cohorts [" + duplicateCheck.trim() +  "]. This is an ERROR; caused either because of data issues or a programming issue. Please submit a bug report");
				cohort.setFeedCohortDeterminationCriteria(animalFeedCohortDeterminationCriteria);
			}
		}
		//IMDLogger.log("\n" + cohort.dtoToJson(""), Util.INFO);
		return cohort;
	}

	private LifecycleEvent getLatestEvent(Animal animal, String targetEventType) {
		return getLatestEvent(animal,targetEventType,null);
	}
	
	
	private LifecycleEvent getLatestEvent(Animal animal, String targetEventType1, String targetEventType2) {
		List<LifecycleEvent> sortedEvents = animal.getLifeCycleEvents();
		// events are assumed to be sorted by date with latest on top and oldest at bottom.
		if (sortedEvents == null || sortedEvents.isEmpty())
			return null;
		else {
			Iterator<LifecycleEvent> it = sortedEvents.iterator();
			while (it.hasNext()) {
				LifecycleEvent event = it.next();
				if (event.isEventOfSpecifiedType(targetEventType1) || event.isEventOfSpecifiedType(targetEventType2))
					return event;
			}
		}			
		return null;
	}	
	
	private DateTime getLatestEventTimeStamp(Animal animal, String targetEventType1, String targetEventType2) {
		LifecycleEvent event = getLatestEvent(animal,targetEventType1, targetEventType2);
		if (event != null) {
			return event.getEventTimeStamp();
		}
		else
			return null;
	}
	private DateTime getLatestEventTimeStamp(Animal animal, String targetEventType) {
		return getLatestEventTimeStamp(animal,targetEventType,null);
	}

	private boolean isWeanedOff(Animal animal) throws IMDException{
		List<LifecycleEvent> events = animal.getLifeCycleEvents();
		boolean weanedOff = false;
		if (events == null || events.isEmpty()) {
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
