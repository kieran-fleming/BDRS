package au.com.gaiaresources.bdrs.model.taxa.impl;


import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.test.AbstractTransactionalTest;

public class AttributeDAOImplTest extends AbstractTransactionalTest {

    @Autowired
    private AttributeDAO attrDAO;
    
    private Attribute attr;
    private AttributeValue av;
    
    @Before
    public void setup() {
        attr = new Attribute();
        attr.setName("my attr");
        attr.setDescription("my attr desc");
        attr.setTypeCode(AttributeType.STRING.toString());
        
        attr = attrDAO.save(attr);
        
        av = new AttributeValue();
        
        av.setStringValue(getTestString(255));
        av.setAttribute(attr);
        
        av = attrDAO.save(av);
    }
    
	// If we can save without any hibernate exceptions the test passes.
    // this is to stop any database migrations that may change the type
    // of the column.
    @Test
    public void testLongAttributeValueString() {
        int testStringLength = 300;
        
        AttributeValue longAv = new AttributeValue();
        longAv.setAttribute(attr);
        longAv.setStringValue(getTestString(testStringLength));
        attrDAO.save(longAv);
    }
    
    private String getTestString(int length) {
        StringBuilder sb = new StringBuilder();
       
        for (int i=0; i<length; ++i) {
            sb.append("a");
        }
        return sb.toString();
    }
}
