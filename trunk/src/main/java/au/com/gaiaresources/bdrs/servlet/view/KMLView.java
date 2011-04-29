package au.com.gaiaresources.bdrs.servlet.view;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

import au.com.gaiaresources.bdrs.kml.KMLWriter;

public class KMLView extends AbstractView {
    public static final String KML_CONTENT_TYPE = "application/vnd.google-earth.kml+xml";
    public static final String DEFAULT_VIEW_NAME = "kml";
    
    public KMLView() {
        super();
        setContentType(KML_CONTENT_TYPE);
    }
    
    
	@Override
    protected void renderMergedOutputModel(@SuppressWarnings("unchecked") Map model, HttpServletRequest request, HttpServletResponse response)
                                           throws Exception 
    {
        if (model.containsKey("kml") && model.get("kml") instanceof KMLWriter) {
            KMLWriter writer = (KMLWriter) model.get("kml");
            writer.write(false, response.getOutputStream());
        } else {
            throw new ServletException("No model property 'kml' found.");
        }
    }
}
