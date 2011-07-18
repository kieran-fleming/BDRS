package au.com.gaiaresources.bdrs.controller.form;

import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import net.sf.cglib.beans.BeanGenerator;

public class RecordFormEnhancer {
	
	private Logger log = Logger.getLogger(RecordFormEnhancer.class);
    
	/**
     * Create an enhanced RecordForm with fields for entering info about the given species.
     * @param species {@link IndicatorSpecies}
     * @return {@link RecordForm} enhanced by CGLIB.
     */
    public RecordForm enhance(IndicatorSpecies species) {
        BeanGenerator generator = new BeanGenerator();
        generator.setSuperclass(RecordForm.class);
        List<String> attributeNames = new ArrayList<String>();
        for (Attribute attribute : species.getTaxonGroup().getAttributes()) {
            if(attribute == null){
                continue;
            }
            AttributeType type = AttributeType.find(attribute.getTypeCode(), 
                                                                        AttributeType.values());
            if (attribute.isTag())
            	continue;
            
            String attributeName = StringUtils.removeNonAlphaNumerics(attribute.getName());
            attributeName = attributeName.toLowerCase();
            switch (type) {
            case DECIMAL:
                generator.addProperty(attributeName, BigDecimal.class);
                break;
            case INTEGER:
                generator.addProperty(attributeName, Integer.class);
                break;
            case INTEGER_WITH_RANGE:
            	break;
            case DATE:
                generator.addProperty(attributeName, Date.class);
                break;
            case FILE:
            case IMAGE:
                generator.addProperty(attributeName , MultipartFile.class);
                generator.addProperty(attributeName + "FileName", String.class);
                break;
            default:
                generator.addProperty(attributeName, String.class);
            }
            attributeNames.add(attributeName);
        }
        
        
        RecordForm f = (RecordForm) generator.create();
        return f;
    }
}
