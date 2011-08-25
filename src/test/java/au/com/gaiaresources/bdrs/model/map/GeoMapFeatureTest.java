package au.com.gaiaresources.bdrs.model.map;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;

public class GeoMapFeatureTest {
    @Test
    public void testAttributeValueOrdering() {
       GeoMapFeature gmf = new GeoMapFeature();
       
       Attribute a1 = new Attribute();
       a1.setWeight(2);
       Attribute a2 = new Attribute();
       a2.setWeight(1);
       Attribute a3 = new Attribute();
       a3.setWeight(0);
       
       AttributeValue av1 = new AttributeValue();
       av1.setAttribute(a1);
       AttributeValue av2 = new AttributeValue();
       av2.setAttribute(a2);
       AttributeValue av3 = new AttributeValue();
       av3.setAttribute(a3);
       
       // has a null attribute
       AttributeValue av4 = new AttributeValue();
       
       Set<AttributeValue> avSet = new HashSet<AttributeValue>();
       avSet.add(av1);
       avSet.add(av2);
       avSet.add(av3);
       avSet.add(av4);
       
       gmf.setAttributes(avSet);
       
       List<AttributeValue> avList = gmf.getOrderedAttributes();
       
       Assert.assertEquals(4, avList.size());
       Assert.assertEquals(av3, avList.get(0));
       Assert.assertEquals(av2, avList.get(1));
       Assert.assertEquals(av1, avList.get(2));
       Assert.assertEquals(av4, avList.get(3));
    }
}
