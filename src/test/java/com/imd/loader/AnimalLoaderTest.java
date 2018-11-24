package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
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

	public Animal createTag026() throws IMDException {
		Dam c026 = new Dam(/*id*/"026",/*tag*/"026",/*dob*/DateTime.parse("2014-02-09"),/*dob estimated*/true,/*price*/331000,/*price currency*/"Rs.");
		c026.setAlias("Laal");
		c026.setOrgID("IMD");
		c026.setAnimalStatus(Util.ANIMAL_STATUS.ACTIVE);
		c026.setMilkingAverageAtPurchase(new MilkingDetail(/*milk freq*/(short)3, /*machine milked*/true, /*record date*/LocalDate.parse("2017-02-08"), 
				/*record time*/LocalTime.parse("18:00:00"), /*milk vol*/27.0f, (short)1));
		c026.setPurchaseDate(DateTime.parse("2017-02-08"));
		c026.setCreatedBy(new User("KASHIF"));
		c026.setCreatedDTTM(DateTime.now());
		c026.setUpdatedBy(c026.getCreatedBy());
		c026.setUpdatedDTTM(c026.getCreatedDTTM());
		setPurchaseFromContact(c026);
		setSireInformation(c026);
		c026.setDamInformation(null);
		Note newNote = new Note (1,"Had four adult teeth at purchase. Dark brown/red shade in the coat. Shy of people, docile, keeps away from humans, hangs out well with other cows, medium built.", LocalDateTime.now());		
		c026.addNote(newNote);
		setMilkingRecord(c026);
		return c026;
	}

	private void setMilkingRecord(Dam c026) throws IMDException {
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
		c026.addToMilkingRecord(dailyMilking);
	}

	private void setSireInformation(Animal c026) throws IMDException {
		Sire sire = new Sire("IMD","NLDM000291306935", DateTime.parse("2000-02-10"), false, 0d, "PKR");
		sire.setAlias("MANDERS MARIUS");
		sire.setMarketedByCompany(new Contact("Not known"));
		Contact company = new Contact("CRV");
		company.setWebURI(URI.create("https://www.crv4all-international.com/find-bull/"));
		sire.setOwnerCompany(company);
		sire.addNote(new Note(1,"Not sure if this is truly the sire. Got minimal information of the sire of 026 from Babar Hameed Jathol", LocalDateTime.now()));
		c026.setAnimalSire(sire);
	}

	private void setPurchaseFromContact(Animal c026) throws IMDException {
		Contact contact = new Contact("Babar", "Hameed", "Jathol");
		contact.setNamePrefix("Mr.");
		
		// Address related tests
		contact.setAddress("Jathol Farm", "25 Km from Jaran Wala","", "Joray Khoo", "Punjab", "Pakistan", null);
		contact.setAddressInstructions("Take motorway from Lahore to Islamabad and take the first exit and continue straight on that road for 25 km. Then take left.");
		
		// Test for bank details
		BankDetails bankDetails;
		bankDetails = new BankDetails("JATHOL AGRI & DAIRY", "Habib Bank Limited", "09977900302603", "HABB0009977900302603");
		contact.setBankAccountInformation(bankDetails);		
		c026.setPurchaseFrom(contact);
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
			// 1: Insert a new Animal.
			Animal animal;
			animal = createTag026();
			AnimalLoader loader = new AnimalLoader();
			int transactionID = loader.insertAnimal(animal);
			assertTrue(transactionID > 0,"Record should have been successfully inserted");
			List <Animal>  animals = loader.retrieveActiveAnimals("IMD");
			Iterator<Animal> it = animals.iterator();
			boolean found = false;
			while (it.hasNext()) {
				animal = it.next();
				if (animal.getOrgID().equalsIgnoreCase("IMD") && animal.getAnimalTag().equalsIgnoreCase("026")) {
					found = true;
				}
			}
			assertTrue(found, "Tag 026 should have been found");
			animal = loader.retrieveAnimal("IMD", "026");
			assertEquals("026",animal.getAnimalTag());
			int transactionId  = loader.deleteAnimal("IMD", "026");
			assertEquals(1,transactionId);
			animal = loader.retrieveAnimal("IMD", "026");
			assertEquals(null,animal);					
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}
	}	

}
