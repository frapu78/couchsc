/**
 * Server package for the Couch Solution Center (CouchSC).
 */
package net.frapu.couchsc.renderer;

import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.domainModel.Attribute;
import net.frapu.code.visualization.domainModel.DomainUtils;
import net.frapu.code.visualization.domainModel.EnumerationClass;
import net.frapu.couchsc.InstanceConnector;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * This class implements a default plain HTML renderer for CouchSC.
 */
public class DefaultHTMLRenderer extends DefaultRenderer {

    private ProcessModel recentModel;

    @Override
    public String getSupportedContentType() {
        return "html";
    }

    /**
     * Creates a new instance of the Default HTML Renderer with a specific ProcessModel.
     * @param model
     */
    public DefaultHTMLRenderer(ProcessModel model) {
        this.recentModel = model;
    }

    public ProcessModel getRecentModel() {
        return recentModel;
    }

    public void setRecentModel(ProcessModel recentModel) {
        this.recentModel = recentModel;
    }

    /**
     * Renders an Attribute a with the instance doc.
     * @param a The attribute
     * @param doc The instance (null if to be created)
     * @return
     * @throws JSONException
     */
    public String renderAttribute(Attribute a, JSONObject doc) throws JSONException  {
        String response = "";
        response += "<tr><td>" + a.getName() + (a.getMultiplicity().startsWith("0..1") ? "*" : "") + "</td>";
        response += "<td>" + a.getType() + "</td>";
        String value = "";
        if (doc==null) {
            // Check for default value in Type
            if (a.getDefault()!=null) {
                value = a.getDefault();
            }

        } else if (doc.has(a.getName())) {
            value = doc.getString(a.getName());
            }
        // Check different types, start with a check if the type is an enumeration in the model
        List<ProcessNode> enums = recentModel.getNodesByClass(EnumerationClass.class);

        //
        // Check for Enumeration
        //
        for (ProcessNode node: enums) {
            if (node.getName().equalsIgnoreCase(a.getType())) {
                //
                // Found enumeration, render as drop-down
                //
                response += "<td><select name='"+a.getName()+"'>";
                EnumerationClass e = (EnumerationClass)node;
                String[] types = e.getTypes();
                Boolean found = false;
                for (String t: types) {
                    if (t.equalsIgnoreCase(value)) found = true;
                    response += "<option "+(t.equalsIgnoreCase(value)?"selected='selected'":"")+" value='"+t+"'>"+t+"</option>";
                }
                response += "<select>";
                if (!found & doc != null) response += "<font color='#d00'> Old value '"+value+"' not supported anymore!</span>";
                response += "</td</tr>";
                return response;
            }
        }
        // Check for DatePicker
        if (a.getType().equalsIgnoreCase(Attribute.TYPE_DATE)) {
            response += "<td><input type=\"text\" id=\"datepicker\" value=\"" + value + "\" size=\"40\" name=\"" + InstanceConnector.normalize(a.getName()) + "\"/>";
        } else {
            //
            // Default rendering as text field (String)
            //
            response += "<td><input type=\"text\" value=\"" + value + "\" size=\"40\" name=\"" + InstanceConnector.normalize(a.getName()) + "\"/>";
        }
        // Check for multi instance (@todo: Implement support for multi instance!
        response += (a.getMultiplicity().endsWith("*") ? "[+]" : "") + "</td></tr>";

        return response;
    }

}
