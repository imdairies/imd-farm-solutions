package com.imd.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Inventory;
import com.imd.dto.Sire;
import com.imd.dto.User;
import com.imd.services.bean.InventoryBean;
import com.imd.services.bean.SireBean;
import com.imd.util.IMDLogger;
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
			
			IMDLogger.log(inv.dtoToJson("   "), Util.INFO);
			InventoryLoader loader = new InventoryLoader();
			int result = loader.deleteSemenInventory(inv.getOrgID(),inv.getItemSKU(), inv.getItemType());
			assertTrue(result >= 0);
			assertEquals(1,loader.insertSemenInventory(inv));
	
			assertEquals(1,loader.deleteSemenInventory(inv.getOrgID(),inv.getItemSKU(), inv.getItemType()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Inventory processing Failed.");
		}
	}
	
	@Test
	void testInsertSemenUsageInv() {
		try {
			InventoryBean invBean = new InventoryBean();
			invBean.setItemSKU("-999");
			invBean.setItemType("Y");
			invBean.setOrderDttm(DateTime.now());
			invBean.setReceivedDttm(DateTime.now());
			invBean.setInventoryAddDttm(new DateTime(2019,1,1,0,0,0));
			
			assertFalse(invBean.validateValues().isEmpty());
			invBean.setQuantity(1.0f);
			invBean.setAuxValue1("-9999");
			
			Inventory inv = new Inventory(invBean);
			inv.setOrgID("IMD");
			inv.setCreatedBy(new User("KASHIF"));
			inv.setCreatedDTTM(DateTime.now());
			inv.setUpdatedBy(new User("KASHIF"));
			inv.setUpdatedDTTM(DateTime.now());
			
			IMDLogger.log(inv.dtoToJson("   "), Util.INFO);
			InventoryLoader loader = new InventoryLoader();
			int result = loader.deleteSemenInventoryUsage(inv.getOrgID(),inv.getItemSKU(), inv.getInventoryAddDttm());
			assertTrue(result >= 0);
			assertEquals(1,loader.addSemenInventoryUsage(inv));
	
			assertEquals(1,loader.deleteSemenInventoryUsage(inv.getOrgID(),inv.getItemSKU(), inv.getInventoryAddDttm()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Inventory processing Failed.");
		}
	}
	@Test
	void testSemenQtyUpdate() {
		try {			
			AnimalLoader animalLoader = new AnimalLoader();
			InventoryLoader invLoader = new InventoryLoader();
			
			SireBean sireBean = new SireBean();
			sireBean.setOrgID("IMD");
			sireBean.setAnimalTag("-999");
			sireBean.setAlias("TEST SIRE");
			sireBean.setBreed(Util.Breed.HFCROSS);
			sireBean.setController("SEMEX");
			sireBean.setCurrentConventionalListPrice(1000.0f);
			sireBean.setCurrentSexListPrice(10000.0f);
			sireBean.setDiscountConventionalPercentage(25.0f);
			sireBean.setDiscountSexPercentage(25.0f);
			sireBean.setPhotoURL("");
			sireBean.setRecordURL("");
			sireBean.setSemenCompany("DRDF");
			sireBean.setSemenInd("Y");
			User user = new User("KASHIF");
			
			int result = animalLoader.deleteSire(sireBean.getAnimalTag());
			assertTrue(result == 0 || result ==1);
			
			animalLoader.insertSire(sireBean, user.getUserId(), DateTime.now(), user.getUserId(), DateTime.now());
			
			InventoryBean invBean = new InventoryBean();
			invBean.setItemSKU("-999");
			invBean.setItemType("Y");
			invBean.setOrderDttm(DateTime.now());
			invBean.setReceivedDttm(DateTime.now());
			invBean.setInventoryAddDttm(new DateTime(2019,1,1,1,1,1));
			invBean.setPrice(1000.0f);
			invBean.setDiscount(10.0f);
			invBean.setQuantity(5.0f);

			Inventory inv = new Inventory(invBean);
			inv.setOrgID("IMD");
			inv.setCreatedBy(new User("KASHIF"));
			inv.setCreatedDTTM(DateTime.now());
			inv.setUpdatedBy(new User("KASHIF"));
			inv.setUpdatedDTTM(DateTime.now());

			
			assertTrue(invLoader.deleteSemenInventoryUsage(inv.getOrgID(), invBean.getItemSKU(), inv.getInventoryAddDttm()) >= 0);
			assertTrue(invLoader.deleteSemenInventory(inv.getOrgID(), invBean.getItemSKU(), invBean.getItemType()) >= 0);
			
			
			invLoader.insertSemenInventory(inv);
			inv.setQuantity(inv.getQuantity()-2);
			invLoader.addSemenInventoryUsage(inv);
			
			List<Sire> sires = animalLoader.retrieveAISire();
			Iterator<Sire> it = sires.iterator();
			while (it.hasNext()) {
				Sire sire = it.next();
				if (sire.getAnimalTag().equalsIgnoreCase(inv.getItemSKU())) {
					assertEquals(null,invLoader.getRemainingSemenInventory(inv.getOrgID(),"-NOTPRESENT",  inv.getItemType()));
					Inventory invOutput = invLoader.getRemainingSemenInventory(inv.getOrgID(),sire.getAnimalTag(),  inv.getItemType());
					IMDLogger.log(invOutput.dtoToJson("   "), Util.INFO);
					assertTrue(invOutput.getQuantity() == 5.0f);
					assertTrue(Float.parseFloat(invOutput.getAuxValue1()) == 3.0f);
					assertTrue(Float.parseFloat(invOutput.getAuxValue2()) == 2.0f);
					break;
				}
			}
			sires = invLoader.getSiresWithAvailableInventory(inv.getOrgID());
			it = sires.iterator();
			boolean found = false;
			while (it.hasNext()) {
				Sire sire = it.next();
				if (sire.getAnimalTag().equalsIgnoreCase(inv.getItemSKU())) {
					assertTrue(sire.getNote(0).getNoteText().equals("2"));
					assertTrue(sire.getNote(1).getNoteText().equals("Y"));
					found=true;
				}
			}
			assertTrue(found);
			assertEquals(1,invLoader.deleteSemenInventoryUsage(inv.getOrgID(), invBean.getItemSKU(), inv.getInventoryAddDttm()));
			assertEquals(1,invLoader.deleteSemenInventory(inv.getOrgID(), invBean.getItemSKU(), invBean.getItemType()));
			assertEquals(1,animalLoader.deleteSire(sireBean.getAnimalTag()));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Inventory processing Failed.");
		}
	}
	

}








