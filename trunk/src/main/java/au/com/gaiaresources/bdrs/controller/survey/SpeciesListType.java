package au.com.gaiaresources.bdrs.controller.survey;

public enum SpeciesListType {

    ONE_SPECIES("ONE", "A Single Species", "e.g. Carnaby&#39;s Cockatoo"),
    MANY_SPECIES("MANY", "A Selection of Species", "e.g. Carnaby&#39;s Cockatoo, Hooded Plover and Red-rumped Parrot"),
    SPECIES_GROUP("GROUP", "A Related Group of Species", "e.g The Cacatuidae family"),
    ALL_SPECIES("ALL", "All Species", "All Known Species");

    private String code;
    private String name;
    private String tip;
    
    private SpeciesListType(String code, String name, String tip) {
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

