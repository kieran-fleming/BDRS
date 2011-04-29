bdrs.portal = {};

/**
 * Adds new fields to the form for the creation of a new PortalEntryPoint.
 * @param portalEntryPointTableSelector jQuery selector for the table where the 
 * new form elements shall be appended.
 *
 * @param indexSelector jQuery selector for the hidden input element storing the
 * current index of added entry points.
 */
bdrs.portal.addPortalEntryPoint = function(portalEntryPointTableSelector, indexSelector) {
    
    var indexElem = jQuery(indexSelector);
    var index = parseInt(indexElem.val(), 10);
    indexElem.val(index + 1);
    
    jQuery.get(bdrs.contextPath+'/bdrs/root/portal/ajaxAddPortalEntryPoint.htm',
            {'index': index}, function(data) {

        var table = jQuery(portalEntryPointTableSelector); 
        var row = jQuery(data);
        table.find("tbody").append(row);
        jQuery('form').ketchup();
    });
};

/*
 * Tests the current regular expression patterns against the specified test URL
 * displaying the portal that would be selected if the a user navigated to that
 * URL.
 * 
 * @param testResultSelector jQuery selector to the element where test results
 * should be displayed.
 */
bdrs.portal.testPortalEntryPointPattern = function(testResultSelector) {
    var testResultElem = jQuery(testResultSelector);
    testResultElem.empty();
    
    var url = bdrs.contextPath+"/bdrs/root/portal/ajaxTestPortalEntryPointPattern.htm?"+jQuery("form").serialize();
    jQuery.getJSON(url, function(data) {
         console.log(data);
        // Matched Portal
        var matchedPortalHeaderCell = jQuery("<th></th>").text("Matched Portal:").css({textAlign:'right'});
        var matchedPortalName = data.matchedPortal === null ? "None" : data.matchedPortal.name;
        var matchedPortalDataCell = jQuery("<td></td>").text(matchedPortalName);
        var matchedPortalRow = jQuery("<tr></tr>").append(matchedPortalHeaderCell).append(matchedPortalDataCell);
        
        // Matched Entry Point
        var matchedEntryPointHeaderCell = jQuery("<th></th>").text("Entry Point Pattern:").css({textAlign:'right'});
        var matchedEntryPointPattern = data.matchedEntryPoint === null ? "None" : data.matchedEntryPoint.pattern;
        var matchedEntryPointDataCell = jQuery("<td></td>").text(matchedEntryPointPattern);
        var matchedEntryPointRow = jQuery("<tr></tr>").append(matchedEntryPointHeaderCell).append(matchedEntryPointDataCell);
        
        // Default Portal
        var defaultPortalHeaderCell = jQuery("<th></th>").text("Default Portal:").css({textAlign:'right'});
        var defaultPortalName = data.defaultPortal === null ? "None" : data.defaultPortal.name;
        var defaultPortalDataCell = jQuery("<td></td>").text(defaultPortalName);
        var defaultPortalRow = jQuery("<tr></tr>").append(defaultPortalHeaderCell).append(defaultPortalDataCell);
        
        // Summary
        var summary = jQuery("<div></div>");
        var targetPortal = data.matchedPortal === null ? data.defaultPortal : data.matchedPortal;
        var targetURL;
        if(data.matchedEntryPoint !== null && data.matchedEntryPoint.redirect.length > 0) {
            targetURL = data.matchedEntryPoint.redirect;
        } else {
            targetURL = data.testURL;
        }
        var testURL = jQuery("<a></a>").attr({href:targetURL}).text(data.testURL);
        summary.append("<span>By navigating to </span>");
        summary.append(testURL);
        summary.append("<span> you will be directed to the &#8220"+targetPortal.name+"&#8221 portal.</span>");
        
        var tbody = jQuery("<tbody></tbody>");
        tbody.append(matchedPortalRow).append(matchedEntryPointRow).append(defaultPortalRow);
        
        var table = jQuery("<table></table>");
        table.append(tbody);
        
        var testResultElem = jQuery(testResultSelector);
        testResultElem.append(summary).append(table);
    });
};