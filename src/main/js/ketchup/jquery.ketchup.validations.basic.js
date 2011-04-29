$.fn.ketchup.validation('required', function(element, value) {
  if(element.attr('type') == 'checkbox') {
    if(element.attr('checked') == true) return true;
    else return false;
  } if(element.attr('type') == 'radio') {
    var radios = element.parents("fieldset").find("[type=radio]");
    for(var i=0; i<radios.length; i++) {
        if(jQuery(radios[i]).attr("checked")) {
            return true;
        }
    }
    return false;
  } else {
    if(value.length === 0) return false;
    else return true;
  }
});

$.fn.ketchup.validation('minlength', function(element, value, minlength) {
  if(value.length < minlength) return false;
  else return true;
});

$.fn.ketchup.validation('maxlength', function(element, value, maxlength) {
  if(value.length > maxlength) return false;
  else return true;
});

$.fn.ketchup.validation('rangelength', function(element, value, minlength, maxlength) {
  if(value.length >= minlength && value.length <= maxlength) return true;
  else return false;
});

$.fn.ketchup.validation('rangelengthOrBlank', function(element, value, minlength, maxlength) {
	return ((value.length >= minlength && value.length <= maxlength)||value.length === 0);
});

$.fn.ketchup.validation('min', function(element, value, min) {
  if(parseInt(value) < min) return false;
  else return true;
});

$.fn.ketchup.validation('max', function(element, value, max) {
  if(parseInt(value) > max) return false;
  else return true;
});

$.fn.ketchup.validation('range', function(element, value, min, max) {
  if(parseInt(value) >= min && parseInt(value) <= max) return true;
  else return false;
});

$.fn.ketchup.validation('rangeFloat', function(element, value, min, max) {
      if(parseFloat(value) >= min && parseFloat(value) <= max) return true;
      else return false;
    });

$.fn.ketchup.validation('rangeOrBlank', function(element, value, min, max) {
  if(element.val().length === 0) {
    return true;
  }
  else {
      if(parseInt(value) >= min && parseInt(value) <= max) return true;
      else return false;
  }
});


$.fn.ketchup.validation('number', function(element, value) {
  if(/^-?(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?$/.test(value)) return true;
  else return false;
});

$.fn.ketchup.validation('numberOrBlank', function(element, value) {
    if(value.length === 0) {
        return true;
    } else {
        if(/^-?(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?$/.test(value)) return true;
        else return false;
    }
  });

$.fn.ketchup.validation('digits', function(element, value) {
  if(/^\d+$/.test(value)) return true;
  else return false;
});


$.fn.ketchup.validation('integer', function(element, value) {
  if(/^\-?\d+$/.test(value)) return true;
  else return false;
});

$.fn.ketchup.validation('integerOrBlank', function(element, value) {
    if(value.length === 0) {
        // blank
        return true;
    } else {
        if(/^\-?\d+$/.test(value)){
            return true;
        } else {
            return false;
        }
    }
});

$.fn.ketchup.validation('positiveIntegerOrBlank', function(element, value) {
    if(value.length === 0) {
        // blank
        return true;
    } else {
        if(/^\d+$/.test(value)) {
            return true;
        } else {
            return false;
        }
    }
});

$.fn.ketchup.validation('positiveInteger', function(element, value) {
    if(/^\d+$/.test(value)) {
        return true;
    } else {
        return false;
    }
});

$.fn.ketchup.validation('positiveIntegerLessThanOneMillion', function(element, value) {
    if(/^\d+$/.test(value)) {
        var v = parseInt(value, 10);
        return v < 1000000;
    } else {
        return false;
    }
});

$.fn.ketchup.validation('email', function(element, value) {
  if(/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i.test(value)) return true;
  else return false;
});

$.fn.ketchup.validation('emailOrBlank', function(element, value) {
  if(element.val().length === 0) {
    return true;
  }
  else {
      if(/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i.test(value)) return true;
      else return false;
  }
});


$.fn.ketchup.validation('url', function(element, value) {
  if(/^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(value)) return true;
  else return false;
});


$.fn.ketchup.validation('username', function(element, value) {
  if(/^([a-zA-Z])[a-zA-Z_-]*[\w_-]*[\S]$|^([a-zA-Z])[0-9_-]*[\S]$|^[a-zA-Z]*[\S]$/.test(value)) return true;
  else return false;
});

$.fn.ketchup.validation('usernameOrBlank', function(element, value) {
	  if(/^([a-zA-Z])[a-zA-Z_-]*[\w_-]*[\S]$|^([a-zA-Z])[0-9_-]*[\S]$|^[a-zA-Z]*[\S]$/.test(value) || value.length == 0) return true;
	  else return false;
});


$.fn.ketchup.validation('match', function(element, value, match) {
  if($(match).val() != value) return false;
  else return true;
});


$.fn.ketchup.validation('date', function(element, value) {
  if(!/Invalid|NaN/.test(new Date(value))) return true;
  else return false;
});


function watchSelect(type) {
  $('input['+$.fn.ketchup.defaults.validationAttribute+'*="'+type+'"]').each(function() {
    var el = $(this);

    $('input[name="'+el.attr('name')+'"]').each(function() {
      var al = $(this);
      if(al.attr($.fn.ketchup.defaults.validationAttribute).indexOf(type) == -1) al.blur(function() { el.blur(); });
    });
  });
}

$(document).ready(function() {
  watchSelect('minselect');
  watchSelect('maxselect');
  watchSelect('rangeselect');
});

$.fn.ketchup.validation('minselect', function(element, value, min) {
  if($('input[name="'+element.attr('name')+'"]:checked').length >= min) return true;
  else return false;
});

$.fn.ketchup.validation('time', function(element, value) {
	  if(/^(20|21|22|23|[01]\d|\d)(([:][0-5]\d){1,2})$/.test(value)) return true;
	  else return false;
	});

$.fn.ketchup.validation('timeOrBlank', function(element, value, min) {
	 if(/^(20|21|22|23|[01]\d|\d)(([:][0-5]\d){1,2})$/.test(value) || value.length == 0) return true;
	  else return false;
	});

$.fn.ketchup.validation('maxselect', function(element, value, max) {
  if($('input[name="'+element.attr('name')+'"]:checked').length <= max) return true;
  else return false;
});

$.fn.ketchup.validation('rangeselect', function(element, value, min, max) {
  var checked = $('input[name="'+element.attr('name')+'"]:checked');

  if(checked.length >= min && checked.length <= max) return true;
  else return false;
});

$.fn.ketchup.validation('unique', function(element, value, uniqueElementsSelector) {
    var elems = jQuery(uniqueElementsSelector);
    var current;
    var isUnique = true;
    for(var i=0; i<elems.length; i++) {
        if(element[0] !== elems[i] && isUnique) {
            current = jQuery(elems[i]);
            if(current.val() === value) {
                isUnique = false;
            }
        }
    }
    return isUnique;
});

$.fn.ketchup.validation('uniqueOrBlank', function(element, value, uniqueElementsSelector) {
    if(value.length === 0) {
        // Blank is allowed
        return true;
    }

    var elems = jQuery(uniqueElementsSelector);
    var current;
    var isUnique = true;
    for(var i=0; i<elems.length; i++) {
        if(element[0] !== elems[i] && isUnique) {
            current = jQuery(elems[i]);
            if(current.val() === value) {
                isUnique = false;
            }
        }
    }
    return isUnique;
});