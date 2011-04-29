package au.com.gaiaresources.bdrs.model.expert;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;

import java.util.List;
import java.util.Set;

public interface ExpertService {
    /**
     * Create a new expert and notify them by e-mail.
     * @param user {@link User} that is the expert.
     * @param taxonGroups {@link Set} of {@link TaxonGroup} that the expert knows about.
     * @param regions {@link Set} of {@link Region} that the expert knows about.
     * @return {@link Expert}.
     */
    Expert createAndNotifyExpert(User user, Set<TaxonGroup> taxonGroups, Set<Region> regions);
    
    Expert createExpert(User user, Set<TaxonGroup> taxonGroup, Set<Region> regions);
    
    Expert updateExpert(Integer id, Set<TaxonGroup> taxonGroup, Set<Region> regions);
    
    Expert getExpert(Integer id);
    
    List<? extends Expert> getExperts();
    
    ReviewRequest requestReview(Record record, String reasonForRequest);
}
