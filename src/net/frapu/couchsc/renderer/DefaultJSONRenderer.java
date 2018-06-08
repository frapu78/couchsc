package net.frapu.couchsc.renderer;

public class DefaultJSONRenderer extends DefaultRenderer {

    @Override
    public String getSupportedContentType() {
        return "application/json";
    }
}
