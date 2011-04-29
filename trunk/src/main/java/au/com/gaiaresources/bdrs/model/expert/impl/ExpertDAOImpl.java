package au.com.gaiaresources.bdrs.model.expert.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.QueryOperation;
import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.model.expert.Expert;
import au.com.gaiaresources.bdrs.model.expert.ExpertDAO;
import au.com.gaiaresources.bdrs.model.expert.ReviewRequest;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;

@Repository
public class ExpertDAOImpl extends AbstractDAOImpl implements ExpertDAO {

    @Override
    public Expert createExpert(User user, Set<TaxonGroup> taxonGroups, Set<Region> regions) {
        Expert e = new Expert();
        e.setUser(user);
        e.setRegions(new HashSet<Region>());
        e.setTaxonGroups(new HashSet<TaxonGroup>());

        for (TaxonGroup tg : taxonGroups) {
            e.getTaxonGroups().add(tg);
        }
        for (Region r : regions) {
            e.getRegions().add(r);
        }
        return save(e);
    }

    @Override
    public Expert updateExpert(Integer id, Set<TaxonGroup> taxonGroups, Set<Region> regions) {
        Expert e = getExpert(id);
        e.getTaxonGroups().clear();
        e.getRegions().clear();

        for (TaxonGroup tg : taxonGroups) {
            e.getTaxonGroups().add(tg);
        }
        for (Region r : regions) {
            e.getRegions().add(r);
        }
        return update(e);
    }

    @Override
    public List<Expert> getExperts() {
    	return find("from Expert e order by e.user.lastName");
    }

    @Override
    public Expert getExpert(Integer id) {
        return getByID(Expert.class, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Expert> findExperts(TaxonGroup group, Region region) {
        return this.find("from Expert e where ? in elements(e.regions) and ? in elements(e.taxonGroups)",
                         new Object[] {group, region});
    }

    @Override
    public ReviewRequest requestReview(Record record, Expert expert, String reasonForRequest) {
        ReviewRequest request = new ReviewRequest();
        request.setExpert(expert);
        request.setReasonForRequest(reasonForRequest);
        request.setRecord(record);

        return save(request);
    }

    @Override
    public List<ReviewRequest> getRequests(Record record) {
    	return find("from ReviewRequest r where r.record = ?", record);
    }
}
