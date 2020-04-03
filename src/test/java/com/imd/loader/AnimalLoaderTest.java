package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Advisement;
import com.imd.dto.Animal;
import com.imd.dto.AnimalPhoto;
import com.imd.dto.BankDetails;
import com.imd.dto.Contact;
import com.imd.dto.Dam;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.MilkingDetail;
import com.imd.dto.Note;
import com.imd.dto.Person;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.services.AnimalSrvc;
import com.imd.services.MilkingInformationSrvc;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.MilkingDetailBean;
import com.imd.services.bean.SireBean;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

class AnimalLoaderTest {
	

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	public Animal createTestAnimal(String animalTag) throws Exception {
		Dam c000 = new Dam(/*orgid*/"IMD",/*tag*/animalTag,/*dob*/DateTime.parse("2014-02-09"),/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		c000.setAlias("Laal");
		c000.setBreed(Util.Breed.HFCROSS);
		c000.setAnimalType("LACTATING");
		c000.setFrontSideImageURL("/assets/img/cow-thumbnails/000/1.png");
		c000.setBackSideImageURL("/assets/img/cow-thumbnails/000/2.png");
		c000.setRightSideImageURL("/assets/img/cow-thumbnails/000/3.png");
		c000.setLeftSideImageURL("/assets/img/cow-thumbnails/000/4.png");
//		c000.setMilkingAverageAtPurchase(new MilkingDetail(/*milk freq*/(short)3, /*machine milked*/true, /*record date*/LocalDate.parse("2017-02-08"), 
//				/*record time*/LocalTime.parse("18:00:00"), /*milk vol*/27.0f, (short)1));
		c000.setPurchaseDate(DateTime.parse("2017-02-08"));
		c000.setCreatedBy(new User("KASHIF"));
		c000.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		c000.setHerdJoiningDate(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(10));
		c000.setHerdLeavingDate(null);
		c000.setUpdatedBy(c000.getCreatedBy());
		c000.setUpdatedDTTM(c000.getCreatedDTTM());
		setPurchaseFromContact(c000);
		setSireInformation(c000);
		c000.setAnimalDam(null);
		Note newNote = new Note (1,"Had four adult teeth at purchase. Dark brown/red shade in the coat. Shy of people, docile, keeps away from humans, hangs out well with other cows, medium built.", DateTime.now(IMDProperties.getServerTimeZone()));		
		c000.addNote(newNote);
		return c000;
	}

	private MilkingDetail createMilkingRecord(String tagNbr, LocalDate milkingDate, LocalTime milkingTime) throws IMDException {
		short milkFreq = 3;
		float milkingVol = 13.0f;
		boolean isMachineMilked = true;	
		MilkingDetail milkingRecord = new MilkingDetail("IMD", tagNbr, milkFreq, isMachineMilked, milkingDate, milkingTime, milkingVol,(short)1);
		milkingRecord.setHumidity(50.0f);
		milkingRecord.setTemperatureInCentigrade(19.3f);
		milkingRecord.setLrValue(28.0f);
		milkingRecord.setFatValue(3.80f);
		milkingRecord.setToxinValue(0.11f);
		milkingRecord.setCreatedBy(new User("KASHIF"));
		milkingRecord.setCreatedDTTM(DateTime.now());
		milkingRecord.setUpdatedBy(new User("KASHIF"));
		milkingRecord.setUpdatedDTTM(DateTime.now());
		return milkingRecord;
	}
	
	private void setSireInformation(Animal c000) throws IMDException {
		Sire sire = new Sire("IMD","NLDM000291306935", DateTime.parse("2000-02-10"), false, 0d, "PKR");
		sire.setAlias("MANDERS MARIUS");
		sire.setMarketedByCompany(new Contact("Not known"));
		Contact company = new Contact("CRV");
		company.setWebURI(URI.create("https://www.crv4all-international.com/find-bull/"));
		sire.setOwnerCompany(company);
		sire.addNote(new Note(1,"Not sure if this is truly the sire. Got minimal'\"%\n information of the sire of 026 from Babar Hameed Jathol", DateTime.now(IMDProperties.getServerTimeZone())));
		c000.setAnimalSire(sire);
	}

	private void setPurchaseFromContact(Animal c000) throws IMDException {
		Contact contact = new Contact("Babar", "Hameed", "Jathol");
		contact.setNamePrefix("Mr.");
		
		// Address related tests
		contact.setAddress("Jathol Farm", "25 Km from Jaran Wala","", "Joray Khoo\n'", "Punjab", "Pakistan", null);
		contact.setAddressInstructions("Take motorway from Lahore to Islamabad and take the first exit and continue straight on that road for 25 km. Then take left.");
		
		// Test for bank details
		BankDetails bankDetails;
		bankDetails = new BankDetails("JATHOL AGRI & DAIRY", "Habib Bank Limited", "09977900302603", "HABB0009977900302603");
		contact.setBankAccountInformation(bankDetails);		
		c000.setPurchaseFrom(contact);
	}
	
	
	@Test
	void testRetrieveCalvingsInDateRange() {
		try {
			int originalLogginMode = IMDLogger.loggingMode;
			IMDLogger.loggingMode = Util.INFO;
			Animal animalInRange = this.createTestAnimal("-999");
			Animal animalOutOfRange = this.createTestAnimal("-998");
			
			DateTime referenceDate = new DateTime(2019,10,1,0,0,IMDProperties.getServerTimeZone());
			AnimalLoader animalLoader = new AnimalLoader();
			
			assertTrue(animalLoader.deleteAnimal(animalInRange.getOrgID(), animalInRange.getAnimalTag()) >= 0);
			assertTrue(animalLoader.deleteAnimal(animalOutOfRange.getOrgID(), animalOutOfRange.getAnimalTag()) >= 0);

			animalInRange.setDateOfBirth(referenceDate);
			animalOutOfRange.setDateOfBirth(referenceDate.minusMonths(1));
			
			HashMap<String,String> values = animalLoader.retrieveCalvingsInDateRange(animalInRange.getOrgID(), 
					new DateTime(2019,1,1,0,0,IMDProperties.getServerTimeZone()),
					new DateTime(2019,12,31,23,59,IMDProperties.getServerTimeZone()));
			String referenceMonthCalvingsCountStr = values.get(referenceDate.getYear() + "-" + referenceDate.getMonthOfYear());
			String referencePreviousMonthCalvingsCountStr = values.get(referenceDate.minusMonths(1).getYear() + "-" + referenceDate.minusMonths(1).getMonthOfYear());
			int referenceMonthCalvingsCount = 0;
			int referencePreviousMonthCalvingsCount = 0;
			if (referenceMonthCalvingsCountStr != null)
				referenceMonthCalvingsCount = Integer.parseInt(referenceMonthCalvingsCountStr);
				
			if (referencePreviousMonthCalvingsCountStr != null)
				referencePreviousMonthCalvingsCount = Integer.parseInt(referencePreviousMonthCalvingsCountStr);
			
			animalLoader.insertAnimal(animalInRange);
			animalLoader.insertAnimal(animalOutOfRange);

			values = animalLoader.retrieveCalvingsInDateRange(animalInRange.getOrgID(), 
					new DateTime(2019,1,1,0,0,IMDProperties.getServerTimeZone()),
					new DateTime(2019,12,31,23,59,IMDProperties.getServerTimeZone()));
			referenceMonthCalvingsCountStr = values.get(referenceDate.getYear()  + "-" + referenceDate.getMonthOfYear());
			referencePreviousMonthCalvingsCountStr = values.get(referenceDate.minusMonths(1).getYear()+ "-" +referenceDate.minusMonths(1).getMonthOfYear());
			assertTrue(referenceMonthCalvingsCountStr != null);
			assertTrue(referencePreviousMonthCalvingsCountStr != null);
			assertEquals(referenceMonthCalvingsCount+1, Integer.parseInt(referenceMonthCalvingsCountStr));
			assertEquals(referencePreviousMonthCalvingsCount+1, Integer.parseInt(referencePreviousMonthCalvingsCountStr));
			
			assertTrue(animalLoader.deleteAnimal(animalInRange.getOrgID(), animalInRange.getAnimalTag()) == 1);
			assertTrue(animalLoader.deleteAnimal(animalOutOfRange.getOrgID(), animalOutOfRange.getAnimalTag()) == 1);
			IMDLogger.loggingMode = originalLogginMode;
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occurred "+ e.getMessage());
		}	}
	
	@Test
	void testAnimalBornAfter() {
		try {
			Animal youngAnimal = this.createTestAnimal("-999");
			Animal oldAnimal = this.createTestAnimal("-998");
			Animal borderLineAnimal = this.createTestAnimal("-997");
			AdvisementLoader advLdr =  new AdvisementLoader();
			List<Advisement> rules = advLdr.getSpecifiedActiveAdvisementRules("IMD", Util.AdvisementRules.CALFWEIGHT);
			assertTrue(rules != null && !rules.isEmpty() && rules.size() == 1,"Exactly one " + Util.AdvisementRules.CALFWEIGHT + " active advisement rule should have been found");
			assertTrue(rules.get(0).getAuxInfo1() != null && !rules.get(0).getAuxInfo1().isEmpty(), Util.AdvisementRules.CALFWEIGHT + " rule should have an AUX_INFO1 value that contains the age in days of the calves that we will consider for this advisement");
			
			DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
			DateTime dob = now.minusDays(Integer.parseInt(rules.get(0).getAuxInfo1()));
			AnimalLoader ldr = new AnimalLoader();
			
			assertTrue(ldr.deleteAnimal(youngAnimal.getOrgID(), youngAnimal.getAnimalTag()) >= 0);
			assertTrue(ldr.deleteAnimal(oldAnimal.getOrgID(), oldAnimal.getAnimalTag()) >= 0);
			assertTrue(ldr.deleteAnimal(borderLineAnimal.getOrgID(), borderLineAnimal.getAnimalTag()) >= 0);

			youngAnimal.setDateOfBirth(dob.plusDays(10));
			oldAnimal.setDateOfBirth(dob.minusDays(100));
			borderLineAnimal.setDateOfBirth(dob);
			
			ldr.insertAnimal(youngAnimal);
			ldr.insertAnimal(oldAnimal);
			ldr.insertAnimal(borderLineAnimal);
			
			
			boolean youngFound = false;
			boolean borderLineFound = false;
			List <Animal> animals = ldr.retrieveAnimalsBornOnOrAfterSpecifiedDate(youngAnimal.getOrgID(), dob);
			assertTrue(animals != null && animals.size() >= 2, " At least two animals should have been retrieved");
			Iterator<Animal> it = animals.iterator();
			while (it.hasNext()) {
				Animal anml = it.next();
				String tag = anml.getAnimalTag();
				if (tag.equals(oldAnimal.getAnimalTag())) {
					fail("The animal " + oldAnimal.getAnimalTag() + " should not have been found as it is " + oldAnimal.getCurrentAgeInDays() + " days old which is older than " + borderLineAnimal.getCurrentAgeInDays() + " days");
				} else if (tag.equals(youngAnimal.getAnimalTag())) {
					youngFound = true;					
				} else if (tag.equals(borderLineAnimal.getAnimalTag())) {
					borderLineFound = true;
				}
			}
			
			assertTrue(youngFound,youngAnimal.getAnimalTag() + " should have been retrieved as it is younger than " + borderLineAnimal.getCurrentAgeInDays() + " days");
			assertTrue(borderLineFound,borderLineAnimal.getAnimalTag() + " should have been retrieved as it is " + borderLineAnimal.getCurrentAgeInDays() + " days in age");
			
			assertTrue(ldr.deleteAnimal(youngAnimal.getOrgID(), youngAnimal.getAnimalTag()) == 1);
			assertTrue(ldr.deleteAnimal(oldAnimal.getOrgID(), oldAnimal.getAnimalTag()) == 1);
			assertTrue(ldr.deleteAnimal(borderLineAnimal.getOrgID(), borderLineAnimal.getAnimalTag()) == 1);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occurred "+ e.getMessage());
		}
		
	}
	 
	@Test
	void testAnimalProcessing() {
		try {
			String animalTag = "000";
			Animal animal;
			animal = createTestAnimal(animalTag);
			AnimalLoader loader = new AnimalLoader();
			loader.deleteAnimal("IMD", animalTag);
			DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
			animal.setDateOfBirth(now.minusDays(10));
			assertEquals(0,animal.getCurrentAge().getYears(), " The current age should be less than a year");
			assertEquals(0,animal.getCurrentAge().getMonths(), " The current age should be less than a month");
			assertEquals(10,animal.getCurrentAgeInDays(), " The current age should be one day but it was " + animal.getCurrentAgeInDays());
			int transactionID = loader.insertAnimal(animal);
			assertTrue(transactionID > 0,"Record should have been successfully inserted");
			List <Animal>  animals = loader.retrieveActiveAnimals("IMD");
			Iterator<Animal> it = animals.iterator();
			boolean found = false;
			while (it.hasNext()) {
				animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase(animalTag)) {
					found = true;
					break;
				}
			}
			assertTrue(found, "Tag 000 should have been found");
			assertEquals("Laal", animal.getAlias(), " Animal Alias should have been Laal");
			assertEquals("/assets/img/cow-thumbnails/000/1.png", animal.getFrontSideImageURL(), " Animal Front Pose Image URL should have been /assets/img/cow-thumbnails/000/1.png");
			animal.setAnimalTag(animalTag);
			String newStatus = Util.AnimalTypes.LCTPRGNT;
			String userID  = (String)Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.USER_ID);
			animal.setAnimalTypeCD(newStatus);
			animal.setUpdatedBy(new User(userID));
			animal.setUpdatedDTTM(DateTime.now());
			int result = loader.updateAnimalStatus(animal);
			assertEquals(1,result);
			
			AnimalBean animalBean = new AnimalBean();
			animalBean.setOrgID("IMD");
			animalBean.setAnimalTag(animalTag);

			animalBean.setDobFrom(Util.getDateInSQLFormat(animal.getDateOfBirth().minusDays(10)));
			animal = loader.retrieveMatchingAnimals(animalBean).get(0);
			assertEquals(animalTag,animal.getAnimalTag());

			animalBean.setDobFrom(null);
			animalBean.setDobTo(Util.getDateInSQLFormat(animal.getDateOfBirth().minusDays(10)));
			assertTrue(loader.retrieveMatchingAnimals(animalBean).isEmpty());

			animalBean.setDobFrom(Util.getDateInSQLFormat(animal.getDateOfBirth().minusDays(10)));
			animalBean.setDobTo(Util.getDateInSQLFormat(animal.getDateOfBirth().plusDays(10)));
			animal = loader.retrieveMatchingAnimals(animalBean).get(0);
			assertEquals(animalTag,animal.getAnimalTag());

			animalBean.setDobFrom(null);
			animalBean.setDobTo(null);
			animalBean.setDateOfBirthStr(null);
			float currentAgeInMonths = (Util.getDaysBetween(now,animal.getDateOfBirth()))/30.5f;
			
			animalBean.setAgeInMonthsFrom((currentAgeInMonths * 3f) + "");
			animalBean.setAgeInMonthsTo((currentAgeInMonths / 3f) + "");
			animalBean.setDobTo(Util.getDateInSQLFormat(animal.getDateOfBirth().plusDays(10)));
			//IMDLogger.loggingMode = Util.INFO;
			assertFalse(loader.retrieveMatchingAnimals(animalBean).isEmpty());
			animal = loader.retrieveMatchingAnimals(animalBean).get(0);
			assertEquals(animalTag,animal.getAnimalTag());
			
			animalBean.setAgeInMonthsFrom(null);
			animalBean.setAgeInMonthsTo(null);
			animalBean.setDobFrom(null);
			animalBean.setDobTo(null);
			animalBean.setDateOfBirthStr(null);
			
			animals = loader.retrieveActiveLactatingAnimals(animalBean.getOrgID());
			it = animals.iterator();
			found = false;
			while (it.hasNext()) {
				animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase(animalTag)) {
					found = true;
					break;
				}
			}
			assertTrue(found, "Tag " + animalTag + " is a lactating cow so it should have been found by retrieveActiveLactatingAnimals API");
			assertTrue(animal.isPregnant(), "Tag " + animalTag + " has the status " + animal.getStatusIndicators() + " and should be considered pregnant");
			assertEquals(0,animal.getPhotoCount().intValue());
			
			loader.deleteAnimal("IMD", animalTag);

			animal.setHerdLeavingDate(now); // mark inactive
			transactionID = loader.insertAnimal(animal);
			assertEquals(1,transactionID);

			animal = loader.retrieveMatchingAnimals(animalBean).get(0);
			assertEquals(animalTag,animal.getAnimalTag());

			animals = loader.retrieveActiveLactatingAnimals(animalBean.getOrgID());
			it = animals.iterator();
			found = false;
			while (it.hasNext()) {
				animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase(animalTag)) {
					found = true;
					break;
				}
			}
			assertFalse(found, "Tag " + animalTag + " was marked inactive it should NOT have been found by retrieveActiveLactatingAnimals API");			
			assertEquals(1,loader.deleteAnimal("IMD", animalTag));
			
			animal = createTestAnimal(animalTag);
			animal.setAnimalType("BULL");
			transactionID = loader.insertAnimal(animal);
			assertEquals(1,transactionID);
			animals = loader.retrieveActiveLactatingAnimals(animalBean.getOrgID());
			it = animals.iterator();
			found = false;
			while (it.hasNext()) {
				animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase(animalTag)) {
					found = true;
					break;
				}
			}
			assertFalse(found, "Tag " + animalTag + " is NOT a lactating cow so it should NOT have been found by retrieveActiveLactatingAnimals API");
			
			animal = loader.retrieveMatchingAnimals(animalBean).get(0);
			assertEquals("000",animal.getAnimalTag());
			animalBean.setAnimalType("DUMMY12");
			assertEquals(0,loader.retrieveMatchingAnimals(animalBean).size());
			AnimalBean searchBean = new AnimalBean();
			searchBean.setOrgID("IMD");
			searchBean.setGender('F');
			animals = loader.retrieveDamOrSire(searchBean);
			it = animals.iterator();
			found = false;
			while (it.hasNext()) {
				animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase("000")) {
					found = true;
					break;
				}
			}
			assertTrue(found, "Tag " + animalTag + " should have been found as a Dam");
			searchBean.setOrgID("IMD");
			searchBean.setGender('M');
			animals = loader.retrieveDamOrSire(searchBean);
			it = animals.iterator();
			found = false;
			while (it.hasNext()) {
				animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase(animalTag)) {
					found = true;
				}
			}
			assertFalse(found, "Tag " + animalTag + " should NOT have been found as a Sire");
			
			int transactionId  = loader.deleteAnimal("IMD", animalTag);
			assertEquals(1,transactionId);
			
			animals = loader.retrieveAllAnimals("IMD");
			String animalTags = "";
			it = animals.iterator();
			found = false;
			while (it.hasNext()) {
				animal = it.next();
				assertTrue(animalTags.indexOf("[" + animal.getAnimalTag() + "]") < 0, "Duplicate records Found for " + animal.getAnimalTag());
				animalTags += "[" + animal.getAnimalTag() + "]";
			}			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed." + e.getMessage());
		}
	}
	
	
	
	@Test
	void testSireProcessing() {
		try {
			String sireTag = "-999";
			String damTag = "-998";
			String alias  = "Tester";
			String breed = Util.Breed.BROWNSWISS;
			String semenInd = "Y";
			String recordUrl = "https://www.google.com/";
			String photoUrl = "https://www.google.com/photo.png";
			String controller = "IMD";
			String semenCompany = "IMD";
			Float currentSexListPrice = 7500.0f;
			Float discountSexPercentage = 0.35f;
			Float currentConventionalListPrice = 1000.0f;
			Float discountConventionalPercentage = 0.35f;
			User user = new User("TEST");


			SireBean sireBean = new SireBean();
			sireBean.setAnimalTag(sireTag);
			sireBean.setAlias(alias);
			sireBean.setBreed(breed);
			sireBean.setSemenInd(semenInd);
			sireBean.setRecordURL(recordUrl);
			sireBean.setPhotoURL(photoUrl);
			sireBean.setController(controller);
			sireBean.setSemenCompany(semenCompany);
			sireBean.setCurrentSexListPrice(currentSexListPrice);
			sireBean.setDiscountSexPercentage(discountSexPercentage);
			sireBean.setCurrentConventionalListPrice(currentConventionalListPrice);
			sireBean.setDiscountConventionalPercentage(discountConventionalPercentage);

			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader evtLoader = new LifeCycleEventsLoader();


			assertTrue(evtLoader.deleteAnimalLifecycleEvents("IMD", sireTag)>=0);
			assertTrue(evtLoader.deleteAnimalLifecycleEvents("IMD", damTag)>=0);
			
			
			
			animalLoader.deleteAnimal("IMD", sireBean.getAnimalTag());

			DateTime now = DateTime.now();

			assertTrue(animalLoader.deleteSire(sireTag)>=0);
			int result = animalLoader.insertSire(sireBean, user.getUserId(), now, user.getUserId(), now);
			assertEquals(1,result);
			
			List <Sire>  sires = animalLoader.retrieveAISire();
			Iterator<Sire> it = sires.iterator();
			Sire sire = null;
			boolean found = false;
			while (it.hasNext()) {
				sire = it.next();
				if (sire.getAnimalTag().equalsIgnoreCase(sireTag)) {
					found = true;
					break;
				}
			}
			assertTrue(found, sireTag + " should have been found");
			assertTrue(sire.getCurrentConventionalListPrice()!=null);
			assertEquals("Y",sire.getSemenInd());
			assertEquals(sireTag, sire.getAnimalTag());
			assertEquals(0, sire.getSemenUsageCount().intValue());			
			assertEquals(0, sire.getSemenSuccessCount().intValue());			
			assertEquals(0, sire.getSemenFailureCount().intValue());			
			assertEquals(0, sire.getSemenTbdCount().intValue());
			

			LifecycleEvent inseminationEvent = new LifecycleEvent(controller,0,damTag,Util.LifeCycleEvents.INSEMINATE,
					user,DateTime.now(IMDProperties.getServerTimeZone()),
					user,DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEvent.setAuxField1Value(sireTag);
			inseminationEvent.setAuxField2Value(Util.NO.toUpperCase());
			inseminationEvent.setAuxField3Value(Util.TBD);
			inseminationEvent.setAuxField4Value(null);
			inseminationEvent.setEventOperator(new Person("KASHIF","KASHIF","KASHIF","KASHIF"));
			inseminationEvent.setEventTimeStamp(DateTime.now().minusMonths(6));
//			inseminationEvent.setCreatedBy(user);
//			inseminationEvent.setUpdatedBy(user);
//			inseminationEvent.setCreatedDTTM(DateTime.now());
//			inseminationEvent.setUpdatedDTTM(DateTime.now());
			
			evtLoader.deleteAnimalLifecycleEvents("IMD", sireTag);
			evtLoader.deleteAnimalLifecycleEvents("IMD", damTag);
			
			
			assertTrue(evtLoader.insertLifeCycleEvent(inseminationEvent) > 0);
			
			sires = animalLoader.retrieveAISire();
			it = sires.iterator();
			sire = null;
			found = false;
			while (it.hasNext()) {
				sire = it.next();
				if (sire.getAnimalTag().equalsIgnoreCase(sireTag)) {
					found = true;
					break;
				}
			}
			assertTrue(found, sireTag + " should have been found");			

			assertEquals(1, sire.getSemenUsageCount().intValue());
			assertEquals(0, sire.getSemenSuccessCount().intValue());	
			assertEquals(0, sire.getSemenFailureCount().intValue());
			assertEquals(1, sire.getSemenTbdCount().intValue());

			assertTrue(evtLoader.deleteAnimalLifecycleEvents("IMD", sireTag)>=0);
			assertEquals(1,evtLoader.deleteAnimalLifecycleEvents("IMD", damTag));
			assertEquals(1,animalLoader.deleteSire(sireTag));
			assertTrue(animalLoader.deleteAnimal("IMD", sireTag)>=0);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}
	}	
	
	@Test
	void testSireRetrieval() {
		try {
			AnimalLoader loader = new AnimalLoader();
			List <Sire>  sires = loader.retrieveAISire();
			Iterator<Sire> it = sires.iterator();
			Sire sire = null;
			boolean found = false;
			while (it.hasNext()) {
				sire = it.next();
				if (sire.getOrgID().equalsIgnoreCase("GBL") && sire.getAlias().equalsIgnoreCase("JUNIOR")) {
					found = true;
					break;
				}
			}
			assertTrue(found, "JUNIOR Bull should have been found");
			assertTrue(sire.getCurrentConventionalListPrice()!=null);
			assertEquals("Y",sire.getSemenInd());
			assertEquals("1HO10219", sire.getAnimalTag(), " Junior code should have been 1HO10219");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Sire Retrieval Failed.");
		}
	}
	
	
	@Test
	void testPregnantRetrieval() {
		try {
			Animal animal;
			animal = createTestAnimal("000");
			AnimalLoader loader = new AnimalLoader();
			loader.deleteAnimal("IMD", animal.getAnimalTag());
			animal.setAnimalType(Util.AnimalTypes.HFRPREGN);
			int transactionID = loader.insertAnimal(animal);
			assertTrue(transactionID > 0,"Record should have been successfully inserted");
			List <Animal>  animals = loader.retrieveActivePregnantAnimals("IMD");
			Iterator<Animal> it = animals.iterator();
			boolean found = false;
			while (it.hasNext()) {
				animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase("000")) {
					found = true;
					break;
				}
			}
			assertTrue(found, "Tag 000 should have been found");
			assertTrue(animal.isPregnant(), "Tag 000 should have been considered pregnant");
			int transactionId  = loader.deleteAnimal("IMD", "000");
			assertEquals(1,transactionId);
			
			animal.setAnimalType(Util.AnimalTypes.LCTPRGNT);
			transactionID = loader.insertAnimal(animal);
			assertTrue(transactionID > 0,"Record should have been successfully inserted");
			animals = loader.retrieveActivePregnantAnimals("IMD");
			it = animals.iterator();
			found = false;
			while (it.hasNext()) {
				animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase("000")) {
					found = true;
					break;
				}
			}
			assertTrue(found, "Tag 000 should have been found");
			transactionId  = loader.deleteAnimal("IMD", "000");
			assertEquals(1,transactionId);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}
	}		

	
	@Test
	void testUpdateAnimal() {
		int originalLoggingMode = IMDLogger.loggingMode;
		try {
			IMDLogger.loggingMode = Util.INFO;
			Animal animal;
			animal = createTestAnimal("-999");
			AnimalLoader loader = new AnimalLoader();
			loader.deleteAnimal(animal.getOrgID(), animal.getAnimalTag());
			animal.setAnimalTypeCD(Util.AnimalTypes.HFRPREGN);
			int transactionID = loader.insertAnimal(animal);
			assertTrue(transactionID > 0,"Record should have been successfully inserted");
			List <Animal>  animals = loader.retrieveSpecifiedAnimalTags(animal.getOrgID(),  "(" + animal.getAnimalTag() + ")");
			Iterator<Animal> it = animals.iterator();
			boolean found = false;
			Animal retrievedAnimal = null;
			while (it.hasNext()) {
				retrievedAnimal = it.next();
				if (retrievedAnimal.getOrgID().equalsIgnoreCase(animal.getOrgID()) && 
						retrievedAnimal.getAnimalTag().equalsIgnoreCase(animal.getAnimalTag())) {
					found = true;
					break;
				}
			}
			assertTrue(found, "Tag " + retrievedAnimal.getAnimalTag() + " should have been found");
			assertTrue(retrievedAnimal.isPregnant(), "Tag " + retrievedAnimal.getAnimalTag() + " should have been considered pregnant");
			assertEquals(animal.getAnimalTypeCD(),retrievedAnimal.getAnimalTypeCD());
			assertEquals(animal.getAlias(),retrievedAnimal.getAlias());
//			int transactionId  = loader.deleteAnimal(animal.getOrgID(), animal.getAnimalTag());
//			assertEquals(1,transactionId);
			
			
			Animal updatedAnimal = new Sire(animal.getOrgID(),animal.getAnimalTag());
			updatedAnimal.setOrgID(animal.getOrgID());
			updatedAnimal.setAnimalTag(animal.getAnimalTag());
			updatedAnimal.setDateOfBirth(animal.getDateOfBirth());
			updatedAnimal.setDateOfBirthEstimated(animal.isDateOfBirthEstimated());
			updatedAnimal.setPurchaseDate(animal.getPurchaseDate());
			updatedAnimal.setPurchaseCurrency(animal.getPurchaseCurrency());
			updatedAnimal.setPurchaseDate(animal.getPurchaseDate());
			updatedAnimal.setAlias(animal.getAlias());
			updatedAnimal.setAnimalSire(animal.getAnimalSire());
			updatedAnimal.setAnimalDam(animal.getAnimalDam());
			updatedAnimal.setWeight(animal.getWeight());
			updatedAnimal.setAnimalTypeCD(Util.AnimalTypes.LCTPRGNT);
			updatedAnimal.setUpdatedBy(animal.getUpdatedBy());
			updatedAnimal.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			transactionID = loader.updateAnimal(updatedAnimal);
			assertEquals(1,transactionID);
			Animal finalUpdatedAnimal = loader.getAnimalRawInfo(updatedAnimal).get(0);

			assertEquals(updatedAnimal.getAnimalTag(),finalUpdatedAnimal.getAnimalTag());
			assertTrue(finalUpdatedAnimal.isPregnant(), "Tag " + finalUpdatedAnimal.getAnimalTag() + " should have been considered pregnant");
			assertEquals(updatedAnimal.getAnimalTypeCD(),finalUpdatedAnimal.getAnimalTypeCD());
			assertEquals(updatedAnimal.getAlias(),finalUpdatedAnimal.getAlias());
			assertEquals(updatedAnimal.getGender(),finalUpdatedAnimal.getGender());
			
			int transactionId  = loader.deleteAnimal(animal.getOrgID(), animal.getAnimalTag());
			assertEquals(1,transactionId);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		} finally {
			IMDLogger.loggingMode = originalLoggingMode;
		}
	}		
	
	
	@Test
	void testAdultFemaleCowsRetrieval() {
		try {
			Animal animal1, animal2;
			animal1 = createTestAnimal("000");
			animal2 = createTestAnimal("-999");
			AnimalLoader loader = new AnimalLoader();
			LifeCycleEventsLoader eventLoader = new LifeCycleEventsLoader();
			User user = new User("KASHIF");

			loader.deleteAnimal(animal1.getOrgID(), animal1.getAnimalTag());
			loader.deleteAnimal(animal2.getOrgID(), animal2.getAnimalTag());
			eventLoader.deleteAnimalLifecycleEvents(animal1.getOrgID(), animal1.getAnimalTag());
			eventLoader.deleteAnimalLifecycleEvents(animal2.getOrgID(), animal2.getAnimalTag());

			animal1.setAnimalType(Util.AnimalTypes.HFRPREGN);
			animal2.setAnimalType(Util.AnimalTypes.HEIFER);
			animal1.setDateOfBirth(DateTime.now().minusDays(2*365)); // 2 years of age
			animal2.setDateOfBirth(DateTime.now().minusDays(240)); // 8 months old
			assertTrue(loader.insertAnimal(animal1) > 0, animal1.getAnimalTag() + " should have been successfully inserted");
			assertTrue(loader.insertAnimal(animal2) > 0, animal1.getAnimalTag() + " should have been successfully inserted");
			
			
			LifecycleEvent event3 = new LifecycleEvent("IMD", 0, "000",Util.LifeCycleEvents.PARTURATE,user,DateTime.now(),user,DateTime.now());
			event3.setEventTimeStamp(DateTime.now());
			event3.setEventOperator(new Person("EMP000'", "Kashif", "", "Manzoor"));
			event3.setCreatedBy(new User("KASHIF"));
			event3.setCreatedDTTM(DateTime.now());
			event3.setUpdatedBy(event3.getCreatedBy());
			event3.setUpdatedDTTM(event3.getCreatedDTTM());
			event3.setEventNote("Parturition");
			eventLoader.insertLifeCycleEvent(event3);

			LifecycleEvent event1 = new LifecycleEvent("IMD", 0, "000",Util.LifeCycleEvents.ABORTION,user,DateTime.now(),user,DateTime.now());
			event1.setEventTimeStamp(DateTime.now());
			event1.setEventOperator(new Person("EMP000'", "Kashif", "", "Manzoor"));
			event1.setCreatedBy(new User("KASHIF"));
			event1.setCreatedDTTM(DateTime.now());
			event1.setUpdatedBy(event1.getCreatedBy());
			event1.setUpdatedDTTM(event1.getCreatedDTTM());
			event1.setEventNote("Parturition");
			eventLoader.insertLifeCycleEvent(event1);
			
			List <Animal>  animals = loader.retrieveAdultFemaleCows("IMD",270);
			Iterator<Animal> it = animals.iterator();
			boolean shouldBeFound = false;
			boolean shouldNotBeFound = false;
			while (it.hasNext()) {
				Animal animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase("000")) {
					shouldBeFound = true;
					assertTrue(animal.getStatusIndicators() != null);
					assertEquals(2,animal.getParturationCount());
				}
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase("-999")) {
					shouldNotBeFound = true;
				}
			}
			assertTrue(shouldBeFound, "Tag 000 should have been found as it is an adult female");
			assertFalse(shouldNotBeFound, "Tag -999 should NOT have been found as it is NOT an adult female");
			assertEquals(1,loader.deleteAnimal("IMD", "000"));
			assertEquals(1,loader.deleteAnimal("IMD", "-999"));
			assertEquals(2,eventLoader.deleteAnimalLifecycleEvents("IMD", "000"));
			assertEquals(0,eventLoader.deleteAnimalLifecycleEvents("IMD", "-999"));

		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}
	}		
	
	@Test
	void testHeifersRetrieval() {
		try {
			Animal animal = createTestAnimal("-999");
			AnimalLoader loader = new AnimalLoader();
			loader.deleteAnimal(animal.getOrgID(), animal.getAnimalTag());
			animal.setAnimalType(Util.AnimalTypes.HEIFER);
			int transactionID = loader.insertAnimal(animal);
			assertTrue(transactionID > 0,"Record should have been successfully inserted");
			List <Animal>  animals = loader.retrieveActiveHeifers("IMD");
			Iterator<Animal> it = animals.iterator();
			boolean found = false;
			while (it.hasNext()) {
				Animal anml = it.next();
				if (anml.getOrgID().equalsIgnoreCase(animal.getOrgID()) && anml.getAnimalTag().equalsIgnoreCase(animal.getAnimalTag())) {
					found = true;
					break;
				}
			}
			assertTrue(found, animal.getAnimalTag()+ " should have been found");
			int transactionId  = loader.deleteAnimal(animal.getOrgID(), animal.getAnimalTag());
			assertEquals(1,transactionId);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}
	}
	@Test
	void testFemaleCalvesRetrieval() {
		try {
			Animal animal = createTestAnimal("-999");
			AnimalLoader loader = new AnimalLoader();
			loader.deleteAnimal(animal.getOrgID(), animal.getAnimalTag());
			animal.setAnimalType(Util.AnimalTypes.FEMALECALF);
			int transactionID = loader.insertAnimal(animal);
			assertTrue(transactionID > 0,"Record should have been successfully inserted");
			List <Animal>  animals = loader.retrieveActiveFemaleCalves(animal.getOrgID());
			Iterator<Animal> it = animals.iterator();
			boolean found = false;
			while (it.hasNext()) {
				Animal animal1 = it.next();
				if (animal1.getOrgID().equalsIgnoreCase(animal.getOrgID()) && animal1.getAnimalTag().equalsIgnoreCase(animal.getAnimalTag())) {
					found = true;
					break;
				}
			}
			assertTrue(found, animal.getAnimalTag() + " should have been found");
			int transactionId  = loader.deleteAnimal(animal.getOrgID(), animal.getAnimalTag());
			assertEquals(1,transactionId);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}
	}
	
	@Test
	void testAnimalPhotos() {
		try {
			Animal animal1 = createTestAnimal("-999");

			AnimalPhoto photo = new AnimalPhoto();
			photo.setOrgID(animal1.getOrgID());
			photo.setAnimalTag(animal1.getAnimalTag());
			photo.setPhotoID("" + DateTime.now().getMillis());
			photo.setPhotoURI(Util.ANIMAL_PHOTO_UPLOAD_PATH + "/" + photo.getAnimalTag() + "/" + photo.getPhotoID() + ".png");
			photo.setComments("Test photo to be deleted");
			photo.setPhotoTimeStamp(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(100));
			photo.setCreatedBy(animal1.getCreatedBy());
			photo.setCreatedDTTM(animal1.getCreatedDTTM());
			photo.setUpdatedBy(photo.getCreatedBy());
			photo.setUpdatedDTTM(photo.getCreatedDTTM());
			
			
			AnimalLoader loader = new AnimalLoader();
			assertTrue(loader.deleteAllAnimalPhotos(animal1.getOrgID(), animal1.getAnimalTag()) >= 0);
			assertTrue(loader.deleteAnimal(animal1.getOrgID(), animal1.getAnimalTag()) >= 0);
			
			assertEquals(1,loader.insertAnimal(animal1));
			assertEquals(1,loader.insertAnimalPhoto(photo));
			photo.setPhotoID("" + DateTime.now().minusDays(1).getMillis());
			photo.setPhotoURI(Util.ANIMAL_PHOTO_UPLOAD_PATH + "/" + photo.getAnimalTag() + "/" + photo.getPhotoID() + " .png");
			assertEquals(1,loader.insertAnimalPhoto(photo));
			
			List<AnimalPhoto> photos = loader.retrieveAnimalPhotos(animal1.getOrgID(), animal1.getAnimalTag());
			assertEquals(2,photos.size());

			assertEquals(2,loader.deleteAllAnimalPhotos(animal1.getOrgID(), animal1.getAnimalTag()));
			assertEquals(1,loader.deleteAnimal(animal1.getOrgID(), animal1.getAnimalTag()));
						
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Photo CRUD operation(s) Failed.");
		}
	}
	
	@Test
	void testRetrieveAnimalProgney() {
		try {
			Animal animal1 = createTestAnimal("-999");
			Animal animal2 = createTestAnimal("-998");
			Dam animalDam = (Dam)createTestAnimal("-997");
			animal1.setAnimalDam(animalDam);
			animal2.setAnimalDam(animalDam);
			AnimalLoader loader = new AnimalLoader();
			loader.deleteAnimal(animal1.getOrgID(), animal1.getAnimalTag());
			loader.deleteAnimal(animal2.getOrgID(), animal2.getAnimalTag());
			loader.deleteAnimal(animalDam.getOrgID(), animalDam.getAnimalTag());
			animal1.setAnimalType(Util.AnimalTypes.FEMALECALF);
			animal2.setAnimalType(Util.AnimalTypes.MALECALF);
			animal1.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(24));
			animal2.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(12));
			animalDam.setAnimalType(Util.AnimalTypes.LCTPOSTPAR);
			animalDam.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(55));
			assertTrue(loader.insertAnimal(animalDam) > 0);
			assertTrue(loader.insertAnimal(animal1) > 0);
			assertTrue(loader.insertAnimal(animal2) > 0);
			List <Animal>  animals = loader.retrieveSpecifiedAnimalProgney(animalDam.getOrgID(), animalDam.getAnimalTag());
			assertEquals(2,animals.size());
			assertEquals(animal2.getAnimalTag(),animals.get(0).getAnimalTag());
			assertEquals(animal1.getAnimalTag(),animals.get(1).getAnimalTag());

			assertEquals(1,loader.deleteAnimal(animal1.getOrgID(), animal1.getAnimalTag()));
			assertEquals(1,loader.deleteAnimal(animal2.getOrgID(), animal2.getAnimalTag()));
			assertEquals(1,loader.deleteAnimal(animalDam.getOrgID(), animalDam.getAnimalTag()));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}
	}		
	@Test
	void testRetrieveAnimalsMilkedAtSpecificMilkingEvent() {
		
		int originalLoggingMode = IMDLogger.loggingMode;
		
		try {
			
			IMDLogger.loggingMode = Util.INFO;
			Animal animal1 = createTestAnimal("-997");
			Animal animal2 = createTestAnimal("-998");
			Animal animal3 = createTestAnimal("-999");
			animal1.setAnimalType(Util.AnimalTypes.DRYPREG);
			animal2.setAnimalType(Util.AnimalTypes.DRYPREG);
			animal3.setAnimalType(Util.AnimalTypes.DRYPREG);

			AnimalLoader loader = new AnimalLoader();
			
			assertTrue(loader.deleteAnimal(animal1.getOrgID(), animal1.getAnimalTag()) >=0);
			assertTrue(loader.deleteAnimal(animal2.getOrgID(), animal2.getAnimalTag()) >=0);
			assertTrue(loader.deleteAnimal(animal3.getOrgID(), animal3.getAnimalTag()) >=0);
			
			
			LocalDate pastMilkDate = new LocalDate(1900,1,1, LocalDate.now(IMDProperties.getServerTimeZone()).getChronology());
			
			MilkingDetailLoader milkLoader = new MilkingDetailLoader();
			assertTrue(milkLoader.deleteMilkingRecordOfaDay(animal1.getOrgID(), animal1.getAnimalTag(), pastMilkDate) >= 0);
			assertTrue(milkLoader.deleteMilkingRecordOfaDay(animal2.getOrgID(), animal2.getAnimalTag(), pastMilkDate) >= 0);
			assertTrue(milkLoader.deleteMilkingRecordOfaDay(animal3.getOrgID(), animal3.getAnimalTag(), pastMilkDate) >= 0);
			AnimalSrvc anmlSrvc = new AnimalSrvc();
			MilkingInformationSrvc milkingSrvc = new MilkingInformationSrvc();
			MilkingDetailBean milkBean = new MilkingDetailBean();
			milkBean.setMilkingDateStr(pastMilkDate.toString());
			milkBean.setMilkingTimeStr("4:00");
			milkBean.setMilkingEventNumber((short)1);			
			String responseStr = milkingSrvc.retrieveLactatingAnimalsMilkRecord(milkBean).getEntity().toString();

			//assertFalse(responseStr.contains("\"message\":\"No matching record found\""));
			
			assertEquals(1,loader.insertAnimal(animal1));
			assertEquals(1,loader.insertAnimal(animal2));
			assertEquals(1,loader.insertAnimal(animal3));
			

			
			MilkingDetail milkingRecord = createMilkingRecord(animal1.getAnimalTag(), pastMilkDate, new LocalTime(4,0,0));
			milkingRecord.setComments("Morning Milking");
			milkingRecord.setMilkingEventNumber((short) 1);

			assertEquals(1,milkLoader.insertMilkRecord(milkingRecord.getMilkingDetailBean()));
			List <Animal>  animals = loader.retrieveAnimalsMilkedAtSpecificMilkingEvent(animal1.getOrgID(),pastMilkDate,1);
			assertEquals(1,animals.size());
			assertEquals(animal1.getAnimalTag(),animals.get(0).getAnimalTag());
			
			milkingRecord.setAnimalTag(animal2.getAnimalTag());
			assertEquals(1,milkLoader.insertMilkRecord(milkingRecord.getMilkingDetailBean()));
			animals = loader.retrieveAnimalsMilkedAtSpecificMilkingEvent(animal2.getOrgID(),pastMilkDate,1);
			assertEquals(2,animals.size());
			assertEquals(animal1.getAnimalTag(),animals.get(0).getAnimalTag());
			assertEquals(animal2.getAnimalTag(),animals.get(1).getAnimalTag());
			
			milkingRecord.setAnimalTag(animal3.getAnimalTag());
			assertEquals(1,milkLoader.insertMilkRecord(milkingRecord.getMilkingDetailBean()));
			animals = loader.retrieveAnimalsMilkedAtSpecificMilkingEvent(animal3.getOrgID(),pastMilkDate,1);
			assertEquals(3,animals.size());
			assertEquals(animal1.getAnimalTag(),animals.get(0).getAnimalTag());
			assertEquals(animal2.getAnimalTag(),animals.get(1).getAnimalTag());
			assertEquals(animal3.getAnimalTag(),animals.get(2).getAnimalTag());
			 
			responseStr = milkingSrvc.retrieveLactatingAnimalsMilkRecord(milkBean).getEntity().toString();

			milkBean.setMilkingDateStr(LocalDate.now(IMDProperties.getServerTimeZone()).toString());
			responseStr = milkingSrvc.retrieveLactatingAnimalsMilkRecord(milkBean).getEntity().toString();
			
			assertFalse(responseStr.contains("\"animalTag\":\"" + animal1.getAnimalTag() + "\""));
			assertFalse(responseStr.contains("\"animalTag\":\"" + animal1.getAnimalTag() + "\""));
			assertFalse(responseStr.contains("\"animalTag\":\"" + animal1.getAnimalTag() + "\""));
			
			

			assertEquals(1,milkLoader.deleteMilkingRecordOfaDay(animal1.getOrgID(), animal1.getAnimalTag(), pastMilkDate));
			assertEquals(1,milkLoader.deleteMilkingRecordOfaDay(animal2.getOrgID(), animal2.getAnimalTag(), pastMilkDate));
			assertEquals(1,milkLoader.deleteMilkingRecordOfaDay(animal3.getOrgID(), animal3.getAnimalTag(), pastMilkDate));
			
			assertEquals(1,loader.deleteAnimal(animal1.getOrgID(), animal1.getAnimalTag()));
			assertEquals(1,loader.deleteAnimal(animal2.getOrgID(), animal2.getAnimalTag()));
			assertEquals(1,loader.deleteAnimal(animal3.getOrgID(), animal3.getAnimalTag()));
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception in retrieveAnimalsMilkedAtSpecificMilkingEvent");
		} finally {
			IMDLogger.loggingMode = originalLoggingMode;
		}
		
	}		
}




