package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import au.com.gaiaresources.bdrs.controller.AbstractGridControllerTest;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;

public class RecordFormFieldCollectionTest extends AbstractGridControllerTest {

    @Test
    public void testEmptyCreation() {
        
        Record r = this.getInitialRecordList(this.singleSiteMultiTaxaSurvey).get(0);
        
        List<RecordProperty> recordPropertyList = Collections.emptyList();
        List<Attribute> attrList = Collections.emptyList();
        RecordFormFieldCollection rffc = new RecordFormFieldCollection("hello", r, false, recordPropertyList, attrList);
        
        Assert.assertEquals("record ids should be equal", r.getId(), rffc.getRecordId());
        // we are passing in empty lists so the form field list size should also be 0
        Assert.assertEquals("# form fields does not match expected", 0, rffc.getFormFields().size());
    }
}
