package au.com.gaiaresources.bdrs.controller.record;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormField;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormFieldFactory;
import au.com.gaiaresources.bdrs.controller.insecure.taxa.ComparePersistentImplByWeight;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationNameComparator;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfileDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.taxa.TaxonRank;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.image.ImageService;
import edu.emory.mathcs.backport.java.util.Collections;

@Controller
public class AtlasController extends AbstractController {

    private Logger log = Logger.getLogger(getClass());
    
    public static final String TAXON_GROUP_ATTRIBUTE_PREFIX = "taxonGroupAttr_";
    public static final String CENSUS_METHOD_ATTRIBUTE_PREFIX = "censusMethodAttr_";
    
    // aka taxonomic record property names
    static final String[] RECORD_PROPERTY_NAMES = new String[] {
            Record.RECORD_PROPERTY_SPECIES, Record.RECORD_PROPERTY_NUMBER,
            Record.RECORD_PROPERTY_WHEN, Record.RECORD_PROPERTY_TIME,
            Record.RECORD_PROPERTY_NOTES, Record.RECORD_PROPERTY_LOCATION,
            Record.RECORD_PROPERTY_POINT, Record.RECORD_PROPERTY_ACCURACY};

    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private LocationDAO locationDAO;
    @Autowired
    private PreferenceDAO preferenceDAO;
    @Autowired
    private SpeciesProfileDAO speciesProfileDAO;
    @Autowired
    private ManagedFileDAO managedFileDAO;
    @Autowired
    private FileService fileService;
    @Autowired
    private ImageService imageService;
    @Autowired 
    private MetadataDAO metadataDAO;
    

    private FormFieldFactory formFieldFactory = new FormFieldFactory();

    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = "/bdrs/user/atlas.htm", method = RequestMethod.GET)
    public ModelAndView addRecord(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "surveyId", required = true) int surveyId,
            @RequestParam(value = "taxonSearch", required = false) String taxonSearch,
            @RequestParam(value = "recordId", required = false, defaultValue = "0") int recordId,
            @RequestParam(value = "guid", required = false) String guid) {
        
        Survey survey = surveyDAO.getSurvey(surveyId);
        Record record = recordDAO.getRecord(recordId);
        
        record = record == null ? new Record() : record;
        
        IndicatorSpecies species = null;
        
        if (guid != null && !guid.isEmpty()) {
            species = taxaDAO.getIndicatorSpeciesByGuid(guid);
        } 
        if (species == null && taxonSearch != null && !taxonSearch.isEmpty()) {
            List<IndicatorSpecies> speciesList = surveyDAO.getSpeciesForSurveySearch(surveyId, taxonSearch);
            if (speciesList.isEmpty()) {
                species = null;
            } else if (speciesList.size() == 1) {
                species = speciesList.get(0);
            } else {
                log.warn("Multiple species found for survey " + surveyId
                        + " and taxon search \"" + taxonSearch
                        + "\". Using the first.");
                species = speciesList.get(0);
            }
        }
        if(species == null && record.getSpecies() != null) {
            species = record.getSpecies();
        }

        ModelAndView mv;
        if(species == null) {
	        InputStreamReader reader = null;
        	try {
        		Preference p = preferenceDAO.getPreferenceByKey("ala.species.short.url");
        		URL url;
        		if (p != null) {
        			url = new URL(p.getValue() + "/" + guid + ".json");
        		} else {
        			url = new URL("http://bie.ala.org.au/species/shortProfile/" + guid + ".json"); // fallback to the BIE
        		}
        		
        		URLConnection conn = url.openConnection();
        		reader = new InputStreamReader(conn.getInputStream());
        		StringBuffer buff = new StringBuffer();
        		int c;
        		while ((c = reader.read()) != -1) {
        			buff.append((char)c);
        		}
        		JSONObject ob = JSONObject.fromObject(buff.toString());
        		IndicatorSpecies taxon = new IndicatorSpecies();
                
        		log.debug("Found species information from the atlas : ");
            	log.debug(buff.toString());
            	// Scientific Name
            	taxon.setScientificName(ob.getString("scientificName"));
            	taxon.setAuthor(ob.getString("author"));
            	taxon.setScientificNameAndAuthor(ob.getString("scientificName") + " " + ob.getString("scientificNameAuthorship"));
            	taxon.setYear(ob.getString("year"));
            	Metadata md = new Metadata();
            	md.setKey(Metadata.SCIENTIFIC_NAME_SOURCE_DATA_ID);
            	md.setValue(guid);
	            metadataDAO.save(md);
	            taxon.getMetadata().add(md);
	            
            	// Rank
            	TaxonRank rank = TaxonRank.findByIdentifier(ob.getString("rank"));
            	taxon.setTaxonRank(rank);
            	
            	// Common Name
            	taxon.setCommonName(ob.getString("commonName"));
            	md = new Metadata();
                md.setKey(Metadata.COMMON_NAME_SOURCE_DATA_ID);
                md.setValue(ob.getString("commonNameGUID"));
                metadataDAO.save(md);
                taxon.getMetadata().add(md);
                
            	// Group
            	String family = ob.getString("family");
            	if (family != null) {
            		TaxonGroup g = taxaDAO.getTaxonGroup(family);
	            	if (g == null) {
	            		g = taxaDAO.createTaxonGroup(family, false, false, false, false, false, true);
	            	}
	            	taxon.setTaxonGroup(g);
            	} else {
            		TaxonGroup g = taxaDAO.createTaxonGroup("Other", false, false, false, false, false, true);
            		taxon.setTaxonGroup(g);
            	}

                // Images.
            	// Thumbnail
            	if (ob.containsKey("thumbnail")) {
	            	SpeciesProfile sp = new SpeciesProfile();
	                sp.setType(SpeciesProfile.SPECIES_PROFILE_THUMBNAIL); // this is a 100x100 image, might resize for the other ones.
	                sp.setHeader("Thumbnail");
	                sp.setDescription("Thumbnail for " + taxon.getScientificName());
	                String filename = ob.getString("thumbnail");
	                String ext = getExtension(filename);
	                ManagedFile mf = new ManagedFile();
	                mf.setContentType("");
	                mf.setCredit("");
	                mf.setLicense("");
	                mf.setDescription(taxon.getScientificName() + " - " + taxon.getCommonName());
	                mf.setFilename(mf.getUuid()+ext);
	                mf = managedFileDAO.save(mf);
	                fileService.createFile(mf.getClass(), mf.getId(), mf.getFilename(), downloadFile(new URL(filename)));
	                mf.setContentType(fileService.getFile(mf, mf.getFilename()).getContentType());
	                managedFileDAO.save(mf);
	                sp.setContent(mf.getUuid());
	                speciesProfileDAO.save(sp);
	            	taxon.getInfoItems().add(sp);
	            	
	            	// Now thumbnail the thumbnail...
	            	BufferedImage fortyXforty = 
	            		imageService.resizeImage(fileService.getFile(mf, mf.getFilename()).getInputStream(), 40, 40);
	            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            	ImageIO.write(fortyXforty, "png", baos);
	                baos.flush();
	                byte[] data = baos.toByteArray();
	                baos.close();
	                
	            	sp = new SpeciesProfile();
	                sp.setType(SpeciesProfile.SPECIES_PROFILE_IMAGE_40x40); // this is a 100x100 image, might resize for the other ones.
	                sp.setHeader("40x40 Thumbnail");
	                sp.setDescription("40x40 Thumbnail for " + taxon.getScientificName());
	                
	                mf = new ManagedFile();
	                mf.setContentType("");
	                mf.setCredit("");
	                mf.setLicense("");
	                mf.setDescription(taxon.getScientificName() + " - " + taxon.getCommonName());
	                mf.setFilename(mf.getUuid()+".png");
	                mf = managedFileDAO.save(mf);
	                fileService.createFile(mf.getClass(), mf.getId(), mf.getFilename(), data);
	                mf.setContentType(fileService.getFile(mf, mf.getFilename()).getContentType());
	                managedFileDAO.save(mf);
	                sp.setContent(mf.getUuid());
	                speciesProfileDAO.save(sp);
	            	taxon.getInfoItems().add(sp);
            	}
            	
            	// Main Image
            	if (ob.containsKey("imageURL")) {
	            	SpeciesProfile sp = new SpeciesProfile();
	                sp.setType(SpeciesProfile.SPECIES_PROFILE_IMAGE); 
	                sp.setHeader("Image");
	                sp.setDescription("Image for " + taxon.getScientificName());
	                String filename = ob.getString("imageURL");
	                String ext = getExtension(filename);
	                ManagedFile mf = new ManagedFile();
	                mf.setContentType("");
	                mf.setCredit("");
	                mf.setLicense("");
	                mf.setDescription(taxon.getScientificName() + " - " + taxon.getCommonName());
	                mf.setFilename(mf.getUuid()+ext);
	                mf = managedFileDAO.save(mf);
	                fileService.createFile(mf.getClass(), mf.getId(), mf.getFilename(), downloadFile(new URL(filename)));
	                mf.setContentType(fileService.getFile(mf, mf.getFilename()).getContentType());
	                managedFileDAO.save(mf);
	                sp.setContent(mf.getUuid());
	                speciesProfileDAO.save(sp);
	            	taxon.getInfoItems().add(sp);
            	}
            	
            	// Save the Taxon
            	species = taxaDAO.save(taxon); 
        	} catch (IOException ioe) {
        		log.error("Could not retrieve species profile from the Atlas", ioe);
        		species = null;
        	} finally {
        		if (reader != null) {
        			try {
	        			reader.close();
	        		} catch (IOException ioe) {
	        			log.error("Error closing stream from Atlas webservice, possible network error",
	        				ioe);
	        		}
        		}
        	}
        	
        }
        
        if (species == null) {
        	log.debug("Could not determine species, reverting to tracker form");
            // The atlas form relies upon a preconfigured species.
            // If we do not have one, fall back to the tracker form.
            mv = new ModelAndView(new RedirectView("tracker.htm"));
            mv.addAllObjects(request.getParameterMap());
            mv.addObject("surveyId", surveyId);
            return mv;
        } else {
            // Add all attribute form fields
            Map<String, FormField> formFieldMap = new HashMap<String, FormField>();
    
            // Add all property form fields
            for (String propertyName : RECORD_PROPERTY_NAMES) {
                formFieldMap.put(propertyName, 
                                       formFieldFactory.createRecordFormField(survey, record, propertyName, species, Taxonomic.TAXONOMIC));
            }
            
            // Determine the file attribute to use for the form (if there is one)
            // Sort the list of survey attributes by weight so that we can
            // correctly select the first file attribute.
            List<Attribute> attributeList = survey.getAttributes();
            Collections.sort(attributeList, new ComparePersistentImplByWeight());

            // Retrieve the first file attribute and if present, the associated
            // record attribute.
            Attribute fileAttr = null;
            AttributeValue fileRecAttr = null;
            for(Attribute attr : attributeList) {
                if(fileAttr == null && AttributeType.FILE.equals(attr.getType())) {
                    // Attribute found.
                    fileAttr = attr;
                    // Try to locate matching record attribute
                    for(AttributeValue recAttr : record.getAttributes()) {
                        if(fileRecAttr == null && fileAttr.equals(recAttr.getAttribute())) {
                            fileRecAttr = recAttr;
                        }
                    }
                }
            }
            
            // Map all the existing file attributes to record attributes.
            Map<Attribute, AttributeValue> fileAttrToRecAttrMap = 
                new HashMap<Attribute, AttributeValue>();
            
            
            FormField fileFormField = formFieldFactory.createRecordFormField(survey, record, fileAttr, fileRecAttr);
            
            Map<String, String> errorMap = (Map<String, String>)getRequestContext().getSessionAttribute("errorMap");
            getRequestContext().removeSessionAttribute("errorMap");
            Map<String, String> valueMap = (Map<String, String>)getRequestContext().getSessionAttribute("valueMap");
            getRequestContext().removeSessionAttribute("valueMap");
            
            Metadata predefinedLocationsMD = survey.getMetadataByKey(Metadata.PREDEFINED_LOCATIONS_ONLY);
            boolean predefinedLocationsOnly = predefinedLocationsMD != null && 
                Boolean.parseBoolean(predefinedLocationsMD.getValue());
            
            Set<Location> locations = new TreeSet<Location>(new LocationNameComparator());
            locations.addAll(survey.getLocations());
            if(!predefinedLocationsOnly) {
                locations.addAll(locationDAO.getUserLocations(getRequestContext().getUser()));
            }
            
            Metadata defaultLocId = getRequestContext().getUser().getMetadataObj(Metadata.DEFAULT_LOCATION_ID);
            Location defaultLocation;
            if(defaultLocId == null) {
                defaultLocation = null;
            } else {
                int defaultLocPk = Integer.parseInt(defaultLocId.getValue());
                defaultLocation = locationDAO.getLocation(defaultLocPk);
            }
            
            mv = new ModelAndView("atlas");
            mv.addObject("record", record);
            mv.addObject("taxon", species);
            mv.addObject("survey", survey);
            mv.addObject("locations", locations);
            mv.addObject("formFieldMap", formFieldMap);
            mv.addObject("fileFormField", fileFormField);
            mv.addObject("preview", request.getParameter("preview") != null);
            mv.addObject("defaultLocation", defaultLocation);
            
            mv.addObject("errorMap", errorMap);
            mv.addObject("valueMap", valueMap);
        }
        
        return mv;
    }
    
    private byte[] downloadFile(URL url) throws IOException {
    	BufferedInputStream bis = null;
    	try {
	    	URLConnection conn = url.openConnection();
			bis = new BufferedInputStream(conn.getInputStream());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[512];
			int count;
			while ((count = bis.read(b)) != -1) {
				bos.write(b, 0, count);
			}
			return bos.toByteArray();
		} catch (IOException ioe) {
			throw ioe; //propagate the exception up.
		} finally {
			if (bis != null) {
				bis.close(); // cleanup streams.
			}
		}
    }
    
    private String getExtension(String filename) {
    	if (filename.indexOf('.') > -1) {
        	return filename.substring(filename.lastIndexOf('.'));
        } else {
        	return filename;
        }
    }
}