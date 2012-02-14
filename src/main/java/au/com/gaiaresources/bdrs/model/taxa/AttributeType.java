package au.com.gaiaresources.bdrs.model.taxa;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

/**
 * Types of attributes.
 * @author Tim Carpenter
 *
 */
public enum AttributeType implements E, JSONEnum {
    INTEGER("IN", "Integer"),
    INTEGER_WITH_RANGE("IR", "Integer Range"),
    DECIMAL("DE", "Decimal"),
    
    BARCODE("BC", "Bar Code"),
    REGEX("RE", "Regular Expression"),
    
    DATE("DA", "Date"),
    TIME("TM", "Time"),

    STRING("ST", "Short Text"),
    STRING_AUTOCOMPLETE("SA", "Short Text (Auto Complete)"),
    TEXT("TA", "Long Text"),
    
    HTML("HL", "HTML (Validated)"),
    HTML_NO_VALIDATION("HV", "HTML (Not Validated)"),
    HTML_COMMENT("CM", "Comment"),
    HTML_HORIZONTAL_RULE("HR", "Horizontal Rule"),

    STRING_WITH_VALID_VALUES("SV", "Selection"),
    
    SINGLE_CHECKBOX("SC", "Single Checkbox"),
    MULTI_CHECKBOX("MC", "Multi Checkbox"),
    MULTI_SELECT("MS", "Multi Select"),

    IMAGE("IM", "Image File"),
    FILE("FI", "Data File");

    private String code;
    private String name;

    private AttributeType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static <T extends E> T find(String code, T[] e) {
        for (T t : e) {
            if (t.getCode().equals(code)) {
                return t;
            }
        }
        return null;
    }
    
    /**
     * Returns true if type is an HTML type, one of:
     * AttributeType.HTML, AttributeType.HTML_NO_VALIDATION, AttributeType.HTML_COMMENT,
     * AttributeType.HTML_HORIZONTAL_RULE
     * @param type the type to find out if it is HTML
     * @return true if the type is an html type, false otherwise
     */
    public static boolean isHTMLType(AttributeType type) {
        return AttributeType.HTML.equals(type) ||
               AttributeType.HTML_NO_VALIDATION.equals(type) ||
               AttributeType.HTML_COMMENT.equals(type) ||
               AttributeType.HTML_HORIZONTAL_RULE.equals(type);
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
