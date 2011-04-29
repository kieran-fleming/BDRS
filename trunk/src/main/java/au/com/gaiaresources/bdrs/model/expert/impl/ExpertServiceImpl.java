package au.com.gaiaresources.bdrs.model.expert.impl;

import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.model.expert.Expert;
import au.com.gaiaresources.bdrs.model.expert.ExpertDAO;
import au.com.gaiaresources.bdrs.model.expert.ExpertService;
import au.com.gaiaresources.bdrs.model.expert.ReviewRequest;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExpertServiceImpl implements ExpertService {
    @Autowired
    private ExpertDAO expertDAO;
    @Autowired
    private EmailService emailService;
    
    @Override
    public Expert createAndNotifyExpert(User user, Set<TaxonGroup> taxonGroups, Set<Region> regions) {
        Expert e = expertDAO.createExpert(user, taxonGroups, regions);
        
        Map<String, Object> subParams = new HashMap<String, Object>();
        subParams.put("expert", e);
        emailService.sendMessage(e.getUser().getEmailAddress(), "Expert Registration", 
                                 "ExpertConfirmation.vm", subParams);
        
        return e;
    }

    @Override
    public Expert createExpert(User user, Set<TaxonGroup> taxonGroup, Set<Region> regions) {
        return expertDAO.createExpert(user, taxonGroup, regions);
    }

    @Override
    public Expert getExpert(Integer id) {
        return expertDAO.getExpert(id);
    }

    @Override
    public List<? extends Expert> getExperts() {
        return expertDAO.getExperts();
    }

    @Override
    public Expert updateExpert(Integer id, Set<TaxonGroup> taxonGroup, Set<Region> regions) {
        return expertDAO.updateExpert(id, taxonGroup, regions);
    }
    
    @Override
    public ReviewRequest requestReview(Record record, String reasonForRequest) {
        List<Expert> experts = new ArrayList<Expert>();
        for (Region r : record.getLocation().getRegions()) {
            experts.addAll(expertDAO.findExperts(record.getSpecies().getTaxonGroup(), r));
        }
        
        if (experts.size() > 0) {
            Random random = new Random();
            int index = (int) Math.floor(random.nextDouble() * experts.size());
            Expert expert = experts.get(index);
            
            return expertDAO.requestReview(record, expert, reasonForRequest);
        }
        
        return null;
    }
}
