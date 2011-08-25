package au.com.gaiaresources.bdrs.controller.record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import au.com.gaiaresources.bdrs.deserialization.record.RecordEntry;
import au.com.gaiaresources.bdrs.model.location.LocationService;

import com.vividsolutions.jts.geom.Geometry;

public class TrackerFormToRecordEntryTransformer {
    
    private LocationService locService;
    
    public TrackerFormToRecordEntryTransformer(LocationService locationService) {
        locService = locationService;
    }

    public List<RecordEntry> httpRequestParamToRecordMap(Map<String, String[]> paramMap, Map<String, MultipartFile> fileMap) {
        
        String[] wktParam = paramMap.get(TrackerController.PARAM_WKT);
        Geometry geom = null;
        if (wktParam != null && wktParam.length > 0 && StringUtils.hasLength(wktParam[0])) {
            geom = locService.createGeometryFromWKT(wktParam[0]);
        }
        
        List<RecordEntry> result = new ArrayList<RecordEntry>();
        RecordEntry entry = new RecordEntry(paramMap, fileMap);
        entry.setGeometry(geom);
        result.add(entry);
        return result;
    }
}
