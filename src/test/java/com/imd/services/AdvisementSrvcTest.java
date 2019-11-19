package com.imd.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.advisement.CalfWeightAdvisement;
import com.imd.dto.Advisement;
import com.imd.dto.Animal;
import com.imd.dto.Dam;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.Note;
import com.imd.dto.Person;
import com.imd.dto.User;
import com.imd.loader.AdvisementLoader;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.UserLoader;
import com.imd.services.bean.AdvisementBean;
import com.imd.services.bean.LifeCycleEventBean;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

class AdvisementSrvcTest {

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
	
	public Animal createTestAnimal(String animalTag, String animalType) throws Exception {
		Dam c000 = new Dam(/*orgid*/"IMD",/*tag*/animalTag,/*dob*/DateTime.parse("2014-02-09"),/*dob estimated*/true,/*price*/331000,/*price currency*/"PKR");
		c000.setAlias("Laal");
		c000.setBreed(Util.Breed.HFCROSS);
		c000.setAnimalType(animalType);
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


	@Test
	void testCalfWeightAdvisementInUrdu() {
		int loggingMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;
		try {
			Animal youngAnimalTh1 = this.createTestAnimal("-999", Util.AnimalTypes.FEMALECALF);
//			Animal youngAnimalTh2 = this.createTestAnimal("-998", Util.AnimalTypes.MALECALF);
//			Animal youngAnimalTh3 = this.createTestAnimal("-997", Util.AnimalTypes.FEMALECALF);
//			Animal youngAnimalOk = this.createTestAnimal("-996", Util.AnimalTypes.FEMALECALF);
//			Animal oldAnimal = this.createTestAnimal("-995", Util.AnimalTypes.FEMALECALF);
			
			
			LifeCycleEventsLoader evtLoader = new LifeCycleEventsLoader();
			AdvisementLoader advLdr =  new AdvisementLoader();
			List<Advisement> rules = advLdr.getSpecifiedActiveAdvisementRules(youngAnimalTh1.getOrgID(), Util.AdvisementRules.CALFWEIGHT);
			
			DateTime now = DateTime.now(IMDProperties.getServerTimeZone());
			DateTime dob = now.minusDays(Integer.parseInt(rules.get(0).getAuxInfo1()));
			AnimalLoader ldr = new AnimalLoader();
			
			
			assertTrue(evtLoader.deleteAnimalLifecycleEvents(youngAnimalTh1.getOrgID(), youngAnimalTh1.getAnimalTag()) >= 0);
			
			assertTrue(ldr.deleteAnimal(youngAnimalTh1.getOrgID(), youngAnimalTh1.getAnimalTag()) >= 0);

			youngAnimalTh1.setDateOfBirth(dob.plusDays(10));
			
			ldr.insertAnimal(youngAnimalTh1);
			User kashif = new User("KASHIF");

			
			LifecycleEvent weightMeasurementEvent = new LifecycleEvent(youngAnimalTh1.getOrgID(), 0,youngAnimalTh1.getAnimalTag(), Util.LifeCycleEvents.WEIGHT, 
					kashif,dob.plusMonths(1), kashif, dob.plusMonths(1));
			weightMeasurementEvent.setEventNote("Test  Event - Weight 1");
			weightMeasurementEvent.setEventTimeStamp(youngAnimalTh1.getDateOfBirth().plusDays(15));
			weightMeasurementEvent.setAuxField1Value("35");
			
			assertTrue(evtLoader.insertLifeCycleEvent(weightMeasurementEvent)>0);

			weightMeasurementEvent.setAuxField1Value("39.5");
			weightMeasurementEvent.setEventTimeStamp(weightMeasurementEvent.getEventTimeStamp().plusDays(10));
			weightMeasurementEvent.setCreatedDTTM(weightMeasurementEvent.getCreatedDTTM().plusDays(10));
			weightMeasurementEvent.setUpdatedDTTM(weightMeasurementEvent.getUpdatedDTTM().plusDays(10));

			assertTrue(evtLoader.insertLifeCycleEvent(weightMeasurementEvent)>0);

			AdvisementBean advBean = new AdvisementBean();
			advBean.setAdvisementID(Util.AdvisementRules.CALFWEIGHT);
			advBean.setAnimalTag("%"/*youngAnimalTh1.getAnimalTag()*/);
			advBean.setThreshold1Violated(true);
			advBean.setThreshold2Violated(true);
			advBean.setThreshold3Violated(true);
			
			AdvisementSrvc srvc = new AdvisementSrvc();
			UserLoader userLoader = new UserLoader();
			User user = userLoader.authenticateUser("IMD", "KASHIF", "DUMMY");
			assertTrue(user != null);
			assertTrue(user.getPassword() != null);
			advBean.setLoginToken(user.getPassword());
			String serviceResponseJson = srvc.retrieveAllAdvisement(advBean).getEntity().toString();
			assertEquals(Util.LanguageCode.URD,Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.LANG_CD));
			assertTrue(serviceResponseJson.indexOf("ان جانوروں کا وزن آدھہ کلو یومیہ سے کم بڑھا ہے") > 0,serviceResponseJson);
			
			assertTrue(evtLoader.deleteAnimalLifecycleEvents(youngAnimalTh1.getOrgID(), youngAnimalTh1.getAnimalTag()) == 2);
			assertTrue(ldr.deleteAnimal(youngAnimalTh1.getOrgID(), youngAnimalTh1.getAnimalTag()) == 1);
			assertEquals(1,userLoader.logoutUser(user.getPassword()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception in AdvisementSrvcTest.testCalfWeightAdvisement");
		} finally {
			IMDLogger.loggingMode = loggingMode;
		}
	}
}
