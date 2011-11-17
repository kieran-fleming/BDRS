package au.com.gaiaresources.bdrs.util;

import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.kml.KMLWriter;
import au.com.gaiaresources.bdrs.model.record.AccessControlledRecordAdapter;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.web.JsonService;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public class KMLUtils {
    private static Logger log = Logger.getLogger(KMLUtils.class);
    
    public static final String GET_RECORD_PLACEMARK_PNG_URL = "/bdrs/public/map/icon/record_placemark.png";
    public static final String KML_RECORD_FOLDER = "Record";
    public static final String KML_POINT_ICON_ID = "pointIcon";
    public static final String KML_POLYGON_STYLE = "polygonStyle";
    public static final String KML_POINT_ICON_ID_HIGHLIGHT = "pointIconHighlight";
    public static final String KML_POLYGON_STYLE_HIGHLIGHT = "polygonStyleHighlight";
    public static final String DEFAULT_PLACEMARK_COLOR = "EE9900";
    public static final String HIGHLIGHT_PLACEMARK_COLOR = "2500FF";

    public static final String KML_CONTENT_TYPE = "application/vnd.google-earth.kml+xml";
        
    private static void writePlacemark(KMLWriter writer, String label, String description, String id, Geometry geom) {
        if (geom instanceof Point) {
            writer.createPlacemark(KML_RECORD_FOLDER, label, description, id, geom, KML_POINT_ICON_ID);
        } else if (geom instanceof MultiPolygon) {
            writer.createPlacemark(KML_RECORD_FOLDER, label, description, id, geom, KML_POLYGON_STYLE);
        } else if (geom instanceof MultiLineString) {
            writer.createPlacemark(KML_RECORD_FOLDER, label, description, id, geom, KML_POLYGON_STYLE);
        } else {
            log.error("Geometry type not supported : " + geom.getClass().getName());    
        }
    }
    
    public static KMLWriter createKMLWriter(String contextPath, String placemarkColorHex) throws JAXBException {
        KMLWriter writer = new KMLWriter();
        String placemark = contextPath + GET_RECORD_PLACEMARK_PNG_URL + "?color=";
        
        placemarkColorHex = placemarkColorHex == null ? DEFAULT_PLACEMARK_COLOR : placemarkColorHex;
        placemark = placemark + placemarkColorHex;
        
        writer.createStyleIcon(KML_POINT_ICON_ID, placemark, 16, 16);
        writer.createStylePoly(KML_POLYGON_STYLE, placemarkColorHex.toCharArray());
        
        // create a highlighted placemark
        String hlPlacemark = contextPath + GET_RECORD_PLACEMARK_PNG_URL + "?color=";
        
        hlPlacemark = hlPlacemark + HIGHLIGHT_PLACEMARK_COLOR;
        
        writer.createStyleIcon(KML_POINT_ICON_ID_HIGHLIGHT, hlPlacemark, 16, 16);
        writer.createStylePoly(KML_POLYGON_STYLE_HIGHLIGHT, HIGHLIGHT_PLACEMARK_COLOR.toCharArray());
        
        writer.createFolder(KML_RECORD_FOLDER);
        return writer;
    }
    
    public static void writeRecords(KMLWriter writer, User currentUser, String contextPath, List<Record> recordList) {
        JsonService jsonService = AppContext.getBean(JsonService.class);
        
        String label;
        String description;
        
        for(Record record : recordList) {
            label = String.format("Record #%d", record.getId());
            AccessControlledRecordAdapter recAdapter = new AccessControlledRecordAdapter(record, currentUser);
            description = jsonService.toJson(recAdapter, contextPath).toString();
            
            Geometry geom = record.getGeometry();
            if (geom != null) {
                writePlacemark(writer, label, description, String.valueOf(record.getId()), geom);
            } else if(record.getLocation() != null && record.getLocation().getLocation() != null) {
                writePlacemark(writer, label, description, String.valueOf(record.getId()), record.getLocation().getLocation());
            } else {
                log.info("Cannot find coordinate for record");
            }
        }
    }

    public static void writeRecordsToKML(User currentUser, String contextPath, String placemarkColorHex, List<Record> recordList, OutputStream outputStream) throws JAXBException {
        KMLWriter writer = createKMLWriter(contextPath, placemarkColorHex);
        writeRecords(writer, currentUser, contextPath, recordList);
        writer.write(false, outputStream);
    }
}