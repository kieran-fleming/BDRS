package au.com.gaiaresources.bdrs.controller.webservice;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import junit.framework.Assert;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.postgresql.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.test.TestDataCreator;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;

public class ApplicationServiceUploadTest extends AbstractControllerTest {
    
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private CensusMethodDAO censusMethodDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private FileService fileService;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
    private Random rand = new Random(123456789);
    
    @Test
    public void testMissingIdent() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        createTestData();
        
        request.setMethod("POST");
        request.setRequestURI("/webservice/application/clientSync.htm");
        request.setParameter("syncData", "[]");
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "postMessage");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "message");
        
        //System.err.println(mv.getModel().get("message"));
        JSONObject json = JSONObject.fromObject(mv.getModel().get("message"));
        Assert.assertEquals(500, json.getInt("status"));
        
        JSONObject errorData = json.getJSONObject("500");
        Assert.assertEquals("NullPointerException", errorData.getString("type"));
    }
    
    @Test
    public void testMissingSyncData() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        createTestData();
        
        request.setMethod("POST");
        request.setRequestURI("/webservice/application/clientSync.htm");
        request.setParameter("ident", getRequestContext().getUser().getRegistrationKey());
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "postMessage");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "message");
        
        //System.err.println(mv.getModel().get("message"));
        JSONObject json = JSONObject.fromObject(mv.getModel().get("message"));
        Assert.assertEquals(500, json.getInt("status"));
        
        JSONObject errorData = json.getJSONObject("500");
        Assert.assertEquals("NullPointerException", errorData.getString("type"));
    }
    
    @Test
    public void testUnauthorized() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        createTestData();
        
        request.setMethod("POST");
        request.setRequestURI("/webservice/application/clientSync.htm");
        request.setParameter("ident", getRequestContext().getUser().getRegistrationKey()+"abc");
        request.setParameter("syncData", "[]");
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "postMessage");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "message");
        
        //System.err.println(mv.getModel().get("message"));
        JSONObject json = JSONObject.fromObject(mv.getModel().get("message"));
        Assert.assertEquals(401, json.getInt("status"));
        
        JSONObject errorData = json.getJSONObject("401");
        Assert.assertEquals("Unauthorized", errorData.getString("message"));
    }
    
    @Test
    public void testSync() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        createTestData();
        String syncData = generateSyncData(false);
        //System.err.println(syncData);
        
        request.setMethod("POST");
        request.setRequestURI("/webservice/application/clientSync.htm");
        request.setParameter("ident", getRequestContext().getUser().getRegistrationKey());
        request.setParameter("syncData", syncData);
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "postMessage");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "message");
        
        JSONObject json = JSONObject.fromObject(mv.getModel().get("message"));
        //System.err.println(mv.getModel().get("message"));
        Assert.assertEquals(200, json.getInt("status"));
        
        JSONObject data = json.getJSONObject("200");
        JSONArray syncResult = data.getJSONArray("sync_result");
        validate(JSONArray.fromObject(syncData), syncResult);
        //System.err.println(data.toString());
    }
    
    @Test
    public void testSyncBlankRecordAttributes() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        createTestData();
        String syncData = generateSyncData(true);
        //System.err.println(syncData);
        
        request.setMethod("POST");
        request.setRequestURI("/webservice/application/clientSync.htm");
        request.setParameter("ident", getRequestContext().getUser().getRegistrationKey());
        request.setParameter("syncData", syncData);
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "postMessage");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "message");
        
        JSONObject json = JSONObject.fromObject(mv.getModel().get("message"));
        //System.err.println(mv.getModel().get("message"));
        Assert.assertEquals(200, json.getInt("status"));
        
        JSONObject data = json.getJSONObject("200");
        JSONArray syncResult = data.getJSONArray("sync_result");
        validate(JSONArray.fromObject(syncData), syncResult);
        //System.err.println(data.toString());
    }
    
    @Test
    public void testSyncUpdate() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        createTestData();
        {
            String syncData = generateSyncData(false);
            MockHttpServletRequest preRequest = new MockHttpServletRequest();
            preRequest.setMethod("POST");
            preRequest.setRequestURI("/webservice/application/clientSync.htm");
            preRequest.setParameter("ident", getRequestContext().getUser().getRegistrationKey());
            preRequest.setParameter("syncData", syncData);
            MockHttpServletResponse preResponse = new MockHttpServletResponse();
            
            ModelAndView preMV = handle(preRequest, preResponse);
            ModelAndViewAssert.assertViewName(preMV, "postMessage");
            ModelAndViewAssert.assertModelAttributeAvailable(preMV, "message");
            
            JSONObject preJSON = JSONObject.fromObject(preMV.getModel().get("message"));
            //System.err.println(mv.getModel().get("message"));
            Assert.assertEquals(200, preJSON.getInt("status"));
            
            JSONObject preData = preJSON.getJSONObject("200");
            JSONArray preSyncResult = preData.getJSONArray("sync_result");
            validate(JSONArray.fromObject(syncData), preSyncResult);
        }
        // --------------------------
        {
            String updateSyncData = generateUpdateSyncData(false);
            request.setMethod("POST");
            request.setRequestURI("/webservice/application/clientSync.htm");
            request.setParameter("ident", getRequestContext().getUser().getRegistrationKey());
            request.setParameter("syncData", updateSyncData);
            
            ModelAndView mv = handle(request, response);
            ModelAndViewAssert.assertViewName(mv, "postMessage");
            ModelAndViewAssert.assertModelAttributeAvailable(mv, "message");
            
            JSONObject json = JSONObject.fromObject(mv.getModel().get("message"));
            //System.err.println(mv.getModel().get("message"));
            Assert.assertEquals(200, json.getInt("status"));
            
            JSONObject data = json.getJSONObject("200");
            JSONArray syncResult = data.getJSONArray("sync_result");
            validate(JSONArray.fromObject(updateSyncData), syncResult);
            //System.err.println(data.toString());
        }
    }
    
    @Test
    public void testSyncUpdateWithBlankAttributes() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        createTestData();
        {
            String syncData = generateSyncData(false);
            MockHttpServletRequest preRequest = new MockHttpServletRequest();
            preRequest.setMethod("POST");
            preRequest.setRequestURI("/webservice/application/clientSync.htm");
            preRequest.setParameter("ident", getRequestContext().getUser().getRegistrationKey());
            preRequest.setParameter("syncData", syncData);
            MockHttpServletResponse preResponse = new MockHttpServletResponse();
            
            ModelAndView preMV = handle(preRequest, preResponse);
            ModelAndViewAssert.assertViewName(preMV, "postMessage");
            ModelAndViewAssert.assertModelAttributeAvailable(preMV, "message");
            
            JSONObject preJSON = JSONObject.fromObject(preMV.getModel().get("message"));
            //System.err.println(mv.getModel().get("message"));
            Assert.assertEquals(200, preJSON.getInt("status"));
            
            JSONObject preData = preJSON.getJSONObject("200");
            JSONArray preSyncResult = preData.getJSONArray("sync_result");
            validate(JSONArray.fromObject(syncData), preSyncResult);
        }
        // --------------------------
        {
            String updateSyncData = generateUpdateSyncData(true);
            request.setMethod("POST");
            request.setRequestURI("/webservice/application/clientSync.htm");
            request.setParameter("ident", getRequestContext().getUser().getRegistrationKey());
            request.setParameter("syncData", updateSyncData);
            
            ModelAndView mv = handle(request, response);
            ModelAndViewAssert.assertViewName(mv, "postMessage");
            ModelAndViewAssert.assertModelAttributeAvailable(mv, "message");
            
            JSONObject json = JSONObject.fromObject(mv.getModel().get("message"));
            //System.err.println(mv.getModel().get("message"));
            Assert.assertEquals(200, json.getInt("status"));
            
            JSONObject data = json.getJSONObject("200");
            JSONArray syncResult = data.getJSONArray("sync_result");
            validate(JSONArray.fromObject(updateSyncData), syncResult);
            //System.err.println(data.toString());
        }
    }
    
    private void validate(JSONArray syncData, JSONArray syncResult) throws IOException {
        // Preprocess the syncData to map records against their client id
        Map<String, JSONObject> syncDataMap = new HashMap<String, JSONObject>(syncData.size());
        for(int j=0; j<syncData.size(); j++) {
            JSONObject jsonRecord = syncData.getJSONObject(j);
            syncDataMap.put(jsonRecord.getString("id"), jsonRecord);
            
            JSONArray jsonRecAttrs = jsonRecord.getJSONArray("attributeValues");
            for(int k=0; k<jsonRecAttrs.size(); k++) {
                JSONObject jsonRecAttr = jsonRecAttrs.getJSONObject(k);
                syncDataMap.put(jsonRecAttr.getString("id"), jsonRecAttr);
            }
        }
        
        for(int i=0; i<syncResult.size(); i++) {
            /*
             * {
             *     "id": "150c6a59-a0df-48d9-b371-051037a1f6f6",
             *     "server_id": 6393,
             *     "klass": "Record"
             * }
             */
            JSONObject jsonResult = syncResult.getJSONObject(i);
            if(Record.class.getSimpleName().equals(jsonResult.getString("klass"))) {
                Assert.assertTrue(syncDataMap.containsKey(jsonResult.getString("id")));
                JSONObject jsonRecord = syncDataMap.get(jsonResult.getString("id"));
                
                Record rec = recordDAO.getRecord(jsonResult.getInt("server_id"));
                Assert.assertNotNull(rec);
                
                Assert.assertEquals(jsonRecord.getDouble("latitude"), rec.getPoint().getY());
                Assert.assertEquals(jsonRecord.getDouble("longitude"), rec.getPoint().getX());
                Assert.assertEquals(jsonRecord.getLong("when"), rec.getWhen().getTime());
                
                if(jsonRecord.get("lastDate") instanceof JSONNull) {
                    Assert.assertEquals(rec.getWhen(), rec.getLastDate());
                } else {
                    Assert.assertEquals(jsonRecord.getLong("lastDate"), rec.getLastDate().getTime());
                }
                Assert.assertEquals(jsonRecord.getString("notes"), rec.getNotes());
                Assert.assertEquals(jsonRecord.getInt("number"), rec.getNumber().intValue());
                Assert.assertEquals(jsonRecord.getInt("survey_id"), rec.getSurvey().getId().intValue());
                if(JSONNull.getInstance().equals(jsonRecord.get("censusMethod_id"))) {
                    Assert.assertNull(rec.getCensusMethod());
                } else {
                    Assert.assertEquals(jsonRecord.getInt("censusMethod_id"), rec.getCensusMethod().getId().intValue());
                    
                    if(Taxonomic.TAXONOMIC.equals(rec.getCensusMethod().getTaxonomic()) || 
                    		Taxonomic.OPTIONALLYTAXONOMIC.equals(rec.getCensusMethod().getTaxonomic())) {
                        Assert.assertEquals(jsonRecord.getInt("taxon_id"), rec.getSpecies().getId().intValue());
                    } else {
                        Assert.assertNull(rec.getSpecies());
                    }
                }
            } else if(AttributeValue.class.getSimpleName().equals(jsonResult.getString("klass"))) {
                Assert.assertTrue(syncDataMap.containsKey(jsonResult.getString("id")));
                JSONObject jsonRecAttr = syncDataMap.get(jsonResult.getString("id"));
                
                AttributeValue recAttr = recordDAO.getAttributeValue(jsonResult.getInt("server_id"));
                Assert.assertNotNull(recAttr);
                
                Attribute attr = recAttr.getAttribute(); 
                switch(attr.getType()) {
                    case INTEGER:
                    case INTEGER_WITH_RANGE:
                        Assert.assertEquals(jsonRecAttr.getString("value"),
                                            recAttr.getStringValue());
                        if(!recAttr.getStringValue().isEmpty()) {
                            Assert.assertEquals(jsonRecAttr.getInt("value"), 
                                                recAttr.getNumericValue().intValue());
                        }
                        break;
                    case DECIMAL:
                        Assert.assertEquals(jsonRecAttr.getString("value"),
                                            recAttr.getStringValue());
                        if(!recAttr.getStringValue().isEmpty()) {
                            Assert.assertEquals(jsonRecAttr.getDouble("value"), 
                                                recAttr.getNumericValue().doubleValue());
                        }
                        break;
                    case DATE:
                        if((jsonRecAttr.get("value") instanceof JSONNull) || 
                                jsonRecAttr.get("value").toString().isEmpty()) {
                            Assert.assertEquals("", recAttr.getStringValue());
                            Assert.assertEquals(null, recAttr.getDateValue());
                        } else {
                            Date date = new Date(jsonRecAttr.getLong(("value")));
                            Assert.assertEquals(dateFormat.format(date), recAttr.getStringValue());
                            Assert.assertEquals(date, recAttr.getDateValue());
                        }
                        break;
                    case STRING:
                    case STRING_AUTOCOMPLETE:
                    case TEXT:
                    case STRING_WITH_VALID_VALUES:
                        Assert.assertEquals(jsonRecAttr.getString("value"),
                                            recAttr.getStringValue());
                        break;
            
                    case IMAGE:
                        if(!recAttr.getStringValue().isEmpty()) {
                            String imgText;
                            if(jsonRecAttr.getInt("server_id") == 0) {
                                imgText = attr.getName();
                            } else {
                                imgText = "Edited "+attr.getName();
                            }
                            String expectedEncodedBase64 = Base64.encodeBytes(createImage(72,72,imgText));
                            File targetFile = fileService.getFile(recAttr, recAttr.getStringValue()).getFile();
                            BufferedImage targetImg = ImageIO.read(targetFile);
                            
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(targetImg, "png", baos);
                            baos.flush();
                            String targetEncodedBase64 = Base64.encodeBytes(baos.toByteArray());
                            baos.close();
                            
                            Assert.assertEquals(expectedEncodedBase64, targetEncodedBase64);
                        }
                        break;
                        
                    default:
                        throw new IllegalArgumentException();
                }
                
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private String generateSyncData(boolean blankRecAttr) throws IOException {
        List<Map<String,Object>> syncData = new ArrayList<Map<String, Object>>();
        for(Survey survey : surveyDAO.getActivePublicSurveys(false)) {
            List<IndicatorSpecies> taxaList = new ArrayList<IndicatorSpecies>(survey.getSpecies());
            if(taxaList.isEmpty()) {
                taxaList = taxaDAO.getIndicatorSpecies();
            }
            
            for(CensusMethod method : survey.getCensusMethods()) {
                IndicatorSpecies taxon = taxaList.get(rand.nextInt(taxaList.size()));
                syncData.add(createJSONRecord(survey, method, taxon, null, blankRecAttr));
            }
            IndicatorSpecies taxon = taxaList.get(rand.nextInt(taxaList.size()));
            syncData.add(createJSONRecord(survey, null, taxon, null, blankRecAttr));
        }
        
        return JSONArray.fromObject(syncData).toString();
    }
    
    private String generateUpdateSyncData(boolean blankRecAttr) throws IOException {
        List<Map<String,Object>> syncData = new ArrayList<Map<String, Object>>();
        for(Survey survey : surveyDAO.getActivePublicSurveys(false)) {
            
            List<IndicatorSpecies> taxaList = new ArrayList<IndicatorSpecies>(survey.getSpecies());
            if(taxaList.isEmpty()) {
                taxaList = taxaDAO.getIndicatorSpecies();
            }
            
            Set<User> users = new HashSet<User>();
            users.add(getRequestContext().getUser());
            for(Record record : recordDAO.getRecords(survey, users)) {
                IndicatorSpecies taxon = taxaList.get(rand.nextInt(taxaList.size()));
                
                syncData.add(createJSONRecord(survey, record.getCensusMethod(), taxon, record, blankRecAttr));
            }
        }
        return JSONArray.fromObject(syncData).toString();
    }
    
    private Map<String, Object> createJSONRecord(Survey survey, CensusMethod method, IndicatorSpecies taxon, Record record, boolean blankRecAttr) throws IOException {
        Map<String, Object> rec = new HashMap<String, Object>();
        
        // Mandatory Stuff First.
        rec.put("id", UUID.randomUUID().toString());
        rec.put("server_id", record == null ? "0" : record.getId().toString());
        rec.put("latitude", record == null ? -31.95222 : -32.962339);
        rec.put("longitude", record == null ? 115.85889 : 117.9422);
        rec.put("accuracy", record == null ? 5: 15);
        rec.put("when", System.currentTimeMillis());
        rec.put("lastDate", rand.nextBoolean() ? null : System.currentTimeMillis());
        rec.put("notes", record == null ? "notes" : "edited notes");
        rec.put("number", rand.nextInt(5));
        rec.put("survey_id", survey.getId());
        
        if(method != null) {
            rec.put("censusMethod_id", method.getId());
            if(Taxonomic.TAXONOMIC.equals(method.getTaxonomic()) || 
            		Taxonomic.OPTIONALLYTAXONOMIC.equals(method.getTaxonomic())) {
                rec.put("taxon_id", taxon.getId());
            } else {
                rec.put("taxon_id", null);
            }
        }
        
        List<Map<String,Object>> recAttrs = new ArrayList<Map<String, Object>>();
        if(record == null) {
            for(Attribute attr : survey.getAttributes()) {
                if(!AttributeType.FILE.equals(attr.getType())) {
                    recAttrs.add(createJSONRecordAttribute(attr, null, blankRecAttr));
                }
            }
            
            if(method != null) {
                for(Attribute attr : method.getAttributes()) {
                    if(!AttributeType.FILE.equals(attr.getType())) {
                        recAttrs.add(createJSONRecordAttribute(attr, null, blankRecAttr));
                    }
                }
                if(Taxonomic.TAXONOMIC.equals(method.getTaxonomic()) || 
                		Taxonomic.OPTIONALLYTAXONOMIC.equals(method.getTaxonomic())) {
                    for(Attribute attr : taxon.getTaxonGroup().getAttributes()) {
                        if(!AttributeType.FILE.equals(attr.getType())) {
                            recAttrs.add(createJSONRecordAttribute(attr, null, blankRecAttr));
                        }
                    }
                }
            }
        } else {
            for(AttributeValue recordAttribute : record.getAttributes()) {
                recAttrs.add(createJSONRecordAttribute(recordAttribute.getAttribute(), recordAttribute, blankRecAttr));
            }
        }
        
        rec.put("attributeValues", recAttrs);

        return rec;
    }

    private Map<String, Object> createJSONRecordAttribute(Attribute attr, 
                                                          AttributeValue recordAttribute,
                                                          boolean blankData) throws IOException {
        
        Map<String, Object> recAttr = new HashMap<String, Object>();
        recAttr.put("id", UUID.randomUUID().toString());
        recAttr.put("server_id", recordAttribute == null ? "0" : recordAttribute.getId().toString());
        recAttr.put("attribute_id", attr.getId());
        
        if(blankData) {
            recAttr.put("value", "");
        } else {
            switch(attr.getType()) {
                case INTEGER:
                case INTEGER_WITH_RANGE:
                    recAttr.put("value", rand.nextInt(5));
                    break;
                case DECIMAL:
                    recAttr.put("value", rand.nextDouble() * 5.0d);
                    break;
                case DATE:
                    recAttr.put("value", rand.nextBoolean() ? System.currentTimeMillis() : "");
                    break;
                case STRING:
                case STRING_AUTOCOMPLETE:
                case TEXT:
                    recAttr.put("value", recordAttribute == null ? attr.getDescription() : "Edited "+attr.getDescription());
                    break;
                case STRING_WITH_VALID_VALUES:
                    // Just get the first option.
                    recAttr.put("value", recordAttribute == null ? attr.getOptions().get(0).getValue() : attr.getOptions().get(1).getValue());
                    break;
                case IMAGE:
                    String imgText = recordAttribute == null ? attr.getName() : "Edited "+attr.getName();
                    String encodedBase64 = Base64.encodeBytes(createImage(72,72,imgText));
                    recAttr.put("value", encodedBase64);
                    break;
                default:
                    throw new IllegalArgumentException("Cannot handle attribute with type: "+attr.getType());
            }
        }
        return recAttr;
    }

    private void createTestData() throws Exception {
        ApplicationContext appContext = getRequestContext().getApplicationContext();
        TestDataCreator testDataCreator = new TestDataCreator(appContext);
        
        testDataCreator.createTaxonGroups(2, 0, true);
        testDataCreator.createTaxa(3, 0);
        testDataCreator.createTaxonProfile();
        testDataCreator.createSurvey(1, 0);
        
        CensusMethod nonTaxonomicParentMethod = createCensusMethod("Non Taxonomic Parent Method", Taxonomic.NONTAXONOMIC, null);
        createCensusMethod("Non Taxonomic Child Method", Taxonomic.NONTAXONOMIC, nonTaxonomicParentMethod);
        
        CensusMethod taxonomicParentMethod = createCensusMethod("Taxonomic Parent Method", Taxonomic.TAXONOMIC, null);
        createCensusMethod("Taxonomic Child Method", Taxonomic.TAXONOMIC, taxonomicParentMethod);
        
        CensusMethod optionallyTaxonomicParentMethod = createCensusMethod("Optionally Taxonomic Parent Method", Taxonomic.OPTIONALLYTAXONOMIC, null);
        createCensusMethod("OPTIONALLYTaxonomic Child Method", Taxonomic.OPTIONALLYTAXONOMIC, optionallyTaxonomicParentMethod);
        
        CensusMethod basicMethod = createCensusMethod("Basic Method", Taxonomic.TAXONOMIC, null);
        
        // Just created a survey so we know there is exactly one survey.
        Survey survey = surveyDAO.getActivePublicSurveys(false).get(0);
        survey.getCensusMethods().add(nonTaxonomicParentMethod);
        survey.getCensusMethods().add(taxonomicParentMethod);
        survey.getCensusMethods().add(basicMethod);
        
        surveyDAO.save(survey);
    }
    
    private CensusMethod createCensusMethod(String name, Taxonomic taxonomic, CensusMethod parent) {
        CensusMethod method = new CensusMethod();
        method.setName(name);
        method.setTaxonomic(taxonomic);
        method.setType("General");
        method.setDescription(name + " Description");
        
        List<Attribute> attributeList = method.getAttributes();
        for(AttributeType attrType : AttributeType.values()) {
            Attribute attr = new Attribute();
            attr.setRequired(true);
            attr.setName(attrType.toString());
            attr.setTypeCode(attrType.getCode());
            attr.setScope(null);
            attr.setTag(false);
            
            if(AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                List<AttributeOption> optionList = new ArrayList<AttributeOption>();
                for(int i=0; i<4; i++) {
                    AttributeOption opt = new AttributeOption();
                    opt.setValue(String.format("Option %d", i));
                    opt = taxaDAO.save(opt);
                    optionList.add(opt);
                }
                attr.setOptions(optionList);
            }
            
            attr = taxaDAO.save(attr);
            attributeList.add(attr);
        }
        
        censusMethodDAO.save(method);
        if(parent != null) {
            parent.getCensusMethods().add(method);
            censusMethodDAO.save(parent);
        }
        return method;
    }
    
    private byte[] createImage(int width, int height, String text) throws IOException {
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
}
