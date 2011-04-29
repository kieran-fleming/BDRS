package au.com.gaiaresources.bdrs.service.db;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;

/**
 * The purpose of the <code>DeletionService</code> is to work around the
 * hibernate limitation of not being able to cascade delete backwards along a
 * foreign key.
 */
@Service
public class DeletionService {
    
    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private ManagedFileDAO managedFileDAO;
    
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    private Map<Class<? extends PersistentImpl>, DeleteCascadeHandler> daoMapping = new HashMap<Class<? extends PersistentImpl>, DeleteCascadeHandler>();

    public void registerDeleteCascadeHandler(Class<? extends PersistentImpl> klass, DeleteCascadeHandler cascadeHandler) {
        daoMapping.put(klass, cascadeHandler);
    }

    public void deregisterDeleteCascadeHandler(Class<? extends PersistentImpl> klass) {
        daoMapping.remove(klass);
    }
    
    public <T extends PersistentImpl> DeleteCascadeHandler getDeleteCascadeHandlerFor(T instance) {
        return getDeleteCascadeHandlerFor(instance.getClass());
    }
    
    public <T extends PersistentImpl> DeleteCascadeHandler getDeleteCascadeHandlerFor(Class<? extends PersistentImpl> klass) {
        return daoMapping.get(klass);
    }

    public void deleteRecords(IndicatorSpecies taxon) {
        for(Record rec : recordDAO.getRecords(taxon)) {
            getDeleteCascadeHandlerFor(rec).deleteCascade(rec);
        }
    }

    public void unlinkFromSurvey(IndicatorSpecies taxon) {
        for(Survey survey : surveyDAO.getSurveys(taxon)) {
            survey.getSpecies().remove(taxon);
            surveyDAO.save(survey);
        }
        taxaDAO.save(taxon);
    }

    public void deleteManagedFileByUUID(String uuid) {
        ManagedFile mf = managedFileDAO.getManagedFile(uuid);
        if(mf != null) {
            getDeleteCascadeHandlerFor(mf).deleteCascade(mf);
        }
    }
}
