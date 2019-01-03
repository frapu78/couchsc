package net.frapu.couchsc.renderer;

import net.frapu.code.visualization.ProcessModel;

/*
 * This class implements an abstract Renderer class.
 */
public abstract class DefaultRenderer {

    protected ProcessModel recentModel;

    public DefaultRenderer(ProcessModel recentModel) {
        this.recentModel = recentModel;
    }

    public ProcessModel getRecentModel() {
        return recentModel;
    }

    public void setRecentModel(ProcessModel recentModel) {
        this.recentModel = recentModel;
    }

    /**
     * Return the content type that the implementing renderer supports.
     * @return
     */
    public abstract String getSupportedContentType();



}
