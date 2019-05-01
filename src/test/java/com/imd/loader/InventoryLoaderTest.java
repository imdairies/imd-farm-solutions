package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Animal;
import com.imd.dto.BankDetails;
import com.imd.dto.Contact;
import com.imd.dto.Dam;
import com.imd.dto.Inventory;
import com.imd.dto.LifecycleEvent;
import com.imd.dto.MilkingDetail;
import com.imd.dto.Note;
import com.imd.dto.Person;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.services.bean.AnimalBean;
import com.imd.services.bean.InventoryBean;
import com.imd.services.bean.SireBean;
import com.imd.util.IMDException;
import com.imd.util.IMDLogger;
import com.imd.util.MessageManager;
import com.imd.util.Util;

class InventoryLoaderTest {

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
	void testInsertSemenInv() {
		try {
			InventoryBean invBean = new InventoryBean();
			invBean.setItemSKU("-999");
			invBean.setItemType("Y");
			invBean.setOrderDttm(DateTime.now());
			invBean.setReceivedDttm(DateTime.now());
			invBean.setInventoryAddDttm(DateTime.now());
			
			assertFalse(invBean.validateValues().isEmpty());
			invBean.setPrice(1000.0f);
			invBean.setDiscount(10.0f);
			invBean.setQuantity(5.0f);
			
			Inventory inv = new Inventory(invBean);
			inv.setOrgID("IMD");
			inv.setCreatedBy(new User("KASHIF"));
			inv.setCreatedDTTM(DateTime.now());
			inv.setUpdatedBy(new User("KASHIF"));
			inv.setUpdatedDTTM(DateTime.now());
			
			InventoryLoader loader = new InventoryLoader();
			int result = loader.deleteSemenInventory(inv.getOrgID(),inv.getItemSKU(), inv.getItemType());
			assertTrue(result >= 0);
			assertEquals(1,loader.insertSemenInventory(inv));
	
			assertEquals(1,loader.deleteSemenInventory(inv.getOrgID(),inv.getItemSKU(), inv.getItemType()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Animal Creation and/or insertion Failed.");
		}
	}
	
	
	

}
