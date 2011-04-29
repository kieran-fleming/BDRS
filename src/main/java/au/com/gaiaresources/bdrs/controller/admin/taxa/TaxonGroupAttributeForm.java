package au.com.gaiaresources.bdrs.controller.admin.taxa;



public class TaxonGroupAttributeForm {
    private Integer taxonGroupId;
    private Integer attributeId;
    private String dataTypeCode;
    private boolean required;
    private String name;
    
    //Optional set of attributes
    
    public void setRequired(boolean required) {
        this.required = required;
    }
    public boolean isRequired() {
        return required;
    }
    public void setDataTypeCode(String dataTypeCode) {
        this.dataTypeCode = dataTypeCode;
    }
    public String getDataTypeCode() {
        return dataTypeCode;
    }
    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }
    public Integer getAttributeId() {
        return attributeId;
    }
    public void setTaxonGroupId(Integer taxonGroupId) {
        this.taxonGroupId = taxonGroupId;
    }
    public Integer getTaxonGroupId() {
        return taxonGroupId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    } 
}
