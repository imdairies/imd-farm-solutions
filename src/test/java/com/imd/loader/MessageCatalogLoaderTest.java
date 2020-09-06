package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Message;
import com.imd.services.bean.MessageBean;
import com.imd.util.IMDLogger;
import com.imd.util.Util;

class MessageCatalogLoaderTest {

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
	void testMessageRetrieval() {
		int originalLoggingMode = IMDLogger.loggingMode;
		try {
			IMDLogger.loggingMode = Util.INFO;
			MessageCatalogLoader loader = new MessageCatalogLoader();
			MessageBean messageBean = new MessageBean();
			messageBean.setOrgId("TEST");
			messageBean.setLanguageCD(Util.LanguageCode.ENG);
			messageBean.setMessageCD("999999");
			messageBean.setMessageText("This is Test message. Should be deleted");
			messageBean.setUserId("TESTUSR");
			int result = loader.deleteMessage(messageBean);
			assertTrue(result == 0 || result == 1);
			assertTrue(loader.insertMessage(messageBean) == 1);
			List<Message> messages = loader.retrieveMessage(messageBean);
			assertTrue(messages.size() >=1);
			Iterator<Message> msgIt = messages.iterator();
			boolean found = false;
			while (msgIt.hasNext()) {
				Message msg = msgIt.next();
				if (( msg.getOrgId() + msg.getLanguageCD() + msg.getMessageCD() + msg.getMessageText()).equalsIgnoreCase(messageBean.getOrgId()+messageBean.getLanguageCD()+messageBean.getMessageCD()+msg.getMessageText())) {
					found = true;
					break;
				}
			}
			assertTrue(found);
			MessageBean bean = new MessageBean();
			bean.setMessageCD(messageBean.getMessageCD());
			messages = loader.retrieveMessage(bean);
			assertTrue(messages.size() >=1);
			msgIt = messages.iterator();
			found = false;
			Message msg = null;
			while (msgIt.hasNext()) {
				msg = msgIt.next();
				if (( msg.getOrgId() + msg.getLanguageCD() + msg.getMessageCD() + msg.getMessageText()).equalsIgnoreCase(messageBean.getOrgId()+messageBean.getLanguageCD()+messageBean.getMessageCD()+msg.getMessageText())) {
					found = true;
					break;
				}
			}
			assertTrue(found);

			found = false;
			bean.setMessageText("Modified Text");
			bean.setOrgId(messageBean.getOrgId());
			bean.setUserId(messageBean.getUserId());
			bean.setMessageCD(messageBean.getMessageCD());
			bean.setLanguageCD(messageBean.getLanguageCD());
			assertEquals(1,loader.updatedMessage(bean));
			messages = loader.retrieveMessage(bean);
			assertTrue(messages.size() >=1);
			msgIt = messages.iterator();
			found = false;
			Message modifiedMessage = null;
			while (msgIt.hasNext()) {
				modifiedMessage = msgIt.next();
				if (( modifiedMessage.getOrgId() + modifiedMessage.getLanguageCD() + modifiedMessage.getMessageCD() + modifiedMessage.getMessageText()).equalsIgnoreCase(messageBean.getOrgId()+messageBean.getLanguageCD()+messageBean.getMessageCD()+bean.getMessageText())) {
					found = true;
					break;
				}
			}
			assertTrue(found);
			
			MessageBean beanOrg = new MessageBean();
			beanOrg.setOrgId("TEST");
			assertEquals(1,loader.retrieveMessage(beanOrg).size());

			beanOrg.setLanguageCD("ENG");
			assertEquals(1,loader.retrieveMessage(beanOrg).size());

			beanOrg.setLanguageCD(null);
			beanOrg.setMessageCD("999999");
			assertEquals(1,loader.retrieveMessage(beanOrg).size());
			
			beanOrg.setLanguageCD("HAH");
			assertEquals(0,loader.retrieveMessage(beanOrg).size());
			
			assertEquals(1,loader.deleteMessage(messageBean));
			messages = loader.retrieveMessage(new MessageBean());
			msgIt = messages.iterator();
			found = false;
			while (msgIt.hasNext()) {
				msg = msgIt.next();
				if (( msg.getOrgId() + msg.getLanguageCD() + msg.getMessageCD() + msg.getMessageText()).equalsIgnoreCase(messageBean.getOrgId()+messageBean.getLanguageCD()+messageBean.getMessageCD()+msg.getMessageText())) {
					found = true;
					break;
				}
			}
			assertTrue(!found);
			
			Message notFoundMessage = MessageCatalogLoader.getMessage("HAHA", "KOK", "7007");
			assertEquals("HAHA",notFoundMessage.getOrgId());
			assertEquals("KOK",notFoundMessage.getLanguageCD());
			assertEquals("7007",notFoundMessage.getMessageCD());
			assertEquals("The requested message does not exist in the catalog [HAHA-KOK-7007]",notFoundMessage.getMessageText());
			
		} catch (Exception ex) {
			fail("Exception");
		} finally {
			IMDLogger.loggingMode = originalLoggingMode;
			
		}
	}
	
	@Test
	void testImmutability() {
		MessageCatalogLoader ldr = new MessageCatalogLoader();
		Message testMessage =  new Message("IMD",Util.LanguageCode.ENG, "-99999");
		testMessage.setMessageText("This is a Test Message the first parameter is: %1 and the second is :%2");
		String key = testMessage.getOrgId()+"-"+testMessage.getLanguageCD()+"-"+testMessage.getMessageCD();
		ldr.getMessageCache().put(key, testMessage);
		List<String> values = new ArrayList<String>();
		values.add("first");
		values.add("second");
		assertEquals("This is a Test Message the first parameter is: first and the second is :second",
				MessageCatalogLoader.getDynamicallyPopulatedMessage(testMessage.getOrgId(),testMessage.getLanguageCD(),testMessage.getMessageCD(),values).getMessageText());
		values.remove(0);
		values.remove(0);
		values.add("1st");
		values.add("2nd");
		assertEquals("This is a Test Message the first parameter is: 1st and the second is :2nd",
				MessageCatalogLoader.getDynamicallyPopulatedMessage(testMessage.getOrgId(),testMessage.getLanguageCD(),testMessage.getMessageCD(),values).getMessageText());
		MessageBean msgBn = new MessageBean();
		msgBn.setOrgId(testMessage.getOrgId());
		msgBn.setLanguageCD(testMessage.getLanguageCD());
		msgBn.setMessageCD(testMessage.getMessageCD());
		ldr.deleteMessage(msgBn);
		assertEquals(null,ldr.getMessageCache().get(key));
	}

}
