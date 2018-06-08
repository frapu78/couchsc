package net.frapu.couchsc.renderer;

/*
 * This class implements an abstract Renderer class.
 */
public abstract class DefaultRenderer {

    /**
     * Return the one(!) type of HTML5 content type that the implementing renderer supports.
     * @return
     */
    public abstract String getSupportedContentType();

}
