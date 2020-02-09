package com.imd.services;

import static org.junit.jupiter.api.Assertions.*;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.User;
import com.imd.loader.UserLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.MilkingDetailBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

class MilkingInformationSrvcTest {

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
	void testCompleteMilkingInformation() {
		IMDLogger.loggingMode = Util.INFO;
		String animalTag = "058";
		MilkingInformationSrvc srvc = new MilkingInformationSrvc();
		UserLoader userLoader = new UserLoader();
		MilkingDetailBean searchBean = new MilkingDetailBean();
		searchBean.setAnimalTag(animalTag);
		Response resp = srvc.retrieveCompleteMilkingRecordOfFarm(searchBean);
		assertEquals(Util.HTTPCodes.UNAUTHORIZED,resp.getStatus());
		
		User user = userLoader.authenticateUser("IMD", "KASHIF", userLoader.encryptPassword("DUMMY"));
		assertTrue(user != null);
		assertTrue(user.getPassword() != null);
		searchBean.setLoginToken(user.getPassword());
		resp = srvc.retrieveCompleteMilkingRecordOfFarm(searchBean);
		assertEquals(Util.HTTPCodes.OK,resp.getStatus());
	}

}
