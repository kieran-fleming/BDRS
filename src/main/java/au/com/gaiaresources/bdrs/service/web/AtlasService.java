/**
 * 
 */
package au.com.gaiaresources.bdrs.service.web;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.imageio.ImageIO;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfileDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.taxa.TaxonRank;
import au.com.gaiaresources.bdrs.util.ImageUtil;
import au.com.gaiaresources.bdrs.util.StringUtils;

/**
 * This service will import a species profile or set of species profiles from 
 * the atlas site at http://bie.ala.org.au into the BDRS database.
 */
@Component
public class AtlasService {

    public static final String SPECIES_PROFILE_SOURCE = "ALA";
    public static final String SPECIES_PROFILE_IMAGE = "Image";
    public static final String SPECIES_PROFILE_THUMB = "Thumbnail";
    public static final String SPECIES_PROFILE_THUMB_40 = "40x40 Thumbnail";
    public static final String SPECIES_PROFILE_HABITAT = "Habitat";
    public static final String SPECIES_PROFILE_COMMON_NAME = "Common Name";
    public static final String SPECIES_PROFILE_SYNONYM = "Synonym";
    public static final String SPECIES_PROFILE_CONSERVATION_STATUS = "Conservation Status";
    public static final String SPECIES_PROFILE_IDENTIFIER = "Identifier";
    public static final String SPECIES_PROFILE_IMPORT_LIFE = "Life";
    
    public static final String KEY_GROUP_IMPORT_TYPE = "ala.species.group.by";
    public static final String KEY_ALA_SPECIES_LONG_URL = "ala.species.url";
    public static final String KEY_ALA_SPECIES_SHORT_URL = "ala.species.short.url";
    public static final String PREF_FAMILY = "family";
    public static final String PREF_KINGDOM = "kingdom";
    
    Logger log = Logger.getLogger(getClass());
    @Autowired
    TaxaDAO taxaDAO;
    @Autowired
    MetadataDAO metadataDAO;
    @Autowired
    ManagedFileDAO managedFileDAO;
    @Autowired
    SpeciesProfileDAO speciesProfileDAO;

    @Autowired
    FileService fileService;
    @Autowired
    PreferenceDAO preferenceDAO;
    
    /**
     * Import a species profile from the ALA.
     * @param guid GUID identifier for Atlas
     * @param shortProfile Flag to indicate whether to import the full profile or the short one
     * @param errorMap
     * @param group the Taxon Group that the species should be imported into.
     * @return the IndicatorSpecies object resulting from the import
     */
    public IndicatorSpecies importSpecies(String guid, boolean shortProfile, Map<String, String> errorMap, String group) {
        // see if the species already exists
        IndicatorSpecies species = taxaDAO.getIndicatorSpeciesByGuid(guid);
        if (species == null) {
            species = new IndicatorSpecies();
        } else {
            log.warn("Species profile for guid (" + guid + ") already exists, overwriting.");
        }
        return importSpecies(species, guid, shortProfile, errorMap, group);
    }
    
    /**
     * Import a species profile from the ALA.
     * @param species An existing taxonomy to add the import to
     * @param shortProfile Flag to indicate whether to import the full profile or the short one
     * @param errorMap
     * @param group the taxon group that the species should be imported into
     * @return the IndicatorSpecies object resulting from the import
     */
    public IndicatorSpecies importSpecies(IndicatorSpecies species, boolean shortProfile, Map<String, String> errorMap, String group) {
        return importSpecies(species, species.getGuid(), shortProfile, errorMap, group);
    }
    
    /**
     * Import a species profile from the ALA.
     * @param species An existing taxonomy to add the import to
     * @param guid GUID identifier for Atlas
     * @param shortProfile Flag to indicate whether to import the full profile or the short one
     * @param errorMap
     * @param group The group that the species should be imported into.
     * @return the IndicatorSpecies object resulting from the import
     */
    public IndicatorSpecies importSpecies(IndicatorSpecies species, String guid, boolean shortProfile, Map<String, String> errorMap, String group) {
        try {
            if (species == null) {
                species = new IndicatorSpecies();
            } 
            if (StringUtils.nullOrEmpty(guid) || "null".equals(guid)) {
                // get the guid from the species if none is specified
                guid = species.getGuid();
            }
            
            JSONObject ob = getJSONObject(guid, shortProfile);
            if (ob == null) {
            	errorMap.put("", "Could not retrieve species profile from the Atlas for guid " + guid);
            	return null;
            }
            
            if (shortProfile) {
                species = createShortProfile(species, ob, guid, group);
            } else {
                species = createFullProfile(species, ob, guid, group);
            }
            
            species = createTaxonMetadata(species, Metadata.TAXON_SOURCE, SPECIES_PROFILE_SOURCE);
            species = createTaxonMetadata(species, Metadata.TAXON_SOURCE_DATA_ID, guid);
            
            if (species != null) {
            	if (species.getScientificName() != null) {
            		// viable species, make sure the common name and sci/author are set.
            		if (species.getCommonName() == null) {
            			species.setCommonName(species.getScientificName());
            		}
            		if (species.getScientificNameAndAuthor() == null) {
            			species.setScientificNameAndAuthor(species.getScientificName());
            		}
            		taxaDAO.save(species);
            	} else {
            		log.error("Species did not have a name.");
                    if (errorMap != null) {
                        errorMap.put("", "Could not retrieve species profile from the Atlas for guid " + guid);
                    }
                    species = null;
            	}
            }
            
        } catch (IOException ioe) {
            log.error("Could not retrieve species profile from the Atlas", ioe);
            if (errorMap != null) {
                errorMap.put("", "Could not retrieve species profile from the Atlas for guid " + guid);
            }
            species = null;
        }
        
        return species;
    }

    /**
     * Retrieves a JSON Object from the ALA for the specified guid.
     * @param guid The guid to look up.
     * @param shortProfile Flag indicating whether to retrieve the full or short profile.
     * @return A JSONObject from the ALA.
     * @throws IOException
     */
    public JSONObject getJSONObject(String guid, boolean shortProfile) throws IOException {
        InputStreamReader reader = null;
        try {
            URL url = null;
            // try to set the url by preference first
            Preference preference = null;
            if (shortProfile) {
                preference = preferenceDAO.getPreferenceByKey(KEY_ALA_SPECIES_SHORT_URL);
                // if it is a short profile, but there is no short.url, try to get the
                // .url preference instead
                if (preference == null) {
                    preference = preferenceDAO.getPreferenceByKey(KEY_ALA_SPECIES_LONG_URL);
                    if (preference != null) {
                        url = new URL(preference.getValue() + "shortProfile/" + guid + ".json");
                    }
                }
            } else {
                preference = preferenceDAO.getPreferenceByKey(KEY_ALA_SPECIES_LONG_URL);
            }
            
            if (preference != null && url == null) {
                url = new URL(preference.getValue() + "/" + guid + ".json");
            }
            
            // if there is no preference set for ala.species.*.url,
            // fall back to the BIE
            if (url == null) {
                url = new URL("http://bie.ala.org.au/species/" + 
                              (shortProfile ? "shortProfile/" : "") + guid + ".json"); 
            }
            
            log.info("importing " + (shortProfile ? "short" : "full") + " profile from "+url);
            
            URLConnection conn = url.openConnection();
            reader = new InputStreamReader(conn.getInputStream());
            StringBuffer buff = new StringBuffer();
            int c;
            while ((c = reader.read()) != -1) {
                    buff.append((char)c);
            }
            JSONObject ob = null;
            try {
            	 ob = JSONObject.fromObject(buff.toString());
            } catch (JSONException jse) {
            	log.warn("Unable to pull out JSON profile for ALA taxa : " + guid);
            }
            return ob;
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

    /**
     * Creates/Modifies an object of type IndicatorSpecies from the json object passed.
     * The object is not persisted in this method.
     * @param taxon The existing taxon to modify
     * @param ob The json object representing the IndicatorSpecies
     * @param guid The guid descriptor for the species
     * @param group the Taxon Group that the species should be imported into
     * @return A newly created/modified IndicatorSpecies representing the json object passed.
     * @throws MalformedURLException
     * @throws IOException
     */
    public IndicatorSpecies createFullProfile(IndicatorSpecies taxon, JSONObject ob, String guid, String group) throws MalformedURLException, IOException {
        JSONObject taxonObj = ob.getJSONObject("taxonConcept");
        List<SpeciesProfile> infoItems = taxon.getInfoItems();
        // remove any previous ALA import items
        removeALAInfoItems(infoItems);
        
        // Scientific Name
        String scientificName = taxonObj.getString("nameString");
        // Author and Year
        String authorYear = taxonObj.getString("author");
        String author = "", year = "";
        if (authorYear != null) {
            String[] split = authorYear.split(",");
            author = split[0] != null && split[0].startsWith("(") ? split[0].substring(1) : split[0];
            year = split.length > 1 ? (split[1] != null && split[1].endsWith(")") ? 
                    trimString(split[1].substring(0, split[1].length()-1)) : trimString(split[1])) : "";
        }
        taxon = setScientificName(taxon, guid, scientificName, author, year);

        // Rank
        taxon = setRank(taxon, taxonObj.getString("rankString"));
        
        // Common Name
        String commonName = scientificName;
        String commonGuid = "";
        if (ob.containsKey("commonNames")) {
            JSONArray commonArr = ob.getJSONArray("commonNames");
            ListIterator commonIter = commonArr.listIterator();
            boolean isPreferred = false;
            // find the common name with a guid or preferred value true
            while (commonIter.hasNext()) {
                JSONObject commonObj = (JSONObject) commonIter.next();
                
                commonName = commonObj.getString("nameString");
                commonGuid = commonObj.getString("guid");
                isPreferred = commonObj.containsKey("preferred") ? 
                        commonObj.getBoolean("preferred") : 
                            commonObj.containsKey("isPreferred") ? 
                                    commonObj.getBoolean("isPreferred") : false;
                // set the common name to the first one in case of the 
                // absence of a preferred entry
                if (taxon.getCommonName() == null) {
                    taxon = setCommonName(taxon, commonName, commonGuid);
                }
                if (!isPreferred) {
                    String infoSource = commonObj.getString("infoSourceName");
                    addProfileInfoItem(infoItems, SpeciesProfile.SPECIES_PROFILE_COMMONNAME, 
                                               SPECIES_PROFILE_COMMON_NAME, 
                                               SPECIES_PROFILE_COMMON_NAME, 
                                               commonName, infoSource, true);
                } else {
                    taxon = setCommonName(taxon, commonName, commonGuid);
                }
            }
        } else {
            // set the common name to the scientific name if none are specified
            taxon = setCommonName(taxon, scientificName, guid);
        }
        

        // Classification
        String family = null, kingdom = null;
        if (ob.containsKey("classification")) {
            JSONObject classObj = ob.getJSONObject("classification");
            // Group/Family
            if (classObj.containsKey("family")) {
                family = classObj.getString("family");
            }
            
            // Kingdom
            if (classObj.containsKey("kingdom")) {
                kingdom = classObj.getString("kingdom");
            }
        }
        
        taxon = setClassification(taxon, family, kingdom, group);
        
        // Images.
        if (ob.containsKey("images")) {
            JSONArray imgArr = ob.getJSONArray("images");
            ListIterator imgIter = imgArr.listIterator();
            // Check to see if there's any images, if so, use the first.
            if (imgIter.hasNext()) {
                // just use the first image as there is no preferred/default setting
                // stating which image to use
                JSONObject imageObj = (JSONObject) imgIter.next();
                String description = imageObj.getString("description");
                
                String filename = "", 
                       type = "",
                       header = "", 
                       profileItemDescription = "", 
                       managedFileDescription = "",
                       imageDescription = 
                           !StringUtils.nullOrEmpty(description) && !"null".equalsIgnoreCase(description) ? 
                               description : "", 
                       contentType = imageObj.containsKey("contentType") ? 
                               !StringUtils.nullOrEmpty(imageObj.getString("contentType")) ? 
                                       imageObj.getString("contentType") : "" : "",
                       credit = imageObj.containsKey("creator") ? 
                               !StringUtils.nullOrEmpty(imageObj.getString("creator")) ? 
                                       imageObj.getString("creator") : "" : "", 
                       license = imageObj.containsKey("licence") ? 
                               !StringUtils.nullOrEmpty(imageObj.getString("licence")) ? 
                                       imageObj.getString("licence") : "" : "",
                       imageSource = imageObj.getString("infoSourceName"),
                       docId = imageObj.containsKey("documentId") ? 
                               !StringUtils.nullOrEmpty(imageObj.getString("documentId")) && 
                               !"null".equals(imageObj.getString("documentId")) ?
                                       imageObj.getString("documentId") : "" : "";
                // Main Image
                if (imageObj.containsKey("repoLocation")) {
                    filename = imageObj.getString("repoLocation");
                    type = SpeciesProfile.SPECIES_PROFILE_IMAGE;
                    header = SPECIES_PROFILE_IMAGE;
                    profileItemDescription = SPECIES_PROFILE_IMAGE + " for " + taxon.getScientificName();
                    managedFileDescription = SPECIES_PROFILE_IMAGE + 
                        (!StringUtils.nullOrEmpty(docId) ? (" " + docId) : "") + 
                        " for " + taxon.getScientificName();
                    ManagedFile mf = createManagedFile(getExtension(filename), contentType, credit, license, 
                                                       profileItemDescription, imageDescription, downloadFile(new URL(filename)));
                    createTaxonImage(infoItems, type, header, profileItemDescription, mf, imageSource);
                }
                
                // Thumbnail
                if (imageObj.containsKey("thumbnail")) {
                    filename = imageObj.getString("thumbnail");
                    type = SpeciesProfile.SPECIES_PROFILE_THUMBNAIL;
                    header = SPECIES_PROFILE_THUMB;
                    managedFileDescription = SPECIES_PROFILE_THUMB + 
                        (!StringUtils.nullOrEmpty(docId) ? (" " + docId) : "") + 
                        " for " + scientificName;
                    profileItemDescription = SPECIES_PROFILE_THUMB + " for " + scientificName; 
                    ManagedFile mf = createManagedFile(getExtension(filename), contentType, credit, 
                                                       license, managedFileDescription, 
                                                       imageDescription, 
                                                       downloadFile(new URL(filename)));
                    createTaxonImage(infoItems, type, header, profileItemDescription, mf, imageSource);
                    
                    // Now thumbnail the thumbnail...
                    BufferedImage fortyXforty = 
                        ImageUtil.resizeImage(fileService.getFile(mf, mf.getFilename()).getInputStream(), 40, 40);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(fortyXforty, "png", baos);
                    baos.flush();
                    byte[] data = baos.toByteArray();
                    baos.close();
                    
                    type = SpeciesProfile.SPECIES_PROFILE_IMAGE_40x40;
                    header = SPECIES_PROFILE_THUMB_40;
                    managedFileDescription = SPECIES_PROFILE_THUMB_40 + 
                        (!StringUtils.nullOrEmpty(docId) ? (" " + docId) : "") + 
                        " for " + taxon.getScientificName();
                    profileItemDescription = SPECIES_PROFILE_THUMB_40 + " for " + taxon.getScientificName();
                    mf = createManagedFile(".png", contentType, credit, license, managedFileDescription, imageDescription, data);
                    
                    createTaxonImage(infoItems, type, header, profileItemDescription, mf, imageSource);
                }
            }
        }
        
        // simple properties
        if (ob.containsKey("simpleProperties")) {
            JSONArray simpleProps = ob.getJSONArray("simpleProperties");
            ListIterator simpleIter = simpleProps.listIterator();
            // find the common name with a guid or preferred value true
            String lastHeader = "";
            while (simpleIter.hasNext()) {
                JSONObject simpleProp = (JSONObject) simpleIter.next();
                
                String name = simpleProp.getString("name");
                int index = name.lastIndexOf("/") + 1;
                String header = name.substring(index);
                if (header.contains("#has")) {
                    // remove the #has
                    header = header.substring(header.indexOf("#has")+4);
                }
                // only add one of each simple property type and overwrite the existing one
                if (!header.equals(lastHeader)) {
                    addProfileInfoItem(infoItems, SpeciesProfile.SPECIES_PROFILE_TEXT, 
                                               header, header, simpleProp.getString("value"), 
                                               simpleProp.getString("infoSourceName"), true);
                    lastHeader = header;
                }
            }
        }
        
        // synonyms as scientific names here
        if (ob.containsKey("synonyms")) {
            JSONArray synonyms = ob.getJSONArray("synonyms");
            ListIterator synoIter = synonyms.listIterator();
            // find the common name with a guid or preferred value true
            while (synoIter.hasNext()) {
                JSONObject synonym = (JSONObject) synoIter.next();
                String syn = synonym.getString("nameString");
                String source = synonym.getString("author");
                if (StringUtils.nullOrEmpty(source)) {
                    // get the source from the synonym name
                    int index = syn.indexOf(",");
                    if (index > 0) {
                        String tmpAuthor = syn.substring(0, index);
                        source = tmpAuthor.substring(tmpAuthor.lastIndexOf(" ")) + syn.substring(index);
                    }
                }
                if (syn.contains(source)) {
                    syn = syn.substring(0, syn.indexOf(source));
                } 
                addProfileInfoItem(infoItems, SpeciesProfile.SPECIES_PROFILE_SCIENTIFICNAME, 
                                           SPECIES_PROFILE_SYNONYM, 
                                           SPECIES_PROFILE_SYNONYM, 
                                           syn, source);
            }
        }
        
        // conservation status
        if (ob.containsKey("conservationStatuses")) {
            JSONArray conservationStatus = ob.getJSONArray("conservationStatuses");
            ListIterator csIter = conservationStatus.listIterator();
            // find the common name with a guid or preferred value true
            while (csIter.hasNext()) {
                JSONObject conservStatus = (JSONObject) csIter.next();
                String id = conservStatus.getString("infoSourceId");
                addProfileInfoItem(infoItems, SpeciesProfile.SPECIES_PROFILE_STATUS, 
                                           SPECIES_PROFILE_CONSERVATION_STATUS, 
                                           SPECIES_PROFILE_CONSERVATION_STATUS, 
                                           (!StringUtils.nullOrEmpty(conservStatus.getString("status")) && 
                                                   !"null".equalsIgnoreCase(conservStatus.getString("status")) ? 
                                                   conservStatus.getString("status") : "") + 
                                           (!StringUtils.nullOrEmpty(conservStatus.getString("region")) && 
                                                   !"null".equalsIgnoreCase(conservStatus.getString("region")) ? 
                                                   " in " + conservStatus.getString("region") : ""),
                                           conservStatus.getString("infoSourceName"));
            }
        }
        
        // habitat
        if (ob.containsKey("habitats")) {
            JSONArray arr = ob.getJSONArray("habitats");
            ListIterator iter = arr.listIterator();
            // find the common name with a guid or preferred value true
            while (iter.hasNext()) {
                JSONObject habitat = (JSONObject) iter.next();
                addProfileInfoItem(infoItems, SpeciesProfile.SPECIES_PROFILE_TEXT, 
                                           SPECIES_PROFILE_HABITAT, 
                                           SPECIES_PROFILE_HABITAT, 
                                           habitat.getString("statusAsString"),
                                           habitat.getString("infoSourceName"));
            }
        }
        
        // identifiers
        if (ob.containsKey("identifiers")) {
            JSONArray arr = ob.getJSONArray("identifiers");
            ListIterator iter = arr.listIterator();
            // find the common name with a guid or preferred value true
            while (iter.hasNext()) {
                String id = (String) iter.next();
                addProfileInfoItem(infoItems, SpeciesProfile.SPECIES_PROFILE_IDENTIFIER, 
                                           SPECIES_PROFILE_IDENTIFIER, SPECIES_PROFILE_IDENTIFIER, id, 
                                           SPECIES_PROFILE_SOURCE);
            }
        }
        
        // update the info items for the taxon
        taxon.setInfoItems(infoItems);
        
        return taxon; 
    }
    
    /**
     * Removes entries from the species profile information items that begin with 
     * the constant <code>SPECIES_PROFILE_SOURCE</code>.  This constant is inserted 
     * into the item headers when the values are imported via this class.
     * @param infoItems The list of SpeciesProfile items to remove values from
     */
    private void removeALAInfoItems(List<SpeciesProfile> infoItems) {
        List<SpeciesProfile> removeItems = new ArrayList<SpeciesProfile>();
        for (SpeciesProfile speciesProfile : infoItems) {
            if (speciesProfile.getHeader().startsWith(SPECIES_PROFILE_SOURCE)) {
                removeItems.add(speciesProfile);
            }
        }
        infoItems.removeAll(removeItems);
    }

    /**
     * Helper method to create a ManagedFile from an entry in the JSON import.
     * @param ext The file type extension
     * @param contentType The content type of the file
     * @param credit The credit for the file
     * @param license The license of the file
     * @param fileDescription The description of the file
     * @param fileName
     * @param fileContent The byte array contents of the file
     * @return The newly created managed file
     * @throws IOException
     */
    private ManagedFile createManagedFile(String ext, String contentType, 
            String credit, String license, String fileName, String fileDescription, byte[] fileContent) throws IOException {
        ManagedFile mf = managedFileDAO.getManagedFileByName(SPECIES_PROFILE_SOURCE + " " + fileName + ext);
        if (mf == null) {
            mf = new ManagedFile();
        }
        if (StringUtils.nullOrEmpty(contentType) || "null".equals(trimString(contentType))) {
            contentType = "";
        }
        mf.setContentType(trimString(contentType));
        if (StringUtils.nullOrEmpty(credit) || "null".equals(trimString(credit))) {
            credit = "";
        }
        mf.setCredit(trimString(credit));
        if (StringUtils.nullOrEmpty(license) || "null".equals(trimString(license))) {
            license = "";
        }
        mf.setLicense(trimString(license));
        if (StringUtils.nullOrEmpty(fileDescription) || "null".equals(trimString(fileDescription))) {
            fileDescription = SPECIES_PROFILE_SOURCE + " " + fileName;
        }
        mf.setDescription(trimString(fileDescription));
        mf.setFilename(SPECIES_PROFILE_SOURCE + " " + fileName + ext);
        mf = managedFileDAO.save(mf);
        fileService.createFile(mf.getClass(), mf.getId(), mf.getFilename(), fileContent);
        mf.setContentType(fileService.getFile(mf, mf.getFilename()).getContentType());
        return managedFileDAO.save(mf);
    }

    /**
     * Creates a taxon image profile item.  This will overwrite an existing image
     * if one exists that matches the header, type, and description.
     * @param infoItems The list of SpeciesProfile info items from the taxon.
     * @param type The type of profile item
     * @param header The header for the item
     * @param description The description of the item
     * @param mf The file to link the item to
     * @return The taxon instance with the profile item added.
     * @throws MalformedURLException
     * @throws IOException
     */
    private void createTaxonImage(List<SpeciesProfile> infoItems,
            String type, String header,
            String description, ManagedFile mf, String source) throws MalformedURLException, IOException {
        addProfileInfoItem(infoItems, type, header, description, mf.getUuid(), source);
    }

    /**
     * Adds a SpeciesProfile record to the list of SpeciesProfile info items from
     * a taxon. Will always create a new one, not overwrite any existing records.
     * @param infoItems The list of SpeciesProfile info items from the taxon.
     * @param type The type of profile item
     * @param header The header for the item
     * @param description The description of the item
     * @param content The content of the item
     */
    private void addProfileInfoItem(List<SpeciesProfile> infoItems,
            String type, String header,
            String description, String content, String source) throws MalformedURLException, IOException {
        addProfileInfoItem(infoItems, type, header, description, content, source, false);
    }
    
    /**
     * Adds a SpeciesProfile record to the list of SpeciesProfile info items from
     * a taxon. If the overwrite flag is true, it will overwrite any existing 
     * record before creating a new one.
     * @param infoItems The list of SpeciesProfile info items from the taxon.
     * @param type The type of profile item
     * @param header The header for the item
     * @param description The description of the item
     * @param content The content of the item
     * @param overwrite Flag indicating whether to create a new record or overwrite existing ones
     */
    private void addProfileInfoItem(List<SpeciesProfile> infoItems,
            String type, String header, String description,
            String content, String source, boolean overwrite) {
        SpeciesProfile sp = new SpeciesProfile();
        sp.setType(type);
        sp.setHeader(SPECIES_PROFILE_SOURCE + " " + header);
        sp.setDescription(description);
        sp.setContent(content);
        // prevent duplicates
        if (overwrite || !infoItems.contains(sp)) {
            // create a metadata for the source
            if (!StringUtils.nullOrEmpty(source) && !"null".equals(source)) {
                Metadata md = new Metadata();
                md.setKey(Metadata.TAXON_SOURCE);
                md.setValue(source);
                metadataDAO.save(md);
                sp.getMetadata().add(md);
            }
            speciesProfileDAO.save(sp);
            infoItems.add(sp);
        }
    }
    
    /**
     * Helper method to set the classification of a species
     * @param taxon The species to modify
     * @param family The family of the species, if null, will not be set
     * @param kingdom The kingdom of the species, if null, will not be set
     * @return The modified IndicatorSpecies object.
     */
    private IndicatorSpecies setClassification(IndicatorSpecies taxon,
            String family, String kingdom, String group) {
        String groupName = SPECIES_PROFILE_IMPORT_LIFE;
        Preference preference = preferenceDAO.getPreferenceByKey(KEY_GROUP_IMPORT_TYPE);
        if (preference != null && preference.getValue() != null) {
            if (preference.getValue().equalsIgnoreCase(PREF_KINGDOM) && 
                kingdom != null && 
                !kingdom.isEmpty() &&
                !kingdom.equalsIgnoreCase("null")) {
                   groupName = kingdom;
            } else if (preference.getValue().equalsIgnoreCase(PREF_FAMILY) && 
                family != null && 
                !family.isEmpty() &&
                !family.equalsIgnoreCase("null")) {
                    groupName = family;
            }
        }
        
        if (kingdom != null && !kingdom.isEmpty() && !kingdom.equalsIgnoreCase("null")) {
            taxon = createTaxonMetadata(taxon, Metadata.TAXON_KINGDOM, kingdom.trim());
        }
        if (family != null && !family.isEmpty() && !family.equalsIgnoreCase("null")) {
            taxon = createTaxonMetadata(taxon, Metadata.TAXON_FAMILY, family.trim());
        }
        
        if (taxon.getTaxonGroup() == null) {
        	String groupToUse = null;
        	if (group != null && !group.trim().isEmpty()) {
        		groupToUse = group.trim();
        	} else {
        		groupToUse = groupName.trim();
        	}
        	TaxonGroup g = taxaDAO.getTaxonGroup(groupToUse);
    		if (g == null) {
                g = taxaDAO.createTaxonGroup(groupToUse, false, false, false, false, false, true);
            }
            taxon.setTaxonGroup(g);
        }
        
        return taxon;
    }

    /**
     * Helper method to set the common name for an IndicatorSpecies.
     * @param taxon The IndicatorSpecies object to modify
     * @param commonName The common name for the species
     * @param commonGuid The guid source for the common name
     * @return The modified IndicatorSpecies object
     */
    private IndicatorSpecies setCommonName(IndicatorSpecies taxon,
            String commonName, String commonGuid) {
        // common name
        taxon.setCommonName(trimString(commonName));
        if (commonGuid != null) {
            taxon = createTaxonMetadata(taxon, Metadata.COMMON_NAME_SOURCE_DATA_ID, commonGuid.trim());
        }
        return taxon;
    }

    /**
     * Helper method to set the rank for an IndicatorSpecies.
     * @param taxon The IndicatorSpecies object to modify
     * @param rankString The string identifier for the TaxonRank
     * @return The modified IndicatorSpecies object
     */
    private IndicatorSpecies setRank(IndicatorSpecies taxon, String rankString) {
        // Rank
        TaxonRank rank = TaxonRank.findByIdentifier(trimString(rankString));
        taxon.setTaxonRank(rank);
        return taxon;
    }

    /**
     * Helper method to set the scientific name for an IndicatorSpecies.
     * @param taxon The IndicatorSpecies object to modify
     * @param guid The guid source for the scientific name
     * @param scientificName The scientific name
     * @param author The author for the scientific name
     * @param year The year the scientific name was published
     * @return The modified IndicatorSpecies object
     */
    private IndicatorSpecies setScientificName(IndicatorSpecies taxon, String guid, 
            String scientificName, String author, String year) {
        return setScientificName(taxon, guid, scientificName, author, year, null);
    }
    
    /**
     * Helper method to set the scientific name for an IndicatorSpecies.
     * @param taxon The IndicatorSpecies object to modify
     * @param guid The guid source for the scientific name
     * @param scientificName The scientific name
     * @param author The author for the scientific name
     * @param year The year the scientific name was published
     * @param scientificNameAndAuthor The combined name and author string
     * @return The modified IndicatorSpecies object
     */
    private IndicatorSpecies setScientificName(IndicatorSpecies taxon,
            String guid, String scientificName, String author, String year,
            String scientificNameAndAuthor) {
        // scientific name
        taxon.setScientificName(trimString(scientificName));
        taxon.setAuthor(trimString(author));
        if (!StringUtils.nullOrEmpty(scientificNameAndAuthor)) {
            taxon.setScientificNameAndAuthor(scientificNameAndAuthor.trim());
        } else {
            // set the scientific name and author to the name and author given here
            taxon.setScientificNameAndAuthor(trimString(scientificName) + " " + trimString(author));
        }
        taxon.setYear(trimString(year));

        return createTaxonMetadata(taxon, Metadata.SCIENTIFIC_NAME_SOURCE_DATA_ID, guid);
    }
    
    private String trimString(String string) {
        return string != null ? string.trim() : string;
    }

    /**
     * Helper method to create/modify a Metadata for an IndicatorSpecies.  This 
     * method will override any existing value for Metadata with the same key.
     * @param taxon The IndicatorSpecies object to attach the Metadata to
     * @param key The key for the Metadata
     * @param value The value for the Metadata
     * @return The IndicatorSpecies object with a newly attached Metadata object
     */
    public IndicatorSpecies createTaxonMetadata(IndicatorSpecies taxon,
            String key, String value) {
        Metadata md = taxon.getMetadataByKey(key);
        if (md == null) {
            md = new Metadata();
            md.setKey(key);
        } else {
            taxon.getMetadata().remove(md);
        }
        md.setValue(trimString(value));
        metadataDAO.save(md);
        taxon.getMetadata().add(md);
        return taxon;
    }

    /**
     * Modifies an object of type IndicatorSpecies from the json object passed.
     * The object is not persisted in this method.
     * @param taxon The existing taxon to modify
     * @param ob The json object representing the IndicatorSpecies
     * @param guid The guid descriptor for the species
     * @return A modified IndicatorSpecies representing the json object passed.
     * @throws IOException
     */
    public IndicatorSpecies createShortProfile(IndicatorSpecies taxon, JSONObject ob, String guid, String group) throws IOException {
        // Scientific Name
        taxon = setScientificName(taxon, guid, ob.getString("scientificName"), 
                                  ob.getString("author"), ob.getString("year"), 
                                  ob.getString("scientificName") + " " + ob.getString("scientificNameAuthorship"));
        
        // Rank
        taxon = setRank(taxon, ob.getString("rank"));
        
        // Common Name
        String commonName = null, commonGuid = null;
        if (ob.containsKey("commonName")) {
            commonName = ob.getString("commonName");
            commonGuid = ob.getString("commonNameGUID");
        } else {
            commonName = taxon.getScientificName();
        }
        taxon = setCommonName(taxon, commonName, commonGuid);
        
        // Group/Family
        String family = null, kingdom = null;
        if (ob.containsKey("family")) {
            family = ob.getString("family");
        }
        
        // Kingdom
        if (ob.containsKey("kingdom")) {
            kingdom = ob.getString("kingdom");
        }
        
        taxon = setClassification(taxon, family, kingdom, group);
        
        List<SpeciesProfile> infoItems = taxon.getInfoItems();
        // remove ALA info items before importing new ones
        removeALAInfoItems(infoItems);
        // Images.
        // Main Image
        if (ob.containsKey("imageURL")) {
            String filename = ob.getString("imageURL"), 
                   type = SpeciesProfile.SPECIES_PROFILE_IMAGE,
                   header = SPECIES_PROFILE_IMAGE, 
                   thumbnailDescription = SPECIES_PROFILE_IMAGE + " for " + taxon.getScientificName(), 
                   fileDescription = taxon.getScientificName() + " - " + taxon.getCommonName(), 
                   contentType = "",
                   credit = "", 
                   license = "";
            ManagedFile mf = createManagedFile(getExtension(filename), contentType, credit, license, thumbnailDescription, fileDescription, downloadFile(new URL(filename)));
            createTaxonImage(infoItems, type, header, thumbnailDescription, mf, null);
        }
        
        // Thumbnail
        if (ob.containsKey("thumbnail")) {
            
            String filename = ob.getString("thumbnail"), 
                   type = SpeciesProfile.SPECIES_PROFILE_THUMBNAIL,
                   header = SPECIES_PROFILE_THUMB, 
                   thumbnailDescription = SPECIES_PROFILE_THUMB + " for " + taxon.getScientificName(), 
                   fileDescription = taxon.getScientificName() + " - " + taxon.getCommonName(), 
                   contentType = "", 
                   credit = "", 
                   license = "";
            ManagedFile mf = createManagedFile(getExtension(filename), contentType, credit, license, thumbnailDescription, fileDescription, downloadFile(new URL(filename)));
            createTaxonImage(infoItems, type, header, thumbnailDescription, mf, null);
            
            // Now thumbnail the thumbnail...
            BufferedImage fortyXforty = 
                ImageUtil.resizeImage(fileService.getFile(mf, mf.getFilename()).getInputStream(), 40, 40);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(fortyXforty, "png", baos);
            baos.flush();
            byte[] data = baos.toByteArray();
            baos.close();

            type = SpeciesProfile.SPECIES_PROFILE_IMAGE_40x40;
            header = SPECIES_PROFILE_THUMB_40;
            thumbnailDescription = SPECIES_PROFILE_THUMB_40 + " for " + taxon.getScientificName();
            
            mf = createManagedFile(".png", contentType, credit, license, thumbnailDescription, fileDescription, data);
            
            createTaxonImage(infoItems, type, header, thumbnailDescription, mf, null);
        }
        
        // set the updated info items
        taxon.setInfoItems(infoItems);
        
        return taxon; 
    }

    /**
     * Helper method to get the byte array contents of a file from a URL.
     * @param url The URL of the file to retrieve
     * @return The byte array contents of the file
     * @throws IOException
     */
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
    
    /**
     * Helper method to get the extension of a filename
     * @param filename The filename to get the extension of
     * @return The extension of the file.
     */
    private String getExtension(String filename) {
        if (filename.indexOf('.') > -1) {
            return filename.substring(filename.lastIndexOf('.'));
        } else {
            return filename;
        }
    }
}
