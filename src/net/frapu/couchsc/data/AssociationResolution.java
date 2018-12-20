/**
 * Data model package for the Couch Solution Center (CouchSC).
 */
package net.frapu.couchsc.data;

import org.json.JSONObject;

/**
 *
 * This class encapsulates a resolved association for rendering.
 *
 * @author frank
 *
 */
public class AssociationResolution {

    private String source;
    private String target;
    private String targetClass;
    private JSONObject targetDoc;

    public AssociationResolution(String source, String target, String targetClass, JSONObject doc) {
        this.source = source;
        this.target = target;
        this.targetClass = targetClass;
        this.targetDoc = doc;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public JSONObject getTargetDoc() {
        return targetDoc;
    }
}
