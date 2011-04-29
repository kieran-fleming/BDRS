package au.com.gaiaresources.bdrs.model.threshold.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.model.threshold.Action;
import au.com.gaiaresources.bdrs.model.threshold.Condition;
import au.com.gaiaresources.bdrs.model.threshold.Threshold;
import au.com.gaiaresources.bdrs.model.threshold.ThresholdDAO;

@Repository
public class ThresholdDAOImpl extends AbstractDAOImpl implements ThresholdDAO {
    Logger log = Logger.getLogger(ThresholdDAOImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Threshold getThreshold(Integer id) {
        return getByID(Threshold.class, id);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Condition getCondition(Integer id) {
        return getByID(Condition.class, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getAction(Integer id) {
        return getByID(Action.class, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Threshold save(Threshold t) {
        return super.save(t);
    }
   
    /**
     * {@inheritDoc}
     */
    @Override
    public Condition save(Condition condition) {
        return super.save(condition);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Action save(Action action) {
        return super.save(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Threshold save(Session sesh, Threshold t) {
        return super.save(sesh, t);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Threshold t) {
        if(t != null) {
            super.delete(t);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Condition condition) {
        if(condition != null) {
            super.delete(condition);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Action action) {
        if(action != null) {
            super.delete(action);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Threshold> all() {
        return find("from Threshold order by className");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Threshold> getEnabledThresholdByClassName(Session sesh, String className) {
        return find(sesh, "from Threshold where className = ? and enabled = true order by className", className);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Threshold> getEnabledThresholdByClassName(String className) {
        return this.getEnabledThresholdByClassName(getSession(), className);
    }
}
