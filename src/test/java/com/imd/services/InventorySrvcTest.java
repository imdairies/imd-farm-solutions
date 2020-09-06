package com.imd.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.User;
import com.imd.loader.UserLoader;
import com.imd.services.bean.FoodInventoryItemBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;
import javax.ws.rs.core.Response;

class InventorySrvcTest {

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
	void testService() {
		int loggingMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			FoodInventoryItemBean searchBean = new FoodInventoryItemBean();
			searchBean.setStockTrackingId("-999FEED-VANDA-2020-05-10-16:36:14");

			UserLoader userLoader = new UserLoader();
			User user = userLoader.authenticateUser("IMD", "KASHIF", userLoader.encryptPassword("DUMMY"));
			assertTrue(user != null);
			assertTrue(user.getPassword() != null);
			
			searchBean.setLoginToken(user.getPassword());

			
			InventorySrvc invSrvc = new InventorySrvc();
			Response resp = invSrvc.retrieveFoodItemInventory(searchBean);
			assertEquals(Util.HTTPCodes.OK,resp.getStatus());
			String searchResults = resp.getEntity().toString();
			assertEquals("[]",searchResults);
		
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception in InventorySrvcTest test method");
		} finally {
			IMDLogger.loggingMode = loggingMode;
		}
		
		

	}

}
