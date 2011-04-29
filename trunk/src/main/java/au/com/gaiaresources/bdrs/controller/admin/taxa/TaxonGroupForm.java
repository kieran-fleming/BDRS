package au.com.gaiaresources.bdrs.controller.admin.taxa;

public class TaxonGroupForm {
    private Integer id;
    private String name;
    private Boolean behaviour;
    private Boolean firstAppearance;
    private Boolean lastAppearance;
    private Boolean habitat;
    private Boolean weather;
    private Boolean number;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Boolean getBehaviour() {
        return behaviour;
    }
    public void setBehaviour(Boolean behaviour) {
        this.behaviour = behaviour;
    }
    public Boolean getFirstAppearance() {
        return firstAppearance;
    }
    public void setFirstAppearance(Boolean firstAppearance) {
        this.firstAppearance = firstAppearance;
    }
    public Boolean getLastAppearance() {
        return lastAppearance;
    }
    public void setLastAppearance(Boolean lastAppearance) {
        this.lastAppearance = lastAppearance;
    }
    public Boolean getHabitat() {
        return habitat;
    }
    public void setHabitat(Boolean habitat) {
        this.habitat = habitat;
    }
    public Boolean getWeather() {
        return weather;
    }
    public void setWeather(Boolean weather) {
        this.weather = weather;
    }
    public Boolean getNumber() {
        return number;
    }
    public void setNumber(Boolean number) {
        this.number = number;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
}
