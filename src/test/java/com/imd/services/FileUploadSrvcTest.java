package com.imd.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.User;
import com.imd.loader.UserLoader;
import com.imd.services.bean.AnimalBean;
import com.imd.util.Util;

class FileUploadSrvcTest {

	private static final String SERVICE_URI = "http://localhost:8080/imd-farm-management/fileupload/uploadphoto";

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
	void testUpload() {
		 final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
		 
		 	File uploadedFile = new File("/Users/kashif.manzoor/Downloads/4.png");
		 	assertTrue(uploadedFile.exists());
		    final FileDataBodyPart filePart = new FileDataBodyPart("file", uploadedFile);
		    FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
		    final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.field("foo", "bar").field("animalTag", "017").bodyPart(filePart);
		    try {
				
		    	AnimalBean searchBean = new AnimalBean();
		    	searchBean.setAnimalTag("017");
				UserLoader userLoader = new UserLoader();
				User user = userLoader.authenticateUser("IMD", "KASHIF", userLoader.encryptPassword("DUMMY"));
				assertTrue(user != null);
				assertTrue(user.getPassword() != null);
				searchBean.setLoginToken(user.getPassword());
				
				multipart.field("loginToken", searchBean.getLoginToken());
				
		    	final WebTarget target = client.target(SERVICE_URI);
		    	final Response response = target.request().post(Entity.entity(multipart, multipart.getMediaType()));
				formDataMultiPart.close();
				multipart.close();
				assertEquals(Util.HTTPCodes.OK ,response.getStatus(), response.toString() + " " + response.getEntity().toString());
				
				FileUploadSrvc srvc = new FileUploadSrvc();

				Response resp = srvc.getAnimalPhotos(searchBean);
				assertEquals(Util.HTTPCodes.OK ,resp.getStatus(), resp.toString() + " " + resp.getEntity().toString());
				
			} catch (IOException e) {
				e.printStackTrace();
				fail("Exception ");
			}
	}

}
