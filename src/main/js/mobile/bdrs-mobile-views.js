function renderHome(){
    var home = "";
    home += tmpl('listItemTmpl',{'id': 'surveys', 'listItemIconUrl':'/BDRS/images/bdrs/mobile/40x40_Survey_NoBorder.png', 'linkUrl': '#/survey', 'alt': 'surveys', 'listItemText': 'surveys', 'listItemSubText': 'list of surveys that you are signed up for' });
    home += tmpl('listItemTmpl',{'id': 'help', 'listItemIconUrl':'/BDRS/images/bdrs/mobile/40x40_Help_NoBorder.png', 'linkUrl': '#/help', 'alt': 'help', 'listItemText': 'help', 'listItemSubText': 'information on how to use this application' });
    home += tmpl('listItemTmpl',{'id': 'about', 'listItemIconUrl':'/BDRS/images/bdrs/mobile/40x40_About_NoBorder.png', 'linkUrl': '#/about', 'alt': 'about', 'listItemText': 'about', 'listItemSubText': 'information on Gaia Resources' });
    home += tmpl('listItemTmpl',{'id': 'contact', 'listItemIconUrl':'/BDRS/images/bdrs/mobile/40x40_Contact_NoBorder.png', 'linkUrl': '#/contact', 'alt': 'contact', 'listItemText': 'contact', 'listItemSubText': 'ways of getting in contact' });
	home = tmpl('listTmpl',{'listItems':home});
    setMainContent(home);
}

/**
 * 
 * @param JSON_data
 * @return
 */
function renderSurveyList(map){
	var view = "";
	for(var i = 0; i<map.surveys.length; i++){
		view += tmpl('listItemTmpl',{'id': map.surveys[i].id, 'listItemIconUrl':'/BDRS/images/bdrs/mobile/sheet_40x40.png', 'linkUrl': '#/survey/?sid=' + map.surveys[i].id, 'alt': map.surveys[i].name, 'listItemText': map.surveys[i].name, 'listItemSubText': map.surveys[i].description });
	}
	view = tmpl('listTmpl',{'listItems':view});
	setMainContent(view);
}

/**
 * 
 * @param JSON_data
 * @return
 */
function renderRecordForm(map){
	/*
	 * Build form view
	 */
	var view = "";
	var surveyAttributes = map.surveyAttributes;
	//for(var surveyatt in map.surveyAttributes){
	for(var i=0; i<surveyAttributes.length; i++){
		var content = "";
		var validationRules = "";
		var attribute = surveyAttributes[i];
		switch(attribute.typeCode){
			case "SV":
				//selection
				var selectoptions ="";
				var options = attribute.options;
				//for(var opt in attribute.options){
				for(var i=0; i<options.length; i++){
					selectoptions += tmpl("inputSelectOptionTmpl",options[i]);
				}
				if(attribute.required != null && attribute.required == 'true'){
					validationRules = "validate(required)";
				}else{
					validationRules = "";
				}
				content = tmpl("inputSelectTmpl",{'content': selectoptions, 'id':attribute.id, 'name':attribute.id, "vRules":validationRules});
				break;
			case "IN":
				//integer
				if(attribute.required != null && attribute.required == 'true'){
					validationRules = "validate(integer)";
				}else{
					validationRules = "validate(integerOrBlank)";
				}
				content = tmpl("inputTextTmpl",{'id':attribute.id,'name':attribute.id,'value':"", "vRules":validationRules});
				break;
			case "DE":
				//decimal
				if(attribute.required != null && attribute.required == 'true'){
					validationRules = "validate(number)";
				}else{
					validationRules = "validate(numberOrBlank)";
				}
				content = tmpl("inputTextTmpl",{'id':attribute.id,'name':attribute.id,'value':"", "vRules":validationRules});
				break;
			case "DA":
				//date
				if(attribute.required != null && attribute.required == 'true'){
					validationRules = "validate(date, required)";
				}else{
					validationRules = "validate(date)";
				}
				content = tmpl("inputDateTmpl",{'id':attribute.id,'name':attribute.id,'value':"", "vRules":validationRules});
				break;
			case "ST":
				//short text
				if(attribute.required != null && attribute.required == 'true'){
					validationRules = "validate(required)";
				}else{
					validationRules = "";
				}
				content = tmpl("inputTextTmpl",{'id':attribute.id,'name':attribute.id,'value':"", "vRules":validationRules});
				break;
			case "TA":
				//long text
				if(attribute.required != null && attribute.required == 'true'){
					validationRules = "validate(required)";
				}else{
					validationRules = "";
				}
				content= tmpl("inputTextAreaTmpl",{'id':attribute.id,'name':attribute.id,'value':"", "vRules":validationRules});
				break;
			case "IM":
				//image file
				content = "Img attachments are not supported yet.";
				break;
			case "FI":
				//data file
				content = "File attachments are not supported yet.";
				break;
			case "SA":
				//short text autocomplete
				if(attribute.required != null && attribute.required == 'true'){
					validationRules = "validate(required)";
				}else{
					validationRules = "";
				}
				//TODO: autocomplete part of the text input
				content = tmpl("inputTextTmpl",{'id':attribute.id,'name':attribute.id,'value':"", "vRules":validationRules});
				break;
			default:
				view="Something went wrong.";
		}
		
		view += tmpl("inputLabelTmpl",{
			'content':content,
			'labelname':attribute.description,
			'labelfor':attribute.id
				});
		
	}
	setMainContent(tmpl("formRecordTmpl", {'content':view}));
	//jQuery(".record_form_item:even").css("background-color","#f2f1f0");
	
	/*
	 * Populate form with available species
	 * and locations.
	 */
	setDatePickerOnFields();
	jQuery("#survey_species_search").autocomplete({
	    source: getSpeciesAutoCompleteField,
	    select: setSpeciesAutoCompleteField,
	    minLength: 2,
	    delay: 300
	});
	
	
	/*
	 * Enable form validation.
	 */
	jQuery('#recordForm').ketchup({
			errorContainer: jQuery('<div>', {'class': 'ketchup_error_container_alt',html: '<ol></ol>'}),
			initialPositionContainer: function(errorContainer, field) {
			  },
			  positionContainer: function(errorContainer, field) {},
			  showContainer: function(errorContainer) {errorContainer.slideDown('fast');},
			  hideContainer: function(errorContainer) {errorContainer.slideUp('fast');}
	});
	
	/*
	 * If forms is for updating pre-popluate fields with record data,
	 * else pre-populate date and time field with current time.
	 */
	if(map.action == "edit"){
		var recordId = jQuery.address.parameter("rid");
        var regkey = getCookie("regkey");
        getRecordById_webSQL({"callback":"setFormData","recordId":recordId,"regkey":regkey});
        ////////jQuery.bbq.pushState({"servicetype":"record","servicename":"getRecordById","callback":"setFormData","recordId":recordId,"regkey":regkey});
		
		/*
		 * Intercept submit after ketchup and update record when ketchup validation is true.
		 */
        var handlers = jQuery('#recordForm').data('events')['submit'];
		var ketchup = handlers[0].handler;
		jQuery('#recordForm').unbind('submit');
		jQuery('#recordForm').bind('submit', function(event) {
			var rVal = ketchup(event);
			if (rVal == undefined) {
				//updateRecord();
				location.replace(jQuery.address.baseURL() + '#/record/?action=update&sid=' + map.sid);
			} 
			return false;
		});
		
	}else{
		
		var curDate = new Date();
		var hours = curDate.getHours();
		if(hours<10){
			hours = "0"+hours;
		}
		var minutes = curDate.getMinutes();
		if(minutes<10){
			minutes = "0"+minutes;
		}
		jQuery('#time').val(hours+":"+minutes);
		jQuery("#when").datepicker('setDate', new Date());
		
		/*
		 * Intercept submit after ketchup and save record when ketchup validation is true.
		 */
		var handlers = jQuery('#recordForm').data('events')['submit'];
		var ketchup = handlers[0].handler;
		jQuery('#recordForm').unbind('submit');
		jQuery('#recordForm').bind('submit', function(event) {
			var rVal = ketchup(event);
			if (rVal == undefined) {
				//saveRecord();
				location.replace(jQuery.address.baseURL() + '#/record/?action=save&sid=' + map.sid);
				return false;
			} 
			return false;
		});
		
	}
}

/**
 * Renders records as a list and accompanies it with a simple menu.
 * @param JSON_data A list of records in json format
 */
function renderRecordsList(map){
	var listMenuItems = "";
	listMenuItems += tmpl('listMenuItemTmpl',{'url':'javascript:void(0)', 'iconUrl':'/BDRS/images/bdrs/mobile/40x40_SelectAll_Grey.png', 'alt':'select all', 'listMenuItemName':'select all', 'func':'selectRecords();' });
	listMenuItems += tmpl('listMenuItemTmpl',{'url':'#/record/?action=add&sid=' + map.sid , 'iconUrl':'/BDRS/images/bdrs/mobile/40x40_Add_Grey.png', 'alt':'add', 'listMenuItemName':'add', 'func':'' });

	listMenuItems += tmpl('listMenuItemTmpl',{'url':'javascript:void(0)', 'iconUrl':'/BDRS/images/bdrs/mobile/40x40_Delete_Grey.png', 'alt':'delete', 'listMenuItemName':'delete', 'func':"deleteRecords();" });
	listMenuItems += tmpl('listMenuItemTmpl',{'url':'#/fieldguide/?sid=' + map.sid, 'iconUrl':'/BDRS/images/bdrs/mobile/40x40_FieldGuide_Grey.png', 'alt':'field guide', 'listMenuItemName':'fieldguide', 'func':'' });
	var listMenu = tmpl('listMenuTmpl',{'listMenuItems':listMenuItems});
	var view = "";
	var records = map.records;
	for( var i=0; i<records.length; i++){
		var record = records[i];
		// format date from long to 'dd M yy'
		var when = jQuery.datepicker.formatDate('dd M yy', new Date(parseFloat(record.when)));
		var onlineRecordId = "";
		if(record.online_recordid){
			onlineRecordId = record.online_recordid;
		}
		view += tmpl("recordsListItemTmpl",{'commonname':record.commonName, 'scientificname':record.scientificName, 'date':when, 'recordid':record.id, 'onlineRecordId':onlineRecordId, 'surveyId':record.surveyid});
	}
/*	for(var record in map.records){		
		// format date from long to 'dd M yy'
		var when = jQuery.datepicker.formatDate('dd M yy', new Date(parseFloat(map.records[record].when)));
		var onlineRecordId = "";
		if(map.records[record].online_recordid){
			onlineRecordId = map.records[record].online_recordid;
		}
		view += tmpl("recordsListItemTmpl",{'commonname':map.records[record].commonName, 'scientificname':map.records[record].scientificName,'date':when, 'recordid':map.records[record].id, 'onlineRecordId':onlineRecordId, 'surveyId':map.records[record].surveyid});
	}*/
	view = tmpl("recordsListContainerTmpl",{'content':view});
	view = listMenu + view;
	setMainContent(view);
}
/*function renderRecordsList(JSON_data){
	var view="";
	for(var record in JSON_data){		
		// format date from long to 'dd M yy'
		var when = jQuery.datepicker.formatDate('dd M yy', new Date(parseFloat(JSON_data[record].when)));
		var onlineRecordId = "";
		if(JSON_data[record].online_recordid){
			onlineRecordId = JSON_data[record].online_recordid;
		}
		view += tmpl("recordsListItemTmpl",{'commonname':JSON_data[record].commonName, 'scientificname':JSON_data[record].scientificName,'date':when, 'recordid':JSON_data[record].id, 'onlineRecordId':onlineRecordId, 'surveyId':JSON_data[record].surveyid});
	}
	view = tmpl("recordsListContainerTmpl",{'content':view});
	//add simple menu <menu_item_class,menu_item_text>
	var menuItems = {
			"select_all":"select all",
			"summary":"summary",
			"delete":"delete"
	};
	var menu_view = "";
	for(var item in menuItems){
		menu_view += tmpl("simpleMenuItemTmpl",{"menu_item_class":item,"menu_item_text":menuItems[item]});
		menu_view += " | ";
	}
	menu_view = menu_view.slice(0,-2);
	menu_view = tmpl("simpleMenuContainerTmpl",{"content":menu_view})
	view = menu_view + view + menu_view;
	setMainContent(view);
	//set background color for local records with remoterecordid
	jQuery('.remoterecordid').each(function(index){
		var recordId = jQuery(this).attr("id").slice(13);
		if(recordId != ""){
			jQuery('#record'+recordId).css("background-color","#8ba81c");
		}
	});
	//settings for summary dialog
	setDialog("records: "+JSON_data.length,{
		title: "Records summary",
		autoOpen: false,
		show: "drop",
		hide: "drop",
		modal: true
	});
	//adding event handlers to simple menu items
	jQuery('.select_all').click(function(){
		if(jQuery(".record_checkbox").attr("checked")){
			jQuery(".record_checkbox").attr("checked",false);
		}else{
			jQuery(".record_checkbox").attr("checked",true);
		}
	});
	jQuery('.summary').click(function(){
		jQuery( "#dialog" ).dialog( "open" );
	});
	jQuery('.delete').click(function(){
		deleteRecords();
	});
}*/

/**
 * 
 * @param JSON_data
 * @return
 */
/*function renderLocationsForRecordForm(JSON_data){*/
function appendLocationsTo(JSON_data, appendId){
	var selectoptions ="";
	//add gps option when gps is supported
	if(navigator.geolocation){
		selectoptions += tmpl("inputSelectOptionTmpl",{'value':"gps location", 'id':"-1"});
	}
	//build up location list from templates
	//for(loc in JSON_data){
	for(var i=0; i<JSON_data.length; i++){
		selectoptions += tmpl("inputSelectOptionTmpl",{'value':JSON_data[i].name, 'id':JSON_data[i].id+","+JSON_data[i].latitude+","+JSON_data[i].longitude});
	}
	//inject locations in record form
	jQuery('#' + appendId).append(selectoptions)
	jQuery('#' + appendId).change(setLocation)
}

/**
 * Renders taxa names as a list and accompanies it with corresponding icons.
 * @param JSON_data
 */
function renderTaxaList(JSON_data){
	var view = "";
	for(var i = 0; i<JSON_data.length; i++){
			view += tmpl('listItemTmpl',{'id': JSON_data[i].id, 'listItemIconUrl':contextPath + "/" +JSON_data[i].listItemIconUrl, 'linkUrl': JSON_data[i].linkUrl, 'alt': JSON_data[i].alt, 'listItemText': JSON_data[i].listItemText, 'listItemSubText': JSON_data[i].listItemSubText });
			//view += tmpl('listItemTmpl', JSON_data[i]);
	}
	view = tmpl('listTmpl',{'listItems':view});
	setMainContent(view);
}

/**
 * Renders indicatorspecies names as a list and accompanies it with corresponding icons.
 * @param JSON_data
 */
function renderSpeciesList(map){
	var listMenuItems = "";
	listMenuItems += tmpl('listMenuItemTmpl',{'url':"#/helpid/?tid="+map.tid+"&sid="+map.sid, 'iconUrl':'/BDRS/images/bdrs/mobile/40x40_HelpID_Grey_MagGlass.png', 'alt':'help id', 'listMenuItemName':'help id', 'func':'' });
	var listMenu = tmpl('listMenuTmpl',{'listMenuItems':listMenuItems});

	var view = "";
	var prevId = 0;
	for(var i = 0; i<map.species.length; i++){
		if(map.species[i].id != prevId){
			view += tmpl('listItemTmpl', map.species[i]);
		}
		prevId = map.species[i].id;
	}
	view = listMenu + tmpl('listTmpl',{'listItems':view});
	setMainContent(view);
}

/**
 * Renders indicatorspecies information.
 * @param JSON_data
 */
function renderSpeciesInfo(JSON_data){
	var view = "";
	var map = "";
	var status = "";
	var commonName;
	var scientificName;
	var listItems = "";
	var thumbNails = "";
	var mainImageUrl = ""
	for(var i = 0; i<JSON_data.length; i++){
		switch(JSON_data[i].type){
			case 'profile_img_med':
				/*thumbNails += tmpl('thumbImageTmpl', {'id':JSON_data[i].indicatorspeciesprofileid,'src':contextPath + "/" + JSON_data[i].content,'alt':JSON_data[i].header});
				mainImageUrl = JSON_data[i].content;
				scientificName =  JSON_data[i].header;*/
				
				thumbNails += tmpl('listItemImageTmpl', {'id':JSON_data[i].content,'src':contextPath + "/" + JSON_data[i].content,'alt':JSON_data[i].header});
				break;
			/*case 'map':
				map = tmpl('mapTmpl', JSON_data[i]);
				break;*/
			case 'text':
				listItems += tmpl('listInfoItemTmpl', {'id':JSON_data[i].indicatorspeciesprofileid, 'description':JSON_data[i].description, 'content':JSON_data[i].content});
				break;
			/*case 'status':
				statusItem += tmpl('statusItem', JSON_data[i]);
				break;
			case 'common_name':
				commonName =  JSON_data[i].content;
				break;
			case 'silhouette':
				break;*/
			default: logMessage("Something went wrong while rendering speciesInfo");
		}
	}
	var listItemImages = tmpl('listItemImagesTmpl',{'id':'images', 'listItemText':'Images', 'listItemSubText':'click thumbnail for larger image', 'listItemImages':thumbNails});
	listItems = listItemImages + listItems;
	view = tmpl('listTmpl',{'listItems':listItems});
	setMainContent(view);
	jQuery('.lightbox-enabled:has(img)').lightbox();
}

/**
 * Renders taxaFilter information.
 * @param JSON_data
 */
function renderTaxaFilter(map){
	var idToolSections = "";
	var view = "";
	prevAttId = 0;
	for(var i = 0; i<map.taxaTags.length; i++){
		
		if ((i != 0) && (map.taxaTags[i].attid != prevAttId)) {
			idToolSections += tmpl('idToolSectionTmpl',{'header':map.taxaTags[i].description, 'content':view});
			view = "";
		}
		switch(map.taxaTags[i].typecode){
			case "IM":
				view += tmpl('idToolItemIMTmpl', {'name':map.taxaTags[i].attid, 'value':map.taxaTags[i].stringvalue});
				break;
			case "ST":
				view += tmpl('idToolItemSTTmpl', {'name':map.taxaTags[i].attid, 'value':map.taxaTags[i].stringvalue});
				break;
			default:
		}
		prevAttId = map.taxaTags[i].attid;
	}
	idToolSections += tmpl('idToolSectionTmpl',{'header':map.taxaTags[map.taxaTags.length-1].description, 'content':view});
	var baseUrl = jQuery.address.baseURL();
	var sid = jQuery.address.parameter('sid');
	var tid = jQuery.address.parameter('tid');
	view = tmpl('idToolTmpl',{
		'content':idToolSections,
		'url': baseUrl + '#/fieldguide/?action=filter&sid=' + sid + '&tid=' + tid
		});
	setMainContent(view);
	
/*	jQuery('#featuresSubmit').unbind().bind('click', function(event) {
		var values = jQuery("#featForm").serializeArray();
		var valuesMap = {};
		for(v in values){
			valuesMap[values[v].name] = values[v].value; 
		}
		
	});*/
	
}

/**
 * Clears the onlineContainer div before injecting the new content
 * @param content Html content
 */
function setMainContent(content){
	  jQuery('#onlineContainer').empty().append(content);
}
