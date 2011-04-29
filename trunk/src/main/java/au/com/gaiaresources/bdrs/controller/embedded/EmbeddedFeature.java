package au.com.gaiaresources.bdrs.controller.embedded;

public enum EmbeddedFeature {
    LATEST_STATISTICS("Latest Statistics");
    
    private String name;

    EmbeddedFeature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
