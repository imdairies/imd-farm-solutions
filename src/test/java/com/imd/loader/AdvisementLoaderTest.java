package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.advisement.AdvisementRuleManager;
import com.imd.advisement.DehorningAdvisement;
import com.imd.advisement.DelayedHeatCowAdvisement;
import com.imd.advisement.DelayedHeatHeiferAdvisement;
import com.imd.advisement.DryCowAdvisement;
import com.imd.advisement.FMDVaccinationAdvisement;
import com.imd.advisement.HeatWarningAdvisement;
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
import com.imd.util.IMDLogger;
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
			animalLoader.deleteAnimal("IMD", "-999"); // Older than 120 days
			animalLoader.deleteAnimal("IMD", "-998"); // Younger than 120 days already weaned off - no violation
			animalLoader.deleteAnimal("IMD", "-997"); // Younger than 120 days Th1 violated
			animalLoader.deleteAnimal("IMD", "-996"); // Younger than 120 days Th2 violated
			animalLoader.deleteAnimal("IMD", "-995"); // Younger than 120 days Th3 violated
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			///////////////////

			Animal oldAnimal = createTestAnimal("-999");
			oldAnimal.setAnimalStatus("ACTIVE");
			oldAnimal.setDateOfBirth(DateTime.now().minusDays(210));
			oldAnimal.setAnimalType(Util.AnimalTypes.HEIFER);
			int transOldAnimal = animalLoader.insertAnimal(oldAnimal);
			assertEquals(1,transOldAnimal, "Exactly one record -999 should have been inserted");
			
			Animal noViolation = createTestAnimal("-998");
			noViolation.setAnimalStatus("ACTIVE");
			noViolation.setAnimalType(Util.AnimalTypes.FEMALECALF);
			noViolation.setDateOfBirth(DateTime.now().minusDays(92));
			LifeCycleEventBean noViolationBean = new LifeCycleEventBean();
			noViolationBean.setAnimalTag(noViolation.getAnimalTag());
			noViolationBean.setEventCode(Util.LifeCycleEvents.WEANEDOFF);
			noViolationBean.setEventComments("Test  Event - does not violate any threshold");
			noViolationBean.setOrgID("IMD");
			noViolationBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(10)));
			LifecycleEvent noViolationEvent = new LifecycleEvent(noViolationBean);
			noViolationEvent.setCreatedBy(new User("KASHIF"));
			noViolationEvent.setUpdatedBy(new User("KASHIF"));
			noViolationEvent.setCreatedDTTM(DateTime.now());
			noViolationEvent.setUpdatedDTTM(DateTime.now());
			int transNoViolation = animalLoader.insertAnimal(noViolation);
			assertEquals(1,transNoViolation, "Exactly one record -998 should have been inserted");
			assertTrue(eventsLoader.insertLifeCycleEvent(noViolationEvent)>0);	
						
			Animal th1Violation = createTestAnimal("-997");
			th1Violation.setAnimalStatus("ACTIVE");
			th1Violation.setAnimalType(Util.AnimalTypes.FEMALECALF);
			th1Violation.setDateOfBirth(DateTime.now().minusDays(86));
			int transTh1Violation = animalLoader.insertAnimal(th1Violation);
			assertEquals(1,transTh1Violation, "Exactly one record -997 should have been inserted");				
						
			Animal th2Violation = createTestAnimal("-996");
			th2Violation.setAnimalStatus("ACTIVE");
			th2Violation.setAnimalType(Util.AnimalTypes.MALECALF);
			th2Violation.setDateOfBirth(DateTime.now().minusDays(91));
			int transTh2Violation = animalLoader.insertAnimal(th2Violation);
			assertEquals(1,transTh2Violation, "Exactly one record -996 should have been inserted");				

			
			Animal th3Violation = createTestAnimal("-995");
			th3Violation.setAnimalStatus("ACTIVE");
			th3Violation.setAnimalType(Util.AnimalTypes.FEMALECALF);
			th3Violation.setDateOfBirth(DateTime.now().minusDays(96));
			int transTh3Violation = animalLoader.insertAnimal(th3Violation);
			assertEquals(1,transTh3Violation, "Exactly one record -995 should have been inserted");				

			List<Animal> animalPop = weanOffHeatAdvisement.getAdvisementRuleAddressablePopulation("IMD");
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
	void testWeightMeasurementAdvisementRule() {

		try {
			WeightMeasurementAdvisement weightMeasurementAdvisement = new WeightMeasurementAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999"); // Older than 180 days
			animalLoader.deleteAnimal("IMD", "-998"); // Younger than 180 days no violation
			animalLoader.deleteAnimal("IMD", "-997"); // Younger than 180 days Th1 violated
			animalLoader.deleteAnimal("IMD", "-996"); // Younger than 180 days Th2 violated
			animalLoader.deleteAnimal("IMD", "-995"); // Younger than 180 days Th3 violated
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			///////////////////

			Animal oldAnimal = createTestAnimal("-999");
			oldAnimal.setAnimalStatus("ACTIVE");
			oldAnimal.setDateOfBirth(DateTime.now().minusDays(210));
			oldAnimal.setAnimalType(Util.AnimalTypes.HEIFER);
			int transOldAnimal = animalLoader.insertAnimal(oldAnimal);
			assertEquals(1,transOldAnimal, "Exactly one record -999 should have been inserted");
			
			Animal noViolation = createTestAnimal("-998");
			noViolation.setAnimalStatus("ACTIVE");
			noViolation.setAnimalType(Util.AnimalTypes.FEMALECALF);
			noViolation.setDateOfBirth(DateTime.now().minusDays(170));
			LifeCycleEventBean noViolationBean = new LifeCycleEventBean();
			noViolationBean.setAnimalTag(noViolation.getAnimalTag());
			noViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			noViolationBean.setEventComments("Test  Event - does not violate any threshold");
			noViolationBean.setOrgID("IMD");
			noViolationBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(10)));
			LifecycleEvent noViolationEvent = new LifecycleEvent(noViolationBean);
			noViolationEvent.setCreatedBy(new User("KASHIF"));
			noViolationEvent.setUpdatedBy(new User("KASHIF"));
			noViolationEvent.setCreatedDTTM(DateTime.now());
			noViolationEvent.setUpdatedDTTM(DateTime.now());
			int transNoViolation = animalLoader.insertAnimal(noViolation);
			assertEquals(1,transNoViolation, "Exactly one record -998 should have been inserted");
			assertTrue(eventsLoader.insertLifeCycleEvent(noViolationEvent)>0);	
						
			Animal th1Violation = createTestAnimal("-997");
			th1Violation.setAnimalStatus("ACTIVE");
			th1Violation.setAnimalType(Util.AnimalTypes.FEMALECALF);
			th1Violation.setDateOfBirth(DateTime.now().minusDays(170));
			LifeCycleEventBean th1ViolationBean = new LifeCycleEventBean();
			th1ViolationBean.setAnimalTag(th1Violation.getAnimalTag());
			th1ViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			th1ViolationBean.setEventComments("Test  Event - Violates Threshold 1");
			th1ViolationBean.setOrgID("IMD");
			th1ViolationBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(16)));
			LifecycleEvent th1ViolationEvent = new LifecycleEvent(th1ViolationBean);
			th1ViolationEvent.setCreatedBy(new User("KASHIF"));
			th1ViolationEvent.setUpdatedBy(new User("KASHIF"));
			th1ViolationEvent.setCreatedDTTM(DateTime.now());
			th1ViolationEvent.setUpdatedDTTM(DateTime.now());
			int transTh1Violation = animalLoader.insertAnimal(th1Violation);
			assertEquals(1,transTh1Violation, "Exactly one record -997 should have been inserted");				
			assertTrue(eventsLoader.insertLifeCycleEvent(th1ViolationEvent)>0);	
						
			Animal th2Violation = createTestAnimal("-996");
			th2Violation.setAnimalStatus("ACTIVE");
			th2Violation.setAnimalType(Util.AnimalTypes.MALECALF);
			th2Violation.setDateOfBirth(DateTime.now().minusDays(170));
			LifeCycleEventBean th2ViolationBean = new LifeCycleEventBean();
			th2ViolationBean.setAnimalTag(th2Violation.getAnimalTag());
			th2ViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			th2ViolationBean.setEventComments("Test  Event -  Violates Threshold 2");
			th2ViolationBean.setOrgID("IMD");
			th2ViolationBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(22)));
			LifecycleEvent th2ViolationEvent = new LifecycleEvent(th2ViolationBean);
			th2ViolationEvent.setCreatedBy(new User("KASHIF"));
			th2ViolationEvent.setUpdatedBy(new User("KASHIF"));
			th2ViolationEvent.setCreatedDTTM(DateTime.now());
			th2ViolationEvent.setUpdatedDTTM(DateTime.now());
			int transTh2Violation = animalLoader.insertAnimal(th2Violation);
			assertEquals(1,transTh2Violation, "Exactly one record -996 should have been inserted");				
			assertTrue(eventsLoader.insertLifeCycleEvent(th2ViolationEvent)>0);	

			
			Animal th3Violation = createTestAnimal("-995");
			th3Violation.setAnimalStatus("ACTIVE");
			th3Violation.setAnimalType(Util.AnimalTypes.FEMALECALF);
			th3Violation.setDateOfBirth(DateTime.now().minusDays(170));
			LifeCycleEventBean th3ViolationBean = new LifeCycleEventBean();
			th3ViolationBean.setAnimalTag(th3Violation.getAnimalTag());
			th3ViolationBean.setEventCode(Util.LifeCycleEvents.WEIGHT);
			th3ViolationBean.setEventComments("Test  Event -  Violates Threshold 3");
			th3ViolationBean.setOrgID("IMD");
			th3ViolationBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(29)));
			LifecycleEvent th3ViolationEvent = new LifecycleEvent(th3ViolationBean);
			th3ViolationEvent.setCreatedBy(new User("KASHIF"));
			th3ViolationEvent.setUpdatedBy(new User("KASHIF"));
			th3ViolationEvent.setCreatedDTTM(DateTime.now());
			th3ViolationEvent.setUpdatedDTTM(DateTime.now());
			int transTh3Violation = animalLoader.insertAnimal(th3Violation);
			assertEquals(1,transTh3Violation, "Exactly one record -995 should have been inserted");				
			assertTrue(eventsLoader.insertLifeCycleEvent(th3ViolationEvent)>0);	

			List<Animal> animalPop = weightMeasurementAdvisement.getAdvisementRuleAddressablePopulation("IMD");
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
			th1_1Violated.setAnimalStatus("ACTIVE");
			th1_1Violated.setAnimalType("DRYAWTHEAT");

			int transTh1_1Violated = animalLoader.insertAnimal(th1_1Violated);
			assertEquals(1,transTh1_1Violated, "Exactly one record -994 should have been inserted");			
			
			LifeCycleEventBean th1_1ViolatedBean1 = new LifeCycleEventBean();
			th1_1ViolatedBean1.setAnimalTag(th1_1Violated.getAnimalTag());
			th1_1ViolatedBean1.setEventCode(Util.LifeCycleEvents.ABORTION);
			th1_1ViolatedBean1.setEventComments("Test  Event - violates 1st threshold");
			th1_1ViolatedBean1.setOrgID("IMD");
			th1_1ViolatedBean1.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(61)));
			LifecycleEvent th1_1ViolatedBeanEvent1 = new LifecycleEvent(th1_1ViolatedBean1);
			th1_1ViolatedBeanEvent1.setCreatedBy(new User("KASHIF"));
			th1_1ViolatedBeanEvent1.setUpdatedBy(new User("KASHIF"));
			th1_1ViolatedBeanEvent1.setCreatedDTTM(DateTime.now());
			th1_1ViolatedBeanEvent1.setUpdatedDTTM(DateTime.now());
			eventsLoader.insertLifeCycleEvent(th1_1ViolatedBeanEvent1);

			LifeCycleEventBean th1_1ViolatedBean2 = new LifeCycleEventBean();
			th1_1ViolatedBean2.setAnimalTag(th1_1Violated.getAnimalTag());
			th1_1ViolatedBean2.setEventCode(Util.LifeCycleEvents.HEAT);
			th1_1ViolatedBean2.setEventComments("Test  Event - violates 1st threshold");
			th1_1ViolatedBean2.setOrgID("IMD");
			th1_1ViolatedBean2.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(240)));
			LifecycleEvent th1_1ViolatedBeanEvent2 = new LifecycleEvent(th1_1ViolatedBean2);
			th1_1ViolatedBeanEvent2.setCreatedBy(new User("KASHIF"));
			th1_1ViolatedBeanEvent2.setUpdatedBy(new User("KASHIF"));
			th1_1ViolatedBeanEvent2.setCreatedDTTM(DateTime.now());
			th1_1ViolatedBeanEvent2.setUpdatedDTTM(DateTime.now());
			eventsLoader.insertLifeCycleEvent(th1_1ViolatedBeanEvent2);
			
			
			
			
			Animal recentlyParturated = createTestAnimal("-999");
			recentlyParturated.setAnimalStatus("ACTIVE");
			recentlyParturated.setAnimalType("LACTATING");
			LifeCycleEventBean recentlyParturatedBean = new LifeCycleEventBean();
			recentlyParturatedBean.setAnimalTag(recentlyParturated.getAnimalTag());
			recentlyParturatedBean.setEventCode(Util.LifeCycleEvents.PARTURATE);
			recentlyParturatedBean.setEventComments("Test  Event - does not violate any threshold");
			recentlyParturatedBean.setOrgID("IMD");
			recentlyParturatedBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(20)));
			LifecycleEvent recentlyParturatedEvent = new LifecycleEvent(recentlyParturatedBean);
			recentlyParturatedEvent.setCreatedBy(new User("KASHIF"));
			recentlyParturatedEvent.setUpdatedBy(new User("KASHIF"));
			recentlyParturatedEvent.setCreatedDTTM(DateTime.now());
			recentlyParturatedEvent.setUpdatedDTTM(DateTime.now());
			int transRecentlyParturated = animalLoader.insertAnimal(recentlyParturated);
			eventsLoader.insertLifeCycleEvent(recentlyParturatedEvent);
			assertEquals(1,transRecentlyParturated, "Exactly one record -999 should have been inserted");
			
			
			Animal th0Violated = createTestAnimal("-995");
			th0Violated.setAnimalStatus("ACTIVE");
			th0Violated.setAnimalType("LACTATING");
			LifeCycleEventBean th00ViolatedBean = new LifeCycleEventBean();
			th00ViolatedBean.setAnimalTag(th0Violated.getAnimalTag());
			th00ViolatedBean.setEventCode(Util.LifeCycleEvents.PARTURATE);
			th00ViolatedBean.setEventComments("Test  Event - does not violate any threshold");
			th00ViolatedBean.setOrgID("IMD");
			th00ViolatedBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(90)));
			LifecycleEvent th00ViolatedEvent = new LifecycleEvent(th00ViolatedBean);
			th00ViolatedEvent.setCreatedBy(new User("KASHIF"));
			th00ViolatedEvent.setUpdatedBy(new User("KASHIF"));
			th00ViolatedEvent.setCreatedDTTM(DateTime.now());
			th00ViolatedEvent.setUpdatedDTTM(DateTime.now());
			int transTh0Violated = animalLoader.insertAnimal(th0Violated);
			assertEquals(1,transTh0Violated, "Exactly one record -995 should have been inserted");				

			eventsLoader.insertLifeCycleEvent(th00ViolatedEvent);
			LifeCycleEventBean th0ViolatedBean = new LifeCycleEventBean();
			th0ViolatedBean.setAnimalTag(th0Violated.getAnimalTag());
			th0ViolatedBean.setEventCode(Util.LifeCycleEvents.HEAT);
			th0ViolatedBean.setEventComments("Test  Event - does not violate any threshold");
			th0ViolatedBean.setOrgID("IMD");
			th0ViolatedBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(10)));
			LifecycleEvent th0ViolatedEvent = new LifecycleEvent(th0ViolatedBean);
			th0ViolatedEvent.setCreatedBy(new User("KASHIF"));
			th0ViolatedEvent.setUpdatedBy(new User("KASHIF"));
			th0ViolatedEvent.setCreatedDTTM(DateTime.now());
			th0ViolatedEvent.setUpdatedDTTM(DateTime.now());
			eventsLoader.insertLifeCycleEvent(th0ViolatedEvent);
			
			Animal th3Violated = createTestAnimal("-996");
			th3Violated.setAnimalStatus("ACTIVE");
			th3Violated.setAnimalType("LACTATING");
			LifeCycleEventBean th3ViolatedBean = new LifeCycleEventBean();
			th3ViolatedBean.setAnimalTag(th3Violated.getAnimalTag());
			th3ViolatedBean.setEventCode(Util.LifeCycleEvents.PARTURATE);
			th3ViolatedBean.setEventComments("Test  Event - violates 3rd threshold");
			th3ViolatedBean.setOrgID("IMD");
			th3ViolatedBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(121)));
			LifecycleEvent th3ViolatedEvent = new LifecycleEvent(th3ViolatedBean);
			th3ViolatedEvent.setCreatedBy(new User("KASHIF"));
			th3ViolatedEvent.setUpdatedBy(new User("KASHIF"));
			th3ViolatedEvent.setCreatedDTTM(DateTime.now());
			th3ViolatedEvent.setUpdatedDTTM(DateTime.now());
			int transTh3Violated = animalLoader.insertAnimal(th3Violated);
			eventsLoader.insertLifeCycleEvent(th3ViolatedEvent);
			assertEquals(1,transTh3Violated, "Exactly one record -996 should have been inserted");			
			
			Animal th2Violated = createTestAnimal("-997");
			th2Violated.setAnimalStatus("ACTIVE");
			th2Violated.setAnimalType("LACTATING");
			LifeCycleEventBean th2ViolatedBean = new LifeCycleEventBean();
			th2ViolatedBean.setAnimalTag(th2Violated.getAnimalTag());
			th2ViolatedBean.setEventCode(Util.LifeCycleEvents.PARTURATE);
			th2ViolatedBean.setEventComments("Test  Event - violates 2nd threshold");
			th2ViolatedBean.setOrgID("IMD");
			th2ViolatedBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(91)));
			LifecycleEvent th2ViolatedEvent = new LifecycleEvent(th2ViolatedBean);
			th2ViolatedEvent.setCreatedBy(new User("KASHIF"));
			th2ViolatedEvent.setUpdatedBy(new User("KASHIF"));
			th2ViolatedEvent.setCreatedDTTM(DateTime.now());
			th2ViolatedEvent.setUpdatedDTTM(DateTime.now());
			int transTh2Violated = animalLoader.insertAnimal(th2Violated);
			eventsLoader.insertLifeCycleEvent(th2ViolatedEvent);
			assertEquals(1,transTh2Violated, "Exactly one record -997 should have been inserted");

			Animal th1Violated = createTestAnimal("-998");
			th1Violated.setAnimalStatus("ACTIVE");
			th1Violated.setAnimalType("LACTATING");
			LifeCycleEventBean th1ViolatedBean = new LifeCycleEventBean();
			th1ViolatedBean.setAnimalTag(th1Violated.getAnimalTag());
			th1ViolatedBean.setEventCode(Util.LifeCycleEvents.PARTURATE);
			th1ViolatedBean.setEventComments("Test  Event - violates 1st threshold");
			th1ViolatedBean.setOrgID("IMD");
			th1ViolatedBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(61)));
			LifecycleEvent th1ViolatedEvent = new LifecycleEvent(th1ViolatedBean);
			th1ViolatedEvent.setCreatedBy(new User("KASHIF"));
			th1ViolatedEvent.setUpdatedBy(new User("KASHIF"));
			th1ViolatedEvent.setCreatedDTTM(DateTime.now());
			th1ViolatedEvent.setUpdatedDTTM(DateTime.now());
			int transTh1Violated = animalLoader.insertAnimal(th1Violated);
			eventsLoader.insertLifeCycleEvent(th1ViolatedEvent);
			assertEquals(1,transTh1Violated, "Exactly one record -998 should have been inserted");
			
			
			
			

			List<Animal> animalPop = delayedHeatCowAdvisement.getAdvisementRuleAddressablePopulation("IMD");
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
			youngHeifer.setDateOfBirth(DateTime.now().minusMonths(11));
			youngHeifer.setAnimalStatus("ACTIVE");
			youngHeifer.setAnimalType("HEIFER");

			Animal th1Heifer =createTestAnimal("-998");
			th1Heifer.setDateOfBirth(DateTime.now().minusDays(366));
			th1Heifer.setAnimalStatus("ACTIVE");
			th1Heifer.setAnimalType("HEIFER");
			
			Animal th2Heifer =createTestAnimal("-997");
			th2Heifer.setDateOfBirth(DateTime.now().minusDays(550));
			th2Heifer.setAnimalStatus("ACTIVE");
			th2Heifer.setAnimalType("HEIFER");

			Animal th3Heifer =createTestAnimal("-996");
			th3Heifer.setDateOfBirth(DateTime.now().minusDays(730));
			th3Heifer.setAnimalStatus("ACTIVE");
			th3Heifer.setAnimalType("HEIFER");

			Animal th3WithHeatHeifer =createTestAnimal("-995");
			th3WithHeatHeifer.setDateOfBirth(DateTime.now().minusDays(730));
			th3WithHeatHeifer.setAnimalStatus("ACTIVE");
			th3WithHeatHeifer.setAnimalType("HFRAWTHEAT");
			
			LifeCycleEventBean eventBean = new LifeCycleEventBean();
			eventBean.setAnimalTag(th3WithHeatHeifer.getAnimalTag());
			eventBean.setEventCode(Util.LifeCycleEvents.HEAT);
			eventBean.setEventComments("Test  Event - violates Threshold 3");
			eventBean.setOrgID("IMD");
			eventBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(100)));
			LifecycleEvent heiferHeatEvent = new LifecycleEvent(eventBean);
			heiferHeatEvent.setCreatedBy(new User("KASHIF"));
			heiferHeatEvent.setUpdatedBy(new User("KASHIF"));
			heiferHeatEvent.setCreatedDTTM(DateTime.now());
			heiferHeatEvent.setUpdatedDTTM(DateTime.now());
			
			Animal th0WithHeatHeifer =createTestAnimal("-994");
			th0WithHeatHeifer.setDateOfBirth(DateTime.now().minusDays(730));
			th0WithHeatHeifer.setAnimalStatus("ACTIVE");
			th0WithHeatHeifer.setAnimalType("HFRAWTHEAT");
			
			LifeCycleEventBean eventTh0Bean = new LifeCycleEventBean();
			eventTh0Bean.setAnimalTag(th0WithHeatHeifer.getAnimalTag());
			eventTh0Bean.setEventCode(Util.LifeCycleEvents.HEAT);
			eventTh0Bean.setEventComments("Test  Event - does not violate Threshold 3");
			eventTh0Bean.setOrgID("IMD");
			eventTh0Bean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(20)));
			LifecycleEvent heiferTh0HeatEvent = new LifecycleEvent(eventTh0Bean);
			heiferTh0HeatEvent.setCreatedBy(new User("KASHIF"));
			heiferTh0HeatEvent.setUpdatedBy(new User("KASHIF"));
			heiferTh0HeatEvent.setCreatedDTTM(DateTime.now());
			heiferTh0HeatEvent.setUpdatedDTTM(DateTime.now());

			
			
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

			List<Animal> animalPop = delayedHeatHeiferAdvisement.getAdvisementRuleAddressablePopulation("IMD");
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
			heatWarningTh1.setAnimalStatus("ACTIVE");
			heatWarningTh1.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean heatBeanTh1 = new LifeCycleEventBean();
			heatBeanTh1.setAnimalTag(heatWarningTh1.getAnimalTag());
			heatBeanTh1.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			heatBeanTh1.setEventComments("Test  Event - violates Threshold 1");
			heatBeanTh1.setOrgID("IMD");
			heatBeanTh1.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(19)));
			LifecycleEvent heatInseminationEventTh1 = new LifecycleEvent(heatBeanTh1);
			heatInseminationEventTh1.setCreatedBy(new User("KASHIF"));
			heatInseminationEventTh1.setUpdatedBy(new User("KASHIF"));
			heatInseminationEventTh1.setCreatedDTTM(DateTime.now());
			heatInseminationEventTh1.setUpdatedDTTM(DateTime.now());
			
			Animal heatWarningTh2 = createTestAnimal("-998");
			heatWarningTh2.setAnimalStatus("ACTIVE");
			heatWarningTh2.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean heatBeanTh2 = new LifeCycleEventBean();
			heatBeanTh2.setAnimalTag(heatWarningTh2.getAnimalTag());
			heatBeanTh2.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			heatBeanTh2.setEventComments("Test  Event - violates Threshold 2");
			heatBeanTh2.setOrgID("IMD");
			heatBeanTh2.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(43)));
			LifecycleEvent heatInseminationEventTh2 = new LifecycleEvent(heatBeanTh2);
			heatInseminationEventTh2.setCreatedBy(new User("KASHIF"));
			heatInseminationEventTh2.setUpdatedBy(new User("KASHIF"));
			heatInseminationEventTh2.setCreatedDTTM(DateTime.now());
			heatInseminationEventTh2.setUpdatedDTTM(DateTime.now());	
			
			Animal heatWarningTh3 = createTestAnimal("-997");
			heatWarningTh3.setAnimalStatus("ACTIVE");
			heatWarningTh3.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean heatBeanTh3 = new LifeCycleEventBean();
			heatBeanTh3.setAnimalTag(heatWarningTh3.getAnimalTag());
			heatBeanTh3.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			heatBeanTh3.setEventComments("Test  Event - violates Threshold 3");
			heatBeanTh3.setOrgID("IMD");
			heatBeanTh3.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(63)));
			LifecycleEvent heatInseminationEventTh3 = new LifecycleEvent(heatBeanTh3);
			heatInseminationEventTh3.setCreatedBy(new User("KASHIF"));
			heatInseminationEventTh3.setUpdatedBy(new User("KASHIF"));
			heatInseminationEventTh3.setCreatedDTTM(DateTime.now());
			heatInseminationEventTh3.setUpdatedDTTM(DateTime.now());				

			Animal heatWarningTh0 = createTestAnimal("-996");
			heatWarningTh0.setAnimalStatus("ACTIVE");
			heatWarningTh0.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean heatBeanTh0 = new LifeCycleEventBean();
			heatBeanTh0.setAnimalTag(heatWarningTh0.getAnimalTag());
			heatBeanTh0.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			heatBeanTh0.setEventComments("Test  Event - does not violate any threshold");
			heatBeanTh0.setOrgID("IMD");
			heatBeanTh0.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(39)));
			LifecycleEvent heatInseminationEventTh0 = new LifecycleEvent(heatBeanTh0);
			heatInseminationEventTh0.setCreatedBy(new User("KASHIF"));
			heatInseminationEventTh0.setUpdatedBy(new User("KASHIF"));
			heatInseminationEventTh0.setCreatedDTTM(DateTime.now());
			heatInseminationEventTh0.setUpdatedDTTM(DateTime.now());
			
			
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

			List<Animal> animalPop = heatWarnAdv.getAdvisementRuleAddressablePopulation("IMD");
			boolean th1Found = false;
			boolean th2Found = false;
			boolean th3Found = false;
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					IMDLogger.log(populationAnimal.getNote(0).getNoteText(), Util.WARNING);
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(heatWarningTh1.getAnimalTag())) {
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
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-999"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-998"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-997"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-996"),"Exactly one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998"),"Exactly one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997"),"Exactly one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996"),"We did not add any Lifecycle event so no record should have been deleted");
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
			femaleCalf.setDateOfBirth(DateTime.now().minusDays(90));
			femaleCalf.setAnimalStatus("ACTIVE");
			femaleCalf.setAnimalType("FEMALECALF");

			Animal maleCalf = createTestAnimal("-998");
			maleCalf.setDateOfBirth(DateTime.now().minusDays(90));
			maleCalf.setAnimalStatus("ACTIVE");
			maleCalf.setAnimalType("MALECALF");
			
			Animal dryPregnant = createTestAnimal("-997");
			dryPregnant.setDateOfBirth(DateTime.now().minusDays(600));
			dryPregnant.setAnimalStatus("ACTIVE");
			dryPregnant.setAnimalType("DRYPREG");

			Animal dehornedCalf = createTestAnimal("-996");
			dehornedCalf.setDateOfBirth(DateTime.now().minusDays(90));
			dehornedCalf.setAnimalStatus("ACTIVE");
			dehornedCalf.setAnimalType("FEMALECALF");
			
			
			LifeCycleEventBean eventBean = new LifeCycleEventBean();
			eventBean.setAnimalTag(maleCalf.getAnimalTag());
			eventBean.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBean.setEventComments("Test Insemination Event");
			eventBean.setOrgID("IMD");
			eventBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(240)));
			LifecycleEvent lcEvent = new LifecycleEvent(eventBean);
			lcEvent.setCreatedBy(new User("KASHIF"));
			lcEvent.setUpdatedBy(new User("KASHIF"));
			lcEvent.setCreatedDTTM(DateTime.now());
			lcEvent.setUpdatedDTTM(DateTime.now());
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

			List<Animal> animalPop = dehornAdv.getAdvisementRuleAddressablePopulation("IMD");
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
			animalPop = dehornAdv.getAdvisementRuleAddressablePopulation("IMD");
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
			PregnancyTestAdvisement fmd = new PregnancyTestAdvisement();
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
			notInseminated.setAnimalStatus("ACTIVE");
			notInseminated.setAnimalType("LACTATING");

			Animal inseminationTh0 = createTestAnimal("-998");
			inseminationTh0.setAnimalStatus("ACTIVE");
			inseminationTh0.setAnimalType("LCTINSEMIN");
			
			Animal inseminationTh3 = createTestAnimal("-997");
			inseminationTh3.setAnimalStatus("ACTIVE");
			inseminationTh3.setAnimalType("LCTINSEMIN");

			Animal inseminationTh2 = createTestAnimal("-996");
			inseminationTh2.setAnimalStatus("ACTIVE");
			inseminationTh2.setAnimalType("LCTINSEMIN");

			Animal inseminationTh1 = createTestAnimal("-995");
			inseminationTh1.setAnimalStatus("ACTIVE");
			inseminationTh1.setAnimalType("LCTINSEMIN");			
			
			LifeCycleEventBean eventBeanTh3 = new LifeCycleEventBean();
			eventBeanTh3.setAnimalTag(inseminationTh3.getAnimalTag());
			eventBeanTh3.setEventCode(Util.LifeCycleEvents.MATING);
			eventBeanTh3.setEventComments("Test  Event - violates Threshold 3");
			eventBeanTh3.setOrgID("IMD");
			eventBeanTh3.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(210)));
			LifecycleEvent inseminationEventTh3 = new LifecycleEvent(eventBeanTh3);
			inseminationEventTh3.setCreatedBy(new User("KASHIF"));
			inseminationEventTh3.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh3.setCreatedDTTM(DateTime.now());
			inseminationEventTh3.setUpdatedDTTM(DateTime.now());
			
			LifeCycleEventBean eventBeanTh2 = new LifeCycleEventBean();
			eventBeanTh2.setAnimalTag(inseminationTh2.getAnimalTag());
			eventBeanTh2.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBeanTh2.setEventComments("Test Event - violates Threshold 2");
			eventBeanTh2.setOrgID("IMD");
			eventBeanTh2.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(75)));
			LifecycleEvent inseminationEventTh2 = new LifecycleEvent(eventBeanTh2);
			inseminationEventTh2.setCreatedBy(new User("KASHIF"));
			inseminationEventTh2.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh2.setCreatedDTTM(DateTime.now());
			inseminationEventTh2.setUpdatedDTTM(DateTime.now());
		
			LifeCycleEventBean eventBeanTh1 = new LifeCycleEventBean();
			eventBeanTh1.setAnimalTag(inseminationTh1.getAnimalTag());
			eventBeanTh1.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBeanTh1.setEventComments("Test Event - violates Threshold 1");
			eventBeanTh1.setOrgID("IMD");
			eventBeanTh1.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(60)));
			LifecycleEvent inseminationEventTh1 = new LifecycleEvent(eventBeanTh1);
			inseminationEventTh1.setCreatedBy(new User("KASHIF"));
			inseminationEventTh1.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh1.setCreatedDTTM(DateTime.now());
			inseminationEventTh1.setUpdatedDTTM(DateTime.now());
			

			LifeCycleEventBean eventBeanTh0 = new LifeCycleEventBean();
			eventBeanTh0.setAnimalTag(inseminationTh0.getAnimalTag());
			eventBeanTh0.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBeanTh0.setEventComments("Test FMD Vaccination Event - no violation");
			eventBeanTh0.setOrgID("IMD");
			eventBeanTh0.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(30)));
			LifecycleEvent inseminationEventTh0 = new LifecycleEvent(eventBeanTh0);
			inseminationEventTh0.setCreatedBy(new User("KASHIF"));
			inseminationEventTh0.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh0.setCreatedDTTM(DateTime.now());
			inseminationEventTh0.setUpdatedDTTM(DateTime.now());			
			

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

			List<Animal> animalPop = fmd.getAdvisementRuleAddressablePopulation("IMD");
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
			noVaccine.setAnimalStatus("ACTIVE");
			noVaccine.setAnimalType("LACTATING");

			Animal vaccineOk = createTestAnimal("-998");
			vaccineOk.setAnimalStatus("ACTIVE");
			vaccineOk.setAnimalType("LCTPRGNT");
			
			Animal vaccineTh3 = createTestAnimal("-997");
			vaccineTh3.setAnimalStatus("ACTIVE");
			vaccineTh3.setAnimalType("DRYPREG");

			Animal vaccineTh2 = createTestAnimal("-996");
			vaccineTh2.setAnimalStatus("ACTIVE");
			vaccineTh2.setAnimalType("DRYPREG");

			Animal vaccineTh1 = createTestAnimal("-995");
			vaccineTh1.setAnimalStatus("ACTIVE");
			vaccineTh1.setAnimalType("DRYPREG");			

			Animal vaccineButNotFMD = createTestAnimal("-994");
			vaccineButNotFMD.setAnimalStatus("ACTIVE");
			vaccineButNotFMD.setAnimalType("DRYPREG");

			LifeCycleEventBean eventBeanNotFMD = new LifeCycleEventBean();
			eventBeanNotFMD.setAnimalTag(vaccineOk.getAnimalTag());
			eventBeanNotFMD.setEventCode(Util.LifeCycleEvents.VACCINE);
			eventBeanNotFMD.setEventComments("Test FMD Vaccination Event - no violation");
			eventBeanNotFMD.setOrgID("IMD");
			eventBeanNotFMD.setAuxField1Value("BQ");
			
			eventBeanNotFMD.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(149)));
			LifecycleEvent inseminationEventNoFMD = new LifecycleEvent(eventBeanNotFMD);
			inseminationEventNoFMD.setCreatedBy(new User("KASHIF"));
			inseminationEventNoFMD.setUpdatedBy(new User("KASHIF"));
			inseminationEventNoFMD.setCreatedDTTM(DateTime.now());
			inseminationEventNoFMD.setUpdatedDTTM(DateTime.now());
			inseminationEventNoFMD.setEventNote("Test Vaccination Event. This animal should not violate any threshold.");
			inseminationEventNoFMD.setAnimalTag(vaccineButNotFMD.getAnimalTag());

			int transNotFMD = animalLoader.insertAnimal(vaccineButNotFMD);
			eventsLoader.insertLifeCycleEvent(inseminationEventNoFMD);
			
			
			
			
			LifeCycleEventBean eventBeanTh3 = new LifeCycleEventBean();
			eventBeanTh3.setAnimalTag(vaccineTh3.getAnimalTag());
			eventBeanTh3.setEventCode(Util.LifeCycleEvents.VACCINE);
			eventBeanTh3.setEventComments("Test FMD Vaccination Event - violates Threshold 3");
			eventBeanTh3.setOrgID("IMD");
			eventBeanTh3.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(210)));
			LifecycleEvent inseminationEventTh3 = new LifecycleEvent(eventBeanTh3);
			inseminationEventTh3.setCreatedBy(new User("KASHIF"));
			inseminationEventTh3.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh3.setCreatedDTTM(DateTime.now());
			inseminationEventTh3.setUpdatedDTTM(DateTime.now());
			
			LifeCycleEventBean eventBeanTh2 = new LifeCycleEventBean();
			eventBeanTh2.setAnimalTag(vaccineTh2.getAnimalTag());
			eventBeanTh2.setEventCode(Util.LifeCycleEvents.VACCINE);
			eventBeanTh2.setEventComments("Test FMD Vaccination Event - violates Threshold 2");
			eventBeanTh2.setOrgID("IMD");
			eventBeanTh2.setAuxField1Value("FOOT&MOUTH");
			eventBeanTh2.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(175)));
			LifecycleEvent inseminationEventTh2 = new LifecycleEvent(eventBeanTh2);
			inseminationEventTh2.setCreatedBy(new User("KASHIF"));
			inseminationEventTh2.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh2.setCreatedDTTM(DateTime.now());
			inseminationEventTh2.setUpdatedDTTM(DateTime.now());
		
			LifeCycleEventBean eventBeanTh1 = new LifeCycleEventBean();
			eventBeanTh1.setAnimalTag(vaccineTh1.getAnimalTag());
			eventBeanTh1.setEventCode(Util.LifeCycleEvents.VACCINE);
			eventBeanTh1.setEventComments("Test FMD Vaccination Event - violates Threshold 1");
			eventBeanTh1.setOrgID("IMD");
			eventBeanTh1.setAuxField1Value("FOOT&MOUTH");

			eventBeanTh1.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(151)));
			LifecycleEvent inseminationEventTh1 = new LifecycleEvent(eventBeanTh1);
			inseminationEventTh1.setCreatedBy(new User("KASHIF"));
			inseminationEventTh1.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh1.setCreatedDTTM(DateTime.now());
			inseminationEventTh1.setUpdatedDTTM(DateTime.now());
			

			LifeCycleEventBean eventBeanTh0 = new LifeCycleEventBean();
			eventBeanTh0.setAnimalTag(vaccineOk.getAnimalTag());
			eventBeanTh0.setEventCode(Util.LifeCycleEvents.VACCINE);
			eventBeanTh0.setEventComments("Test FMD Vaccination Event - no violation");
			eventBeanTh0.setOrgID("IMD");
			eventBeanTh0.setAuxField1Value("FOOT&MOUTH");
			
			eventBeanTh0.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(149)));
			LifecycleEvent inseminationEventTh0 = new LifecycleEvent(eventBeanTh0);
			inseminationEventTh0.setCreatedBy(new User("KASHIF"));
			inseminationEventTh0.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh0.setCreatedDTTM(DateTime.now());
			inseminationEventTh0.setUpdatedDTTM(DateTime.now());			
			

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

			List<Animal> animalPop = fmd.getAdvisementRuleAddressablePopulation("IMD");
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
			lactatingNotPregnant.setAnimalStatus("ACTIVE");
			lactatingNotPregnant.setAnimalType("LACTATING");

			Animal lactatingNonDryPregnantThreshold3 = createTestAnimal("-998");
			lactatingNonDryPregnantThreshold3.setAnimalStatus("ACTIVE");
			lactatingNonDryPregnantThreshold3.setAnimalType("LCTPRGNT");
			
			Animal dryPregnant = createTestAnimal("-997");
			dryPregnant.setAnimalStatus("ACTIVE");
			dryPregnant.setAnimalType("DRYPREG");

			Animal lactatingNonDryPregnantThreshold1 = createTestAnimal("-996");
			lactatingNonDryPregnantThreshold1.setAnimalStatus("ACTIVE");
			lactatingNonDryPregnantThreshold1.setAnimalType("LCTPRGNT");
			
			
			LifeCycleEventBean eventBean1 = new LifeCycleEventBean();
			eventBean1.setAnimalTag(lactatingNonDryPregnantThreshold3.getAnimalTag());
			eventBean1.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBean1.setEventComments("Test Insemination Event");
			eventBean1.setOrgID("IMD");
			eventBean1.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(240)));
			LifecycleEvent inseminationEvent1 = new LifecycleEvent(eventBean1);
			inseminationEvent1.setCreatedBy(new User("KASHIF"));
			inseminationEvent1.setUpdatedBy(new User("KASHIF"));
			inseminationEvent1.setCreatedDTTM(DateTime.now());
			inseminationEvent1.setUpdatedDTTM(DateTime.now());

			LifeCycleEventBean eventBean2 = new LifeCycleEventBean();
			eventBean2.setAnimalTag(lactatingNonDryPregnantThreshold1.getAnimalTag());
			eventBean2.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBean2.setEventComments("Test Insemination Event");
			eventBean2.setOrgID("IMD");
			eventBean2.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(161)));
			LifecycleEvent inseminationEvent2 = new LifecycleEvent(eventBean2);
			inseminationEvent2.setCreatedBy(new User("KASHIF"));
			inseminationEvent2.setUpdatedBy(new User("KASHIF"));
			inseminationEvent2.setCreatedDTTM(DateTime.now());
			inseminationEvent2.setUpdatedDTTM(DateTime.now());

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

			List<Animal> animalPop = dryAdv.getAdvisementRuleAddressablePopulation("IMD");
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
	void testAdvisementManager() {
//		IMDLogger.loggingMode = Util.INFO;

		try {			
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			
			///// clean up /////
			animalLoader.deleteAnimal("IMD", "-999");
			animalLoader.deleteAnimal("IMD", "-998");
			animalLoader.deleteAnimal("IMD", "-997");
			animalLoader.deleteAnimal("IMD", "-996");
			animalLoader.deleteAnimal("IMD", "-995");
			animalLoader.deleteAnimal("IMD", "-994");
			animalLoader.deleteAnimal("IMD", "-993");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-993");
			///////////////////			

			Animal lactatingNotPregnant = createTestAnimal("-999");
			lactatingNotPregnant.setAnimalStatus("ACTIVE");
			lactatingNotPregnant.setAnimalType("LACTATING");

			Animal lactatingNonDryPregnant = createTestAnimal("-998");
			lactatingNonDryPregnant.setAnimalStatus("ACTIVE");
			lactatingNonDryPregnant.setAnimalType("LCTPRGNT");
			
			Animal dryPregnant = createTestAnimal("-997");
			dryPregnant.setAnimalStatus("ACTIVE");
			dryPregnant.setAnimalType("DRYPREG");
			
			Animal nonDehornedCalf = createTestAnimal("-996");
			nonDehornedCalf.setDateOfBirth(DateTime.now().minusDays(90));
			nonDehornedCalf.setAnimalStatus("ACTIVE");
			nonDehornedCalf.setAnimalType("FEMALECALF");

			Animal nonFmd = createTestAnimal("-995");
			nonFmd.setDateOfBirth(DateTime.now().minusDays(90));
			nonFmd.setAnimalStatus("ACTIVE");
			nonFmd.setAnimalType("FEMALECALF");

			Animal inseminationAnimalTh3 = createTestAnimal("-994");
			inseminationAnimalTh3.setAnimalStatus("ACTIVE");
			inseminationAnimalTh3.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean inseminationBeanTh3 = new LifeCycleEventBean();
			inseminationBeanTh3.setAnimalTag(inseminationAnimalTh3.getAnimalTag());
			inseminationBeanTh3.setEventCode(Util.LifeCycleEvents.MATING);
			inseminationBeanTh3.setEventComments("Test  Event - violates Threshold ");
			inseminationBeanTh3.setOrgID("IMD");
			inseminationBeanTh3.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(82)));
			LifecycleEvent inseminationEventTh3 = new LifecycleEvent(inseminationBeanTh3);
			inseminationEventTh3.setCreatedBy(new User("KASHIF"));
			inseminationEventTh3.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh3.setCreatedDTTM(DateTime.now());
			inseminationEventTh3.setUpdatedDTTM(DateTime.now());			

			
			Animal heatWarningTh1 = createTestAnimal("-993");
			heatWarningTh1.setAnimalStatus("ACTIVE");
			heatWarningTh1.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean heatWarningBeanTh1 = new LifeCycleEventBean();
			heatWarningBeanTh1.setAnimalTag(heatWarningTh1.getAnimalTag());
			heatWarningBeanTh1.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			heatWarningBeanTh1.setEventComments("Test  Event - violates Threshold ");
			heatWarningBeanTh1.setOrgID("IMD");
			heatWarningBeanTh1.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(82)));
			LifecycleEvent heatWarningEventTh1 = new LifecycleEvent(heatWarningBeanTh1);
			heatWarningEventTh1.setCreatedBy(new User("KASHIF"));
			heatWarningEventTh1.setUpdatedBy(new User("KASHIF"));
			heatWarningEventTh1.setCreatedDTTM(DateTime.now());
			heatWarningEventTh1.setUpdatedDTTM(DateTime.now());			
			
			
			LifeCycleEventBean eventBean = new LifeCycleEventBean();
			eventBean.setAnimalTag(lactatingNonDryPregnant.getAnimalTag());
			eventBean.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBean.setEventComments("Test Insemination Event");
			eventBean.setOrgID("IMD");
			eventBean.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(240)));
			LifecycleEvent inseminationEvent = new LifecycleEvent(eventBean);
			inseminationEvent.setCreatedBy(new User("KASHIF"));
			inseminationEvent.setUpdatedBy(new User("KASHIF"));
			inseminationEvent.setCreatedDTTM(DateTime.now());
			inseminationEvent.setUpdatedDTTM(DateTime.now());
			
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
			
			assertEquals(1,transactionID1, "Exactly one record -999 should have been inserted"); // OK
			assertEquals(1,transactionID2, "Exactly one record -998 should have been inserted"); // Violates the DRYCOW rule
			assertEquals(1,transactionID3, "Exactly one record -997 should have been inserted"); // OK
			assertEquals(1,transactionID4, "Exactly one record -996 should have been inserted"); // violates the DEHORN rule
			assertEquals(1,transactionID5, "Exactly one record -995 should have been inserted"); // violates the FMDVACCINE rule
			assertEquals(1,transactionID6, "Exactly one record -994 should have been inserted"); // violates the PREGNANCYTEST rule
			assertEquals(1,transactionID7, "Exactly one record -993 should have been inserted"); // violates the HEATWARNING rule
			
			AdvisementLoader advLoader = new AdvisementLoader();
			List<Advisement> activeRules = advLoader.getAllActiveRules("IMD");
			assertTrue(activeRules!=null && !activeRules.isEmpty(), "At least one rule should have been enabled");
			assertTrue(activeRules.get(0).getOrgId()!=null && !activeRules.get(0).getOrgId().isEmpty(), "Org ID should have been set");
			AdvisementRuleManager advManager = new AdvisementRuleManager();
			List<AnimalAdvisement> advResults = advManager.executeAllRules(activeRules,false,false,false);			
			assertTrue(advResults == null || advResults.isEmpty(),"We had set all thresholds to false so nothing should have been returned");
			
			advResults = advManager.executeAllRules(activeRules,true,true,true);
			assertTrue(advResults != null && !advResults.isEmpty(),"We had set all thresholds to true so we should have received some values provided there were some enabled rules");

			boolean lactatingNonDryPregnantFound = false;
			boolean nonDehornedCalfFound = false;
			boolean nonFMDFound = false;
			boolean pregTestFound = false;
			boolean heatWarning1Found = false;
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
				}
			}
			assertTrue(lactatingNonDryPregnantFound,lactatingNonDryPregnant.getAnimalTag() +  "("+ lactatingNonDryPregnant.getAnimalType() + ") cow should have been included in the Dry Cow Advisement population");
			assertTrue(nonDehornedCalfFound,nonDehornedCalf.getAnimalTag() +  "("+ nonDehornedCalf.getAnimalType() + ") should have been included in the Dehorning Advisement population");
			assertTrue(nonFMDFound,nonFmd.getAnimalTag() +  "("+ nonFmd.getAnimalType() + ") should have been included in the FMD Vaccination Advisement population");
			assertTrue(pregTestFound,inseminationAnimalTh3.getAnimalTag() +  "("+ inseminationAnimalTh3.getAnimalType() + ") should have been included in the Pregnancy Test Advisement population");
			assertTrue(heatWarning1Found,heatWarningTh1.getAnimalTag() +  "("+ heatWarningTh1.getAnimalType() + ") should have been included in the Heat Warning Test Advisement population");
			
			///// clean up /////
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-999"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-998"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-997"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-996"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-995"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-994"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-993"),"Exactly one record should have been deleted");
			assertEquals(0,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998"),"Exactly one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997"),"Exactly one record should have been deleted");
			assertEquals(0,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(0,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994"),"We added Lifecycle events so one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-993"),"We added Lifecycle events so one record should have been deleted");
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}	
	public Animal createTestAnimal(String animalTag) throws Exception {
		Dam c000 = new Dam(/*orgid*/"IMD",/*tag*/animalTag,/*dob*/DateTime.parse("2014-02-09"),/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		c000.setAlias("Laal");
		c000.setAnimalType("LACTATING");
		c000.setAnimalStatus(Util.ANIMAL_STATUS.ACTIVE);
		c000.setFrontSideImageURL("/assets/img/cow-thumbnails/000/1.png");
		c000.setBackSideImageURL("/assets/img/cow-thumbnails/000/2.png");
		c000.setRightSideImageURL("/assets/img/cow-thumbnails/000/3.png");
		c000.setLeftSideImageURL("/assets/img/cow-thumbnails/000/4.png");
		c000.setPurchaseDate(DateTime.parse("2017-02-08"));
		c000.setCreatedBy(new User("KASHIF"));
		c000.setCreatedDTTM(DateTime.now());
		c000.setUpdatedBy(c000.getCreatedBy());
		c000.setUpdatedDTTM(c000.getCreatedDTTM());
		c000.setAnimalSire(null);
		c000.setAnimalDam(null);
		return c000;
	}	
	
}
