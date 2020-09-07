package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.advisement.AdvisementRuleManager;
import com.imd.advisement.CalfWeightAdvisement;
import com.imd.advisement.CalvingPrepFeedAdvisement;
import com.imd.advisement.DehorningAdvisement;
import com.imd.advisement.DelayedHeatCowAdvisement;
import com.imd.advisement.DelayedHeatHeiferAdvisement;
import com.imd.advisement.DewormingAdvisement;
import com.imd.advisement.DryCowAdvisement;
import com.imd.advisement.FMDVaccinationAdvisement;
import com.imd.advisement.HeatWarningAdvisement;
import com.imd.advisement.HeiferWeightAdvisement;
import com.imd.advisement.MastitisTestAdvisement;
import com.imd.advisement.PregnancyTestAdvisement;
import com.imd.advisement.WeanOffAdvisement;
import com.imd.advisement.WeightMeasurementAdvisement;
import com.imd.dto.Advisement;
import com.imd.dto.Animal;
import com.imd.dto.AnimalAdvisement;
import com.imd.dto.Dam;
import com.imd.dto.LifeCycleEventCode;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.User;
import com.imd.services.bean.LifeCycleEventBean;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

class AdvisementLoaderTest {

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
	void testWeanOffAdvisementRule() {

		try {
			WeanOffAdvisement weanOffHeatAdvisement = new WeanOffAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-999"); // Older than 120 days
			animalLoader.deleteAnimal("IMD", "-998"); // Younger than 120 days already weaned off - no violation
			animalLoader.deleteAnimal("IMD", "-997"); // Younger than 120 days Th1 violated
			animalLoader.deleteAnimal("IMD", "-996"); // Younger than 120 days Th2 violated
			animalLoader.deleteAnimal("IMD", "-995"); // Younger than 120 days Th3 violated
			///////////////////

			Animal oldAnimal = createTestAnimal("-999");
			oldAnimal.setHerdJoiningDate(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(12));
			oldAnimal.setHerdLeavingDate(null);
			oldAnimal.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(210));
			oldAnimal.setAnimalType(Util.AnimalTypes.HEIFER);
			int transOldAnimal = animalLoader.insertAnimal(oldAnimal);
			assertEquals(1,transOldAnimal, "Exactly one record -999 should have been inserted");
			
			Animal noViolation = createTestAnimal("-998");
			noViolation.setHerdJoiningDate(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(12));
			noViolation.setHerdLeavingDate(null);
			noViolation.setAnimalType(Util.AnimalTypes.FEMALECALF);
			noViolation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(92));
			LifeCycleEventBean noViolationBean = new LifeCycleEventBean();
			noViolationBean.setAnimalTag(noViolation.getAnimalTag());
			noViolationBean.setEventCode(Util.LifeCycleEvents.WEANEDOFF);
			noViolationBean.setEventComments("Test  Event - does not violate any threshold");
			noViolationBean.setOrgID("IMD");
			noViolationBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(10)));
			LifecycleEvent noViolationEvent = new LifecycleEvent(noViolationBean);
			noViolationEvent.setCreatedBy(new User("KASHIF"));
			noViolationEvent.setUpdatedBy(new User("KASHIF"));
			noViolationEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			noViolationEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transNoViolation = animalLoader.insertAnimal(noViolation);
			assertEquals(1,transNoViolation, "Exactly one record -998 should have been inserted");
			assertTrue(eventsLoader.insertLifeCycleEvent(noViolationEvent)>0);	
						
			Animal th1Violation = createTestAnimal("-997");
			th1Violation.setHerdJoiningDate(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(12));
			th1Violation.setHerdLeavingDate(null);
			th1Violation.setAnimalType(Util.AnimalTypes.FEMALECALF);
			th1Violation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(86));
			int transTh1Violation = animalLoader.insertAnimal(th1Violation);
			assertEquals(1,transTh1Violation, "Exactly one record -997 should have been inserted");				
						
			Animal th2Violation = createTestAnimal("-996");
			th2Violation.setHerdJoiningDate(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(12));
			th2Violation.setHerdLeavingDate(null);
			th2Violation.setAnimalType(Util.AnimalTypes.MALECALF);
			th2Violation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(91));
			int transTh2Violation = animalLoader.insertAnimal(th2Violation);
			assertEquals(1,transTh2Violation, "Exactly one record -996 should have been inserted");				

			
			Animal th3Violation = createTestAnimal("-995");
			th3Violation.setHerdJoiningDate(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(12));
			th3Violation.setHerdLeavingDate(null);
			th3Violation.setAnimalType(Util.AnimalTypes.FEMALECALF);
			th3Violation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(96));
			int transTh3Violation = animalLoader.insertAnimal(th3Violation);
			assertEquals(1,transTh3Violation, "Exactly one record -995 should have been inserted");				

			List<Animal> animalPop = weanOffHeatAdvisement.applyAdvisementRule("IMD", null);
			boolean th3Found = false;
			boolean th2Found = false;
			boolean th1Found = false;
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(oldAnimal.getAnimalTag())) {
						fail(oldAnimal.getAnimalTag() +  "("+ oldAnimal.getAnimalType() + ") is older than 6 months, it should not be considered by this advisement");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(noViolation.getAnimalTag())) {
						fail(noViolation.getAnimalTag() +  "("+ noViolation.getAnimalType() + ") has been weaned off. The weanoff advisement should not have been triggered for this animal");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th1Violation.getAnimalTag())) {
						assertTrue(populationAnimal.isThreshold1Violated() ,"This animal " + populationAnimal.getAnimalTag() + " should have violated first threshold.");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal " + populationAnimal.getAnimalTag() + " should have violated first threshold.");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal " + populationAnimal.getAnimalTag() + " should have violated first threshold.");
						th1Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th2Violation.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal " + populationAnimal.getAnimalTag() + " should have violated second threshold");
						assertTrue(populationAnimal.isThreshold2Violated() ,"This animal " + populationAnimal.getAnimalTag() + " should have violated second threshold");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal " + populationAnimal.getAnimalTag() + " should have violated second threshold.");
						th2Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th3Violation.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal " + populationAnimal.getAnimalTag() + " should have violated third threshold. " + th3Violation.getAnimalTag());
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal " + populationAnimal.getAnimalTag() + " should have violated third threshold. " + th3Violation.getAnimalTag());
						assertTrue(populationAnimal.isThreshold3Violated() ,"This animal " + populationAnimal.getAnimalTag() + " should have violated third threshold. " + th3Violation.getAnimalTag());
						th3Found = true;
					}
				}
			}
			assertTrue(th3Found,th3Violation.getAnimalTag() +  "("+ th3Violation.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			assertTrue(th2Found,th2Violation.getAnimalTag() +  "("+ th2Violation.getAnimalType() + ") should have been included in the Threshold2 Violation Advisement population");
			assertTrue(th1Found,th1Violation.getAnimalTag() +  "("+ th1Violation.getAnimalType() + ") should have been included in the Threshold1 Violation Advisement population");
			
			///// clean up /////
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}	
			
	
	@Test
	void testCalfWeightAdvisement() {
		try {
			Animal youngAnimalTh1 = this.createTestAnimal("-999");
			Animal youngAnimalTh2 = this.createTestAnimal("-998");
			Animal youngAnimalTh3 = this.createTestAnimal("-997");
			Animal youngAnimalOk = this.createTestAnimal("-996");
			Animal oldAnimal = this.createTestAnimal("-995");
			
			
			LifeCycleEventsLoader evtLoader = new LifeCycleEventsLoader();
			AdvisementLoader advLdr =  new AdvisementLoader();
			List<Advisement> rules = advLdr.getSpecifiedActiveAdvisementRules("IMD", Util.AdvisementRules.CALFWEIGHT);
			assertTrue(rules != null && !rules.isEmpty() && rules.size() == 1,"Exactly one " + Util.AdvisementRules.CALFWEIGHT + " active advisement rule should have been found");
			assertTrue(rules.get(0).getAuxInfo1() != null && !rules.get(0).getAuxInfo1().isEmpty(), Util.AdvisementRules.CALFWEIGHT + " rule should have an AUX_INFO1 value that contains the age in days of the calves that we will consider for this advisement");
			
			Advisement rule = rules.get(0);
			
			DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
			DateTime dob = now.minusDays(Integer.parseInt(rules.get(0).getAuxInfo1()));
			AnimalLoader ldr = new AnimalLoader();
			
			
			assertTrue(evtLoader.deleteAnimalLifecycleEvents(youngAnimalOk.getOrgId(), youngAnimalOk.getAnimalTag()) >= 0);
			assertTrue(evtLoader.deleteAnimalLifecycleEvents(youngAnimalTh1.getOrgId(), youngAnimalTh1.getAnimalTag()) >= 0);
			assertTrue(evtLoader.deleteAnimalLifecycleEvents(youngAnimalTh2.getOrgId(), youngAnimalTh2.getAnimalTag()) >= 0);
			assertTrue(evtLoader.deleteAnimalLifecycleEvents(youngAnimalTh3.getOrgId(), youngAnimalTh3.getAnimalTag()) >= 0);

			
			assertTrue(ldr.deleteAnimal(youngAnimalTh1.getOrgId(), youngAnimalTh1.getAnimalTag()) >= 0);
			assertTrue(ldr.deleteAnimal(youngAnimalTh2.getOrgId(), youngAnimalTh2.getAnimalTag()) >= 0);
			assertTrue(ldr.deleteAnimal(youngAnimalTh3.getOrgId(), youngAnimalTh3.getAnimalTag()) >= 0);
			assertTrue(ldr.deleteAnimal(youngAnimalOk.getOrgId(), youngAnimalOk.getAnimalTag()) >= 0);
			assertTrue(ldr.deleteAnimal(oldAnimal.getOrgId(), oldAnimal.getAnimalTag()) >= 0);

			youngAnimalTh1.setDateOfBirth(dob.plusDays(10));
			youngAnimalTh2.setDateOfBirth(dob.plusDays(20));
			youngAnimalTh3.setDateOfBirth(dob.plusDays(30));
			youngAnimalOk.setDateOfBirth(dob.plusHours(2));			
			oldAnimal.setDateOfBirth(dob.minusDays(100));
			
			ldr.insertAnimal(youngAnimalTh1);
			ldr.insertAnimal(youngAnimalTh2);
			ldr.insertAnimal(youngAnimalTh3);
			ldr.insertAnimal(youngAnimalOk);
			ldr.insertAnimal(oldAnimal);
			
			
			CalfWeightAdvisement adv = new CalfWeightAdvisement();
			List<Animal> advAnml = adv.applyAdvisementRule(youngAnimalOk.getOrgId(), Util.LanguageCode.ENG);
			assertTrue(advAnml != null && advAnml.size() >= 3, " At least three animals should have been retrieved");
			Iterator<Animal> it = advAnml.iterator();
			while (it.hasNext()) {
				Animal anml = it.next();
				String tag = anml.getAnimalTag();
				if (tag.equals(oldAnimal.getAnimalTag())) {
					fail("The animal " + tag + " should not have been found as it does not violate " + Util.AdvisementRules.CALFWEIGHT + " advisement");
				} else if (tag.equals(youngAnimalOk.getAnimalTag())) {
					assertTrue(anml.isThreshold3Violated());
					assertFalse(anml.isThreshold2Violated());
					assertFalse(anml.isThreshold1Violated());
				} else if (tag.equals(youngAnimalTh1.getAnimalTag())) {
					assertTrue(anml.isThreshold3Violated());
					assertFalse(anml.isThreshold2Violated());
					assertFalse(anml.isThreshold1Violated());
				} else if (tag.equals(youngAnimalTh2.getAnimalTag())) {
					assertTrue(anml.isThreshold3Violated());
					assertFalse(anml.isThreshold2Violated());
					assertFalse(anml.isThreshold1Violated());
				} else if (tag.equals(youngAnimalTh3.getAnimalTag())) {
					assertTrue(anml.isThreshold3Violated());
					assertFalse(anml.isThreshold2Violated());
					assertFalse(anml.isThreshold1Violated());
				}
			}
			
			int deltaDays = 15;
			float th3 = rule.getThirdThreshold();
			float th2 = rule.getSecondThreshold();
			float th1 = rule.getFirstThreshold();
			
			User kashif = new User ("KASHIF");
			LifecycleEvent weightEvent1 = new LifecycleEvent(youngAnimalOk.getOrgId(), 
					0, youngAnimalOk.getAnimalTag(), Util.LifeCycleEvents.WEIGHT, 
					kashif, now, kashif, now);
			weightEvent1.setEventTimeStamp(now.minusDays(100));
			weightEvent1.setAuxField1Value("50");
			
			LifecycleEvent weightEvent2 = new LifecycleEvent(youngAnimalOk.getOrgId(), 
					0, youngAnimalOk.getAnimalTag(), Util.LifeCycleEvents.WEIGHT, 
					kashif, now, kashif, now);
			weightEvent2.setEventTimeStamp(now.minusDays(100-deltaDays));
			weightEvent2.setAuxField1Value((50 + (deltaDays * (th1 + 0.1))) + "");

			evtLoader.insertLifeCycleEvent(weightEvent2);
			evtLoader.insertLifeCycleEvent(weightEvent1);
			
			weightEvent1.setAnimalTag(youngAnimalTh1.getAnimalTag());
			weightEvent2.setAnimalTag(youngAnimalTh1.getAnimalTag());
			weightEvent2.setAuxField1Value((50 + (deltaDays * (th1 - 0.05))) + "");
			evtLoader.insertLifeCycleEvent(weightEvent2);
			evtLoader.insertLifeCycleEvent(weightEvent1);
			
			weightEvent1.setAnimalTag(youngAnimalTh2.getAnimalTag());
			weightEvent2.setAnimalTag(youngAnimalTh2.getAnimalTag());
			weightEvent2.setAuxField1Value((50 + (deltaDays * (th2 - 0.05))) + "");
			evtLoader.insertLifeCycleEvent(weightEvent2);
			evtLoader.insertLifeCycleEvent(weightEvent1);
			
			weightEvent1.setAnimalTag(youngAnimalTh3.getAnimalTag());
			weightEvent2.setAnimalTag(youngAnimalTh3.getAnimalTag());
			weightEvent2.setAuxField1Value((50 + (deltaDays * (th3 - 0.05))) + "");
			evtLoader.insertLifeCycleEvent(weightEvent2);
			evtLoader.insertLifeCycleEvent(weightEvent1);

			advAnml = adv.applyAdvisementRule(youngAnimalOk.getOrgId(), null);
			assertTrue(advAnml != null && advAnml.size() >= 3, "At least three animals should have been retrieved, but we got " + advAnml.size() + " instead");
			it = advAnml.iterator();
			while (it.hasNext()) {
				Animal anml = it.next();
				String tag = anml.getAnimalTag();
				if (tag.equals(oldAnimal.getAnimalTag()) || tag.equals(youngAnimalOk.getAnimalTag())) {
					fail("The animal " + tag + " should not have been found as it does not violate " + Util.AdvisementRules.CALFWEIGHT + " advisement");
				} else if (tag.equals(youngAnimalTh1.getAnimalTag())) {
					assertTrue(anml.isThreshold1Violated());
					assertFalse(anml.isThreshold2Violated());
					assertFalse(anml.isThreshold3Violated());
				} else if (tag.equals(youngAnimalTh2.getAnimalTag())) {
					assertTrue(anml.isThreshold2Violated());
					assertFalse(anml.isThreshold1Violated());
					assertFalse(anml.isThreshold3Violated());
				} else if (tag.equals(youngAnimalTh3.getAnimalTag())) {
					assertTrue(anml.isThreshold3Violated());
					assertFalse(anml.isThreshold2Violated());
					assertFalse(anml.isThreshold1Violated());
				}
			}
			
			assertTrue(evtLoader.deleteAnimalLifecycleEvents(youngAnimalOk.getOrgId(), youngAnimalOk.getAnimalTag()) == 2);
			assertTrue(evtLoader.deleteAnimalLifecycleEvents(youngAnimalTh1.getOrgId(), youngAnimalTh1.getAnimalTag()) == 2);
			assertTrue(evtLoader.deleteAnimalLifecycleEvents(youngAnimalTh2.getOrgId(), youngAnimalTh2.getAnimalTag()) == 2);
			assertTrue(evtLoader.deleteAnimalLifecycleEvents(youngAnimalTh3.getOrgId(), youngAnimalTh3.getAnimalTag()) == 2);
			
			assertTrue(ldr.deleteAnimal(youngAnimalTh1.getOrgId(), youngAnimalTh1.getAnimalTag()) == 1);
			assertTrue(ldr.deleteAnimal(youngAnimalTh2.getOrgId(), youngAnimalTh2.getAnimalTag()) == 1);
			assertTrue(ldr.deleteAnimal(youngAnimalTh3.getOrgId(), youngAnimalTh3.getAnimalTag()) == 1);
			assertTrue(ldr.deleteAnimal(youngAnimalOk.getOrgId() , youngAnimalOk.getAnimalTag())  == 1);
			assertTrue(ldr.deleteAnimal(oldAnimal.getOrgId(), oldAnimal.getAnimalTag()) == 1);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occurred "+ e.getMessage());
		}
	}
	
	@Test
	void testSpecificExistingAnimal() {
		CalfWeightAdvisement wtAdvisement = new CalfWeightAdvisement();
		wtAdvisement.applyAdvisementRule("IMD", Util.LanguageCode.ENG);
	}
	
	@Test
	void testWeightMeasurementAdvisementRule() {

		try {
			WeightMeasurementAdvisement weightMeasurementAdvisement = new WeightMeasurementAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-999"); // Older than 180 days
			animalLoader.deleteAnimal("IMD", "-998"); // Younger than 180 days no violation
			animalLoader.deleteAnimal("IMD", "-997"); // Younger than 180 days Th1 violated
			animalLoader.deleteAnimal("IMD", "-996"); // Younger than 180 days Th2 violated
			animalLoader.deleteAnimal("IMD", "-995"); // Younger than 180 days Th3 violated
			///////////////////

			Animal oldAnimal = createTestAnimal("-999");
			oldAnimal.setHerdJoiningDate(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(24));
			oldAnimal.setHerdLeavingDate(null);
			oldAnimal.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(24));
			oldAnimal.setAnimalType(Util.AnimalTypes.HEIFER);
			int transOldAnimal = animalLoader.insertAnimal(oldAnimal);
			assertEquals(1,transOldAnimal, "Exactly one record -999 should have been inserted");
			
			Animal noViolation = createTestAnimal("-998");
			noViolation.setAnimalType(Util.AnimalTypes.FEMALECALF);
			noViolation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(170));
			LifeCycleEventBean noViolationBean = new LifeCycleEventBean();
			noViolationBean.setAnimalTag(noViolation.getAnimalTag());
			noViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			noViolationBean.setEventComments("Test  Event - does not violate any threshold");
			noViolationBean.setOrgID("IMD");
			noViolationBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(10)));
			LifecycleEvent noViolationEvent = new LifecycleEvent(noViolationBean);
			noViolationEvent.setCreatedBy(new User("KASHIF"));
			noViolationEvent.setUpdatedBy(new User("KASHIF"));
			noViolationEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			noViolationEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transNoViolation = animalLoader.insertAnimal(noViolation);
			assertEquals(1,transNoViolation, "Exactly one record -998 should have been inserted");
			assertTrue(eventsLoader.insertLifeCycleEvent(noViolationEvent)>0);	
						
			Animal th1Violation = createTestAnimal("-997");
			th1Violation.setAnimalType(Util.AnimalTypes.FEMALECALF);
			th1Violation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(170));
			LifeCycleEventBean th1ViolationBean = new LifeCycleEventBean();
			th1ViolationBean.setAnimalTag(th1Violation.getAnimalTag());
			th1ViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			th1ViolationBean.setEventComments("Test  Event - Violates Threshold 1");
			th1ViolationBean.setOrgID("IMD");
			th1ViolationBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(16)));
			LifecycleEvent th1ViolationEvent = new LifecycleEvent(th1ViolationBean);
			th1ViolationEvent.setCreatedBy(new User("KASHIF"));
			th1ViolationEvent.setUpdatedBy(new User("KASHIF"));
			th1ViolationEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th1ViolationEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transTh1Violation = animalLoader.insertAnimal(th1Violation);
			assertEquals(1,transTh1Violation, "Exactly one record -997 should have been inserted");				
			assertTrue(eventsLoader.insertLifeCycleEvent(th1ViolationEvent)>0);	
						
			Animal th2Violation = createTestAnimal("-996");
			th2Violation.setAnimalType(Util.AnimalTypes.MALECALF);
			th2Violation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(170));
			LifeCycleEventBean th2ViolationBean = new LifeCycleEventBean();
			th2ViolationBean.setAnimalTag(th2Violation.getAnimalTag());
			th2ViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			th2ViolationBean.setEventComments("Test  Event -  Violates Threshold 2");
			th2ViolationBean.setOrgID("IMD");
			th2ViolationBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(22)));
			LifecycleEvent th2ViolationEvent = new LifecycleEvent(th2ViolationBean);
			th2ViolationEvent.setCreatedBy(new User("KASHIF"));
			th2ViolationEvent.setUpdatedBy(new User("KASHIF"));
			th2ViolationEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th2ViolationEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transTh2Violation = animalLoader.insertAnimal(th2Violation);
			assertEquals(1,transTh2Violation, "Exactly one record -996 should have been inserted");				
			assertTrue(eventsLoader.insertLifeCycleEvent(th2ViolationEvent)>0);	

			
			Animal th3Violation = createTestAnimal("-995");
			th3Violation.setAnimalType(Util.AnimalTypes.FEMALECALF);
			th3Violation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(170));
			LifeCycleEventBean th3ViolationBean = new LifeCycleEventBean();
			th3ViolationBean.setAnimalTag(th3Violation.getAnimalTag());
			th3ViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			th3ViolationBean.setEventComments("Test  Event -  Violates Threshold 3");
			th3ViolationBean.setOrgID("IMD");
			th3ViolationBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(29)));
			LifecycleEvent th3ViolationEvent = new LifecycleEvent(th3ViolationBean);
			th3ViolationEvent.setCreatedBy(new User("KASHIF"));
			th3ViolationEvent.setUpdatedBy(new User("KASHIF"));
			th3ViolationEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th3ViolationEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transTh3Violation = animalLoader.insertAnimal(th3Violation);
			assertEquals(1,transTh3Violation, "Exactly one record -995 should have been inserted");				
			assertTrue(eventsLoader.insertLifeCycleEvent(th3ViolationEvent)>0);	

			List<Animal> animalPop = weightMeasurementAdvisement.applyAdvisementRule("IMD", Util.LanguageCode.ENG);
			boolean th3Found = false;
			boolean th2Found = false;
			boolean th1Found = false;
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(oldAnimal.getAnimalTag())) {
						fail(oldAnimal.getAnimalTag() +  "("+ oldAnimal.getAnimalType() + ") is older than 6 months, it should not be considered by this advisement");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(noViolation.getAnimalTag())) {
						fail(noViolation.getAnimalTag() +  "("+ noViolation.getAnimalType() + ") has been weighed recently. The late weight advisement should not have been triggered for this animal");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th1Violation.getAnimalTag())) {
						assertTrue(populationAnimal.isThreshold1Violated() ,"This animal should have violated first threshold.");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated first threshold.");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated first threshold.");
						th1Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th2Violation.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated second threshold");
						assertTrue(populationAnimal.isThreshold2Violated() ,"This animal should have violated second threshold");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated second threshold.");
						th2Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th3Violation.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated third threshold. " + th3Violation.getAnimalTag());
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated third threshold. " + th3Violation.getAnimalTag());
						assertTrue(populationAnimal.isThreshold3Violated() ,"This animal should have violated third threshold. " + th3Violation.getAnimalTag());
						th3Found = true;
					}
				}
			}
			assertTrue(th3Found,th3Violation.getAnimalTag() +  "("+ th3Violation.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			assertTrue(th2Found,th2Violation.getAnimalTag() +  "("+ th2Violation.getAnimalType() + ") should have been included in the Threshold2 Violation Advisement population");
			assertTrue(th1Found,th1Violation.getAnimalTag() +  "("+ th1Violation.getAnimalType() + ") should have been included in the Threshold1 Violation Advisement population");
			
			///// clean up /////
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}	
		
	@Test
	void testHeiferWeightAdvisement() {

		try {
			HeiferWeightAdvisement weightMeasurementAdvisement = new HeiferWeightAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999"); // Older than 365 days no violation
			animalLoader.deleteAnimal("IMD", "-998"); // Younger than 365 days no violation
			animalLoader.deleteAnimal("IMD", "-997"); // Older than 365 days Th1 violated
			animalLoader.deleteAnimal("IMD", "-996"); // Older than 365 days Th2 violated
			animalLoader.deleteAnimal("IMD", "-995"); // Older than 365 days Th3 violated
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			///////////////////

			Animal oldAnimalNoViolation = createTestAnimal("-999");
			oldAnimalNoViolation.setHerdLeavingDate(null);
			oldAnimalNoViolation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(14));
			oldAnimalNoViolation.setHerdJoiningDate(oldAnimalNoViolation.getDateOfBirth());
			oldAnimalNoViolation.setAnimalType(Util.AnimalTypes.HEIFER);
			int transOldAnimalNoViolation = animalLoader.insertAnimal(oldAnimalNoViolation);
			assertEquals(1,transOldAnimalNoViolation, "Exactly one record -999 should have been inserted");
			LifeCycleEventBean oldNoViolationBean = new LifeCycleEventBean();
			oldNoViolationBean.setAnimalTag(oldAnimalNoViolation.getAnimalTag());
			oldNoViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			oldNoViolationBean.setEventComments("Test  Event - does not violate any threshold");
			oldNoViolationBean.setOrgID("IMD");
			oldNoViolationBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(10)));
			LifecycleEvent oldNoViolationEvent = new LifecycleEvent(oldNoViolationBean);
			oldNoViolationEvent.setAuxField1Value(Util.DefaultValues.HEIFER_TARGET_WEIGHT + 5 + "");
			oldNoViolationEvent.setCreatedBy(new User("KASHIF"));
			oldNoViolationEvent.setUpdatedBy(new User("KASHIF"));
			oldNoViolationEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			oldNoViolationEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			assertTrue(eventsLoader.insertLifeCycleEvent(oldNoViolationEvent)>0);	
			
			
			Animal youngNoViolation = createTestAnimal("-998");
			youngNoViolation.setAnimalType(Util.AnimalTypes.HEIFER);
			youngNoViolation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(180));
			LifeCycleEventBean younNoViolationBean = new LifeCycleEventBean();
			younNoViolationBean.setAnimalTag(youngNoViolation.getAnimalTag());
			younNoViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			younNoViolationBean.setEventComments("Test  Event - does not violate any threshold");
			younNoViolationBean.setOrgID("IMD");
			younNoViolationBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(10)));
			LifecycleEvent youngNoViolationEvent = new LifecycleEvent(younNoViolationBean);
			youngNoViolationEvent.setAuxField1Value("50.0");
			youngNoViolationEvent.setCreatedBy(new User("KASHIF"));
			youngNoViolationEvent.setUpdatedBy(new User("KASHIF"));
			youngNoViolationEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			youngNoViolationEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transYoungNoViolation = animalLoader.insertAnimal(youngNoViolation);
			assertEquals(1,transYoungNoViolation, "Exactly one record -998 should have been inserted");
			assertTrue(eventsLoader.insertLifeCycleEvent(youngNoViolationEvent)>0);	
						
			Animal th1Violation = createTestAnimal("-997");
			th1Violation.setAnimalType(Util.AnimalTypes.HEIFER);
			th1Violation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(370));
			LifeCycleEventBean th1ViolationBean = new LifeCycleEventBean();
			th1ViolationBean.setAnimalTag(th1Violation.getAnimalTag());
			th1ViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			th1ViolationBean.setEventComments("Test  Event - Violates Threshold 1");
			th1ViolationBean.setOrgID("IMD");
			th1ViolationBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(16)));
			LifecycleEvent th1ViolationEvent = new LifecycleEvent(th1ViolationBean);
			th1ViolationEvent.setAuxField1Value(Util.DefaultValues.HEIFER_TARGET_WEIGHT-1 + "");
			th1ViolationEvent.setCreatedBy(new User("KASHIF"));
			th1ViolationEvent.setUpdatedBy(new User("KASHIF"));
			th1ViolationEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th1ViolationEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transTh1Violation = animalLoader.insertAnimal(th1Violation);
			assertEquals(1,transTh1Violation, "Exactly one record -997 should have been inserted");				
			assertTrue(eventsLoader.insertLifeCycleEvent(th1ViolationEvent)>0);	
						
			Animal th2Violation = createTestAnimal("-996");
			th2Violation.setAnimalType(Util.AnimalTypes.HEIFER);
			th2Violation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(426));
			LifeCycleEventBean th2ViolationBean = new LifeCycleEventBean();
			th2ViolationBean.setAnimalTag(th2Violation.getAnimalTag());
			th2ViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			th2ViolationBean.setEventComments("Test  Event -  Violates Threshold 2");
			th2ViolationBean.setOrgID("IMD");
			th2ViolationBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(22)));
			LifecycleEvent th2ViolationEvent = new LifecycleEvent(th2ViolationBean);
			th2ViolationEvent.setAuxField1Value(Util.DefaultValues.HEIFER_TARGET_WEIGHT-1 + "");
			th2ViolationEvent.setCreatedBy(new User("KASHIF"));
			th2ViolationEvent.setUpdatedBy(new User("KASHIF"));
			th2ViolationEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th2ViolationEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transTh2Violation = animalLoader.insertAnimal(th2Violation);
			assertEquals(1,transTh2Violation, "Exactly one record -996 should have been inserted");				
			assertTrue(eventsLoader.insertLifeCycleEvent(th2ViolationEvent)>0);	

			
			Animal th3Violation = createTestAnimal("-995");
			th3Violation.setAnimalType(Util.AnimalTypes.HEIFER);
			th3Violation.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(550));
			LifeCycleEventBean th3ViolationBean = new LifeCycleEventBean();
			th3ViolationBean.setAnimalTag(th3Violation.getAnimalTag());
			th3ViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			th3ViolationBean.setEventComments("Test  Event -  Violates Threshold 3");
			th3ViolationBean.setOrgID("IMD");
			th3ViolationBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(29)));
			LifecycleEvent th3ViolationEvent = new LifecycleEvent(th3ViolationBean);
			th3ViolationEvent.setAuxField1Value(Util.DefaultValues.HEIFER_TARGET_WEIGHT-1 + "");
			th3ViolationEvent.setCreatedBy(new User("KASHIF"));
			th3ViolationEvent.setUpdatedBy(new User("KASHIF"));
			th3ViolationEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th3ViolationEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transTh3Violation = animalLoader.insertAnimal(th3Violation);
			assertEquals(1,transTh3Violation, "Exactly one record -995 should have been inserted");				
			assertTrue(eventsLoader.insertLifeCycleEvent(th3ViolationEvent)>0);	

			List<Animal> animalPop = weightMeasurementAdvisement.applyAdvisementRule("IMD", null);
			boolean th3Found = false;
			boolean th2Found = false;
			boolean th1Found = false;
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(oldAnimalNoViolation.getAnimalTag())) {
						fail(oldAnimalNoViolation.getAnimalTag() +  "("+ oldAnimalNoViolation.getAnimalType() + ") should not be flagged by this advisement.");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(youngNoViolation.getAnimalTag())) {
						fail(youngNoViolation.getAnimalTag() +  "("+ youngNoViolation.getAnimalType() + ") should not have been flagged by this advisement.");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th1Violation.getAnimalTag())) {
						assertTrue(populationAnimal.isThreshold1Violated() ,"This animal should have violated first threshold.");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated first threshold.");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated first threshold.");
						th1Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th2Violation.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated second threshold");
						assertTrue(populationAnimal.isThreshold2Violated() ,"This animal should have violated second threshold");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated second threshold.");
						th2Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th3Violation.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated third threshold. " + th3Violation.getAnimalTag());
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated third threshold. " + th3Violation.getAnimalTag());
						assertTrue(populationAnimal.isThreshold3Violated() ,"This animal should have violated third threshold. " + th3Violation.getAnimalTag());
						th3Found = true;
					}
				}
			}
			assertTrue(th1Found,th1Violation.getAnimalTag() +  "("+ th1Violation.getAnimalType() + ") should have been included in the Threshold1 Violation Advisement population");
			assertTrue(th2Found,th2Violation.getAnimalTag() +  "("+ th2Violation.getAnimalType() + ") should have been included in the Threshold2 Violation Advisement population");
			assertTrue(th3Found,th3Violation.getAnimalTag() +  "("+ th3Violation.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}	
			
	
	@Test
	void testDelayedHeatCowAdvisementRule() {

		try {
			DelayedHeatCowAdvisement delayedHeatCowAdvisement = new DelayedHeatCowAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999"); // OK - recently parturated
			animalLoader.deleteAnimal("IMD", "-998"); // TH1 violated
			animalLoader.deleteAnimal("IMD", "-997"); // TH2 violated
			animalLoader.deleteAnimal("IMD", "-996"); // TH3 violated
			animalLoader.deleteAnimal("IMD", "-995"); // OK - Recently parturated but had a heat soon after
			animalLoader.deleteAnimal("IMD", "-994"); // TH1 - aborted and hasn't come in heat yet
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994");
			///////////////////

			
			Animal th1_1Violated = createTestAnimal("-994");
			th1_1Violated.setAnimalType("DRYAWTHEAT");

			int transTh1_1Violated = animalLoader.insertAnimal(th1_1Violated);
			assertEquals(1,transTh1_1Violated, "Exactly one record -994 should have been inserted");			
			
			LifeCycleEventBean th1_1ViolatedBean1 = new LifeCycleEventBean();
			th1_1ViolatedBean1.setAnimalTag(th1_1Violated.getAnimalTag());
			th1_1ViolatedBean1.setEventCode(Util.LifeCycleEvents.ABORTION);
			th1_1ViolatedBean1.setEventComments("Test  Event - violates 1st threshold");
			th1_1ViolatedBean1.setOrgID("IMD");
			th1_1ViolatedBean1.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(61)));
			LifecycleEvent th1_1ViolatedBeanEvent1 = new LifecycleEvent(th1_1ViolatedBean1);
			th1_1ViolatedBeanEvent1.setCreatedBy(new User("KASHIF"));
			th1_1ViolatedBeanEvent1.setUpdatedBy(new User("KASHIF"));
			th1_1ViolatedBeanEvent1.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th1_1ViolatedBeanEvent1.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			eventsLoader.insertLifeCycleEvent(th1_1ViolatedBeanEvent1);

			LifeCycleEventBean th1_1ViolatedBean2 = new LifeCycleEventBean();
			th1_1ViolatedBean2.setAnimalTag(th1_1Violated.getAnimalTag());
			th1_1ViolatedBean2.setEventCode(Util.LifeCycleEvents.HEAT);
			th1_1ViolatedBean2.setEventComments("Test  Event - violates 1st threshold");
			th1_1ViolatedBean2.setOrgID("IMD");
			th1_1ViolatedBean2.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(240)));
			LifecycleEvent th1_1ViolatedBeanEvent2 = new LifecycleEvent(th1_1ViolatedBean2);
			th1_1ViolatedBeanEvent2.setCreatedBy(new User("KASHIF"));
			th1_1ViolatedBeanEvent2.setUpdatedBy(new User("KASHIF"));
			th1_1ViolatedBeanEvent2.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th1_1ViolatedBeanEvent2.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			eventsLoader.insertLifeCycleEvent(th1_1ViolatedBeanEvent2);
			
			
			
			
			Animal recentlyParturated = createTestAnimal("-999");
			recentlyParturated.setAnimalType("LACTATING");
			LifeCycleEventBean recentlyParturatedBean = new LifeCycleEventBean();
			recentlyParturatedBean.setAnimalTag(recentlyParturated.getAnimalTag());
			recentlyParturatedBean.setEventCode(Util.LifeCycleEvents.PARTURATE);
			recentlyParturatedBean.setEventComments("Test  Event - does not violate any threshold");
			recentlyParturatedBean.setOrgID("IMD");
			recentlyParturatedBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(20)));
			LifecycleEvent recentlyParturatedEvent = new LifecycleEvent(recentlyParturatedBean);
			recentlyParturatedEvent.setCreatedBy(new User("KASHIF"));
			recentlyParturatedEvent.setUpdatedBy(new User("KASHIF"));
			recentlyParturatedEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			recentlyParturatedEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transRecentlyParturated = animalLoader.insertAnimal(recentlyParturated);
			eventsLoader.insertLifeCycleEvent(recentlyParturatedEvent);
			assertEquals(1,transRecentlyParturated, "Exactly one record -999 should have been inserted");
			
			
			Animal th0Violated = createTestAnimal("-995");
			th0Violated.setAnimalType("LACTATING");
			LifeCycleEventBean th00ViolatedBean = new LifeCycleEventBean();
			th00ViolatedBean.setAnimalTag(th0Violated.getAnimalTag());
			th00ViolatedBean.setEventCode(Util.LifeCycleEvents.PARTURATE);
			th00ViolatedBean.setEventComments("Test  Event - does not violate any threshold");
			th00ViolatedBean.setOrgID("IMD");
			th00ViolatedBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(90)));
			LifecycleEvent th00ViolatedEvent = new LifecycleEvent(th00ViolatedBean);
			th00ViolatedEvent.setCreatedBy(new User("KASHIF"));
			th00ViolatedEvent.setUpdatedBy(new User("KASHIF"));
			th00ViolatedEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th00ViolatedEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transTh0Violated = animalLoader.insertAnimal(th0Violated);
			assertEquals(1,transTh0Violated, "Exactly one record -995 should have been inserted");				

			eventsLoader.insertLifeCycleEvent(th00ViolatedEvent);
			LifeCycleEventBean th0ViolatedBean = new LifeCycleEventBean();
			th0ViolatedBean.setAnimalTag(th0Violated.getAnimalTag());
			th0ViolatedBean.setEventCode(Util.LifeCycleEvents.HEAT);
			th0ViolatedBean.setEventComments("Test  Event - does not violate any threshold");
			th0ViolatedBean.setOrgID("IMD");
			th0ViolatedBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(10)));
			LifecycleEvent th0ViolatedEvent = new LifecycleEvent(th0ViolatedBean);
			th0ViolatedEvent.setCreatedBy(new User("KASHIF"));
			th0ViolatedEvent.setUpdatedBy(new User("KASHIF"));
			th0ViolatedEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th0ViolatedEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			eventsLoader.insertLifeCycleEvent(th0ViolatedEvent);
			
			Animal th3Violated = createTestAnimal("-996");
			th3Violated.setAnimalType("LACTATING");
			LifeCycleEventBean th3ViolatedBean = new LifeCycleEventBean();
			th3ViolatedBean.setAnimalTag(th3Violated.getAnimalTag());
			th3ViolatedBean.setEventCode(Util.LifeCycleEvents.PARTURATE);
			th3ViolatedBean.setEventComments("Test  Event - violates 3rd threshold");
			th3ViolatedBean.setOrgID("IMD");
			th3ViolatedBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(121)));
			LifecycleEvent th3ViolatedEvent = new LifecycleEvent(th3ViolatedBean);
			th3ViolatedEvent.setCreatedBy(new User("KASHIF"));
			th3ViolatedEvent.setUpdatedBy(new User("KASHIF"));
			th3ViolatedEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th3ViolatedEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transTh3Violated = animalLoader.insertAnimal(th3Violated);
			eventsLoader.insertLifeCycleEvent(th3ViolatedEvent);
			assertEquals(1,transTh3Violated, "Exactly one record -996 should have been inserted");			
			
			Animal th2Violated = createTestAnimal("-997");
			th2Violated.setAnimalType("LACTATING");
			LifeCycleEventBean th2ViolatedBean = new LifeCycleEventBean();
			th2ViolatedBean.setAnimalTag(th2Violated.getAnimalTag());
			th2ViolatedBean.setEventCode(Util.LifeCycleEvents.PARTURATE);
			th2ViolatedBean.setEventComments("Test  Event - violates 2nd threshold");
			th2ViolatedBean.setOrgID("IMD");
			th2ViolatedBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(91)));
			LifecycleEvent th2ViolatedEvent = new LifecycleEvent(th2ViolatedBean);
			th2ViolatedEvent.setCreatedBy(new User("KASHIF"));
			th2ViolatedEvent.setUpdatedBy(new User("KASHIF"));
			th2ViolatedEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th2ViolatedEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transTh2Violated = animalLoader.insertAnimal(th2Violated);
			eventsLoader.insertLifeCycleEvent(th2ViolatedEvent);
			assertEquals(1,transTh2Violated, "Exactly one record -997 should have been inserted");

			Animal th1Violated = createTestAnimal("-998");
			th1Violated.setAnimalType("LACTATING");
			LifeCycleEventBean th1ViolatedBean = new LifeCycleEventBean();
			th1ViolatedBean.setAnimalTag(th1Violated.getAnimalTag());
			th1ViolatedBean.setEventCode(Util.LifeCycleEvents.PARTURATE);
			th1ViolatedBean.setEventComments("Test  Event - violates 1st threshold");
			th1ViolatedBean.setOrgID("IMD");
			th1ViolatedBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(61)));
			LifecycleEvent th1ViolatedEvent = new LifecycleEvent(th1ViolatedBean);
			th1ViolatedEvent.setCreatedBy(new User("KASHIF"));
			th1ViolatedEvent.setUpdatedBy(new User("KASHIF"));
			th1ViolatedEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			th1ViolatedEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transTh1Violated = animalLoader.insertAnimal(th1Violated);
			eventsLoader.insertLifeCycleEvent(th1ViolatedEvent);
			assertEquals(1,transTh1Violated, "Exactly one record -998 should have been inserted");

			List<Animal> animalPop = delayedHeatCowAdvisement.applyAdvisementRule("IMD", null);
			boolean th3Found = false;
			boolean th2Found = false;
			boolean th1Found = false;
			boolean th1_1Found = false;
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(recentlyParturated.getAnimalTag())) {
						fail(recentlyParturated.getAnimalTag() +  "("+ recentlyParturated.getAnimalType() + ") parturated recently, it is not expected to come to heat so soon");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th0Violated.getAnimalTag())) {
						fail(th0Violated.getAnimalTag() +  "("+ th0Violated.getAnimalType() + ") has recently come in heat. The late heat advisement should not have been triggered for this animal");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th1_1Violated.getAnimalTag())) {
						assertTrue(populationAnimal.isThreshold1Violated(),"This animal should have violated first threshold.");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated first threshold.");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated first threshold.");
						th1_1Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th1Violated.getAnimalTag())) {
						assertTrue(populationAnimal.isThreshold1Violated(),"This animal should have violated first threshold.");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated first threshold.");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated first threshold.");
						th1Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th2Violated.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated second threshold");
						assertTrue(populationAnimal.isThreshold2Violated(),"This animal should have violated second threshold");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated second threshold.");
						th2Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th3Violated.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated third threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated third threshold");
						assertTrue(populationAnimal.isThreshold3Violated(),"This animal should have violated third threshold.");
						th3Found = true;
					}
				}
			}
			assertTrue(th3Found,th3Violated.getAnimalTag() +  "("+ th3Violated.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			assertTrue(th2Found,th2Violated.getAnimalTag() +  "("+ th2Violated.getAnimalType() + ") should have been included in the Threshold2 Violation Advisement population");
			assertTrue(th1Found,th1Violated.getAnimalTag() +  "("+ th1Violated.getAnimalType() + ") should have been included in the Threshold1 Violation Advisement population");
			assertTrue(th1_1Found,th1_1Violated.getAnimalTag() +  "("+ th1_1Violated.getAnimalType() + ") should have been included in the Threshold1 Violation Advisement population");
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-994");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}	
	

	@Test
	void testDelayedHeatHeiferAdvisementRule() {

		try {
			
			//TODO: Add a test case that handles the 022 scenario i.e. the heifer aborted in 6 months. So the delayed heat advisement should have kicked in 60 days after abortion
			DelayedHeatHeiferAdvisement delayedHeatHeiferAdvisement = new DelayedHeatHeiferAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999"); // OK
			animalLoader.deleteAnimal("IMD", "-998"); // TH1 violated
			animalLoader.deleteAnimal("IMD", "-997"); // TH2 violated
			animalLoader.deleteAnimal("IMD", "-996"); // TH3 violated
			animalLoader.deleteAnimal("IMD", "-995"); // Should come back in heat in 2 months violated
			animalLoader.deleteAnimal("IMD", "-994"); // OK
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994");
			///////////////////

			Animal youngHeifer = createTestAnimal("-999");
			youngHeifer.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(11));
			youngHeifer.setAnimalType("HEIFER");

			Animal th1Heifer =createTestAnimal("-998");
			th1Heifer.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(366));
			th1Heifer.setAnimalType("HEIFER");
			
			Animal th2Heifer =createTestAnimal("-997");
			th2Heifer.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(550));
			th2Heifer.setAnimalType("HEIFER");

			Animal th3Heifer =createTestAnimal("-996");
			th3Heifer.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(730));
			th3Heifer.setAnimalType("HEIFER");

			Animal th3WithHeatHeifer =createTestAnimal("-995");
			th3WithHeatHeifer.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(730));
			th3WithHeatHeifer.setAnimalType("HFRAWTHEAT");
			
			LifeCycleEventBean eventBean = new LifeCycleEventBean();
			eventBean.setAnimalTag(th3WithHeatHeifer.getAnimalTag());
			eventBean.setEventCode(Util.LifeCycleEvents.HEAT);
			eventBean.setEventComments("Test  Event - violates Threshold 3");
			eventBean.setOrgID("IMD");
			eventBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(100)));
			LifecycleEvent heiferHeatEvent = new LifecycleEvent(eventBean);
			heiferHeatEvent.setCreatedBy(new User("KASHIF"));
			heiferHeatEvent.setUpdatedBy(new User("KASHIF"));
			heiferHeatEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			heiferHeatEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			
			Animal th0WithHeatHeifer =createTestAnimal("-994");
			th0WithHeatHeifer.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(730));
			th0WithHeatHeifer.setAnimalType("HFRAWTHEAT");
			
			LifeCycleEventBean eventTh0Bean = new LifeCycleEventBean();
			eventTh0Bean.setAnimalTag(th0WithHeatHeifer.getAnimalTag());
			eventTh0Bean.setEventCode(Util.LifeCycleEvents.HEAT);
			eventTh0Bean.setEventComments("Test  Event - does not violate Threshold 3");
			eventTh0Bean.setOrgID("IMD");
			eventTh0Bean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(20)));
			LifecycleEvent heiferTh0HeatEvent = new LifecycleEvent(eventTh0Bean);
			heiferTh0HeatEvent.setCreatedBy(new User("KASHIF"));
			heiferTh0HeatEvent.setUpdatedBy(new User("KASHIF"));
			heiferTh0HeatEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			heiferTh0HeatEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));

			int transYoungHeifer = animalLoader.insertAnimal(youngHeifer);
			int transTh1Heifer = animalLoader.insertAnimal(th1Heifer);
			int transTh2Heifer = animalLoader.insertAnimal(th2Heifer);
			int transTh3Heifer = animalLoader.insertAnimal(th3Heifer);
	
			int transTh3WithHeatHeifer = animalLoader.insertAnimal(th3WithHeatHeifer);
			eventsLoader.insertLifeCycleEvent(heiferHeatEvent);
	
			int transTh0WithHeatHeifer = animalLoader.insertAnimal(th0WithHeatHeifer);
			eventsLoader.insertLifeCycleEvent(heiferTh0HeatEvent);

			
			assertEquals(1,transYoungHeifer, "Exactly one record -999 should have been inserted");
			assertEquals(1,transTh1Heifer, "Exactly one record -998 should have been inserted");			
			assertEquals(1,transTh2Heifer, "Exactly one record -997 should have been inserted");
			assertEquals(1,transTh3Heifer, "Exactly one record -996 should have been inserted");
			assertEquals(1,transTh3WithHeatHeifer, "Exactly one record -995 should have been inserted");
			assertEquals(1,transTh0WithHeatHeifer, "Exactly one record -994 should have been inserted");

			List<Animal> animalPop = delayedHeatHeiferAdvisement.applyAdvisementRule("IMD", null);
			boolean th3WithHeatHeiferFound = false;
			boolean th2HeiferFound = false;
			boolean th1HeiferFound = false;
			boolean th3HeiferFound = false;
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(youngHeifer.getAnimalTag())) {
						fail(youngHeifer.getAnimalTag() +  "("+ youngHeifer.getAnimalType() + ") is too young to be expected to come in heat");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th0WithHeatHeifer.getAnimalTag())) {
						fail(th0WithHeatHeifer.getAnimalTag() +  "("+ th0WithHeatHeifer.getAnimalType() + ") has recently come in heat. The late heat advisement should not have been triggered for this animal");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th3WithHeatHeifer.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated third threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated third threshold");
						assertTrue(populationAnimal.isThreshold3Violated(),"This animal should have violated third threshold.");
						th3WithHeatHeiferFound = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th1Heifer.getAnimalTag())) {
						assertTrue(populationAnimal.isThreshold1Violated(),"This animal should have violated first threshold.");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated first threshold.");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated first threshold.");
						th1HeiferFound = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th2Heifer.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated second threshold");
						assertTrue(populationAnimal.isThreshold2Violated(),"This animal should have violated second threshold");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated second threshold.");
						th2HeiferFound = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(th3Heifer.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated third threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated third threshold");
						assertTrue(populationAnimal.isThreshold3Violated(),"This animal should have violated third threshold.");
						th3HeiferFound = true;
					}
				}
			}
			assertTrue(th3WithHeatHeiferFound,th3WithHeatHeifer.getAnimalTag() +  "("+ th3WithHeatHeifer.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			assertTrue(th1HeiferFound,th1Heifer.getAnimalTag() +  "("+ th1Heifer.getAnimalType() + ") should have been included in the Threshold1 Violation Advisement population");
			assertTrue(th2HeiferFound,th2Heifer.getAnimalTag() +  "("+ th2Heifer.getAnimalType() + ") should have been included in the Threshold2 Violation Advisement population");
			assertTrue(th3HeiferFound,th3Heifer.getAnimalTag() +  "("+ th3Heifer.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-994");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}	
	
	@Test
	void testHeatWarningAdvisement() {
		
		try {
			HeatWarningAdvisement heatWarnAdv = new HeatWarningAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			///////////////////
			
			Animal heatWarningTh1 = createTestAnimal("-999");
			heatWarningTh1.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean heatBeanTh1 = new LifeCycleEventBean();
			heatBeanTh1.setAnimalTag(heatWarningTh1.getAnimalTag());
			heatBeanTh1.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			heatBeanTh1.setEventComments("Test  Event - violates Threshold 1");
			heatBeanTh1.setOrgID("IMD");
			heatBeanTh1.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(19)));
			LifecycleEvent heatInseminationEventTh1 = new LifecycleEvent(heatBeanTh1);
			heatInseminationEventTh1.setCreatedBy(new User("KASHIF"));
			heatInseminationEventTh1.setUpdatedBy(new User("KASHIF"));
			heatInseminationEventTh1.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			heatInseminationEventTh1.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			
			Animal heatWarningTh2 = createTestAnimal("-998");
			heatWarningTh2.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean heatBeanTh2 = new LifeCycleEventBean();
			heatBeanTh2.setAnimalTag(heatWarningTh2.getAnimalTag());
			heatBeanTh2.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			heatBeanTh2.setEventComments("Test  Event - violates Threshold 2");
			heatBeanTh2.setOrgID("IMD");
			heatBeanTh2.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(43)));
			LifecycleEvent heatInseminationEventTh2 = new LifecycleEvent(heatBeanTh2);
			heatInseminationEventTh2.setCreatedBy(new User("KASHIF"));
			heatInseminationEventTh2.setUpdatedBy(new User("KASHIF"));
			heatInseminationEventTh2.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			heatInseminationEventTh2.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));	
			
			Animal heatWarningTh3 = createTestAnimal("-997");
			heatWarningTh3.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean heatBeanTh3 = new LifeCycleEventBean();
			heatBeanTh3.setAnimalTag(heatWarningTh3.getAnimalTag());
			heatBeanTh3.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			heatBeanTh3.setEventComments("Test  Event - violates Threshold 3");
			heatBeanTh3.setOrgID("IMD");
			heatBeanTh3.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(63)));
			LifecycleEvent heatInseminationEventTh3 = new LifecycleEvent(heatBeanTh3);
			heatInseminationEventTh3.setCreatedBy(new User("KASHIF"));
			heatInseminationEventTh3.setUpdatedBy(new User("KASHIF"));
			heatInseminationEventTh3.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			heatInseminationEventTh3.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));				

			Animal heatWarningTh0 = createTestAnimal("-996");
			heatWarningTh0.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean heatBeanTh0 = new LifeCycleEventBean();
			heatBeanTh0.setAnimalTag(heatWarningTh0.getAnimalTag());
			heatBeanTh0.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			heatBeanTh0.setEventComments("Test  Event - does not violate any threshold");
			heatBeanTh0.setOrgID("IMD");
			heatBeanTh0.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(39)));
			LifecycleEvent heatInseminationEventTh0 = new LifecycleEvent(heatBeanTh0);
			heatInseminationEventTh0.setCreatedBy(new User("KASHIF"));
			heatInseminationEventTh0.setUpdatedBy(new User("KASHIF"));
			heatInseminationEventTh0.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			heatInseminationEventTh0.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			
			
			int transactionID0 = animalLoader.insertAnimal(heatWarningTh0);
			heatInseminationEventTh0.setAnimalTag(heatWarningTh0.getAnimalTag());
			heatInseminationEventTh0.setEventNote("Test Insemination Event. This cow should not violate the " + heatWarnAdv.getAdvisementID() + " Advisement");
			eventsLoader.insertLifeCycleEvent(heatInseminationEventTh0);


			int transactionID1 = animalLoader.insertAnimal(heatWarningTh1);
			heatInseminationEventTh1.setAnimalTag(heatWarningTh1.getAnimalTag());
			heatInseminationEventTh1.setEventNote("Test Insemination Event. This cow should violate the " + heatWarnAdv.getAdvisementID() + " Advisement Threshold 1");
			eventsLoader.insertLifeCycleEvent(heatInseminationEventTh1);
			

			int transactionID2 = animalLoader.insertAnimal(heatWarningTh2);
			heatInseminationEventTh2.setAnimalTag(heatWarningTh2.getAnimalTag());
			heatInseminationEventTh2.setEventNote("Test Insemination Event. This cow should violate the " + heatWarnAdv.getAdvisementID() + " Advisement Threshold 2");
			eventsLoader.insertLifeCycleEvent(heatInseminationEventTh2);
		

			int transactionID3 = animalLoader.insertAnimal(heatWarningTh3);
			heatInseminationEventTh3.setAnimalTag(heatWarningTh3.getAnimalTag());
			heatInseminationEventTh3.setEventNote("Test Insemination Event. This cow should violate the " + heatWarnAdv.getAdvisementID() + " Advisement Threshold 3");
			eventsLoader.insertLifeCycleEvent(heatInseminationEventTh3);
						
			assertEquals(1,transactionID0, "Exactly one record -999 should have been inserted");
			assertEquals(1,transactionID1, "Exactly one record -998 should have been inserted");			
			assertEquals(1,transactionID2, "Exactly one record -997 should have been inserted");
			assertEquals(1,transactionID3, "Exactly one record -996 should have been inserted");

			List<Animal> animalPop = heatWarnAdv.applyAdvisementRule("IMD", null);
			boolean th1Found = false;
			boolean th2Found = false;
			boolean th3Found = false;
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText(), Util.WARNING);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(heatWarningTh1.getAnimalTag())) {
						assertTrue(populationAnimal.isInseminated(), " This animal should have been considered inseminated.");
						assertTrue(populationAnimal.isThreshold1Violated(),"This animal "+ heatWarningTh1.getAnimalTag() +  "("+ heatWarningTh0.getAnimalType() + ") should have violated first threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal "+ heatWarningTh1.getAnimalTag() +  "("+ heatWarningTh0.getAnimalType() + ") should have violated first threshold");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal "+ heatWarningTh1.getAnimalTag() +  "("+ heatWarningTh0.getAnimalType() + ") should have violated first threshold");
						th1Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(heatWarningTh2.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal "+ heatWarningTh2.getAnimalTag() +  "("+ heatWarningTh2.getAnimalType() + ") should have violated second threshold");
						assertTrue(populationAnimal.isThreshold2Violated(),"This animal "+ heatWarningTh2.getAnimalTag() +  "("+ heatWarningTh2.getAnimalType() + ") should have violated second threshold");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal "+ heatWarningTh2.getAnimalTag() +  "("+ heatWarningTh2.getAnimalType() + ") should have violated second threshold");
						th2Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(heatWarningTh3.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal "+ heatWarningTh3.getAnimalTag() +  "("+ heatWarningTh3.getAnimalType() + ") should have violated third threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal "+ heatWarningTh3.getAnimalTag() +  "("+ heatWarningTh3.getAnimalType() + ") should have violated third threshold");
						assertTrue(populationAnimal.isThreshold3Violated(),"This animal "+ heatWarningTh3.getAnimalTag() +  "("+ heatWarningTh3.getAnimalType() + ") should have violated third threshold");
						th3Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(heatWarningTh0.getAnimalTag())) {
						fail(heatWarningTh0.getAnimalTag() +  "("+ heatWarningTh0.getAnimalType() + ") should not have violated any threshold" );	
					}
				}
			}
			assertTrue(th1Found,heatWarningTh1.getAnimalTag() +  "("+ heatWarningTh1.getAnimalType() + ") should have been included in the Advisement population");
			assertTrue(th2Found,heatWarningTh2.getAnimalTag() +  "("+ heatWarningTh2.getAnimalType() + ") should have been included in the Advisement population");
			assertTrue(th3Found,heatWarningTh2.getAnimalTag() +  "("+ heatWarningTh3.getAnimalType() + ") should have been included in the Advisement population");
			
			//// CLEAN UP /////
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998"),"Exactly one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997"),"Exactly one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-999"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-998"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-997"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-996"),"Exactly one record should have been deleted");
			/////////
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testDehornAdvisementRulePopulationRetrieval() {
		//IMDLogger.loggingMode = Util.WARNING;

		try {
			DehorningAdvisement dehornAdv = new DehorningAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			///////////////////
			

			Animal femaleCalf = createTestAnimal("-999");
			femaleCalf.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(90));
			femaleCalf.setAnimalType("FEMALECALF");

			Animal maleCalf = createTestAnimal("-998");
			maleCalf.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(90));
			maleCalf.setAnimalType("MALECALF");
			
			Animal dryPregnant = createTestAnimal("-997");
			dryPregnant.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(600));
			dryPregnant.setAnimalType("DRYPREG");

			Animal dehornedCalf = createTestAnimal("-996");
			dehornedCalf.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(90));
			dehornedCalf.setAnimalType("FEMALECALF");
			
			
			LifeCycleEventBean eventBean = new LifeCycleEventBean();
			eventBean.setAnimalTag(maleCalf.getAnimalTag());
			eventBean.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBean.setEventComments("Test Insemination Event");
			eventBean.setOrgID("IMD");
			eventBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(240)));
			LifecycleEvent lcEvent = new LifecycleEvent(eventBean);
			lcEvent.setCreatedBy(new User("KASHIF"));
			lcEvent.setUpdatedBy(new User("KASHIF"));
			lcEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			lcEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			int transactionID1 = animalLoader.insertAnimal(femaleCalf);			
			int transactionID2 = animalLoader.insertAnimal(maleCalf);
			
			int transactionID3 = animalLoader.insertAnimal(dryPregnant);
			lcEvent.setAnimalTag(dryPregnant.getAnimalTag());
			lcEvent.setEventNote("Test Insemination Event. This cow HAS been dried so DryCow advisement should NOT be triggered for this cow.");
			eventsLoader.insertLifeCycleEvent(lcEvent);

			int transactionID4 = animalLoader.insertAnimal(dehornedCalf);
			lcEvent.setAnimalTag(dehornedCalf.getAnimalTag());
			lcEvent.setEventNote("Test dehorning Event. This calf HAS been dehorned so Dehorning advisement should NOT be triggered for this calf.");
			lcEvent.setEventType(new LifeCycleEventCode(Util.LifeCycleEvents.DEHORN,null,null));
			eventsLoader.insertLifeCycleEvent(lcEvent);			
			
			assertEquals(1,transactionID1, "Exactly one record -999 should have been inserted");
			assertEquals(1,transactionID2, "Exactly one record -998 should have been inserted");			
			assertEquals(1,transactionID3, "Exactly one record -997 should have been inserted");
			assertEquals(1,transactionID4, "Exactly one record -996 should have been inserted");

			List<Animal> animalPop = dehornAdv.applyAdvisementRule("IMD", null);
			boolean maleFound = false;
			boolean femaleFound = false;
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText(), Util.WARNING);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(femaleCalf.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This calf should have violated third threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This calf should have violated third threshold");
						assertTrue(populationAnimal.isThreshold3Violated(),"This calf should have violated third threshold");
						femaleFound = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(maleCalf.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This calf should have violated third threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This calf should have violated third threshold");
						assertTrue(populationAnimal.isThreshold3Violated(),"This calf should have violated third threshold");
						maleFound = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(dryPregnant.getAnimalTag())) {
						fail(dryPregnant.getAnimalTag() +  "("+ dryPregnant.getAnimalType() + ") is NOT a calf so it should not have been in the Dehorning Advisement population.");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(dehornedCalf.getAnimalTag())) {
						fail(dehornedCalf.getAnimalTag() +  "("+ dehornedCalf.getAnimalType() + ") is a calf which is already DEHORNED so it should not have been in the Dehorning Advisement population.");
					}
				}
			}
			assertTrue(maleFound,maleCalf.getAnimalTag() +  "("+ maleCalf.getAnimalType() + ") calf should have been included in the Dehorning Advisement population");
			assertTrue(femaleFound,femaleCalf.getAnimalTag() +  "("+ femaleCalf.getAnimalType() + ") calf should have been included in the Dehorning Advisement population");

			// let us remove the dehorning event from the record. This animal should have now been picked up by the Dehorning Advisement
			eventsLoader.deleteAnimalLifecycleEvents("IMD", dehornedCalf.getAnimalTag());
			animalPop = dehornAdv.applyAdvisementRule("IMD", null);
			boolean nowFound = false;
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(dehornedCalf.getAnimalTag())) {
						nowFound = true; 
					}
				}
			}
			assertTrue(nowFound,dehornedCalf.getAnimalTag() +  "("+ maleCalf.getAnimalType() + ") calf should have been included in the Dehorning Advisement population because it doesn't have any dehorning event performed to it");
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}	
	}

	

	@Test
	void testPregnancyTestAdvisementRule() {

		try {
			PregnancyTestAdvisement pregTestAdvisement = new PregnancyTestAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			///////////////////

			Animal notInseminated = createTestAnimal("-999");
			notInseminated.setAnimalType("LACTATING");

			Animal inseminationTh0 = createTestAnimal("-998");
			inseminationTh0.setAnimalType("LCTINSEMIN");
			
			Animal inseminationTh3 = createTestAnimal("-997");
			inseminationTh3.setAnimalType("LCTINSEMIN");

			Animal inseminationTh2 = createTestAnimal("-996");
			inseminationTh2.setAnimalType("LCTINSEMIN");

			Animal inseminationTh1 = createTestAnimal("-995");
			inseminationTh1.setAnimalType("LCTINSEMIN");			
			
			LifeCycleEventBean eventBeanTh3 = new LifeCycleEventBean();
			eventBeanTh3.setAnimalTag(inseminationTh3.getAnimalTag());
			eventBeanTh3.setEventCode(Util.LifeCycleEvents.MATING);
			eventBeanTh3.setEventComments("Test  Event - violates Threshold 3");
			eventBeanTh3.setOrgID("IMD");
			eventBeanTh3.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(210)));
			LifecycleEvent inseminationEventTh3 = new LifecycleEvent(eventBeanTh3);
			inseminationEventTh3.setCreatedBy(new User("KASHIF"));
			inseminationEventTh3.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh3.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEventTh3.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			
			LifeCycleEventBean eventBeanTh2 = new LifeCycleEventBean();
			eventBeanTh2.setAnimalTag(inseminationTh2.getAnimalTag());
			eventBeanTh2.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBeanTh2.setEventComments("Test Event - violates Threshold 2");
			eventBeanTh2.setOrgID("IMD");
			eventBeanTh2.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(75)));
			LifecycleEvent inseminationEventTh2 = new LifecycleEvent(eventBeanTh2);
			inseminationEventTh2.setCreatedBy(new User("KASHIF"));
			inseminationEventTh2.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh2.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEventTh2.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		
			LifeCycleEventBean eventBeanTh1 = new LifeCycleEventBean();
			eventBeanTh1.setAnimalTag(inseminationTh1.getAnimalTag());
			eventBeanTh1.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBeanTh1.setEventComments("Test Event - violates Threshold 1");
			eventBeanTh1.setOrgID("IMD");
			eventBeanTh1.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(60)));
			LifecycleEvent inseminationEventTh1 = new LifecycleEvent(eventBeanTh1);
			inseminationEventTh1.setCreatedBy(new User("KASHIF"));
			inseminationEventTh1.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh1.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEventTh1.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			

			LifeCycleEventBean eventBeanTh0 = new LifeCycleEventBean();
			eventBeanTh0.setAnimalTag(inseminationTh0.getAnimalTag());
			eventBeanTh0.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBeanTh0.setEventComments("Test Pregrnancy Test Event - no violation");
			eventBeanTh0.setOrgID("IMD");
			eventBeanTh0.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(30)));
			LifecycleEvent inseminationEventTh0 = new LifecycleEvent(eventBeanTh0);
			inseminationEventTh0.setCreatedBy(new User("KASHIF"));
			inseminationEventTh0.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh0.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEventTh0.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));			
			

			int transNotInseminated = animalLoader.insertAnimal(notInseminated);

			int transTh0 = animalLoader.insertAnimal(inseminationTh0);
			inseminationEventTh0.setEventNote("Test Insemination Event. This animal should not violate any threshold.");
			inseminationEventTh0.setAnimalTag(eventBeanTh0.getAnimalTag());
			eventsLoader.insertLifeCycleEvent(inseminationEventTh0);
			
			int transTh3 = animalLoader.insertAnimal(inseminationTh3);
			inseminationEventTh3.setAnimalTag(inseminationTh3.getAnimalTag());
			inseminationEventTh3.setEventNote("Test Insemination Event. This cow violated TH3.");
			eventsLoader.insertLifeCycleEvent(inseminationEventTh3);

			int transTh2 = animalLoader.insertAnimal(inseminationTh2);
			inseminationEventTh2.setAnimalTag(inseminationTh2.getAnimalTag());
			inseminationEventTh2.setEventNote("Test Insemination Event. This cow violated TH2.");
			eventsLoader.insertLifeCycleEvent(inseminationEventTh2);

			int transTh1 = animalLoader.insertAnimal(inseminationTh1);
			inseminationEventTh1.setAnimalTag(inseminationTh1.getAnimalTag());
			inseminationEventTh1.setEventNote("Test Insemination Event. This cow violated TH1.");
			eventsLoader.insertLifeCycleEvent(inseminationEventTh1);
			
			
			assertEquals(1,transNotInseminated, "Exactly one record -999 should have been inserted");
			assertEquals(1,transTh0, "Exactly one record -998 should have been inserted");			
			assertEquals(1,transTh3, "Exactly one record -997 should have been inserted");
			assertEquals(1,transTh2, "Exactly one record -996 should have been inserted");
			assertEquals(1,transTh1, "Exactly one record -995 should have been inserted");

			List<Animal> animalPop = pregTestAdvisement.applyAdvisementRule("IMD", null);
			boolean inseminationTh3Found = false;
			boolean inseminationTh2Found = false;
			boolean inseminationTh1Found = false;
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(inseminationTh0.getAnimalTag())) {
						fail(inseminationTh0.getAnimalTag() +  "("+ inseminationTh0.getAnimalType() + ") has been inseminated recently it should not have violated any Threshold");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(notInseminated.getAnimalTag())) {
						fail(notInseminated.getAnimalTag() +  "("+ notInseminated.getAnimalType() + ") has never been inseminated it should not have violated any Threshold");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(inseminationTh3.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated third threshold.");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated third threshold.");
						assertTrue(populationAnimal.isThreshold3Violated(),"This animal should have violated third threshold.");
						inseminationTh3Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(inseminationTh2.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated second threshold");
						assertTrue(populationAnimal.isThreshold2Violated(),"This animal should have violated second threshold");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated second threshold.");
						inseminationTh2Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(inseminationTh1.getAnimalTag())) {
						assertTrue(populationAnimal.isThreshold1Violated(),"This animal should have violated first threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated first threshold");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated first threshold.");
						inseminationTh1Found = true;
					}
				}
			}
			assertTrue(inseminationTh3Found,inseminationTh3.getAnimalTag() +  "("+ inseminationTh3.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			assertTrue(inseminationTh2Found,inseminationTh2.getAnimalTag() +  "("+ inseminationTh2.getAnimalType() + ") should have been included in the Threshold2 Violation Advisement population");
			assertTrue(inseminationTh1Found,inseminationTh1.getAnimalTag() +  "("+ inseminationTh1.getAnimalType() + ") should have been included in the Threshold1 Violation Advisement population");
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}	
	}
	
	
	
	@Test
	void testFMDVaccinationAdvisementRule() {

		try {
			FMDVaccinationAdvisement fmd = new FMDVaccinationAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-994");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994");
			///////////////////
			

			Animal noVaccine = createTestAnimal("-999");
			noVaccine.setAnimalType("LACTATING");

			Animal vaccineOk = createTestAnimal("-998");
			vaccineOk.setAnimalType("LCTPRGNT");
			
			Animal vaccineTh3 = createTestAnimal("-997");
			vaccineTh3.setAnimalType("DRYPREG");

			Animal vaccineTh2 = createTestAnimal("-996");
			vaccineTh2.setAnimalType("DRYPREG");

			Animal vaccineTh1 = createTestAnimal("-995");
			vaccineTh1.setAnimalType("DRYPREG");			

			Animal vaccineButNotFMD = createTestAnimal("-994");
			vaccineButNotFMD.setAnimalType("DRYPREG");

			LifeCycleEventBean eventBeanNotFMD = new LifeCycleEventBean();
			eventBeanNotFMD.setAnimalTag(vaccineOk.getAnimalTag());
			eventBeanNotFMD.setEventCode(Util.LifeCycleEvents.VACCINE);
			eventBeanNotFMD.setEventComments("Test FMD Vaccination Event - no violation");
			eventBeanNotFMD.setOrgID("IMD");
			eventBeanNotFMD.setAuxField1Value("BQ");
			
			eventBeanNotFMD.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(149)));
			LifecycleEvent inseminationEventNoFMD = new LifecycleEvent(eventBeanNotFMD);
			inseminationEventNoFMD.setCreatedBy(new User("KASHIF"));
			inseminationEventNoFMD.setUpdatedBy(new User("KASHIF"));
			inseminationEventNoFMD.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEventNoFMD.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEventNoFMD.setEventNote("Test Vaccination Event. This animal should not violate any threshold.");
			inseminationEventNoFMD.setAnimalTag(vaccineButNotFMD.getAnimalTag());

			int transNotFMD = animalLoader.insertAnimal(vaccineButNotFMD);
			eventsLoader.insertLifeCycleEvent(inseminationEventNoFMD);
			
			
			
			
			LifeCycleEventBean eventBeanTh3 = new LifeCycleEventBean();
			eventBeanTh3.setAnimalTag(vaccineTh3.getAnimalTag());
			eventBeanTh3.setEventCode(Util.LifeCycleEvents.VACCINE);
			eventBeanTh3.setEventComments("Test FMD Vaccination Event - violates Threshold 3");
			eventBeanTh3.setOrgID("IMD");
			eventBeanTh3.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(210)));
			LifecycleEvent inseminationEventTh3 = new LifecycleEvent(eventBeanTh3);
			inseminationEventTh3.setCreatedBy(new User("KASHIF"));
			inseminationEventTh3.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh3.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEventTh3.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			
			LifeCycleEventBean eventBeanTh2 = new LifeCycleEventBean();
			eventBeanTh2.setAnimalTag(vaccineTh2.getAnimalTag());
			eventBeanTh2.setEventCode(Util.LifeCycleEvents.VACCINE);
			eventBeanTh2.setEventComments("Test FMD Vaccination Event - violates Threshold 2");
			eventBeanTh2.setOrgID("IMD");
			eventBeanTh2.setAuxField1Value("FOOT&MOUTH");
			eventBeanTh2.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(175)));
			LifecycleEvent inseminationEventTh2 = new LifecycleEvent(eventBeanTh2);
			inseminationEventTh2.setCreatedBy(new User("KASHIF"));
			inseminationEventTh2.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh2.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEventTh2.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		
			LifeCycleEventBean eventBeanTh1 = new LifeCycleEventBean();
			eventBeanTh1.setAnimalTag(vaccineTh1.getAnimalTag());
			eventBeanTh1.setEventCode(Util.LifeCycleEvents.VACCINE);
			eventBeanTh1.setEventComments("Test FMD Vaccination Event - violates Threshold 1");
			eventBeanTh1.setOrgID("IMD");
			eventBeanTh1.setAuxField1Value("FOOT&MOUTH");

			eventBeanTh1.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(151)));
			LifecycleEvent inseminationEventTh1 = new LifecycleEvent(eventBeanTh1);
			inseminationEventTh1.setCreatedBy(new User("KASHIF"));
			inseminationEventTh1.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh1.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEventTh1.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			

			LifeCycleEventBean eventBeanTh0 = new LifeCycleEventBean();
			eventBeanTh0.setAnimalTag(vaccineOk.getAnimalTag());
			eventBeanTh0.setEventCode(Util.LifeCycleEvents.VACCINE);
			eventBeanTh0.setEventComments("Test FMD Vaccination Event - no violation");
			eventBeanTh0.setOrgID("IMD");
			eventBeanTh0.setAuxField1Value("FOOT&MOUTH");
			
			eventBeanTh0.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(149)));
			LifecycleEvent inseminationEventTh0 = new LifecycleEvent(eventBeanTh0);
			inseminationEventTh0.setCreatedBy(new User("KASHIF"));
			inseminationEventTh0.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh0.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEventTh0.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));			
			

			int transNoVaccine = animalLoader.insertAnimal(noVaccine);

			int transVaccineOk = animalLoader.insertAnimal(vaccineOk);
			inseminationEventTh0.setEventNote("Test Vaccination Event. This animal should not violate any threshold.");
			inseminationEventTh0.setAnimalTag(vaccineOk.getAnimalTag());
			eventsLoader.insertLifeCycleEvent(inseminationEventTh0);
			
			int transVaccineTh3 = animalLoader.insertAnimal(vaccineTh3);
			inseminationEventTh3.setAnimalTag(vaccineTh3.getAnimalTag());
			inseminationEventTh3.setEventNote("Test Vaccination Event. This cow violated TH3.");
			eventsLoader.insertLifeCycleEvent(inseminationEventTh3);


			int transVaccineTh2 = animalLoader.insertAnimal(vaccineTh2);
			inseminationEventTh2.setAnimalTag(vaccineTh2.getAnimalTag());
			inseminationEventTh2.setEventNote("Test Vaccination Event. This cow violated TH2.");
			eventsLoader.insertLifeCycleEvent(inseminationEventTh2);

			int transVaccineTh1 = animalLoader.insertAnimal(vaccineTh1);
			inseminationEventTh1.setAnimalTag(vaccineTh1.getAnimalTag());
			inseminationEventTh1.setEventNote("Test Insemination Event. This cow violated TH1.");
			eventsLoader.insertLifeCycleEvent(inseminationEventTh1);
			
			assertEquals(1,transNoVaccine,  "Exactly one record -999 should have been inserted");
			assertEquals(1,transVaccineOk,  "Exactly one record -998 should have been inserted");			
			assertEquals(1,transVaccineTh3, "Exactly one record -997 should have been inserted");
			assertEquals(1,transVaccineTh2, "Exactly one record -996 should have been inserted");
			assertEquals(1,transVaccineTh1, "Exactly one record -995 should have been inserted");
			assertEquals(1,transNotFMD,     "Exactly one record -994 should have been inserted");

			List<Animal> animalPop = fmd.applyAdvisementRule("IMD", null);
			boolean th3Found = false;
			boolean th2Found = false;
			boolean th1Found = false;
			boolean noVaccineFound = false;
			boolean vaccineButNotFMDFound = false;
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(vaccineOk.getAnimalTag())) {
						fail(vaccineOk.getAnimalTag() +  "("+ vaccineOk.getAnimalType() + ") has been vaccinated recently it should not have violated the Threshold");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(noVaccine.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated third threshold. It was never vaccinated");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated third threshold. It was never vaccinated");
						assertTrue(populationAnimal.isThreshold3Violated(),"This animal should have violated third threshold. It was never vaccinated");
						noVaccineFound = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(vaccineButNotFMD.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated third threshold. It was never vaccinated");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated third threshold. It was never vaccinated");
						assertTrue(populationAnimal.isThreshold3Violated(),"This animal should have violated third threshold. It was never vaccinated");
						vaccineButNotFMDFound = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(vaccineTh3.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated third threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated third threshold");
						assertTrue(populationAnimal.isThreshold3Violated(),"This animal should have violated third threshold.");
						th3Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(vaccineTh2.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated Second threshold");
						assertTrue(populationAnimal.isThreshold2Violated(),"This animal should have violated Second threshold");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated Second threshold.");
						th2Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(vaccineTh1.getAnimalTag())) {
						assertTrue(populationAnimal.isThreshold1Violated(),populationAnimal.getAnimalTag() + " should have violated First threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated First threshold");
						assertFalse(populationAnimal.isThreshold3Violated(),"This animal should have violated First threshold.");
						th1Found = true;
					}
				}
			}
			assertTrue(noVaccineFound,noVaccine.getAnimalTag() +  "("+ noVaccine.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			assertTrue(vaccineButNotFMDFound,vaccineButNotFMD.getAnimalTag() +  "("+ vaccineButNotFMD.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			assertTrue(th1Found,vaccineTh1.getAnimalTag() +  "("+ vaccineTh1.getAnimalType() + ") should have been included in the Threshold1 Violation Advisement population");
			assertTrue(th2Found,vaccineTh2.getAnimalTag() +  "("+ vaccineTh2.getAnimalType() + ") should have been included in the Threshold2 Violation Advisement population");
			assertTrue(th3Found,vaccineTh3.getAnimalTag() +  "("+ vaccineTh3.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-994");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}
	
	@Test
	void testMastitisAdvisementRule() {

		int originalLoggingMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;

		try {
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994");
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-994");
			///////////////////
			


			Animal notLactating = createTestAnimal("-994");
			notLactating.setAnimalType(Util.AnimalTypes.DRYPREG);
						
			Animal medTestOk = createTestAnimal("-998");
			medTestOk.setAnimalType(Util.AnimalTypes.LCTPRGNT);
			
			
			Animal medTestTh3 = createTestAnimal("-997");
			medTestTh3.setAnimalType(Util.AnimalTypes.LCTINSEMIN);

			Animal noMedTest = createTestAnimal("-999");
			noMedTest.setAnimalType(Util.AnimalTypes.LACTATING);
			
			Animal medTestTh2 = createTestAnimal("-995");
			medTestTh2.setAnimalType(Util.AnimalTypes.LCTAWTHEAT);			


			User user = new User("KASHIF");
			DateTime now = DateTime.now(IMDProperties.getServerTimeZone());

			LifecycleEvent mastitisEventTh3Violated = new LifecycleEvent(medTestTh3.getOrgId(), 0, 
					medTestTh3.getAnimalTag(), Util.LifeCycleEvents.MEDICALTST, user, now, 
					user, now);
			mastitisEventTh3Violated.setAuxField1Value(Util.AdvisementRules.MASTITIS);
			mastitisEventTh3Violated.setEventNote("Very Old Mastitis Test.");
			mastitisEventTh3Violated.setEventTimeStamp(now.minusDays(22));

			
			LifecycleEvent mastitisEventTh2Violated = new LifecycleEvent(medTestTh2.getOrgId(), 0, 
					medTestTh2.getAnimalTag(), Util.LifeCycleEvents.MEDICALTST, user, now, 
					user, now);
			mastitisEventTh2Violated.setAuxField1Value(Util.AdvisementRules.MASTITIS);
			mastitisEventTh2Violated.setEventNote("Older Mastitis Test.");
			mastitisEventTh2Violated.setEventTimeStamp(now.minusDays(17));
			
			LifecycleEvent mastitisEventNoViolation = new LifecycleEvent(medTestOk.getOrgId(), 0, 
					medTestOk.getAnimalTag(), Util.LifeCycleEvents.MEDICALTST, user, now, 
					user, now);
			mastitisEventNoViolation.setAuxField1Value(Util.AdvisementRules.MASTITIS);
			mastitisEventNoViolation.setEventNote("Recent Mastitis test.");
			mastitisEventNoViolation.setEventTimeStamp(now.minusDays(1));
			

			assertEquals(1,animalLoader.insertAnimal(medTestOk));
			assertEquals(1,animalLoader.insertAnimal(notLactating));
			assertEquals(1,animalLoader.insertAnimal(noMedTest));
			assertEquals(1,animalLoader.insertAnimal(medTestTh2));
			assertEquals(1,animalLoader.insertAnimal(medTestTh3));
			assertTrue(eventsLoader.insertLifeCycleEvent(mastitisEventNoViolation) > 0);
			assertTrue(eventsLoader.insertLifeCycleEvent(mastitisEventTh2Violated) > 0);
			assertTrue(eventsLoader.insertLifeCycleEvent(mastitisEventTh3Violated) > 0);
			
			MastitisTestAdvisement adv = new MastitisTestAdvisement();
			List<Animal> animalPop = adv.applyAdvisementRule(medTestOk.getOrgId(), null);
			
			

			boolean noMedTestFound = false;
			boolean medTestTh2Found = false;
			boolean medTestTh3Found = false;
			
			if (animalPop != null) {
			
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					assertFalse(populationAnimal.getAnimalTag().equalsIgnoreCase(notLactating.getAnimalTag()), notLactating.getAnimalTag() + " was not lactating so it should not have been included in the advisement");
					assertFalse(populationAnimal.getAnimalTag().equalsIgnoreCase(medTestOk.getAnimalTag()), medTestOk.getAnimalTag() + " was tested recently so it should not have been included in the advisement");
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(noMedTest.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),noMedTest.getAnimalTag() + " should have violated third threshold. It was never tested for mastitis");
						assertFalse(populationAnimal.isThreshold2Violated(),noMedTest.getAnimalTag() + " should have violated third threshold. It was never tested for mastitis");
						assertTrue(populationAnimal.isThreshold3Violated(),noMedTest.getAnimalTag()  + " should have violated third threshold. It was never tested for mastitis");
						noMedTestFound = true;
					} else 	if (populationAnimal.getAnimalTag().equalsIgnoreCase(medTestTh2.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),medTestTh2.getAnimalTag() + " should have violated second threshold. It was tested for mastitis between 16-20 days");
						assertTrue(populationAnimal.isThreshold2Violated(),medTestTh2.getAnimalTag() + " should have violated second threshold. It was tested for mastitis between 16-20 days");
						assertFalse(populationAnimal.isThreshold3Violated(),medTestTh2.getAnimalTag() + " should have violated second threshold. It was tested for mastitis between 16-20 days");
						medTestTh2Found = true;
					} else 	if (populationAnimal.getAnimalTag().equalsIgnoreCase(medTestTh3.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),medTestTh3.getAnimalTag() + " should have violated third threshold. It was tested for mastitis more than 21 days ago");
						assertFalse(populationAnimal.isThreshold2Violated(),medTestTh3.getAnimalTag() + " should have violated third threshold. It was tested for mastitis more than 21 days ago");
						assertTrue(populationAnimal.isThreshold3Violated(),medTestTh3.getAnimalTag() + " should have violated third threshold. It was tested for mastitis more than 21 days ago");
						medTestTh3Found = true;
					}

				}
			}
			assertTrue(noMedTestFound,noMedTest.getAnimalTag() +  "("+ noMedTest.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			assertTrue(medTestTh2Found,medTestTh2.getAnimalTag() +  "("+ medTestTh2.getAnimalType() + ") should have been included in the Threshold2 Violation Advisement population");
			assertTrue(medTestTh3Found,medTestTh3.getAnimalTag() +  "("+ medTestTh3.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			
				
			///// clean up /////
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994");
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-994");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		} finally {
			IMDLogger.loggingMode = originalLoggingMode;
		}
	}
	
	@Test
	void testDryCowAdvisementRulePopulationRetrieval() {

		try {
			DryCowAdvisement dryAdv = new DryCowAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			///////////////////
			
			Animal lactatingNotPregnant = createTestAnimal("-999");
			lactatingNotPregnant.setAnimalType("LACTATING");

			Animal lactatingNonDryPregnantThreshold3 = createTestAnimal("-998");
			lactatingNonDryPregnantThreshold3.setAnimalType("LCTPRGNT");
			
			Animal dryPregnant = createTestAnimal("-997");
			dryPregnant.setAnimalType("DRYPREG");

			Animal lactatingNonDryPregnantThreshold1 = createTestAnimal("-996");
			lactatingNonDryPregnantThreshold1.setAnimalType("LCTPRGNT");			
			
			LifeCycleEventBean eventBean1 = new LifeCycleEventBean();
			eventBean1.setAnimalTag(lactatingNonDryPregnantThreshold3.getAnimalTag());
			eventBean1.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBean1.setEventComments("Test Insemination Event");
			eventBean1.setOrgID("IMD");
			eventBean1.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(240)));
			LifecycleEvent inseminationEvent1 = new LifecycleEvent(eventBean1);
			inseminationEvent1.setCreatedBy(new User("KASHIF"));
			inseminationEvent1.setUpdatedBy(new User("KASHIF"));
			inseminationEvent1.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEvent1.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));

			LifeCycleEventBean eventBean2 = new LifeCycleEventBean();
			eventBean2.setAnimalTag(lactatingNonDryPregnantThreshold1.getAnimalTag());
			eventBean2.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBean2.setEventComments("Test Insemination Event");
			eventBean2.setOrgID("IMD");
			eventBean2.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(180)));
			LifecycleEvent inseminationEvent2 = new LifecycleEvent(eventBean2);
			inseminationEvent2.setCreatedBy(new User("KASHIF"));
			inseminationEvent2.setUpdatedBy(new User("KASHIF"));
			inseminationEvent2.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEvent2.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));

			int transactionID1 = animalLoader.insertAnimal(lactatingNotPregnant);
			
			int transactionID2 = animalLoader.insertAnimal(lactatingNonDryPregnantThreshold3);
			inseminationEvent1.setEventNote("Test Insemination Event. This cow has NOT yet been dried so DryCow advisement THRESHOLD3 SHOULD be triggered for this cow.");
			inseminationEvent1.setAnimalTag(lactatingNonDryPregnantThreshold3.getAnimalTag());
			eventsLoader.insertLifeCycleEvent(inseminationEvent1);
			
			int transactionID3 = animalLoader.insertAnimal(dryPregnant);
			inseminationEvent2.setAnimalTag(dryPregnant.getAnimalTag());
			inseminationEvent2.setEventNote("Test Insemination Event. This cow HAS been dried so DryCow advisement should NOT be triggered for this cow.");
			eventsLoader.insertLifeCycleEvent(inseminationEvent2);

			int transactionID4 = animalLoader.insertAnimal(lactatingNonDryPregnantThreshold1);
			inseminationEvent2.setEventNote("Test Insemination Event. This cow has NOT yet been dried so DryCow advisement THRESHOLD1 SHOULD be triggered for this cow.");
			inseminationEvent2.setAnimalTag(lactatingNonDryPregnantThreshold1.getAnimalTag());
			eventsLoader.insertLifeCycleEvent(inseminationEvent2);
			
			assertEquals(1,transactionID1, "Exactly one record -999 should have been inserted");
			assertEquals(1,transactionID2, "Exactly one record -998 should have been inserted");			
			assertEquals(1,transactionID3, "Exactly one record -997 should have been inserted");
			assertEquals(1,transactionID4, "Exactly one record -997 should have been inserted");

			List<Animal> animalPop = dryAdv.applyAdvisementRule("IMD", null);
			boolean th3Found = false;
			boolean th1Found = false;
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(lactatingNotPregnant.getAnimalTag())) {
						fail(lactatingNotPregnant.getAnimalTag() +  "("+ lactatingNotPregnant.getAnimalType() + ") is lactating and not pregnant so it should not be in the DryCow Advisement population");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(lactatingNonDryPregnantThreshold3.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This cow should have violated third threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This cow should have violated third threshold");
						assertTrue(populationAnimal.isThreshold3Violated(),"This cow should have violated third threshold");
						th3Found = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(dryPregnant.getAnimalTag())) {
						fail(dryPregnant.getAnimalTag() +  "("+ dryPregnant.getAnimalType() + ") cow was already dry so it should not have been in the DryCow Advisement population.");
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(lactatingNonDryPregnantThreshold1.getAnimalTag())) {
						assertTrue(populationAnimal.isThreshold1Violated(),"This cow should have violated first threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This cow should have violated first threshold");
						assertFalse(populationAnimal.isThreshold3Violated(),"This cow should have violated first threshold");
						th1Found = true;
					}
				}
			}
			assertTrue(th1Found,lactatingNonDryPregnantThreshold1.getAnimalTag() +  "("+ lactatingNonDryPregnantThreshold1.getAnimalType() + ") cow should have been included in the DryCow Threshold1 Advisement population");
			assertTrue(th3Found,lactatingNonDryPregnantThreshold3.getAnimalTag() +  "("+ lactatingNonDryPregnantThreshold3.getAnimalType() + ") cow should have been included in the DryCow Threshold3 Advisement population");
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}

	@Test
	void testPreCalvingFeedPopulationRetrieval() {

		try {
			IMDLogger.loggingMode = Util.INFO;
			CalvingPrepFeedAdvisement preCalveFeedAdv = new CalvingPrepFeedAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			///////////////////
			
			Animal dryPregnant = createTestAnimal("-999");
			dryPregnant.setAnimalType(Util.AnimalTypes.DRYPREG);

			Animal lactatingPregnantThreshold3 = createTestAnimal("-998");
			lactatingPregnantThreshold3.setAnimalType("LCTPRGNT");
			
			
			LifeCycleEventBean eventBean1 = new LifeCycleEventBean();
			eventBean1.setAnimalTag(dryPregnant.getAnimalTag());
			eventBean1.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBean1.setEventComments("Test Insemination Event");
			eventBean1.setOrgID("IMD");
			eventBean1.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(265)));
			LifecycleEvent inseminationEvent1 = new LifecycleEvent(eventBean1);
			inseminationEvent1.setEventNote("Test Insemination Event. This cow has NOT yet been give pre-calving feed. So Pre Calving Feed advisement THRESHOLD3 SHOULD be triggered for this cow.");
			inseminationEvent1.setAnimalTag(dryPregnant.getAnimalTag());
			inseminationEvent1.setCreatedBy(new User("KASHIF"));
			inseminationEvent1.setUpdatedBy(new User("KASHIF"));
			inseminationEvent1.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(265));
			inseminationEvent1.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));

			LifeCycleEventBean eventBean2 = new LifeCycleEventBean();
			eventBean2.setAnimalTag(dryPregnant.getAnimalTag());
			eventBean2.setEventCode(Util.LifeCycleEvents.PRECAVNGFD);
			eventBean2.setEventComments("Pre-Calving Feed GLUCOSA Event");
			eventBean2.setOrgID("IMD");
			eventBean2.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(31)));
			LifecycleEvent glucosaEvent = new LifecycleEvent(eventBean2);
			glucosaEvent.setAuxField1Value(Util.FeedItems.GLYCOLINE);
			glucosaEvent.setAuxField2Value(Util.YES);
			glucosaEvent.setCreatedBy(new User("KASHIF"));
			glucosaEvent.setUpdatedBy(new User("KASHIF"));
			glucosaEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			glucosaEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));

			int transactionID1 = animalLoader.insertAnimal(dryPregnant);
			assertEquals(1,transactionID1);
			eventsLoader.insertLifeCycleEvent(inseminationEvent1);
			
			List<Animal> animalPop = preCalveFeedAdv.applyAdvisementRule(dryPregnant.getOrgId(), null);

			boolean th3Found = false;
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(dryPregnant.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This cow should have violated third threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This cow should have violated third threshold");
						assertTrue(populationAnimal.isThreshold3Violated(),"This cow should have violated third threshold");
						th3Found = true;
					}
				}
			}
			assertTrue(th3Found,dryPregnant.getAnimalTag() +  "("+ dryPregnant.getAnimalType() + ") cow should have been included in the Pre Calving Feed Threshold3 Advisement population");
			
			
			// now we add the feed event and the threshold violation should go away.
			eventsLoader.insertLifeCycleEvent(glucosaEvent);
			animalPop = preCalveFeedAdv.applyAdvisementRule(dryPregnant.getOrgId(), null);

			th3Found = false;
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(dryPregnant.getAnimalTag())) {
						assertTrue(populationAnimal.getNote(1).getNoteText().indexOf(Util.FeedItems.GLUCOSA) < 0);
						assertTrue(populationAnimal.getNote(1).getNoteText().indexOf(Util.FeedItems.VANDA) > 0);
						// we have effectively disabled the OIL advisement by setting unachievable threshold in DB
						assertTrue(populationAnimal.getNote(1).getNoteText().indexOf(Util.FeedItems.OIL) < 0);
						th3Found = true;
					}
				}
			}
			assertTrue(th3Found,dryPregnant.getAnimalTag() +  "("+ dryPregnant.getAnimalType() + ") cow should have been included in the Pre Calving Feed Threshold3 Advisement population");
			
			
			
			eventBean2 = new LifeCycleEventBean();
			eventBean2.setAnimalTag(dryPregnant.getAnimalTag());
			eventBean2.setEventCode(Util.LifeCycleEvents.PRECAVNGFD);
			eventBean2.setEventComments("Pre-Calving Feed VANDA Event");
			eventBean2.setOrgID("IMD");
			eventBean2.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(50)));
			glucosaEvent = new LifecycleEvent(eventBean2);
			glucosaEvent.setAuxField1Value(Util.FeedItems.VANDA);
			glucosaEvent.setAuxField2Value(Util.YES);
			glucosaEvent.setCreatedBy(new User("KASHIF"));
			glucosaEvent.setUpdatedBy(new User("KASHIF"));
			glucosaEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			glucosaEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));

			assertTrue(eventsLoader.insertLifeCycleEvent(glucosaEvent)>0);

			animalPop = preCalveFeedAdv.applyAdvisementRule(dryPregnant.getOrgId(), null);
			th3Found = false;
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(dryPregnant.getAnimalTag())) {
						assertTrue(populationAnimal.getNote(1).getNoteText().indexOf(Util.FeedItems.VANDA) < 0, populationAnimal.getNote(1).getNoteText());
						assertTrue(populationAnimal.getNote(1).getNoteText().indexOf(Util.FeedItems.GLUCOSA) < 0, populationAnimal.getNote(1).getNoteText());
						assertTrue(populationAnimal.getNote(1).getNoteText().indexOf(Util.FeedItems.OIL) < 0, populationAnimal.getNote(1).getNoteText());
						th3Found = true;
					}
				}
			}
//			assertFalse(th3Found,dryPregnant.getAnimalTag() +  "("+ dryPregnant.getAnimalType() + ") cow should have been included in the Pre Calving Feed Threshold3 Advisement population");
			assertFalse(th3Found,dryPregnant.getAnimalTag() +  "("+ dryPregnant.getAnimalType() + ") cow should NOT have been included in the Pre Calving Feed Threshold3 Advisement population");
			
			eventBean2 = new LifeCycleEventBean();
			eventBean2.setAnimalTag(dryPregnant.getAnimalTag());
			eventBean2.setEventCode(Util.LifeCycleEvents.PRECAVNGFD);
			eventBean2.setEventComments("Pre-Calving Feed OIL Event");
			eventBean2.setOrgID("IMD");
			eventBean2.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(51)));
			glucosaEvent = new LifecycleEvent(eventBean2);
			glucosaEvent.setAuxField1Value(Util.FeedItems.OIL);
			glucosaEvent.setAuxField2Value(Util.YES);
			glucosaEvent.setCreatedBy(new User("KASHIF"));
			glucosaEvent.setUpdatedBy(new User("KASHIF"));
			glucosaEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			glucosaEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));

			assertTrue(eventsLoader.insertLifeCycleEvent(glucosaEvent)>0);
			
			
			
			animalPop = preCalveFeedAdv.applyAdvisementRule(dryPregnant.getOrgId(), null);

			th3Found = false;
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(dryPregnant.getAnimalTag())) {
						th3Found = true;
					}
				}
			}
			assertFalse(th3Found,dryPregnant.getAnimalTag() +  "("+ dryPregnant.getAnimalType() + ") cow should NOT have been included in the Pre Calving Feed Threshold3 Advisement population");
			
			
			
			
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}
	@Test
	void testDeWormingAdvisementRule() {
		//IMDLogger.loggingMode = Util.ERROR;

		try {			
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-994");
			animalLoader.deleteAnimal("IMD", "-993");
			animalLoader.deleteAnimal("IMD", "-992");
			
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-993");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-992");
			///////////////////			
						
			Animal dewormOk = createTestAnimal("-992");
			dewormOk.setAnimalType(Util.AnimalTypes.FEMALECALF);
			assertEquals(1,animalLoader.insertAnimal(dewormOk));
			assertTrue(insertEvent(dewormOk.getAnimalTag(), "DOES NOT Violate ANY Threshold", Util.LifeCycleEvents.DEWORM, DateTime.now(IMDProperties.getServerTimeZone()).minusDays(10)) >= 0);
			

			Animal dewormTh1 = createTestAnimal("-993");
			dewormTh1.setAnimalType(Util.AnimalTypes.FEMALECALF);
			assertEquals(1,animalLoader.insertAnimal(dewormTh1));
			assertTrue(insertEvent(dewormTh1.getAnimalTag(), "Violates Threshold 1", Util.LifeCycleEvents.DEWORM, DateTime.now(IMDProperties.getServerTimeZone()).minusDays(118)) >= 0);

			Animal dewormTh2 = createTestAnimal("-994");
			dewormTh2.setAnimalType(Util.AnimalTypes.FEMALECALF);
			assertEquals(1,animalLoader.insertAnimal(dewormTh2));
			assertTrue(insertEvent(dewormTh2.getAnimalTag(), "Violates Threshold 2", Util.LifeCycleEvents.DEWORM, DateTime.now(IMDProperties.getServerTimeZone()).minusDays(121)) >= 0);
			
			
			Animal dewormTh3 = createTestAnimal("-995");
			dewormTh3.setAnimalType(Util.AnimalTypes.DRYINSEMIN);
			assertEquals(1,animalLoader.insertAnimal(dewormTh3));
			assertTrue(insertEvent(dewormTh3.getAnimalTag(), "Violates Threshold 3", Util.LifeCycleEvents.DEWORM, DateTime.now(IMDProperties.getServerTimeZone()).minusDays(Util.HTTPCodes.OK)) >= 0);
			
			boolean th1Found = false;
			boolean th2Found = false;
			boolean th3Found = false;
			DewormingAdvisement advisement = new DewormingAdvisement();
			List<Animal> results = advisement.applyAdvisementRule("IMD", null);
			Iterator<Animal> it = results.iterator();
			while (it.hasNext()) {
				Animal populationAnimal = it.next();
				if (populationAnimal.getAnimalTag().equalsIgnoreCase(dewormOk.getAnimalTag())) {
					fail(dewormOk.getAnimalTag() +  "("+ dewormOk.getAnimalType() + ") should not be in the " + advisement.getAdvisementID() + "  Advisement population");
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(dewormTh3.getAnimalTag())) {
					th3Found = true;
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(dewormTh2.getAnimalTag())) {
					th2Found = true;
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(dewormTh1.getAnimalTag())) {
					th1Found = true;
				}
			}
			assertTrue(th3Found,dewormTh3.getAnimalTag() +  "("+ dewormTh3.getAnimalType() + ") cow should have been included in the Deworming Advisement population");
			assertTrue(th2Found,dewormTh2.getAnimalTag() +  "("+ dewormTh2.getAnimalType() + ") cow should have been included in the Deworming Advisement population");
			assertTrue(th1Found,dewormTh1.getAnimalTag() +  "("+ dewormTh1.getAnimalType() + ") cow should have been included in the Deworming Advisement population");
			
			///// clean up /////
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995"),"We added a Lifecycle event so one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994"),"We added a Lifecycle event so one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-993"),"We added a Lifecycle event so one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-992"),"We added a Lifecycle event so one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-995"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-994"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-993"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-992"),"Exactly one record should have been deleted");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}

	@Test
	void testAdvisementManager() {
		IMDLogger.loggingMode = Util.INFO;

		try {			
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-993");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-992");

			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-994");
			animalLoader.deleteAnimal("IMD", "-993");
			animalLoader.deleteAnimal("IMD", "-992");
			
			///////////////////			
						
			Animal dewormTh1 = createTestAnimal("-992");
			dewormTh1.setAnimalType("FEMALECALF");
			

			Animal lactatingNotPregnant = createTestAnimal("-999");
			lactatingNotPregnant.setAnimalType("LACTATING");

			Animal lactatingNonDryPregnant = createTestAnimal("-998");
			lactatingNonDryPregnant.setAnimalType("LCTPRGNT");
			
			Animal dryPregnant = createTestAnimal("-997");
			dryPregnant.setAnimalType("DRYPREG");
			
			Animal nonDehornedCalf = createTestAnimal("-996");
			nonDehornedCalf.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(90));
			nonDehornedCalf.setAnimalType("FEMALECALF");

			Animal nonFmd = createTestAnimal("-995");
			nonFmd.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(90));
			nonFmd.setAnimalType("FEMALECALF");

			Animal inseminationAnimalTh3 = createTestAnimal("-994");
			inseminationAnimalTh3.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean inseminationBeanTh3 = new LifeCycleEventBean();
			inseminationBeanTh3.setAnimalTag(inseminationAnimalTh3.getAnimalTag());
			inseminationBeanTh3.setEventCode(Util.LifeCycleEvents.MATING);
			inseminationBeanTh3.setEventComments("Test  Event - violates Threshold ");
			inseminationBeanTh3.setOrgID("IMD");
			inseminationBeanTh3.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(82)));
			LifecycleEvent inseminationEventTh3 = new LifecycleEvent(inseminationBeanTh3);
			inseminationEventTh3.setCreatedBy(new User("KASHIF"));
			inseminationEventTh3.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh3.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEventTh3.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));			

			
			Animal heatWarningTh1 = createTestAnimal("-993");
			heatWarningTh1.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean heatWarningBeanTh1 = new LifeCycleEventBean();
			heatWarningBeanTh1.setAnimalTag(heatWarningTh1.getAnimalTag());
			heatWarningBeanTh1.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			heatWarningBeanTh1.setEventComments("Test  Event - violates Threshold ");
			heatWarningBeanTh1.setOrgID("IMD");
			heatWarningBeanTh1.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(82)));
			LifecycleEvent heatWarningEventTh1 = new LifecycleEvent(heatWarningBeanTh1);
			heatWarningEventTh1.setCreatedBy(new User("KASHIF"));
			heatWarningEventTh1.setUpdatedBy(new User("KASHIF"));
			heatWarningEventTh1.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			heatWarningEventTh1.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));					
			
			
			LifeCycleEventBean eventBean = new LifeCycleEventBean();
			eventBean.setAnimalTag(lactatingNonDryPregnant.getAnimalTag());
			eventBean.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBean.setEventComments("Test Insemination Event");
			eventBean.setOrgID("IMD");
			eventBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(240)));
			LifecycleEvent inseminationEvent = new LifecycleEvent(eventBean);
			inseminationEvent.setCreatedBy(new User("KASHIF"));
			inseminationEvent.setUpdatedBy(new User("KASHIF"));
			inseminationEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			inseminationEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			
			int transactionID1 = animalLoader.insertAnimal(lactatingNotPregnant);
			
			int transactionID2 = animalLoader.insertAnimal(lactatingNonDryPregnant);
			inseminationEvent.setEventNote("Test Insemination Event. This cow has NOT yet been dried so DryCow advisement SHOULD be triggered for this cow.");
			inseminationEvent.setAnimalTag(lactatingNonDryPregnant.getAnimalTag());
			eventsLoader.insertLifeCycleEvent(inseminationEvent);
			
			int transactionID3 = animalLoader.insertAnimal(dryPregnant);
			inseminationEvent.setAnimalTag(dryPregnant.getAnimalTag());
			inseminationEvent.setEventNote("Test Insemination Event. This cow HAS been dried so DryCow advisement should NOT be triggered for this cow.");
			eventsLoader.insertLifeCycleEvent(inseminationEvent);
			
			
			int transactionID4 = animalLoader.insertAnimal(nonDehornedCalf);
			int transactionID5 = animalLoader.insertAnimal(nonFmd);

			int transactionID6 = animalLoader.insertAnimal(inseminationAnimalTh3);
			inseminationEventTh3.setEventNote("Test Insemination Event. This cow's pregnancy has not yet been tested. So Pregnancy Test advisement SHOULD be triggered for this cow.");
			inseminationEventTh3.setAnimalTag(inseminationAnimalTh3.getAnimalTag());
			eventsLoader.insertLifeCycleEvent(inseminationEventTh3);
			

			int transactionID7 = animalLoader.insertAnimal(heatWarningTh1);
			eventsLoader.insertLifeCycleEvent(heatWarningEventTh1);
			
			int transactionID8 = animalLoader.insertAnimal(dewormTh1);

			assertEquals(1,transactionID1, "Exactly one record -999 should have been inserted"); // OK
			assertEquals(1,transactionID2, "Exactly one record -998 should have been inserted"); // Violates the DRYCOW rule
			assertEquals(1,transactionID3, "Exactly one record -997 should have been inserted"); // OK
			assertEquals(1,transactionID4, "Exactly one record -996 should have been inserted"); // violates the DEHORN rule
			assertEquals(1,transactionID5, "Exactly one record -995 should have been inserted"); // violates the FMDVACCINE rule
			assertEquals(1,transactionID6, "Exactly one record -994 should have been inserted"); // violates the PREGNANCYTEST rule
			assertEquals(1,transactionID7, "Exactly one record -993 should have been inserted"); // violates the HEATWARNING rule
			assertEquals(1,transactionID8, "Exactly one record -992 should have been inserted"); // violates the DEWORM rule
			
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Advisement> activeRules = advLoader.getAllActiveRules("IMD");
			assertTrue(activeRules!=null && !activeRules.isEmpty(), "At least one rule should have been enabled");
			assertTrue(activeRules.get(0).getOrgId()!=null && !activeRules.get(0).getOrgId().isEmpty(), "Org ID should have been set");
			AdvisementRuleManager advManager = new AdvisementRuleManager();
			List<AnimalAdvisement> advResults = advManager.executeAllRules(activeRules,false,false,false, null);			
			assertTrue(advResults == null || advResults.isEmpty(),"We had set all thresholds to false so nothing should have been returned");
			
			advResults = advManager.executeAllRules(activeRules,true,true,true, null);
			assertTrue(advResults != null && !advResults.isEmpty(),"We had set all thresholds to true so we should have received some values provided there were some enabled rules");

			boolean lactatingNonDryPregnantFound = false;
			boolean nonDehornedCalfFound = false;
			boolean nonFMDFound = false;
			boolean pregTestFound = false;
			boolean heatWarning1Found = false;
			boolean dewormFound = false;
			Iterator<AnimalAdvisement> it = advResults.iterator();
			while (it.hasNext()) {
				AnimalAdvisement populationAnimal = it.next();
				IMDLogger.log(populationAnimal.toString(), Util.WARNING);				
				IMDLogger.log(populationAnimal.toString() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
				if (populationAnimal.getAnimalTag().equalsIgnoreCase(lactatingNotPregnant.getAnimalTag()) && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.DRYCOW)) {
					fail(lactatingNotPregnant.getAnimalTag() +  "("+ lactatingNotPregnant.getAnimalType() + ") is lactating and not pregnant so it should not be in the " + populationAnimal.getAppliedAdvisementRule() + "  Advisement population");
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(lactatingNonDryPregnant.getAnimalTag())  && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.DRYCOW)) {
					lactatingNonDryPregnantFound = true;
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(lactatingNonDryPregnant.getAnimalTag())  && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.DRYCOW)) {
					lactatingNonDryPregnantFound = true;
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(heatWarningTh1.getAnimalTag()) && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.HEATWARNING)) {
					heatWarning1Found = true;
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(nonDehornedCalf.getAnimalTag())  && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.DEHORN)) {
					nonDehornedCalfFound = true;
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(nonFmd.getAnimalTag()) && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.VACCINEFMD)) {
					nonFMDFound = true;
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(inseminationAnimalTh3.getAnimalTag()) && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.PREGNANCYTEST)) {
					pregTestFound = true;
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(dewormTh1.getAnimalTag()) && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.DEWORM)) {
					dewormFound = true;
				}
			}
			assertTrue(lactatingNonDryPregnantFound,lactatingNonDryPregnant.getAnimalTag() +  "("+ lactatingNonDryPregnant.getAnimalType() + ") cow should have been included in the Dry Cow Advisement population");
			assertTrue(nonDehornedCalfFound,nonDehornedCalf.getAnimalTag() +  "("+ nonDehornedCalf.getAnimalType() + ") should have been included in the Dehorning Advisement population");
			assertTrue(nonFMDFound,nonFmd.getAnimalTag() +  "("+ nonFmd.getAnimalType() + ") should have been included in the FMD Vaccination Advisement population");
			assertTrue(pregTestFound,inseminationAnimalTh3.getAnimalTag() +  "("+ inseminationAnimalTh3.getAnimalType() + ") should have been included in the Pregnancy Test Advisement population");
			assertTrue(heatWarning1Found,heatWarningTh1.getAnimalTag() +  "("+ heatWarningTh1.getAnimalType() + ") should have been included in the Heat Warning Test Advisement population");
			assertTrue(dewormFound,dewormTh1.getAnimalTag() +  "("+ dewormTh1.getAnimalType() + ") should have been included in the Deworming Test Advisement population");
			
			///// clean up /////
			assertEquals(0,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998"),"Exactly one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997"),"Exactly one record should have been deleted");
			assertEquals(0,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(0,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994"),"We added Lifecycle events so one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-993"),"We added Lifecycle events so one record should have been deleted");
			assertEquals(0,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-992"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-999"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-998"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-997"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-996"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-995"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-994"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-993"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-992"),"Exactly one record should have been deleted");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}	
	public Animal createTestAnimal(String animalTag) throws Exception {
		Dam c000 = new Dam(/*orgid*/"IMD",/*tag*/animalTag,/*dob*/DateTime.parse("2014-02-09"),/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		c000.setAlias("Laal");
		c000.setBreed(Util.Breed.HFCROSS);
		c000.setAnimalType("LACTATING");
		c000.setHerdJoiningDate(DateTime.now(IMDProperties.getServerTimeZone()).minusMonths(12));
		c000.setFrontSideImageURL("/assets/img/cow-thumbnails/000/1.png");
		c000.setBackSideImageURL("/assets/img/cow-thumbnails/000/2.png");
		c000.setRightSideImageURL("/assets/img/cow-thumbnails/000/3.png");
		c000.setLeftSideImageURL("/assets/img/cow-thumbnails/000/4.png");
		c000.setPurchaseDate(DateTime.parse("2017-02-08"));
		c000.setCreatedBy(new User("KASHIF"));
		c000.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		c000.setUpdatedBy(c000.getCreatedBy());
		c000.setUpdatedDTTM(c000.getCreatedDTTM());
		c000.setAnimalSire(null);
		c000.setAnimalDam(null);
		return c000;
	}	
	private int insertEvent(String animalTag, String comments, String eventCode, DateTime eventDTTM) throws IMDException, SQLException {
		LifeCycleEventBean eventBean = new LifeCycleEventBean();
		eventBean.setAnimalTag(animalTag);
		eventBean.setEventCode(eventCode);
		eventBean.setEventComments(comments);
		eventBean.setOrgID("IMD");
		eventBean.setEventTimeStamp(Util.getDateTimeInSQLFormat(eventDTTM));
		LifecycleEvent lifecycleEvent = new LifecycleEvent(eventBean);
		lifecycleEvent.setCreatedBy(new User("KASHIF"));
		lifecycleEvent.setUpdatedBy(new User("KASHIF"));
		lifecycleEvent.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		lifecycleEvent.setUpdatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
		LifeCycleEventsLoader loader = new LifeCycleEventsLoader();
		
		return (loader.insertLifeCycleEvent(lifecycleEvent));
		
	}	
	
}
