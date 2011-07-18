package au.com.gaiaresources.bdrs.model.survey;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;

public class SurveyServiceImplTest extends AbstractControllerTest {
    @Autowired
    SurveyService surveyService;
    
    // test walking an cyclic tree
    @Test
    public void testCatalogCensusMethods() {
        CensusMethod a = new CensusMethod();
        CensusMethod b = new CensusMethod();
        CensusMethod c = new CensusMethod();
        CensusMethod d = new CensusMethod();
        CensusMethod e = new CensusMethod();
        CensusMethod f = new CensusMethod();
        CensusMethod g = new CensusMethod();
        CensusMethod h = new CensusMethod();
        CensusMethod i = new CensusMethod();
        CensusMethod j = new CensusMethod();
        
        a.getCensusMethods().add(b);
        a.getCensusMethods().add(c);
        a.getCensusMethods().add(d);
        
        b.getCensusMethods().add(e);
        b.getCensusMethods().add(f);
        
        d.getCensusMethods().add(i);
        i.getCensusMethods().add(h);
        
        f.getCensusMethods().add(g);
        
        g.getCensusMethods().add(b);
        
        e.getCensusMethods().add(a);
        
        h.getCensusMethods().add(j);
        
        Survey survey = new Survey();
        
        survey.getCensusMethods().add(a);
        survey.getCensusMethods().add(h);
        survey.getCensusMethods().add(i);
        
        Set<CensusMethod> result = surveyService.catalogCensusMethods(survey);
        
        Assert.assertEquals(10, result.size());
        
        Assert.assertTrue(result.contains(a));
        Assert.assertTrue(result.contains(b));
        Assert.assertTrue(result.contains(c));
        Assert.assertTrue(result.contains(d));
        Assert.assertTrue(result.contains(e));
        Assert.assertTrue(result.contains(f));
        Assert.assertTrue(result.contains(g));
        Assert.assertTrue(result.contains(h));
        Assert.assertTrue(result.contains(i));
        Assert.assertTrue(result.contains(j));
    }
}
