package au.com.gaiaresources.bdrs.db.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// lifted from http://labs.jodd.org/d/paginate-with-hibernate.html

public class HqlUtil {

    public static String removeSelect(String hql) {
        int beginPos = hql.toLowerCase().indexOf("from");
        return hql.substring(beginPos);
    }

    private static final Pattern p = Pattern.compile("order\\s*by[\\w|\\W|\\s|\\S]*", Pattern.CASE_INSENSITIVE);

    public static String removeOrders(String hql) {
        Matcher m = p.matcher(hql);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
