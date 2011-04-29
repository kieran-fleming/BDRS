package au.com.gaiaresources.bdrs.model.metadata;

import java.util.List;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;


public interface MetadataDAO extends TransactionDAO {

    /**
     * Returns the requested metadata
     * @param the id of the metadata that is requested.
     * @return the requested Metadata.
     */
     Metadata get(Integer id);

     /**
      * Creates or updates the specified metadata instance.
      * @param md the instance to be persisted
      * @return the saved metadata instance.
      */
     Metadata save(Metadata md);
     
     /**
      * Creates or updates the specified metadata instance using the provided session.
      * @param sesh the session to use when persistenting.
      * @param md the instance to be persisted
      * @return the saved metadata instance.
      */
     Metadata save(Session sesh, Metadata md);

     /**
      * Deletes the specified metadata from the database.
      * @param md the metadata to be removed.
      */
     void delete(Metadata md);
     
     Metadata update(Metadata md);

     /**
      * Returns all metadata.
      * @return a list of all metadata.
      */
     List<Metadata> getMetadata();
}
