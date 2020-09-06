package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.User;
import com.imd.services.bean.UserBean;
import com.imd.util.IMDLogger;
import com.imd.util.IMDProperties;
import com.imd.util.Util;

class UserLoaderTest {

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
	void testUpdateUserProfile() {
		int oldMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			UserLoader loader = new UserLoader();
			
			User newUser = new User("-999");
			newUser.setOrgId("IMD");
			newUser.setPassword(loader.encryptPassword("PASSWORD"));
			newUser.setActive(true);
			newUser.setPersonId("PERSON_ID");
			newUser.setPreferredLanguage(Util.LanguageCode.URD);
			newUser.setPreferredCurrency(Util.CurrencyCode.PKR);
			newUser.setCreatedBy(new User("-999"));
			newUser.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			newUser.setUpdatedBy(newUser.getCreatedBy());
			newUser.setUpdatedDTTM(newUser.getCreatedDTTM());
			
			assertTrue(loader.deleteUser(newUser.getOrgId(), newUser.getUserId())>=0);

			assertEquals(1,loader.insertUser(newUser));
			User addedUser = loader.retrieveUser(newUser.getOrgId(), newUser.getUserId());
			assertTrue(addedUser != null);
			assertEquals(addedUser.getUserId(),newUser.getUserId());
			assertEquals(addedUser.getPassword(),newUser.getPassword());
			assertEquals(addedUser.isActive(),newUser.isActive());
			assertEquals(addedUser.getPreferredLanguage(),newUser.getPreferredLanguage());
			assertEquals(addedUser.getPreferredCurrency(),newUser.getPreferredCurrency());
			assertEquals(Util.getDateInSQLFormat(addedUser.getCreatedDTTM()),Util.getDateInSQLFormat(newUser.getCreatedDTTM()));
			
			UserBean updateUserBean = new UserBean();
			updateUserBean.setOrgId(newUser.getOrgId());
			updateUserBean.setUserId(newUser.getUserId());
			updateUserBean.setUserId(newUser.getUserId());
			updateUserBean.setPreferredCurrency(Util.CurrencyCode.USD);
			updateUserBean.setPreferredLanguage(Util.LanguageCode.ENG);
			updateUserBean.setUserPreference1("userPreference1");
			updateUserBean.setUserPreference2("userPreference2");
			updateUserBean.setUserPreference3("userPreference3");
			updateUserBean.setUserPreference4("userPreference4");
			updateUserBean.setUserPreference5("userPreference5");
			Thread.sleep(1000);// So that the updated DTTM can be at least a second after the original insertion.
			assertEquals(1,loader.updateUserProfile(updateUserBean));
			
			User updatedUser = loader.retrieveUser(newUser.getOrgId(), newUser.getUserId());
			assertTrue(updatedUser != null);
			assertEquals(updateUserBean.getUserId(),updatedUser.getUserId());
			assertEquals(addedUser.getPassword(),updatedUser.getPassword());
			assertEquals(addedUser.isActive(),updatedUser.isActive());
			assertEquals(updateUserBean.getPreferredLanguage(),updatedUser.getPreferredLanguage());
			assertEquals(updateUserBean.getPreferredCurrency(),updatedUser.getPreferredCurrency());
			assertEquals(updateUserBean.getUserPreference1(),updatedUser.getPreference1());
			assertEquals(updateUserBean.getUserPreference2(),updatedUser.getPreference2());
			assertEquals(updateUserBean.getUserPreference3(),updatedUser.getPreference3());
			assertEquals(updateUserBean.getUserPreference4(),updatedUser.getPreference4());
			assertEquals(updateUserBean.getUserPreference5(),updatedUser.getPreference5());
			

			assertTrue(addedUser.getUpdatedDTTM().isBefore(updatedUser.getUpdatedDTTM()), addedUser.getUpdatedDTTM() + " should be before " + updatedUser.getUpdatedDTTM());
			assertTrue(addedUser.getCreatedDTTM().isEqual(updatedUser.getCreatedDTTM()));

			
			assertEquals(1,loader.deleteUser(newUser.getOrgId(), newUser.getUserId()));
			
		} catch (Exception ex) {
			fail("Exception " + ex.getMessage());
		} finally {
			IMDLogger.loggingMode = oldMode;
		}
	}

	
	@Test
	void testRetrieveUser() {
		int oldMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			UserLoader loader = new UserLoader();
			
			User newUser = new User("-999");
			newUser.setOrgId("IMD");
			newUser.setPassword(loader.encryptPassword("PASSWORD"));
			newUser.setActive(true);
			newUser.setPersonId("PERSON_ID");
			newUser.setPreferredLanguage(Util.LanguageCode.URD);
			newUser.setPreferredCurrency(Util.CurrencyCode.PKR);
			newUser.setCreatedBy(new User("-998"));
			newUser.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			newUser.setUpdatedBy(newUser.getCreatedBy());
			newUser.setUpdatedDTTM(newUser.getCreatedDTTM());
			
			assertTrue(loader.deleteUser(newUser.getOrgId(), newUser.getUserId())>=0);
			assertEquals(null,loader.retrieveUser(newUser.getOrgId(), newUser.getUserId()));

			assertEquals(1,loader.insertUser(newUser));
			User addedUser = loader.retrieveUser(newUser.getOrgId(), newUser.getUserId());
			assertTrue(addedUser != null);
			assertEquals(addedUser.getUserId(),newUser.getUserId());
			assertEquals(addedUser.getPassword(),newUser.getPassword());
			assertEquals(addedUser.isActive(),newUser.isActive());
			assertEquals(addedUser.getPreferredLanguage(),newUser.getPreferredLanguage());
			assertEquals(addedUser.getPreferredCurrency(),newUser.getPreferredCurrency());
			assertEquals(Util.getDateInSQLFormat(addedUser.getCreatedDTTM()),Util.getDateInSQLFormat(newUser.getCreatedDTTM()));
			
			assertEquals(1,loader.deleteUser(newUser.getOrgId(), newUser.getUserId()));
			
		} catch (Exception ex) {
			fail("Exception " + ex.getMessage());
		} finally {
			IMDLogger.loggingMode = oldMode;
		}
	}
	@Test
	void testAuthenticateUser() {
		int oldMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			UserLoader loader = new UserLoader();
			
			User newUser = new User("-999");
			newUser.setOrgId("IMD");
			newUser.setPassword(loader.encryptPassword("PASSWORD"));
			newUser.setActive(true);
			newUser.setPersonId("PERSON_ID");
			newUser.setPreferredLanguage(Util.LanguageCode.URD);
			newUser.setPreferredCurrency(Util.CurrencyCode.PKR);
			newUser.setCreatedBy(new User("-998"));
			newUser.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			newUser.setUpdatedBy(newUser.getCreatedBy());
			newUser.setUpdatedDTTM(newUser.getCreatedDTTM());
			
			int cacheSize = loader.getSessionCache().size();
			
			assertTrue(loader.deleteUser(newUser.getOrgId(), newUser.getUserId())>=0);
			assertEquals(null,loader.authenticateUser(newUser.getOrgId(), newUser.getUserId(),newUser.getPassword()));

			assertEquals(1,loader.insertUser(newUser));
			IMDLogger.loggingMode = Util.INFO;

			assertEquals(null,loader.authenticateUser(newUser.getOrgId(), newUser.getUserId(),loader.encryptPassword("incorrectPassword")));
			User authenticatedUser = loader.authenticateUser(newUser.getOrgId(), newUser.getUserId(),newUser.getPassword());
			assertTrue(authenticatedUser !=  null);
			assertTrue(authenticatedUser.getPassword()!=null);
			assertEquals(cacheSize + 1, loader.getSessionCache().size());
			assertEquals(authenticatedUser.getUserId(), loader.isUserAuthenticated(authenticatedUser.getPassword(),false).getUserId(),authenticatedUser.toString());
			
			// simulate multiple logins by the same user
			authenticatedUser = loader.authenticateUser(newUser.getOrgId(), newUser.getUserId(),newUser.getPassword());
			assertTrue(authenticatedUser !=  null);
			assertEquals(cacheSize + 1, loader.getSessionCache().size());						
			
			assertEquals(1,loader.logoutUser(authenticatedUser.getPassword()));
			assertEquals(null,loader.isUserAuthenticated(authenticatedUser.getPassword(),false));			
			
			assertEquals(1,loader.deleteUser(newUser.getOrgId(), newUser.getUserId()));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception " + ex.getMessage());
		} finally {
			IMDLogger.loggingMode = oldMode;
		}
	}
	@Test
	void testTokenRenewal() {
		int oldMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			UserLoader loader = new UserLoader();
			Integer inactivityLimitInMinutes = (Integer)Util.getConfigurations().getOrganizationConfigurationValue(Util.ConfigKeys.TOKEN_EXPIRY_MINUTES);
			Util.getConfigurations().setOrganizationConfigurationValue(Util.ConfigKeys.TOKEN_EXPIRY_MINUTES, (int)(1));
			
			User newUser = new User("-999");
			newUser.setOrgId("IMD");
			newUser.setPassword(loader.encryptPassword("PASSWORD"));
			newUser.setActive(true);
			newUser.setPersonId("PERSON_ID");
			newUser.setPreferredLanguage(Util.LanguageCode.URD);
			newUser.setPreferredCurrency(Util.CurrencyCode.PKR);
			newUser.setCreatedBy(new User("-998"));
			newUser.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			newUser.setUpdatedBy(newUser.getCreatedBy());
			newUser.setUpdatedDTTM(newUser.getCreatedDTTM());
			
			int cacheSize = loader.getSessionCache().size();
			
			assertTrue(loader.deleteUser(newUser.getOrgId(), newUser.getUserId())>=0);
			assertEquals(null,loader.authenticateUser(newUser.getOrgId(), newUser.getUserId(),newUser.getPassword()));

			assertEquals(1,loader.insertUser(newUser));
			IMDLogger.loggingMode = Util.INFO;
			
			

			assertEquals(null,loader.authenticateUser(newUser.getOrgId(), newUser.getUserId(),loader.encryptPassword("incorrectPassword")));
			User authenticatedUser = loader.authenticateUser(newUser.getOrgId(), newUser.getUserId(),newUser.getPassword());
			assertTrue(authenticatedUser !=  null);
			assertTrue(authenticatedUser.getPassword()!=null);
			assertEquals(cacheSize + 1, loader.getSessionCache().size());
			assertEquals(authenticatedUser.getUserId(), loader.isUserAuthenticated(authenticatedUser.getPassword(),true).getUserId(),authenticatedUser.toString());
			
			Thread.sleep(1000 /*1 second sleep*/);
			assertEquals(authenticatedUser.getUserId(), loader.isUserAuthenticated(authenticatedUser.getPassword(),true).getUserId(),authenticatedUser.toString());
			Thread.sleep(2000 /* 2 seconds sleep*/);
			assertEquals(authenticatedUser.getUserId(), loader.isUserAuthenticated(authenticatedUser.getPassword(),true).getUserId(),authenticatedUser.toString());

			// disabled to avoid having to wait for 1 minute for this unit test to finish. Life is too fast to be waiting for 1 min :)
//			Thread.sleep(70 * 1000 /* sleep for 1 minute 10 seconds*/);
//			assertEquals(null, loader.isUserAuthenticated(authenticatedUser.getPassword(),true));

			// simulate multiple logins by the same user
			authenticatedUser = loader.authenticateUser(newUser.getOrgId(), newUser.getUserId(),newUser.getPassword());
			assertTrue(authenticatedUser !=  null);
			assertEquals(cacheSize + 1, loader.getSessionCache().size());						
			
			assertEquals(1,loader.logoutUser(authenticatedUser.getPassword()));
			assertEquals(null,loader.isUserAuthenticated(authenticatedUser.getPassword(),true));			

			assertEquals(1,loader.deleteUser(newUser.getOrgId(), newUser.getUserId()));
			Util.getConfigurations().setOrganizationConfigurationValue(Util.ConfigKeys.TOKEN_EXPIRY_MINUTES, inactivityLimitInMinutes);
						
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception " + ex.getMessage());
		} finally {
			IMDLogger.loggingMode = oldMode;
		}
	}


}








