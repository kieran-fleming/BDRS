exports.FIELD_GUIDE_SELECTOR = "#fieldguide";
exports.IDENTIFY_TOOL_SELECTOR = "#identify";
exports.SAVE_BUTTON_SELECTOR = "#configure-dashboard-save-button";

exports.Init =  function() {

    var handler = function() {
        bdrs.mobile.pages.configure_dashboard._saveFeatureVisibility(
        bdrs.mobile.form.inputsToMap("#configure-dashboard"));
        jQuery.mobile.changePage("#settings", {showLoadMsg: false});
    };

    jQuery(bdrs.mobile.pages.configure_dashboard.SAVE_BUTTON_SELECTOR).click(handler);
};

exports.BeforeShow = function() {
};
    
exports.Show = function() {

    bdrs.mobile.pages.configure_dashboard._updateFeatureVisibility(
        bdrs.mobile.pages.dashboard.FIELD_GUIDE_VISIBILITY_SETTINGS_KEY,
        bdrs.mobile.pages.dashboard.DEFAULT_FIELD_GUIDE_VISIBILITY,
        bdrs.mobile.pages.configure_dashboard.FIELD_GUIDE_SELECTOR);

    bdrs.mobile.pages.configure_dashboard._updateFeatureVisibility(
        bdrs.mobile.pages.dashboard.IDENTIFY_TOOL_VISIBILITY_SETTINGS_KEY,
        bdrs.mobile.pages.dashboard.DEFAULT_IDENTIFY_TOOL_VISIBILITY,
        bdrs.mobile.pages.configure_dashboard.IDENTIFY_TOOL_SELECTOR);
   
};
    
exports.Hide = function() {
    
};

exports._updateFeatureVisibility = function(settingsKey, defaultValue, inputSelector) {
    
    var setting;
    waitfor(setting) {
        Settings.all().filter('key', '=', settingsKey).one(resume);
    }
    var value = setting === null ? defaultValue : setting.value();
    var elem = jQuery(inputSelector);

    elem.find("option[value="+value+"]").prop("selected","selected");
   
    elem.slider('refresh');
};

exports._saveFeatureVisibility = function(inputMap) {

    var fieldGuideSetting = bdrs.mobile.pages.configure_dashboard._updateFeatureVisibilitySettings(
        bdrs.mobile.pages.dashboard.FIELD_GUIDE_VISIBILITY_SETTINGS_KEY,
        inputMap.get('fieldguide'));
    var identifyToolSetting = bdrs.mobile.pages.configure_dashboard._updateFeatureVisibilitySettings(
        bdrs.mobile.pages.dashboard.IDENTIFY_TOOL_VISIBILITY_SETTINGS_KEY,
        inputMap.get('identify'));

    persistence.flush();

    // Update the dashboard
    bdrs.mobile.pages.dashboard.refreshDashboard(fieldGuideSetting, identifyToolSetting);
};

exports._updateFeatureVisibilitySettings = function(key, value) {
    var settings;
    waitfor(settings) {
        Settings.all().filter('key', '=', key).one(resume);
    }
    if(settings === null || settings === undefined) {
        settings = new Settings();
        settings.key(key);
        persistence.add(settings);
    }

    settings.value(value);
    return settings;
};