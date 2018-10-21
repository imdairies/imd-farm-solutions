/**
 * 
 */
package com.imd.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.util.IMDException;
import com.imd.util.MessageManager;

/**
 * @author kashif.manzoor
 *
 */
class MessageManagerTest {
	
	static long englishFileSize= 9999999999l;
	static long urduFileSize= 9999999999l;
	static String testCodeEN1 = "@TEST001";
	static String testMessageEN1 = "This is a test message";
	static String testMessageUR1 = "\\u06CC\\u06C1 \\u0679\\u06CC\\u0633\\u0679 \\u067E\\u06CC\\u063A\\u0627\\u0645 \\u06C1\\u06D2";	
	static String testCodeEN2 = "@TEST002";
	static String testMessageEN2 = "This is second test message";
	static String testMessageUR2 ="\\u06CC\\u06C1 \\u062F\\u0648\\u0633\\u0631\\u0627 \\u0679\\u06CC\\u0633\\u0679 \\u067E\\u06CC\\u063A\\u0627\\u0645 \\u06C1\\u06D2";
	static String testMessageUR1C = "یہ ٹیسٹ پیغام ہے";
	static String testMessageUR2C = "یہ دوسرا ٹیسٹ پیغام ہے";
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		String rootPathEN = Thread.currentThread().getContextClassLoader().getResource("").getPath() + ".."+File.separatorChar + "resources" + File.separatorChar + "IMDMessages_EN.properties";
		String rootPathUR = Thread.currentThread().getContextClassLoader().getResource("").getPath() + ".."+File.separatorChar + "resources" + File.separatorChar + "IMDMessages_UR.properties";
		try {
			englishFileSize = Files.size(Paths.get(rootPathEN));
		    Files.write(Paths.get(rootPathEN), (System.lineSeparator() + testCodeEN1 + "=" + testMessageEN1 + System.lineSeparator() + testCodeEN2 + "=" + testMessageEN2).getBytes(), StandardOpenOption.APPEND);
			
		    urduFileSize = Files.size(Paths.get(rootPathUR));
		    Files.write(Paths.get(rootPathUR), (System.lineSeparator() + testCodeEN1 + "=" + testMessageUR1 + System.lineSeparator() + testCodeEN2 + "=" + testMessageUR2).getBytes(), StandardOpenOption.APPEND);
		}catch (IOException e) {
		    //exception handling left as an exercise for the reader
			e.printStackTrace();
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	static void tearDownAfterClass() throws Exception {
		
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath()+ ".."+File.separatorChar + "resources" + File.separatorChar + "IMDMessages_EN.properties";
		FileChannel outChan = new FileOutputStream (new File(rootPath), true).getChannel();
		outChan.truncate(englishFileSize);
		outChan.close();
		rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath()+ ".."+File.separatorChar + "resources" + File.separatorChar + "IMDMessages_UR.properties";
		outChan = new FileOutputStream (new File(rootPath), true).getChannel();
		outChan.truncate(urduFileSize);
		outChan.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void loadEnglishMessageFile() {
			String langCode = "EN";
			String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath()+".."+File.separatorChar + "resources" + File.separatorChar + "IMDMessages_" + langCode + ".properties";
			Properties appProps = new Properties();
			try {
				appProps.load(new FileInputStream(rootPath));
				MessageManager.loadMessages(langCode, appProps);

				String strMessage = MessageManager.getMessage(langCode, testCodeEN1);
				assertNotNull(strMessage);
				assertTrue(strMessage.equalsIgnoreCase(testMessageEN1), "The expected message for the code [" + testCodeEN1 + "] was {" + testMessageEN1 + "} but we got {" +strMessage + "}");	
				
				strMessage = MessageManager.getMessage(langCode, testCodeEN2);
				assertNotNull(strMessage);
				assertTrue(strMessage.equalsIgnoreCase(testMessageEN2), "The expected message for the code [" + testCodeEN2 + "] was {" + testMessageEN2 + "} but we got {" +strMessage + "}");	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(langCode + " message file could not be loaded");
			} catch (IMDException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.getMessage());
			}
	}

	@Test
	void loadMultipleMessageFiles() {
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String rootPathUR = rootPath + ".."+File.separatorChar + "resources" + File.separatorChar + "IMDMessages_UR.properties";
		String rootPathEN = rootPath + ".."+File.separatorChar + "resources" + File.separatorChar + "IMDMessages_EN.properties";
		Properties appPropsUR = new Properties();
		Properties appPropsEN = new Properties();
		try {
			appPropsUR.load(new FileInputStream(rootPathUR));
			MessageManager.loadMessages("UR", appPropsUR);
			
			appPropsEN.load(new FileInputStream(rootPathEN));
			MessageManager.loadMessages("EN", appPropsEN);
			
			String strMessage = MessageManager.getMessage("EN", testCodeEN1);
			assertNotNull(strMessage);
			assertTrue(strMessage.equalsIgnoreCase(testMessageEN1), "The expected message for the code [" + testCodeEN1 + "] was {" + testMessageEN1 + "} but we got {" +strMessage + "}");	
			strMessage = MessageManager.getMessage("EN", testCodeEN2);
			assertNotNull(strMessage);
			assertTrue(strMessage.equalsIgnoreCase(testMessageEN2), "The expected message for the code [" + testCodeEN2 + "] was {" + testMessageEN2 + "} but we got {" +strMessage + "}");	

			
			strMessage = MessageManager.getMessage("UR", testCodeEN1);
			assertNotNull(strMessage);
			assertTrue(strMessage.equalsIgnoreCase(testMessageUR1C), "The expected message for the code [" + testCodeEN1 + "] was {" + testMessageUR1C + "} but we got {" +strMessage + "}");	
			strMessage = MessageManager.getMessage("UR", testCodeEN2);
			assertNotNull(strMessage);
			assertTrue(strMessage.equalsIgnoreCase(testMessageUR2C), "The expected message for the code [" + testCodeEN2 + "] was {" + testMessageUR2C + "} but we got {" +strMessage + "}");	

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("One or more language code files could not be loaded.");
		} catch (IMDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void loadUrduMessageFile() {
		
			String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + ".." + File.separatorChar + "resources" + File.separatorChar + "IMDMessages_UR.properties";
			Properties appProps = new Properties();
			try {
				appProps.load(new FileInputStream(rootPath));
				MessageManager.loadMessages("UR", appProps);
				String strMessage = MessageManager.getMessage("UR", testCodeEN1);
				assertNotNull(strMessage);
				assertTrue(strMessage.equalsIgnoreCase(testMessageUR1C), "The expected message for the code [" + testCodeEN1 + "] was {" + testMessageUR1C + "} but we got {" +strMessage + "}");	
			
				strMessage = MessageManager.getMessage("UR", testCodeEN2);
				assertNotNull(strMessage);
				assertTrue(strMessage.equalsIgnoreCase(testMessageUR2C), "The expected message for the code [" + testCodeEN2 + "] was {" + testMessageUR2C + "} but we got {" +strMessage + "}");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail("UR message file could not be loaded");
			} catch (IMDException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.getMessage());
			}
	}
	
	@Test
	void loadNonExistentLanguageMessageFile() {
			String langCode = "UR11";
			String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
			rootPath += ".."+File.separatorChar + "resources" + File.separatorChar + "IMDMessages_" + langCode + ".properties";
			Properties appProps = new Properties();
			try {
				appProps.load(new FileInputStream(rootPath));
				fail("The non-existent file should not have been loaded");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				assertTrue(true, "Non-existent file could not be loaded, rightly so !");
			}
	}
	
	@Test
	void loadNonExistentMessageCodes() {
			String langCode = "EN";
			String messageCode = "-----";
			String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
			rootPath += ".."+File.separatorChar + "resources" + File.separatorChar + "IMDMessages_" + langCode + ".properties";
			Properties appProps = new Properties();
			try {
				appProps.load(new FileInputStream(rootPath));
				MessageManager.loadMessages(langCode, appProps);
				MessageManager.getMessage(langCode, messageCode);
				fail("Non-existent message code should not have been read");
			} catch (IOException e) {
				e.printStackTrace();
				fail(langCode + " message file could not be loaded");
			} catch (IMDException e) {
				// this exception should be thrown as the message code does not exist. If the control comes to this block then the unit test passes.
			}
	}
}
