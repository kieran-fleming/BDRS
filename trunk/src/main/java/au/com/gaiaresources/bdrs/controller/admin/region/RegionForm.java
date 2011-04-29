package au.com.gaiaresources.bdrs.controller.admin.region;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;

@SuppressWarnings("unchecked")
public class RegionForm {
    private Integer id;
    private String regionName;
    private List<CoordinateForm> coordinates = LazyList.decorate(new ArrayList<CoordinateForm>(), 
                                                                 FactoryUtils.instantiateFactory(CoordinateForm.class));
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getRegionName() {
        return regionName;
    }
    public void setRegionName(String name) {
        this.regionName = name;
    }
    public List<CoordinateForm> getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(List<CoordinateForm> coordinates) {
        this.coordinates = coordinates;
    }
}
