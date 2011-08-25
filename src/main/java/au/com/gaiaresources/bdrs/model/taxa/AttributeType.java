package au.com.gaiaresources.bdrs.model.taxa;

/**
 * Types of attributes.
 * @author Tim Carpenter
 *
 */
public enum AttributeType implements E {
    INTEGER("IN", "Integer"),
    INTEGER_WITH_RANGE("IR", "Integer Range"),
    DECIMAL("DE", "Decimal"),
    
    BARCODE("BC", "Bar Code"),

    DATE("DA", "Date"),
    TIME("TM", "Time"),

    STRING("ST", "Short Text"),
    STRING_AUTOCOMPLETE("SA", "Short Text (Auto Complete)"),
    TEXT("TA", "Long Text"),
    
    HTML("HL", "HTML"),
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
}
