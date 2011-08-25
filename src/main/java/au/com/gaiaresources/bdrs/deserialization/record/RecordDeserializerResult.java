package au.com.gaiaresources.bdrs.deserialization.record;

import java.util.Collections;
import java.util.Map;

import au.com.gaiaresources.bdrs.model.record.Record;


public class RecordDeserializerResult {

    private Map<String, String> errorMap = Collections.EMPTY_MAP;
    
    private RecordEntry entry;
    private boolean authorizedAccess = true;
    private Record record;
    
	public RecordDeserializerResult(RecordEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry cannot be null");
        }
        this.entry = entry;
    }
    
    public void setErrorMap(Map<String, String> errorMap) {
        this.errorMap = errorMap;
    }
    
    public Map<String, String> getErrorMap() {
        return this.errorMap;
    }
    
    public RecordEntry getRecordEntry() {
        return this.entry;
    }
    
    /**
     * Was the authorization checks ok?
     * 
     * @return
     */
    public boolean isAuthorizedAccess() {
        return authorizedAccess;
    }
    
    public void setAuthorizedAccess(boolean value) {
        authorizedAccess = value;
    }
    
    public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

}
