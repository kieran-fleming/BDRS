package au.com.gaiaresources.bdrs.db.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * A predicate is the part of the HQL sentence following the " from ClassName "
 * sentence Example usage: HqlQuery query = new HqlQuery( "from MyClass obj" );
 * query.and( Predicate.eq( "property", value ) .and(
 * Predicate.ilike("property", value+"%" ) .and( Predicate.eq("prop1", value
 * ).or( Predicate.eq( "prop2", value )));
 * 
 * @author Davide Alberto Molin (davide.molin@gmail.com)
 * 
 *         adapted from
 *         http://developme.wordpress.com/2010/03/03/creating-a-criteria
 *         -like-api-to-build-hql-queries/
 */
public class Predicate {
    private static Logger log = Logger.getLogger(Predicate.class);
    private String predicate;
    private List<Object> paramValues;

    public Predicate() {
        this.predicate = "";
        paramValues = new ArrayList<Object>();
    }

    public Predicate(String predicate) {
        this.predicate = (predicate == null ? "" : predicate);
        paramValues = new ArrayList();
    }

    public Predicate(String predicate, Object... paramValues) {
        this.predicate = (predicate == null ? "" : predicate);
        this.paramValues = new ArrayList();
        this.paramValues.addAll(Arrays.asList(paramValues));
    }

    public Predicate and(Predicate pg) {
        if (predicate.length() == 0)
            this.predicate = " WHERE " + enclose(pg.getQueryString());
        else
            this.predicate += " AND " + enclose(pg.getQueryString());
        this.paramValues.addAll(Arrays.asList(pg.getParametersValue()));
        return this;
    }

    public Predicate or(Predicate pg) {
        if (predicate.length() == 0)
            this.predicate = " WHERE " + enclose(pg.getQueryString());
        else
            this.predicate += " OR " + enclose(pg.getQueryString());
        this.paramValues.addAll(Arrays.asList(pg.getParametersValue()));
        return this;
    }

    private String enclose(String src) {
        return "(" + src + ")";
    }

    public static Predicate enclose(Predicate pg) {
        return new Predicate("(" + pg.getQueryString() + ")",
                pg.getParametersValue());
    }

    public static Predicate eq(String expression, Object value) {
        return new Predicate(expression + " = ?", value);
    }

    public static Predicate neq(String expression, Object value) {
        return new Predicate(expression + " != ?", value);
    }

    public static Predicate like(String expression, String value) {
        if (value == null)
            value = "";
        return new Predicate(expression + " like ?", value);
    }

    public static Predicate ilike(String expression, String value) {
        if (value == null)
            value = "";
        return new Predicate("upper(" + expression + ") like ?",
                value.toUpperCase());
    }

    public static Predicate in(String expression, Object[] values) {
        String cond = expression + " in (";
        for (Object o : values) {
            cond += (cond.endsWith("(") ? "?" : ",?");
        }
        cond += ")";
        return new Predicate(cond, values);
    }

    public static Predicate inElements(String expression, Object value) {
        StringBuilder cond = new StringBuilder();
        cond.append("?");
        cond.append(" in ");
        cond.append(expression);
        return new Predicate(cond.toString(), value);
    }
    
    public static Predicate notInElements(String expression, Object value) {
        StringBuilder cond = new StringBuilder();
        cond.append("?");
        cond.append(" not in ");
        cond.append(expression);
        return new Predicate(cond.toString(), value);
    }

    @Override
    public String toString() {
        return getQueryString();
    }

    public String getQueryString() {
        return predicate;
    }

    public Object[] getParametersValue() {
        return paramValues.toArray();
    }
}
