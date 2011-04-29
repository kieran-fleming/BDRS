package au.com.gaiaresources.bdrs.model.grid;

import java.util.List;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;

public interface GridService {
    void handleRecord(Record record);
    
    List<? extends Grid> getGrids();
    
    List<? extends GridEntry> getGridEntries(Grid grid, IndicatorSpecies s);
}
