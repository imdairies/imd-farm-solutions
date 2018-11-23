package com.imd.services;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.imd.util.IMDProperties;
import com.imd.util.Util;

import java.io.IOException;
import java.net.URI;

/**
 * Main class. 
 *
 */
public class ServiceController {
    // Base URI the Grizzly HTTP server will listen on

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.imd.services package
        final ResourceConfig rc = new ResourceConfig().packages("com.imd.services");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(IMDProperties.getProperty(Util.PROPERTIES.IMD_SERVICES_URL)), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", IMDProperties.getProperty(Util.PROPERTIES.IMD_SERVICES_URL)));
        System.in.read();
        server.stop();
    }
}
