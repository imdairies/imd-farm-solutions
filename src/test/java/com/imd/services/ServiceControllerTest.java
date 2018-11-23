package com.imd.services;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.util.IMDProperties;
import com.imd.util.Util;

class ServiceControllerTest {
    private HttpServer server;
    private WebTarget target;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		IMDProperties.loadProperties();
        // start the server
        server = ServiceController.startServer();
        // create the client
        Client c = ClientBuilder.newClient();

//        c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());

        target = c.target(IMDProperties.getProperty(Util.PROPERTIES.IMD_SERVICES_URL));
		
	}

	@AfterEach
	void tearDown() throws Exception {
        server.stop();
	}

	@Test
	void testServiceController() {
        String responseMsg = target.path("myresource").request().get(String.class);
        assertEquals("Got it!", responseMsg);
	}
	@Test
	void testLVLifecycleEvent() {
        String responseMsg = target.path("/lv-lifecycle-event/ERRORVALUE").request().get(String.class);
        assertEquals("No Record Found", responseMsg);
        responseMsg = target.path("/lv-lifecycle-event/HEAT").request().get(String.class);
        assertTrue(responseMsg.indexOf("\"eventCode\":\"HEAT\"") > 0);
        
	}

}
