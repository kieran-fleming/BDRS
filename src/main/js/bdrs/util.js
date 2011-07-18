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
}

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
        trigger.text(maximiseLabel);
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
            zIndex: 1500
        });
        content.addClass("maximise");
        trigger.text(minimiseLabel);
    }
};

bdrs.util.createColorPicker = function(selector) {
	var getBeforeShowFunc = function(selector) {
        return function(el) {
            var color = $(selector).val();
            $(selector).ColorPickerSetColor(color);
        };
    };
    var getOnChangeFunc = function(selector) {
        return function(hsb, hex, rgb, el) {
            var color = '#' + hex;
            $(selector).val(color);
        }
    };
    var getOnSubmitFunc = function() {
        return function(hsb, hex, rgb, el) {
            $(el).val('#' + hex);
            $(el).ColorPickerHide();
        }
    };
	$(selector).ColorPicker({
        onBeforeShow: getBeforeShowFunc(selector),
        onChange: getOnChangeFunc(selector),
        onSubmit: getOnSubmitFunc()
    });
};
