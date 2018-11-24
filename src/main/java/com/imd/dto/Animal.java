package com.imd.dto;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;

import com.google.gson.Gson;
import com.imd.util.IMDException;
import com.imd.util.Util;

public class Animal extends IMDairiesDTO{

	private String animalTag;
	private DateTime dateOfBirth;
	private boolean isDateOfBirthEstimated;
	private double purchasePrice;
	private String purchaseCurrency = "Rs.";
	private Contact purchaseFromContact;
	private DateTime purchaseDate;
	private String alias;
	private Sire animalSire;
	private Animal animalDam;
	private ArrayList<byte[]> photos;
	private ArrayList<LifeCycleEventCode> lifeCycleEvents;
	/**
	 * This is the tag ID that is visible on the animal. Tag id should be unique across all active animals at the farm.
	 */
	private String tagID;
	/**
	 * M: Male
	 * F: Female
	 * U: Unknown
	 * 
	 */
	private char gender;
	/** 
	 * Holds any user entered text against this Animal.
	 */
	private ArrayList<Note> notes;
	private String animalStatus;

	public Animal(String orgID, String tagNumber, DateTime birthDate, boolean isDobEstimated, double purPrice, String priceCurr) throws IMDException{

		if (tagNumber == null || birthDate == null || priceCurr == null)
			throw new IMDException ("One or more values contain null. Please provide valid values for all the parameters");
		else {
			this.setOrgID(orgID);
			this.animalTag = tagNumber;
			this.dateOfBirth = birthDate;
			this.isDateOfBirthEstimated = isDobEstimated;
			this.purchasePrice = purPrice;
			this.purchaseCurrency = priceCurr;
		}
	}

	public Animal(String tagNumber) throws IMDException {
		if (tagNumber == null || tagNumber.isEmpty())
			throw new IMDException ("Tag Number can't be null or empty");
		else {
			this.tagID = tagNumber;
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
	public String getTagID() {
		return tagID;
	}
	public void setTagID(String tagID) {
		this.tagID = tagID;
	}
	public char getGender() {
		return gender;
	}
	protected void setGender(char gender) {
		this.gender = gender;
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
	
	/**
	 * returns the period between today and the date of birth.
	 * @return
	 */
	public Days getCurrentAge() {
		return Days.daysBetween(dateOfBirth, DateTime.now());
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

	public Animal getAnimalDam() {
		return animalDam;
	}

	public void setAnimalDam(Animal animalDam) {
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

	public void setDamInformation(Dam dam) {
		
	}

	public ArrayList<LifeCycleEventCode> getLifeCycleEvents() {
		return lifeCycleEvents;
	}

	public void setLifeCycleEvents(ArrayList<LifeCycleEventCode> lifeCycleEvents) {
		this.lifeCycleEvents = lifeCycleEvents;
	}
	public void addLifecycleEvent(LifeCycleEventCode event) throws IMDException {
		if (lifeCycleEvents == null) 
			lifeCycleEvents = new ArrayList<LifeCycleEventCode> ();
		Util.throwExceptionIfNull(event, "Life Cycle Event");
		lifeCycleEvents.add(event);
	}

	public String getAnimalStatus() {
		return this.animalStatus;
	}	
	
	public void setAnimalStatus(String animalStatus) {
		this.animalStatus = animalStatus;
	}	
	
}