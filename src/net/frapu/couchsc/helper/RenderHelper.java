package net.frapu.couchsc.helper;

import net.frapu.code.visualization.domainModel.DomainClass;
import net.frapu.code.visualization.domainModel.DomainUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.StringTokenizer;

public class RenderHelper {

    public static String resolveKey(DomainClass dc, JSONObject doc) throws JSONException {

        String key = dc.getKey();
        System.out.println("KEY="+key);
        // if key is NULL, check if superclass might have one...
        if (key==null) {
            List<DomainClass> sc = dc.getParents();
            for (DomainClass p: sc) {
                if (p.getKey() != null) {
                    key = p.getKey();
                    break;
                }
            }
        }
        if (key == null) return doc.getString("_id");

        String result = "";

        StringTokenizer st = new StringTokenizer(key, "+");

        while (st.hasMoreElements()) {
            String part = st.nextToken();
            // Check for "
            if (part.startsWith("\"")) {
                if (part.endsWith("\"")) {
                    if (part.length()>1) {
                        // ok, take text inside
                        result += part.substring(1, part.length()-1);
                    }
                }
            }

            // Check for doc.
            if (part.startsWith("doc.")) {
                String field = part.substring(4);
                // Try to fetch value from doc
                try {
                    result += doc.get(field);
                } catch (JSONException e) {
                }
            }
        }

        return result;
    }
}
