bdrs.util = {};
bdrs.util.file = {};

bdrs.util.file.IMAGE_FILE_EXTENSIONS = ['jpg','png','gif','jpeg'];

/**
 * Ensures that only image files can be added by inspecting the file extension
 * of the input value.
 * @param element the file input element.
 */
bdrs.util.file.imageFileUploadChangeHandler = function(element) {
    element = jQuery(element);
    var val = element.val();
    if(val !== undefined && val !== null && val.length > 0) {
        var ext = val.substr(val.lastIndexOf('.')+1, val.length).toLowerCase();

        // Put image types in the order of likelihood of showing up
        var IMAGE_TYPES = bdrs.util.file.IMAGE_FILE_EXTENSIONS;
        var imageType;
        var isValid = false;
        for(var i=0; i<IMAGE_TYPES.length; i++) {
            imageType = IMAGE_TYPES[i];
            if(ext === imageType) {
                isValid = true;
            }
        }

        if(isValid === false) {
            element.val(null);
        }
    }
    element.trigger("blur");
};

/**
 * Displays a confirmation dialog with the specified message. If the user
 * confirms the action, the specified form is submitted.
 *
 * @param message the message to be displayed in the confirmation dialog.
 * @param formSelector the jQuery selector to the form to be submitted if the 
 * user confirms the action.
 */
bdrs.util.confirmSubmit = function(message, formSelector) {
    var confirmation = confirm(message);
    if (confirmation) {
        jQuery(formSelector).submit();
    } 
};

bdrs.util.confirmExec = function(message, callback) {
    var confirmation = confirm(message);
    if(confirmation) {
        callback();
    }
};
bdrs.util.confirm = function(message) {
	return confirm(message);
};

/**
 * Formats a javascript Date object as a string dd MMM yyyy.
 *
 * @param a javascript Date object
 * @param a string representation of the Date object.
 */
bdrs.util.formatDate = function(date) {
    if(date === undefined || date === null) {
        return '';
    } else {
        return bdrs.util.zerofill(date.getDate(), 2) + " " + bdrs.monthNames[date.getMonth()] + " " +
            date.getFullYear();
    }
};

/**
 * Parses a date from the format dd MMM yyyy to a date object, or null if it cannot be parsed.
 * @param dateStr [string] the raw value to be parsed.
 */
bdrs.util.parseDate = function(dateStr) {
        var dateSplit = dateStr.split(' ');
        if(dateSplit.length !== 3) {
            return null;
        }
        
        var day = parseInt(dateSplit[0], 10);
        var month = dateSplit[1].toLowerCase();
        var year = parseInt(dateSplit[2], 10);
        
        // Resolve the months
        var complete = false;
        for(var i=0; !complete && i<bdrs.monthNames.length; i++) {
            if(bdrs.monthNames[i].toLowerCase() === month.toLowerCase()) {
                complete = true;
                month = i;  // converted to an index.
            }
        }
        
        if(!complete) {
            return null; 
        }
        
        var date = new Date(year, month, day);
        if(/Invalid|NaN/.test(date)) {
            return null;
        } else {
            return date;
        }
};

bdrs.util.zerofill = function(number, width) {
    width -= number.toString().length;
    if (width>0){
        var pattern = /\./;
        // Done this way because jslint does not like new Array();
        var array = [];
        array.length = width + (pattern.test( number ) ? 2 : 1);
        return array.join( '0' ) + number;
    }
    return number;
};

bdrs.util.formToMap = function(formSelector) {
    var data = {};
    var form = jQuery(formSelector);
    var inputs = form.find("select, input");

    var name;
    var inp;
    for(var i=0; i<inputs.length; i++) {
        inp = jQuery(inputs[i]);
        name = inp.attr("name");
        if(name !== undefined && name !== null && name.length > 0) {
            if(data.name === undefined) {
                data[name] = inp.val();
            } else if(!jQuery.isArray(data.name)){
                data[name] = [data[name], inp.val()];
            } else {
                data[name].push(inp.val());
            }
        }
    }
    return data;
};

/**
 * Event handler attached to key press events, this function prevents the
 * user from submitting the form by pressing the return key.
 */
bdrs.util.preventReturnKeySubmit = function(event) {
    var key;
    if(window.event) {
        //IE
        key = window.event.keyCode; 
    } else {
        //firefox
        key = e.which; 
    }      

     return (key != 13);
};

/**
 * Maximise the amount of screen real-estate provided to an element by wrapping
 * it in a div and making that div fill the entire screen.
 * 
 * @param triggerSelector the element that triggers the maximise/minimise state.
 * @param contentSelector the content that shall be displayed maximised or minimised.
 * @param maximiseLabel the label to indicate a maximise action.
 * @param minimiseLabel the label to indicate a minimise action.
 */
bdrs.util.maximise = function(triggerSelector, contentSelector, maximiseLabel, minimiseLabel) {
    var content = jQuery(contentSelector);
    var trigger = jQuery(triggerSelector);
    if(content.hasClass("maximise")) {
        content.unwrap();
        content.removeClass("maximise");
        jQuery('body').css('overflow', 'auto');
        trigger.text(maximiseLabel);
        jQuery(document).unbind('keyup');
    } else {
        var wrapper = content.wrap('<div></div>').parent();
        wrapper.css({
            position: 'fixed',
            top: 0,
            bottom: 0,
            left: 0,
            right: 0,
            padding: "2em 2em 2em 2em",
            backgroundColor: 'white',
            zIndex: 1500,
            overflow: 'auto'
        });
        jQuery('body').css('overflow', 'hidden');
        content.addClass("maximise");
        trigger.text(minimiseLabel);
        
      //add escape listener
		jQuery(document).keyup(function(key){
			if (key.keyCode === 27) {
				bdrs.util.maximise(triggerSelector, contentSelector, maximiseLabel, minimiseLabel);
			}
		});
    }
};

bdrs.util.createColorPicker = function(jqueryElement) {
    var getBeforeShowFunc = function(jqueryElement) {
        return function(el) {
            var color = jqueryElement.val();
            jqueryElement.ColorPickerSetColor(color);
        };
    };
    var getOnChangeFunc = function(jqueryElement) {
        return function(hsb, hex, rgb, el) {
            var color = '#' + hex;
            jqueryElement.val(color);
        }
    };
    var getOnSubmitFunc = function() {
        return function(hsb, hex, rgb, el) {
            $(el).val('#' + hex);
            $(el).ColorPickerHide();
        }
    };
    jqueryElement.ColorPicker({
        onBeforeShow: getBeforeShowFunc(jqueryElement),
        onChange: getOnChangeFunc(jqueryElement),
        onSubmit: getOnSubmitFunc()
    });
};

bdrs.util.printElement = function(selector) {
	$(selector).jqprint({
		importCSS: true
	});
};

if (bdrs.util.cookie === undefined) {
	bdrs.util.cookie = {};
}

bdrs.util.cookie.create = function(name, value, days){
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

bdrs.util.cookie.read = function(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

bdrs.util.cookie.erase = function(name) {
	createCookie(name,"",-1);
}
