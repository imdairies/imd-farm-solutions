package com.imd.controller.feed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.imd.dto.Animal;
import com.imd.dto.CohortNutritionalNeeds;
import com.imd.dto.FeedCohort;
import com.imd.dto.FeedItem;
import com.imd.dto.FeedItemNutritionalStats;
import com.imd.dto.FeedPlan;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.LookupValues;
import com.imd.dto.MilkingDetail;
import com.imd.dto.Sire;
import com.imd.loader.AnimalLoader;
import com.imd.loader.FeedLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.LookupValuesLoader;
import com.imd.loader.MilkingDetailLoader;
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
			animal.setFeedCohortInformation(getAnimalFeedCohort(animal.getOrgID(), animal.getAnimalTag()));
			animal.setAnimalNutritionalNeeds(this.getFeedCohortNutritionalNeeds(animal.getFeedCohortInformation(),animal.getAnimalTag()));
//			IMDLogger.log(animal.getFeedCohortInformation().toString(), Util.INFO);
		}		
		return farmActiveHerd;
	}

	private CohortNutritionalNeeds getFeedCohortNutritionalNeeds(FeedCohort feedCohort, String animalTag) {
		FeedLoader feedLoader = new FeedLoader();
		List<CohortNutritionalNeeds> needs = feedLoader.retrieveCohortNutritionalNeeds(feedCohort, 0f, 9999f);
		if (needs == null || needs.isEmpty()) {
			IMDLogger.log("Nutritional Needs for the animal " + animalTag + " could not be found. This indicates that nutritioanl needs for the cohort " + feedCohort.getFeedCohortLookupValue().getLookupValueCode() + " has not been configured", Util.ERROR);
			return null;
		} else if (needs.size() >1) {
			IMDLogger.log(needs.size() + " nutritional needs records were found for the animal " + animalTag + " belonging to the cohort " + feedCohort.getFeedCohortLookupValue().getLookupValueCode() + ". We will use the record with the smallest start and end values.", Util.ERROR);
		} 
		return needs.get(0);
	}
	
	public FeedCohort getAnimalFeedCohort(String orgID, String animalTag) throws Exception {
		AnimalLoader animalLoader = new AnimalLoader();
		LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
		List<Animal> animals = animalLoader.getAnimalRawInfo(orgID, animalTag);		
		if (animals == null || animals.isEmpty()) {
			throw new IMDException("Animal not found [" + orgID + "-" + animalTag + "]");
		}
		if (animals.size() > 1) {
			throw new IMDException("Multiple animals found for the ORG-TAG [" + orgID + "-" + animalTag + "]. This indicates that the data has been corrupted. Please remove the duplicate record.");			
		}
		Animal animal = animals.get(0);
		animal.setLifeCycleEvents(eventLoader.retrieveAllLifeCycleEventsForAnimal(orgID, animalTag));
		return findAnimalFeedCohort(animal);
	}
	
	public FeedPlan getPersonalizedFeedPlan(FeedCohort cohort, FeedPlan cohortFeedPlan, Animal animal) throws Exception {
		return getPersonalizedPlanOfAnAnimal(animal, cohortFeedPlan);
	}
	public FeedPlan getPersonalizedFeedPlan(FeedCohort cohort, Animal animal) throws Exception {
		return getPersonalizedPlanOfAnAnimal(animal, null);
	}
	


	public FeedPlan getPersonalizedPlanOfAnAnimal(Animal animal, FeedPlan cohortFeedPlan) throws Exception {
		FeedPlan personalizePlan = new FeedPlan();
		FeedLoader feedLoader = new FeedLoader();
		FeedCohort animalFeedCohort = animal.getFeedCohortInformation();
		if (animalFeedCohort == null || animalFeedCohort.getCohortNutritionalNeeds() == null) {
			animalFeedCohort = this.getAnimalFeedCohort(animal.getOrgID(), animal.getAnimalTag());
			if (animalFeedCohort == null) {
				personalizePlan.setPlanAnalysisComments("Could not determine the feed cohort of " + animal.getAnimalTag() + " probably because of some missing configurations. It's personalized feed plan can not be determined.");
				return personalizePlan;
			}
			animal.setFeedCohortInformation(animalFeedCohort);
		}
		// ... by now we would have figured out the animal's feedcohort and the nutritional needs of that feed cohort.

		// A lot of our calculations depend on the animal's weight.
		Float animalWeight = animal.getWeight();
		if (animalWeight == null) {
			animalWeight = this.getLatestEventWeight(animal.getOrgID(), animal.getAnimalTag());
			if (animalWeight == null) {
				// assume a weight based on the animal's age.
				personalizePlan.setPlanAnalysisComments(animal.getAnimalTag() + " has never been weighed. Animal must have a weight event before we can determine its personalized feed plan");
				return personalizePlan;
			}
			animal.setWeight(animalWeight);
		}
		//... now we have the cohort and the weight.
		
		// Now we need to figure out the specific nutritional needs of THIS particular animal.
		CohortNutritionalNeeds animalNeeds = this.getAnimalNutritionalNeeds(animal);
		animal.setAnimalNutritionalNeeds(animalNeeds);
		
		String feedCohortCD =  animal.getFeedCohortInformation().getFeedCohortLookupValue().getLookupValueCode();
		// Now we get the feed plan with which we wish to fulfill the cohort needs.
		if (cohortFeedPlan == null)
			cohortFeedPlan = feedLoader.retrieveFeedPlan(animal.getOrgID(), feedCohortCD);
		// Finally we get to determining the feed plan for THIS animal with which we will meet THIS animal's nutritional needs.
		personalizePlan = this.getAnimalPersonalizePlanFromCohortFeedPlan(cohortFeedPlan, animal);
		return personalizePlan;
	}	
	
	private CohortNutritionalNeeds getAnimalNutritionalNeeds(Animal animal) {
		CohortNutritionalNeeds animalNeeds = new CohortNutritionalNeeds();
		Float dm = animal.getFeedCohortInformation().getCohortNutritionalNeeds().getDryMatter();
		Float cp = animal.getFeedCohortInformation().getCohortNutritionalNeeds().getCrudeProtein();
		Float me = animal.getFeedCohortInformation().getCohortNutritionalNeeds().getMetabloizableEnergy();
		Float animalDM = dm * animal.getWeight();
		Float animalCP = animalDM * cp;
		Float animalME = me;// ME measured in daily ME requirements
		animalNeeds.setDryMatter(animalDM);
		animalNeeds.setCrudeProtein(animalCP);
		animalNeeds.setMetabloizableEnergy(animalME);
		return animalNeeds;
	}

	private FeedPlan getAnimalPersonalizePlanFromCohortFeedPlan(FeedPlan cohortFeedPlan, Animal animal) {
		FeedPlan animalSpecificFeedPlan = new FeedPlan();
		animalSpecificFeedPlan.setFeedPlan(new ArrayList<FeedItem>());
		Float animalWeight = animal.getWeight();
		Iterator<FeedItem> it = cohortFeedPlan.getFeedPlan().iterator();
		while (it.hasNext()) {
			FeedItem feedItem = it.next();
			String fulFillmentTypeCD = feedItem.getFulFillmentTypeCD();
			if (fulFillmentTypeCD.equals(Util.FulfillmentType.ABSOLUTE)) {
				Float dailyIntake = feedItem.getFulfillmentPct();
				FeedItemNutritionalStats itemStats = feedItem.getFeedItemNutritionalStats();
				Float dm = itemStats.getDryMatter() * dailyIntake;
				Float cp = itemStats.getCrudeProtein() * dm;
				Float me = itemStats.getMetabolizableEnergy() * dm;
				feedItem.setDailyIntake(dailyIntake);				
				feedItem.getFeedItemNutritionalStats().setDryMatter(dm);
				feedItem.getFeedItemNutritionalStats().setCrudeProtein(cp);
				feedItem.getFeedItemNutritionalStats().setMetabolizableEnergy(me);

				feedItem.setPersonalizedFeedMessage("Give the animal " + dailyIntake + " " + feedItem.getUnits() + " of " + feedItem.getFeedItemLookupValue().getShortDescription());
				
				animalSpecificFeedPlan.getFeedPlan().add(feedItem);
				animalSpecificFeedPlan = updatePlanNutritionalStats(animalSpecificFeedPlan, dm, cp, me);
				
			} else if (fulFillmentTypeCD.equals(Util.FulfillmentType.FREEFLOW)) {
				feedItem.setPersonalizedFeedMessage("Put " + feedItem.getFeedItemLookupValue().getShortDescription() + " infront of the animal and let it consume "  + feedItem.getFeedItemLookupValue().getShortDescription() + " freely.");
				Float dm = 0f;
				Float cp = 0f * dm;
				Float me = 0f * dm;
				feedItem.setDailyIntake(0f);				
				feedItem.getFeedItemNutritionalStats().setDryMatter(dm);
				feedItem.getFeedItemNutritionalStats().setCrudeProtein(cp);
				feedItem.getFeedItemNutritionalStats().setMetabolizableEnergy(me);
				animalSpecificFeedPlan.getFeedPlan().add(feedItem);
				animalSpecificFeedPlan = updatePlanNutritionalStats(animalSpecificFeedPlan, dm, cp, me);
			} else if (fulFillmentTypeCD.equals(Util.FulfillmentType.BODYWEIGHT)) {
				Float dailyIntake = (float)(feedItem.getFulfillmentPct() * animalWeight);
				FeedItemNutritionalStats itemStats = feedItem.getFeedItemNutritionalStats();
				Float dm = (itemStats.getDryMatter()==Util.FulfillmentType.NO_DM_MEASUREONVOLUME ? 1f:itemStats.getDryMatter()) * dailyIntake;
				Float cp = itemStats.getCrudeProtein() * dm;
				Float me = itemStats.getMetabolizableEnergy() * dm;
				feedItem.setDailyIntake(dailyIntake);				
				feedItem.getFeedItemNutritionalStats().setDryMatter(itemStats.getDryMatter()==Util.FulfillmentType.NO_DM_MEASUREONVOLUME ? 0f:dm);
				feedItem.getFeedItemNutritionalStats().setCrudeProtein(cp);
				feedItem.getFeedItemNutritionalStats().setMetabolizableEnergy(me);

				feedItem.setPersonalizedFeedMessage("Give the animal " + Util.formatToSpecifiedDecimalPlaces(dailyIntake,2) + " " + feedItem.getUnits() + " " + feedItem.getFeedItemLookupValue().getShortDescription() + " (last measured weight of the animal was: " + animalWeight + " Kgs.)");

				animalSpecificFeedPlan.getFeedPlan().add(feedItem);
				animalSpecificFeedPlan = updatePlanNutritionalStats(animalSpecificFeedPlan, feedItem.getFeedItemNutritionalStats().getDryMatter(), cp, me);
				
			} else if (fulFillmentTypeCD.equals(Util.FulfillmentType.BYDMREQPCT)) {
				// intake to be determined by the specified pecentage of Dry Matter needs of the animal.
				// for example if DM need of the animal is 3.0% of body weight. Assume the body weight is
				// 600 Kgs. Assume that the DM of this feeditem is 20%. Then a 0.50 value of feedItem.getFulfillmentPct()
				// will mean that the daily intake of this feeditem will be = (600 * 30% * 0.50) / 20% = 45 Kgs
				FeedItemNutritionalStats itemStats = feedItem.getFeedItemNutritionalStats();
				Float feedItemDM = itemStats.getDryMatter();
				Float cohortDMNeeds = animal.getFeedCohortInformation().getCohortNutritionalNeeds().getDryMatter();
				Float animalDMNeeds = animalWeight * cohortDMNeeds;
				Float animalDMNeedsToBeFilledByThisItem = animalDMNeeds * feedItem.getFulfillmentPct();
				Float dailyIntake = animalDMNeedsToBeFilledByThisItem / feedItemDM;
				Float cp = itemStats.getCrudeProtein() * animalDMNeedsToBeFilledByThisItem;
				Float me = itemStats.getMetabolizableEnergy() * animalDMNeedsToBeFilledByThisItem;

				feedItem.setDailyIntake(dailyIntake);				
				feedItem.getFeedItemNutritionalStats().setDryMatter(animalDMNeedsToBeFilledByThisItem);
				feedItem.getFeedItemNutritionalStats().setCrudeProtein(cp);
				feedItem.getFeedItemNutritionalStats().setMetabolizableEnergy(me);

				feedItem.setPersonalizedFeedMessage("Give the animal " + dailyIntake + " " + feedItem.getUnits() + " of " + feedItem.getFeedItemLookupValue().getShortDescription());
				
				animalSpecificFeedPlan.getFeedPlan().add(feedItem);
				animalSpecificFeedPlan = updatePlanNutritionalStats(animalSpecificFeedPlan, animalDMNeedsToBeFilledByThisItem, cp, me);
			} else if (fulFillmentTypeCD.equals(Util.FulfillmentType.MILKPROD)) {
				LocalDate startAverageCalculationFrom = LocalDate.now().minusDays(1);
				int numOfAdditionalDaysToAverage = 2;
				Float dailyIntake = null;
				Float milkAverage = getMilkAverage(animal.getOrgID(), animal.getAnimalTag(),startAverageCalculationFrom, numOfAdditionalDaysToAverage);
				if (milkAverage == null) {
					feedItem.setPersonalizedFeedMessage("In order to determine the " + feedItem.getFeedItemLookupValue().getShortDescription() + " requirements of the animal, we need its daily milk record. We could not find the milk record. Please add the animal's milk record and try again");
					dailyIntake = 0f;
					milkAverage = 0f;
				}
				else {
					feedItem.setPersonalizedFeedMessage("Give the animal " + Util.formatToSpecifiedDecimalPlaces((float)(milkAverage/feedItem.getFulfillmentPct()),1) + " " + feedItem.getUnits() + " " + feedItem.getFeedItemLookupValue().getShortDescription() + " (animal's last " + (numOfAdditionalDaysToAverage+1) + " days' milk average was: " + Util.formatToSpecifiedDecimalPlaces(milkAverage,1) + " Liters)");
					dailyIntake = (float)(milkAverage/feedItem.getFulfillmentPct());
				}
				FeedItemNutritionalStats itemStats = feedItem.getFeedItemNutritionalStats();
				Float cp = itemStats.getCrudeProtein() * milkAverage;
				Float me = itemStats.getMetabolizableEnergy() * milkAverage;
				feedItem.setDailyIntake(dailyIntake);				
				feedItem.getFeedItemNutritionalStats().setDryMatter(0f);
				feedItem.getFeedItemNutritionalStats().setCrudeProtein(cp);
				feedItem.getFeedItemNutritionalStats().setMetabolizableEnergy(me);
				animalSpecificFeedPlan.getFeedPlan().add(feedItem);
				animalSpecificFeedPlan = updatePlanNutritionalStats(animalSpecificFeedPlan, 0f, cp, me);				
			} else {
				feedItem.setPersonalizedFeedMessage("Unable to determine the usage of " + feedItem.getFeedItemLookupValue());
				animalSpecificFeedPlan.getFeedPlan().add(feedItem);
			}
		}
		return animalSpecificFeedPlan;
	}

	private FeedPlan updatePlanNutritionalStats(FeedPlan animalSpecificFeedPlan, Float dm, Float cp, Float me) {
		if (animalSpecificFeedPlan.getPlanDM() == null)
			animalSpecificFeedPlan.setPlanDM(dm);
		else 
			animalSpecificFeedPlan.setPlanDM(animalSpecificFeedPlan.getPlanDM() + dm);
		
		if (animalSpecificFeedPlan.getPlanCP() == null)
			animalSpecificFeedPlan.setPlanCP(cp);
		else 
			animalSpecificFeedPlan.setPlanCP(animalSpecificFeedPlan.getPlanCP() + cp);
		
		if (animalSpecificFeedPlan.getPlanME() == null)
			animalSpecificFeedPlan.setPlanME(me);
		else 
			animalSpecificFeedPlan.setPlanME(animalSpecificFeedPlan.getPlanME() + me);
		return animalSpecificFeedPlan;
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
		if (!animal.isLactating()  && animal.getAnimalTypeCD().equalsIgnoreCase(Util.AnimalTypes.LCTPOSTPAR) &&
				latestParturationEventTS != null && now.isBefore(latestParturationEventTS.plusDays(RECENT_PARTURATION_DAYS_LIMIT))){
			animalFeedCohortCD = Util.FeedCohortType.LCTPOSTPAR;
			duplicateCheck += animalFeedCohortCD + " ";
			animalFeedCohortDeterminatationMessage = animal.getAnimalTag() + " is " + animalFeedCohortCD + ". It calved " + Util.getDaysBetween(DateTime.now(), latestParturationEventTS)+ " days ago. Its milk is not yet fit for general consumption.";
			animalFeedCohortDeterminationCriteria = "Animal whose type indicates that it is LCTPOSTPAR and it parturated/aborted with-in the last " 
			+ RECENT_PARTURATION_DAYS_LIMIT + " days";
		}
		
		// remaining cohorts: high yielder lactating early, high yielder lactating mid, high yielder lactation far, lactating dry
		FeedCohort cohort = null;
		LookupValues cohortLV = null;
		
		if (animalFeedCohortCD == null) {
			cohortLV = new LookupValues(Util.LookupValues.FEEDCOHORT, "UNDETERMINED", "UNDETERMINED", "");
			cohort = new FeedCohort(animal.getOrgID(),cohortLV, "");
			cohort.setAnimalFeedCohortDeterminatationMessage("The animal " + animal.getAnimalTag() + " could not be mapped to any Feed Cohort.");
			cohort.setFeedCohortDeterminationCriteria("None of the determination criteria matches the profle of this animal");
		} else {
			LookupValuesLoader lvLoader = new LookupValuesLoader();
			cohortLV = lvLoader.retrieveLookupValue(Util.LookupValues.FEEDCOHORT, animalFeedCohortCD);
			if (cohortLV == null)
				cohortLV = new LookupValues(Util.LookupValues.FEEDCOHORT, animalFeedCohortCD, animalFeedCohortCD, "");
			cohort = new FeedCohort(animal.getOrgID(),cohortLV, "");
			if (duplicateCheck.trim().equals(animalFeedCohortCD)) {
				cohort.setAnimalFeedCohortDeterminatationMessage(animalFeedCohortDeterminatationMessage);
				cohort.setFeedCohortDeterminationCriteria(animalFeedCohortDeterminationCriteria);
				cohort.setCohortNutritionalNeeds(getFeedCohortNutritionalNeeds(cohort, animal.getAnimalTag()));
			} else {
				// multiple rules were fired by this animal.
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

	public FeedPlan getPersonalizedFeedPlan(FeedCohort feedCohortType, String animalTag) throws Exception {
		AnimalLoader anmlLoader = new AnimalLoader();
		Animal animal = anmlLoader.getAnimalRawInfo(feedCohortType.getOrgID(), animalTag).get(0);		
		return getPersonalizedFeedPlan(feedCohortType,animal);
	}
}
