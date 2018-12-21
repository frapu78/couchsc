/**
 * Server package for the Couch Solution Center (CouchSC).
 * <p>
 * This class handles the loading of resources from the file system.
 *
 * @author Frank Puhlmann
 */
package net.frapu.couchsc.handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceHandler {

    public static final String RESOURCE_FOLDER = "./resources/";

    /**
     * Returns the (static) content type of a resource detected by its name
     * @param name
     * @return
     */
    public final static String getContentType(String name) {
        if (name.endsWith(".css")) return "text/css";
        if (name.endsWith(".js")) return "application/javascript";
        if (name.endsWith(".jpg") | (name.endsWith(".jpeg"))) return "image/jpeg";
        if (name.endsWith(".gif")) return "image/gif";
        // Default
        return "text/plain";
    }

    /**
     * Returns a resource file.
     * @param name
     * @return
     * @throws Exception
     */
    public static int fetchResource(String name, HttpExchange he) throws Exception {
        // @todo Refactor so that resources are loaded from the file system and JARs
        System.out.println("FETCHING RESOURCE "+name);
        String pathName = RESOURCE_FOLDER+name;
        File f = new File(pathName);
        if (!f.exists()) return 404; // File not found

        Path path = Paths.get(pathName);
        byte[] bytes = Files.readAllBytes(path);

        he.getResponseHeaders().set("Content-Type", ResourceHandler.getContentType(name));
        he.sendResponseHeaders(200, bytes.length);
        BufferedOutputStream bos = new BufferedOutputStream(he.getResponseBody());
        bos.write(bytes);
        bos.close();

        return 200;
    }

}
