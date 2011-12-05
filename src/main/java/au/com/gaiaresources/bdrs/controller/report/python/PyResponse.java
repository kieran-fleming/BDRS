package au.com.gaiaresources.bdrs.controller.report.python;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Encapsulates the response that the Python report wishes to send back to the
 * requesting browser.
 */
public class PyResponse {
    
    /**
     * The content type string of a HTML document.
     */
    public static final String HTML_CONTENT_TYPE = "text/html";
    
    private String headerName = null;
    private String headerValue = null;
    private String contentType = HTML_CONTENT_TYPE;
    private byte[] content = "".getBytes();
    private boolean isError = false;
    private boolean isStandalone = false;
    
    /**
     * @return the headerName
     */
    public String getHeaderName() {
        return headerName;
    }
    /**
     * @param headerName the headerName to set
     */
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
    /**
     * @return the headerValue
     */
    public String getHeaderValue() {
        return headerValue;
    }
    /**
     * @param headerValue the headerValue to set
     */
    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }
    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }
    
    public void setContent(String content) {
        this.content = content.getBytes();
    }
    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    /**
     * @return the content
     */
    public byte[] getContent() {
        return Arrays.copyOf(this.content, this.content.length);
    }
    /**
     * @param content the content to set
     */
    public void setContent(byte[] content) {
        this.content = Arrays.copyOf(content, content.length);
    }
    /**
     * @return the isStandalone
     */
    public boolean isStandalone() {
        return isStandalone;
    }
    /**
     * @param isStandalone the isStandalone to set
     */
    public void setStandalone(boolean isStandalone) {
        this.isStandalone = isStandalone;
    }
    /**
     * @return the isError
     */
    public boolean isError() {
        return isError;
    }
    /**
     * @param isError the isError to set
     */
    public void setError(boolean isError) {
        this.isError = isError;
    }
}
