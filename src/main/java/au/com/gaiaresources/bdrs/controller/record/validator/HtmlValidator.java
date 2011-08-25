/**
 * 
 */
package au.com.gaiaresources.bdrs.controller.record.validator;

import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jdom.input.BuilderErrorHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.service.property.PropertyService;

/**
 * @author stephanie
 *
 */
public class HtmlValidator extends StringValidator {

    /**
     * 
     * @param propertyService
     * @param required
     * @param blank
     */
    public HtmlValidator(PropertyService propertyService, boolean required,
            boolean blank) {
        super(propertyService, required, blank);
    }

    @Override
    public boolean validate(Map<String, String[]> parameterMap, String key,
            Attribute attribute, Map<String, String> errorMap) {
        String value = getSingleParameter(parameterMap, key);
        // the value is not likely to have <html><body> elements so add them here
        String htmlValue = (value != null && !value.startsWith("<html>") ? "<html><body>" : "") + 
            value + (value != null && !value.startsWith("<html>") ? "</body></html>" : "");
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // specify the error handler as the default writes to the console
            builder.setErrorHandler(new BuilderErrorHandler());
            builder.parse(new InputSource(new StringReader(htmlValue)));
            /*
            Source html = new StreamSource(new StringReader(htmlValue));
            SchemaFactory schemaFactory = SchemaFactory.newInstance(//XMLConstants.XML_NS_URI);
                                                                    "http://www.w3.org/2001/XMLSchema");
            Schema schema = schemaFactory.newSchema(//);
                                                    //new URL("http://www.w3.org/2002/08/xhtml/xhtml1-strict.xsd"));
                                                    new File("html3.xsd"));
                                                    //new File("xhtml1-strict.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(html);
            */
        }
        catch (Exception e) {
            errorMap.put(attribute != null ? attribute.getName() : key, e.getMessage());
        }
        return !errorMap.containsKey(key);
    }
}
