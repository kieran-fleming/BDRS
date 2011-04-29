package au.com.gaiaresources.bdrs.model.record;

import org.springframework.context.ApplicationEvent;

public class NewRecordEvent extends ApplicationEvent {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NewRecordEvent(Record r) {
        super(r);
    }
    
    public Record getRecord() {
        return (Record) getSource();
    }
}
