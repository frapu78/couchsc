/**
 * Server package for the Couch Solution Center (CouchSC).
 */
package net.frapu.couchsc.handler;

public class HTTPConstants {

    // HTTP Request Type
    public final static String HTTP_GET = "GET";
    public final static String HTTP_PUT = "PUT";
    public final static String HTTP_POST = "POST";
    public final static String HTTP_DELETE = "DELETE";

    // Header value
    public final static String HEADER_ACCEPT = "Accept";
    public final static String HEADER_CONTENT_TYPE = "Content-Type";

    // Content types
    public final static String CONTENT_TYPE_HTML = "text/html";
    public final static String CONTENT_TYPE_JSON = "application/json";
    public final static String CONTENT_TYPE_CSS = "text/css";
    public final static String CONTENT_TYPE_JS = "application/javascript";
    public final static String CONTENT_TYPE_JPG = "image/jpeg";
    public final static String CONTENT_TYPE_GIF = "image/gif";
    public final static String CONTENT_TYPE_TEXT = "text/plain";

    // Response codes
    public final static int HTTP_OK = 200;
    public final static int HTTP_OK_NO_CONTENT =201;
    public final static int HTTP_NOT_FOUND = 404;
    public final static int HTTP_ERROR = 500;
}
