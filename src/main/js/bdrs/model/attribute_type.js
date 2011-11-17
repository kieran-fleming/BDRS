if (window.bdrs === undefined) {
    window.bdrs = {};
}

if (bdrs.model === undefined) {
    bdrs.model = {};
}

if (bdrs.model.taxa === undefined) {
    bdrs.model.taxa = {};
}

if (bdrs.model.taxa.attributeType === undefined) {
    bdrs.model.taxa.attributeType = {};
}

if (bdrs.model.taxa.attributeType.code === undefined) {
    bdrs.model.taxa.attributeType.code = {};
}

if (bdrs.model.taxa.attributeType.value === undefined) {
    bdrs.model.taxa.attributeType.value = {};
}

/**
 * A Javascript representation of the AttributeType object.
 * @param [string] value the name of the enumeration.1
 * @param [string] code the type code of this attribute type.
 * @param [string] name the name of this attribute type.
 */
bdrs.model.taxa.attributeType.AttributeType = function(value, code, name) {
    this.value = value;
    this.code = code;
    this.name = name;

    // Registration with the various lookups.
    bdrs.model.taxa.attributeType.code[this.code] = this;
    bdrs.model.taxa.attributeType.value[this.value] = this;
    
    /**
     * @return [boolean] true if this is a file type, false otherwise.
     */
    this.isFileType = function() {
        return (bdrs.model.taxa.attributeType.value.IMAGE === this) || 
            (bdrs.model.taxa.attributeType.value.FILE === this);
    };
    
    return this;
};

// Initialising the attribute types.
new bdrs.model.taxa.attributeType.AttributeType('INTEGER', 'IN', 'Integer');
new bdrs.model.taxa.attributeType.AttributeType('INTEGER_WITH_RANGE', 'IR', 'Integer Range');
new bdrs.model.taxa.attributeType.AttributeType('DECIMAL', 'DE', 'Decimal');
new bdrs.model.taxa.attributeType.AttributeType('BARCODE', 'BC', 'Bar Code');
new bdrs.model.taxa.attributeType.AttributeType('DATE', 'DA', 'Date');
new bdrs.model.taxa.attributeType.AttributeType('TIME', 'TM', 'Time');
new bdrs.model.taxa.attributeType.AttributeType('STRING', 'ST', 'Short Text');
new bdrs.model.taxa.attributeType.AttributeType('STRING_AUTOCOMPLETE', 'SA', 'Short Text (Auto Complete)');
new bdrs.model.taxa.attributeType.AttributeType('TEXT', 'TA', 'Long Text');
new bdrs.model.taxa.attributeType.AttributeType('HTML', 'HL', 'HTML');
new bdrs.model.taxa.attributeType.AttributeType('HTML_COMMENT', 'CM', 'Comment');
new bdrs.model.taxa.attributeType.AttributeType('HTML_HORIZONTAL_RULE', 'HR', 'Horizontal Rule');
new bdrs.model.taxa.attributeType.AttributeType('STRING_WITH_VALID_VALUES', 'SV', 'Selection');
new bdrs.model.taxa.attributeType.AttributeType('SINGLE_CHECKBOX', 'SC', 'Single Checkbox');
new bdrs.model.taxa.attributeType.AttributeType('MULTI_CHECKBOX', 'MC', 'Multi Checkbox');
new bdrs.model.taxa.attributeType.AttributeType('MULTI_SELECT', 'MS', 'Multi Select');
new bdrs.model.taxa.attributeType.AttributeType('IMAGE', 'IM', 'Image File');
new bdrs.model.taxa.attributeType.AttributeType('FILE', 'FI', 'Data File');


