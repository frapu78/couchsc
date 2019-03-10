/**
 * Server package for the Couch Solution Center (CouchSC).
 */
package net.frapu.couchsc.handler;

import net.frapu.couchsc.CouchSCServer;
import net.frapu.couchsc.InstanceConnector;
import com.inubit.research.server.multipart.MultiPartItem;
import com.inubit.research.server.multipart.MultiPartObject;
import com.inubit.research.server.multipart.SimpleMultipartParser;
import com.inubit.research.server.request.handler.UserRequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.domainModel.Aggregation;
import net.frapu.code.visualization.domainModel.Association;
import net.frapu.code.visualization.domainModel.Attribute;
import net.frapu.code.visualization.domainModel.DomainClass;
import net.frapu.code.visualization.domainModel.DomainUtils;
import net.frapu.couchsc.data.AssociationResolution;
import net.frapu.couchsc.helper.RenderHelper;
import net.frapu.couchsc.renderer.DefaultHTMLRenderer;
import net.frapu.couchsc.renderer.DefaultJSONRenderer;
import net.frapu.couchsc.renderer.DefaultRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author frank
 *
 */
public class DefaultHandler implements HttpHandler {

    private Map<String, DefaultRenderer> renderer = new HashMap<>();

    public DefaultHandler() {
        // Get default renderer
        CouchSCServer cscs = CouchSCServer.getInstance();
        // Add HTML renderer
        DefaultRenderer htmlRenderer = new DefaultHTMLRenderer(cscs);
        renderer.put(htmlRenderer.getSupportedContentType(), htmlRenderer);
        // Add JSON renderer
        DefaultRenderer jsonRenderer = new DefaultJSONRenderer(cscs);
        renderer.put(jsonRenderer.getSupportedContentType(), jsonRenderer);
    }

    public void handle(HttpExchange he) throws IOException {

        if (he.getRequestMethod().equalsIgnoreCase(HTTPConstants.HTTP_GET)) {
            handleGetRequest(he);
        }

        if (he.getRequestMethod().equalsIgnoreCase(HTTPConstants.HTTP_POST)) {
            handlePostRequest(he);
        }

        if (he.getRequestMethod().equalsIgnoreCase(HTTPConstants.HTTP_PUT)) {
            handlePutRequest(he);
        }
    }

    private void handlePutRequest(HttpExchange he) throws IOException {
        // Handle PUT request here...
        System.out.println("Received PUT for " + he.getRequestURI());
        CouchSCServer cscs = CouchSCServer.getInstance();
        String requestUri = he.getRequestURI().toASCIIString();
        String response = "";
        response = renderHeader(response, cscs);
        int responseCode = HTTPConstants.HTTP_OK;

        SimpleMultipartParser smp = new SimpleMultipartParser();
        MultiPartObject mpo = smp.parseSource(he.getRequestBody());

        // Send response
        he.sendResponseHeaders(responseCode, response.length());
        PrintWriter w = new PrintWriter(he.getResponseBody());
        w.print(response);
        w.close();
    }

    private void handlePostRequest(HttpExchange he) throws IOException {
        // Handle POST request here...
        System.out.println("Received POST for " + he.getRequestURI());
        CouchSCServer cscs = CouchSCServer.getInstance();
        String requestUri = he.getRequestURI().toASCIIString();
        String response = "";
        response = renderHeader(response, cscs);
        int responseCode = HTTPConstants.HTTP_OK_NO_CONTENT;

        SimpleMultipartParser smp = new SimpleMultipartParser();
        MultiPartObject mpo = smp.parseSource(he.getRequestBody());

        if (requestUri.matches("/types/.*")) {
            // Create new instance of given type
            String requestedType = requestUri.replace("/types/", "");
            DomainClass typeClass = getDomainClassFromURI(cscs, requestedType);
            if (typeClass == null) {
                responseCode = HTTPConstants.HTTP_NOT_FOUND;
                response += "<h2>404 - NOT FOUND</h2>";
            } else {
                try {
                    // Create new document in db
                    JSONObject doc = new JSONObject();
                    doc.put(CouchSCServer.TYPE_INDICATOR, requestedType);
                    for (MultiPartItem item : mpo.getItems()) {
                        if (item.getDispositionAttribute("name") != null) {
                            doc.put(item.getDispositionAttribute("name"),
                                    item.getContent().substring(0, item.getContent().length() - 1));
                        }
                    }
                    JSONObject result = cscs.getInstanceConnector().putDocument(doc);
                    response += "<h2>Result</h2>";
                    if (result.getBoolean("ok")) {
                        response += "<p>Created with instance id: " + result.getString("id") + "</p>";
                    } else {
                        responseCode = HTTPConstants.HTTP_ERROR;
                        response += "<p>" + result.toString() + "</p>";
                    }
                    response += "<input class=\"button\" value=\"Ok\" type=\"button\" onClick=\"javascript:window.location='/'\"/>";
                } catch (Exception e) {
                    // Show error
                    responseCode = HTTPConstants.HTTP_ERROR;
                    response += "<h2>500 - DB SERVER ERROR</h2>";
                    response += "<p>" + e.getMessage() + "</p>";
                }
            }
        } else if (requestUri.matches("/data/.*")) {
            String requestedId = requestUri.replace("/data/", "");
            // Update document
            try {
                // 1: Fetch recent document
                JSONObject doc = cscs.getInstanceConnector().getDocument(requestedId);
                if (!doc.has(CouchSCServer.TYPE_INDICATOR)) {
                    responseCode = HTTPConstants.HTTP_NOT_FOUND;
                    response += "<h2>404 - TYPE NOT FOUND IN DOCUMENT</h2>";
                } else {
                    String requestedType = doc.getString(CouchSCServer.TYPE_INDICATOR);
                    DomainClass typeClass = getDomainClassFromURI(cscs, requestedType);
                    if (typeClass == null) {
                        responseCode = HTTPConstants.HTTP_NOT_FOUND;
                        response += "<h2>404 - TYPE NOT FOUND IN DOMAIN MODEL</h2>";
                    } else {
                        // 2: Merge received fields that comply to recent type
                        for (MultiPartItem item : mpo.getItems()) {
                            if (item.getDispositionAttribute("name") != null) {
                                String name = item.getDispositionAttribute("name");
                                String value = item.getContent().substring(0, item.getContent().length() - 1);
                                doc.put(name, value);
                            }
                        }
                        // 3: Put back
                        JSONObject result = cscs.getInstanceConnector().putDocument(doc);
                        response += "<h2>Result</h2>";
                        if (result.getBoolean("ok")) {
                            response += "<p>Updated with instance id: " + result.getString("id") + "</p>";
                        } else {
                            responseCode = HTTPConstants.HTTP_ERROR;
                            response += "<p>" + result.toString() + "</p>";
                        }
                        response += "<input class=\"button\" value=\"Ok\" type=\"button\" onClick=\"javascript:window.location='/'\"/>";
                    }
                }

            } catch (Exception e) {
                // Show error
                responseCode = HTTPConstants.HTTP_ERROR;
                response += "<h2>500 - SERVER ERROR</h2>";
                response += "<p>" + e.getMessage() + "</p>";
            }
        }

        // Send response
        he.sendResponseHeaders(responseCode, response.length());
        PrintWriter w = new PrintWriter(he.getResponseBody());
        w.print(response);
        w.close();
    }

    private void handleGetRequest(HttpExchange he) throws IOException {
        // Handle GET request here...
        CouchSCServer cscs = CouchSCServer.getInstance();
        String requestUri = he.getRequestURI().toASCIIString();

        String contentType = HTTPConstants.CONTENT_TYPE_HTML; // Default content type
        if (he.getRequestHeaders().containsKey(HTTPConstants.HEADER_ACCEPT)) {
            contentType = he.getRequestHeaders().getFirst(HTTPConstants.HEADER_ACCEPT);
            //System.out.println("Found content type: "+contentType);
        }

        String response = "";
        if (contentType.contains(HTTPConstants.CONTENT_TYPE_HTML)) {
            response = renderHeader(response, cscs);
        }
        int responseCode = HTTPConstants.HTTP_OK;

        // Parse URI here
        if (requestUri.matches("/")) {
            // List all root classes from the domain model
            DomainClass root_instance = cscs.getDomainModel().getRootInstance();
            try {
                response += "<img src=\"" + cscs.getRecentModelLocation().toString() + "/preview?size=256&" + UserRequestHandler.SESSION_ATTRIBUTE + "=" + cscs.getRecentUserCredentials().getSessionId() + "\" align=\"right\">";
            } catch (Exception ex) {
            }
            ;
            response += "<h2>Browsing root_instance " + root_instance.getName() + "</h2>";
            response = renderRootAggregations(response, root_instance, cscs);
        } else if (requestUri.matches("/types/.*")) {
            //
            // GET returns default form for specific type
            //
            String requestedType = requestUri.replace("/types/", "");
            DomainClass typeClass = getDomainClassFromURI(cscs, requestedType);
            if (typeClass == null) {
                responseCode = HTTPConstants.HTTP_NOT_FOUND;
                response += "<h2>404 - TYPE NOT FOUND IN DOMAIN MODEL</h2>";
            } else {
                if (typeClass.getProperty(DomainClass.PROP_ABSTRACT).equals(DomainClass.TRUE)) {
                    responseCode = HTTPConstants.HTTP_NOT_FOUND;
                    response += "<h2>404 - ABSTRACT TYPE CANNOT BE INSTANTIATED</h2>";
                } else {
                    try {
                        response += "<img src=\"" + cscs.getRecentModelLocation().toString() + "/nodes/" + typeClass.getId() + ".png?" + UserRequestHandler.SESSION_ATTRIBUTE + "=" + cscs.getRecentUserCredentials().getSessionId() + "\" align=\"right\">";
                        response += "<h2>Create new instance of " + typeClass.getName() + "</h2>";
                        response += "<form action=\"/types/" + requestedType + "\" method=\"post\" enctype=\"multipart/form-data\">";
                        response += "<div id=\"data-edit\" class=\"ui-widget\">";
                        response += "<table class=\"ui-widget ui-widget-content\">";
                        response += "<tr class=\"ui-widget-header\"><td>Attribute</td><td>Type</td><td>Value</td></tr>";
                        List<DomainClass> dcs = DomainUtils.getParents(typeClass, cscs.getDomainModel());
                        for (DomainClass dc : dcs) {
                            for (Attribute a : dc.getAttributesByIDs().values()) {
                                //@todo Refactor hack for renderer(!)
                                response += ((DefaultHTMLRenderer)renderer.get("html")).renderAttribute(a, null);
                            }
                        }
                        response += "</table></div>";
                        response += "<input class=\"ui-button ui-widget ui-corner-all\" value=\"Create\" type=\"submit\"/>";
                        response += "<input class=\"ui-button ui-widget ui-corner-all\"  value=\"Cancel\" type=\"button\" onClick=\"javascript:window.location='/'\"/>";
                        response += "</form>";
                    } catch (Exception ex) {
                    }
                }
            }
        } else if (requestUri.matches("/data/.*")) {
            //
            // Show default form for existing document
            //
            String requestedId = requestUri.replace("/data/", "");
            try {
                JSONObject doc = cscs.getInstanceConnector().getDocument(requestedId);
                JSONObject assocs = cscs.getInstanceConnector().getAssociationsFromDocument(requestedId, 0, 20);
                // Check if found
                if (doc.has("error")) {
                    String error = doc.getString("error");
                    if (error.equals("not_found")) {
                        responseCode = HTTPConstants.HTTP_NOT_FOUND;
                        response += "<h2>404 - NOT FOUND</h2>";
                    } else {
                        responseCode = HTTPConstants.HTTP_ERROR;
                        response += "<h2>500 - DB ERROR</h2>";
                        response += "<p>" + error + ":" + doc.getString("reason") + "</p>";
                    }
                } else {
                    // Get type
                    if (doc.has(CouchSCServer.TYPE_INDICATOR)) {
                        // No error, process
                        String requestedType = doc.getString(CouchSCServer.TYPE_INDICATOR);
                        DomainClass typeClass = getDomainClassFromURI(cscs, requestedType);
                        if (typeClass == null) {
                            responseCode = HTTPConstants.HTTP_NOT_FOUND;
                            response += "<h2>404 - TYPE NOT FOUND IN DOMAIN MODEL</h2>";
                        } else {
                            // Render values here...
                            response += "<img src=\"" + cscs.getRecentModelLocation().toString() + "/nodes/" + typeClass.getId() + ".png?" + UserRequestHandler.SESSION_ATTRIBUTE + "=" + cscs.getRecentUserCredentials().getSessionId() + "\" align=\"right\">";
                            response += "<h2>Edit instance of " + typeClass.getName() + " (" + doc.getString("_id") + ")</h2>";
                            response += "<form action=\"/data/" + doc.getString("_id") + "\" method=\"post\" enctype=\"multipart/form-data\">";
                            response += "<div  id=\"data-edit\" class=\"ui-widget\">";
                            response += "<table class=\"ui-widget ui-widget-content\">";
                            response += "<tr class=\"ui-widget-header \"><td>Attribute</td><td>Type</td><td>Value</td></tr>";
                            List<DomainClass> dcs = DomainUtils.getParents(typeClass, cscs.getDomainModel());
                            for (DomainClass dc : dcs) {
                                for (Attribute a : dc.getAttributesByIDs().values()) {
                                    //@todo Refactor hack for renderer(!)
                                    response += ((DefaultHTMLRenderer)renderer.get("html")).renderAttribute(a, doc);
                                }
                            }
                            response += "</table></div>";
                            response = renderAssociations(typeClass, cscs, response, doc.getString("_id"), assocs);

                            response += "<input class=\"ui-button ui-widget ui-corner-all\"  value=\"Update\" type=\"submit\"/>";
                            response += "<input class=\"ui-button ui-widget ui-corner-all\"  value=\"Cancel\" type=\"button\" onClick=\"javascript:window.location='/'\"/>";
                            response += "</form>";
                        }
                    } else {
                        // No type found
                        responseCode = HTTPConstants.HTTP_ERROR;
                        response += "<h2>500 - THE REQUESTED DOCUMENT HAS NO TYPE</h2>";
                        response += "<p>" + doc + "</p>";
                    }
                }
            } catch (Exception e) {
                responseCode = HTTPConstants.HTTP_ERROR;
                response += "<h2>500 - INTERNAL ERROR</h2>";
                response += "<p>" + e + "</p>";
            }
        } else if (requestUri.matches("/resources/.*")) {
            //
            // Check for resource file
            //
            try {
                String requestedResource = requestUri.replace("/resources/", "");

                responseCode = ResourceHandler.fetchResource(requestedResource, he);
                if (responseCode==HTTPConstants.HTTP_OK) return;
                if (responseCode==HTTPConstants.HTTP_NOT_FOUND) response += "<h2>404 - NOT FOUND</h2>";

            } catch (Exception e) {

                System.out.println(e);

                responseCode = HTTPConstants.HTTP_ERROR;
                response += "<h2>500 - INTERNAL ERROR</h2>";
                response += "<p>" + e + "</p>";
            }
        } else if (requestUri.matches("/search/.*")) {
            //
            // Check for search content type: application/json or text/html(!)
            //
            try {
                if (contentType.contains("application/json")) {
                    // Create search response as json
                    DefaultJSONRenderer jsonRenderer = (DefaultJSONRenderer)renderer.get("application/json");
                    response = jsonRenderer.search(requestUri);

                } else {
                    // Handle search page
                    response+="Search Picker...";
                }
            } catch (Exception e) {

                System.out.println(e);

                responseCode = HTTPConstants.HTTP_ERROR;
                response += "<h2>500 - INTERNAL ERROR</h2>";
                response += "<p>" + e + "</p>";
            }
        } else {
                responseCode = HTTPConstants.HTTP_NOT_FOUND;
                response += "<h2>404 - NOT FOUND</h2>";
            }

        if (contentType.contains(HTTPConstants.CONTENT_TYPE_HTML)) {
            response = renderFooter(response);
        }

        // Send response
        he.sendResponseHeaders(responseCode, response.length());
        PrintWriter w = new PrintWriter(he.getResponseBody());
        w.print(response);
        w.close();

    }

    /**
     * Renders all associated instances of a given node instance according to
     * the recent domain model.
     * @param typeClass
     * @param cscs
     * @param response
     * @param assocs
     * @return
     * @throws JSONException
     */
    private String renderAssociations(DomainClass typeClass, CouchSCServer cscs, String response, String sourceId, JSONObject assocs) throws JSONException {
        // Get Associations
        List<Association> modelAssocs = DomainUtils.getAssociations(typeClass, cscs.getDomainModel());
        if (modelAssocs.size() > 0) {

            // Fetch all outgoing associations and sort by target type
            LinkedList<AssociationResolution> assocList = new LinkedList<>();
            if (assocs.getInt("total_rows") > 0) {
                JSONArray rows = assocs.getJSONArray("rows");
                for (int pos = 0; pos < rows.length() && pos < CouchSCServer.getInstance().getLimit(); pos++) {
                    JSONObject value = rows.getJSONObject(pos).getJSONObject("value");
                    String targetId = value.getString("target");
                    // Fetch targetId
                    try {
                        JSONObject doc = cscs.getInstanceConnector().getDocument(targetId);
                        String targetType = doc.getString(CouchSCServer.TYPE_INDICATOR);
                        assocList.push(new AssociationResolution(sourceId, targetId, targetType, doc));
                    } catch (Exception e) {
                        // Ignore for now, skip
                    }
                }
                response += "</ul>";
            }

            response += "<ul>";
            for (Association assEdge : modelAssocs) {
                String edgeStyle = getEdgeStyle(assEdge);
                String name = assEdge.getLabel();
                if (name.isEmpty()) {
                    name = assEdge.getSource().getText() + edgeStyle + assEdge.getTarget().getText();
                }
                response += "<li>" + name;
                response += "<img src=\"/resources/images/plus_small.gif\" border=\"0\" align=\"bottom\"> create";
                // Check if they are associations in... (preprocess by type)
                for (AssociationResolution ar: assocList) {
                    response += "<ul>";
                    List<DomainClass> parentClasses = DomainUtils.getChildren((DomainClass)assEdge.getTarget(), cscs.getDomainModel() );
                    parentClasses.add((DomainClass)assEdge.getTarget());
                    for (DomainClass dc: parentClasses) {
                        if (ar.getTargetClass().equals(dc.getText())) {
                            // Found something that belongs to this association class
                            String targetKey = RenderHelper.resolveKey(dc, ar.getTargetDoc());
                            response += "<li><a href=\"/data/" + ar.getTarget() + "\">" + targetKey+"</a> (delete)</li>";
                            break;
                        }
                    }
                    response += "</ul>";
                }

                response += "</li>";
            }
            response += "</ul>";
        }
        return response;
    }

    /**
     * Renders all aggregated instances of a given root_instance.
     * @param response
     * @param root_instance
     * @param cscs
     * @return
     */
    private String renderRootAggregations(String response, DomainClass root_instance, CouchSCServer cscs) {
        response += "<ul>";
        List<Aggregation> rootAggs = DomainUtils.getAggregations(root_instance, cscs.getDomainModel());
        for (Aggregation agg : rootAggs) {
            response += "<li>" + agg.getSource().getName() + "<>--" + agg.getTarget().getName();
            response += "<img src=\"/resources/images/plus_small.gif\" border=\"0\" align=\"bottom\"> ";
            // List other types here (if existing)
            List<DomainClass> dcs = DomainUtils.getChildren((DomainClass) agg.getTarget(), cscs.getDomainModel());
            if (dcs.size() > 0) {
                for (DomainClass dc : dcs) {
                    if (dc.getProperty(DomainClass.PROP_ABSTRACT).equalsIgnoreCase(DomainClass.FALSE)) {
                        // Only non-abstract classes can be instantiated
                        response += " <a style=\"font-size:8pt\" href=\"types/" + InstanceConnector.normalize(dc.getName()) + "\">" + dc.getName() + "</a>";
                    }
                }
            }
            response += "<ul>";
            // Fetch views
            try {
                // Only fetch the first twenty (LIMIT) results
                JSONObject instances = cscs.getInstanceConnector().getView(InstanceConnector.normalize(agg.getTarget().getName()), 0, CouchSCServer.getInstance().getLimit());
                // Get count
                int countLeft = instances.getInt("total_rows") - instances.getInt("offset");
                // Get the rows
                JSONArray rows = instances.getJSONArray("rows");
                for (int pos = 0; pos < rows.length(); pos++) {
                    JSONObject row = rows.getJSONObject(pos);
                    String id = row.getString("id");
                    String key = row.getString("key");
                    response += "<li><a href=\"/data/" + id + "\">" + key + "</a></li>";
                }
                if (countLeft>cscs.getLimit()) {
                    response += "<li>... ("+(countLeft-cscs.getLimit())+" more)</li>";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println(ex);
            }
            response += "</ul>";
            response += "</li>";
        }
        response += "</ul>";
        return response;
    }

    public DomainClass getDomainClassFromURI(CouchSCServer cscs, String requestedType) {
        DomainClass typeClass = null;
        // Try to find type in model
        for (ProcessNode node : cscs.getDomainModel().getNodes()) {
            if (node instanceof DomainClass) {
                DomainClass sc = (DomainClass) node;
                String type = InstanceConnector.normalize(sc.getName());
                if (type.equalsIgnoreCase(requestedType)) {
                    typeClass = sc;
                }
            }
        }
        return typeClass;
    }

    private String renderFooter(String response) {
        // FOOTER
        response += "</body>";
        return response;
    }

    private String renderHeader(String response, CouchSCServer cscs) {
        // HEADER
        response = "<html><head><title>CouchSC Server</title>";
        // Include JQueryUI, see https://jqueryui.com
        response += "<link rel=\"stylesheet\" href=\"/resources/jquery-ui.css\">" +
                    " <link rel=\"stylesheet\" href=\"/resources/style.css\">" +
                    " <script src=\"/resources/jquery-1.12.4.js\"></script>" +
                    " <script src=\"/resources/jquery-ui.js\"></script>";
        response += "<style>" +
                    "    label, input { display:block; }" +
                    "    input.text { margin-bottom:12px; width:95%; padding: .4em; }" +
                    "    fieldset { padding:0; border:0; margin-top:25px; }" +
                    "    h1 { font-size: 1.2em; margin: .6em 0; }" +
                    "    div#data-edit { width: 350px; margin: 20px 0; }" +
                    "    div#data-edit table { margin: 1em 0; border-collapse: collapse; width: 100%; }" +
                    "    div#data-edit table td, div#data-edit table th { border: 1px solid #eee; padding: .6em 10px; text-align: left; }" +
                    "    .ui-dialog .ui-state-error { padding: .3em; }" +
                    "    .validateTips { border: 1px solid transparent; padding: 0.3em; }" +
                    "  </style>";
        response += "  <script>" +
                    "  $( function() {" +
                    "    $( \"#datepicker\" ).datepicker({" +
                    "       changeMonth: true," +
                    "      changeYear: true" +
                    "    });" +
                    "  } );" +
                    "  </script>";
        response += "</head><body>\n";
        response += "<table border=\"0\" width='100%'><tr>";
        response += "<td><h1><a href=\"/\">CouchSC Server</a></h1></td>";
        response += "<td valign='top' align='right'>";
        response += "<p>CouchDB: " + cscs.getCouchDBVersion() + " (" + cscs.getCouchDB() + cscs.getInstanceConnector().getDbName() + ")" + "<br>";
        response += "root_instance: " + cscs.getDomainModel().getRootInstance().getName() + " (" + cscs.getRecentModelLocation() + ")</p>";
        response += "</td></tr></table>";
        response += "<hr>";
        return response;
    }

    public String getEdgeStyle(Association assEdge) {
        String edgeStyle = "-->";
        if (assEdge.getProperty(Association.PROP_DIRECTION).equalsIgnoreCase(Association.DIRECTION_NONE)) {
            edgeStyle = "---";
        }
        if (assEdge.getProperty(Association.PROP_DIRECTION).equalsIgnoreCase(Association.DIRECTION_BOTH)) {
            edgeStyle = "<->";
        }
        return edgeStyle;
    }
}
