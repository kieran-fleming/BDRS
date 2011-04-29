package au.com.gaiaresources.bdrs.controller.survey;

public enum UserSelectionType {

    ALL_USERS("ALL", "All Users", "All Registered Users"),
    SELECTED_USERS("SELECTED", "A Selection of Users", "e.g. John Smith, Jane Doe");

    private String code;
    private String name;
    private String tip;

    private UserSelectionType(String code, String name, String tip) {
        this.code = code;
        this.name = name;
        this.tip = tip;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getTip() {
        return tip;
    }
}

