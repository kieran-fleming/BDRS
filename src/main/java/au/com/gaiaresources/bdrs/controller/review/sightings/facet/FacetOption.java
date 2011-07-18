package au.com.gaiaresources.bdrs.controller.review.sightings.facet;

/**
 * Represents a parameter to the selection criteria represented by the
 * {@link Facet}.
 */
public abstract class FacetOption {

    private String displayName;
    private String value;
    private Long count;
    private boolean selected;

    /**
     * Creates a new <code>FacetOption</code>
     * @param displayName the name/heading to be displayed to the user.
     * @param value the value that will be applied if the option is selected.
     * @param count the number of records that this query will affect.
     * @param selected true if this option is applied, false otherwise.
     */
    public FacetOption(String displayName, String value, Long count,
            boolean selected) {
        super();
        this.displayName = displayName;
        this.value = value;
        this.count = count;
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
