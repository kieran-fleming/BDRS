package au.com.gaiaresources.bdrs.db;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

public class CoreInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 1L;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state,
            String[] propertyNames, Type[] types) {
        return updateWhoColumns(entity, id, state, propertyNames, types);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onFlushDirty(Object entity, Serializable id,
            Object[] currentState, Object[] previousState,
            String[] propertyNames, Type[] types) {
        return updateWhoColumns(entity, id, currentState, propertyNames, types);
    }

    private boolean updateWhoColumns(Object entity, Serializable id,
            Object[] state, String[] propertyNames, Type[] types) {
        RequestContext context = RequestContextHolder.getContext();
        boolean changed = false;

        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyNames[i].equals("createdAt")) {
                if (state[i] == null) {
                    state[i] = new Date();
                    changed = true;
                }
            } else if (propertyNames[i].equals("updatedAt")) {
                state[i] = new Date();
                changed = true;
            } else if (propertyNames[i].equals("createdBy")) {
                if (state[i] == null) {
                    state[i] = context.getUser() != null ? context.getUser().getId()
                            : null;
                    changed = true;
                }
            } else if (propertyNames[i].equals("updatedBy")) {
                state[i] = context.getUser() != null ? context.getUser().getId()
                        : null;
                changed = true;
            }
        }
        return changed;
    }
}
