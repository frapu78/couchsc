package net.frapu.couchsc.renderer;

import com.sun.net.httpserver.HttpExchange;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.couchsc.CouchSCServer;
import net.frapu.couchsc.InstanceConnector;
import org.json.JSONArray;
import org.json.JSONObject;

public class DefaultJSONRenderer extends DefaultRenderer {


    public DefaultJSONRenderer(CouchSCServer cscs) {
        super(cscs);
    }

    @Override
    public String getSupportedContentType() {
        return "application/json";
    }

    public String search(String requestUri) {
        // Parse URI according to scheme /search/{type}/{startLetters}
        try {
            String tmp = requestUri.replace("/search/", "");
            String type = tmp.substring(0, tmp.indexOf("/"));
            String term = tmp.substring(tmp.indexOf("/")+1);
            System.out.println("Found JSON /search request for type='"+type+"' with term '"+term+"'");

            // Only fetch the first twenty (LIMIT) results
            JSONObject instances = cscs.getInstanceConnector().getViewWithFilter(type, 0, CouchSCServer.getInstance().getLimit(), term);
            // Get count
            int countLeft = instances.getInt("total_rows") - instances.getInt("offset");
            // Get the rows
            JSONArray rows = instances.getJSONArray("rows");
            JSONObject response = new JSONObject();
            JSONArray responseList = new JSONArray();
            response.put("result", responseList);
            for (int pos = 0; pos < rows.length(); pos++) {
                JSONObject row = rows.getJSONObject(pos);
                String id = row.getString("id");
                String key = row.getString("key");
                JSONObject responseObj = new JSONObject();
                responseObj.put("id", id);
                responseObj.put("key", key);
                responseList.put(responseObj);
            }

            return response.toString();
        } catch (Exception ex) {
            return "{}";
        }

    }
}
