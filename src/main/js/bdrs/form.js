/**
 * @file Generic form related functions should go in here
 * @author kehan
 */

bdrs.form = {};
/**
 * Automatically populates form fields with values from query parameters in the URL
 */
bdrs.form.prepopulate = function(){
	var fields = jQuery("form").serializeArray();
	for ( var i = 0; i < fields.length; i++) {
		var field = fields[i];
		if(field.value == ""){
		var value = bdrs.getParameterByName(field.name);
		var selector = '[name="' + field.name +'"]';
		jQuery(selector).filter(function(){
			/**
			 * Exclude any fields we don't want to using css
			 */
			return !$(this).hasClass("skipPrepopulate", "bdrsPrepopulated");
		}).val(value).addClass("bdrsPrepopulated");
		}
	}
	
	// update the date field to the current date last if it has not already been set
    if (jQuery("#date").val() != undefined && jQuery("#date").val().length === 0) {
    	jQuery("#date").val(bdrs.util.formatDate(new Date()));
    }
};
