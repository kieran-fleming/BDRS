bdrs.threshold = {};

bdrs.threshold.handlers = {};

bdrs.threshold.handlers.changePropertyPath = function(classNameSelector, propertyPathElem, index, conditionId) {
    
    // Delete all elements to the right
    jQuery(propertyPathElem).nextAll().remove();
    
    var params = {};
    params.className = jQuery(classNameSelector).val();
    params.propertyPath = jQuery(propertyPathElem).val();
    if(index === null) {
        params.conditionId = conditionId;
    } else {
        params.index = index;
    }
    
    jQuery.get(bdrs.contextPath+"/bdrs/admin/threshold/ajaxChangePropertyPath.htm", params, function(data) {
        var pathElem = jQuery(propertyPathElem);
        var subPathElem = jQuery(data);
        pathElem.after(subPathElem);
        subPathElem.trigger("change");
        
        subPathElem.parents("form").ketchup();
        bdrs.initDatePicker();
    });
};


bdrs.threshold.handlers.changeClass = function(event) {
    var data = event.data;
    jQuery(data.conditionContainerSelector).empty();
};

bdrs.threshold.handlers.changeActionType = function(actionTypeSelector, actionCellSelector, index, actionId) {
    jQuery(actionCellSelector).empty();
    
    var params = {
        actionType : jQuery(actionTypeSelector).val()
    };
    if(index === null) {
        params.actionId = actionId;
    } else {
        params.index = index;
    }
    
    jQuery.get(bdrs.contextPath+"/bdrs/admin/threshold/ajaxActionValueForActionType.htm", params, function(data) {
        jQuery(actionCellSelector).append(data);
    });
};

bdrs.threshold.addCondition = function(indexSelector, classNameSelector, conditionContainerSelector) {
    var indexElem = jQuery(indexSelector);
    var index = parseInt(indexElem.val(), 10);
    indexElem.val(index+1);
    
    var classNameElem = jQuery(classNameSelector);
    var params = {
        className : classNameElem.val(),
        index: index
    };
    
    jQuery.get(bdrs.contextPath+"/bdrs/admin/threshold/ajaxAddCondition.htm", params, function(data) {
        var conditionContainerElem = jQuery(conditionContainerSelector);
        var condition = jQuery(data);
        conditionContainerElem.append(condition);
        condition.find(".pathSelect").trigger("change");
    });
};

bdrs.threshold.addAction = function(indexSelector, classNameSelector, actionTableSelector) {
    var indexElem = jQuery(indexSelector);
    var index = parseInt(indexElem.val(), 10);
    indexElem.val(index+1);
    
    var classNameElem = jQuery(classNameSelector);
    var params = {
        className : classNameElem.val(),
        index: index
    };
    
    jQuery.get(bdrs.contextPath+"/bdrs/admin/threshold/ajaxAddAction.htm", params, function(data) {
        var actionTableBodyElem = jQuery(actionTableSelector).find("tbody");
        actionTableBodyElem.append(data);
    });
};