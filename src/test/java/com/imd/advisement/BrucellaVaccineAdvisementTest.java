package com.imd.advisement;

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

import com.imd.dto.Advisement;
import com.imd.dto.Animal;
import com.imd.dto.Dam;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.User;
import com.imd.loader.AdvisementLoader;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.services.bean.LifeCycleEventBean;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

class BrucellaVaccineAdvisementTest {

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
	private int insertEvent(String animalTag, String comments, String eventCode, String aux1, String aux2, DateTime eventDTTM) throws IMDException, SQLException {
		LifeCycleEventBean eventBean = new LifeCycleEventBean();
		eventBean.setAnimalTag(animalTag);
		eventBean.setEventCode(eventCode);
		if (aux1 != null)
			eventBean.setAuxField1Value(aux1);
		if (aux2 != null)
			eventBean.setAuxField1Value(aux2);
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
	@Test
	void testThreshold3Violation() {
		try {
			IMDLogger.loggingMode = Util.INFO;
			BrucellaVaccineAdvisement brucellaAdvisement = new BrucellaVaccineAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			AdvisementLoader advLdr = new AdvisementLoader();
			
			Animal animal = createTestAnimal("-999");

			///// clean up /////
			animalLoader.deleteAnimal(animal.getOrgId(), animal.getAnimalTag());
			eventsLoader.deleteAnimalLifecycleEvents(animal.getOrgId(), animal.getAnimalTag());
			///////////////////

			Advisement rulDto = advLdr.getSpecifiedActiveAdvisementRules(animal.getOrgId(), Util.AdvisementRules.BRUCELLAVACCINE).get(0);
			animal.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(Integer.parseInt(rulDto.getAuxInfo2()) - 1));
			animal.setHerdLeavingDate(null);
			animal.setHerdJoiningDate(animal.getDateOfBirth());
			animal.setAnimalType(Util.AnimalTypes.HEIFER);
			assertEquals(1,animalLoader.insertAnimal(animal), "Exactly one record -999 should have been inserted");
			
//			assertTrue(insertEvent(animal.getAnimalTag(), "TO BE DELETED", Util.LifeCycleEvents.VACCINE, animal.getDateOfBirth().plusMonths(5)) > 0);
			

			List<Animal> animalPop = brucellaAdvisement.applyAdvisementRule(animal.getOrgId(), Util.LanguageCode.ENG);
			boolean thFound = false;
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(animal.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated third threshold. " + animal.getAnimalTag());
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated third threshold. " + animal.getAnimalTag());
						assertTrue(populationAnimal.isThreshold3Violated() ,"This animal should have violated third threshold. " + animal.getAnimalTag());
						thFound = true;
					}
				}
			}
			assertTrue(thFound,animal.getAnimalTag() +  "("+ animal.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			

			assertTrue(insertEvent(animal.getAnimalTag(), "TO BE DELETED", Util.LifeCycleEvents.VACCINE, Util.LookupValues.BRUCELLA, null, animal.getDateOfBirth().plusMonths(5)) > 0);

			animalPop = brucellaAdvisement.applyAdvisementRule(animal.getOrgId(), Util.LanguageCode.ENG);
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(animal.getAnimalTag())) {
						fail("This animal should has been vaccinated and should not have violated third threshold. " + animal.getAnimalTag());
					}
				}
			}
			assertTrue(thFound,animal.getAnimalTag() +  "("+ animal.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			
			
			///// clean up /////
			animalLoader.deleteAnimal(animal.getOrgId(), animal.getAnimalTag());
			eventsLoader.deleteAnimalLifecycleEvents(animal.getOrgId(), animal.getAnimalTag());
			///////////////////
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}

	@Test
	void testThreshold2Violation() {
		try {
			IMDLogger.loggingMode = Util.INFO;
			BrucellaVaccineAdvisement brucellaAdvisement = new BrucellaVaccineAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			AdvisementLoader advLdr = new AdvisementLoader();
			
			Animal animal = createTestAnimal("-999");

			///// clean up /////
			animalLoader.deleteAnimal(animal.getOrgId(), animal.getAnimalTag());
			eventsLoader.deleteAnimalLifecycleEvents(animal.getOrgId(), animal.getAnimalTag());
			///////////////////

			Advisement rulDto = advLdr.getSpecifiedActiveAdvisementRules(animal.getOrgId(), Util.AdvisementRules.BRUCELLAVACCINE).get(0);
			animal.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays((int)rulDto.getSecondThreshold() + 1));
			animal.setHerdLeavingDate(null);
			animal.setHerdJoiningDate(animal.getDateOfBirth());
			animal.setAnimalType(Util.AnimalTypes.HEIFER);
			assertEquals(1,animalLoader.insertAnimal(animal), "Exactly one record -999 should have been inserted");
			
//			assertTrue(insertEvent(animal.getAnimalTag(), "TO BE DELETED", Util.LifeCycleEvents.VACCINE, animal.getDateOfBirth().plusMonths(5)) > 0);
			

			List<Animal> animalPop = brucellaAdvisement.applyAdvisementRule(animal.getOrgId(), Util.LanguageCode.ENG);
			boolean thFound = false;
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(animal.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated 2nd threshold. " + animal.getAnimalTag());
						assertTrue(populationAnimal.isThreshold2Violated(),"This animal should have violated 2nd threshold. " + animal.getAnimalTag());
						assertFalse(populationAnimal.isThreshold3Violated() ,"This animal should have violated 2nd threshold. " + animal.getAnimalTag());
						thFound = true;
					}
				}
			}
			assertTrue(thFound,animal.getAnimalTag() +  "("+ animal.getAnimalType() + ") should have been included in the Threshold2 Violation Advisement population");
			

			assertTrue(insertEvent(animal.getAnimalTag(), "TO BE DELETED", Util.LifeCycleEvents.VACCINE, Util.LookupValues.BRUCELLA, null, animal.getDateOfBirth().plusMonths(5)) > 0);

			animalPop = brucellaAdvisement.applyAdvisementRule(animal.getOrgId(), Util.LanguageCode.ENG);
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(animal.getAnimalTag())) {
						fail("This animal should has been vaccinated and should not have violated 2nd threshold. " + animal.getAnimalTag());
					}
				}
			}
			assertTrue(thFound,animal.getAnimalTag() +  "("+ animal.getAnimalType() + ") should have been included in the 2nd Violation Advisement population");
			
			
			///// clean up /////
			animalLoader.deleteAnimal(animal.getOrgId(), animal.getAnimalTag());
			eventsLoader.deleteAnimalLifecycleEvents(animal.getOrgId(), animal.getAnimalTag());
			///////////////////
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}	

	@Test
	void testThreshold1Violation() {
		try {
			IMDLogger.loggingMode = Util.INFO;
			BrucellaVaccineAdvisement brucellaAdvisement = new BrucellaVaccineAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			AdvisementLoader advLdr = new AdvisementLoader();
			
			Animal animal = createTestAnimal("-999");

			///// clean up /////
			animalLoader.deleteAnimal(animal.getOrgId(), animal.getAnimalTag());
			eventsLoader.deleteAnimalLifecycleEvents(animal.getOrgId(), animal.getAnimalTag());
			///////////////////

			Advisement rulDto = advLdr.getSpecifiedActiveAdvisementRules(animal.getOrgId(), Util.AdvisementRules.BRUCELLAVACCINE).get(0);
			animal.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays((int)rulDto.getFirstThreshold() + 1));
			animal.setHerdLeavingDate(null);
			animal.setHerdJoiningDate(animal.getDateOfBirth());
			animal.setAnimalType(Util.AnimalTypes.HEIFER);
			assertEquals(1,animalLoader.insertAnimal(animal), "Exactly one record -999 should have been inserted");
			
//			assertTrue(insertEvent(animal.getAnimalTag(), "TO BE DELETED", Util.LifeCycleEvents.VACCINE, animal.getDateOfBirth().plusMonths(5)) > 0);
			

			List<Animal> animalPop = brucellaAdvisement.applyAdvisementRule(animal.getOrgId(), Util.LanguageCode.ENG);
			boolean thFound = false;
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(animal.getAnimalTag())) {
						assertTrue(populationAnimal.isThreshold1Violated(),"This animal should have violated 1st threshold. " + animal.getAnimalTag());
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated 1st threshold. " + animal.getAnimalTag());
						assertFalse(populationAnimal.isThreshold3Violated() ,"This animal should have violated 1st threshold. " + animal.getAnimalTag());
						thFound = true;
					}
				}
			}
			assertTrue(thFound,animal.getAnimalTag() +  "("+ animal.getAnimalType() + ") should have been included in the 1st Violation Advisement population");
			

			assertTrue(insertEvent(animal.getAnimalTag(), "TO BE DELETED", Util.LifeCycleEvents.VACCINE, Util.LookupValues.BRUCELLA, null, animal.getDateOfBirth().plusMonths(3)) > 0);

			animalPop = brucellaAdvisement.applyAdvisementRule(animal.getOrgId(), Util.LanguageCode.ENG);
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(animal.getAnimalTag())) {
						fail("This animal should has been vaccinated and should not have violated 1st threshold. " + animal.getAnimalTag());
					}
				}
			}
			assertTrue(thFound,animal.getAnimalTag() +  "("+ animal.getAnimalType() + ") should have been included in the 1st Violation Advisement population");
			
			
			///// clean up /////
			animalLoader.deleteAnimal(animal.getOrgId(), animal.getAnimalTag());
			eventsLoader.deleteAnimalLifecycleEvents(animal.getOrgId(), animal.getAnimalTag());
			///////////////////
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}
	

	@Test
	void testAgeLimits() {
		try {
			IMDLogger.loggingMode = Util.INFO;
			BrucellaVaccineAdvisement brucellaAdvisement = new BrucellaVaccineAdvisement();
			AnimalLoader animalLoader = new AnimalLoader();
			LifeCycleEventsLoader eventsLoader = new LifeCycleEventsLoader();
			AdvisementLoader advLdr = new AdvisementLoader();
			
			Animal animal = createTestAnimal("-999");

			///// clean up /////
			animalLoader.deleteAnimal(animal.getOrgId(), animal.getAnimalTag());
			eventsLoader.deleteAnimalLifecycleEvents(animal.getOrgId(), animal.getAnimalTag());
			///////////////////

			Advisement rulDto = advLdr.getSpecifiedActiveAdvisementRules(animal.getOrgId(), Util.AdvisementRules.BRUCELLAVACCINE).get(0);
			animal.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays((int)rulDto.getFirstThreshold() - 1));
			animal.setHerdLeavingDate(null);
			animal.setHerdJoiningDate(animal.getDateOfBirth());
			animal.setAnimalType(Util.AnimalTypes.HEIFER);
			assertEquals(1,animalLoader.insertAnimal(animal), "Exactly one record -999 should have been inserted");
			
			List<Animal> animalPop = brucellaAdvisement.applyAdvisementRule(animal.getOrgId(), Util.LanguageCode.ENG);
			boolean thFound = false;
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(animal.getAnimalTag())) {
						fail("This animal " + animal.getAnimalTag() + " is younger than the minimum age at which this advisement is applied, so it should not have been reported as a violation");
					}
				}
			}

			assertEquals(1,animalLoader.deleteAnimal(animal.getOrgId(), animal.getAnimalTag()));
			animal.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(Integer.parseInt(rulDto.getAuxInfo2()) - 1));
			assertEquals(1,animalLoader.insertAnimal(animal), "Exactly one record -999 should have been inserted");
			animalPop = brucellaAdvisement.applyAdvisementRule(animal.getOrgId(), Util.LanguageCode.ENG);
			thFound = false;
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(animal.getAnimalTag())) {
						assertFalse(populationAnimal.isThreshold1Violated(),"This animal should have violated third threshold. " + animal.getAnimalTag());
						assertFalse(populationAnimal.isThreshold2Violated(),"This animal should have violated third threshold. " + animal.getAnimalTag());
						assertTrue(populationAnimal.isThreshold3Violated() ,"This animal should have violated third threshold. " + animal.getAnimalTag());
						thFound = true;
					}
				}
			}
			assertTrue(thFound,animal.getAnimalTag() +  "("+ animal.getAnimalType() + ") should have been included in the Threshold3 Violation Advisement population");
			
			assertEquals(1,animalLoader.deleteAnimal(animal.getOrgId(), animal.getAnimalTag()));
			animal.setDateOfBirth(DateTime.now(IMDProperties.getServerTimeZone()).minusDays(Integer.parseInt(rulDto.getAuxInfo2()) + 1));

			animalPop = brucellaAdvisement.applyAdvisementRule(animal.getOrgId(), Util.LanguageCode.ENG);
			
			if (animalPop != null && !animalPop.isEmpty()) {
				Iterator<Animal> it = animalPop.iterator();
				while (it.hasNext()) {
					Animal populationAnimal = it.next();
					if (populationAnimal.getAnimalTag().equalsIgnoreCase(animal.getAnimalTag())) {
						fail("This animal " + animal.getAnimalTag() + " is older than the maximum age at which this advisement is applied, so it should not have been reported as a violation");
					}
				}
			}
			///// clean up /////
			animalLoader.deleteAnimal(animal.getOrgId(), animal.getAnimalTag());
			eventsLoader.deleteAnimalLifecycleEvents(animal.getOrgId(), animal.getAnimalTag());
			///////////////////
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception occurred " + ex.getMessage());
		}
	}
		
	
	
}
