/**
 * Server package for the Couch Solution Center (CouchSC).
 * <p>
 * This class handles the loading of resources from the file system.
 *
 * @author Frank Puhlmann
 */
package net.frapu.couchsc.handler;

import java.io.*;

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
        // Default
        return "text/plain";
    }

    /**
     * Returns a resource file.
     * @param name
     * @return
     * @throws Exception
     */
    public static String fetchTextResource(String name) throws Exception {
        System.out.println("FETCHING RESOURCE "+name);
        // @todo Refactor so that resources are loaded from the file system and JARs
        String result = "";

        File f = new File(RESOURCE_FOLDER+name);
        InputStreamReader r = new FileReader(f);
        BufferedReader br = new BufferedReader(r);
        while (br.ready()) {
            result += br.readLine();
        }
        br.close();
        r.close();

        return result;
    }

}
