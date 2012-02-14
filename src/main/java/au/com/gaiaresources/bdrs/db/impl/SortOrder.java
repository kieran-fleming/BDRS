package au.com.gaiaresources.bdrs.db.impl;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

/**
 * An indication if a dataset is to be sorted in ascending or descending order.
 */
public enum SortOrder implements JSONEnum {
    ASCENDING("1"), DESCENDING("2");

    private String text;

    SortOrder(String text) {
        this.text = text;
    }

    /**
     * Performs a case insensitive conversion of the text "asc" to {@link #ASCENDING} and "desc" to {@link #DESCENDING}.
     * 
     * @param text the text to be parsed.
     * @return the {@link SortOrder} representation of the provided string.
     * @throws NullPointerException if the text is null.
     * @throws ParseException if the text cannot be converted to a {@link SortOrder}.
     */
    public static SortOrder fromString(String text) throws ParseException,
            NullPointerException {
        if (text != null) {
            for (SortOrder e : SortOrder.values()) {
                if (text.equalsIgnoreCase(e.text)) {
                    return e;
                }
            }
            // some more definitions for asc / desc sort orders...
            if (text.equalsIgnoreCase("desc")) {
                return SortOrder.DESCENDING;
            }
            if (text.equalsIgnoreCase("asc")) {
                return SortOrder.ASCENDING;
            }
            throw new ParseException(
                    "Cannot create enum PaginationFilter.SortOrder from text: "
                            + text, 0);
        }
        throw new NullPointerException(
                "Cannot create enum PaginationFilter.SortOrder from null string");
    }
    
    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONEnumUtil.writeJSONString(out, this);
    }

    @Override
    public String toJSONString() {
        return JSONEnumUtil.toJSONString(this);
    }
}