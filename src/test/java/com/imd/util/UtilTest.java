package com.imd.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.services.bean.FarmMilkingDetailBean;
import com.imd.services.bean.InputDelimitedFileBean;

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
	void testDecimalFormatting() {
		assertEquals("100.1", Util.formatToSpecifiedDecimalPlaces(100.12f, 1));
		assertEquals("100.12", Util.formatTwoDecimalPlaces(100.123f));
		assertEquals("100.13", Util.formatToSpecifiedDecimalPlaces(100.129f, 2));
	}
	@Test
	void testParseFarmMilkingDetailBean() {
		String inputCSVFile = "2019-01-01" + Util.FILE_RECORD_SEPARATOR + "4:00" + Util.FILE_RECORD_SEPARATOR + "1" + "\n" /*Timestamp, event number*/+ 
				"32.3"  + Util.FILE_RECORD_SEPARATOR + "\n" + /*Temp,Humidity*/
				 Util.FILE_RECORD_SEPARATOR + Util.FILE_RECORD_SEPARATOR ; /*Fat,LR,Aflatoxin*/;
		InputDelimitedFileBean fileBean = new InputDelimitedFileBean();
		fileBean.setInputDelimitedFileContents(inputCSVFile);
		FarmMilkingDetailBean farmBean;
		try {

			farmBean = Util.parseFarmMilkingDetailBean(fileBean);
			IMDLogger.log(farmBean.toString(), Util.INFO);
			assertEquals(null,farmBean.getHumidity());
			assertEquals(null,farmBean.getFatValue());
			assertEquals(null,farmBean.getLrValue());
			assertEquals(null,farmBean.getToxinValue());
			assertEquals(32.3f,farmBean.getTemperatureInCentigrade().floatValue());
			assertEquals("2019-01-01",Util.getDateInSQLFormat(farmBean.getRecordDate()));
			assertEquals("2019-01-01",farmBean.getMilkingDateStr());
			assertEquals("04:00",Util.getTimeInSQLFormart(farmBean.getRecordTime()));
			assertEquals(1,farmBean.getMilkingEventNumber());
			assertEquals(null,farmBean.getFarmMilkingEventRecords());
			
			
			inputCSVFile = "2019-02-02" + Util.FILE_RECORD_SEPARATOR + "12:00"  + Util.FILE_RECORD_SEPARATOR + "2" + "\n" /*Timestamp, event number*/+ 
					"16" + Util.FILE_RECORD_SEPARATOR +"5" + "\n" + /*Temp,Humidity*/
					"3.95" + Util.FILE_RECORD_SEPARATOR + "28.5  " + "\n" + /*3.95=Fat,28.5=LR,null=Aflatoxin*/
					"6" /*forCalvesUse*/ + Util.FILE_RECORD_SEPARATOR +
					"2.5" /*forFarmUse*/ + Util.FILE_RECORD_SEPARATOR +
					"3" /*forFamilyUse*/ + Util.FILE_RECORD_SEPARATOR +
					"0" /*forPersonalUse*/ + Util.FILE_RECORD_SEPARATOR +
					/*forOtherUse*/ Util.FILE_RECORD_SEPARATOR +
					"6" /*forWasteAdj*/ + "\n" +
					"012" + Util.FILE_RECORD_SEPARATOR + "11.5" + "\n" + /*012=Tag number, 11.5=volume, null=comment" */
					"014" + Util.FILE_RECORD_SEPARATOR + "10 " + "\n" + 
					"015" + Util.FILE_RECORD_SEPARATOR + "13.5" + Util.FILE_RECORD_SEPARATOR + "Test\\ Comments";
			fileBean.setInputDelimitedFileContents(inputCSVFile);
			farmBean = Util.parseFarmMilkingDetailBean(fileBean);
			IMDLogger.log(farmBean.toString(), Util.INFO);

			assertEquals(5f,farmBean.getHumidity().floatValue());
			assertEquals(3.95f,farmBean.getFatValue().floatValue());
			assertEquals(28.5,farmBean.getLrValue().floatValue());
			assertEquals(null,farmBean.getToxinValue());
			assertEquals(16f,farmBean.getTemperatureInCentigrade().floatValue());
			assertEquals(6.0f,farmBean.getForCalvesUse().floatValue());
			assertEquals(2.5f,farmBean.getForFarmUse().floatValue());
			assertEquals(3.0f,farmBean.getForFamilyUse().floatValue());
			assertEquals(0.0f,farmBean.getForPersonalUse().floatValue());
			assertEquals(null,farmBean.getForOtherUse());
			assertEquals(6.0f,farmBean.getForWasteAdj().floatValue());
			assertEquals("2019-02-02",Util.getDateInSQLFormat(farmBean.getRecordDate()));
			assertEquals("2019-02-02",farmBean.getMilkingDateStr());
			assertEquals("12:00",Util.getTimeInSQLFormart(farmBean.getRecordTime()));
			assertEquals(2,farmBean.getMilkingEventNumber());
			assertEquals(3,farmBean.getFarmMilkingEventRecords().size());
			assertEquals("012",farmBean.getFarmMilkingEventRecords().get(0).getTag());
			assertEquals("014",farmBean.getFarmMilkingEventRecords().get(1).getTag());
			assertEquals("015",farmBean.getFarmMilkingEventRecords().get(2).getTag());
			assertEquals("11.5",farmBean.getFarmMilkingEventRecords().get(0).getVolume());
			assertEquals("10.0",farmBean.getFarmMilkingEventRecords().get(1).getVolume());
			assertEquals("13.5",farmBean.getFarmMilkingEventRecords().get(2).getVolume());
			
			assertEquals(null,farmBean.getFarmMilkingEventRecords().get(0).getComments());
			assertEquals(null,farmBean.getFarmMilkingEventRecords().get(1).getComments());
			assertEquals("Test Comments",farmBean.getFarmMilkingEventRecords().get(2).getComments());
			
			
		} catch (IMDException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	

}


