package au.com.gaiaresources.bdrs.kml;

import java.awt.Color;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import au.com.gaiaresources.bdrs.util.StringUtils;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.AbstractFeatureType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.BasicLinkType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.BoundaryType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.ColorModeEnumType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.DocumentType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.FolderType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.IconStyleType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.KmlType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.LineStringType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.LinearRingType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.MultiGeometryType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.ObjectFactory;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.PlacemarkType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.PointType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.PolyStyleType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.PolygonType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.StyleType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.UnitsEnumType;
import au.com.gaiaresources.bdrs.kml.net.opengis.kml.Vec2Type;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * Writes KML files.
 * @author Tim Carpenter
 *
 */
public class KMLWriter {
    private JAXBContext context;
    private ObjectFactory objectFactory;

    private KmlType kmlType;
    private JAXBElement<KmlType> kml;
    private DocumentType documentType;

    /**
     * Constructor.
     * @throws JAXBException If the JAXBContext cannot be created.
     */
    public KMLWriter() throws JAXBException {
        context = JAXBContext.newInstance(ObjectFactory.class,
                                          au.com.gaiaresources.bdrs.kml.org.w3.atom.ObjectFactory.class,
                                          au.com.gaiaresources.bdrs.kml.oasis.names.tc.ciq.xsdschema.xal.ObjectFactory.class);
        objectFactory = new ObjectFactory();

        kmlType = objectFactory.createKmlType();
        kml = objectFactory.createKml(kmlType);

        documentType = objectFactory.createDocumentType();
        kmlType.setAbstractFeatureGroup(objectFactory.createDocument(documentType));
    }

    public void createStyle(String id) {
        StyleType styleType = objectFactory.createStyleType();
        styleType.setId(id);

        documentType.getAbstractStyleSelectorGroup().add(objectFactory.createStyle(styleType));
    }

    public void createStyleIcon(String id, String iconUrl) {
        StyleType styleType = objectFactory.createStyleType();
        styleType.setId(id);
        IconStyleType iconStyleType = objectFactory.createIconStyleType();
        BasicLinkType linkType = objectFactory.createBasicLinkType();
        linkType.setHref(iconUrl);
        iconStyleType.setIcon(linkType);
        styleType.setIconStyle(iconStyleType);

        documentType.getAbstractStyleSelectorGroup().add(objectFactory.createStyle(styleType));
    }

    public void createStyleIcon(String id, String iconUrl, int hotSpotX, int hotSpotY) {
        StyleType styleType = objectFactory.createStyleType();
        styleType.setId(id);
        IconStyleType iconStyleType = objectFactory.createIconStyleType();
        BasicLinkType linkType = objectFactory.createBasicLinkType();
        linkType.setHref(iconUrl);
        iconStyleType.setIcon(linkType);
        Vec2Type hotSpot = new Vec2Type();
        hotSpot.setX((double) hotSpotX);
        hotSpot.setY((double) hotSpotY);
        hotSpot.setXunits(UnitsEnumType.PIXELS);
        hotSpot.setYunits(UnitsEnumType.PIXELS);
        iconStyleType.setHotSpot(hotSpot);
        styleType.setIconStyle(iconStyleType);

        documentType.getAbstractStyleSelectorGroup().add(objectFactory.createStyle(styleType));
    }

    public void createStylePoly(String id, char[] colour) {
        StyleType styleType = objectFactory.createStyleType();
        styleType.setId(id);

        PolyStyleType polyStyle = objectFactory.createPolyStyleType();
        polyStyle.setColorMode(ColorModeEnumType.NORMAL);

        try {
            byte[] b = Hex.decodeHex(colour);
            polyStyle.setColor(b);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("Invalid colour.", e);
        }
        styleType.setPolyStyle(polyStyle);

        documentType.getAbstractStyleSelectorGroup().add(objectFactory.createStyle(styleType));
    }

    public void createStylePoly(String id, Color c) {
        String red = Integer.toHexString(c.getRed());
        String green = Integer.toHexString(c.getGreen());
        String blue = Integer.toHexString(c.getBlue());
        String alpha = Integer.toHexString(c.getAlpha());

        if (red.length() == 1) { red = "0" + red; }
        if (green.length() == 1) { green = "0" + green; }
        if (blue.length() == 1) { blue = "0" + blue; }
        if (alpha.length() == 1) { alpha = "0" + alpha; }

        createStylePoly(id, (alpha + blue + green + red).toCharArray());
    }

    /**
     * Create a folder to contain placemarks.
     * @param name <code>String</code> the name of the folder.
     */
    public void createFolder(String name) {
        FolderType folderType = objectFactory.createFolderType();
        folderType.setName(name);
        documentType.getAbstractFeatureGroup().add(objectFactory.createFolder(folderType));
    }

    /**
     * Create a placemark.
     * @param folderName The name of the folder.
     * @param label The label for the placemark.
     * @param location The location as a JTS <code>Geometry</code>.
     */
    public void createPlacemark(String folderName, String label, Geometry location) {
        createPlacemark(folderName, label, location, null);
    }

    /**
     * Create a placemark.
     * @param folderName The name of the folder.
     * @param label The label for the placemark.
     * @param location The location as a JTS <code>Geometry</code>.
     * @param style The name of the style.
     */
    public void createPlacemark(String folderName, String label, Geometry location, String style) {
        this.createPlacemark(folderName, label, null, location, style);
    }

    /**
     * Create a placemark.
     * @param folderName The name of the folder.
     * @param label The label for the placemark.
     * @param location The location as a JTS <code>Geometry</code>.
     * @param style The name of the style.
     */
    public void createPlacemark(String folderName, String label,
            String description, Geometry location, String style) {
        PlacemarkType placemarkType = objectFactory.createPlacemarkType();
        placemarkType.setName(label);
        if(description != null) {
            placemarkType.setDescription(description);
        }
        if (StringUtils.notEmpty(style)) {
            placemarkType.setStyleUrl("#" + style);
        }
        getFolder(folderName).getAbstractFeatureGroup().add(objectFactory.createPlacemark(placemarkType));

        if (location instanceof Point) {
            placemarkType.setAbstractGeometryGroup(createPoint((Point) location));
        } else if (location instanceof Polygon) {
            placemarkType.setAbstractGeometryGroup(createPolygon((Polygon) location));
        } else if (location instanceof MultiPolygon) {
            placemarkType.setAbstractGeometryGroup(createMultiPolygon((MultiPolygon)location));
        }
    }

    private JAXBElement<PointType> createPoint(Point p) {
        PointType pointType = objectFactory.createPointType();
        // pointType.getCoordinates().add(p.getX() + "," + p.getY());
        // Refer to KML spec. Longitude, Latitude, Altitude
        // http://code.google.com/apis/kml/documentation/kmlreference.html#point
        pointType.getCoordinates().add(p.getX() + "," + p.getY());
        return objectFactory.createPoint(pointType);
    }

    private JAXBElement<LineStringType> createLineString(LineString l) {
        LineStringType lineType = objectFactory.createLineStringType();
        for (Coordinate c : l.getCoordinates())
            lineType.getCoordinates().add(c.x + "," + c.y);
        return objectFactory.createLineString(lineType);
    }

    private JAXBElement<MultiGeometryType> createMultiPolygon(MultiPolygon mpoly) {
        MultiGeometryType mgtype = objectFactory.createMultiGeometryType();
        for (int i = 0; i < mpoly.getNumGeometries(); i++)
        {
            Geometry g = mpoly.getGeometryN(i);
            mgtype.getAbstractGeometryGroup().add(createPolygon((Polygon)g));
        }
        return objectFactory.createMultiGeometry(mgtype);
    }


    private JAXBElement<PolygonType> createPolygon(Polygon p) {
        PolygonType polygonType = objectFactory.createPolygonType();
        polygonType.setExtrude(false);
        polygonType.setTessellate(false);

        LinearRingType ring = objectFactory.createLinearRingType();
        ring.setExtrude(false);
        ring.setTessellate(false);
        LineString exteriorRing = p.getExteriorRing();
        for (Coordinate c : exteriorRing.getCoordinates()) {
            ring.getCoordinates().add(c.x + "," + c.y);
        }

        BoundaryType outerBoundary = objectFactory.createBoundaryType();
        outerBoundary.setLinearRing(ring);

        polygonType.setOuterBoundaryIs(outerBoundary);

        return objectFactory.createPolygon(polygonType);
    }

    private FolderType getFolder(String name) {
        for (JAXBElement<? extends AbstractFeatureType> feature : documentType.getAbstractFeatureGroup()) {
            if (feature.getDeclaredType().equals(FolderType.class)) {
                FolderType folder = (FolderType) feature.getValue();
                if (folder.getName().equals(name)) {
                    return folder;
                }
            }
        }
        createFolder(name);
        return getFolder(name);
    }

    /**
     * Write the file to a <code>String</code>.
     * @param formatted Should the output be formatted?
     * @return <code>String</code>.
     * @throws JAXBException If the file cannot be written.
     */
    public String write(boolean formatted) throws JAXBException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", formatted);
        StringWriter writer = new StringWriter();
        marshaller.marshal(kml, writer);
        return writer.toString();
    }

    /**
     * Write the file to an <code>OutputStream</code>.
     * @param formatted Should the output be formatted?
     * @param outputStream To write to.
     * @throws JAXBException If the file cannot be written.
     */
    public void write(boolean formatted, OutputStream outputStream) throws JAXBException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", formatted);
        marshaller.marshal(kml, outputStream);
    }
}
