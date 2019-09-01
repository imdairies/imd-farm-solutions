package com.imd.controller.feed;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.imd.util.Util.NutritionalStats;

public class FeedManager {
	
	private static final float MIN_FULFILLMENT_VALUE = -9999f;
	private static final float MAX_FULFILLMENT_VALUE = 9999f;
	public static final int MATURITY_AGE_IN_DAYS = 270;
	public static final int HEIFER_MIN_AGE_IN_DAYS = 180;
	public static final int DRYOFF_BY_DAYS = 210;
	public static final int RECENT_PARTURATION_DAYS_LIMIT = 90;
	public static final int MID_PARTURATION_DAYS_START = 90;
	public static final int MID_PARTURATION_DAYS_END = 180;
	public static final int PREGNANCY_DURATION_DAYS = 270;
	public static final int NEAR_PARTURATION_THRESHOLD_DAYS = 15;

	public List<Animal> getFeedCohortInformationForFarmActiveAnimals(String orgID) throws Exception {
		AnimalLoader aLoader = new AnimalLoader();
		List<Animal> farmActiveHerd = aLoader.retrieveActiveAnimals(orgID);
		if (farmActiveHerd == null || farmActiveHerd.isEmpty())
			throw new IMDException("The farm does not seem to have any active animal");
		Iterator<Animal> it = farmActiveHerd.iterator();
		while (it.hasNext()) {
			Animal animal = it.next();
			animal.setWeight(getLatestEventWeight(animal.getOrgID(), animal.getAnimalTag()));
			animal.setFeedCohortInformation(getAnimalFeedCohort(animal.getOrgID(), animal.getAnimalTag()));
			animal.getFeedCohortInformation().setCohortNutritionalNeeds(this.getFeedCohortNutritionalNeeds(animal.getFeedCohortInformation(),animal));
			animal.setAnimalNutritionalNeeds(getAnimalNutritionalNeeds(animal));
		}
		return farmActiveHerd;
	}

	private CohortNutritionalNeeds getFeedCohortNutritionalNeeds(FeedCohort feedCohort, Animal animal) {
		FeedLoader feedLoader = new FeedLoader();
		CohortNutritionalNeeds cohortNeed = null;
		List<CohortNutritionalNeeds> needs = null;
		
		needs = feedLoader.retrieveCohortNutritionalNeeds(feedCohort, 0f, 9999f);
		if (needs == null || needs.isEmpty()) {
			IMDLogger.log("Nutritional Needs for the animal " + animal.getAnimalTag() + " could not be found. This indicates that nutritioanl needs for the cohort " + feedCohort.getFeedCohortLookupValue().getLookupValueCode() + " has not been configured", Util.ERROR);
			cohortNeed =  null;
		} else {
			if (needs.size() >1) {
				IMDLogger.log(needs.size() + " nutritional needs records were found for the animal " + animal.getAnimalTag() + " belonging to the cohort " + feedCohort.getFeedCohortLookupValue().getLookupValueCode() + ". We will use the record with the smallest start and end values.", Util.ERROR);
			}
			cohortNeed = needs.get(0);
		}
		if (cohortNeed != null && 
				(feedCohort.getFeedCohortLookupValue().getLookupValueCode().equalsIgnoreCase(Util.FeedCohortType.LCTEARLY) || 
					feedCohort.getFeedCohortLookupValue().getLookupValueCode().equalsIgnoreCase(Util.FeedCohortType.LCTMID) ||
					feedCohort.getFeedCohortLookupValue().getLookupValueCode().equalsIgnoreCase(Util.FeedCohortType.LCTOLD) 
				)) {
			Float wt = animal.getWeight();
			if (wt == null) {
				IMDLogger.log("Animal " + animal.getAnimalTag() + " does not have any weight specified. We will assume the weight to be " + Util.DefaultValues.ADULT_COW_WEIGHT + " in our getMetabolizableEnergyRequiremnt calculations", Util.ERROR);
				wt = Util.DefaultValues.ADULT_COW_WEIGHT.floatValue();
			}
			Double threeDaysMilkingAverage = this.getMilkAverage(animal.getOrgID(), animal.getAnimalTag(), LocalDate.now(), 3).doubleValue();
			Integer daysIntoPregnancy = null;
			LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
			if (animal.isPregnant()) {
				List<LifecycleEvent> evts = loader.retrieveSpecificLifeCycleEventsForAnimal(animal.getOrgID(), 
						animal.getAnimalTag(), LocalDate.now().minusDays(270),null,
						Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING,null,null,null,null);
				if (evts != null && !evts.isEmpty()) {
					if (evts.get(0).getEventType().getEventCode().equals(Util.LifeCycleEvents.INSEMINATE) && 
							evts.get(0).getAuxField3Value().equals(Util.YES)) {
						daysIntoPregnancy = Util.getDaysBetween(evts.get(0).getEventTimeStamp(),DateTime.now());
					} else if (evts.get(0).getEventType().getEventCode().equals(Util.LifeCycleEvents.MATING) && 
							evts.get(0).getAuxField2Value().equals(Util.YES)) {
						daysIntoPregnancy = Util.getDaysBetween(evts.get(0).getEventTimeStamp(),DateTime.now());
					}
				}
				if (daysIntoPregnancy == null)
					IMDLogger.log("Animal " + animal.getAnimalTag() + " has been marked pregnant but its last insemination/mating was not marked as successful. We will assume that the animal is NOT pregnant in our getMetabolizableEnergyRequiremnt calculations", Util.ERROR);
			}
			CohortNutritionalNeeds calculation = this.getMetabolizableEnergyRequiremnt(new Double(wt), threeDaysMilkingAverage, daysIntoPregnancy, feedCohort.getFeedCohortLookupValue().getLookupValueCode(), null, null);
			cohortNeed.setMetabloizableEnergy(calculation.getMetabloizableEnergy());
			cohortNeed.setNutritionalNeedsTDN(calculation.getNutritionalNeedsTDN());
//			cohortNeed.setDryMatter(cohortNeed.getDryMatter() * wt);
//			cohortNeed.setCrudeProtein(cohortNeed.getCrudeProtein() * cohortNeed.getDryMatter());
		}
//		else if (feedCohort.getFeedCohortLookupValue().getLookupValueCode().equalsIgnoreCase(Util.FeedCohortType.LCTMID))
//		;
//		else if (feedCohort.getFeedCohortLookupValue().getLookupValueCode().equalsIgnoreCase(Util.FeedCohortType.LCTOLD))
//		;
		return cohortNeed;
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
		animal.setWeight(this.getLatestEventWeight(animal.getOrgID(), animal.getAnimalTag()));
		animal.setLifeCycleEvents(eventLoader.retrieveAllLifeCycleEventsForAnimal(orgID, animalTag));
		return findAnimalFeedCohort(animal);
	}
	
	public FeedPlan getPersonalizedFeedPlan(FeedCohort cohort, FeedPlan cohortFeedPlan, Animal animal) throws Exception {
		return getPersonalizedPlanOfAnAnimal(animal, cohortFeedPlan);
	}
	public FeedPlan getPersonalizedFeedPlan(FeedCohort cohort, Animal animal) throws Exception {
		return getPersonalizedPlanOfAnAnimal(animal, null);
	}
	public FeedPlan getPersonalizedFeedPlan(Animal animal) throws Exception {
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
		if (animal.getAnimalNutritionalNeeds() != null) {
			CohortNutritionalNeeds animalNeeds = this.getAnimalNutritionalNeeds(animal);
			animal.setAnimalNutritionalNeeds(animalNeeds);
		}
		
		String feedCohortCD =  animal.getFeedCohortInformation().getFeedCohortLookupValue().getLookupValueCode();
		// Now we get the feed plan with which we wish to fulfill the cohort needs.
		if (cohortFeedPlan == null)
			cohortFeedPlan = feedLoader.retrieveFeedPlan(animal.getOrgID(), feedCohortCD);
		// Finally we get to determining the feed plan for THIS animal with which we will meet THIS animal's nutritional needs.
		personalizePlan = this.getAnimalPersonalizePlanFromCohortFeedPlan(cohortFeedPlan, animal);
		return personalizePlan;
	}	
	
	public CohortNutritionalNeeds getAnimalNutritionalNeeds(Animal animal) {
		CohortNutritionalNeeds animalNeeds = new CohortNutritionalNeeds();
		Float dm = animal.getFeedCohortInformation().getCohortNutritionalNeeds().getDryMatter();
		Float cp = animal.getFeedCohortInformation().getCohortNutritionalNeeds().getCrudeProtein();
		Float me = animal.getFeedCohortInformation().getCohortNutritionalNeeds().getMetabloizableEnergy();
		Float animalDM = dm * (animal.getWeight() == null ? 0 : animal.getWeight());
		Float animalCP = animalDM * cp;
		Float animalME = me;// ME measured in daily ME requirements
		animalNeeds.setDryMatter(animalDM);
		animalNeeds.setCrudeProtein(animalCP);
		animalNeeds.setMetabloizableEnergy(animalME);
		animalNeeds.setFeedCohortCD(animal.getFeedCohortInformation().getFeedCohortLookupValue().getLookupValueCode());
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
				Float dailyIntake = getAllowedDailyIntakeValue(feedItem,null);
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
				feedItem.setDailyIntake(getAllowedDailyIntakeValue(feedItem,0f));
				feedItem.getFeedItemNutritionalStats().setDryMatter(dm);
				feedItem.getFeedItemNutritionalStats().setCrudeProtein(cp);
				feedItem.getFeedItemNutritionalStats().setMetabolizableEnergy(me);
				animalSpecificFeedPlan.getFeedPlan().add(feedItem);
				animalSpecificFeedPlan = updatePlanNutritionalStats(animalSpecificFeedPlan, dm, cp, me);
			} else if (fulFillmentTypeCD.equals(Util.FulfillmentType.BODYWEIGHT)) {
				Float dailyIntake = (float)(feedItem.getFulfillmentPct() * animalWeight);
				dailyIntake = getAllowedDailyIntakeValue(feedItem,dailyIntake);

				FeedItemNutritionalStats itemStats = feedItem.getFeedItemNutritionalStats();
				Float dm = (itemStats.getDryMatter().floatValue() == Util.FulfillmentType.NO_DM_MEASUREONVOLUME.floatValue() ? 1f:itemStats.getDryMatter()) * dailyIntake;
				Float cp = itemStats.getCrudeProtein() * dm;
				Float me = itemStats.getMetabolizableEnergy() * dm;
				feedItem.setDailyIntake(dailyIntake);				
				feedItem.getFeedItemNutritionalStats().setDryMatter(((itemStats.getDryMatter().floatValue() == Util.FulfillmentType.NO_DM_MEASUREONVOLUME.floatValue() ? 0f:itemStats.getDryMatter()) * dailyIntake));
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
				dailyIntake = getAllowedDailyIntakeValue(feedItem,dailyIntake);
//				dailyIntake = dailyIntake > feedItem.getMaximumFulfillment() ?  feedItem.getMaximumFulfillment() : dailyIntake;
//				dailyIntake = feedItem.getMaximumFulfillment() != null && dailyIntake > feedItem.getMaximumFulfillment() ?  feedItem.getMaximumFulfillment() : dailyIntake;

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
					dailyIntake = getAllowedDailyIntakeValue(feedItem,dailyIntake);
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

	private Float getAllowedDailyIntakeValue(FeedItem feedItem, Float dailyIntake) {
		
		Float maxFulfillment = feedItem.getMaximumFulfillment();
		Float minFulfillment = feedItem.getMinimumFulfillment();
//		IMDLogger.log("**********************************************", Util.INFO);		
//		IMDLogger.log(feedItem.getFeedItemLookupValue().getLookupValueCode() + " = " + dailyIntake, Util.INFO);
//		IMDLogger.log(" MAX = " + maxFulfillment, Util.INFO);
//		IMDLogger.log(" MIN = " + minFulfillment, Util.INFO);
		
		if (dailyIntake == null) {
			dailyIntake = feedItem.getFulfillmentPct();
		}
		if (maxFulfillment == null)
			maxFulfillment = MAX_FULFILLMENT_VALUE;
		if (minFulfillment == null)
			minFulfillment = MIN_FULFILLMENT_VALUE;
		
		if (minFulfillment > maxFulfillment) {
			IMDLogger.log("The following FeedItem's minimum and maximum fulfillments are incorrectly configured. Min fulfillment must be smaller than maximum fulfillment. We shall ignore the min and max fulfillments for this calculation: " + feedItem.getFeedCohortCD().getLookupValueCode() + " (" + feedItem.getFeedItemLookupValue().getLookupValueCode() + ")" , Util.ERROR);
		} else {	
			dailyIntake = Math.min(dailyIntake, maxFulfillment);
			dailyIntake = Math.max(dailyIntake, minFulfillment);
		}
//		IMDLogger.log(feedItem.getFeedItemLookupValue().getLookupValueCode() + " = " + dailyIntake, Util.INFO);
//		IMDLogger.log("**********************************************", Util.INFO);		
		
		return dailyIntake;
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

	private Float getMilkAverage(String orgID, String animalTag, LocalDate startAverageCalculationFrom, int pastNumOfDaysToAverage) {
		MilkingDetailLoader loader = new MilkingDetailLoader();
		Float milkAverage = null;
		try {
			List<MilkingDetail> milkInfo = loader.retrieveFarmMilkVolumeForSpecifiedDateRangeForSpecificAnimal(orgID, animalTag, startAverageCalculationFrom.minusDays(pastNumOfDaysToAverage), startAverageCalculationFrom, false);
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
		if (milkAverage == null) {
			IMDLogger.log("Milking Average could not be determined for the animal: " + animalTag + ". Will use the famr default value of: " + Util.DefaultValues.THREE_DAY_MILKING_AVERAGE, Util.WARNING);
			milkAverage = Util.DefaultValues.THREE_DAY_MILKING_AVERAGE.floatValue();		
		}
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
			if (weightEvent == null) {
				IMDLogger.log("Could not find a weight event for the animal : " + 
								animalTag + ", would not be able to determine the personalized feed plan of this animal.", Util.ERROR);
				return null;
			} else if (weightEvent.getAuxField1Value() == null || weightEvent.getAuxField1Value().isEmpty()) {
				IMDLogger.log("Could not find a valid weight values for the animal : " + 
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
				cohort.setCohortNutritionalNeeds(getFeedCohortNutritionalNeeds(cohort, animal));
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
	
	/**
	 * Determine Energy, DM, CP requirements of dairy cow based on the information provided in the following
	 * text (Chapter 6 & 7): 
	 * Tropical dairy farming : feeding management for small holder dairy farmers in the humid tropics By John Moran, 312 pp., Landlinks Press, 2005
	 * @param Animal
	 * @return NutritionalStats
	 */
	public CohortNutritionalNeeds determineFeedRequirementsOfLactatingCow(Animal animal) throws IMDException {
		if (animal.getWeight() == null) {
			throw new IMDException ("We can not calculate the dietary requirements of the animal " + animal.getAnimalTag() + " without its weight. Please add a Weight event for this animal and try again");
		}
		Double animalWeight = new Double (animal.getWeight());
		Float milkAverage = this.getMilkAverage(animal.getOrgID(), animal.getAnimalTag(), LocalDate.now(), 3);
		if (milkAverage == null) {
			throw new IMDException ("We can not calculate the dietary requirements of the animal " + animal.getAnimalTag() + " without its milking average. Please add its milking information for at least the last three days");
		}
		
		Integer pregnancyDays = null;
		if (animal.isPregnant()) {
			DateTime latestInseminationOrMatingEventTS = getLatestEventTimeStamp(animal,Util.LifeCycleEvents.INSEMINATE, Util.LifeCycleEvents.MATING);
			if (latestInseminationOrMatingEventTS != null) {
				pregnancyDays = Util.getDaysBetween(DateTime.now(),latestInseminationOrMatingEventTS);		
			}
		}
		CohortNutritionalNeeds nutritionalNeeds = getMetabolizableEnergyRequiremnt(animalWeight,new Double(milkAverage), pregnancyDays, animal.getFeedCohortInformation().getFeedCohortLookupValue().getLookupValueCode(),null,null);
		// CP
		// DM
		return nutritionalNeeds;
	}
	public CohortNutritionalNeeds getMetabolizableEnergyRequiremnt(Double animalWeight, Double threeDaysMilkingAverage, Integer daysIntoPregnancy, String feedCohort, Double fat, Double protein) {
		CohortNutritionalNeeds needs = new CohortNutritionalNeeds();
		CohortNutritionalNeeds maintenanceNeeds = getMaintenanceEnergyRequirement(animalWeight);
		CohortNutritionalNeeds pregnancyNeeds = getPregnancyEnergyRequirement(daysIntoPregnancy);
		if ( threeDaysMilkingAverage == null) {
			threeDaysMilkingAverage = Util.DefaultValues.THREE_DAY_MILKING_AVERAGE ;
		}
		CohortNutritionalNeeds milkProdNeeds = getMilkingEnergyRequirement(threeDaysMilkingAverage, (fat == null ? Util.DefaultValues.DEFAULT_FAT_PCT : fat), (protein == null ? Util.DefaultValues.DEFAULT_PROTEIN_PCT : protein));

		Double maintenanceME = maintenanceNeeds == null || maintenanceNeeds.getMetabloizableEnergy() == null ? 0d : maintenanceNeeds.getMetabloizableEnergy();
		Double milkME = milkProdNeeds == null || milkProdNeeds.getMetabloizableEnergy() == null ? 0d : milkProdNeeds.getMetabloizableEnergy();
		Double pregME = pregnancyNeeds == null || pregnancyNeeds.getMetabloizableEnergy() == null ? 0d : pregnancyNeeds.getMetabloizableEnergy();

		Double maintenanceTDN = maintenanceNeeds == null || maintenanceNeeds.getNutritionalNeedsTDN() == null ? 0d : maintenanceNeeds.getNutritionalNeedsTDN();
		Double milkTDN = milkProdNeeds == null || milkProdNeeds.getNutritionalNeedsTDN() == null ? 0d : milkProdNeeds.getNutritionalNeedsTDN();
		Double pregTDN = pregnancyNeeds == null || pregnancyNeeds.getNutritionalNeedsTDN() == null ? 0d : pregnancyNeeds.getNutritionalNeedsTDN();
		
		needs.setMetabloizableEnergy(new Float(maintenanceME + pregME + milkME));
		needs.setNutritionalNeedsTDN(new Float(maintenanceTDN + pregTDN + milkTDN));
		return needs;
	}

	public CohortNutritionalNeeds getMilkingEnergyRequirement(Double threeDaysMilkingAverage, Double fat, Double protein) {
		double meMatrix[][] = new double[16][10];
		double tdnMatrix[][] = new double[16][10];
		if (fat == null)
			fat = Util.DefaultValues.DEFAULT_FAT_PCT;
		if (protein == null)
			protein = Util.DefaultValues.DEFAULT_PROTEIN_PCT;
		int row = 0;
		String[] rowVal = "4.5,4.5,4.6,4.7,4.8,4.8,4.9,5,5,5.1".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "4.6,4.7,4.7,4.8,4.9,5,5,5.1,5.2,5.2".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "4.7,4.8,4.9,4.9,5,5.1,5.2,5.2,5.3,5.4".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "4.9,4.9,5,5.1,5.1,5.2,5.3,5.4,5.4,5.5".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "5,5.1,5.1,5.2,5.3,5.3,5.4,5.5,5.6,5.6".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "5.1,5.2,5.3,5.3,5.4,5.5,5.5,5.6,5.7,5.8".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "5.3,5.3,5.4,5.5,5.5,5.6,5.7,5.7,5.8,5.9".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "5.4,5.5,5.5,5.6,5.7,5.7,5.8,5.9,6,6".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "5.5,5.6,5.7,5.7,5.8,5.9,5.9,6,6.1,6.2".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "5.6,5.7,5.8,5.9,5.9,6,6.1,6.1,6.2,6.3".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "5.8,5.8,5.9,6,6.1,6.1,6.2,6.3,6.3,6.4".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "5.9,6,6,6.1,6.2,6.3,6.3,6.4,6.5,6.5".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "6,6.1,6.2,6.3,6.3,6.4,6.5,6.5,6.6,6.7".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "6.2,6.2,6.3,6.4,6.5,6.5,6.6,6.7,6.7,6.8".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "6.3,6.4,6.4,6.5,6.6,6.7,6.7,6.8,6.9,6.9".split(",");
		meMatrix[row++] = parseValues(rowVal);
		rowVal = "6.4,6.5,6.6,6.6,6.7,6.8,6.9,6.9,7,7.1".split(",");
		meMatrix[row++] = parseValues(rowVal);
		int rowIndex = determineIndex(fat,3d,6d,0.2d);
		int colIndex = determineIndex(protein,2.6d,4.4d,0.2d);
		CohortNutritionalNeeds needs = new CohortNutritionalNeeds();
		needs.setMetabloizableEnergy(new Float(meMatrix[rowIndex][colIndex] * threeDaysMilkingAverage));
//		IMDLogger.log(">>>>>> Milking Energy Requirement: [" + meMatrix[rowIndex][colIndex] + " * " + threeDaysMilkingAverage+ "]" + (meMatrix[rowIndex][colIndex] * threeDaysMilkingAverage), Util.INFO);

		
		row = 0;
		rowVal = "0.3,0.3,0.3,0.3,0.3,0.3,0.3,0.4,0.4,0.4".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.3,0.3,0.3,0.3,0.3,0.3,0.4,0.4,0.4,0.4".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.3,0.3,0.3,0.3,0.4,0.4,0.4,0.4,0.4,0.4".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.3,0.3,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.5".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.4,0.5,0.5".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.4,0.4,0.4,0.4,0.4,0.5,0.5,0.5,0.5,0.5".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.4,0.4,0.4,0.5,0.5,0.5,0.5,0.5,0.5,0.5".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.4,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5".split(",");
		tdnMatrix[row++] = parseValues(rowVal);
		rowVal = "0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5".split(",");
		tdnMatrix[row++] = parseValues(rowVal);

		rowIndex = determineIndex(fat,3d,6d,0.2d);
		colIndex = determineIndex(protein,2.6d,4.4d,0.2d);
		needs.setNutritionalNeedsTDN(new Float(tdnMatrix[rowIndex][colIndex] * threeDaysMilkingAverage));	
		return needs;
	}
	private int determineIndex(Double fat, double start, double end, double increment) {
		int index = 0;
		for (double f = start; f <= end; f += increment) {
			if (fat.doubleValue() > f)
				index++;
			else
				break;
		}
		return index;
	}

	private double[] parseValues(String[] strValues) {
		double[] dblValues = new double[strValues.length];
		for (int i=0; i<strValues.length; i++) {
			dblValues[i] = Float.parseFloat(strValues[i]);
		}
		return dblValues;
	}

	/**
	 * Calculations as per the research presented in (Chapter 6 & 7): 
	 * Tropical dairy farming : feeding management for small holder dairy farmers in the humid tropics By John Moran, 
	 * 312 pp., Landlinks Press, 2005
	 * @param daysSinceSuccessfulInsemination
	 * @return
	 */
	public CohortNutritionalNeeds getPregnancyEnergyRequirement(Integer daysSinceSuccessfulInsemination) {
		Float me = null;
		Float tdn = null;
		if (daysSinceSuccessfulInsemination == null)
			return null;
		else if (daysSinceSuccessfulInsemination <= 150f) {
			me = new Float(0);
			tdn = new Float(0);
		} else if (daysSinceSuccessfulInsemination > 150 && daysSinceSuccessfulInsemination <= 180) {
			// sixth month of pregnancy
			me = new Float(8f);
			tdn = new Float(0.6f);
		} else if (daysSinceSuccessfulInsemination > 180 && daysSinceSuccessfulInsemination <= 210) {
			// seventh month of pregnancy
			me = new Float(10f);
			tdn = new Float(0.7f);
		} else if (daysSinceSuccessfulInsemination > 210 && daysSinceSuccessfulInsemination <= 240) {
			// eighth month of pregnancy
			me = new Float(15f);
			tdn = new Float(1.1f);
		} else if (daysSinceSuccessfulInsemination > 240) {
			// ninth month of pregnancy
			me = new Float(20f);
			tdn = new Float(1.4f);
		}
		CohortNutritionalNeeds energyRequirements = new CohortNutritionalNeeds();
		energyRequirements.setMetabloizableEnergy(me);
		energyRequirements.setNutritionalNeedsTDN(tdn);
//		IMDLogger.log(">>>>>> Pregnancy Energy Requirement: " + me, Util.INFO);
		return energyRequirements;	
	}	
	
	/**
	 * Calculations as per the research presented in (Chapter 6 & 7): 
	 * Tropical dairy farming : feeding management for small holder dairy farmers in the humid tropics By John Moran, 
	 * 312 pp., Landlinks Press, 2005
	 * @param animalWeight
	 * @return
	 */
	public CohortNutritionalNeeds getMaintenanceEnergyRequirement(Double animalWeight) {
		Float me = null;
		Float tdn = null;
		Float y1 = null;
		Float y2 = null;
		Float x1 = null;
		Float x2 = null;
		Float z1 = null;
		Float z2 = null;
		if (animalWeight == null)
			return null;
		else if (animalWeight <= 100f) {
			y1 = 0f;
			y2 = 100f;
			x1 = 0f;
			x2 = 17f;
			z1 = 0f;
			z2 = 1.2f;
		} else if (animalWeight <= 150) {
			y1 = 100f;
			y2 = y1 + 50;
			x1 = 17f;
			x2 = x1 + 5f;
			z1 = 1.2f;
			z2 = z1 + 0.3f;
		} else if (animalWeight <= 200) {
			y1 = 150f;
			y2 = y1 + 50;
			x1 = 22f;
			x2 = x1 + 5f;
			z1 = 1.5f;
			z2 = z1 + 0.4f;
		} else if (animalWeight <= 250) {
			y1 = 200f;
			y2 = 250f;
			x1 = 27f;
			x2 = x1 + 4f;
			z1 = 1.9f;
			z2 = z1 + 0.3f;
		} else if (animalWeight <= 300) {
			y1 = 250f;
			y2 = y1 + 50;
			x1 = 31f;
			x2 = x1 + 5f;
			z1 = 2.2f;
			z2 = z1 + 0.3f;
		} else if (animalWeight <= 350) {
			y1 = 300f;
			y2 = y1 + 50;
			x1 = 36f;
			x2 = x1 + 4f;
			z1 = 2.5f;
			z2 = z1 + 0.3f;
		} else if (animalWeight <= 400) {
			y1 = 350f;
			y2 = y1 + 50;
			x1 = 40f;
			x2 = x1 + 5f;
			z1 = 2.8f;
			z2 = z1 + 0.3f;
		} else if (animalWeight <= 450) {
			y1 = 400f;
			y2 = y1 + 50;
			x1 = 45f;
			x2 = x1 + 4f;
			z1 = 3.1f;
			z2 = z1 + 0.3f;
		} else if (animalWeight <= 500) {
			y1 = 450f;
			y2 = y1 + 50;
			x1 = 49f;
			x2 = x1 + 5f;
			z1 = 3.4f;
			z2 = z1 + 0.4f;
		} else if (animalWeight <= 550) {
			y1 = 500f;
			y2 = y1 + 50;
			x1 = 54f;
			x2 = x1 + 5f;
			z1 = 3.8f;
			z2 = z1 + 0.3f;
		} else {
			/* > 550 kg */ 
			y1 = 550f;
			y2 = y1 + 50;
			x1 = 59f;
			x2 = x1 + 4f;
			z1 = 4.1f;
			z2 = z1 + 0.3f;
		}
		me = new Float( Util.formatTwoDecimalPlaces((x1 + ((animalWeight - y1) *  (x2 - x1)/ (y2-y1)))));
		tdn = new Float(  Util.formatTwoDecimalPlaces(z1 + ((animalWeight - y1) *  (z2 - z1)/ (y2-y1))));
		CohortNutritionalNeeds energyRequirements = new CohortNutritionalNeeds();
		energyRequirements.setMetabloizableEnergy(me);
		energyRequirements.setNutritionalNeedsTDN(tdn);
//		IMDLogger.log(">>>>>> Maintenance Energy Requirement: " + me, Util.INFO);
		return energyRequirements;
	}
}
