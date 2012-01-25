if(bdrs === undefined) {
    window.bdrs = {};
}

if(bdrs.review === undefined) {
    bdrs.review = {};
}

bdrs.review.mysightings = {};

bdrs.review.mysightings.xhr_pool = [];

// This is the same as Integer.MAX_INT in Java.
// Using the Java max int value for consistency.
bdrs.review.mysightings.INTEGER_MAX_INT = 2147483647; 

bdrs.review.mysightings.SURVEY_CRITERIA_SELECTOR = "#survey_id";
bdrs.review.mysightings.GROUP_CRITERIA_SELECTOR = "#taxon_group_id";
bdrs.review.mysightings.START_DATE_CRITERIA_SELECTOR = "#start_date";
bdrs.review.mysightings.END_DATE_CRITERIA_SELECTOR = "#end_date";

bdrs.review.mysightings.RELATIVE_PATH_FROM_CONTEXT = 'map/mySightings.htm';

bdrs.review.mysightings.SHOW_ALL_SELECTOR = "#show_all";
bdrs.review.mysightings.LIMIT_SELECTOR = "#limit";
bdrs.review.mysightings.LIMIT_WARNING_SELECTOR = "#limit_warning";

bdrs.review.mysightings.TAB_HANDLE_SELECTOR = ".tab_handle";
bdrs.review.mysightings.TAB_HANDLE_SUFFIX = "_handle";

bdrs.review.mysightings.SELECTED_TAB_SELECTOR = "#selected_tab";
bdrs.review.mysightings.MAP_TAB_SELECTOR = "#map_tab";
bdrs.review.mysightings.TABLE_TAB_SELECTOR = "#table_tab";
bdrs.review.mysightings.DOWNLOAD_TAB_SELECTOR = "#download_tab";

bdrs.review.mysightings.SELECTED_TAB_CLASS = "displayTabSelected";

bdrs.review.mysightings.SEARCH_BUTTON_SELECTOR = "#search_criteria";
bdrs.review.mysightings.SEARCH_CRITERIA_FORM_SELECTOR = "#search_criteria_form";
bdrs.review.mysightings.SEARCH_CRITERIA_CONTAINER_SELECTOR = "#search_criteria_container";
bdrs.review.mysightings.SEARCH_CRITERIA_EXPAND_COLLAPSE_SELECTOR = "#search_criteria_expand_collapse";

bdrs.review.mysightings.PERMALINK_LINK_SELECTOR = "#permalink_link";
bdrs.review.mysightings.PERMALINK_INPUT_SELECTOR = "#permalink_input";

bdrs.review.mysightings.LOADING_INDICATOR = "#loading";

bdrs.review.mysightings.HIGHLIGHTED_RECORD_ID_SELECTOR = "#highlighted_record_id";

bdrs.review.mysightings.RECORD_TABLE_SELECTOR = "#record_table";
bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_SELECTOR = "#page_number"; 
bdrs.review.mysightings.TABLE_TAB_PAGE_COUNT_SELECTOR = "#page_count";

bdrs.review.mysightings.RECORD_COUNT_PANEL_SELECTOR = ".recordCountPanel";


bdrs.review.mysightings.DISPLAY_CHANGE_EVENT_TYPE = "display_changed";

bdrs.review.mysightings.CRITERIA_EXPAND_LABEL = "Expand";
bdrs.review.mysightings.CRITERIA_COLLAPSE_LABEL = "Collapse";

bdrs.review.mysightings.TABLE_ROW_TMPL = '\
<tr id="record_row_${ id }">\
    <td class="textcenter"><a href="${ contextPath }/bdrs/user/surveyRenderRedirect.htm?surveyId=${ survey }&recordId=${ id }">${ _when }</a></td>\
    <td>${ species.commonName }</td>\
    <td class=\"scientificName\">${ species.scientificName }</td>\
    <td class=\"nowrap\">${ latitude }</td>\
    <td class=\"nowrap\">${ longitude }</td>\
    <td>${ number }</td>\
    <td>${ notes }</td>\
    <td class="textcenter"><a href="${ contextPath }/bdrs/user/surveyRenderRedirect.htm?surveyId=${ survey }&recordId=${ id }">Edit</a></td>\
    <td class="textcenter"><a href="javascript:bdrs.review.mysightings.display_record_info(\'#record_row_${ id }\')">Info</a></td>\
</tr>';

bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_NEXT_TMPL = '\
<span id="next_page_${ page_number }" class="pagenumber">\
    Next&nbsp;&#187;\
</span>';

bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_NEXT_LINK_TMPL = '\
<span id="next_page_${ page_number }" class="pagenumber">\
    <a href="javascript:bdrs.review.mysightings.page_selected(${ page_number });">Next&nbsp;&#187;</a>\
</span>';

bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_PREVIOUS_TMPL = '\
<span id="previous_page_${ page_number }" class="pagenumber">\
    &#171;&nbsp;Previous\
</span>';

bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_PREVIOUS_LINK_TMPL = '\
<span id="previous_page_${ page_number }" class="pagenumber">\
    <a href="javascript:bdrs.review.mysightings.page_selected(${ page_number });">&#171;&nbsp;Previous</a>\
</span>';

bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_TMPL = '\
<span id="page_number_${ page_number }" class="pagenumber">\
    ${ page_number }\
</span>';

bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_LINK_TMPL = '\
<span id="page_number_${ page_number }" class="pagenumber">\
    <a href="javascript:bdrs.review.mysightings.page_selected(${ page_number });">${ page_number }</a>\
</span>';

bdrs.review.mysightings.NON_TAXONOMIC_SPECIES_PLACEHOLDER = {
    scientificName: 'N/A',
    commonName: 'N/A'
};

/**
 * Event handler invoked when the user checkes the show all checkbox thereby
 * disabling the limit.
 */
bdrs.review.mysightings.show_all_change_handler = function (event) {
    var limit_elem = jQuery(bdrs.review.mysightings.LIMIT_SELECTOR);
    if(isNaN(parseInt(limit_elem.val(), 10))) {
        // If the limit is garbage, then sanitise it so that it can be sent to 
        // the server.
        limit_elem.val(0);
    }    
    
    var limit_warning = jQuery(bdrs.review.mysightings.LIMIT_WARNING_SELECTOR);
    var show_all = jQuery(bdrs.review.mysightings.SHOW_ALL_SELECTOR);
    
    if(show_all.prop("checked")) {
        limit_warning.show();
        limit_elem.prop("disabled", true);
    } else {
        limit_warning.hide();
        limit_elem.prop("disabled", false);
    }
};

/**
 * Shows or hides the ajax data loading icon.
 * @param show [boolean] true if the loading icon should be displayed, 
 * false otherwise.
 */
bdrs.review.mysightings.show_loading_icon = function(show) {
    var indicator = jQuery(bdrs.review.mysightings.LOADING_INDICATOR);
    if(show) {
        indicator.fadeIn('fast');
    } else {
        indicator.fadeOut('fast');
    }
};

/**
 * Event handler that is invoked when the user selects a tab.
 */
bdrs.review.mysightings.tab_changed_handler = function(event) {
    bdrs.review.mysightings.abort_all_xhr();
    var tab_handles = jQuery(bdrs.review.mysightings.TAB_HANDLE_SELECTOR);
    var currently_selected_tab_handle = tab_handles.find("." + bdrs.review.mysightings.SELECTED_TAB_CLASS).parent();
    
    var new_selected_tab_handle = jQuery(event.currentTarget);
    
    // Invoked when the first tab is deselected, this will show the target tab.
    var on_tab_deselected = function() {
        bdrs.review.mysightings.update_tab_selection(new_selected_tab_handle, true, function() {
            bdrs.review.mysightings.show_loading_icon(false);
            bdrs.review.mysightings.update_permalink();
        });
    };
    
    // Show the loading icon.
    bdrs.review.mysightings.show_loading_icon(true);
    // Deselect the original tab, when that is complete, trigger the select on the target tab.
    bdrs.review.mysightings.update_tab_selection(currently_selected_tab_handle, false, on_tab_deselected);
};

/**
 * Updates the specified tab which may have been selected or deselected.
 * @param tab_handle [jquery element] the tab (handle) to be updated.
 * @param is_selected true if this tab was selected, false otherwise.
 * @param on_complete_callback to be invoked when the tab has been updated.
 */
bdrs.review.mysightings.update_tab_selection = function(tab_handle, is_selected, on_complete_callback) {
    var tab = bdrs.review.mysightings.get_tab_from_handle(tab_handle);
    if(is_selected) {
        tab_handle.children().addClass(bdrs.review.mysightings.SELECTED_TAB_CLASS); 
        tab.show();
    } else {
        tab_handle.children().removeClass(bdrs.review.mysightings.SELECTED_TAB_CLASS);
        tab.hide();
    }
    
    tab.trigger(bdrs.review.mysightings.DISPLAY_CHANGE_EVENT_TYPE, [is_selected, on_complete_callback]);
};

/**
 * Returns the tab content for the provided tab handle. The tab handle is the
 * part of the tab containing the name of the tab. The tab content is the portion
 * of the tab that contains the data to be displayed by the tab.
 *
 * @param tab_handle the handle to the tab to be returned.
 */
bdrs.review.mysightings.get_tab_from_handle = function(tab_handle) {
    // The tab has the same id as the tab_handle sans the "_handle";
    var handle_id = tab_handle.attr("id");
    var tab_id = handle_id.substr(0, handle_id.indexOf(bdrs.review.mysightings.TAB_HANDLE_SUFFIX));
    return jQuery("#"+tab_id);
};

/**
 * Returns the URL used to retrieve the KML representation of the records based on the selection criteria.
 */
bdrs.review.mysightings.get_kml_url = function() {
    return [
        bdrs.contextPath,
        '/map/ajaxMySightingsKML.htm?',
        jQuery(bdrs.review.mysightings.SEARCH_CRITERIA_FORM_SELECTOR).serialize()
    ].join('');
};

/**
 * Updates the mapping tab.
 * @param event [event data] Not used
 * @param is_selected true if this tab was selected, false otherwise.
 * @param on_complete_callback to be invoked when the tab has been updated.
 */
bdrs.review.mysightings.map_tab_display_change_handler = function(event, is_selected, on_complete_callback) {
    if(bdrs.map.baseMap === null && is_selected) {
	    // Initialise the maps
	    bdrs.map.initBaseMap('record_map', { geocode: { selector: '#geocode' }});
	    bdrs.map.centerMap(bdrs.map.baseMap, null, 3);
	    bdrs.map.baseMap.events.register('addlayer', null, bdrs.map.addFeaturePopUpHandler);
	    bdrs.map.baseMap.events.register('removeLayer', null, bdrs.map.removeFeaturePopUpHandler);
    }
    
    bdrs.map.clearPopups(bdrs.map.baseMap);
    bdrs.map.clearAllVectorLayers(bdrs.map.baseMap);
    bdrs.review.mysightings.clearRecordCount();
    
    if(is_selected) {
        jQuery(bdrs.review.mysightings.SELECTED_TAB_SELECTOR).val("map");
    
	    var kmlURL = bdrs.review.mysightings.get_kml_url(); 
	    var selectedId = jQuery(bdrs.review.mysightings.HIGHLIGHTED_RECORD_ID_SELECTOR).val();
	    var style = bdrs.map.createOpenlayersStyleMap(selectedId.toString());
	    
	    var layerOptions = {
	        visible: true,
	        includeClusterStrategy: true,
	        styleMap: style
	    };
	
	    var layer = bdrs.map.addKmlLayer(bdrs.map.baseMap, "Sightings", kmlURL, layerOptions, selectedId);
	    layer.events.register('loadend', layer, function(event) {
	        bdrs.map.centerMapToLayerExtent(bdrs.map.baseMap, layer);
	        if(on_complete_callback !== null && on_complete_callback !== undefined) {
	            // Update the Record Count
	            var count = 0;
	            var features = layer.features;
	            var feature;
	            for(var i=0; i<features.length; i++) {
                    feature = features[i];
                    if(feature.cluster !== undefined) {
                        count += feature.cluster.length;
                    } else {
                        count += 1;
                    }
                }
                bdrs.review.mysightings.update_record_count(count);
                
	            on_complete_callback();
	        }
	    });
    } else {
        if(on_complete_callback !== null && on_complete_callback !== undefined) {
           on_complete_callback();
        }
    }
};

/**
 * Updates the table tab.
 * @param event [event data] Not used
 * @param is_selected true if this tab was selected, false otherwise.
 * @param on_complete_callback to be invoked when the tab has been updated.
 */
bdrs.review.mysightings.table_tab_display_change_handler = function(event, is_selected, on_complete_callback) {

    // No matter what, begin by clearing the table.
    jQuery(bdrs.review.mysightings.RECORD_TABLE_SELECTOR).find("tbody").remove();
    // And the record count
    bdrs.review.mysightings.clearRecordCount();
    // And the page count
    jQuery(bdrs.review.mysightings.TABLE_TAB_PAGE_COUNT_SELECTOR).empty();

    if(is_selected) {
        jQuery(bdrs.review.mysightings.SELECTED_TAB_SELECTOR).val("table");
    
        // Retrieve the table content
        var json_url = [
            bdrs.contextPath,
            '/map/ajaxMySightingsJSON.htm?',
            jQuery(bdrs.review.mysightings.SEARCH_CRITERIA_FORM_SELECTOR).serialize()
        ].join('');
	    jQuery.getJSON(json_url, function(data) {
	        var rec;
	        var row;
	        var body = jQuery("<tbody></tbody>");
	        var compiled_tmpl = jQuery.template(bdrs.review.mysightings.TABLE_ROW_TMPL);
	        for(var i=0; i<data.length; i++) {
	            rec = data[i];
	            
	            // Preprocessing and Formatting
	            rec.contextPath = bdrs.contextPath;
	            rec._when = bdrs.util.formatDate(new Date(rec.when)).replace(/ /gi, "&nbsp;");
	            if(rec.species === null || rec.species === undefined) {
	                rec.species = bdrs.review.mysightings.NON_TAXONOMIC_SPECIES_PLACEHOLDER;
	            }
	            
	            // Template Processing
	            row = jQuery.tmpl(compiled_tmpl, rec);
	            row.data('record', rec);
	            
	            // Buffering
	            body.append(row);
	        }
	        
	        // Attach the rows to the DOM in a single update.
	        jQuery(bdrs.review.mysightings.RECORD_TABLE_SELECTOR).append(body);
	        if(on_complete_callback !== null && on_complete_callback !== undefined) {
                on_complete_callback();
            }
            
            // Update the Record Count
            bdrs.review.mysightings.update_record_count(data.length);
	    });
	    
	    // Generate the page numbers
	    bdrs.review.mysightings.generate_page_numbers();
	    
    } else {
        if(on_complete_callback !== null && on_complete_callback !== undefined) {
            on_complete_callback();
        }
    }
};

/**
 * Generates the list of page numbers beneath the table on the table view.
 */
bdrs.review.mysightings.generate_page_numbers = function() {
    var rec_count_url = [
        bdrs.contextPath,
        '/map/ajaxMySightingsRecordCount.htm?',
        jQuery(bdrs.review.mysightings.SEARCH_CRITERIA_FORM_SELECTOR).serialize()
    ].join('');
    jQuery.getJSON(rec_count_url, function(data) {
        
        var record_count = data.record_count;
        var records_per_page = data.records_per_page;
        var max_pages = data.max_pages;
        
        var limit = parseInt(jQuery(bdrs.review.mysightings.LIMIT_SELECTOR).val(), 10);
        var page_num_elem = jQuery(bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_SELECTOR);
        var page_num = parseInt(page_num_elem.val(), 10);
        
        records_per_page = Math.min(limit, records_per_page);
        
        // Calculate the largest possible page and make sure you do not exceed that.
        page_num = Math.min(page_num, Math.ceil(record_count / records_per_page));  
        page_num_elem.val(page_num);
        
        var total_pages = Math.ceil(record_count / records_per_page);
        
        var start_page = Math.max(1, (page_num - Math.round(max_pages/2)));
        var end_page = Math.min((start_page + max_pages), total_pages);
        
        // This is to simply avoid making lots of little DOM changes.
        var page_container = jQuery("<span/>");
        var tmpl_params = {
           'page_number': -1
        };
        
        // Previous Page Link
        if(start_page === page_num) {
           tmpl_params.page_number = page_num;
           page_container.append(jQuery.tmpl(bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_PREVIOUS_TMPL, tmpl_params));
        } else {
           tmpl_params.page_number = page_num - 1;
           page_container.append(jQuery.tmpl(bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_PREVIOUS_LINK_TMPL, tmpl_params));
        }
        
        // Page Numbers
        var compiled_page_num_link = jQuery.template(bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_LINK_TMPL);
        for(var i=start_page; i <= end_page; i++) {
           tmpl_params.page_number = i;
           if(i === page_num) {
               page_container.append(jQuery.tmpl(bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_TMPL, tmpl_params));
           } else {
               page_container.append(jQuery.tmpl(compiled_page_num_link, tmpl_params)); 
           }
        }
        
        // Next Page Link           
        if(end_page === page_num) {
           tmpl_params.page_number = page_num;
           page_container.append(jQuery.tmpl(bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_NEXT_TMPL, tmpl_params));
        } else {
           tmpl_params.page_number = page_num + 1;
           page_container.append(jQuery.tmpl(bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_NEXT_LINK_TMPL, tmpl_params));
        }
        
        jQuery(bdrs.review.mysightings.TABLE_TAB_PAGE_COUNT_SELECTOR).append(page_container);
    });
};

/**
 * Invoked when the user clicks on a page number. This function updates the
 * page number input and refreshes the table.
 * @param page_number [int] the new page that was selected by the user.
 */
bdrs.review.mysightings.page_selected = function(page_number) {
    jQuery(bdrs.review.mysightings.TABLE_TAB_PAGE_NUMBER_SELECTOR).val(page_number);
    bdrs.review.mysightings.update_selected_tab();
};

/**
 * Displays a popup showing additional details (such as attributes) about a record.
 * @param data_elem_selector the selector for the element containing the record
 * element (stored using jquery.data)
 */
bdrs.review.mysightings.display_record_info = function(data_elem_selector) {
    var record = jQuery(data_elem_selector).data('record');
	bdrs.review.record.displayRecordInfo(record);
};

/**
 * Updates the downloads tab.
 * @param event [event data] Not used
 * @param is_selected true if this tab was selected, false otherwise.
 * @param on_complete_callback to be invoked when the tab has been updated.
 */
bdrs.review.mysightings.download_tab_display_change_handler = function(event, is_selected, on_complete_callback) {
    
    if(is_selected) {
        jQuery(bdrs.review.mysightings.SELECTED_TAB_SELECTOR).val("download");
    }
        
    if(on_complete_callback !== null && on_complete_callback !== undefined) {
        on_complete_callback();
    }
};

/**
 * Updates the currently selected tab.
 */
bdrs.review.mysightings.update_selected_tab = function() {
    bdrs.review.mysightings.abort_all_xhr();
    bdrs.review.mysightings.update_permalink();
    
    var selected_tab = jQuery("."+bdrs.review.mysightings.SELECTED_TAB_CLASS).parent();
    bdrs.review.mysightings.show_loading_icon(true);
    bdrs.review.mysightings.update_tab_selection(selected_tab, true, function() {
        bdrs.review.mysightings.show_loading_icon(false);
    });
};

/**
 * Clear the record count and insert placeholder text until the new count is available.
 */
bdrs.review.mysightings.clearRecordCount = function() {
    var loading = jQuery("<span/>");
    loading.text("Loading ...");
    jQuery(bdrs.review.mysightings.RECORD_COUNT_PANEL_SELECTOR).empty().append(loading);
};

/**
 * Creates and renders the new record count.
 * @param count [int] the new count of records to be displayed. 
 */
bdrs.review.mysightings.update_record_count = function(count) {
    var name = count === 1 ? "Record" : "Records";
    var panel = jQuery(bdrs.review.mysightings.RECORD_COUNT_PANEL_SELECTOR);
    panel.empty();
    var elem = jQuery("<span/>");
    elem.text(count + " " + name);
    panel.append(elem);
};

/**
 * Updates the contents of the permalink input.
 */
bdrs.review.mysightings.update_permalink = function() {
    var url = [
	    window.location.protocol,
	    "//",
	    window.location.host,
        bdrs.contextPath,
        '/portal/',
        bdrs.review.mysightings.PORTAL_ID,
        '/',
        bdrs.review.mysightings.RELATIVE_PATH_FROM_CONTEXT,
        '?',
        jQuery(bdrs.review.mysightings.SEARCH_CRITERIA_FORM_SELECTOR).serialize()
    ].join('');
    jQuery(bdrs.review.mysightings.PERMALINK_INPUT_SELECTOR).val(url);
};

/**
 * Updates the available list of taxon groups for a given survey (primary key). 
 *
 * @param survey_id [int] the primary key of the currently selected survey or
 * 0 if all groups should be retrieved. 
 */
bdrs.review.mysightings.updateTaxonGroups = function(survey_id) {
    var url;
    var params = {};
    if(survey_id === 0) {
        url = "/webservice/taxon/getAllTaxonGroups.htm";
    } else {
        url = "/webservice/survey/taxaForSurvey.htm";
        params.surveyId = survey_id;
    }
    
    url = bdrs.contextPath + url;

    // Update the Taxon Groups
    jQuery.getJSON(url, params, function(data) {
        var group_elem = jQuery(bdrs.review.mysightings.GROUP_CRITERIA_SELECTOR);
        var group_pk = parseInt(group_elem.val(), 10);
        var all_groups = group_elem.find("[value=0]");
        group_elem.empty();
        
        // Replace the all groups option
        group_elem.append(all_groups);
        
        var group;
        var opt;
        for(var i=0; i<data.length; i++) {
            group = data[i];
            opt = jQuery("<option></option>");
            opt.attr("value", group.id);
            opt.text(group.name);
            if(group.id === group_pk) {
                opt.attr("selected", "selected");
            }
            
            group_elem.append(opt);
        }
        bdrs.review.mysightings.update_permalink();
    });
};

/**
 * Aborts all registered XHR requests.
 */
bdrs.review.mysightings.abort_all_xhr = function() {
    var copy = bdrs.review.mysightings.xhr_pool;
    bdrs.review.mysightings.xhr_pool = [];
    
    for(var i=0; i<copy.length; i++) {
        copy[i].abort();
    }
};

/**
 * Initialises the mysightings view.
 * @portal_id the primary key of the current portal.
 */ 
bdrs.review.mysightings.init = function(portal_id) {
    bdrs.review.mysightings.PORTAL_ID = parseInt(portal_id, 10);
    
    // Initialise XHR Request Management
	jQuery.ajaxSetup({
	  beforeSend: function(jqXHR) {
	      bdrs.review.mysightings.xhr_pool.push(jqXHR);
	  },
	  complete: function(jqXHR) {
	      var index = jQuery.inArray(jqXHR, bdrs.review.mysightings.xhr_pool);
	      if(index > -1){
	          bdrs.review.mysightings.xhr_pool.splice(index, 1);
	      }
	  }
	});
    
    // Initialise the survey change listener
    jQuery(bdrs.review.mysightings.SURVEY_CRITERIA_SELECTOR).change(function(event) {
        var survey_id = jQuery(bdrs.review.mysightings.SURVEY_CRITERIA_SELECTOR).val();
        survey_id = parseInt(survey_id, 10);
        bdrs.review.mysightings.updateTaxonGroups(survey_id);
        
        if(survey_id === 0) {
            var startDateStr = bdrs.util.formatDate(new Date(0));
            var endDateStr = bdrs.util.formatDate(new Date());
            jQuery(bdrs.review.mysightings.START_DATE_CRITERIA_SELECTOR).val(startDateStr);
            jQuery(bdrs.review.mysightings.END_DATE_CRITERIA_SELECTOR).val(endDateStr);
            
            bdrs.review.mysightings.update_permalink();
        } else {        
	        var survey_url = bdrs.contextPath + '/webservice/survey/getSurvey.htm';
	        jQuery.getJSON(survey_url, {'surveyId' : survey_id, 'ident' : bdrs.ident }, function(data) {
	            var startDate = new Date(data.startDate);
	            var startDateStr = bdrs.util.formatDate(startDate);
	            
	            var endDate = data.endDate === null ? new Date() : new Date(data.endDate);
	            var endDateStr = bdrs.util.formatDate(endDate);
	            
	            jQuery(bdrs.review.mysightings.START_DATE_CRITERIA_SELECTOR).val(startDateStr);
	            jQuery(bdrs.review.mysightings.END_DATE_CRITERIA_SELECTOR).val(endDateStr);
	            
	            bdrs.review.mysightings.update_permalink();
	        });
        }
    });

    // Initialise the show all check box change handler.
    var limit = jQuery(bdrs.review.mysightings.LIMIT_SELECTOR);
    limit.change(function(event) {
        var elem = jQuery(event.currentTarget);
        var limit = parseInt(elem.val(), 10);
        var searchButton = jQuery(bdrs.review.mysightings.SEARCH_BUTTON_SELECTOR);
        searchButton.prop("disabled", isNaN(limit));
        if(!isNaN(limit)) {
            // Clean the value because '123abc' will parse to '123'.
            elem.val(limit);
        } 
    });
    var show_all = jQuery(bdrs.review.mysightings.SHOW_ALL_SELECTOR);
    show_all.change(bdrs.review.mysightings.show_all_change_handler);
    limit.prop("disabled", show_all.prop("checked"));
    
    // Initialise the criteria search button
    jQuery(bdrs.review.mysightings.SEARCH_BUTTON_SELECTOR).click(function(event) {
        if(!jQuery(event.currentTarget).prop("disabled")) {
            bdrs.review.mysightings.update_selected_tab();
        }
    });
    
    // Initialise additional validation on the start and end date
    var dates = jQuery(bdrs.review.mysightings.START_DATE_CRITERIA_SELECTOR).add(bdrs.review.mysightings.END_DATE_CRITERIA_SELECTOR);
    dates.blur(function(event) {
        var elem = jQuery(event.currentTarget);
        var date = bdrs.util.parseDate(elem.val());
        
        var search_button = jQuery(bdrs.review.mysightings.SEARCH_BUTTON_SELECTOR);
        search_button.prop("disabled", date === null);
        
        if(date !== null) {
            // Clean the value. This will conver something like 31 Feb 2011 to 03 Mar 2011
            // which is the same behaviour as javascript.
            elem.val(bdrs.util.formatDate(date));
        }
    });
    
    // Initialise the expand/collapse link
    var expand_collapse = jQuery(bdrs.review.mysightings.SEARCH_CRITERIA_EXPAND_COLLAPSE_SELECTOR);
    expand_collapse.click(function() {
        jQuery(bdrs.review.mysightings.SEARCH_CRITERIA_CONTAINER_SELECTOR).slideToggle("fast", function() {
            var is_hidden = jQuery(bdrs.review.mysightings.SEARCH_CRITERIA_CONTAINER_SELECTOR).is(":hidden");
            var expand_collapse = jQuery(bdrs.review.mysightings.SEARCH_CRITERIA_EXPAND_COLLAPSE_SELECTOR);
            if(is_hidden) {
                expand_collapse.text(bdrs.review.mysightings.CRITERIA_EXPAND_LABEL);
            } else {
                expand_collapse.text(bdrs.review.mysightings.CRITERIA_COLLAPSE_LABEL);
            }
        });
    });
    
    // Initialise the tab panes
    var tab_handles = jQuery(bdrs.review.mysightings.TAB_HANDLE_SELECTOR);
    tab_handles.click(bdrs.review.mysightings.tab_changed_handler);
    
    jQuery(bdrs.review.mysightings.MAP_TAB_SELECTOR).bind(bdrs.review.mysightings.DISPLAY_CHANGE_EVENT_TYPE, 
        bdrs.review.mysightings.map_tab_display_change_handler);
	jQuery(bdrs.review.mysightings.TABLE_TAB_SELECTOR).bind(bdrs.review.mysightings.DISPLAY_CHANGE_EVENT_TYPE, 
        bdrs.review.mysightings.table_tab_display_change_handler);
	jQuery(bdrs.review.mysightings.DOWNLOAD_TAB_SELECTOR).bind(bdrs.review.mysightings.DISPLAY_CHANGE_EVENT_TYPE, 
        bdrs.review.mysightings.download_tab_display_change_handler);
        
    // Disable file downloading if no format is selected, and update the permalink
	
	bdrs.review.downloadSightingsWidget.init(bdrs.review.mysightings.SEARCH_CRITERIA_FORM_SELECTOR, 
												'/map/ajaxMySightingsDownload.htm', 
												bdrs.review.mysightings.update_permalink);
        
    // Initialise the Permalink
    bdrs.review.mysightings.update_permalink();
    jQuery(bdrs.review.mysightings.PERMALINK_LINK_SELECTOR).click(function() {
        jQuery(bdrs.review.mysightings.PERMALINK_LINK_SELECTOR).hide();
        jQuery(bdrs.review.mysightings.PERMALINK_INPUT_SELECTOR).show().focus();
    });
    jQuery(bdrs.review.mysightings.PERMALINK_INPUT_SELECTOR).blur(function() {
        jQuery(bdrs.review.mysightings.PERMALINK_LINK_SELECTOR).show();
        jQuery(bdrs.review.mysightings.PERMALINK_INPUT_SELECTOR).hide();
    });
        
    // Initialise the selected tab (fake a display click)
    bdrs.review.mysightings.update_selected_tab();
};
