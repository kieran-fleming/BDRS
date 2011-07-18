if (!window.bdrs) {
    bdrs = {};
}

if (!bdrs.mobile) {
    bdrs.mobile = {};
}

bdrs.mobile.TIME_DELIMITER = ':';

bdrs.mobile.cameraExists = function() {
	return navigator.camera;
};

bdrs.mobile.fileMgrExists = function() {
    return navigator.fileMgr !== null && navigator.fileMgr !== undefined;
}

bdrs.mobile.Point = function(str) {
        var latitude;
        var longitude;
        
        var work = str.substring(str.indexOf('(') + 1, str.lastIndexOf(')'));
        work = work.split(' ');
        longitude = parseFloat(work[0]);
        latitude = parseFloat(work[1]);
        
        this.getLatitude = function() { 
        	return latitude; 
        }
        
        this.getLongitude = function() { 
        	return longitude; 
        }
        
        return this;
	};

String.IsNullOrEmpty = function(value) {
	var isNullOrEmpty = true;
	if (value) {
		if (value.length > 0) {
			isNullOrEmpty = false;
		}
	}
  	return isNullOrEmpty;
}

bdrs.mobile.months = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

bdrs.mobile.getCurrentDate = function() {
	return bdrs.mobile.formatDate(new Date());
};

bdrs.mobile.formatDate = function(date) {
    return bdrs.mobile.zerofill(date.getDate(), 0) + " " + bdrs.mobile.months[date.getMonth()] + " " + date.getFullYear();
};

bdrs.mobile.parseDate = function(dateStr) {
        var dateSplit = dateStr.split(' ');
        if(dateSplit.length !== 3) {
            return null;
        }
        
        var day = parseInt(dateSplit[0], 10);
        var month = dateSplit[1].toLowerCase();
        var year = parseInt(dateSplit[2], 10);
        
        // Resolve the months
        var complete = false;
        for(var i=0; !complete && i<bdrs.mobile.months.length; i++) {
            if(bdrs.mobile.months[i].toLowerCase() == month.toLowerCase()) {
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

bdrs.mobile.getCurrentTime = function() {
	var now = new Date();
	return now.getHours() + bdrs.mobile.TIME_DELIMITER + bdrs.mobile.zerofill(now.getMinutes(), 2) + bdrs.mobile.TIME_DELIMITER + bdrs.mobile.zerofill(now.getSeconds(), 2);
};

bdrs.mobile.getDaysBetween = function(startDate, endDate) {
    return Math.floor((endDate - startDate)/(1000 * 60 * 60 * 24));
};

bdrs.mobile.getDaysBetweenAsFormattedString = function(startDate, endDate) {
    var daysBetween = bdrs.mobile.getDaysBetween(startDate, endDate);
    var str;
    
    if(daysBetween === 0) {
        str = "Today";
    } else { 
        var dayStr = Math.abs(daysBetween) === 1 ? 'day' : 'days';
        if(daysBetween < 0) {
            str = ["In", Math.abs(daysBetween), dayStr].join(' ');
        } else {
            str = [daysBetween, dayStr, "ago"].join(' ');
        }
    }
    return str;
};

bdrs.mobile.zerofill = function(number, width) {
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

/** 
 * Round a number to given number of decimal places.
 */ 
bdrs.mobile.roundnumber = function(num, dec) {
	var result = Math.round( Math.round( num * Math.pow( 10, dec + 1 ) ) / Math.pow( 10, 1 ) ) / Math.pow(10,dec);
	return result;
};

bdrs.mobile.restyle = function(target) {
    var element = jQuery(target);
    element.find("button, input[type=button], input[type=submit], input[type=reset]").button();
    element.find("input[type=text], input[type=number], textarea").textinput();
    element.find("input[type=file]").textinput();
    element.find("input[type=radio]").checkboxradio();
    element.find("input[type=checkbox]").checkboxradio('refresh');
    element.find("select").selectmenu();
    element.find("[data-role=controlgroup]").controlgroup();
    element.find("[data-role=fieldcontain]").fieldcontain();
    element.find("[data-role=listview]").listview();
    
    element.find("[data-role=collapsible]").collapsible();
    // Make sure to account for those collapsibles that do not specify data-collapsed.
    element.find("[data-collapsed=true]").trigger('collapse');
    element.find("[data-collapsed=false]").trigger('expand');
};

bdrs.mobile.guidGenerator = function() {
    var S4 = function() {
       return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
    };
    return (S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
};

bdrs.mobile.toHtmlString = function(elem) {
    var tmpParent = jQuery("<span></span");
    jQuery(elem).appendTo(tmpParent);
    return tmpParent.html();
};

bdrs.mobile.getParameterByName = function(name) {
	name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	var regexS = "[\\?&]" + name + "=([^&#]*)";
	var regex = new RegExp(regexS);
	var results = regex.exec(window.location.href);
	if(results == null)
		return "";
	else
	    return decodeURIComponent(results[1].replace(/\+/g, " "));
};


///////////////////////////
// Record Utils
///////////////////////////
bdrs.mobile.record = {};
bdrs.mobile.record.util = {};
bdrs.mobile.record.util.getDescriptor = function(record) {
    var descriptor = {};
    if(record.censusMethod() !== undefined && record.censusMethod() !== null) {
        descriptor.title = record.censusMethod().name() + " : " + record.latitude() + ', ' + record.longitude();
    } else if(record.species() !== undefined && record.species() !== null) {
        descriptor.title = record.species().scientificName() + " : " + record.latitude() + ', ' + record.longitude(); 
    } else {
        descriptor.title = record.latitude() + ', ' + record.longitude();
    }
    
    descriptor.description = bdrs.mobile.formatDate(record.when());
    return descriptor;
};

