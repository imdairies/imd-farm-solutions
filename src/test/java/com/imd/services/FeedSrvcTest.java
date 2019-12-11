package com.imd.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Advisement;
import com.imd.dto.Animal;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.User;
import com.imd.loader.AdvisementLoader;
import com.imd.loader.AnimalLoader;
import com.imd.loader.LifeCycleEventsLoader;
import com.imd.loader.UserLoader;
import com.imd.services.bean.AdvisementBean;
import com.imd.services.bean.AnimalBean;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

class FeedSrvcTest {

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
	void testFarmFeedListing() {
		int loggingMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;
		try {
//			Animal youngAnimalTh1 = this.createTestAnimal("-999", Util.AnimalTypes.FEMALECALF);
//			Animal youngAnimalTh2 = this.createTestAnimal("-998", Util.AnimalTypes.MALECALF);
//			Animal youngAnimalTh3 = this.createTestAnimal("-997", Util.AnimalTypes.FEMALECALF);
//			Animal youngAnimalOk = this.createTestAnimal("-996", Util.AnimalTypes.FEMALECALF);
//			Animal oldAnimal = this.createTestAnimal("-995", Util.AnimalTypes.FEMALECALF);
			
			AnimalBean animalBean = new AnimalBean();
			FeedSrvc feedSrvc = new FeedSrvc();
			User kashif = new User("KASHIF");			
			UserLoader userLoader = new UserLoader();
			User user = userLoader.authenticateUser("IMD", "KASHIF", userLoader.encryptPassword("DUMMY"));
			assertTrue(user != null);
			assertTrue(user.getPassword() != null);
			animalBean.setLoginToken(user.getPassword());
			Response serviceResponse = feedSrvc.retrieveActiveAnimalFeedListing(animalBean);
			assertEquals(Util.HTTPCodes.OK,serviceResponse.getStatus());
//			assertEquals(Util.LanguageCode.URD,Util.getConfigurations().getSessionConfigurationValue(Util.ConfigKeys.LANG_CD));
//			assertTrue(serviceResponseJson.toString()!= null);
//			assertTrue(serviceResponseJson.indexOf("ان جانوروں کا وزن آدھہ کلو یومیہ سے کم بڑھا ہے") > 0,serviceResponseJson);
			
//			assertTrue(evtLoader.deleteAnimalLifecycleEvents(youngAnimalTh1.getOrgID(), youngAnimalTh1.getAnimalTag()) == 2);
//			assertTrue(ldr.deleteAnimal(youngAnimalTh1.getOrgID(), youngAnimalTh1.getAnimalTag()) == 1);
			assertEquals(1,userLoader.logoutUser(user.getPassword()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception in AdvisementSrvcTest.testCalfWeightAdvisement");
		} finally {
			IMDLogger.loggingMode = loggingMode;
		}
	}
}
