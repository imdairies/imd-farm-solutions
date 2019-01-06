package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.BankDetails;
import com.imd.dto.Contact;
import com.imd.dto.Dam;
import com.imd.dto.MilkingDetail;
import com.imd.dto.Note;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.services.bean.AnimalBean;
import com.imd.util.IMDException;
import com.imd.util.MessageManager;
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

	public Animal createTestAnimalTag000() throws Exception {
		Dam c000 = new Dam(/*orgid*/"IMD",/*tag*/"000",/*dob*/DateTime.parse("2014-02-09"),/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		c000.setAlias("Laal");
		c000.setAnimalType("LACTATING");
		c000.setAnimalStatus(Util.ANIMAL_STATUS.ACTIVE);
		c000.setFrontImageURL("/assets/img/cow-thumbnails/000/1.png");
		c000.setBacksideImageURL("/assets/img/cow-thumbnails/000/2.png");
		c000.setRightSideImageURL("/assets/img/cow-thumbnails/000/3.png");
		c000.setLeftSideImageURL("/assets/img/cow-thumbnails/000/4.png");
		c000.setMilkingAverageAtPurchase(new MilkingDetail(/*milk freq*/(short)3, /*machine milked*/true, /*record date*/LocalDate.parse("2017-02-08"), 
				/*record time*/LocalTime.parse("18:00:00"), /*milk vol*/27.0f, (short)1));
		c000.setPurchaseDate(DateTime.parse("2017-02-08"));
		c000.setCreatedBy(new User("KASHIF"));
		c000.setCreatedDTTM(DateTime.now());
		c000.setUpdatedBy(c000.getCreatedBy());
		c000.setUpdatedDTTM(c000.getCreatedDTTM());
		setPurchaseFromContact(c000);
		setSireInformation(c000);
		c000.setAnimalDam(null);
		Note newNote = new Note (1,"Had four adult teeth at purchase. Dark brown/red shade in the coat. Shy of people, docile, keeps away from humans, hangs out well with other cows, medium built.", LocalDateTime.now());		
		c000.addNote(newNote);
		setMilkingRecord(c000);
		return c000;
	}

	private void setMilkingRecord(Dam c000) throws IMDException {
		MilkingDetail dailyMilking;
		short milkFreq = 3;
		int milkingHr = 5;
		int milkingMin = 0;
		int milkingSec = 0;
		LocalTime milkingTime = LocalTime.of(milkingHr,milkingMin,milkingSec);
		LocalDate milkingDate = LocalDate.of(2018,2,14);
		float milkingVol = 7.0f;
		boolean isMachineMilked = true;		
		dailyMilking =  new MilkingDetail(milkFreq,isMachineMilked,milkingDate,milkingTime,milkingVol,(short)1);
		c000.addToMilkingRecord(dailyMilking);
	}

	private void setSireInformation(Animal c000) throws IMDException {
		Sire sire = new Sire("IMD","NLDM000291306935", DateTime.parse("2000-02-10"), false, 0d, "PKR");
		sire.setAlias("MANDERS MARIUS");
		sire.setMarketedByCompany(new Contact("Not known"));
		Contact company = new Contact("CRV");
		company.setWebURI(URI.create("https://www.crv4all-international.com/find-bull/"));
		sire.setOwnerCompany(company);
		sire.addNote(new Note(1,"Not sure if this is truly the sire. Got minimal'\"%\n information of the sire of 026 from Babar Hameed Jathol", LocalDateTime.now()));
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
	
	private void loadMessagesForAllSupportedLanguages() {
		String rootPath = "resources" + File.separatorChar + "IMDMessages_UR.properties";
		Properties appProps = new Properties();
		try {
			appProps.load(new FileInputStream(rootPath));
			MessageManager.loadMessages("UR", appProps);
			rootPath = "resources" + File.separatorChar + "IMDMessages_EN.properties";
			appProps = new Properties();	
			appProps.load(new FileInputStream(rootPath));
			MessageManager.loadMessages("EN", appProps);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	 
	@Test
	void testAnimalProcessing() {
		try {
			Animal animal;
			animal = createTestAnimalTag000();
			AnimalLoader loader = new AnimalLoader();
			loader.deleteAnimal("IMD", animal.getAnimalTag());
			DateTime now = DateTime.now();
			animal.setDateOfBirth(now.minusDays(1));
			assertEquals(0,animal.getCurrentAge().getYears(), " The current age should be less than a year");
			assertEquals(0,animal.getCurrentAge().getMonths(), " The current age should be less than a month");
			assertEquals(1,animal.getCurrentAge().getDays(), " The current age should be one day");
			int transactionID = loader.insertAnimal(animal);
			assertTrue(transactionID > 0,"Record should have been successfully inserted");
			List <Animal>  animals = loader.retrieveActiveAnimals("IMD");
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
			assertEquals("Laal", animal.getAlias(), " Animal Alias should have been Laal");
			assertEquals("/assets/img/cow-thumbnails/000/1.png", animal.getFrontImageURL(), " Animal Front Pose Image URL should have been /assets/img/cow-thumbnails/000/1.png");
			AnimalBean animalBean = new AnimalBean();
			animalBean.setOrgID("IMD");
			animalBean.setAnimalTag("000");
			animal = loader.retrieveMatchingAnimals(animalBean).get(0);
			assertEquals("000",animal.getAnimalTag());

			animals = loader.retrieveActiveLactatingAnimals(animalBean);
			it = animals.iterator();
			found = false;
			while (it.hasNext()) {
				animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase("000")) {
					found = true;
					break;
				}
			}
			assertTrue(found, "Tag 000 is a lactating cow so it should have been found by retrieveActiveLactatingAnimals API");

			loader.deleteAnimal("IMD", animal.getAnimalTag());
			animal = createTestAnimalTag000();
			animal.setAnimalType("BULL");
			transactionID = loader.insertAnimal(animal);
			animals = loader.retrieveActiveLactatingAnimals(animalBean);
			it = animals.iterator();
			found = false;
			while (it.hasNext()) {
				animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase("000")) {
					found = true;
					break;
				}
			}
			assertFalse(found, "Tag 000 is NOT a lactating cow so it should NOT have been found by retrieveActiveLactatingAnimals API");
			
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
			assertTrue(found, "Tag 000 should have been found as a Dam");
			searchBean.setOrgID("IMD");
			searchBean.setGender('M');
			animals = loader.retrieveDamOrSire(searchBean);
			it = animals.iterator();
			found = false;
			while (it.hasNext()) {
				animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase("000")) {
					found = true;
				}
			}
			assertFalse(found, "Tag 000 should NOT have been found as a Sire");
			
			int transactionId  = loader.deleteAnimal("IMD", "000");
			assertEquals(1,transactionId);
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
			assertEquals("1HO10219", sire.getAnimalTag(), " Junior code should have been 1HO10219");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Sire Retrieval Failed.");
		}
	}		

}
