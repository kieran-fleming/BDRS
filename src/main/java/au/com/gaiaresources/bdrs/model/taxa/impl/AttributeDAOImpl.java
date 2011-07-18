package au.com.gaiaresources.bdrs.model.taxa.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.service.db.DeleteCascadeHandler;
import au.com.gaiaresources.bdrs.service.db.DeletionService;

@Repository
public class AttributeDAOImpl extends AbstractDAOImpl implements AttributeDAO {

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private DeletionService delService;
    
    @PostConstruct
    public void init() throws Exception {
        delService.registerDeleteCascadeHandler(Attribute.class, new DeleteCascadeHandler() {
            @Override
            public void deleteCascade(PersistentImpl instance) {
                delete((Attribute)instance);
            }
        });
        delService.registerDeleteCascadeHandler(AttributeOption.class, new DeleteCascadeHandler() {
            @Override
            public void deleteCascade(PersistentImpl instance) {
                delete((AttributeOption)instance);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAttributeValues(Attribute attr, String searchString) {
        Object[] obs = new Object[2];
        obs[0] = attr;
        obs[1] = "%" + searchString.toLowerCase() + "%";
        List<AttributeValue> attrs = find("from AttributeValue r where r.attribute = ? and lower(r.stringValue) like ?", obs);
        HashSet<String> values = new HashSet<String>();
        for (TypedAttributeValue ra : attrs) {
            values.add(ra.getStringValue());
        }
        List<String> sorted = new ArrayList<String>(values);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAttributeValues(int attributePK, String searchString) {
        Attribute attr = getByID(Attribute.class, attributePK);
        return getAttributeValues(attr, searchString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAttributeValues(Attribute attr) {
        List<AttributeValue> attrs = find("from AttributeValue r where r.attribute = ?", attr);
        HashSet<String> values = new HashSet<String>();
        for (TypedAttributeValue ra : attrs) {
            values.add(ra.getStringValue());
        }
        List<String> sorted = new ArrayList<String>(values);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAttributeValues(int attributePK) {
        Attribute attr = getByID(Attribute.class, attributePK);
        return getAttributeValues(attr);
    }

    @Override
    public Attribute save(Attribute attribute) {
        return super.save(attribute);
    }

    @Override
    public AttributeOption save(AttributeOption attributeOption) {
        return super.save(attributeOption);
    }

    @Override
    public void delete(AttributeOption option) {
        deleteByQuery(option);
    }
    
    @Override
    public void delete(Attribute attr) {
        List<AttributeOption> optionList = new ArrayList<AttributeOption>(attr.getOptions());
        attr.getOptions().clear();
        save(attr);
        
        DeleteCascadeHandler cascadeHandler = 
            delService.getDeleteCascadeHandlerFor(AttributeOption.class);
        for(AttributeOption option : optionList) {
            cascadeHandler.deleteCascade(option);
        }
        
        deleteByQuery(attr);
    }
    
    @Override
    public Attribute get(Integer pk) {
        return this.getByID(Attribute.class, pk);
    }
    
    @Override
    public AttributeValue save(AttributeValue av) {
        return super.save(av);
    }
    
    @Override
    public AttributeValue update(AttributeValue av) {
        return super.update(av);
    }
    
    @Override
    public void delete(AttributeValue av) {
        super.delete(av);
    }
    
    @Override
    public List<AttributeValue> getAttributeValueObjects(Attribute attr) {
        return getAttributeValueObjects(getSession(), attr);
    }

    @Override
    public void delete(Session sesh, Attribute attr) {
        super.delete(sesh, attr);
        
    }

    @Override
    public void delete(Session sesh, AttributeValue av) {
        super.delete(sesh, av);
        
    }

    @Override
    public List<AttributeValue> getAttributeValueObjects(Session sesh,
            Attribute attr) {
        return find(sesh, "from AttributeValue r where r.attribute = ?", attr);
    }

    @Override
    public Attribute save(Session sesh, Attribute attr) {
        return super.save(sesh, attr);
    }

    @Override
    public AttributeValue save(Session sesh, AttributeValue av) {
        return super.save(sesh, av);
    }
}
