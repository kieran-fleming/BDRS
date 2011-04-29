package au.com.gaiaresources.bdrs.model.expert;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;

import java.util.List;
import java.util.Set;

/**
 * Interface for DAO for expert related stuff.
 * @author Tim Carpenter
 *
 */
public interface ExpertDAO {
    /**
     * Create an expert that knows about the given set of taxon groups and regions.
     * @param user {@link User} The expert should be already registered on the system.
     * @param taxonGroup {@link Set} of {@link TaxonGroup}s that the expert knows about.
     * @param regions {@link Set} of {@link Region}s that the expert knows about.
     * @return {@link Expert}
     */
    Expert createExpert(User user, Set<TaxonGroup> taxonGroup, Set<Region> regions);
    
    /**
     * Update an expert that knows about the given set of taxon groups and regions.
     * @param id The id of the expert {@link Integer}
     * @param taxonGroup {@link Set} of {@link TaxonGroup}s that the expert knows about.
     * @param regions {@link Set} of {@link Region}s that the expert knows about.
     * @return {@link Expert}
     */
    Expert updateExpert(Integer id, Set<TaxonGroup> taxonGroup, Set<Region> regions);
    
    /**
     * Get an expert.
     * @param id {@link Integer}
     * @return {@link Expert}
     */
    Expert getExpert(Integer id);
    
    /**
     * Get all experts.
     * @return {@link List} of {@link Expert}
     */
    List<? extends Expert> getExperts();
    
    /**
     * Find experts that know about the given group and region.
     * @param group {@link TaxonGroup}
     * @param region {@link Region}
     * @return {@link List} of {@link Expert}
     */
    List<? extends Expert> findExperts(TaxonGroup group, Region region);
    
    /**
     * Create a review request for the given record by the given expert.
     * @param record {@link Record}
     * @param expert {@link Expert}
     * @param reasonForRequest The reason for the request.
     * @return {@link ReviewRequest}
     */
    ReviewRequest requestReview(Record record, Expert expert, String reasonForRequest);
    
    /**
     * Get all existing review requests for a record.
     * @param record {@link Record}
     * @return {@link List} of {@link ReviewRequest}
     */
    List<? extends ReviewRequest> getRequests(Record record);
}
