package au.com.gaiaresources.bdrs.controller.embedded;

public enum EmbeddedFeature {
    LATEST_STATISTICS("Latest Statistics"),
    IMAGE_SLIDESHOW("Image Slideshow");
    
    private String name;

    EmbeddedFeature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
