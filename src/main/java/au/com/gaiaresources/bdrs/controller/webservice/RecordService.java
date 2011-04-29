package au.com.gaiaresources.bdrs.controller.webservice;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.gaiaresources.bdrs.service.bulkdata.AbstractBulkDataService;
import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordAttribute;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaService;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;

@Controller
public class RecordService extends AbstractController {
	private Logger log = Logger.getLogger(getClass());
	@Autowired
	private SurveyDAO surveyDAO;
	@Autowired
	private RecordDAO recordDAO;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private LocationService locationService;
	@Autowired
	private TaxaService taxaService;
	@Autowired
	private AbstractBulkDataService bulkDataService;

	@RequestMapping(value = "/webservice/record/lastRecords.htm", method = RequestMethod.GET)
	public void getLatestRecords(
			@RequestParam(value = "species", defaultValue = "") String species,
			@RequestParam(value = "user", defaultValue = "0") int userPk,
			@RequestParam(value = "limit", defaultValue = "5") int limit,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// RequestParam user - the user
		// RequestParam limit - the number of records to return
		// RequestParam species - the species to search for optional

		List<Record> recordList = recordDAO.getRecord(userPk, 0, 0, 0, null,
				null, species, limit);

		JSONArray array = new JSONArray();
		for (Record r : recordList) {
			array.add(r.flatten());
		}

		response.setContentType("application/json");
		response.getWriter().write(array.toString());
	}

	@RequestMapping(value = "/webservice/record/lastSpecies.htm", method = RequestMethod.GET)
	public void getLastSpecies(
			@RequestParam(value = "user", defaultValue = "0") int userPk,
			@RequestParam(value = "limit", defaultValue = "5") int limit,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// RequestParam user - the user
		// RequestParam limit - number of last species to return
		// [ { species : {species}, record : {record} }, ... ]
		List<IndicatorSpecies> species = recordDAO
				.getLastSpecies(userPk, limit);
		JSONArray array = new JSONArray();
		for (IndicatorSpecies s : species) {
			List<Record> recordList = recordDAO.getRecord(userPk, 0, 0, 0,
					null, null, s.getScientificName(), 1);
			if (recordList.size() > 0) {
				JSONObject ob = new JSONObject();
				ob.put("species", s.flatten());
				ob.put("record", recordList.get(0).flatten());
				array.add(ob);
			}
		}

		response.setContentType("application/json");
		response.getWriter().write(array.toString());

	}

	@RequestMapping(value = "/webservice/record/searchRecords.htm", method = RequestMethod.GET)
	public void searchRecords(
			@RequestParam(value = "ident", defaultValue = "") String ident,
			@RequestParam(value = "species", defaultValue = "") String species,
			@RequestParam(value = "user", defaultValue = "0") int userPk,
			@RequestParam(value = "group", defaultValue = "0") int groupPk,
			@RequestParam(value = "survey", defaultValue = "0") int surveyPk,
			@RequestParam(value = "taxon_group", defaultValue = "0") int taxonGroupPk,
			@RequestParam(value = "date_start", defaultValue = "01 Jan 1970") Date startDate,
			@RequestParam(value = "date_end", defaultValue = "01 Jan 9999") Date endDate,
			@RequestParam(value = "limit", defaultValue = "5000") int limit,
			HttpServletResponse response) throws IOException {

		User user;
		if (ident.isEmpty()) {
			throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			user = userDAO.getUserByRegistrationKey(ident);
			if (user == null) {
				throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}

		// If you are not the administrator, you can only see your own records.
		if (!user.isAdmin()) {
			userPk = user.getId();
		}

		List<Record> recordList = recordDAO.getRecord(userPk, groupPk,
				surveyPk, taxonGroupPk, startDate, endDate, species, limit);

		JSONArray array = new JSONArray();
		for (Record r : recordList) {
			array.add(r.flatten());
		}

		response.setContentType("application/json");
		response.getWriter().write(array.toString());
	}

	/**
	 * Saves the records of a particular survey in the database.
	 * 
	 * @param surveyId
	 *            The id of the survey of which records are to be stored.
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @throws IOException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/webservice/record/uploadRecords.htm", method = RequestMethod.POST)
	public void uploadRecords(
			@RequestParam(value = "survey", defaultValue = "") Integer surveyId,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ParseException {
		// variables
		Map<String, Object> jsonResponse = new HashMap<String, Object>();
		User user;
		JSONObject jsonRecordObject = JSONObject.fromObject(request
				.getParameter("JSONrecords"));

		Survey survey = surveyDAO.getSurvey(surveyId);
		List<Attribute> surveyatts = survey.getAttributes();
		String string_codes = new String("TASVSTIMFISA"); //
		String numeric_codes = new String("INDE"); //
		Map<String, Integer> onlineRecordIds = new HashMap<String, Integer>();
		JSONObject jsonRecord;

		// authorisation
		user = userDAO.getUserByRegistrationKey(request.getParameter("ident"));
		if (user == null) {
			throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
		}

		// convert JSON records in to Record objects
		for (Object key : jsonRecordObject.keySet()) {
			Record record = new Record();
			Set<RecordAttribute> newAttributes = new HashSet<RecordAttribute>();
			jsonRecord = (JSONObject) jsonRecordObject.get(key);
			// standard attributes
			double latitude = jsonRecord.getDouble("latitude");
			double longitude = jsonRecord.getDouble("longitude");
			record.setPoint(locationService.createPoint(latitude, longitude));
			record.setNotes(jsonRecord.getString("notes"));
			String number_string = request.getParameter("numberseen");
			if (number_string != "" & (number_string != null)) {
				// set number seen if there are any
				record.setNumber(Integer.valueOf(number_string));
			}
			record.setFirstAppearance(true);
			record.setLastAppearance(true);
			record.setUser(user);
			record.setSpecies(taxaService.getIndicatorSpecies(jsonRecord
					.getInt("fkindicatorspeciesid")));
			record.setWhen(new Date(new Long(jsonRecord.getString("when"))));
			record.setTime(new Long(jsonRecord.getString("time")));
			record
					.setLastDate(new Date(
							new Long(jsonRecord.getString("when"))));
			record.setLastTime(new Long(jsonRecord.getString("time")));
			record.setHeld(true);
			record.setSurvey(survey);
			// custom attributes
			JSONArray jsonRecordAttributeList = jsonRecord
					.getJSONArray("attributes");

			for (Attribute att : surveyatts) {
				String rec_att_value = jsonRecordAttributeList.getString(att
						.getId());
				RecordAttribute recAttr = new RecordAttribute();
				recAttr.setAttribute(att);
				if (string_codes.contains(att.getType().getCode())) {
					recAttr.setStringValue(rec_att_value);
				} else if (numeric_codes.contains(att.getType().getCode())) {
					recAttr.setNumericValue(new BigDecimal(rec_att_value));
				} else {
					Date date_custom = new SimpleDateFormat("dd MMM yyyy")
							.parse(rec_att_value);
					Calendar cal_custom = new GregorianCalendar();
					cal_custom.setTime(date_custom);
					date_custom = cal_custom.getTime();
					recAttr.setDateValue(date_custom);
				}
				recAttr = recordDAO.saveRecordAttribute(recAttr);
				// save attribute and store in Set
				newAttributes.add(recAttr);
			}
			// store custom attributes in record
			record.setAttributes(newAttributes);
			// save record
			record = recordDAO.saveRecord(record);
			// add mapping for online and offline record ids to onlineRecordIds
			onlineRecordIds.put(jsonRecord.getString("id"), record.getId());
		}
		jsonResponse.put("succes", "true");
		jsonResponse.put("recordIdsMapping", onlineRecordIds);
		JSONObject jsonObject = JSONObject.fromObject(jsonResponse);
		response.getWriter().write(jsonObject.toString());
	}

	@RequestMapping(value = "/webservice/record/updateRecords.htm", method = RequestMethod.POST)
	public void updateRecords(
			@RequestParam(value = "survey", defaultValue = "") Integer surveyId,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ParseException {
		// variables
		Map<String, Object> jsonResponse = new HashMap<String, Object>();
		User user;
		JSONObject jsonRecordObject = JSONObject.fromObject(request
				.getParameter("JSONrecords"));

		Survey survey = surveyDAO.getSurvey(surveyId);
		List<Attribute> surveyatts = survey.getAttributes();
		String string_codes = new String("TASVSTIMFI"); //
		String numeric_codes = new String("INDE"); //
		Map<String, Integer> onlineRecordIds = new HashMap<String, Integer>();
		JSONObject jsonRecord;

		// authorisation
		user = userDAO.getUserByRegistrationKey(request.getParameter("ident"));
		if (user == null) {
			throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
		}

		// convert JSON records in to Record objects
		for (Object key : jsonRecordObject.keySet()) {
			jsonRecord = (JSONObject) jsonRecordObject.get(key);
			Record record = recordDAO.getRecord(jsonRecord
					.getInt("online_recordid"));
			Set<RecordAttribute> newAttributes = new HashSet<RecordAttribute>();

			// set standard fields
			Date date = new Date(new Long(jsonRecord.getString("when")));
			date.setTime(new Long(jsonRecord.getString("time")));
			record.setSpecies(taxaService.getIndicatorSpecies(jsonRecord
					.getInt("fkindicatorspeciesid")));
			double latitude = jsonRecord.getDouble("latitude");
			double longitude = jsonRecord.getDouble("longitude");
			record.setPoint(locationService.createPoint(latitude, longitude));
			record.setTime(date.getTime());
			record.setWhen(date);
			// record.setNumber(new Integer(request.getParameter("number")));
			record.setNotes(jsonRecord.getString("notes"));
			record.setLastDate(date);
			record.setLastTime(date.getTime());
			record.setSurvey(survey);
			record.setUser(user);
			record.setHeld(true);
			// custom attributes
			JSONArray jsonRecordAttributeList = jsonRecord
					.getJSONArray("attributes");

			// set custom fields
			Map<Integer, RecordAttribute> recordAttributesMap = new HashMap<Integer, RecordAttribute>();
			Set<RecordAttribute> recordAttributes = record.getAttributes();
			// convert Set in to Map
			for (RecordAttribute ra : recordAttributes) {
				recordAttributesMap.put(ra.getAttribute().getId(), ra);
			}
			// empty set
			recordAttributes.clear();

			for (Attribute att : surveyatts) {
				String rec_att_value = jsonRecordAttributeList.getString(att
						.getId());
				RecordAttribute recAttr = new RecordAttribute();
				recAttr.setAttribute(att);
				if (string_codes.contains(att.getType().getCode())) {
					recAttr.setStringValue(rec_att_value);
				} else if (numeric_codes.contains(att.getType().getCode())) {
					recAttr.setNumericValue(new BigDecimal(rec_att_value));
				} else {
					Date date_custom = new SimpleDateFormat("dd MMM yyyy")
							.parse(rec_att_value);
					Calendar cal_custom = new GregorianCalendar();
					cal_custom.setTime(date_custom);
					date_custom = cal_custom.getTime();
					recAttr.setDateValue(date_custom);
				}
				recAttr = recordDAO.saveRecordAttribute(recAttr);
				// save attribute and store in Set
				newAttributes.add(recAttr);
			}
			// add the updated RecordAttributes to the record
			record.setAttributes(recordAttributes);
			// update record
			record = recordDAO.updateRecord(record);
			// add mapping for online and offline record ids to onlineRecordIds
			onlineRecordIds.put(jsonRecord.getString("id"), record.getId());
		}
		jsonResponse.put("succes", "true");
		jsonResponse.put("recordIdsMapping", onlineRecordIds);
		JSONObject jsonObject = JSONObject.fromObject(jsonResponse);
		response.getWriter().write(jsonObject.toString());
	}

	@RequestMapping(value = "/webservice/record/downloadRecords.htm", method = RequestMethod.GET)
	public void downloadRecords(
			@RequestParam(value = "ident", defaultValue = "") String ident,
			@RequestParam(value = "species", defaultValue = "") String species,
			@RequestParam(value = "user", defaultValue = "0") int userPk,
			@RequestParam(value = "group", defaultValue = "0") int groupPk,
			@RequestParam(value = "survey", defaultValue = "1") int surveyPk,
			@RequestParam(value = "taxon_group", defaultValue = "0") int taxonGroupPk,
			@RequestParam(value = "date_start", defaultValue = "01 Jan 1970") Date startDate,
			@RequestParam(value = "date_end", defaultValue = "01 Jan 9999") Date endDate,
			@RequestParam(value = "limit", defaultValue = "5000") int limit,
			HttpServletResponse response) throws IOException {

		// We are changing the flush mode here to prevent checking for dirty
		// objects in the session cache. Normally this is desireable so that
		// you will not receive stale objects however in this situation
		// the controller will only be performing reads and the objects cannot
		// be stale. We are explicitly setting the flush mode here because
		// we are potentially loading a lot of objects into the session cache
		// and continually checking if it is dirty is prohibitively expensive.
		// https://forum.hibernate.org/viewtopic.php?f=1&t=936174&view=next
		getRequestContext().getHibernate().setFlushMode(FlushMode.MANUAL);

		User user;
		if (ident.isEmpty()) {
			throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			user = userDAO.getUserByRegistrationKey(ident);
			if (user == null) {
				throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}

		// If you are not the administrator, you can only see your own records.
		if (!user.isAdmin()) {
			userPk = user.getId();
		}

		List<Record> recordList = recordDAO.getRecord(userPk, groupPk,
				surveyPk, taxonGroupPk, startDate, endDate, species, limit,
				true);

		Survey survey = surveyDAO.getSurvey(surveyPk);

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition",
				"attachment;filename=records_"
						+ new Long(System.currentTimeMillis()) + ".xls");
		bulkDataService.exportSurveyRecords(survey, recordList, response
				.getOutputStream());
	}

	/**
	 * Returns the records of a particular user and survey
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param regkey
	 *            Registration key of the user
	 * @param surveyId
	 *            Id of the survey of which records are requested
	 * @throws IOException
	 */
	@RequestMapping(value = "/webservice/record/recordsForSurvey.htm", method = RequestMethod.GET)
	public void recordsForSurvey(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "regkey", defaultValue = "0") String regkey,
			@RequestParam(value = "surveyId", defaultValue = "0") int surveyId)
			throws IOException {

		// check authorisation
		if (regkey.equals("0")) {
			throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
		}
		User user = userDAO.getUserByRegistrationKey(regkey);
		if (user == null) {
			  response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		// get Records
		Survey survey = surveyDAO.get(surveyId);
		Set<User> users = new HashSet<User>();
		users.add(user);
		List<Record> records = recordDAO.getRecords(survey, users);
		// parse to JSON
		JSONArray array = new JSONArray();
		for (Record r : records) {
			Map<String, Object> flattendRecord = r.flatten();
			flattendRecord.put("commonName", r.getSpecies().getCommonName());
			flattendRecord.put("scientificName", r.getSpecies()
					.getScientificName());
			array.add(flattendRecord);
		}

		// return JSON
		response.setContentType("application/json");
		response.getWriter().write(array.toString());
	}

	@RequestMapping(value = "/webservice/record/getRecordsForLocation.htm", method = RequestMethod.GET)
	public void getRecordsForLocation(
			@RequestParam(value = "ident", required = true) String ident,
			@RequestParam(value = "surveyId", defaultValue = "0", required = true) int surveyPk,
			@RequestParam(value = "locationId", defaultValue = "0", required = true) int locationPk,
			HttpServletResponse response) throws IOException {

		List<Record> recordList = recordDAO.getRecords(ident, surveyPk,
				locationPk);

		JSONArray array = new JSONArray();
		for (Record r : recordList) {
			array.add(r.flatten());
		}
		response.setContentType("application/json");
		response.getWriter().write(array.toString());
	}

	/**
	 * Delete a record of a particular user.
	 * 
	 * @param ident
	 *            The registration key assigned to the user.
	 * @param surveyPk
	 *            Id of the survey of which records are requested
	 * @param recordPk
	 *            Id of the record that needs to be deleted.
	 * @param response
	 *            HttpServletResponse
	 * @throws IOException
	 */
	@RequestMapping(value = "/webservice/record/deleteRecord.htm", method = RequestMethod.GET)
	public void deleteRecord(
			@RequestParam(value = "regkey", required = true) String ident,
			@RequestParam(value = "surveyId", defaultValue = "0", required = true) int surveyPk,
			@RequestParam(value = "recordId", defaultValue = "0", required = true) int recordPk,
			HttpServletResponse response) throws IOException {

		User user;
		if (ident.isEmpty()) {
			throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			user = userDAO.getUserByRegistrationKey(ident);
			if (user == null) {
				throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}

		recordDAO.deleteById(recordPk);
	}

	/**
	 * Deletes records from the database with the given record ids.
	 * 
	 * @param ident
	 *            The users registration key.
	 * @param recordIds
	 *            The ids of the records that need to be deleted.
	 * @throws IOException
	 */
	@RequestMapping(value = "/webservice/record/deleteRecords.htm", method = RequestMethod.POST)
	public void deleteRecords(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "ident", required = true) String ident)
			throws IOException {
		
		JSONObject jsonRecordIdsMap = JSONObject.fromObject(request
				.getParameter("JSONrecords"));
		// Authenticate the user
		User user;
		if (ident.isEmpty()) {
			throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			user = userDAO.getUserByRegistrationKey(ident);
			if (user == null) {
				throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}

		// Delete the records
		for (Object key : jsonRecordIdsMap.keySet()) {
			try {
				Integer id = new Integer((Integer) jsonRecordIdsMap.get(key));
				recordDAO.deleteById(id);
			} catch (Exception e) {
				log.error("The id in the jsonRecordIdsMap is not an Integer.");
			}
		}

		// return true if succesfull
		JSONObject jsonObject = new JSONObject();
		jsonObject.element("succeeded", true);
		response.setContentType("application/json");
		response.getWriter().write(jsonObject.toString());
	}

	@RequestMapping(value = "/webservice/record/getRecordById.htm", method = RequestMethod.GET)
	public void getRecordById(
			@RequestParam(value = "regkey", required = true) String ident,
			@RequestParam(value = "recordId", defaultValue = "0", required = true) int recordPk,
			HttpServletResponse response) throws IOException {
		User user;
		if (ident.isEmpty()) {
			throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			user = userDAO.getUserByRegistrationKey(ident);
			if (user == null) {
				throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}

		Record record = recordDAO.getRecord(recordPk);

		Map<String, Object> flattendRecord = record.flatten();
		flattendRecord.put("commonName", record.getSpecies().getCommonName());
		flattendRecord.put("scientificName", record.getSpecies()
				.getScientificName());
		Map<String, String> attVals = new HashMap<String, String>();
		for (AttributeValue ra : record.getAttributes()) {
			String attId = ra.getAttribute().getId().toString();
			String attVal = ra.getStringValue();
			attVals.put(attId, attVal);
		}
		flattendRecord.put("attributes", attVals);

		JSONObject rec = new JSONObject();
		rec.accumulateAll(flattendRecord);

		// return JSON
		response.setContentType("application/json");
		response.getWriter().write(rec.toString());

	}

	@RequestMapping(value = "/webservice/record/getRecordAttributeById.htm", method = RequestMethod.GET)
	public void getRecordAttributeById(
			@RequestParam(value = "ident", required = true) String ident,
			@RequestParam(value = "recordAttributeId", defaultValue = "0", required = true) int recordAttributePk,
			HttpServletResponse response) throws IOException {

		User user;
		if (ident.isEmpty()) {
			throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			user = userDAO.getUserByRegistrationKey(ident);
			if (user == null) {
				throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}

		RecordAttribute recAttr = recordDAO
				.getRecordAttribute(recordAttributePk);
		JSONObject rec = new JSONObject();
		if (recAttr != null) {
			rec.accumulateAll(recAttr.flatten());
		}
		response.setContentType("application/json");
		response.getWriter().write(rec.toString());
	}

	@InitBinder
	public void initBinder(HttpServletRequest request,
			ServletRequestDataBinder binder) {
		DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(
				dateFormat, true));
	}

	/**
	 * Deletes records from the database with the given record ids.
	 * 
	 * @param ident
	 *            The users registration key.
	 * @param recordIds
	 *            The ids of the records that need to be deleted.
	 * @throws IOException
	 */
	@RequestMapping(value = "/webservice/record/syncToServer.htm", method = RequestMethod.POST)
	public void sync(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "ident", required = true) String ident)
			throws IOException, ParseException {
		
		User user;
		// Authenticate the user
		if (ident.isEmpty()) {
			throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			user = userDAO.getUserByRegistrationKey(ident);
			if (user == null) {
				throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}
		
		Survey survey;
		HashMap<Integer, Survey> surveyMap = new HashMap<Integer, Survey>();
		String string_codes = new String("TASVSTIMFISA"); //
		String numeric_codes = new String("INDE"); //
		Map<String, Integer> onlineRecordIds = new HashMap<String, Integer>();
		Map<String, Integer> updatedRecordIds = new HashMap<String, Integer>();
		Map<String, String> deletedRecordIds = new HashMap<String, String>();
		
		
		// Gets records :  from JSONArray and puts them in a JSONObject
		JSONArray allRecordsObject = JSONArray.fromObject(request.getParameter("JSONrecords"));
		
		// Delete records : gets the JSONArray with records that need to be deleted from the allRecords Object 
		JSONArray deleteRecordsArray = allRecordsObject.getJSONArray(0);
		for (Object delete_r : deleteRecordsArray) {
			try {
				// Type cast record object to JSONObject
				JSONObject jsonDeleteRecord = (JSONObject) delete_r;
				// Get survey from map if exists in the map otherwise get it from the database
				if(surveyMap.containsKey(jsonDeleteRecord.getInt("surveyid"))){
					survey = surveyMap.get(jsonDeleteRecord.getInt("surveyid"));
				}else{
					survey = surveyDAO.getSurvey(jsonDeleteRecord.getInt("surveyid"));
					surveyMap.put(survey.getId(), survey);
				}
				//delete the actual record
				
				if(!jsonDeleteRecord.getString("online_recordid").isEmpty()){
					//there is an online record id
					recordDAO.deleteById(jsonDeleteRecord.getInt("online_recordid"));
				}
				deletedRecordIds.put(jsonDeleteRecord.getString("id"), jsonDeleteRecord.getString("online_recordid"));
			} catch (Exception e) {
				log.error("Something went wrong while trying to delete a record in the Record sync method.");
			}
		}
		
		// Update records : gets the JSONArray with records that need to be updated from the allRecords Object 
		JSONArray updateRecordsArray = allRecordsObject.getJSONArray(1);
		for (Object update_r : updateRecordsArray) {
			// Type cast record object to JSONObject
			JSONObject jsonUpdateRecord = (JSONObject) update_r;
			// Get the original record from the database
			Record updateRecord = recordDAO.getRecord(jsonUpdateRecord.getInt("online_recordid"));
			Set<RecordAttribute> newUpdateAttributes = new HashSet<RecordAttribute>();
			// Get survey from map if exists in the map otherwise get it from the database
			if(surveyMap.containsKey(jsonUpdateRecord.getInt("surveyid"))){
				survey = surveyMap.get(jsonUpdateRecord.getInt("surveyid"));
			}else{
				survey = surveyDAO.getSurvey(jsonUpdateRecord.getInt("surveyid"));
				surveyMap.put(survey.getId(), survey);
			}
			//get attributes from the survey related to the current record
			List<Attribute> surveyatts = survey.getAttributes();
			// set standard fields
			Date date = new Date(new Long(jsonUpdateRecord.getString("when")));
			date.setTime(new Long(jsonUpdateRecord.getString("time")));
			updateRecord.setSpecies(taxaService.getIndicatorSpecies(jsonUpdateRecord.getInt("fkindicatorspeciesid")));
			double latitude = jsonUpdateRecord.getDouble("latitude");
			double longitude = jsonUpdateRecord.getDouble("longitude");
			updateRecord.setPoint(locationService.createPoint(latitude, longitude));
			updateRecord.setTime(date.getTime());
			updateRecord.setWhen(date);
			updateRecord.setNotes(jsonUpdateRecord.getString("notes"));
			updateRecord.setLastDate(date);
			updateRecord.setLastTime(date.getTime());
			updateRecord.setSurvey(survey);
			updateRecord.setUser(user);
			updateRecord.setHeld(true);
			// custom attributes
			Map<String, String> jsonUpdateRecordAttributeList = jsonUpdateRecord.getJSONObject("attributes");
			for (Attribute att : surveyatts) {
				String rec_att_value = jsonUpdateRecordAttributeList.get(att.getId().toString());
				RecordAttribute recAttr = new RecordAttribute();
				recAttr.setAttribute(att);
				if (string_codes.contains(att.getType().getCode())) {
					recAttr.setStringValue(rec_att_value);
				} else if (numeric_codes.contains(att.getType().getCode())) {
					recAttr.setNumericValue(new BigDecimal(rec_att_value));
				} else {
					Date date_custom = new SimpleDateFormat("dd MMM yyyy")
							.parse(rec_att_value);
					Calendar cal_custom = new GregorianCalendar();
					cal_custom.setTime(date_custom);
					date_custom = cal_custom.getTime();
					recAttr.setDateValue(date_custom);
				}
				recAttr = recordDAO.saveRecordAttribute(recAttr);
				// save attribute and store in Set
				newUpdateAttributes.add(recAttr);
			}
			// store custom attributes in record
			updateRecord.setAttributes(newUpdateAttributes);
			// update record
			updateRecord = recordDAO.updateRecord(updateRecord);
			// add mapping for online and off-line record ids to
			// updatedRecordIds
			updatedRecordIds.put(jsonUpdateRecord.getString("id"), updateRecord
					.getId());
		}
		
		// Save records : gets the JSONArray with records that are new from the allRecords Object 
		JSONArray uploadRecordsArray = allRecordsObject.getJSONArray(2);
		for (Object upload_r : uploadRecordsArray) {
			JSONObject jsonUploadRecord = (JSONObject) upload_r;
			// Get survey from map if exists in the map otherwise get it from the database
			if(surveyMap.containsKey(jsonUploadRecord.getInt("surveyid"))){
				survey = surveyMap.get(jsonUploadRecord.getInt("surveyid"));
			}else{
				survey = surveyDAO.getSurvey(jsonUploadRecord.getInt("surveyid"));
				surveyMap.put(survey.getId(), survey);
			}
			Record uploadRecord = new Record();
			Set<RecordAttribute> newUploadAttributes = new HashSet<RecordAttribute>();
			// standard attributes
			double uploadLatitude = jsonUploadRecord.getDouble("latitude");
			double uploadLongitude = jsonUploadRecord.getDouble("longitude");
			uploadRecord.setPoint(locationService.createPoint(uploadLatitude,uploadLongitude));
			uploadRecord.setNotes(jsonUploadRecord.getString("notes"));
			String number_string = jsonUploadRecord.getString("numberseen");
			if (number_string != "" & (number_string != null)) {
				// set number seen if there are any
				uploadRecord.setNumber(Integer.valueOf(number_string));
			}
			uploadRecord.setFirstAppearance(true);
			uploadRecord.setLastAppearance(true);
			uploadRecord.setUser(user);
			uploadRecord.setSpecies(taxaService
					.getIndicatorSpecies(jsonUploadRecord
							.getInt("fkindicatorspeciesid")));
			uploadRecord.setWhen(new Date(new Long(jsonUploadRecord.getString("when"))));
			uploadRecord.setTime(new Long(jsonUploadRecord.getString("time")));
			uploadRecord.setLastDate(new Date(new Long(jsonUploadRecord
					.getString("when"))));
			uploadRecord.setLastTime(new Long(jsonUploadRecord
					.getString("time")));
			uploadRecord.setHeld(true);
			uploadRecord.setSurvey(survey);
			// custom attributes
			Map<String, String> jsonUploadRecordAttributeList = jsonUploadRecord.getJSONObject("attributes");
			//get attributes from the survey related to the current record
			List<Attribute> surveyatts = survey.getAttributes();
			for (Attribute att : surveyatts) {
				String rec_att_value = jsonUploadRecordAttributeList.get(att
						.getId().toString());
				RecordAttribute recAttr = new RecordAttribute();
				recAttr.setAttribute(att);
				if (string_codes.contains(att.getType().getCode())) {
					recAttr.setStringValue(rec_att_value);
				} else if (numeric_codes.contains(att.getType().getCode())) {
					recAttr.setNumericValue(new BigDecimal(rec_att_value));
				} else {
					Date date_custom = new SimpleDateFormat("dd MMM yyyy")
							.parse(rec_att_value);
					Calendar cal_custom = new GregorianCalendar();
					cal_custom.setTime(date_custom);
					date_custom = cal_custom.getTime();
					recAttr.setDateValue(date_custom);
				}
				recAttr = recordDAO.saveRecordAttribute(recAttr);
				// save attribute and store in Set
				newUploadAttributes.add(recAttr);
			}
			// store custom attributes in record
			uploadRecord.setAttributes(newUploadAttributes);
			// save record
			uploadRecord = recordDAO.saveRecord(uploadRecord);
			// add mapping for online and off-line record ids to
			// onlineRecordIds
			onlineRecordIds.put(jsonUploadRecord.getString("id"), uploadRecord
					.getId());
		}

		JSONObject jsonReturnObject = new JSONObject();
		jsonReturnObject.element("succeeded", true);
		jsonReturnObject.element("deleteResponse", deletedRecordIds);
		jsonReturnObject.element("updateResponse", updatedRecordIds);
		jsonReturnObject.element("uploadResponse", onlineRecordIds);
		response.setContentType("application/json");
		response.getWriter().write(jsonReturnObject.toString());
	}
}
