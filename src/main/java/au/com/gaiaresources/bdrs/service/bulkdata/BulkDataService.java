package au.com.gaiaresources.bdrs.service.bulkdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;

@Service 
public class BulkDataService extends AbstractBulkDataService {
    
   
   @Override
   protected RecordRow getRecordRow(Survey survey) {
	   if (survey != null ) {
		   return new XlsRecordRow(propertyService, surveyService, bulkDataReadWriteService, survey);   
	   } else {
		   return new XlsRecordRow(propertyService, surveyService, bulkDataReadWriteService);
	   }
       
   }
}
