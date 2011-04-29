package au.com.gaiaresources.bdrs.service.db;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;

public interface DeleteCascadeHandler {
    public void deleteCascade(PersistentImpl instance);
}
