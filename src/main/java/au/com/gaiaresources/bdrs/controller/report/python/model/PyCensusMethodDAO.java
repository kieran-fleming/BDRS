package au.com.gaiaresources.bdrs.controller.report.python.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;

/**
 * Represents a facade over the {@link CensusMethodDAO} ensuring that any data
 * retrieved using this facade is readonly.
 */
public class PyCensusMethodDAO {
    
    private CensusMethodDAO censusMethodDAO;
    
    /**
     * Creates a new instance.
     * 
     * @param censusMethodDAO retrieves census method related data.
     */
    public PyCensusMethodDAO(CensusMethodDAO censusMethodDAO) {
        this.censusMethodDAO = censusMethodDAO;
    }
    
    /**
     * Returns a JSON serialized census method with the specified primary key. 
     * 
     * @param id the primary key of the census method to be returned.
     * @return a JSON serialized census method with the specified primary key. 
     */
    public String getCensusMethodById(int censusMethodId, boolean includeAttributes) {
        CensusMethod cm = censusMethodDAO.get(censusMethodId);
        Map<String, Object> flatenedMethod = cm.flatten();
        
        if(includeAttributes) {
            List<Map<String, Object>> flatAttrList = new ArrayList<Map<String, Object>>();
            for(Attribute attr : cm.getAttributes()) {
                flatAttrList.add(attr.flatten());
            }
            flatenedMethod.put("attributes", flatAttrList);
        }
        
        return JSONObject.fromObject(flatenedMethod).toString();
    }
}
