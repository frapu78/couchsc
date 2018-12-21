/**
 * Server package for the Couch Solution Center (CouchSC).
 */
package net.frapu.couchsc;

import com.inubit.research.client.JSONHttpRequest;
import com.inubit.research.client.UserCredentials;
import net.frapu.couchsc.handler.DefaultHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.domainModel.Association;
import net.frapu.code.visualization.domainModel.Attribute;
import net.frapu.code.visualization.domainModel.DomainClass;
import net.frapu.code.visualization.domainModel.DomainModel;
import net.frapu.code.visualization.domainModel.DomainUtils;
import net.frapu.couchsc.utils.CouchDBHelper;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * The main class to start the server.
 *
 * @author frank
 */
public class CouchSCServer {

    private HttpServer server;
    private static CouchSCServer instance;
    private boolean running = false;
    private DomainModel model;
    private InstanceConnector ic = null;
    private String couchDBVersion = "";
    private static final int PORT = 2107;

    private static final int LIMIT = 20;

    //private final URI modelLocation = URI.create(("http://goofy.local:1205/models/1943951744"));
    private final URI modelLocation = URI.create(("http://localhost:1205/models/1033709973"));
    // Credentials for CouchDB
    private final String couchDBUrl = "http://localhost:5984/";
    private final String couchDBCredentials = CouchDBHelper.getBasicAuthString("admin", "admin");
    // Credentials for ProcessEditorServer
    private final UserCredentials modelUserCredentials = new UserCredentials("http://localhost:1205", "root", "inubit");

    public CouchSCServer() throws Exception {
    }

    public void start() {
        System.out.println("Server started at "+server.getAddress()+"...");
        if (this.server != null) {
            this.server.start();
        }
        this.running = true;
    }

    /**
     * Stop the server
     */
    public void stop() {
        System.out.println("Server stopped...");
        if (server != null) {
            this.server = null;
        }

        this.running = false;
    }

    public void updateDBViews() {
        try {
            createClassViews();
            createAssocViews();
        } catch (Exception e) {
            System.err.println("ERROR UPDATING DB VIEWS");
            e.printStackTrace();
        }

    }
    
    private void createAssocViews() throws JSONException, IOException {
        Map<String, JSONObject> assocs = new HashMap<String, JSONObject>();
        // Iterate over all edges
        for (ProcessEdge edge : model.getEdges()) {
            if (edge instanceof Association) {
                // Generate Javascript
                JSONObject map = new JSONObject();
                Association assoc = (Association)edge;
                String function = "function(doc) {";
                function += "if( doc.type==\"Association\") {";
                function += "emit( doc.source, doc); ";
                function += "}}";
                map.put("map", function);
                // Create a view for each class
                JSONObject view = new JSONObject();                
                assocs.put("AssociationsFrom", map);
            }
        }
        JSONObject doc = new JSONObject();
        doc.put("_id", "_design/assoc_" + ic.getDbName());
        doc.put("language", "javascript");
        doc.put("views", assocs);
        try {
            // Try to catch old version's _rev id
            JSONObject ov = ic.getDocument("_design/assoc_" + ic.getDbName());
            doc.put("_rev", ov.get("_rev"));
        } catch (Exception e) {
        }
        JSONObject result = ic.putDocument(doc);
    }    

    private void createClassViews() throws JSONException, IOException {
        Map<String, JSONObject> views = new HashMap<String, JSONObject>();
        // Iterate over all classes
        for (ProcessNode node : model.getNodes()) {
            if (node instanceof DomainClass) {
                // Generate Javascript
                JSONObject map = new JSONObject();
                DomainClass dc = (DomainClass) node;
                String function = "function(doc) {";
                function += "if(";
                // Iterate over all child classes, too
                int size = DomainUtils.getChildren(dc, model).size();
                for (DomainClass dc1 : DomainUtils.getChildren(dc, model)) {
                    function += "doc.type==\"" + InstanceConnector.normalize(dc1.getName()) + "\"";
                    if ((size--) > 1) {
                        function += "|";
                    }
                }
                function += ") {";
                function += "emit(";
                // Check if the DomainClass has a key
                if (dc.getKey() != null) {
                    function += dc.getKey();
                } else {
                    // Use id as key
                    function += "doc._id";
                }
                function += ",";
                if (((DomainClass) node).getAttributesByIDs().size() > 0) {
                    function += "{";
                    int count = 0;
                    for (Attribute a : dc.getAttributesByIDs().values()) {
                        function += a.getName() + ": doc." + a.getName();
                        if (count < dc.getAttributesByIDs().size() - 1) {
                            function += ", ";
                        }
                        count++;
                    }
                    function += "}";
                } else {
                    function += " null";
                }
                function += ");";
                function += "}}";
                map.put("map", function);
                // Create a view for each class
                JSONObject view = new JSONObject();
                views.put(InstanceConnector.normalize(node.getName()), map);
            }
        }
        JSONObject doc = new JSONObject();
        doc.put("_id", "_design/view_" + ic.getDbName());
        doc.put("language", "javascript");
        doc.put("views", views);
        try {
            // Try to catch old version's _rev id
            JSONObject ov = ic.getDocument("_design/view_" + ic.getDbName());
            doc.put("_rev", ov.get("_rev"));
        } catch (Exception e) {
        }
        JSONObject result = ic.putDocument(doc);
    }

    public boolean isRunning() {
        return this.running;
    }

    public URI getRecentModelLocation() {
        return modelLocation;
    }

    public UserCredentials getRecentUserCredentials() {
        return modelUserCredentials;
    }

    public void init() throws Exception {
        // Load sample model
        model = (DomainModel) ProcessUtils.parseProcessModelSerialization(modelLocation,
                modelUserCredentials);
        // Connect to CouchDB and get version (just a check)
        JSONHttpRequest req = new JSONHttpRequest(URI.create(couchDBUrl));
        req.setRequestProperty("Authorization", couchDBCredentials);
        JSONObject obj = req.executeGetRequest();
        couchDBVersion = obj.optString("version");
        // Establish instance connector
        ic = new InstanceConnector(model, couchDBUrl, couchDBCredentials);
        // Create default entry

        /*
        JSONObject doc = new JSONObject();
        doc.put("_id", "123-456-788");
        doc.put("type", "Person");
        doc.put("Vorname", "Frank");
        doc.put("Nachname", "Puhlmann");
        doc.put("Geburtsdatum", "15/03/1978");

        System.out.println(doc);
        try {
            ic.putDocument(doc);
        } catch (IOException ioe) {
            // Don't care if already exists
        }
        */

        // Update db views
        System.out.print("Updating db views...");
        updateDBViews();
        System.out.println("OK");

        // Bind to all interfaces
        InetSocketAddress address = new InetSocketAddress(PORT);
        this.server = HttpServer.create(address, 255);
        // Add contexts
        server.createContext("/", new DefaultHandler());
    }

    public DomainModel getDomainModel() {
        return model;
    }

    public String getCouchDB() {
        return couchDBUrl;
    }

    public String getCouchDBVersion() {
        return couchDBVersion;
    }

    // Returns the limit for each entry to show
    public int getLimit() { return LIMIT;}

    public static CouchSCServer getInstance() {
        if (instance == null) {
            try {
                instance = new CouchSCServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public InstanceConnector getInstanceConnector() {
        return ic;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        CouchSCServer server = getInstance();
        server.init();
        server.start();

    }
}
