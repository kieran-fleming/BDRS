package au.com.gaiaresources.bdrs.service.threshold.actionhandler;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.service.threshold.ActionHandler;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.threshold.Action;
import au.com.gaiaresources.bdrs.model.threshold.Threshold;

/**
 * Sets the {@link Record} <code>held</code> property to <code>true</code>.
 */
public class HoldRecordHandler implements ActionHandler {
    private Logger log = Logger.getLogger(getClass());

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeAction(Session sesh, Threshold threshold, Object entity, Action action) {
        if(!(entity instanceof Record)) {
            log.error("Cannot apply HoldRecord for an entity of type: "+entity.getClass().getCanonicalName());
            return;
        }
        
        Record rec = (Record) entity;
        
        // No need to reapply held = true if it is already true.
        if(!rec.getHeld()) {
            rec.setHeld(true);
            sesh.save(rec);
        }
    }
}
