package net.frapu.couchsc;

import com.inubit.research.client.JSONHttpRequest;
import com.inubit.research.server.ProcessEditorServerUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import net.frapu.code.visualization.domainModel.DomainClass;
import net.frapu.code.visualization.domainModel.DomainModel;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author frank
 */
public class InstanceConnector {

    public static String VALIDCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    private DomainModel model;
    private String server;
    private String dbName;
    private URI dbUri;

    public InstanceConnector(DomainModel model, String server) throws Exception {
        this.model = model;
        this.server = server;
        if (!server.endsWith("/")) {
            server += "/";
        }

        // Try to create database on server
        DomainClass root = model.getRootInstance();
        dbName = normalize(root.getName()).toLowerCase();
        dbUri = URI.create(server + dbName);
        JSONHttpRequest req = new JSONHttpRequest(dbUri);
        try {
            req.executePutRequest(null);
        } catch (IOException e) {
            if (req.getLastStatus() != 412) {
                throw e;
            }
        }
        //System.out.println("RESPONSE: " + req.getLastStatus());
    }

    public String getDbName() {
        return dbName;
    }

    public static String normalize(String in) {
        // Build normalized name
        in = in.replaceAll("ö", "oe");
        in = in.replaceAll("ä", "ae");
        in = in.replaceAll("ü", "ue");
        in = in.replaceAll("Ö", "OE");
        in = in.replaceAll("Ä", "AE");
        in = in.replaceAll("Ü", "UE");
        in = in.replaceAll("ß", "SS");
        String result = "";
        // Check for valid chars
        for (int i = 0; i < in.length(); i++) {
            if (VALIDCHARS.indexOf(in.charAt(i)) >= 0) {
                result += in.charAt(i);
            } else {
                result += "_";
            }
        }
        return result;
    }

    public JSONObject getView(String viewname, int start, int limit) throws
            IOException, ParserConfigurationException, JSONException {
        URI uri = URI.create(server + dbName+"/_design/view_"+dbName+"/_view/"+viewname);
        JSONHttpRequest req = new JSONHttpRequest(uri);
        return req.executeGetRequest();
    }

    public JSONObject getAssociationsFromDocument(String docname, int start, int limit) throws
            IOException, ParserConfigurationException, JSONException, URISyntaxException{
        URI serverUri = URI.create(server);
        URI uri = new URI(serverUri.getScheme(),
                serverUri.getUserInfo(),
                serverUri.getHost(),
                serverUri.getPort(),
                "/"+dbName+"/_design/assoc_"+dbName+"/_view/AssociationsFrom","key=\""+docname+"\"",
                null );
        JSONHttpRequest req = new JSONHttpRequest(uri);
        return req.executeGetRequest();
    }

    public JSONObject getDocument(String id) throws
            JSONException, MalformedURLException, IOException, ParserConfigurationException {
        JSONHttpRequest req = new JSONHttpRequest(URI.create(server + dbName+"/"+id));
        return req.executeGetRequest();
    }

    /**
     * Creates a new document in the connected db (generates an id)
     */
    public JSONObject putDocument(JSONObject doc) throws
            JSONException, MalformedURLException, IOException {
        // Check if uuid is contained, otherwise add one
        if (!doc.has("_id")) {
            doc.put("_id", UUID.randomUUID().toString());
        }
        // PUT
        JSONHttpRequest req = new JSONHttpRequest(URI.create(server + dbName + "/" + doc.get("_id")));
        return req.executePutRequest(doc);

    }
}
