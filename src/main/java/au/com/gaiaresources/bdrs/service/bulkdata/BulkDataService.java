package au.com.gaiaresources.bdrs.service.bulkdata;

import org.springframework.stereotype.Service;

@Service 
public class BulkDataService extends AbstractBulkDataService {

   @Override
    protected RecordRow getRecordRow() {
        return new XlsRecordRow(propertyService, surveyService, bulkDataReadWriteService);
    }
}
