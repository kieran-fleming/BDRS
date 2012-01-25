
bdrs.contribute.tracker = {};

bdrs.contribute.tracker.RECORD_TAB_SELECTOR = "#record_tab_handle";
bdrs.contribute.tracker.SUB_RECORD_TAB_SELECTOR = "#sub_record_tab_handle";

bdrs.contribute.tracker.TAB_RECORD = "record";
bdrs.contribute.tracker.TAB_SUB_RECORD = "subRecord";

bdrs.contribute.tracker.SUBMIT_AND_SWITCH_TO_SUB_REC_SELECTOR = "#submitAndSwitchToSubRecordTab";

bdrs.contribute.tracker.TRACKER_URL = "/bdrs/user/tracker.htm";

bdrs.contribute.tracker.RECORD_DATA_PREFIX = "record_";

/**
 * initialises the tracker form.
 * @param {Object} selectedTab - indicates of the currently selected tab
 * @param {Object} surveyId - survey ID
 * @param {Object} censusMethodId - census method ID
 * @param {Object} recordId - record ID if we are viewing an existing record
 */
bdrs.contribute.tracker.init = function(selectedTab, surveyId, censusMethodId, recordId) {
	
	// prepare to detect form changes:
	// If it is a null record formChanged is always true as we want to
	// do validation.
	if (recordId === null || recordId === undefined || recordId === "") {
		bdrs.contribute.tracker.formChanged = true;
	} else {
        bdrs.contribute.tracker.formChanged = false;	
	}
	
	jQuery("form input, form select, form textarea").change(function() {
		bdrs.contribute.tracker.formChanged = true;
	});
	
	if (selectedTab === bdrs.contribute.tracker.TAB_RECORD) {
	   jQuery(bdrs.contribute.tracker.SUB_RECORD_TAB_SELECTOR).click(function() {
	       if (bdrs.contribute.tracker.formChanged === true) {
                if (confirm("You have unsaved changes. Would you like to save and switch tabs?")) {
                    jQuery(bdrs.contribute.tracker.SUBMIT_AND_SWITCH_TO_SUB_REC_SELECTOR).click();
                }
		   } else {
		      window.location = bdrs.contribute.tracker.getTrackerUrl(bdrs.contribute.tracker.TAB_SUB_RECORD, surveyId, censusMethodId, recordId);
		   }
	   });
	} else if (selectedTab === bdrs.contribute.tracker.TAB_SUB_RECORD) {
	   jQuery(bdrs.contribute.tracker.RECORD_TAB_SELECTOR).click(function() {
            window.location = bdrs.contribute.tracker.getTrackerUrl(bdrs.contribute.tracker.TAB_RECORD, surveyId, censusMethodId, recordId);
	   });	
	}
};

/**
 * Create the tracker url that we can navigate to with all the essential query parameters.
 * 
 * @param {Object} selectedTab - the tab we are selecting
 * @param {Object} surveyId - survey we are creating a record for (if we are creating a record)
 * @param {Object} censusMethodId - census method we are creating a record for (if we are creating a record)
 * @param {Object} recordId - record we are viewing (if we are viewing an existing record)
 */
bdrs.contribute.tracker.getTrackerUrl = function(selectedTab, surveyId, censusMethodId, recordId) {
	var params = [];
	if (selectedTab) {
	   params.push("selectedTab=" + selectedTab);	
	}
    if (surveyId) {
	   params.push("surveyId=" + surveyId);	
	}
	if (censusMethodId) {
	   params.push("censusMethodId=" + censusMethodId);	
	}
	if (recordId) {
	   params.push("recordId=" + recordId);	
	}
    return bdrs.contextPath + bdrs.contribute.tracker.TRACKER_URL + "?" + params.join("&");
};

/**
 * Function implementing jqgrid formatter interface for record
 * 'when' field.
 * 
 * @param {Object} cellvalue
 * @param {Object} options
 * @param {Object} rowObject - the record object
 */
bdrs.contribute.tracker.whenFormatter = function(cellvalue, options, record) {
    if (record.when === null || record.when === undefined) {
        return "N/A";
    }
    var date = new Date(record.when);
    return bdrs.util.formatDate(date);
};
    
/**
 * Function implementing jqgrid formatter interface for record
 * 'species' field.
 * 
 * @param {Object} cellvalue
 * @param {Object} options
 * @param {Object} record - the record object
 */
bdrs.contribute.tracker.speciesFormatter = function(cellvalue, options, record) {
    if (record.species === null || record.species === undefined) {
        return "No species recorded";
    }
	var spans = [];
	if (record.species.scientificName) {
		spans.push('<span class="scientificName">'+record.species.scientificName.replace(' ','&nbsp;')+'</span>');
	}
	if (record.species.commonName) {
		spans.push('<span>'+record.species.commonName.replace(' ','&nbsp;')+'</span>');
	}
    return spans.join('&nbsp;:&nbsp;');
};

/**
 * Create a jqGrid that shows the child records with a given census method.
 * Puts the jqGrid in the targetDiv
 * 
 * @param {Object} targetDivSelector - selector for the div we want to place the jqGrid 
 * @param {Object} parentRecordId - int
 * @param {Object} censusMethodId - int
 */
bdrs.contribute.tracker.createRecordGrid = function(targetDivSelector, parentRecordId, censusMethodId) {
	
	var targetDiv = jQuery(targetDivSelector);
	
	// should be enough to give us a unique table / pager id
    var idSuffix = "_" + parentRecordId + "_" + censusMethodId;
    
    var tableId = "table" + idSuffix;
	var tableSelector = "#"+tableId;
    
    var pagerId = "pager" + idSuffix;
    var pagerSelector = "#"+pagerId;
	
	var actionLinkFormatter = function(cellvalue, options, rowObject) {
		
		var rowId = rowObject.id; 
		
        var links = [];
		// Open record tab for selected record in read only mode
		links.push('<a title="Open record for viewing" href="'
          + bdrs.contextPath 
          + '/bdrs/user/surveyRenderRedirect.htm?selectedTab='
          + bdrs.contribute.tracker.TAB_RECORD+'&recordId=' 
          + rowId + '">View</a>');
		// Open related reecords tab for selected record in read only mode  
        links.push('<a title="Browse to this record while staying in the related records tab" href="'
		  + bdrs.contextPath 
		  + '/bdrs/user/surveyRenderRedirect.htm?selectedTab='
		  + bdrs.contribute.tracker.TAB_SUB_RECORD+'&recordId=' 
		  + rowId + '">Browse</a>');
		// Open a dialog showing information on the selected record
		links.push('<a title="Open a popup showing information on this record" class="neverVisited" href="javascript:bdrs.contribute.tracker.displayRecordInfo(\''+tableSelector+'\','+rowId+')"'
		+'>Info</a>');
        
		return '<div class="textcenter">' + links.join(' | '); + '</div>';
    };
	
	var table = jQuery('<table id='+tableId+'></table>').appendTo(targetDiv);
	var pager = jQuery('<div id="'+pagerId+'"></div>').appendTo(targetDiv);

	// create the grid
    jQuery(table).jqGrid({
            url: bdrs.contextPath + "/webservice/record/getChildRecords.htm",
			postData: {
				parentRecordId: parentRecordId,
				censusMethodId : censusMethodId
			},
            datatype: "json",
            mtype: "GET",
            colNames:['Record ID','Date','Species','Action'],
            colModel:[
                {name:'id',width:'15%', index:'id'},
				{name:'when',width:'20%', index:'when', formatter:bdrs.contribute.tracker.whenFormatter},
				{name:'species.commonName', width:'45%', index:'species.commonName', formatter:bdrs.contribute.tracker.speciesFormatter},
                {name:'action', width:'20%', sortable:false, formatter:actionLinkFormatter}
            ],
            autowidth: true,
			forceFit: true,
            jsonReader : { repeatitems: false },
            rowNum:10,
            rowList:[10,20,30],
            pager: pagerSelector,
            sortname: 'id',
            viewrecords: true,
            sortorder: "asc",
			// we don't want scroll bars in the grid...
            width: '100%',
            height: '100%',
			// save the original item objects.
			// jqGrid ignores key value pairs that don't appear in the grid columns.
			afterInsertRow: function( rowId, rowData, rowElem ) {
				jQuery(tableSelector).data(bdrs.contribute.tracker.RECORD_DATA_PREFIX+rowId, rowElem);
			}
    });
	
	// remove the sortable class from the non sortable columns.
	var grid = jQuery(tableSelector);
	var cm = grid[0].p.colModel;
    jQuery.each(grid[0].grid.headers, function(index, value) {
        var cmi = cm[index], colName = cmi.name;
        if(!cmi.sortable && colName !== 'rn' && colName !== 'cb' && colName !== 'subgrid') {
            jQuery('div.ui-jqgrid-sortable',value.el).css({cursor:"default"});
        }
    });

	// we don't want scroll bars in the grid...
	targetDiv.children().filter('.ui-jqgrid-bdiv').css('overflow-x', 'hidden');
	
	// create the pager...
    pager.jqGrid('navGrid', pagerSelector, {edit:false,add:false,del:false});
};

/**
 * Open a dialog and display record info
 * 
 * @param {Object} tableSelector - table selector for the grid we are operating on
 * @param {Object} rowId - the row id to display
 */
bdrs.contribute.tracker.displayRecordInfo = function(tableSelector, rowId) {
	var record = jQuery(tableSelector).data(bdrs.contribute.tracker.RECORD_DATA_PREFIX+rowId);
	bdrs.review.record.displayRecordInfo(record);
};

