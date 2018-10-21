package com.imd.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.URI;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.Contact;
import com.imd.dto.Dam;
import com.imd.dto.MilkingDetail;
import com.imd.dto.Note;
import com.imd.dto.Sire;
import com.imd.util.IMDException;
import com.imd.util.ParseMilkingRecord;

class DamTest {

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

	@Test
	void testCreation() {

		Dam c026 = null;
		Dam c017 = null;
		
		try {
			c026 = new Dam("","026",LocalDate.parse("2014-02-09"),false,0,"Rs.");
		} catch (IMDException e) {
			assertTrue(e.getMessage().contains("One or more values contain null."), "Null values check failed");
		}
		try {
			c026 = new Dam(null);
		} catch (IMDException e) {
			assertTrue(e.getMessage().contains("Tag Number can't be null or empty"), "Null values check failed");
		}
		
		try {
			c026 = new Dam("026","026",LocalDate.parse("2014-02-09"),false,0,"Rs.");
			c026.setPurchaseFrom(new Contact("Babar",null, null));
			
			// Verify purchase date is properly set.
			c026.setPurchaseDate(LocalDate.parse("2017-02-08"));
			assertEquals(2,c026.getAgeAtPurchase().getYears(), " Years do not match");	
			assertEquals(11,c026.getAgeAtPurchase().getMonths(), " Month do not match");
			assertEquals(30,c026.getAgeAtPurchase().getDays(), " Days do not match");


			// Verify that milking records are correctly handled
			c026.addToMilkingRecord((MilkingDetail)ParseMilkingRecord.readMilkingRecordsFromSMS("026{12", LocalDate.now(),(short)3,new Integer(1)).get("026"));
			c026.addToMilkingRecord((MilkingDetail)ParseMilkingRecord.readMilkingRecordsFromSMS("026{11.5", LocalDate.now(),(short)3,new Integer(2)).get("026"));
			c026.addToMilkingRecord((MilkingDetail)ParseMilkingRecord.readMilkingRecordsFromSMS("026{11", LocalDate.now(),(short)3,new Integer(3)).get("026"));
			
			assertTrue(c026.getMilkingEvent(LocalDate.now(), new Integer(1)).getMilkVolume()==12.0f," Milk volume should be 12");
			assertTrue(c026.getMilkingEvent(LocalDate.now(),new Integer(2)).getMilkVolume()==11.5f," Milk volume should be 11.5");
			assertTrue(c026.getMilkingEvent(LocalDate.now(),new Integer(3)).getMilkVolume()==11.0f," Milk volume should be 11");
			assertTrue(c026.getDailyMilkTotal(LocalDate.now())==34.5f, " Daily milk total should be 34.5");
			
			// Verify that Sire detail is correctly handled
			Sire sire = new Sire("", "NLDM000291306935", LocalDate.parse("2000-02-10"), false, 0, "Rs.");
			sire.setAlias("MANDERS MARIUS");
			sire.setMarketedByCompany(new Contact("Not known"));
			Contact company = new Contact("CRV");
			company.setWebURI(URI.create("https://www.crv4all-international.com/find-bull/"));
			sire.setOwnerCompany(company);
			sire.addNote(new Note(1,"Not sure if this is truly the sire. Got minimal information of the sire of 026 from Babar Hameed Jathol",LocalDateTime.now()));
			c026.setAnimalSire(sire);
			c026.setAnimalDam(null);
			// Verify that the age is properly calculated
			Period age = Period.between(LocalDate.parse("2014-02-09"),LocalDate.now());
			assertTrue(c026.getCurrentAge().equals(age), " Age mismatched was expecting " + age + " got " + c026.getCurrentAge().toString() + " instead");
			assertTrue(c026.getAnimalSire().getTagID().equals("NLDM000291306935"),"Sire Tag does not match");
			
			// Verify that notes are properly added.
			c026.addNote(new Note(1,"Had four adult teeth at purchase. Dark brown/red shade in the coat. Shy of people, docile, keeps away from humans, hangs out well with other cows, medium built."));
			c026.addNote(new Note(2,"Alhumdullilah, First successful insemination of our farm, second parturation of the farm and first femal parturation of the farm"));
			assertTrue(c026.getNotesCount()==2, " Expected to see 2 notes but got " + c026.getNotesCount());
			
			// Verify that the life time totals and averages are properly calculated.
			assertTrue(c026.getLifetimeMilkTotals().getMilkVolume()==34.5f," Total was expected to be 34.5 but got " + 
					c026.getLifetimeMilkTotals().getMilkVolume());
			assertTrue(c026.getLifetimeMilkTotals().getMilkingEventNumber()==1," Total was expected to be 34.5 but got " + 
					c026.getLifetimeMilkTotals().getMilkingEventNumber());
			assertTrue(c026.getLifeTimeDailyMilkingAverage()==34.5f," Average was expected to be 34.5 but got " + 
					c026.getLifeTimeDailyMilkingAverage());					
			c026.addToMilkingRecord((MilkingDetail)ParseMilkingRecord.readMilkingRecordsFromSMS("026{12", LocalDate.parse("2018-02-15"),(short)3,new Integer(1)).get("026"));
			c026.addToMilkingRecord((MilkingDetail)ParseMilkingRecord.readMilkingRecordsFromSMS("026{12", LocalDate.parse("2018-02-15"),(short)3,new Integer(2)).get("026"));
			c026.addToMilkingRecord((MilkingDetail)ParseMilkingRecord.readMilkingRecordsFromSMS("026{12", LocalDate.parse("2018-02-15"),(short)3,new Integer(3)).get("026"));
			assertTrue(c026.getLifetimeMilkTotals().getMilkVolume()==70.50f," Total was expected to be 70.5 but got " + 
					c026.getLifetimeMilkTotals().getMilkVolume());
			assertTrue(c026.getLifetimeMilkTotals().getMilkingEventNumber()==2," Total was expected to be 2 but got " + 
					c026.getLifetimeMilkTotals().getMilkingEventNumber());
			assertTrue(c026.getLifeTimeDailyMilkingAverage()==35.25f," Average was expected to be 35.25 but got " + 
					c026.getLifeTimeDailyMilkingAverage());
			c026.addToMilkingRecord((MilkingDetail)ParseMilkingRecord.readMilkingRecordsFromSMS("026{10", LocalDate.parse("2018-02-15"),(short)3,new Integer(1)).get("026"));
			c026.addToMilkingRecord((MilkingDetail)ParseMilkingRecord.readMilkingRecordsFromSMS("026{10", LocalDate.parse("2018-02-15"),(short)3,new Integer(2)).get("026"));
			c026.addToMilkingRecord((MilkingDetail)ParseMilkingRecord.readMilkingRecordsFromSMS("026{10", LocalDate.parse("2018-02-15"),(short)3,new Integer(3)).get("026"));
			assertTrue(c026.getLifetimeMilkTotals().getMilkVolume()==64.50f," Total was expected to be 64.5 but got " + 
					c026.getLifetimeMilkTotals().getMilkVolume());
			assertTrue(c026.getLifetimeMilkTotals().getMilkingEventNumber()==2," Total was expected to be 2 but got " + 
					c026.getLifetimeMilkTotals().getMilkingEventNumber());
			assertTrue(c026.getLifeTimeDailyMilkingAverage()==32.25f," Average was expected to be 32.25 but got " + 
					c026.getLifeTimeDailyMilkingAverage());
			
			// Verify the totals for specified time duration
			assertTrue(c026.getMilkTotalsForSpecifiedPeriod(LocalDate.parse("2018-02-15"), LocalDate.parse("2018-02-15")).getMilkVolume()==30f," Average was expected to be 30 but got " + 
					c026.getMilkTotalsForSpecifiedPeriod(LocalDate.parse("2018-02-15"), LocalDate.parse("2018-02-15")).getMilkVolume());
			assertTrue(c026.geDailyMilkingAverageForSpecifiedPeriod(LocalDate.parse("2018-02-15"), LocalDate.parse("2018-02-15"))==30f," Average was expected to be 30 but got " + 
					c026.geDailyMilkingAverageForSpecifiedPeriod(LocalDate.parse("2018-02-15"), LocalDate.parse("2018-02-15")));
		
			assertTrue(c026.getMilkTotalsForSpecifiedPeriod(LocalDate.parse("2018-02-15"), LocalDate.now()).getMilkVolume()==64.5f," Total was expected to be 64.5 but got " + 
					c026.getMilkTotalsForSpecifiedPeriod(LocalDate.parse("2018-02-15"), LocalDate.now()).getMilkVolume());
			assertTrue(c026.geDailyMilkingAverageForSpecifiedPeriod(LocalDate.parse("2018-02-15"), LocalDate.now())==32.25f," Average was expected to be 32.25 but got " + 
					c026.geDailyMilkingAverageForSpecifiedPeriod(LocalDate.parse("2018-02-15"), LocalDate.now()));		
			
			// Verify that mass upload of milking data works properly.
			String fileURI = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "26_RECORD.csv";
			HashMap<LocalDate, HashMap<Integer,MilkingDetail>> milkingReport = (HashMap <LocalDate, HashMap<Integer,MilkingDetail>>) ParseMilkingRecord.readMilkingRecordsFromCSV(fileURI);
			c026.setCompleteMilkingRecord(milkingReport);
			float actualVol = c026.getMilkingEvent(LocalDate.parse("2018-02-19"),new Integer(1)).getMilkVolume();
			assertTrue(actualVol == 13.0f," Was expecting 13 liters volume got " + actualVol + " instead");
			assertTrue(c026.getCompleteMilkingRecord().size() == 283, "Was expecting 283 but got " + milkingReport.size() + " instead");
			assertTrue(c026.getMilkTotalsForSpecifiedPeriod(LocalDate.parse("2000-01-01"), LocalDate.now()).getMilkVolume()==5199.2f," Total was expected to be 5199.2 but got " + 
					c026.getMilkTotalsForSpecifiedPeriod(LocalDate.parse("2000-01-01"), LocalDate.now()).getMilkVolume());
			float average = Math.round(c026.geDailyMilkingAverageForSpecifiedPeriod(LocalDate.parse("2000-01-01"), LocalDate.now())*1000f)/1000f;
			assertTrue(average == 18.372f," Average was expected to be 18.372 but got " + average);		
			String csvFile;
			//12,Medical treatment,,25,2018-02-17T00:00,,"Vitamin A-DE injection for energy\, plus other medication to avoid risk of milk fever (ask Waqar for confirmation)"
			// Verify LifeCycle Events loading when all records are well formed.
			csvFile = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "26_EVENTS.csv";
			c026.setLifeCycleEvents(ParseMilkingRecord.readLifeCycleEventFromCSV(csvFile,true));
			assertEquals("1",c026.getLifeCycleEvents().get(0).getEventCode());
			assertEquals("De-worming",c026.getLifeCycleEvents().get(0).getEventShortDescription());
			assertEquals(1,c026.getLifeCycleEvents().get(0).getEventTransactionID());
			assertEquals("2016-12-01T00:00",c026.getLifeCycleEvents().get(0).getEventTimeStamp().toString());
			assertEquals("Kashif Manzoor",c026.getLifeCycleEvents().get(0).getEventOperator());
			assertEquals("Levamisole Salt",c026.getLifeCycleEvents().get(0).getEventNote());
			assertEquals(25, c026.getLifeCycleEvents().size());
			
		} catch (IMDException e) {
			e.printStackTrace();
			fail("C026 verification failed with following message:" + e.getMessage());
		}
		try {
			//////////////// Verifying C017 record ///////////////
			c017 = loadC17Record();
			Period age = Period.between(LocalDate.parse("2014-12-27"),LocalDate.now());
			assertTrue(c017.getCurrentAge().equals(age), " Age mismatched, was expecting " + age + " got " + c017.getCurrentAge().toString() + " instead");
			assertTrue(c017.getNotesCount()==3, " Expected to see 3 notes but got " + c017.getNotesCount());
			assertEquals(2,c017.getAgeAtPurchase().getYears());	
			assertEquals(6,c017.getAgeAtPurchase().getMonths());
			assertEquals(0,c017.getAgeAtPurchase().getDays());
			float actualVol = c017.getMilkingEvent(LocalDate.parse("2018-02-19"),new Integer(1)).getMilkVolume();
			assertEquals(7.5f,actualVol);
			assertEquals(240,c017.getCompleteMilkingRecord().size());
			assertEquals(4287.56f,Math.round(c017.getMilkTotalsForSpecifiedPeriod(LocalDate.parse("2000-01-01"), LocalDate.now()).getMilkVolume()*100f)/100f);
			assertEquals(17.86f,Math.round(c017.geDailyMilkingAverageForSpecifiedPeriod(LocalDate.parse("2000-01-01"), LocalDate.now())*100f)/100f);
		} catch (IMDException e) {
			// TODO Auto-generated catch block
			fail("C017 verification failed.");
		}
	}

	@Test
	void testLifeCycleEventLoad() throws IMDException {
		Dam c017 = loadC17Record();
		String csvFile = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "17_EVENTS_WITH_ERRORS.csv";
		//////////
		// Verify LifeCycle Events loading when some records are ill-formaed
		c017.setLifeCycleEvents(ParseMilkingRecord.readLifeCycleEventFromCSV(csvFile,false));
		assertEquals("14",c017.getLifeCycleEvents().get(0).getEventCode());
		assertEquals("Milk Quality Testing",c017.getLifeCycleEvents().get(0).getEventShortDescription());
		assertEquals(1,c017.getLifeCycleEvents().get(0).getEventTransactionID());
		assertEquals("2017-06-27T00:00",c017.getLifeCycleEvents().get(0).getEventTimeStamp().toString());
		assertEquals("Engro",c017.getLifeCycleEvents().get(0).getEventOperator());
		assertEquals("Fat 4.6%,LR:27.5 @35 C",c017.getLifeCycleEvents().get(0).getEventNote());
		assertEquals(20, c017.getLifeCycleEvents().size());
	}
	
	private Dam loadC17Record() {
		Dam c017 = null;
		try {
			c017 = new Dam("017","017",LocalDate.parse("2014-12-27"),false,200000,"Rs.");
			c017.setPurchaseFrom(new Contact("Azam",null, "Riaz"));
			c017.setPurchaseDate(LocalDate.parse("2017-06-27"));
			c017.setAnimalSire(null);
			c017.setAnimalDam(null);
			
			// Verify that notes are properly added.
			c017.addNote(new Note(1,"Had two adult teeth at purchase. We found ticks on the cow and it had not properly excreted all the material after birth. Therefore it went through lot of medical treatement for the next 3 weeks after purchase."));
			c017.addNote(new Note(2,"Alhumdullilah, it maintained a healthy consistent daily milk output throughout the year."));
			c017.addNote(new Note(3,"This was also seen by Sumair and he had not liked the cow."));

			
			// Verify that mass upload of milking data works properly.
			String fileURI = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "17_RECORD.csv";
			HashMap<LocalDate, HashMap<Integer,MilkingDetail>> milkingReport = (HashMap <LocalDate, HashMap<Integer,MilkingDetail>>) ParseMilkingRecord.readMilkingRecordsFromCSV(fileURI);
			c017.setCompleteMilkingRecord(milkingReport);
				//////////
		} catch (IMDException e) {
			e.printStackTrace();
			fail("Contact could not be created.");
		}
		return c017;
	}
}
