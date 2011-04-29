package au.com.gaiaresources.bdrs.controller.admin.taxa;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.springframework.web.multipart.MultipartFile;

import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.util.StringUtils;


public class IndicatorSpeciesForm {
    private Integer id;
    private String taxonGroup;
    private String scientificName;
    private String commonName;
    private String regionList;
    private MultipartFile file;
	
    @SuppressWarnings("unchecked")
    private Collection<String> regions = LazyList.decorate(new ArrayList<String>(), 
                                                     FactoryUtils.instantiateFactory(String.class));
    
    public IndicatorSpeciesForm() {
    }
    
    IndicatorSpeciesForm(IndicatorSpecies s) {
        this.setId(s.getId());
        this.setCommonName(s.getCommonName());
        this.setRegions(s.getRegionNames());
        this.setScientificName(s.getScientificName());
        this.setTaxonGroup(s.getTaxonGroup().getName());
        String[] regionNames = s.getRegionNames().toArray(new String[s.getRegionNames().size()]);
        this.setRegionList(StringUtils.buildDelimitedConcatenation(regionNames, ", ", false));
    }
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getTaxonGroup() {
        return taxonGroup;
    }
    public void setTaxonGroup(String taxonGroup) {
        this.taxonGroup = taxonGroup;
    }
    public String getScientificName() {
        return scientificName;
    }
    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }
    public String getCommonName() {
        return commonName;
    }
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
    public String getRegionList() {
        return regionList;
    }
    public void setRegionList(String regionList) {
        this.regionList = regionList;
    }
    public Collection<String> getRegions() {
        return regions;
    }
    public void setRegions(Collection<String> regions) {
        this.regions = regions;
    }
    public void setFile(MultipartFile file) {
        this.file = file;
    }
    public MultipartFile getFile() {
        return file;
    }
}
