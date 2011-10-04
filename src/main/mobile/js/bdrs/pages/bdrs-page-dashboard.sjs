exports.DEFAULT_FIELD_GUIDE_VISIBILITY = 'false';
exports.FIELD_GUIDE_VISIBILITY_SETTINGS_KEY = "display-field-guide";

exports.DEFAULT_IDENTIFY_TOOL_VISIBILITY = 'false';
exports.IDENTIFY_TOOL_VISIBILITY_SETTINGS_KEY = "display-identify-tool";

exports.Create = function() {
    bdrs.mobile.pages.dashboard._initDashboardConfiguration();
    persistence.flush();
};

exports.Show = function() {
    if(!bdrs.mobile.pages.clock_warning.isShown()) {
        var transition = jQuery.mobile.defaultPageTransition === 'none' ? 'none' : 'slidedown';
        jQuery.mobile.changePage("#clock-warning", transition);
    }
            
    jQuery('.dashboard-status').empty();
    Settings.findBy('key', 'current-survey' , function(settings) {
        if (settings != null) {
            bdrs.template.render('dashboard-status', { name : settings.value }, '.dashboard-status');
        }
    });

};

exports.Hide = function() {

};

exports.refreshDashboard = function(fieldGuideSetting, identifyToolSetting) {
    
    var listItemArray = [];

    // ----------------------
    // Field Guide
    // ----------------------
    var displayFieldGuide = bdrs.mobile.parseBoolean(fieldGuideSetting.value());
    var fieldGuideListItem = jQuery("#dashboard-fieldguide").parents('li');
    if(displayFieldGuide) {
        
        var listItem;
        if(fieldGuideListItem.length === 0) {
            var tmplParams = {
                linkId: 'dashboard-fieldguide',
                link: '#guide',
                header: 'Fieldguide',
                description: 'View the fieldguide'
            }
            
            waitfor(listItem) {
               bdrs.template.renderOnlyCallback('listViewItem', tmplParams, resume);
            }
            
        } else {
            listItem = fieldGuideListItem.remove();
        }
        listItemArray.push(listItem);
    } else {
        fieldGuideListItem.remove();
    }

    // ----------------------
    // Identify
    // ----------------------
    var displayIdentifyTool = bdrs.mobile.parseBoolean(identifyToolSetting.value());
    var identifyListItem = jQuery("#dashboard-identify").parents('li');
    if(displayIdentifyTool) {
        
        var listItem;
        if(identifyListItem.length === 0) {
            var tmplParams = {
                linkId: 'dashboard-identify',
                link: '#id',
                header: 'Identify',
                description: 'Identify a species'
            }
            
            waitfor(listItem) {
               bdrs.template.renderOnlyCallback('listViewItem', tmplParams, resume);
            }
            
        } else {
            listItem = identifyListItem.remove();
        }
        listItemArray.push(listItem);

    } else {
        identifyListItem.remove();
    }
    
    // We will be prepending the elements so we need to do this is reverse order.
    listItemArray.reverse();
    
    var dashboardList = jQuery("#dashboard ul[data-role=listview]");
    for(var i=0; i<listItemArray.length; i++) {
        dashboardList.prepend(listItemArray[i]);
    }
    
    dashboardList.listview('refresh');
}

exports._initDashboardConfiguration = function() {
    var fieldGuideSetting = bdrs.mobile.pages.dashboard._initialiseSetting(
        bdrs.mobile.pages.dashboard.FIELD_GUIDE_VISIBILITY_SETTINGS_KEY,
        bdrs.mobile.pages.dashboard.DEFAULT_FIELD_GUIDE_VISIBILITY
    );

    var identifySetting = bdrs.mobile.pages.dashboard._initialiseSetting(
        bdrs.mobile.pages.dashboard.IDENTIFY_TOOL_VISIBILITY_SETTINGS_KEY,
        bdrs.mobile.pages.dashboard.DEFAULT_IDENTIFY_TOOL_VISIBILITY
    );


    bdrs.mobile.pages.dashboard.refreshDashboard(fieldGuideSetting, identifySetting);
};

exports._initialiseSetting = function(key, defaultValue) {
    
    var setting;
    waitfor(setting) {
        Settings.all().filter('key', '=', key).one(resume);
    }
    
    if(setting === null || setting === undefined) {
        setting = new Settings();
        setting.key(key);
        setting.value(defaultValue);
        persistence.add(setting);
    }

    return setting;
};

