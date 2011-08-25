package au.com.gaiaresources.bdrs.db.impl;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;

import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;

/**
 * Inherit this class if you require Metadata in your PortalPersistentImpl.
 * Note that a database migration will be required! 
 * 
 * Metadata is intended to store configuration information about a persisted object.
 * Try not to use it for 'user data' (for example data that would normally fit better
 * in an AttributeValue).
 * 
 * Build up this class with more type specific metadata methods as required.
 * 
 * @author aaron
 *
 */
@MappedSuperclass
public abstract class MetadataPortalPersistentImpl extends PortalPersistentImpl {
    
    private Set<Metadata> metadata = new HashSet<Metadata>();
    
    /**
     * Don't manipulate this collection outside of the MetadataPortalpersistentImpl.
     * The getter/setter is only here so hibernate can populate the collection
     * properly. All management is done internally.
     * 
     * @return
     */
    @ManyToMany(fetch = FetchType.LAZY)
    public Set<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(Set<Metadata> metadata) {
        this.metadata = metadata;
    }

    protected boolean getBooleanMetadata(String key, boolean defaultValue) {
        String value = getMetadataValueByKey(key, Boolean.valueOf(defaultValue).toString());
        return Boolean.parseBoolean(value);
    }
    
    protected Metadata setBooleanMetadata(String key, boolean value, MetadataDAO mdDAO) {
        if (mdDAO == null) {
            throw new IllegalArgumentException("mdDAO cannot be null");
        }
        return setMetadataByKey(key, Boolean.valueOf(value).toString(), mdDAO);
    }

    protected String getMetadataValueByKey(String key, String defaultValue) {
        if(key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        Metadata md = getMetadataByKey(key);
        if (md == null) {
            return defaultValue;
        }
        return md.getValue();
    }
    
    // We can't rely on the set to detect whether the metadata item has already been added to it.
    // I assume it has something to do with the equals() implementation in PortalPersistentImpl.
    // It can lead to having a duplicate in the set which is of course, bad. I don't want to
    // override the behaviour of equals() incase something else is relying on it. Check for
    // existince in the set via metadata key!
    // REFACTOR: this into a superclass that deals with metadata
    protected Metadata getMetadataByKey(String key) {
     // the above does not handle adding keys, the lookup is not updated.
        for (Metadata m : metadata) {
            if (key.equals(m.getKey())) {
                return m;
            }
        }
        return null;
    }
    
    protected Metadata setMetadataByKey(String key, String value, MetadataDAO mdDAO) {
        if(key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        Metadata md = getMetadataByKey(key);
        if (md == null) {
            // not found. create it!
            md = new Metadata();
            md.setKey(key);
            metadata.add(md);
        }
        md.setValue(value);
        metadata.add(md);
        return mdDAO.save(md);
    }
}
