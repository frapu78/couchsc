package net.frapu.couchsc.renderer;

import net.frapu.code.visualization.ProcessModel;

public class DefaultJSONRenderer extends DefaultRenderer {

    public DefaultJSONRenderer(ProcessModel recentModel) {
        super(recentModel);
    }

    @Override
    public String getSupportedContentType() {
        return "application/json";
    }
}
