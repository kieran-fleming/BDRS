package au.com.gaiaresources.bdrs.servlet.jsp.tag;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Formatter {
    
    private DateFormat dateFormat;
    
    public Formatter() {
        dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
    }

    public String formatDate(Date d) {
        return dateFormat.format(d);
    }
}
