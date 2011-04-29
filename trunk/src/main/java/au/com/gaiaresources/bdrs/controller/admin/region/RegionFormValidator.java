package au.com.gaiaresources.bdrs.controller.admin.region;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.region.RegionService;
import au.com.gaiaresources.bdrs.validation.Validator;

@Component
public class RegionFormValidator extends Validator<RegionForm> {
    @Autowired
    private RegionService regionService;
    
    @Override
    protected Class<RegionForm> getSupportedClass() {
        return RegionForm.class;
    }

    @Override
    protected void internalValidate(RegionForm target, Errors errors) {
        Region r = regionService.getRegion(target.getRegionName());
        if (r != null) {
            if (target.getId() == null || !target.getId().equals(r.getId())) {
                errors.rejectValue("name", "RegionForm.uniqueName", new Object[] {target.getRegionName()}, "");
            }
        }
    }
}
