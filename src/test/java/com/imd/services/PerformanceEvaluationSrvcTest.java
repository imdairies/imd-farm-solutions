package com.imd.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.User;
import com.imd.loader.UserLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

import javax.ws.rs.core.Response;

class PerformanceEvaluationSrvcTest {

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
	void testSpecificAnimalPerformanceEvaluation() {
		IMDLogger.loggingMode = Util.INFO;
		String animalTag = "058";
		PerformanceEvaluationSrvc srvc = new PerformanceEvaluationSrvc();
		UserLoader userLoader = new UserLoader();
		AnimalBean animalBean = new AnimalBean();
		animalBean.setAnimalTag(animalTag);
		Response resp = srvc.evaluateAnimalPerformance(animalBean);
		assertEquals(Util.HTTPCodes.UNAUTHORIZED,resp.getStatus());
		
		User user = userLoader.authenticateUser("IMD", "KASHIF", userLoader.encryptPassword("DUMMY"));
		assertTrue(user != null);
		assertTrue(user.getPassword() != null);
		animalBean.setLoginToken(user.getPassword());
		resp = srvc.evaluateAnimalPerformance(animalBean);
		assertEquals(Util.HTTPCodes.OK,resp.getStatus());
	}
}





