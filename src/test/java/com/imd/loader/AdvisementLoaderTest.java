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
import com.imd.advisement.DryCowAdvisement;
import com.imd.advisement.FMDVaccinationAdvisement;
import com.imd.advisement.PregnancyTestAdvisement;
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
	void testDehornAdvisementRulePopulationRetrieval() {
		IMDLogger.loggingMode = Util.WARNING;

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
						assertFalse(populationAnimal.isThreshold1Violated(),"This calf should have violated second threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This calf should have violated second threshold");
						assertTrue(populationAnimal.isThreshold3Violated(),"This calf should have violated second threshold");
						femaleFound = true;
					} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(maleCalf.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This calf should have violated second threshold");
						assertFalse(populationAnimal.isThreshold2Violated(),"This calf should have violated second threshold");
						assertTrue(populationAnimal.isThreshold3Violated(),"This calf should have violated second threshold");
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
			eventBeanTh3.setEventCode(Util.LifeCycleEvents.INSEMINATE);
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
			eventBeanTh2.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(95)));
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
			assertEquals(1,transTh2, "Exactly one record -997 should have been inserted");
			assertEquals(1,transTh1, "Exactly one record -997 should have been inserted");

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
		}	}
	
	
	
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
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
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
			
			LifeCycleEventBean eventBeanTh3 = new LifeCycleEventBean();
			eventBeanTh3.setAnimalTag(vaccineTh3.getAnimalTag());
			eventBeanTh3.setEventCode(Util.LifeCycleEvents.FMDVACCINE);
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
			eventBeanTh2.setEventCode(Util.LifeCycleEvents.FMDVACCINE);
			eventBeanTh2.setEventComments("Test FMD Vaccination Event - violates Threshold 2");
			eventBeanTh2.setOrgID("IMD");
			eventBeanTh2.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(175)));
			LifecycleEvent inseminationEventTh2 = new LifecycleEvent(eventBeanTh2);
			inseminationEventTh2.setCreatedBy(new User("KASHIF"));
			inseminationEventTh2.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh2.setCreatedDTTM(DateTime.now());
			inseminationEventTh2.setUpdatedDTTM(DateTime.now());
		
			LifeCycleEventBean eventBeanTh1 = new LifeCycleEventBean();
			eventBeanTh1.setAnimalTag(vaccineTh1.getAnimalTag());
			eventBeanTh1.setEventCode(Util.LifeCycleEvents.FMDVACCINE);
			eventBeanTh1.setEventComments("Test FMD Vaccination Event - violates Threshold 1");
			eventBeanTh1.setOrgID("IMD");
			eventBeanTh1.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(151)));
			LifecycleEvent inseminationEventTh1 = new LifecycleEvent(eventBeanTh1);
			inseminationEventTh1.setCreatedBy(new User("KASHIF"));
			inseminationEventTh1.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh1.setCreatedDTTM(DateTime.now());
			inseminationEventTh1.setUpdatedDTTM(DateTime.now());
			

			LifeCycleEventBean eventBeanTh0 = new LifeCycleEventBean();
			eventBeanTh0.setAnimalTag(vaccineOk.getAnimalTag());
			eventBeanTh0.setEventCode(Util.LifeCycleEvents.FMDVACCINE);
			eventBeanTh0.setEventComments("Test FMD Vaccination Event - no violation");
			eventBeanTh0.setOrgID("IMD");
			eventBeanTh0.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(149)));
			LifecycleEvent inseminationEventTh0 = new LifecycleEvent(eventBeanTh0);
			inseminationEventTh0.setCreatedBy(new User("KASHIF"));
			inseminationEventTh0.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh0.setCreatedDTTM(DateTime.now());
			inseminationEventTh0.setUpdatedDTTM(DateTime.now());			
			

			int transNoVaccine = animalLoader.insertAnimal(noVaccine);

			int transVaccineOk = animalLoader.insertAnimal(vaccineOk);
			inseminationEventTh0.setEventNote("Test Insemination Event. This animal should not violate any threshold.");
			inseminationEventTh0.setAnimalTag(vaccineOk.getAnimalTag());
			eventsLoader.insertLifeCycleEvent(inseminationEventTh0);
			
			int transVaccineTh3 = animalLoader.insertAnimal(vaccineTh3);
			inseminationEventTh3.setAnimalTag(vaccineTh3.getAnimalTag());
			inseminationEventTh3.setEventNote("Test Insemination Event. This cow violated TH3.");
			eventsLoader.insertLifeCycleEvent(inseminationEventTh3);


			int transVaccineTh2 = animalLoader.insertAnimal(vaccineTh2);
			inseminationEventTh2.setAnimalTag(vaccineTh2.getAnimalTag());
			inseminationEventTh2.setEventNote("Test Insemination Event. This cow violated TH2.");
			eventsLoader.insertLifeCycleEvent(inseminationEventTh2);

			int transVaccineTh1 = animalLoader.insertAnimal(vaccineTh1);
			inseminationEventTh1.setAnimalTag(vaccineTh1.getAnimalTag());
			inseminationEventTh1.setEventNote("Test Insemination Event. This cow violated TH1.");
			eventsLoader.insertLifeCycleEvent(inseminationEventTh1);
			
			
			
			assertEquals(1,transNoVaccine, "Exactly one record -999 should have been inserted");
			assertEquals(1,transVaccineOk, "Exactly one record -998 should have been inserted");			
			assertEquals(1,transVaccineTh3, "Exactly one record -997 should have been inserted");
			assertEquals(1,transVaccineTh2, "Exactly one record -997 should have been inserted");
			assertEquals(1,transVaccineTh1, "Exactly one record -997 should have been inserted");

			List<Animal> animalPop = fmd.getAdvisementRuleAddressablePopulation("IMD");
			boolean th3Found = false;
			boolean th2Found = false;
			boolean th1Found = false;
			boolean noVaccineFound = false;
			
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
			assertTrue(th1Found,vaccineTh1.getAnimalTag() +  "("+ vaccineTh1.getAnimalType() + ") should have been included in the Threshold1 Violation Advisement population");
			assertTrue(th2Found,vaccineTh2.getAnimalTag() +  "("+ vaccineTh2.getAnimalType() + ") should have been included in the Threshold2 Violation Advisement population");
			assertTrue(th3Found,vaccineTh3.getAnimalTag() +  "("+ vaccineTh3.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			
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
		}	}
	
	
	
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
		IMDLogger.loggingMode = Util.WARNING;

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
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995");
			eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994");
			///////////////////
			
			Animal inseminationTh3 = createTestAnimal("-994");
			inseminationTh3.setAnimalStatus("ACTIVE");
			inseminationTh3.setAnimalType("LCTINSEMIN");
			LifeCycleEventBean eventBeanTh3 = new LifeCycleEventBean();
			eventBeanTh3.setAnimalTag(inseminationTh3.getAnimalTag());
			eventBeanTh3.setEventCode(Util.LifeCycleEvents.INSEMINATE);
			eventBeanTh3.setEventComments("Test  Event - violates Threshold 3");
			eventBeanTh3.setOrgID("IMD");
			eventBeanTh3.setEventTimeStamp(Util.getDateInSQLFormart(DateTime.now().minusDays(210)));
			LifecycleEvent inseminationEventTh3 = new LifecycleEvent(eventBeanTh3);
			inseminationEventTh3.setCreatedBy(new User("KASHIF"));
			inseminationEventTh3.setUpdatedBy(new User("KASHIF"));
			inseminationEventTh3.setCreatedDTTM(DateTime.now());
			inseminationEventTh3.setUpdatedDTTM(DateTime.now());			


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
			
			int transactionID6 = animalLoader.insertAnimal(inseminationTh3);

			assertEquals(1,transactionID1, "Exactly one record -999 should have been inserted"); // OK
			assertEquals(1,transactionID2, "Exactly one record -998 should have been inserted"); // Violates the DRYCOW rule
			assertEquals(1,transactionID3, "Exactly one record -997 should have been inserted"); // OK
			assertEquals(1,transactionID4, "Exactly one record -996 should have been inserted"); // violates the DEHORN rule
			assertEquals(1,transactionID5, "Exactly one record -995 should have been inserted"); // violates the FMDVACCINE rule
			assertEquals(1,transactionID6, "Exactly one record -994 should have been inserted"); // violates the PREGNANCYTEST rule
			
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
			Iterator<AnimalAdvisement> it = advResults.iterator();
			while (it.hasNext()) {
				AnimalAdvisement populationAnimal = it.next();
				IMDLogger.log(populationAnimal.toString(), Util.WARNING);				
				IMDLogger.log(populationAnimal.toString() + "[" + populationAnimal.getAnimalTag() + "]", Util.INFO);
				if (populationAnimal.getAnimalTag().equalsIgnoreCase(lactatingNotPregnant.getAnimalTag()) && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.DRYCOW)) {
					fail(lactatingNotPregnant.getAnimalTag() +  "("+ lactatingNotPregnant.getAnimalType() + ") is lactating and not pregnant so it should not be in the " + populationAnimal.getAppliedAdvisementRule() + "  Advisement population");
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(lactatingNonDryPregnant.getAnimalTag())  && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.DRYCOW)) {
					lactatingNonDryPregnantFound = true;
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(dryPregnant.getAnimalTag()) && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.DRYCOW)) {
					IMDLogger.log(populationAnimal.getRuleOutcomeLongMessage(), Util.INFO);
					fail(dryPregnant.getAnimalTag() +  "("+ dryPregnant.getAnimalType() + ") cow was already dry so it should not have been in the DryCow Advisement population.");
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(nonDehornedCalf.getAnimalTag())  && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.DEHORN)) {
					nonDehornedCalfFound = true;
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(nonFmd.getAnimalTag()) && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.VACCINEFMD)) {
					nonFMDFound = true;
				} else if (populationAnimal.getAnimalTag().equalsIgnoreCase(inseminationEventTh3.getAnimalTag()) && populationAnimal.getAppliedAdvisementRule().equalsIgnoreCase(Util.AdvisementRules.PREGNANCYTEST)) {
					pregTestFound = true;
				}
			}
			assertTrue(lactatingNonDryPregnantFound,lactatingNonDryPregnant.getAnimalTag() +  "("+ lactatingNonDryPregnant.getAnimalType() + ") cow should have been included in the Dry Cow Advisement population");
			assertTrue(nonDehornedCalfFound,nonDehornedCalf.getAnimalTag() +  "("+ nonDehornedCalf.getAnimalType() + ") should have been included in the Dehorning Advisement population");
			assertTrue(nonFMDFound,nonFmd.getAnimalTag() +  "("+ nonFmd.getAnimalType() + ") should have been included in the FMD Vaccination Advisement population");
			assertTrue(pregTestFound,inseminationTh3.getAnimalTag() +  "("+ inseminationTh3.getAnimalType() + ") should have been included in the Pregnancy Test Advisement population");
			
			///// clean up /////
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-999"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-998"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-997"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-996"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-995"),"Exactly one record should have been deleted");
			assertEquals(1,animalLoader.deleteAnimal("IMD", "-994"),"Exactly one record should have been deleted");
			assertEquals(0,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-999"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-998"),"Exactly one record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-997"),"Exactly one record should have been deleted");
			assertEquals(0,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-996"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(0,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-995"),"We did not add any Lifecycle event so no record should have been deleted");
			assertEquals(1,eventsLoader.deleteAnimalLifecycleEvents("IMD", "-994"),"We added Lifecycle events so one record should have been deleted");
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
