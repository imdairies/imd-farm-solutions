package com.imd.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.User;
import com.imd.loader.UserLoader;
import com.imd.services.bean.MessageBean;
import com.imd.util.Util;
import com.sun.research.ws.wadl.Response;

class MessageCatalogSrvcTest {

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
	void testBadRequest() {
		UserLoader userLoader = new UserLoader();
		User user = userLoader.authenticateUser("IMD", "KASHIF", userLoader.encryptPassword("DUMMY"));
		MessageCatalogSrvc srvc = new MessageCatalogSrvc();
		MessageBean messageBean = new MessageBean();
		messageBean.setOrgId("IMD");
		messageBean.setUserId(user.getUserId());
		assertTrue(user != null);
		assertTrue(user.getPassword() != null);
		messageBean.setLoginToken(user.getPassword());
		javax.ws.rs.core.Response resp = srvc.searchMessageCatalog(messageBean);
		assertEquals(Util.HTTPCodes.OK,resp.getStatus());
		messageBean.setLanguageCD(Util.LanguageCode.URD);
		resp = srvc.searchMessageCatalog(messageBean);
		assertEquals(Util.HTTPCodes.OK,resp.getStatus());
		messageBean.setMessageCD("99999");
		resp = srvc.searchMessageCatalog(messageBean);
		assertEquals(Util.HTTPCodes.OK,resp.getStatus());
	}

}
