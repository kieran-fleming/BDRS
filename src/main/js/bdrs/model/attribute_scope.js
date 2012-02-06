if (window.bdrs === undefined) {
    window.bdrs = {};
}

if (bdrs.model === undefined) {
    bdrs.model = {};
}

if (bdrs.model.taxa === undefined) {
    bdrs.model.taxa = {};
}

if (bdrs.model.taxa.attributeScope === undefined) {
    bdrs.model.taxa.attributeScope = {};
}

if (bdrs.model.taxa.attributeScope.code === undefined) {
    bdrs.model.taxa.attributeScope.code = {};
}

if (bdrs.model.taxa.attributeScope.value === undefined) {
    bdrs.model.taxa.attributeScope.value = {};
}

/**
 * A Javascript representation of the AttributeScope object.
 * @param [string] value the name of the enumeration.
 * @param [string] name the name of this attribute scope.
 */
bdrs.model.taxa.attributeScope.AttributeScope = function(value, name) {
    this.value = value;
    this.name = name;

    // Registration with the various lookups.
    bdrs.model.taxa.attributeScope.value[this.value] = this;
    
    /**
     * @return [boolean] true if this is a moderation scope type, false otherwise.
     */
    this.isModerationScope = function() {
        return (bdrs.model.taxa.attributeScope.value.RECORD_MODERATION === this) || 
            (bdrs.model.taxa.attributeScope.value.SURVEY_MODERATION === this);
    };
    
    return this;
};

// Initialising the attribute types.
bdrs.model.taxa.attributeScope.RECORD = new bdrs.model.taxa.attributeScope.AttributeScope("SURVEY", "Survey");
bdrs.model.taxa.attributeScope.SURVEY = new bdrs.model.taxa.attributeScope.AttributeScope("RECORD", "Record");
bdrs.model.taxa.attributeScope.LOCATION = new bdrs.model.taxa.attributeScope.AttributeScope("LOCATION", "Location");
bdrs.model.taxa.attributeScope.RECORD_MODERATION = new bdrs.model.taxa.attributeScope.AttributeScope("SURVEY_MODERATION", "Survey Moderation");
bdrs.model.taxa.attributeScope.SURVEY_MODERATION = new bdrs.model.taxa.attributeScope.AttributeScope("RECORD_MODERATION", "Record Moderation");


