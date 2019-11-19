package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.User;
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
	void testRetrieveUser() {
		int oldMode = IMDLogger.loggingMode;
		IMDLogger.loggingMode = Util.INFO;
		
		try {
			UserLoader loader = new UserLoader();
			
			User newUser = new User("-999");
			newUser.setOrgID("IMD");
			newUser.setPassword("PASSWORD");
			newUser.setActive(true);
			newUser.setPersonId("PERSON_ID");
			newUser.setPreferredLanguage(Util.LanguageCode.URD);
			newUser.setPreferredCurrency(Util.CurrencyCode.PKR);
			newUser.setCreatedBy(new User("-998"));
			newUser.setCreatedDTTM(DateTime.now(IMDProperties.getServerTimeZone()));
			newUser.setUpdatedBy(newUser.getCreatedBy());
			newUser.setUpdatedDTTM(newUser.getCreatedDTTM());
			
			assertTrue(loader.deleteUser(newUser.getOrgID(), newUser.getUserId())>=0);
			assertEquals(null,loader.retrieveUser(newUser.getOrgID(), newUser.getUserId()));

			assertEquals(1,loader.insertUser(newUser));
			User addedUser = loader.retrieveUser(newUser.getOrgID(), newUser.getUserId());
			assertTrue(addedUser != null);
			assertEquals(addedUser.getUserId(),newUser.getUserId());
			assertEquals(addedUser.getPassword(),newUser.getPassword());
			assertEquals(addedUser.isActive(),newUser.isActive());
			assertEquals(addedUser.getPreferredLanguage(),newUser.getPreferredLanguage());
			assertEquals(addedUser.getPreferredCurrency(),newUser.getPreferredCurrency());
			assertEquals(Util.getDateInSQLFormat(addedUser.getCreatedDTTM()),Util.getDateInSQLFormat(newUser.getCreatedDTTM()));
			
			assertEquals(1,loader.deleteUser(newUser.getOrgID(), newUser.getUserId()));
			
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
			newUser.setOrgID("IMD");
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
			
			assertTrue(loader.deleteUser(newUser.getOrgID(), newUser.getUserId())>=0);
			assertEquals(null,loader.authenticateUser(newUser.getOrgID(), newUser.getUserId(),newUser.getPassword()));

			assertEquals(1,loader.insertUser(newUser));
			IMDLogger.loggingMode = Util.INFO;

			assertEquals(null,loader.authenticateUser(newUser.getOrgID(), newUser.getUserId(),loader.encryptPassword("incorrectPassword")));
			User authenticatedUser = loader.authenticateUser(newUser.getOrgID(), newUser.getUserId(),newUser.getPassword());
			assertTrue(authenticatedUser !=  null);
			assertEquals(cacheSize + 1, loader.getSessionCache().size());
			assertEquals(authenticatedUser.getUserId(), loader.isUserAuthenticated(authenticatedUser.getPassword()).getUserId(),authenticatedUser.toString());
			
			// simulate multiple logins by the same user
			authenticatedUser = loader.authenticateUser(newUser.getOrgID(), newUser.getUserId(),newUser.getPassword());
			assertTrue(authenticatedUser !=  null);
			assertEquals(cacheSize + 1, loader.getSessionCache().size());						
			
			assertEquals(1,loader.logoutUser(authenticatedUser.getPassword()));
			assertEquals(null,loader.isUserAuthenticated(authenticatedUser.getPassword()));			
			
			assertEquals(1,loader.deleteUser(newUser.getOrgID(), newUser.getUserId()));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception " + ex.getMessage());
		} finally {
			IMDLogger.loggingMode = oldMode;
		}
	}
}
