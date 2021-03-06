package com.imd.dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormatter;

import com.imd.loader.AnimalLoader;
import com.imd.util.IMDException;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

public class Animal extends IMDairiesDTO{

	/**
	 * This is the tag ID that is visible on the animal. Tag id should be unique across all active animals at the farm.
	 */
	
	private String animalTag;
	private DateTime dateOfBirth;
	private boolean isDateOfBirthEstimated;
	private double purchasePrice;
	private String purchaseCurrency = "PKR";
	private Contact purchaseFromContact;
	private DateTime purchaseDate;
	private String alias;
	private Sire animalSire;
	private Dam animalDam;
	private Float weight;
	private Float milkingAverage; 
	private ArrayList<byte[]> photos;
	private Integer photoCount;
	
	// events must ALWAYS be kept sorted by date with latest on top and oldest at bottom. All the code assumes this !
	private List<LifecycleEvent> lifeCycleEvents; 
	
	private String animalType;
	private String animalTypeCD;
	private String frontSideImageURL;
	private String backSideImageURL;
	private String leftSideImageURL;
	private String rightSideImageURL;
	private boolean isBornThroughAI;
	private boolean isThreshold1Violated;
	private boolean isThreshold2Violated;
	private boolean isThreshold3Violated;
	private String statusIndicators;
	private int parturationCount;
	private String breed;
	private DateTime herdJoiningDate;
	private DateTime herdLeavingDate;
	private FeedCohort feedCohortInformation;
	private CohortNutritionalNeeds animalNutritionalNeeds;
	private FeedPlan animalFeedPlan;
	private String operatorAtBirth;
	
	/**
	 * M: Male
	 * F: Female
	 * U: Unknown
	 * 
	 */
	private String gender;
	/** 
	 * Holds any user entered text against this Animal.
	 */
	private ArrayList<Note> notes;
	

	public Animal(String orgID, String tagNumber, DateTime birthDate, boolean isDobEstimated, double purPrice, String priceCurr) throws IMDException{

		if (tagNumber == null || birthDate == null || priceCurr == null)
			throw new IMDException ("One or more values contain null. Please provide valid values for all the parameters");
		else {
			this.setOrgId(orgID);
			this.animalTag = tagNumber;
			this.dateOfBirth = birthDate;
			this.isDateOfBirthEstimated = isDobEstimated;
			this.purchasePrice = purPrice;
			this.purchaseCurrency = priceCurr;
		}
	}

	public Animal(String orgID, String tagNumber) throws IMDException {
		if (orgID == null || orgID.isEmpty())
			throw new IMDException ("orgID can't be null or empty");
		else {
			this.setOrgId(orgID);
		}
		if (tagNumber == null || tagNumber.isEmpty())
			throw new IMDException ("Tag Number can't be null or empty");
		else {
			this.animalTag = tagNumber;
		}
	}
	public String getAnimalTag() {
		return this.animalTag;
	}
	public void setAnimalTag(String aID) {
		this.animalTag = aID;
	}
	public DateTime getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(DateTime dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	public boolean isDateOfBirthEstimated() {
		return isDateOfBirthEstimated;
	}
	public void setDateOfBirthEstimated(boolean isDateOfBirthEstimated) {
		this.isDateOfBirthEstimated = isDateOfBirthEstimated;
	}
	public String getGender() {
		return gender;
	}
	protected void setGender(String gender) {
		this.gender = gender;
	}
	protected void setGender(char gender) {
		this.gender = gender + "";
	}
	public Note getNote(int index) {
		return notes.get(index);
	}
	
	public ArrayList<Note> getAllNotes() {
		return notes;
	}
	
	public int getNotesCount() {
		return notes.size();
	}
	
	public Integer getPhotoCount() {
		return this.photoCount;
	}
	public void setPhotoCount(Integer count) {
		this.photoCount = count;
	}
	
	public void setNotes(ArrayList<Note> notesList) {
		this.notes = notesList;
	}
	
	public void addNote(Note note) {
		if (this.notes == null)
			this.notes = new ArrayList <Note>();
		this.notes.add(note);
	}
	public void removeNote(int index) {
		if (this.notes != null)
			this.notes.remove(index);
	}
	
	public Period getCurrentAge() {
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		if (this.dateOfBirth == null ) return new Period(now, now, PeriodType.yearMonthDayTime());
		if (Util.getDaysBetween(now, dateOfBirth) <= 1) {
			return new Period(this.dateOfBirth, now, PeriodType.yearMonthDayTime());
		}
		else
			return new Period(this.dateOfBirth, now, PeriodType.yearMonthDay());			
	}
	public int getCurrentAgeInDays() {
		DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
		if (this.dateOfBirth == null ) 
			return 0;
		return Util.getDaysBetween(now, dateOfBirth);
	}
	public double getPurchasePrice() {
		return purchasePrice;
	}
	public void setPurchasePrice(double purchasePrice) {
		this.purchasePrice = purchasePrice;
	}
	public String getPurchaseCurrency() {
		return purchaseCurrency;
	}
	public void setPurchaseCurrency(String purchaseCurrency) {
		this.purchaseCurrency = purchaseCurrency;
	}
	public Contact getPurchaseFrom() {
		return purchaseFromContact;
	}
	public void setPurchaseFrom(Contact purchaseFrom) {
		this.purchaseFromContact = purchaseFrom;
	}
	public DateTime getPurchaseDate() {
		return purchaseDate;
	}
	public void setPurchaseDate(DateTime purchaseDate) {
		this.purchaseDate = purchaseDate;
	}
	public Days getAgeAtPurchase() {
		return Days.daysBetween(dateOfBirth,purchaseDate);
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Sire getAnimalSire() {
		return animalSire;
	}

	public void setAnimalSire(Sire animalSire) {
		this.animalSire = animalSire;
	}

	public Dam getAnimalDam() {
		return animalDam;
	}

	public void setAnimalDam(Dam animalDam) {
		this.animalDam = animalDam;
	}

	public int removePhoto(int index) {
		int size = 0;
		if (photos != null && !photos.isEmpty()) {
			photos.remove(index);
			size = photos.size();
		}
		return size;
	}

	public void addPhoto(byte[] photo) {
		this.photos.add(photo);
	}
	

	public List<byte[]> getAllPhotos() {
		return photos;
	}


	public List<LifecycleEvent> getLifeCycleEvents() {
		return lifeCycleEvents;
	}

	public void setLifeCycleEvents(List<LifecycleEvent> lifeCycleEvents) {
		this.lifeCycleEvents = lifeCycleEvents;
	}
	public void addLifecycleEvent(LifecycleEvent event) throws IMDException {
		if (lifeCycleEvents == null) 
			lifeCycleEvents = new ArrayList<LifecycleEvent> ();
		Util.throwExceptionIfNull(event, "Life Cycle Event");
		lifeCycleEvents.add(event);
	}

	public String getAnimalStatus() {
		if (herdJoiningDate != null && herdLeavingDate == null) 
			// active
			return "ACTIVE";
		else 
			// in active
			return "INACTIVE";
	}
	public boolean isInseminated() {
		return (getStatusIndicators() == null ? false : getStatusIndicators().indexOf(AnimalLoader.INSEMINATED_INDICATOR)>=0);
	}

	public boolean isPregnant() {
		return (getStatusIndicators() == null ? false : getStatusIndicators().indexOf(AnimalLoader.PREGNANT_INDICATOR)>=0);
	}
	
	public boolean isHeifer() {
		return (getStatusIndicators() == null ? false : getStatusIndicators().indexOf(AnimalLoader.HEIFER_INDICATOR)>=0);
	}
	public boolean isLactating() {
		return (getStatusIndicators() == null ? false : getStatusIndicators().indexOf(AnimalLoader.LACTATING_INDICATOR)>=0);
	}
	public boolean isDry() {
		return (getStatusIndicators() == null ? false : getStatusIndicators().indexOf(AnimalLoader.DRY_INDICATOR)>=0);
	}
	
	public String toString() {
		String value =  stringify(" ");
		int eventCount = 0;
		if (this.lifeCycleEvents == null || this.lifeCycleEvents.isEmpty())
			return value;
		Iterator<LifecycleEvent> it = this.lifeCycleEvents.iterator();
		value += "\"lifecycleEvents\"=[\n";
		while (it.hasNext()) {
			eventCount++;
			if (eventCount == this.lifeCycleEvents.size())
				value +=  "  {\n" + it.next().dtoToJson("     ") + "\n }\n";
			else
				value +=  "  {\n" + it.next().dtoToJson("     ") + "\n },\n";
		}
		return value;
	}
	private String stringify(String prefix) {
		String cohort = (this.feedCohortInformation == null ? "" : this.feedCohortInformation.dtoToJson(prefix,false));

		return  prefix + fieldToJson("orgID", getOrgId()) + ",\n" + 
				prefix + fieldToJson("animalTag", this.animalTag) + ",\n" +
				prefix + fieldToJson("animalType", this.animalType) + ",\n" +
				prefix + fieldToJson("animalTypeCD", this.animalTypeCD) + ",\n" +
				prefix + fieldToJson("weight", this.weight == null ? "" : this.weight.toString()) + ",\n" +
				prefix + fieldToJson("milkingAverage", this.milkingAverage == null ? "" : Util.formatToSpecifiedDecimalPlaces(this.milkingAverage, 2)) + ",\n" +
				prefix + fieldToJson("statusIndicators", this.statusIndicators == null ? "" : this.statusIndicators) + ",\n" + 
				prefix + fieldToJson("breed", this.breed) + ",\n" +
				prefix + fieldToJson("animalStatus", this.getAnimalStatus()) + ",\n" + 
				prefix + fieldToJson("parturationCount", this.parturationCount) + ",\n" + 
				prefix + fieldToJson("dateOfBirth", this.dateOfBirth) + ",\n" + 
				prefix + fieldToJson("herdJoiningDate", this.herdJoiningDate) + ",\n" + 
				prefix + fieldToJson("herdLeavingDate", this.herdLeavingDate) + ",\n" + 
				prefix + fieldToJson("isDateOfBirthEstimated", this.isDateOfBirthEstimated) + ",\n" + 
				prefix + fieldToJson("currentAge", this.getCurrentAge()) + ",\n" + 
				prefix + fieldToJson("gender", this.gender) + ",\n" + 
				prefix + fieldToJson("animalDam", this.animalDam == null ? "" : this.animalDam.getAnimalTag()) + ",\n" + 
				prefix + fieldToJson("animalSire", this.animalSire == null ? "" : this.animalSire.getAnimalTag()) + ",\n" +
				prefix + fieldToJson("animalSireURL", this.animalSire == null ? "" : this.animalSire.getSireSpecification()) + ",\n" +
				prefix + fieldToJson("animalSireAlias", this.animalSire == null ? "" : this.animalSire.getAlias()) + ",\n" +
				prefix + fieldToJson("isBornThroughAI", this.isBornThroughAI) + ",\n" + 
				prefix + fieldToJson("alias", this.alias == null ? "" : this.alias) + ",\n" +
				prefix + fieldToJson("frontSideImageURL", this.frontSideImageURL) + ",\n" +
				prefix + fieldToJson("backSideImageURL", this.backSideImageURL) + ",\n" + 
				prefix + fieldToJson("rightSideImageURL", this.rightSideImageURL) + ",\n" +
				prefix + fieldToJson("photoCount", this.getPhotoCount()) + ",\n" +
				prefix + fieldToJson("leftSideImageURL", this.leftSideImageURL) + ",\n" + cohort +
				(this.animalNutritionalNeeds == null ? "" : this.animalNutritionalNeeds.dtoToJson(prefix,false));
	}
	

	public String dtoToJson(String prefix)  {		
		return stringify(prefix) + super.dtoToJson(prefix);
	}
	
	public String dtoToJson(String prefix, DateTimeFormatter fmt)  {
		return (stringify(prefix) + super.dtoToJson(prefix, fmt));
	}

	public String getAnimalType() {
		return animalType;
	}

	public void setAnimalType(String animalType) {
		this.animalType = animalType;
	}

	public String getFrontSideImageURL() {
		return frontSideImageURL;
	}

	public void setFrontSideImageURL(String frontImageURL) {
		this.frontSideImageURL = frontImageURL;
	}

	public String getLeftSideImageURL() {
		return leftSideImageURL;
	}

	public void setLeftSideImageURL(String leftSideImageURL) {
		this.leftSideImageURL = leftSideImageURL;
	}

	public String getRightSideImageURL() {
		return rightSideImageURL;
	}

	public void setRightSideImageURL(String rightSideImageURL) {
		this.rightSideImageURL = rightSideImageURL;
	}

	public String getBackSideImageURL() {
		return backSideImageURL;
	}

	public void setBackSideImageURL(String backsideImageURL) {
		this.backSideImageURL = backsideImageURL;
	}	
	public boolean isBornThroughAI() {
		return isBornThroughAI;
	}
	public void setBornThroughAI(boolean isBornThroughAI) {
		this.isBornThroughAI = isBornThroughAI;
	}

	public String getAnimalTypeCD() {
		return animalTypeCD;
	}

	public void setAnimalTypeCD(String animalTypeCD) {
		this.animalTypeCD = animalTypeCD;
	}

	public boolean isThreshold1Violated() {
		return isThreshold1Violated;
	}

	public void setThreshold1Violated(boolean isThreshold1Violated) {
		this.isThreshold1Violated = isThreshold1Violated;
	}

	public boolean isThreshold2Violated() {
		return isThreshold2Violated;
	}

	public void setThreshold2Violated(boolean isThreshold2Violated) {
		this.isThreshold2Violated = isThreshold2Violated;
	}

	public boolean isThreshold3Violated() {
		return isThreshold3Violated;
	}

	public void setThreshold3Violated(boolean isThreshold3Violated) {
		this.isThreshold3Violated = isThreshold3Violated;
	}

	public String getStatusIndicators() {
		return statusIndicators;
	}

	public void setStatusIndicators(String statusIndicators) {
		this.statusIndicators = statusIndicators;
	}

	public int getParturationCount() {
		return parturationCount;
	}

	public void setParturationCount(int parturationCount) {
		this.parturationCount = parturationCount;
	}
	public String getBreed() {
		return breed;
	}

	public void setBreed(String breed) {
		this.breed = breed;
	}

	public DateTime getHerdJoiningDate() {
		return herdJoiningDate;
	}

	public void setHerdJoiningDate(DateTime herdJoiningDate) {
		this.herdJoiningDate = herdJoiningDate;
	}

	public DateTime getHerdLeavingDate() {
		return herdLeavingDate;
	}

	public void setHerdLeavingDate(DateTime herdLeavingDate) {
		this.herdLeavingDate = herdLeavingDate;
	}

	public FeedCohort getFeedCohortInformation() {
		return feedCohortInformation;
	}

	public void setFeedCohortInformation(FeedCohort feedCohortInformation) {
		this.feedCohortInformation = feedCohortInformation;
	}
	
	public void setAnimalNutritionalNeeds(CohortNutritionalNeeds needs) {
//		if (weightInKg != null && needs != null) {
//			this.animalNutritionalNeeds = needs;
//			this.animalNutritionalNeeds.setDryMatter(needs.getDryMatter() * weightInKg);
//			this.animalNutritionalNeeds.setCrudeProtein(needs.getCrudeProtein() * this.animalNutritionalNeeds.getDryMatter());
//			this.animalNutritionalNeeds.setMetabloizableEnergy(needs.getMetabloizableEnergy());
//		}
		this.animalNutritionalNeeds = needs;
	}
	
	public CohortNutritionalNeeds getAnimalNutritionalNeeds() {
		return this.animalNutritionalNeeds;
	}

	public void setWeight(Float latestEventWeight) {
		this.weight = latestEventWeight;
	}
	public Float getWeight() {
		return this.weight;
	}

	public FeedPlan getAnimalFeedPlan() {
		return animalFeedPlan;
	}

	public void setAnimalFeedPlan(FeedPlan animalIndividualizedFeedPlan) {
		this.animalFeedPlan = animalIndividualizedFeedPlan;
	}

	public String getOperatorAtBirth() {
		return operatorAtBirth;
	}

	public void setOperatorAtBirth(String operatorAtBirth) {
		this.operatorAtBirth = operatorAtBirth;
	}

	public Float getMilkingAverage() {
		return milkingAverage;
	}

	public void setMilkingAverage(Float milkingAverage) {
		this.milkingAverage = milkingAverage;
	}

//	public void setAnimalNutritionalNeeds(CohortNutritionalNeeds needs) {
//		this.animalNutritionalNeeds = needs;		
//	}
	
	
}




