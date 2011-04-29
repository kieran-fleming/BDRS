package au.com.gaiaresources.bdrs.controller.webservice;


import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.bytecode.opencsv.CSVWriter;
import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordAttribute;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;


/**
 * The Survey Service provides a web API for Survey based services.
 */
@Controller
public class SurveyService extends AbstractController {

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private LocationDAO locationDAO;
    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private LocationService locationService;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private SessionFactory sessionFactory;

    @RequestMapping(value="/webservice/survey/surveysForUser.htm", method=RequestMethod.GET)
    public void surveysForUser(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        if(request.getParameter("ident") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String ident = request.getParameter("ident");
        User user = userDAO.getUserByRegistrationKey(ident);
        if(user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        List<Survey> surveyList = surveyDAO.getActiveSurveysForUser(user);
        JSONArray array = new JSONArray();
        for(Survey s : surveyList) {
            array.add(s.flatten());
        }

    	// support for JSONP
    	if (request.getParameter("callback") != null) {
    		response.setContentType("application/javascript");        	
    		response.getWriter().write(request.getParameter("callback") + "(");
    	} else {
    		response.setContentType("application/json");
    	}

        response.getWriter().write(array.toString());
    	if (request.getParameter("callback") != null) {
    		response.getWriter().write(");");
    	}
    }

    @RequestMapping(value="/webservice/survey/speciesForSurvey.htm", method=RequestMethod.GET)
    public void speciesForSurvey(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "surveyId", defaultValue = "0") int surveyPk,
            @RequestParam(value = "q", defaultValue = "") String speciesSearch)
            throws IOException {
        List<IndicatorSpecies> speciesList;
        if (surveyDAO.countSpeciesForSurvey(surveyPk) == 0) {
            speciesList = taxaDAO.getIndicatorSpeciesByNameSearch(speciesSearch);
        } else {
            speciesList = surveyDAO.getSpeciesForSurveySearch(surveyPk, speciesSearch);
        }

        JSONArray array = new JSONArray();
        for(IndicatorSpecies species : speciesList) {
        	Map<String,Object> flattendSpecies = species.flatten();
        	String sn = species.getScientificName();
        	String cn = species.getCommonName();
        	flattendSpecies.put("label", sn + " | " + cn);
        	flattendSpecies.put("value",sn);
            array.add(flattendSpecies);
        }

        response.setContentType("application/json");
        response.getWriter().write(array.toString());
    }
    
    @RequestMapping(value="/webservice/survey/surveySpeciesForTaxon.htm", method=RequestMethod.GET)
    public void surveySpeciesForTaxon(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value="surveyId", defaultValue="0") int surveyPk,
                                    @RequestParam(value="taxonId", defaultValue="0") int taxonGroupId)
        throws IOException {

        List<IndicatorSpecies> speciesList = surveyDAO.getSurveySpeciesForTaxon(surveyPk, taxonGroupId);

        JSONArray array = new JSONArray();
        for(IndicatorSpecies species : speciesList) {
            array.add(species.flatten());
        }

        response.setContentType("application/json");
        response.getWriter().write(array.toString());
    }
    
    
    @RequestMapping(value="/webservice/survey/taxaForSurvey.htm", method=RequestMethod.GET)
    public void taxaForSurvey(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value="surveyId", defaultValue="0") int surveyPk)
        throws IOException {

    	
        List<TaxonGroup> taxaList = taxaDAO.getTaxonGroup(surveyDAO.getSurvey(surveyPk));

        JSONArray array = new JSONArray();
        for(TaxonGroup taxon : taxaList) {
            array.add(taxon.flatten());
        }

        response.setContentType("application/json");
        response.getWriter().write(array.toString());
    }
    
    /* Retrieves features related to a specific survey.
     * E.g. idtool, fieldguide, recording
     */
    @RequestMapping(value="/webservice/survey/featuresForSurvey.htm", method=RequestMethod.GET)
    public void featuresForSurvey(HttpServletRequest request,HttpServletResponse response)
        throws IOException {
    	
        JSONArray array = new JSONArray();
        // build 4 test buttons and convert them to json
        HashMap<String, String> btnmap1 = new HashMap<String, String>();
        btnmap1.put("btnid", Integer.toString(0));
        btnmap1.put("btnlabelname", "fieldguide");
        btnmap1.put("btniconurl", "");
        btnmap1.put("btnurl", "#servicetype=survey&servicename=taxaForSurvey&surveyId=1");
        array.add(btnmap1);
        HashMap<String, String> btnmap2 = new HashMap<String, String>();
        btnmap2.put("btnid", Integer.toString(0));
        btnmap2.put("btnlabelname", "aLabel1");
        btnmap2.put("btniconurl", "");
        btnmap2.put("btnurl", "#servicetype=&servicename=");
        array.add(btnmap2);
        HashMap<String, String> btnmap3 = new HashMap<String, String>();
        btnmap3.put("btnid", Integer.toString(0));
        btnmap3.put("btnlabelname", "aLabel2");
        btnmap3.put("btniconurl", "");
        btnmap3.put("btnurl", "#servicetype=&servicename=");
        array.add(btnmap3);
        HashMap<String, String> btnmap4 = new HashMap<String, String>();
        btnmap4.put("btnid", Integer.toString(0));
        btnmap4.put("btnlabelname", "aLabel3");
        btnmap4.put("btniconurl", "");
        btnmap4.put("btnurl", "#servicetype=&servicename=");
        array.add(btnmap4);


        response.setContentType("application/json");
        response.getWriter().write(array.toString());
    }
    	
    	@RequestMapping(value="/webservice/survey/attributesForSurvey.htm", method=RequestMethod.GET)
        public void attributesForSurvey(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @RequestParam(value="surveyId", defaultValue="0") int surveyId)
            throws IOException {
    		
    		Survey survey = surveyDAO.getSurvey(surveyId);
    		List<Attribute> surveyatts = survey.getAttributes();
    		JSONArray array = new JSONArray();
            for(Attribute surveyatt : surveyatts) {
            	List<AttributeOption> options =  surveyatt.getOptions();
            	Map<String, Object> optionsMap = new HashMap<String, Object>();
            	for(AttributeOption option: options){
            		optionsMap.put(option.getId().toString(), option.getValue());
            	}
            	Map<String, Object> flattenedSurveyAtt = surveyatt.flatten();
            	flattenedSurveyAtt.put("options", optionsMap);
                array.add(flattenedSurveyAtt);
            }
         
            response.setContentType("application/json");
            response.getWriter().write(array.toString());
        }
    	
    	@RequestMapping(value="/webservice/survey/locationsForSurvey.htm", method=RequestMethod.GET)
        public void locationsForSurvey(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @RequestParam(value="regkey", defaultValue="0") String regkey,
                                        @RequestParam(value="surveyId", defaultValue="0") int surveyId)
            throws IOException {
    		
    		//check authorisation
   		 	if(regkey == "0") {
   	            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
   	        }
   	        User user = userDAO.getUserByRegistrationKey(regkey);
   	        if(user == null) {
   	            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
   	        }
    		
    		
    		
    		JSONArray array = new JSONArray();
            List<Location> survey_locations = surveyDAO.getSurvey(surveyId).getLocations();
            for(Location location : survey_locations){
            	Double latitude = location.getLocation().getX();
            	Double longitude = location.getLocation().getY();
            	Map<String,Object> locationFlat = location.flatten();
            	locationFlat.put("latitude", latitude);
            	locationFlat.put("longitude", longitude);
            	array.add(locationFlat);
            }
            
            response.setContentType("application/json");
            response.getWriter().write(array.toString());
        }
    	
    	/* Creates a new record
    	 * @param request		contains the record form data
    	 * @return jsonObject	contains the record id of the inserted record 
    	 */
    	@RequestMapping(value="/webservice/survey/saveRecord.htm", method=RequestMethod.GET)
        public void saveRecord(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @RequestParam(value="regkey", defaultValue="0") String regkey)
            throws IOException, ParseException {
    		
    		//check authorisation
    		 if(regkey == "0") {
    	            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    	        }
    	        User user = userDAO.getUserByRegistrationKey(regkey);
    	        if(user == null) {
    	        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    	        }
    	    
    	    Record record;
    		Survey survey = surveyDAO.getSurvey(new Integer(request.getParameter("survey")));
			List<Attribute> surveyatts = survey.getAttributes();
			Set<RecordAttribute> newAttributes = new HashSet<RecordAttribute>();
			String string_codes = new String("TASVSTIMFI"); //
			String numeric_codes= new String("INDE"); //
			record = new Record();
			
			//set standard fields
	        Date date = new Date(new Long(request.getParameter("when")));
	        date.setTime(new Long(request.getParameter("time")));
	        record.setSpecies(taxaDAO.getIndicatorSpecies(new Integer(request.getParameter("selected_species"))));
	       
	        String number_locationid = request.getParameter("locationid");
			if(number_locationid != ""){
				//set location if there is one
				record.setLocation(locationDAO.getLocation(Integer.valueOf(number_locationid)));
			}
	        
			record.setPoint(locationService.createPoint(
							new Double(request.getParameter("locationLatitude")), 
							new Double(request.getParameter("locationLongitude"))));
	        record.setTime(date.getTime());
			record.setWhen(date);
			
			String number_string = request.getParameter("number");
			if(number_string != ""){
				//set number seen if there are any
				record.setNumber(Integer.valueOf(number_string));
			}
			
			record.setNotes(request.getParameter("notes"));
	        record.setLastDate(date);
	        record.setLastTime(date.getTime());
			record.setSurvey(survey);
			record.setUser(user);
			record.setHeld(true);
			
			//set custom fields
			for(Attribute att : surveyatts){
				String rec_att_value = request.getParameter("attribute"+att.getId());
				RecordAttribute recAttr = new RecordAttribute();
				recAttr.setAttribute(att);
				if(string_codes.contains(att.getType().getCode())){
					recAttr.setStringValue(rec_att_value);
				}else if(numeric_codes.contains(att.getType().getCode())){
					if(rec_att_value != null && rec_att_value != ""){
						recAttr.setNumericValue(new BigDecimal(rec_att_value));
					}
				}else{
					if(rec_att_value != null && rec_att_value != ""){
						Date date_custom = new SimpleDateFormat("dd MMM yyyy").parse(rec_att_value);
						Calendar cal_custom = new GregorianCalendar();
						cal_custom.setTime(date_custom);
						date_custom = cal_custom.getTime();
						recAttr.setDateValue(date_custom);
					}
				}
				
				//save attribute and store in Set
				newAttributes.add(recordDAO.saveRecordAttribute(recAttr));
			}
			//add the new RecordAttributes to the record 
			record.setAttributes(newAttributes);
			//save record
			record = recordDAO.saveRecord(record);
			//return record id as JSON
			JSONObject jsonObject = new JSONObject();
			jsonObject.element("onlineRecordId", record.getId());
            response.setContentType("application/json");
            response.getWriter().write(jsonObject.toString());	
        }
    	
    	/* Updates an existing record
    	 * @param request		contains the record form data that includes the id of the record that needs to be updated
    	 * @return jsonObject	contains the record id of the updated record 
    	 */
    	@RequestMapping(value="/webservice/survey/updateRecord.htm", method=RequestMethod.GET)
        public void updateRecord(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @RequestParam(value="regkey", defaultValue="0") String regkey)
            throws IOException, ParseException {
    		//check authorisation
    		 if(regkey == "0") {
    	            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    	        }
    	        User user = userDAO.getUserByRegistrationKey(regkey);
    	        if(user == null) {
    	            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    	        }
    	    Record record;
    		Survey survey = surveyDAO.getSurvey(new Integer(request.getParameter("survey")));
			List<Attribute> surveyatts = survey.getAttributes();
			String string_codes = new String("TASVSTIMFI"); 
			String numeric_codes= new String("INDE"); 
			Integer rec_id = new Integer(request.getParameter("record"));
			record = recordDAO.getRecord(rec_id);
			
			//set standard fields
	        Date date = new Date(new Long(request.getParameter("when")));
	        date.setTime(new Long(request.getParameter("time")));
	        record.setSpecies(taxaDAO.getIndicatorSpecies(new Integer(request.getParameter("selected_species"))));
	        if(request.getParameter("locationid")!= null){
	        	//set location if there is one
	        	record.setLocation(locationDAO.getLocation(Integer.valueOf(request.getParameter("locationid"))));
	        }
			record.setPoint(locationService.createPoint(
							new Double(request.getParameter("locationLatitude")), 
							new Double(request.getParameter("locationLongitude"))));
	        record.setTime(date.getTime());
			record.setWhen(date);
			record.setNumber(new Integer(request.getParameter("number")));
			record.setNotes(request.getParameter("notes"));
	        record.setLastDate(date);
	        record.setLastTime(date.getTime());
			record.setSurvey(survey);
			record.setUser(user);
			record.setHeld(true);
			
			//set custom fields
			Map<Integer, RecordAttribute> recordAttributesMap = new HashMap<Integer, RecordAttribute>();
			Set<RecordAttribute> recordAttributes =  record.getAttributes();
			//convert Set in to Map
			for(RecordAttribute ra : recordAttributes){
				recordAttributesMap.put(ra.getAttribute().getId(), ra);
			}
			//empty set
			recordAttributes.clear();
			
			for(Attribute att : surveyatts){
				RecordAttribute ra = recordAttributesMap.get(att.getId());
				ra.setAttribute(att);
				//update record att
				if(string_codes.contains(att.getType().getCode())){
					ra.setStringValue(request.getParameter("attribute"+att.getId()));
				}else if(numeric_codes.contains(att.getType().getCode())){
					ra.setNumericValue(new BigDecimal(request.getParameter("attribute"+att.getId())));
				}else{
					Date date_custom = new SimpleDateFormat("dd MMM yyyy").parse(request.getParameter("attribute"+att.getId()));
					Calendar cal_custom = new GregorianCalendar();
					cal_custom.setTime(date_custom);
					date_custom = cal_custom.getTime();
					ra.setDateValue(date_custom);
				}
				recordAttributes.add(recordDAO.saveRecordAttribute(ra));
			}
			//add the updated RecordAttributes to the record 
			record.setAttributes(recordAttributes);
			//update record
			record = recordDAO.updateRecord(record);
			//return recordid as JSON
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("online_recordid", record.getId());
            response.setContentType("application/json");
            response.getWriter().write(jsonObject.toString());	
        }

  @RequestMapping(value = "/webservice/survey/checklist.htm", method = RequestMethod.GET)
    public void checklistForSurvey(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "surveyId", defaultValue = "0") int surveyPk,
            @RequestParam(value = "format", defaultValue = "csv") String format)
            throws IOException {

        getRequestContext().getHibernate().setFlushMode(FlushMode.MANUAL);
        Session sesh = sessionFactory.openSession();

        Survey survey = surveyDAO.getSurvey(sesh, surveyPk);

        format = format.toLowerCase();
        boolean csv_format = "csv".equals(format);
        boolean zip_format = "zip".equals(format);
        if (csv_format || zip_format) {
            CSVWriter writer;
            if (csv_format) {
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment;filename=checklist.csv");
                writer = new CSVWriter(response.getWriter(), ',');
            } else {
                // Has to be zip format
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment;filename=checklist.zip");

                ZipOutputStream zos = new ZipOutputStream(
                        response.getOutputStream());
                zos.putNextEntry(new ZipEntry("checklist.csv"));
                writer = new CSVWriter(new PrintWriter(zos), ',');
            }

            writer.writeNext(new String[] { "Rank", "Scientific Name",
                    "Common Name", "Group" });

            String[] speciesArray = new String[4];
            int start = 0;
            int size = 10000;
            List<IndicatorSpecies> surveySpecies = taxaDAO.getIndicatorSpeciesBySurvey(sesh, survey, start, size);
            while (!surveySpecies.isEmpty()) {
                for (IndicatorSpecies species : surveySpecies) {
                    speciesArray[0] = species.getTaxonRank() == null ? new String() : species.getTaxonRank().toString();
                    speciesArray[1] = species.getScientificName();
                    speciesArray[2] = species.getCommonName();
                    speciesArray[3] = species.getTaxonGroup().getName();

                    writer.writeNext(speciesArray);
                }
                writer.flush();

                sesh.clear();
                start = start + surveySpecies.size();
                surveySpecies = taxaDAO.getIndicatorSpeciesBySurvey(sesh, survey, start, size);
            }
            writer.close();

        } else {
            throw new UnsupportedOperationException(
                    String.format("Unknown Checklist Format \"%s\"", format));
        }
    }

}
