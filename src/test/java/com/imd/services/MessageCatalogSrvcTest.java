package com.imd.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.ws.rs.core.Response;

import com.imd.dto.User;
import com.imd.loader.MessageCatalogLoader;
import com.imd.loader.UserLoader;
import com.imd.services.bean.MessageBean;
import com.imd.util.Util;

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
	void testRetrieveAllMessages() {
		UserLoader userLoader = new UserLoader();
		User user = userLoader.authenticateUser("IMD", "KASHIF", userLoader.encryptPassword("DUMMY"));
		MessageCatalogSrvc srvc = new MessageCatalogSrvc();
		MessageBean messageBean = new MessageBean();
		messageBean.setOrgId("IMD");
		messageBean.setUserId(user.getUserId());
		assertTrue(user != null);
		assertTrue(user.getPassword() != null);
		messageBean.setLoginToken(user.getPassword());
		Response resp = srvc.searchMessageCatalog(messageBean);
		assertEquals(Util.HTTPCodes.OK,resp.getStatus());
		messageBean.setLanguageCD(Util.LanguageCode.URD);
		resp = srvc.searchMessageCatalog(messageBean);
		assertEquals(Util.HTTPCodes.OK,resp.getStatus());
		messageBean.setMessageCD("99999");
		resp = srvc.searchMessageCatalog(messageBean);
		assertEquals(Util.HTTPCodes.OK,resp.getStatus());
	}
	
	@Test
	void testInsertNewMessage() {
		String testCode = "-999999";
		String messageText = "Dummy Message to be deleted once testing is over";
		String messageCategoryCD = "TEST";
		UserLoader userLoader = new UserLoader();
		MessageCatalogLoader loader = new MessageCatalogLoader();

		User user = userLoader.authenticateUser("IMD", "KASHIF", userLoader.encryptPassword("DUMMY"));
		MessageCatalogSrvc srvc = new MessageCatalogSrvc();
		MessageBean messageBean = new MessageBean();
		messageBean.setOrgId("IMD");
		messageBean.setUserId(user.getUserId());
		assertTrue(user != null);
		assertTrue(user.getPassword() != null);
		messageBean.setLoginToken(user.getPassword());
		
		
		Response insertResp = srvc.addToMessageCatalog(messageBean);
		assertEquals(Util.HTTPCodes.BAD_REQUEST,insertResp.getStatus());
		
		messageBean.setLanguageCD(Util.LanguageCode.ENG);
		messageBean.setMessageCD(testCode);
		
		assertTrue(loader.deleteMessage(messageBean) >= 0);

		Response updateResp = srvc.updateMessageCatalog(messageBean);
		assertEquals(Util.HTTPCodes.BAD_REQUEST,updateResp.getStatus());
		
		messageBean.setMessageText(messageText);

		insertResp = srvc.addToMessageCatalog(messageBean);
		assertEquals(Util.HTTPCodes.OK,insertResp.getStatus());
		
		messageBean.setMessageText(null);
		Response searchResp = srvc.searchMessageCatalog(messageBean);
		assertEquals(Util.HTTPCodes.OK,searchResp.getStatus());
		
		messageBean.setMessageCategoryCD(messageCategoryCD);

		messageBean.setMessageText(messageText);
		updateResp = srvc.updateMessageCatalog(messageBean);
		assertEquals(Util.HTTPCodes.OK,updateResp.getStatus());
		
		assertEquals(1,loader.deleteMessage(messageBean));
		
	}
}
