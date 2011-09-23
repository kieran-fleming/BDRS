package au.com.gaiaresources.bdrs.service.bulkdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;

@Service 
public class BulkDataService extends AbstractBulkDataService {
    @Autowired
    private CensusMethodDAO censusMethodDAO;
    
    
   @Override
    protected RecordRow getRecordRow() {
        return new XlsRecordRow(propertyService, surveyService, censusMethodDAO, bulkDataReadWriteService);
    }
}
