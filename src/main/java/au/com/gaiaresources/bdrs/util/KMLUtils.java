package au.com.gaiaresources.bdrs.util;


import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.kml.KMLWriter;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.service.web.JsonService;

public class KMLUtils {
    private static Logger log = Logger.getLogger(KMLUtils.class);
    
    public static final String GET_RECORD_PLACEMARK_PNG_URL = "/bdrs/public/map/icon/record_placemark.png";
    public static final String KML_RECORD_FOLDER = "Record";
    public static final String KML_POINT_ICON_ID = "pointIcon";

    public static final String KML_CONTENT_TYPE = "application/vnd.google-earth.kml+xml";
    
    public static void writeRecordsToKML(String contextPath, String placemarkColorHex, List<Record> recordList, OutputStream outputStream) throws JAXBException {
        JsonService jsonService = AppContext.getBean(JsonService.class);
        
        KMLWriter writer = new KMLWriter();
        String placemark = contextPath + GET_RECORD_PLACEMARK_PNG_URL + "?color=";
        
        placemarkColorHex = placemarkColorHex == null ? "EE9900" : placemarkColorHex;
        placemark = placemark + placemarkColorHex;
        
        writer.createStyleIcon(KML_POINT_ICON_ID, placemark, 16, 16);
        writer.createFolder(KML_RECORD_FOLDER);
        String label;
        String description;

        for(Record record : recordList) {
            
            label = String.format("Record #%d", record.getId());
            description = jsonService.toJson(record).toString();

            if(record.getPoint() != null) {
                writer.createPlacemark(KML_RECORD_FOLDER, label, description, record.getPoint(), KML_POINT_ICON_ID);
            } else if(record.getLocation() != null && record.getLocation().getLocation() != null) {
                writer.createPlacemark(KML_RECORD_FOLDER, label, description, record.getLocation().getLocation(), KML_POINT_ICON_ID);
            } else {
                log.info("Cannot find coordinate for record");
            }
        }

        writer.write(false, outputStream);
    }
}