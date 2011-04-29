package au.com.gaiaresources.bdrs.event;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.event.PostCollectionRecreateEvent;
import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostCollectionUpdateEvent;
import org.hibernate.event.PostCollectionUpdateEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.service.threshold.ThresholdService;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.threshold.Action;
import au.com.gaiaresources.bdrs.model.threshold.Condition;
import au.com.gaiaresources.bdrs.model.threshold.Threshold;
import au.com.gaiaresources.bdrs.model.threshold.ThresholdDAO;

/**
 * The <code>ThresholdEventListener</code> is triggered whenever a insert, 
 * update or delete occurs on an object. If thresholds have been created for
 * that class of object then the conditions on each threshold are applied to the
 * object. If the conditions for the threshold pass, the registered actions
 * for the threshold are applied. 
 */
public class ThresholdEventListener implements PostUpdateEventListener,
        PostInsertEventListener, PostCollectionUpdateEventListener,
        PostCollectionRecreateEventListener {

    private static final long serialVersionUID = 5389414102803552714L;

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private ThresholdDAO thresholdDAO;

    @Autowired
    private ThresholdService thresholdService;

    @Autowired
    private SessionFactory sessionFactory;

    public ThresholdEventListener() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        triggerUpdateOrInsert(event.getEntity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPostInsert(PostInsertEvent event) {
        triggerUpdateOrInsert(event.getEntity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
        triggerUpdateOrInsert(event.getAffectedOwnerEntityName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
        triggerUpdateOrInsert(event.getAffectedOwnerEntityName());
    }

    private void triggerUpdateOrInsert(Object entity) {
        if ((entity != null) && (entity instanceof PersistentImpl) && (((PersistentImpl)entity).getId() != null)) {
            
            PersistentImpl original = (PersistentImpl)entity;
            if (thresholdService.isRegisteredReference(original)) {
                // Triggered by an action.
            } else {
                
                Session sesh = sessionFactory.openSession();
                Transaction tx = sesh.beginTransaction();
                
                PersistentImpl persistent = (PersistentImpl) sesh.get(entity.getClass(), ((PersistentImpl) entity).getId());
                
                // Test if the transaction was rolled back
                if(persistent != null && entity.equals(persistent)) {
                    thresholdService.registerReference(persistent);                
                    
                    // Executing Thresholds
                    String className = persistent.getClass().getCanonicalName();
                    List<Threshold> thresholdList = thresholdDAO.getEnabledThresholdByClassName(sesh, className);
    
                    for (Threshold threshold : thresholdList) {
                        
                        boolean conditionsPassed = true;
                        for (Condition condition : threshold.getConditions()) {
                            
                            conditionsPassed = conditionsPassed
                                    && condition.applyCondition(sesh, persistent, thresholdService);
                        }
                        
                        // Conditions Passed. Applying Actions
                        if (conditionsPassed) {
                            for (Action action : threshold.getActions()) {
                                thresholdService.applyAction(sesh, threshold, persistent, action);
                            }
                        }
                    }
                } 
                
                // It is imperative that the transaction is committed before the
                // persistent is deregistered. Any events that get triggered
                // via action will be fired on commit. Only after that point
                // can the object be deregistered.
                tx.commit();
                sesh.close();
                
                // Removing an object that possibly was not registered
                thresholdService.deregisterReference(persistent);
            }
        }
    }
}
