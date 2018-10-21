package com.imd.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.util.IMDException;
import com.imd.util.Util;

class UtilTest {

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
	void testNullOrEmptyString() {
		try {
			Util.throwExceptionIfNullOrEmpty(null,"Test Parameter");
			fail("IMDException should have been thrown since we passed a null value to the method");
		} catch (IMDException e) {
			assertTrue(e.getMessage().contains("null value"),"Exception message should indicate that the parameter was null");	
			assertTrue(e.getMessage().contains("Test Parameter"),"Exception message should indicate that the parameter was null");	
		}
		try {
			Util.throwExceptionIfNullOrEmpty("","Test Parameter");
			fail("IMDException should have been thrown since we passed an empty value to the method");
		} catch (IMDException e) {
			assertTrue(e.getMessage().contains("empty value"),"Exception message should indicate that the parameter was empty");	
			assertTrue(e.getMessage().contains("Test Parameter"),"Exception message should indicate that the parameter was null");	
		}
	}

}
