package au.com.gaiaresources.bdrs.controller.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.record.AttributeParser;
import au.com.gaiaresources.bdrs.controller.taxonomy.TaxonomyManagementController;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpeciesAttribute;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.taxa.TaxonRank;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.servlet.Interceptor;
import au.com.gaiaresources.bdrs.servlet.RecaptchaInterceptor;

public class TestDataCreator implements TestDataConstants {
    
    private Logger log = Logger.getLogger(getClass());
    
    private Random random;
    
    private PreferenceDAO prefDAO;
    private TaxaDAO taxaDAO;
    private ManagedFileDAO managedFileDAO;
    private SurveyDAO surveyDAO;
    private MetadataDAO metadataDAO;
    private FileService fileService;

    private ApplicationContext appContext;

    private SimpleDateFormat dateFormat;
    private RegistrationService regService;
    
    public TestDataCreator(ApplicationContext appContext) {
        
        this.appContext = appContext;
        
        random = new Random(System.currentTimeMillis());
        dateFormat = new SimpleDateFormat("dd MMM yyyy");
        
        prefDAO = appContext.getBean(PreferenceDAO.class);
        taxaDAO = appContext.getBean(TaxaDAO.class);
        managedFileDAO = appContext.getBean(ManagedFileDAO.class);
        surveyDAO = appContext.getBean(SurveyDAO.class);
        metadataDAO = appContext.getBean(MetadataDAO.class);
        fileService = appContext.getBean(FileService.class);
        regService = appContext.getBean(RegistrationService.class);
    }
    
    public void createTestUsers(int count, int rand) {
        if(rand > 0) {
            count = count + (random.nextBoolean() ? random.nextInt(rand) : -random.nextInt(rand));
        }
        // Always create these user role stereotypes
        regService.signUp("user", "user@gaiabdrs.com", "user", "user", "password", Role.USER, true);
        regService.signUp("poweruser", "poweruser@gaiabdrs.com", "poweruser", "poweruser", "password", Role.POWERUSER, true);
        regService.signUp("supervisor", "supervisor@gaiabdrs.com", "supervisor", "supervisor", "password", Role.SUPERVISOR, true);
        
        for (int i=0; i<count; ++i) {
            String firstName = generateRandomPersonFirstName();
            String lastName = generateRandomPersonLastName();
            regService.signUp(firstName.toLowerCase(), firstName + "@gaiabdrs.com", firstName, lastName, firstName.toLowerCase(), Role.USER, true);
        }
    }
    
    public void createSurvey(int count, int rand) throws Exception {
        if(rand > 0) {
            count = count + (random.nextBoolean() ? random.nextInt(rand) : -random.nextInt(rand));
        }
        log.info(String.format("Creating %d Surveys", count));
        
        Preference testDataDirPref = prefDAO.getPreferenceByKey(TEST_DATA_IMAGE_DIR); 
        
        Survey survey;
        Metadata surveyRenderer;
        Metadata surveyLogoMD;
        byte[] surveyLogo;
        String surveyName;
        String attrName;
        String description;
        List<Attribute> attributeList;
        
        for(int i=0; i<count; i++) {
            
            // Attributes
            attributeList = new ArrayList<Attribute>();
            Attribute attr;
            for(AttributeType attrType : AttributeType.values()) {
                for(AttributeScope scope : new AttributeScope[] { AttributeScope.RECORD, AttributeScope.SURVEY, null }) {
                    
                    attr = new Attribute();
                    attr.setRequired(false);
                    if(scope == null) {
                        description = attrType.getName() + " scope null";
                        attrName = generateLoremIpsum(2, false)+"null scope";
                    } else {
                        description = attrType.getName() + " scope " + scope.getName();
                        attrName = generateLoremIpsum(2, false)+scope.getName()+" scope";
                    }
                    attr.setDescription(description);
                    attr.setName(attrName);
                    attr.setTypeCode(attrType.getCode());
                    attr.setScope(scope);
                    attr.setTag(false);
                    
                    if(AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                        List<AttributeOption> optionList = new ArrayList<AttributeOption>();
                        for(int j=1; j<4; j++) {
                            AttributeOption opt = new AttributeOption();
                            opt.setValue(String.format("Option %d", j));
                            opt = taxaDAO.save(opt);
                            optionList.add(opt);
                        }
                        attr.setOptions(optionList);
                    }
                    
                    attr = taxaDAO.save(attr);
                    attributeList.add(attr);
                }
            }
            
            // Survey
            surveyName = generateLoremIpsum(2, false)+"Survey";
            
            survey = new Survey();
            survey.setName(surveyName);
            survey.setActive(true);
            survey.setPublic(true);
            survey.setDate(new Date());
            survey.setDescription(generateLoremIpsum(5));
            survey.setAttributes(attributeList);
            
            surveyRenderer = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
            surveyRenderer = metadataDAO.save(surveyRenderer);
            
            surveyLogo = getRandomImage(testDataDirPref, 640, 480);
            surveyLogo = surveyLogo == null ? createImage(640, 480, surveyName) : surveyLogo;
            surveyLogoMD = new Metadata();
            surveyLogoMD.setKey(Metadata.SURVEY_LOGO);
            surveyLogoMD.setValue(surveyName+".png");
            surveyLogoMD = metadataDAO.save(surveyLogoMD);
            
            fileService.createFile(surveyLogoMD.getClass(), surveyLogoMD.getId(), surveyLogoMD.getValue(), surveyLogo);
            
            survey = surveyDAO.save(survey);
        }
    }
    
    public void createBasicSurvey() {
        
        String surveyName = "Basic Survey";
        Metadata surveyLogoMD = new Metadata(Metadata.SURVEY_LOGO, surveyName+".png");
        Survey survey = new Survey();
        
        survey.setName(surveyName);
        survey.setActive(true);
        survey.setPublic(true);
        survey.setDate(new Date());
        survey.setDescription(generateLoremIpsum(5));
        Metadata surveyRenderer  = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);;
        metadataDAO.save(surveyRenderer);
        
        survey = surveyDAO.save(survey);
    }
    
    public void createTaxonGroups(int count, int rand, boolean createAttributes) throws Exception {
        if(rand > 0) {
            count = count + (random.nextBoolean() ? random.nextInt(rand) : -random.nextInt(rand));
        }
        log.info(String.format("Creating %d Taxon Groups", count));

        // Look for the directory of images, if found we will use that 
        // otherwise we will use a generated set of images.
        byte[] defaultThumbnail = createImage(250, 140, "Test Taxon Group Thumb");
        byte[] defaultImage = createImage(640, 480, "Test Taxon Group Image");
        Map<String, byte[]> defaultImageMap = new HashMap<String, byte[]>();
        defaultImageMap.put("image", defaultImage);
        defaultImageMap.put("thumbNail", defaultThumbnail);
        
        Preference testDataDirPref = prefDAO.getPreferenceByKey(TEST_DATA_IMAGE_DIR); 
        
        MockMultipartHttpServletRequest request;
        MockHttpServletResponse response;
        for(int i=0; i<count; i++) {
            request = new MockMultipartHttpServletRequest();
            response = new MockHttpServletResponse();
            
            request.setMethod("POST");
            request.setRequestURI("/bdrs/admin/taxongroup/edit.htm");
            
            request.setParameter("name", TEST_GROUPS[random.nextInt(TEST_GROUPS.length)]);
            request.setParameter("behaviourIncluded", String.valueOf(random.nextBoolean()));
            request.setParameter("firstAppearanceIncluded", String.valueOf(random.nextBoolean()));
            request.setParameter("lastAppearanceIncluded", String.valueOf(random.nextBoolean()));
            request.setParameter("habitatIncluded", String.valueOf(random.nextBoolean()));
            request.setParameter("weatherIncluded", String.valueOf(random.nextBoolean()));
            request.setParameter("numberIncluded", String.valueOf(random.nextBoolean()));
            
            // Image and Thumbnail
            for(String propertyName : new String[] { "image", "thumbNail" }) {
                String key = String.format("%s_file", propertyName);
                String image_filename = String.format("%s_filename.png", propertyName);
                
                byte[] imageData = getRandomImage(testDataDirPref, 250, 140);
                imageData = imageData == null ? defaultImageMap.get(propertyName) : imageData;
                
                MockMultipartFile mockImageFile = new MockMultipartFile(key, image_filename, "image/png", imageData);
                ((MockMultipartHttpServletRequest)request).addFile(mockImageFile);
                
                request.setParameter(propertyName, image_filename);
            }

            // Attributes
            if(createAttributes) {
                int curWeight = 0;
                String attributeOptions = "Option A, Option B, Option C, Option D";
                int index = 0;
                for(Boolean isTag : new Boolean[] { true, false }) {
                    for (AttributeType attrType : AttributeType.values()) {
            
                        request.addParameter("add_attribute", String.valueOf(index));
                        request.setParameter(String.format("add_weight_%d", index), String.valueOf(curWeight));
                        request.setParameter(String.format("add_name_%d", index), String.format("%s name%s", attrType.getName(), isTag ? " Tag" : ""));
                        request.setParameter(String.format("add_description_%d", index), String.format("%s description%s", attrType.getName(), isTag ? " Tag" : ""));
                        request.setParameter(String.format("add_typeCode_%d", index), attrType.getCode());
                        request.setParameter(String.format("add_tag_%d", index), isTag.toString().toLowerCase());
                        //request.setParameter(String.format("add_scope_%d", index), scope.toString());
            
                        if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                            request.setParameter(String.format("add_option_%d", index), attributeOptions);
                        }
            
                        index = index + 1;
                        curWeight = curWeight + 100;
                    }
                }
            }
            
            handle(request, response);
        }
    }

    public void createTaxa(int count, int rand) throws Exception {
        MockMultipartHttpServletRequest request;
        MockHttpServletResponse response;
        
        if(rand > 0) {
            count = count + (random.nextBoolean() ? random.nextInt(rand) : -random.nextInt(rand));
        }
        log.info(String.format("Creating %d Taxa", count));
        
        Map<TaxonGroup, String> groupToGenusMap = new HashMap<TaxonGroup, String>();
        String genusName;
        for(TaxonGroup taxonGroup : taxaDAO.getTaxonGroups()) {
            genusName = generateScientificName(TaxonRank.GENUS);
            groupToGenusMap.put(taxonGroup, genusName);
            for(int i=0; i<count; i++) {
                request = new MockMultipartHttpServletRequest();
                response = new MockHttpServletResponse();

                request.setMethod("POST");
                request.setRequestURI("/bdrs/admin/taxonomy/edit.htm");

                TaxonRank rank = TaxonRank.SPECIES;
                request.setParameter("taxonRank", rank.toString());
                String scientificName = generateScientificName(rank, genusName);
                request.setParameter("scientificName", scientificName);
                request.setParameter("commonName", generateCommonName(rank, scientificName));
                request.setParameter("parentPk", new String());
                
                request.setParameter("taxonGroupPk", taxonGroup.getId().toString());
                request.setParameter("author", generateRandomPersonName());
                request.setParameter("year", String.valueOf(1700 + random.nextInt(2011 - 1700)));
                
                createTaxonAttributes(request, taxonGroup);
                
                handle(request, response);
            }
        }
        
        TaxonGroup life = taxaDAO.getTaxonGroups().get(0);
        List<IndicatorSpecies> genusList=  new ArrayList<IndicatorSpecies>();
        for(TaxonGroup taxonGroup: taxaDAO.getTaxonGroups()) {
            
            TaxonRank rank = TaxonRank.GENUS;
            IndicatorSpecies genus = new IndicatorSpecies();
            genus.setScientificName(groupToGenusMap.get(taxonGroup));
            genus.setCommonName(generateCommonName(rank, genus.getScientificName()));
            genus.setTaxonGroup(life);
            genus.setTaxonRank(rank);
            genus.setAuthor(generateRandomPersonName());
            genus.setYear(String.valueOf(1700 + random.nextInt(2011 - 1700)));
            
            genus = taxaDAO.save(genus);
            genus = createTaxonAttributes(genus);
            genusList.add(genus);
            
            for(IndicatorSpecies species : taxaDAO.getIndicatorSpecies(taxonGroup)) {
                species.setParent(genus);
                taxaDAO.save(species);
            }
        }
        
        IndicatorSpecies previousTaxon = null;
        for(int i=0; i<TAXON_RANKS.length-2; i++) {
            IndicatorSpecies taxon = new IndicatorSpecies();
            taxon.setScientificName(generateScientificName(TAXON_RANKS[i]));
            taxon.setCommonName(generateCommonName(TAXON_RANKS[i], taxon.getScientificName()));
            taxon.setTaxonGroup(life);
            taxon.setParent(previousTaxon);
            taxon.setTaxonRank(TAXON_RANKS[i]);
            taxon.setAuthor(generateRandomPersonName());
            taxon.setYear(String.valueOf(1700 + random.nextInt(2011 - 1700)));
            taxon = taxaDAO.save(taxon);
            taxon = createTaxonAttributes(taxon);
            previousTaxon = taxon;
        }
        
        for(IndicatorSpecies genus : genusList) {
            genus.setParent(previousTaxon);
            taxaDAO.save(genus);
        }
    }

    private void createTaxonAttributes(MockMultipartHttpServletRequest request,
            TaxonGroup taxonGroup) throws ParseException, IOException {
        
        String name;
        IndicatorSpeciesAttribute taxonAttr;
        Preference testDataDirPref = prefDAO.getPreferenceByKey(TEST_DATA_IMAGE_DIR);
        for(Attribute attr : taxonGroup.getAttributes()) {
            if(attr.isTag()) {
                
                name = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, AttributeParser.DEFAULT_PREFIX, attr.getId());
                
                taxonAttr = new IndicatorSpeciesAttribute();
                taxonAttr.setAttribute(attr);
                
                if(AttributeType.IMAGE.equals(attr.getType()) || AttributeType.FILE.equals(attr.getType())) {
                    byte[] data;
                    String contentType;
                    String filename;
                    if(AttributeType.IMAGE.equals(attr.getType())) {
                        data = getRandomImage(testDataDirPref, 640, 480);
                        data = data == null ? createImage(640, 480, attr.getDescription()) : data;
                        contentType = "image/png";
                        filename = String.format("%s.png",attr.getName());
                    } else {
                        data = generateDataFile();
                        contentType = "text/plain";
                        filename = String.format("%s.txt",attr.getName());
                    }
                    
                    String attrFileKey = String.format(AttributeParser.ATTRIBUTE_FILE_NAME_TEMPLATE, AttributeParser.DEFAULT_PREFIX, attr.getId());
                    
                    MockMultipartFile mockFileFile = new MockMultipartFile(attrFileKey, filename, contentType, data);
                    request.addFile(mockFileFile);
                    taxonAttr.setStringValue(filename);
                    
                } else {
                    setValueForTaxonAttribute(taxonAttr);
                }
                request.addParameter(name, taxonAttr.getStringValue());
            }
        }
    }

    private IndicatorSpecies createTaxonAttributes(IndicatorSpecies taxon) throws ParseException, IOException {
        
        IndicatorSpeciesAttribute taxonAttr;
        Preference testDataDirPref = prefDAO.getPreferenceByKey(TEST_DATA_IMAGE_DIR);
        
        for(Attribute attr : taxon.getTaxonGroup().getAttributes()) {
            if(attr.isTag()) {
                
                taxonAttr = new IndicatorSpeciesAttribute();
                taxonAttr.setAttribute(attr);
                
                byte[] data = null;
                String filename = null;
                if(AttributeType.IMAGE.equals(attr.getType()) || AttributeType.FILE.equals(attr.getType())) {
                    if(AttributeType.IMAGE.equals(attr.getType())) {
                        data = getRandomImage(testDataDirPref, 640, 480);
                        data = data == null ? createImage(640, 480, attr.getDescription()) : data;
                        filename = String.format("%s.png",attr.getName());
                    } else {
                        data = generateDataFile();
                        filename = String.format("%s.txt",attr.getName());
                    }
                    taxonAttr.setStringValue(filename);
                    
                } else {
                    setValueForTaxonAttribute(taxonAttr);
                }
                
                taxonAttr = taxaDAO.save(taxonAttr);
                taxon.getAttributes().add(taxonAttr);
                taxon = taxaDAO.save(taxon);
                
                // Taxon Attribute must be saved before the file can be saved.
                if(AttributeType.IMAGE.equals(attr.getType()) || AttributeType.FILE.equals(attr.getType())) {
                    fileService.createFile(taxonAttr.getClass(), taxonAttr.getId(), filename, data);
                }
            }
        }
        return taxon;
    }
    
    private void setValueForTaxonAttribute(IndicatorSpeciesAttribute taxonAttr) throws ParseException {
        switch(taxonAttr.getAttribute().getType()) {
            case INTEGER:
                taxonAttr.setNumericValue(new BigDecimal(random.nextInt(50)));
                taxonAttr.setStringValue(taxonAttr.getNumericValue().toPlainString());
                break;
            case DECIMAL:
                taxonAttr.setNumericValue(new BigDecimal(random.nextDouble()));
                taxonAttr.setStringValue(taxonAttr.getNumericValue().toPlainString());
                break;
            case DATE:
                Date d = new Date();
                taxonAttr.setStringValue(dateFormat.format(d));
                taxonAttr.setDateValue(dateFormat.parse(taxonAttr.getStringValue()));
                break;
            case STRING:
                taxonAttr.setStringValue(generateLoremIpsum(3, 3));
                break;
            case STRING_AUTOCOMPLETE:
                taxonAttr.setStringValue(generateLoremIpsum(3, 3));
                break;
            case TEXT:
                taxonAttr.setStringValue(generateLoremIpsum(5, 8));
                break;
            case STRING_WITH_VALID_VALUES:
                List<AttributeOption> optionList = taxonAttr.getAttribute().getOptions();
                String val = optionList.get(random.nextInt(optionList.size())).getValue();
                taxonAttr.setStringValue(val);
                break;
            default:
                throw new RuntimeException();
        }
    }

    public void createTaxonProfile()
            throws Exception {
        log.info(String.format("Creating Taxon Profiles"));

        TaxonomyManagementController controller = appContext.getBean(TaxonomyManagementController.class);
        
        Preference testDataDirPref = prefDAO.getPreferenceByKey(TEST_DATA_IMAGE_DIR);
        
        MockMultipartHttpServletRequest request;
        MockHttpServletResponse response;
        
        List<SpeciesProfile> profileList;
        for (IndicatorSpecies species : taxaDAO.getIndicatorSpecies()) {
            profileList = controller.loadSpeciesProfileTemplate(species.getTaxonGroup(), species.getInfoItems());
            
            request = new MockMultipartHttpServletRequest();
            response = new MockHttpServletResponse();
            
            request.setMethod("POST");
            request.setRequestURI("/bdrs/admin/taxonomy/edit.htm");
            
            request.setParameter("taxonPk", species.getId().toString());
            request.setParameter("scientificName", species.getScientificName());
            request.setParameter("commonName", species.getCommonName());
            request.setParameter("taxonRank", species.getTaxonRank().toString());
            request.setParameter("parentPk", species.getParent() == null ? "" : species.getParent().getId().toString());
            request.setParameter("taxonGroupPk", species.getTaxonGroup().getId().toString());
            request.setParameter("author", species.getAuthor() == null ? "" : species.getAuthor());
            request.setParameter("year", species.getYear() == null ? "" : species.getYear());
            
            int index = 0;
            int weight = 0;
            File speciesImage = getRandomFile(testDataDirPref, new WebImageFileFilter());
            for(SpeciesProfile profile : profileList) {
                request.addParameter("new_profile", String.valueOf(index));
                request.setParameter(String.format("new_profile_type_%d", index), profile.getType());
                request.setParameter(String.format("new_profile_content_%d", index), generateSpeciesProfileData(profile, testDataDirPref, speciesImage, request));
                request.setParameter(String.format("new_profile_description_%d", index), profile.getDescription());
                request.setParameter(String.format("new_profile_header_%d", index), profile.getHeader());
                request.setParameter(String.format("new_profile_weight_%d", index), String.valueOf(weight));
                
                weight = weight + 100;
                index = index + 1;
            }
            
            handle(request, response);
        }
    }
    
    private String generateSpeciesProfileData(SpeciesProfile profile,
            Preference testDataDirPref, File speciesImage,
            MockMultipartHttpServletRequest request) throws IOException {
        String content;
        if(profile.isFileType()) {
            if(profile.isImgType()) {
                int width;
                int height;
                String message = profile.getType();
                
                if (SpeciesProfile.SPECIES_PROFILE_IMAGE.equals(profile.getType())
                        || SpeciesProfile.SPECIES_PROFILE_MAP.equals(profile.getType())
                        || SpeciesProfile.SPECIES_PROFILE_SILHOUETTE.equals(profile.getType())) {
                    width = -1;
                    height = -1;
                } else if (SpeciesProfile.SPECIES_PROFILE_IMAGE_40x40.equals(profile.getType())
                        || SpeciesProfile.SPECIES_PROFILE_MAP_40x40.equals(profile.getType())
                        || SpeciesProfile.SPECIES_PROFILE_SILHOUETTE_40x40.equals(profile.getType())) {
                    width = 40;
                    height = 40;
                } else if (SpeciesProfile.SPECIES_PROFILE_IMAGE_32x32.equals(profile.getType())) {
                    width = 32;
                    height = 32;
                } else if (SpeciesProfile.SPECIES_PROFILE_IMAGE_16x16.equals(profile.getType())) {
                    width = 16;
                    height = 16;
                } else {
                    width = -1;
                    height = -1;
                }

                byte[] imageData = getAndScaleImageData(width, height, speciesImage);
                imageData = imageData == null ? createImage(width, height, message) : imageData;
                ManagedFile mf = createManagedFile(imageData, ".png");
                content = mf.getUuid();
                
            } else if(profile.isAudioType()) {
                byte[] audioData = getRandomAudio(testDataDirPref);
                if(audioData == null) {
                    audioData = new byte[4096];
                    Arrays.fill(audioData, (byte)0);
                }
                if(audioData != null) {
                    ManagedFile mf = createManagedFile(audioData, ".mp3");
                    content = mf.getUuid();
                } else {
                    content = "";
                }
            } else {
                content = "";
            }
        } else {
            content = generateLoremIpsum(25, 150);
        }
        
        return content;
    }

    private ManagedFile createManagedFile(byte[] data, String ext) throws IOException {
        
        ManagedFile mf = new ManagedFile();
        mf.setContentType("");
        mf.setCredit(generateRandomPersonName());
        mf.setLicense(generateLoremIpsum(4));
        mf.setDescription(generateLoremIpsum(25));
        mf.setFilename(mf.getUuid()+ext);
        mf = managedFileDAO.save(mf);
        
        fileService.createFile(mf.getClass(), mf.getId(), mf.getFilename(), data);
        
        mf.setContentType(fileService.getFile(mf, mf.getFilename()).getContentType());
        return managedFileDAO.save(mf);
    }
    
    private String generateLoremIpsum(int minWords, int maxWords) {
        return this.generateLoremIpsum(minWords, maxWords, true);
    }
    
    private String generateLoremIpsum(int minWords, int maxWords, boolean startWithLoremIpsum) {
        int words;
        if(minWords == maxWords) {
            words = minWords;
        } else {
            words = random.nextInt(maxWords - minWords) + minWords;
        }
        StringBuilder builder = new StringBuilder();
        if(startWithLoremIpsum) {
            builder.append("Lorem Ipsum ");
            words = words -2;
        }
        
        int sentenceLength = random.nextInt(MAX_WORDS_IN_SENTENCE - MIN_WORDS_IN_SENTENCE) + MIN_WORDS_IN_SENTENCE;
        int wordIndex = 0;
        
        String word;
        for(int i=0; i<words; i++) {
            word = LATIN_WORDS[random.nextInt(LATIN_WORDS.length)];
            word = wordIndex == 0 ? word : word.toLowerCase();
            builder.append(word);
            
            if(wordIndex >= sentenceLength) {
                builder.append(". ");
                wordIndex = 0;
                sentenceLength = random.nextInt(MAX_WORDS_IN_SENTENCE - MIN_WORDS_IN_SENTENCE) + MIN_WORDS_IN_SENTENCE;
            } else {
                builder.append(" ");
                wordIndex++;
            }
        }
        
        return builder.toString();
    }

    private String generateLoremIpsum(int words) {
        return this.generateLoremIpsum(words, words, true);
    }
    
    private String generateLoremIpsum(int words, boolean startWithLoremIpsum) {
        return this.generateLoremIpsum(words, words, startWithLoremIpsum);
    }

    private ModelAndView handle(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        final HandlerMapping handlerMapping = appContext.getBean(HandlerMapping.class);
        final HandlerAdapter handlerAdapter = appContext.getBean(HandlerAdapter.class);
        final HandlerExecutionChain handler = handlerMapping.getHandler(request);

        Object controller = handler.getHandler();
        // if you want to override any injected attributes do it here

        HandlerInterceptor[] interceptors = handlerMapping.getHandler(request).getInterceptors();
        for (HandlerInterceptor interceptor : interceptors) {
            if (handleInterceptor(interceptor)) {
                final boolean carryOn = interceptor.preHandle(request, response, controller);
                if (!carryOn) {
                    return null;
                }
            }
        }
        ModelAndView mv = handlerAdapter.handle(request, response, controller);
        return mv;
    }
    
    private boolean handleInterceptor(HandlerInterceptor interceptor) {
        return !(interceptor instanceof Interceptor)
                && !(interceptor instanceof RecaptchaInterceptor);
    }
    
    private byte[] createImage(int width, int height, String text) throws IOException {
        if(width < 0) {
            width = random.nextInt(DEFAULT_MAX_IMAGE_WIDTH - DEFAULT_MIN_IMAGE_WIDTH) + DEFAULT_MIN_IMAGE_WIDTH;
        }
        if(height < 0) {
            height = random.nextInt(DEFAULT_MAX_IMAGE_HEIGHT - DEFAULT_MIN_IMAGE_HEIGHT) + DEFAULT_MIN_IMAGE_HEIGHT;
        }
        
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2 = (Graphics2D)img.getGraphics();
        g2.setBackground(new Color(220,220,220));

        Dimension size;
        float fontSize = g2.getFont().getSize();
        // Make the text as large as possible.
        do {
            g2.setFont(g2.getFont().deriveFont(fontSize));
            FontMetrics metrics = g2.getFontMetrics(g2.getFont());
            int hgt = metrics.getHeight();
            int adv = metrics.stringWidth(text);
            size = new Dimension(adv+2, hgt+2);
            fontSize = fontSize + 1f;
        } while(size.width < Math.round(0.9*width) && size.height < Math.round(0.9*height));
        
        g2.setColor(Color.DARK_GRAY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString(text, (width-size.width)/2, (height-size.height)/2);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(0,0,width-1,height-1);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(width * height);
        ImageIO.write(img, "png", baos);
        baos.flush();
        byte[] rawBytes = baos.toByteArray();
        baos.close();
        
        return rawBytes;
    }
    
    private byte[] getRandomAudio(Preference testDataDirPref) throws IOException {
        File file = getRandomFile(testDataDirPref, new WebAudioFileFilter());
        return file == null ? null : getBytesFromFile(file);
    }

    private byte[] getRandomImage(Preference testDataDirPref, int targetWidth, int targetHeight) throws IOException {
        File file = getRandomFile(testDataDirPref, new WebImageFileFilter());
        return file == null ? null : getAndScaleImageData(targetWidth, targetHeight, file); 
    }

    private byte[] getAndScaleImageData(int targetWidth, int targetHeight, File file) throws IOException {
        if(file == null) {
            return null;
        }
        
        BufferedImage sourceImage = ImageIO.read(file);
        BufferedImage targetImage;
        if(targetWidth > -1 && targetHeight > -1) {
            // Resize the image as required to fit the space
            targetImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2_scaled = targetImage.createGraphics();
            // Better scaling
            g2_scaled.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            g2_scaled.setBackground(Color.WHITE);
            g2_scaled.clearRect(0,0,targetWidth,targetHeight);

            int sourceWidth = sourceImage.getWidth();
            int sourceHeight = sourceImage.getHeight();

            double widthRatio = (double) targetWidth / (double) sourceWidth;
            double heightRatio = (double) targetHeight / (double) targetHeight;
            
            if (heightRatio > widthRatio) {
                int scaledHeight = (int) Math.round(widthRatio * sourceHeight);
                g2_scaled.drawImage(sourceImage, 0, (targetImage.getHeight() - scaledHeight) / 2, targetImage.getWidth(), scaledHeight, g2_scaled.getBackground(), null);
            } else {
                int scaledWidth = (int) Math.round(heightRatio * sourceWidth);
                g2_scaled.drawImage(sourceImage, (targetImage.getWidth() - scaledWidth) / 2, 0, scaledWidth, targetImage.getHeight(), new Color(
                        0, 0, 0, 255), null);
            }
        } else {
            targetImage = sourceImage;
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(targetWidth * targetHeight);
        ImageIO.write(targetImage, "png", baos);
        baos.flush();
        byte[] data = baos.toByteArray();
        baos.close();
        
        return data;
    }
    
    private File getRandomFile(Preference testDataDirPref, FileFilter filter) throws IOException {
        if(testDataDirPref == null) {
            return null;
        }
        
        File dir = new File(testDataDirPref.getValue());
        if(dir.exists() && dir.isDirectory()) {
            File[] imageFiles = dir.listFiles(filter);
            return imageFiles[random.nextInt(imageFiles.length)];
        } else {
            return null;
        }
    }
    
    private byte[] generateDataFile() {
        return generateLoremIpsum(250, 300).getBytes();
    }
    
    private byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    private String generateRandomPersonName() {
        String first = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String surname = SURNAMES[random.nextInt(SURNAMES.length)];
        
        return String.format("%s %s", first, surname);
    }
    
    private String generateRandomPersonFirstName() {
        return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];        
    }
    
    private String generateRandomPersonLastName() {
        return SURNAMES[random.nextInt(SURNAMES.length)];
    }
    
    private String generateScientificName(TaxonRank rank) {
        return this.generateScientificName(rank, null);
    }

    private String generateScientificName(TaxonRank rank, String genus) {
        String latin = LATIN_WORDS[random.nextInt(LATIN_WORDS.length)];
        switch(rank) {
            case KINGDOM:
            case PHYLUM:
            case CLASS:       
            case ORDER:
                latin = latin + "a";
                break;
            case FAMILY:
                latin = latin + "ae";
                break;
            case GENUS:
                latin = latin + "us";
                break;
            case SPECIES:
                genus = genus == null ? generateScientificName(TaxonRank.GENUS) : genus;
                latin = genus + " " + latin.toLowerCase();
                break;
            default:
                // do nothing
        }
        
        return latin;
    }

    private String generateCommonName(TaxonRank rank, String scientificName) {
        String[] split = scientificName.split(" ");
        String suffix = split[split.length-1];
        
        String prefix;
        if(TaxonRank.SPECIES.equals(rank)) {
            prefix = APPEARANCE_ADJECTIVES[random.nextInt(APPEARANCE_ADJECTIVES.length)];
        } else {
            prefix = rank.getIdentifier();
        }
        return String.format("%s %s", prefix, suffix);
    }
}
