package net.frapu.couchsc.renderer;

import net.frapu.code.visualization.ProcessModel;
import net.frapu.couchsc.CouchSCServer;

/*
 * This class implements an abstract Renderer class.
 */
public abstract class DefaultRenderer {

    protected CouchSCServer cscs;


    public DefaultRenderer(CouchSCServer cscs) {
        this.cscs = cscs;
    }

    /**
     * Return the content type that the implementing renderer supports.
     * @return
     */
    public abstract String getSupportedContentType();

}
