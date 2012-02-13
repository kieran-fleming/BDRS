package au.com.gaiaresources.bdrs.dwca;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.service.lsid.LSIDService;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;
import au.com.gaiaresources.bdrs.util.CSVUtils;
import au.com.gaiaresources.bdrs.util.FileUtils;
import au.com.gaiaresources.bdrs.util.ZipUtils;

public class RecordDwcaWriter {
    
    private Logger log = Logger.getLogger(getClass());
    
    public static final String META_FILE = "meta.xml";
    public static final String OCCURRENCE_FILE = "occurence.txt";
    public static final String MEASUREMENT_OR_FACT_FILE = "measurementorfact.txt";
    
    public static final String SURVEY_EXT_FORMAT = "survey_%s.txt";
    public static final String CENSUS_METHOD_EXT_FORMAT = "census_method_%s.txt";
       
    private static final String FIELDS_TERMINATED_BY = "\\t";
    private static final char FIELDS_TERMINATED_BY_CHAR = '\t';
    private static final String LINES_TERMINATED_BY = "\\n";
    
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    
    private final DateFormat ISO8601Local = new SimpleDateFormat(ISO_DATE_FORMAT);
    
    private Map<CorePropertyGetter, ConceptTerm> DWC_TERMS;
    private Map<MeasurementOrFactExtensionPropertyGetter, ConceptTerm> MOF_EXT_TERMS;
    
    public RecordDwcaWriter(LSIDService lsidService, LocationService locService, RedirectionService redirService) { 
        
        if (lsidService == null) {
            throw new IllegalArgumentException("LSIDService, lsidService, cannot be null");
        }
        if (locService == null) {
            throw new IllegalArgumentException("LocationService, locService, cannot be null");
        }
        if (redirService == null) {
            throw new IllegalArgumentException("RedirectionService, redirService, cannot be null");
        }

        // Create mapping. Use a LinkedHashMap to preserve order
        Map<CorePropertyGetter, ConceptTerm> coreMapBuilder = new LinkedHashMap<CorePropertyGetter, ConceptTerm>();
        coreMapBuilder.put(new CorePathGetter("id"), DwcTerm.catalogNumber);
        coreMapBuilder.put(new CoreScientificNameGetter(), DwcTerm.scientificName);
        coreMapBuilder.put(new CoreCoordGetter("latitude", locService), DwcTerm.decimalLatitude);
        coreMapBuilder.put(new CoreCoordGetter("longitude", locService), DwcTerm.decimalLongitude);
        coreMapBuilder.put(new CoreFixedGetter("HumanObservation"), DwcTerm.basisOfRecord);
        coreMapBuilder.put(new CoreFixedGetter("BDRS"), DwcTerm.institutionCode);
        coreMapBuilder.put(new CoreFixedGetter("BDRS"), DwcTerm.collectionCode);
        coreMapBuilder.put(new CoreSurveyLsidGetter(lsidService), DwcTerm.datasetID);
        coreMapBuilder.put(new CorePathGetter("survey.name"), DwcTerm.datasetName);
        coreMapBuilder.put(new CorePathGetter("user.fullName"), DwcTerm.recordedBy);
        coreMapBuilder.put(new CorePathGetter("number"), DwcTerm.individualCount);
        coreMapBuilder.put(new CorePathGetter("when"), DwcTerm.eventDate);
        coreMapBuilder.put(new CoreTaxonLsidGetter(lsidService), DwcTerm.taxonID);
        coreMapBuilder.put(new CoreTaxonLsidGetter(lsidService), DwcTerm.scientificNameID);
        
        DWC_TERMS = Collections.unmodifiableMap(coreMapBuilder);
        
        Map<MeasurementOrFactExtensionPropertyGetter, ConceptTerm> mofExtMapBuilder = new LinkedHashMap<MeasurementOrFactExtensionPropertyGetter, ConceptTerm>();
        mofExtMapBuilder.put(new MeasurementOrFactCoreIdGetter(), DwcTerm.catalogNumber);
        mofExtMapBuilder.put(new MeasurementOrFactIdGetter(), DwcTerm.measurementID);
        mofExtMapBuilder.put(new MeasurementOrFactTypeGetter(), DwcTerm.measurementType);
        mofExtMapBuilder.put(new MeasurementOrFactValueGetter(redirService), DwcTerm.measurementValue);
        mofExtMapBuilder.put(new MeasurementOrFactMethodGetter(), DwcTerm.measurementMethod);
        mofExtMapBuilder.put(new MeasurementOrFactRemarksGetter(), DwcTerm.measurementRemarks);
        
        MOF_EXT_TERMS = Collections.unmodifiableMap(mofExtMapBuilder);
    }
        
    /**
     * Does the work of writing the archive
     */
    public File writeArchive(ScrollableRecords scrollableRecords) {
        
        if (scrollableRecords == null) {
            throw new IllegalArgumentException("ScrollableRecords, scrollableRecords, cannot be null");
        }

        try {
            File targetDir = FileUtils.createTempDirectory("record_dwca");
                        
            writeDwcDataFiles(targetDir, scrollableRecords);
            writeMetaFile(targetDir);
            
            File outfile = new File(targetDir, "bdrs_dwca.zip");
            List<File> filesToCompress = new ArrayList<File>();

            filesToCompress.add(FileUtils.getFileFromDir(targetDir, META_FILE));
            filesToCompress.add(FileUtils.getFileFromDir(targetDir, OCCURRENCE_FILE));
            filesToCompress.add(FileUtils.getFileFromDir(targetDir, MEASUREMENT_OR_FACT_FILE));
            
            ZipUtils.compress(filesToCompress, outfile);
            
            return outfile; 
            
        } catch (IOException e) {
            log.error("failed to write archive", e);
            return null;
        } catch (ParserConfigurationException e) {
            log.error("failed to write archive", e);
            return null;
        }
    }
    
    /**
     * helper method for writing the meta file
     * 
     * @param targetDir
     * @throws IOException
     * @throws ParserConfigurationException
     */
    private void writeMetaFile(File targetDir) throws IOException, ParserConfigurationException {
        File metaFile = new File(targetDir.getAbsolutePath() + "/" + META_FILE);
        if (!metaFile.createNewFile()) {
            throw new IOException("Could not create meta file : " + metaFile.getAbsolutePath());
        }
        
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element archiveElement = document.createElement("archive");
        
        //archiveElement.setAttribute("metadata", "eml.xml");
        archiveElement.setAttribute("xmlns", "http://rs.tdwg.org/dwc/text/");
        
        document.appendChild(archiveElement);
        
        // CORE SECTION 
        {
            Element coreElement = createFileSection(document, archiveElement, "core", OCCURRENCE_FILE, DwcTerm.Occurrence.qualifiedName());
            // ID field - index 0.
            {
                // indicates the 0th row will be used to uniquely identify the record
                Element idElement = document.createElement("id");
                idElement.setAttribute("index", "0");
                coreElement.appendChild(idElement);
            }
            
            int fieldCount = 0;
            for (Entry<CorePropertyGetter, ConceptTerm> entry : DWC_TERMS.entrySet()) {
                
                Element fieldElement = document.createElement("field");
                fieldElement.setAttribute("index", String.format("%d", fieldCount));
                fieldElement.setAttribute("term", entry.getValue().qualifiedName());
                
                coreElement.appendChild(fieldElement);
                
                ++fieldCount;
            }
        }
        
        // MEASUREMENT OR FACT EXTENSION SECTION
        {
            Element mofExtElement = createFileSection(document, archiveElement, "extension", MEASUREMENT_OR_FACT_FILE, DwcTerm.MeasurementOrFact.qualifiedName());
            // core id field
            {
                Element coreIdElement = document.createElement("coreId");
                // says the field at index 0 (i.e. the first one) will map the extension file row to the core file row
                coreIdElement.setAttribute("index", "0");
                mofExtElement.appendChild(coreIdElement);
            }
            
            int fieldCount = 0;
            for (Entry<MeasurementOrFactExtensionPropertyGetter, ConceptTerm> entry : MOF_EXT_TERMS.entrySet()) {
                
                Element fieldElement = document.createElement("field");
                fieldElement.setAttribute("index", String.format("%d", fieldCount));
                fieldElement.setAttribute("term", entry.getValue().qualifiedName());
                
                mofExtElement.appendChild(fieldElement);
                
                ++fieldCount;
            }
        }
        
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(metaFile);
            XMLSerializer serializer = new XMLSerializer();
            serializer.setOutputByteStream(fos);
            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            serializer.setOutputFormat(format);
            serializer.serialize(document);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
    
    /**
     * Write the data files. Not we use scrollable records object to avoid running out of memory.
     * 
     * @param targetDir
     * @param scrollableRecords
     * @param surveySet - output
     * @param cmSet - output
     * @throws IOException
     */
    private void writeDwcDataFiles(File targetDir, ScrollableRecords scrollableRecords) throws IOException {
        File occurrenceFile = new File(targetDir.getAbsolutePath() + "/" + OCCURRENCE_FILE);
        File mofFile = new File(targetDir.getAbsolutePath() + "/" + MEASUREMENT_OR_FACT_FILE);
        
        if (!occurrenceFile.createNewFile()) {
            throw new IOException("Could not create occurence file : " + occurrenceFile.getAbsolutePath());
        }
        
        Writer coreWriter = null;
        Writer mofWriter = null;
        try {
            coreWriter = new FileWriter(occurrenceFile);
            mofWriter = new FileWriter(mofFile);
            
            while (scrollableRecords.hasMoreElements()) {

                Record rec = scrollableRecords.nextElement();
                
                writeCoreValues(coreWriter, rec);

                for (AttributeValue av : rec.getAttributes()) {
                    // only have an entry if the AV value has a meaningful value
                    if (av.isPopulated()) {
                        writeMeasurementOrFactValues(mofWriter, rec, av);
                    }
                }
            }   
        } finally {
            try {
                if (coreWriter != null) {
                    coreWriter.close();
                }
            } catch (IOException ioe) {
                log.error("could not close writer", ioe);
            }
            try {
                if (mofWriter != null) {
                    mofWriter.close();
                }
            } catch (IOException ioe) {
                log.error("could not close writer", ioe);
            }
        }
    }
    
    /**
     * helper method for writing core values
     * 
     * @param writer
     * @param rec
     * @throws IOException
     */
    private void writeCoreValues(Writer writer, Record rec) throws IOException {
        String[] coreFileCsvValues = new String[DWC_TERMS.size()];
        
        int idx = 0;
        
        for (Entry<CorePropertyGetter, ConceptTerm> entry : DWC_TERMS.entrySet()) {
            String value = entry.getKey().getValue(rec);
            coreFileCsvValues[idx] = value != null ? value : "";
            ++idx;
        }
        writer.write(CSVUtils.toCSVString(coreFileCsvValues, FIELDS_TERMINATED_BY_CHAR, '\'', false));
    }
    
    /**
     * helper method for writing MOF values
     * 
     * @param writer
     * @param rec
     * @param av
     * @throws IOException
     */
    private void writeMeasurementOrFactValues(Writer writer, Record rec, AttributeValue av) throws IOException {
        
        // certain attribute value types should never be written
        Attribute a = av.getAttribute();
        if (AttributeType.isHTMLType(a.getType())) {
            // don't write
            return;
        }
        
        String[] mofCsvValues = new String[MOF_EXT_TERMS.size()];
        
        int idx = 0;
        
        for (Entry<MeasurementOrFactExtensionPropertyGetter, ConceptTerm> entry : MOF_EXT_TERMS.entrySet()) {
            String value = entry.getKey().getValue(rec, av);
            mofCsvValues[idx] = value != null ? value : "";
            ++idx;
        }
        writer.write(CSVUtils.toCSVString(mofCsvValues, FIELDS_TERMINATED_BY_CHAR, '\'', false));
    }
    
    /**
     * Create a file section. Can be for the core section or one or more extension sections
     * 
     * @param document - the xml document
     * @param parent - the parent node
     * @param sectionName - will be 'core' or 'extension'
     * @param filename - the file that contains the data for this section
     * @param rowType - the row type. See valid row types for the DwC-A schema.
     * @return The xml element for the section. It will already have been appended to the parent node
     */
    private Element createFileSection(Document document, Element parent, String sectionName, String filename, String rowType) {
        Element section = document.createElement(sectionName);
        
        section.setAttribute("encoding", "utf-8");
        section.setAttribute("fieldsTerminatedBy", FIELDS_TERMINATED_BY);
        section.setAttribute("linesTerminatedBy", LINES_TERMINATED_BY);
        section.setAttribute("fieldsEnclosedBy", "'");
        section.setAttribute("ignoreHeaderLines", "0");
        section.setAttribute("rowType", rowType);
        
        Element files = document.createElement("files");
        Element location = document.createElement("location");
        parent.appendChild(section);
        section.appendChild(files);
        files.appendChild(location);
        location.appendChild(document.createTextNode(filename));
        return section;
    }
    
    /**
     * An interface for retrieving bits of the record object.
     * 
     * @author aaron
     *
     */
    private interface CorePropertyGetter {
        String getValue(Record r);
    }
    
    /**
     * Returns the string passed into the constructor i.e., a fixed value
     * 
     * @author aaron
     *
     */
    private static class CoreFixedGetter implements CorePropertyGetter {
        
        private String value;
        
        public CoreFixedGetter(String value) {
            this.value = value;
        }
        
        @Override
        public String getValue(Record r) {
            return value;
        }
    }
    
    /**
     * Return the string representation of the value specified by
     * the 'path' 
     * 
     * @author aaron
     *
     */
    private static class CorePathGetter implements CorePropertyGetter {
        private String path;
        
        private final DateFormat ISO8601Local = new SimpleDateFormat (ISO_DATE_FORMAT);
        private Logger log = Logger.getLogger(getClass());
        
        public CorePathGetter(String path) {
            this.path = path;
        }
        @Override
        public String getValue(Record r) {
            try {
                Object value = PropertyUtils.getProperty(r, path);
                if (value == null) {
                    return "";
                }
                if (value instanceof Date) {
                    return ISO8601Local.format((Date)value);
                } else {
                    return value.toString();
                }
            } catch (IllegalAccessException iae) {
                log.error("Error while retrieving property", iae);
                return "";
            } catch (InvocationTargetException ite) {
                log.error("Error while retrieving property", ite);
                return "";
            } catch (NoSuchMethodException nsme) {
                log.error("Error while retrieving property", nsme);
                return "";
            }
        }
    }
    
    /**
     * Returns the scientific name. Will use the scientific name with authoring if available.
     * @author aaron
     *
     */
    private static class CoreScientificNameGetter implements CorePropertyGetter {

        @Override
        public String getValue(Record r) {
            if (r.getSpecies() != null) {
                IndicatorSpecies s = r.getSpecies();
                return s.getScientificNameAndAuthor() != null ? s.getScientificNameAndAuthor() : s.getScientificName();
            }
            return "";
        }
    }
    
    /**
     * Returns the lsid of the taxonomic entry. If a Guid exists use it. Else use
     * a BDRS generated LSID.
     * 
     * @author aaron
     *
     */
    private static class CoreTaxonLsidGetter implements CorePropertyGetter {

        private LSIDService lsidService;
        
        public CoreTaxonLsidGetter(LSIDService lsidService) {
            if (lsidService == null) {
                throw new IllegalArgumentException("LSIDService, lsidService, cannot be null");
            }
            this.lsidService = lsidService;
        }
        
        @Override
        public String getValue(Record r) {
            if (r.getSpecies() != null) {
                IndicatorSpecies s = r.getSpecies();
                // no guid, generate our own lsid.
                String lsid = s.getSourceId();
                if (lsid == null) {
                    return lsidService.toLSID(s).toString();
                } else {
                    return lsid;
                }
            }
            return "";
        }
    }
    
    /**
     * Returns the lsid of a survey object
     * @author aaron
     *
     */
    private static class CoreSurveyLsidGetter implements CorePropertyGetter {
        
        private LSIDService lsidService;
        
        public CoreSurveyLsidGetter(LSIDService lsidService) {
            if (lsidService == null) {
                throw new IllegalArgumentException("LSIDService, lsidService, cannot be null");
            }
            this.lsidService = lsidService;
        }
        
        @Override
        public String getValue(Record r) {
            return lsidService.toLSID(r.getSurvey()).toString();
        }
    }
    
    /**
     * Truncates coordinates to the correct number of decimal places.
     * @author aaron
     *
     */
    private static class CoreCoordGetter implements CorePropertyGetter {
        private String path;
        private LocationService locService;
        
        private Logger log = Logger.getLogger(getClass());
        
        public CoreCoordGetter(String path, LocationService locService) {
            if (path == null) {
                throw new IllegalArgumentException("String, path, cannot be null");
            }
            if (locService == null) {
                throw new IllegalArgumentException("LocationService, locService, cannot be null");
            }
            this.path = path;
            this.locService = locService;
        }
        
        @Override
        public String getValue(Record r) {
            try {
                Object value = PropertyUtils.getProperty(r, path);
                if (value == null) {
                    return null;
                }
                if (value instanceof Double) {
                    double truncDouble = this.locService.truncate(((Double) value).doubleValue());
                    return String.format("%f", truncDouble);
                } else {
                    throw new IllegalStateException("coord must be of type double");
                }
            } catch (IllegalAccessException iae) {
                log.error("Error while retrieving property", iae);
                return "";
            } catch (InvocationTargetException ite) {
                log.error("Error while retrieving property", ite);
                return "";
            } catch (NoSuchMethodException nsme) {
                log.error("Error while retrieving property", nsme);
                return "";
            }
        }
    }
    
    private interface MeasurementOrFactExtensionPropertyGetter {
        String getValue(Record r, AttributeValue av);
    }
    
    /**
     * Returns the attribute id NOT the attribute value id
     * 
     * The attribute value ID can change if the attribute value is edited, The attribute
     * will remain constant.
     * 
     * @author aaron
     *
     */
    private static class MeasurementOrFactIdGetter implements MeasurementOrFactExtensionPropertyGetter {

        @Override
        public String getValue(Record r, AttributeValue av) {
            return av.getAttribute().getId().toString();
        }
    }
    
    /**
     * maps the attribute value description field to the Measurement or Fact 'type' field
     * 
     * @author aaron
     *
     */
    private static class MeasurementOrFactTypeGetter implements MeasurementOrFactExtensionPropertyGetter {
        
        @Override
        public String getValue(Record r, AttributeValue av) {
            return av.getAttribute().getDescription();
        }
    }
    
    /**
     * Maps the A.V. 'value' field to the M.O.F. 'value' field
     * 
     * @author aaron
     *
     */
    private static class MeasurementOrFactValueGetter implements MeasurementOrFactExtensionPropertyGetter {
        
        private RedirectionService redirService;
        
        public MeasurementOrFactValueGetter(RedirectionService redirService) {
            if (redirService == null) {
                throw new IllegalArgumentException("RedirectionService, redirService, cannot be null");
            }
            this.redirService = redirService; 
        }
        
        @Override
        public String getValue(Record r, AttributeValue av) {
            
            Attribute a = av.getAttribute();
            switch (a.getType()) {
            case INTEGER:
            case INTEGER_WITH_RANGE:
                return av.getNumericValue() != null ? Integer.toString(av.getNumericValue().intValue()) : "";
                
            case DECIMAL:
                return av.getNumericValue() != null ? av.getNumericValue().toString() : "";
            
            case DATE:
                return av.getDateValue() != null ? av.getDateValue().toString() : "";
            case REGEX:
            case BARCODE:
            case TIME:
            case STRING:
            case STRING_AUTOCOMPLETE:
            case TEXT:
            case STRING_WITH_VALID_VALUES:
            case MULTI_CHECKBOX:
            case MULTI_SELECT:
            case SINGLE_CHECKBOX:
                return av.getStringValue() != null ? av.getStringValue() : "";

            case IMAGE:
            case FILE:
                // returns the full URL 
                return redirService.getFileDownloadUrl(av, true);
                
            case HTML:
            case HTML_NO_VALIDATION:
            case HTML_COMMENT:
            case HTML_HORIZONTAL_RULE:
            default:
                throw new IllegalStateException("Type not handled : " + a.getTypeCode());
            }
        }
    }
    
    /**
     * Maps the census method name that owns the attribute to the M.O.F. 'method' field
     * 
     * @author aaron
     *
     */
    private static class MeasurementOrFactMethodGetter implements MeasurementOrFactExtensionPropertyGetter {

        @Override
        public String getValue(Record r, AttributeValue av) {
            return r.getCensusMethod() != null ? r.getCensusMethod().getName() : "";
        }
        
    }
    
    /**
     * Adds the survey and census method names to the M.O.F.' remarks field.
     * 
     * @author aaron
     *
     */
    private static class MeasurementOrFactRemarksGetter implements MeasurementOrFactExtensionPropertyGetter {

        @Override
        public String getValue(Record r, AttributeValue av) {
            StringBuilder sb = new StringBuilder();
            sb.append("survey: ");
            sb.append(r.getSurvey().getName());
            if (r.getCensusMethod() != null) {
                sb.append(", census method: ");
                sb.append(r.getCensusMethod().getName());
            }
            return sb.toString();
        }
    }
    
    /**
     * Maps the record id to the M.O.F. 'core id' field. Used to link the M.O.F. row back to a 
     * core field row.
     * 
     * @author aaron
     *
     */
    private static class MeasurementOrFactCoreIdGetter implements MeasurementOrFactExtensionPropertyGetter {
        
        @Override
        public String getValue(Record r, AttributeValue av) {
            return r.getId().toString();
        }
    }
}
