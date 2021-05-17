package sot.service;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import sot.service.resources.CustomApplicationConfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Publisher {

    private static final URI BASE_URI = URI.create("https://localhost:9090/shop");

    private static File getFile(String fileName) throws URISyntaxException {
        ClassLoader classLoader = Publisher.class.getClassLoader();
        URL url = classLoader.getResource(fileName);
        if (url == null) {
            throw new RuntimeException("Cannot open file " + fileName);
        }
        return new File(url.toURI());
    }

    public static void main(String[] args) {
        try {
            final File keystoreFile  = getFile("keystore_service");
            final File truststoreFile  = getFile("truststore_service");

            // Grizzly ssl configuration
            SSLContextConfigurator sslContext = new SSLContextConfigurator();

            // set up security context
            sslContext.setKeyStoreFile(keystoreFile.getAbsolutePath()); // contains server keypair
            sslContext.setKeyStorePass("asdfgh");

            sslContext.setTrustStoreFile(truststoreFile.getAbsolutePath()); // contains client key
            sslContext.setTrustStorePass("asdfgh");

            sslContext.setKeyPass("asdfgh");

            SSLEngineConfigurator sslEngineConfigurator =
                    new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(true);

            CustomApplicationConfig config = new CustomApplicationConfig();
            HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI,config,true,sslEngineConfigurator);
            System.out.println("Hosting resources at " + BASE_URI.toURL());
        } catch (Exception ex) {
            Logger.getLogger(Publisher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
