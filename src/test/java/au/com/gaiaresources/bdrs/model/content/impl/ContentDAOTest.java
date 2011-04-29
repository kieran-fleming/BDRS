package au.com.gaiaresources.bdrs.model.content.impl;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;

public class ContentDAOTest extends AbstractControllerTest {

    @Autowired
    private ContentDAO contentDAO;

    private final String[] KEYS = { "BLAH", "blee", "woowoo", "page/asdlkfjas",
            "cvvsdf8f98w3r", "asdfa", "asdfasdfj" };

    @Before
    public void setup() throws Exception {
        for (String s : KEYS) {
            contentDAO.saveContent(s, "randomcontentwedontcare");
        }
    }

    @Test
    public void testGetKeys() {
        List<String> result = contentDAO.getAllKeys();
        for (String s : KEYS) {
            Assert.assertTrue(result.contains(s));
        }
    }
}
