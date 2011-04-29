package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import au.com.gaiaresources.bdrs.util.BeanUtils;
import au.com.gaiaresources.bdrs.util.DateFormatter;
import au.com.gaiaresources.bdrs.util.DateUtils;
import au.com.gaiaresources.bdrs.util.StringUtils;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class DisplayElementTag extends TagSupport {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object bean;
    private String render;
    private String format;
    
    protected enum TYPE {
        /** Field */
        F,
        /** Edit button */
        E,
        /** Action button */
        A,
        /** Text */
        T
    }
    
    protected enum FORMAT {
        /** Number */
        N,
        /** Date */
        D,
        /** Time */
        T,
        /** String */
        S,
        /** Checkbox */
        C
    }
    
    @Override
    public int doStartTag() throws JspException {
        String[] elements = render.split(":");
        if (elements.length > 0) {
            TYPE t = TYPE.valueOf(elements[0].toUpperCase());
            if (t != null) {
                HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
                switch (t) {
                case T:
                    String text = elements[1];
                    String[] subsNames = Arrays.copyOfRange(elements, 2, elements.length);
                    String[] subVals = new String[subsNames.length];
                    for (int i = 0; i < subsNames.length; i++) {
                        try {
                            subVals[i] = StringUtils.toString(BeanUtils.extractProperty(bean, subsNames[i]));
                        } catch (Exception e) {
                            throw new JspException("Failed to extract property " + subsNames[i] + " from class "
                                                 + bean.getClass().getName(), e);
                        }
                    }
                    text = StringUtils.substitution(text, "%", subVals);
                    try {
                        write(text);
                    } catch (IOException ioe) {
                        throw new JspException("Failed to write value.", ioe);
                    }
                    break;
                case F:
                    try {
                        Object value = BeanUtils.extractProperty(bean, elements[1]);
                        start(t, value);
                        if (value != null) {
                            String outputString = "";
                            if (elements.length > 2 && StringUtils.notEmpty(elements[2])) {
                                String[] formatElements = Arrays.copyOfRange(elements, 2, elements.length);
                                FORMAT f = FORMAT.valueOf(formatElements[0].toUpperCase());
                                switch (f) {
                                case N:
                                    int dp = 0;
                                    if (formatElements.length > 1) {
                                        dp = Integer.parseInt(formatElements[1]);
                                    }
                                    if (value instanceof Number) {
                                        NumberFormat nf = NumberFormat.getInstance();
                                        nf.setMaximumFractionDigits(dp);
                                        outputString = nf.format(value);
                                    }
                                    break;
                                case D:
                                    Date d = (Date) value;
                                    outputString = DateFormatter.format(d, DateFormatter.DAY_MONTH_YEAR);
                                    break;
                                case T:
                                    d = null;
                                    if (value instanceof Long) {
                                        d = new Date((Long) value);
                                    } else if (value instanceof Date) {
                                        d = (Date) value;
                                    } else {
                                        throw new NullPointerException("value is not an instance of Long or Date");
                                    }
                                    outputString = DateFormatter.format(d, DateFormatter.TIME, true);
                                    break;
                                case C:
                                    Boolean b = false;
                                    if (value instanceof Boolean) {
                                        b = (Boolean) value;
                                    }
                                    if (b) {
                                        outputString = "<img src=\"" + req.getContextPath() + "/images/icons/yes.png\"/>";
                                    } else {
                                        outputString = "<img src=\"" + req.getContextPath() + "/images/icons/no.png\"/>";
                                    }
                                    break;
                                default:
                                    outputString = value.toString();
                                }
                            } else {
                                outputString = value.toString();
                            }
                            write(outputString);
                        }
                    } catch (Exception e) {
                        throw new JspException("Failed to extract property " + render 
                                             + " from instance of " + bean.getClass(), e);
                    }
                    break;
                case A:
                case E:
                    ImageButtonTag imageButton = t.equals(TYPE.E) ? new EditButtonTag() : new ActionButtonTag();
                    
                    String url = elements[1];
                    String[] subNames = Arrays.copyOfRange(elements, 2, elements.length);
                    String[] subs = new String[subNames.length];
                    for (int i = 0; i < subNames.length; i++) {
                        try {
                            subs[i] = StringUtils.toString(BeanUtils.extractProperty(bean, subNames[i]));
                        } catch (Exception e) {
                            throw new JspException("Failed to extract property " + subNames[i] + " from class "
                                                 + bean.getClass().getName(), e);
                        }
                    }
                    url = StringUtils.substitution(url, "%", subs);
                    
                    try {
                        start(t, url);
                        
                        imageButton.setUrl(url);
                        imageButton.setPageContext(pageContext);
                        imageButton.setParent(this);
                        imageButton.doStartTag();
                        imageButton.doEndTag();
                        
                        end(t);
                    } catch (Exception e) {
                        throw new JspException("Failed to render link.", e);
                    }
                    break;
                default:
                    break;
                }
            }
        }
        
        return SKIP_BODY;
    }
    
    protected void start(TYPE type, Object value) throws IOException {
        // Nothing to do, wrappers can override this
    }
    
    protected void write(Object value) throws IOException {
        JspWriter out = pageContext.getOut();
        String writeValue = "";
        if (value instanceof Date) {
            Date d = (Date) value;
            if (DateUtils.hasTimeComponent(d)) {
                writeValue = DateFormatter.format(d, DateFormatter.DAY_MONTH_YEAR_TIME);
            } else {
                writeValue = DateFormatter.format(d, DateFormatter.DAY_MONTH_YEAR);
            }
        } else {
            writeValue = value.toString();
        }
        out.write(writeValue);
    }
    
    protected void write(String outputValue) throws IOException {
        JspWriter out = pageContext.getOut();
        out.write(outputValue);
    }
    
    protected void end(TYPE type) throws IOException {
        // Nothing to do, wrappers can override this
    }
    
    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }
    
    public Object getBean() {
        return bean;
    }
    public void setBean(Object bean) {
        this.bean = bean;
    }
    public String getRender() {
        return render;
    }
    public void setRender(String render) {
        this.render = render;
    }
    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }
}
