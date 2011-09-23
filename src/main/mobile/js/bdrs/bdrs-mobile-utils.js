if (!window.bdrs) {
    bdrs = {};
}

if (!bdrs.mobile) {
    bdrs.mobile = {};
}

///////////////////////////
// Events
///////////////////////////
bdrs.mobile.fireEvent = function(type) {
    var e = document.createEvent('Events');
    e.initEvent(type);
    document.dispatchEvent(e);
};

bdrs.mobile.TIME_DELIMITER = ':';

bdrs.mobile.cameraExists = function() {
	return navigator.camera;
};

bdrs.mobile.fileIO = {};
bdrs.mobile.fileIO.SEP = '/';

/**
 * Returns true if the application can access the file system, false otherwise.
 */
bdrs.mobile.fileIO.exists = function() {
    return jQuery.isFunction(window.requestFileSystem);
};

/**
 * Logs the error code and human readable error type of the specified
 * Phonegap FileError object.
 *
 * @param fileError a phonegap FileError object.
 */
bdrs.mobile.fileIO.logFileError = function(fileError) {
    var msg = '';
    switch(fileError.code) {
        case FileError.NOT_FOUND_ERR:
            msg = "Not Found Error";
            break;
        case FileError.SECURITY_ERR:
            msg = "Security Error";
            break;
        case FileError.ABORT_ERR:
            msg = "Abort Error";
            break;
        case FileError.NOT_READABLE_ERR:
            msg = "Not Readable Error";
            break;
        case FileError.ENCODING_ERR:
            msg = "Encoding Error";
            break;
        case FileError.NO_MODIFICATION_ALLOWED_ERR:
            msg = "No Modification Allowed Error";
            break;
        case FileError.INVALID_STATE_ERR:
            msg = "Invalid State Error"
            break;
        case FileError.SYNTAX_ERR:
            msg = "Syntax Error";
            break;
        case FileError.INVALID_MODIFICATION_ERR:
            msg = "Invalid Modification Error";
            break;
        case FileError.QUOTA_EXCEEDED_ERR:
            msg = "Quota Exceeded Error";
            break;
        case FileError.TYPE_MISMATCH_ERR:
            msg = "Type Mismatch Error";
            break;
        case FileError.PATH_EXISTS_ERR:
            msg = "Path Exists Error";
            break;
        default:
            msg = "Unknown Error";
            break;
    }

    bdrs.mobile.Error("File Error "+fileError.code+": "+msg);
};

/**
 * Creates the specified directory and all parent directories.
 *
 * @param dirEntry a phonegap DirectoryEntry specifying the parent directory
 * that will contain all child directories to be created.
 * @param path a string specifying the directories to create. For example "foo/bar/spam".
 * @param successCallback invoked on success with the deepest phonegap DirectoryEntry instance.
 * @param errorCallback invoked on error with no parameters.
 */
bdrs.mobile.fileIO.makeDirs = function(dirEntry, path, successCallback, errorCallback) {
    // Remove leading slashes
    while(path.length > 0 && path.indexOf(bdrs.mobile.fileIO.SEP) === 0) {
        path = path.slice(1,path.length);
    }

    if(path.length === 0) {
        successCallback(dirEntry);
    } else {
        var split = path.split(bdrs.mobile.fileIO.SEP);
        var dirName = split.splice(0,1)[0];
        var subDirs = split.join(bdrs.mobile.fileIO.SEP);
        bdrs.mobile.Debug('Creating directory: "' + dirName +'" in ' + dirEntry.fullPath);

        dirEntry.getDirectory(dirName, {create:true}, 
            function(childDirEntry) {
                // Create Directory Success Handler
                bdrs.mobile.fileIO.makeDirs(childDirEntry, subDirs, successCallback, errorCallback);
            }, 
            function(fileError) {
                // Create Directory Error Handler
                bdrs.mobile.Error("Directory Creation Error");
                bdrs.mobile.fileIO.logFileError(fileError);
                errorCallback();
            }
        );
    }
};

/**
 * Writes the specified content to the file specified by the path.
 *
 * @param filepath the path to the file that shall be written. If the file
 * or any parent directories do not exist, they will be created. An example
 * value for the filepath may be "foo/bar/spam/egg.txt".
 * @param content the string that will be written to file.
 * @param successCallback invoked on success with no parameters.
 * @param errorCallback invoked on error with no parameters.
 */
bdrs.mobile.fileIO.writeFile = function(filepath, content, successCallback, errorCallback) {
    if(!bdrs.mobile.fileIO.exists()) {
        bdrs.mobile.Debug("Cannot write file. No file system.");
        errorCallback();
        return;
    }

    if(filepath.trim().length === 0) {
        bdrs.mobile.Debug("Cannot write file. No file specified.");
        errorCallback();
        return;
    }

    window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, 
        function(fileSystem) {
            // requestFileSystem success handler
            var split = filepath.split(bdrs.mobile.fileIO.SEP);
            var path = split.slice(0, split.length-1).join(bdrs.mobile.fileIO.SEP);
            var filename = split.slice(split.length-1, split.length)[0];

            bdrs.mobile.fileIO.makeDirs(fileSystem.root, path, 
                function(dirEntry) {
                    // makeDirs success handler
                    dirEntry.getFile(filename, {create:true}, 
                        function(fileEntry) {
                            // getFile success handler
                            fileEntry.createWriter(
                                function(writer) {
                                    // createWriter success handler
                                    writer.onwrite = successCallback();
                                    writer.write(content);
                                },
                                function(fileError) {
                                    // createWriter error handler
                                    bdrs.mobile.Debug("Error creating file writer");
                                    bdrs.mobile.fileIO.logFileError(fileError);
                                    errorCallback();
                                }
                            );
                        }, 
                        function(fileError) {
                            // getFile error handler
                            bdrs.mobile.Debug("Error creating file: " + filename);
                            bdrs.mobile.fileIO.logFileError(fileError);
                            errorCallback();
                        }
                    );
                }, 
                errorCallback
            );
        }, 
        function(fileError) {
            // requestFileSystem error Handler
            bdrs.mobile.Debug("Error requesting file system.");
            bdrs.mobile.fileIO.logFileError(fileError);
            errorCallback();
        }
    );
};

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

bdrs.mobile.parseBoolean = function(boolStr) {
    if(boolStr === null || boolStr === undefined) {
        return false;
    }
    
    return boolStr.toString().toLowerCase() === "true";
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
    element.find("input[type=checkbox]").checkboxradio();
    element.find("input[type=checkbox]").checkboxradio('refresh');
    element.find("select").selectmenu();
    element.find("[data-role=controlgroup]").controlgroup();
    element.find("[data-role=fieldcontain]").fieldcontain();
    element.find("[data-role=listview]").listview();
    element.find("a[data-role=button]").button();
    
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

///////////////////////////
// CSV
///////////////////////////
bdrs.mobile.csv = {};
bdrs.mobile.csv.toCSVString = function(strArray) {
    if(strArray === null || strArray === undefined) {
        return "";
    } else if(strArray.length === 0) {
        return "";
    }
    
    var escapedAndQuoted = [];
    for(var i=0; i<strArray.length; i++) {
        var raw = strArray[i];
        raw = raw.replace(/\"/i, '""');
        escapedAndQuoted.push(raw);
    }
    var csv = '"' + escapedAndQuoted.join('","') + '"';
    return csv;
};

bdrs.mobile.csv.fromCSVString = function(csv) {
    if(csv === null || csv === undefined) {
        return [];
    } else if (csv.length === 0) {
        return [];
    }
    
    var rawSplit = csv.substr(1,csv.length-2).split('","');
    var split = [];
    for(var i=0; i<rawSplit.length; i++) {
        split.push(rawSplit[i].replace(/\"\"/i, '"'));
    }
    return split;
};

///////////////////////////
// Decoupling of Inputs
///////////////////////////
bdrs.mobile.form = {};
bdrs.mobile.form.InputMap = function() {
    
    this._data = {};
    
    this.put = function(key, value) {
        this._data[key] = [value];
    };

    this.add = function(key, value) {
        var a = this._data[key];
        if(a === undefined) {
            a = [];
        }
        a.push(value);
        this._data[key] = a;
    };

    this.get = function(key) {
        var a = this.getArray(key);
        return a === null ? null : a[0];
    };

    this.getArray = function(key) {
        var a = this._data[key];
        return a === undefined ? null : a;
    };

    return this;
};

bdrs.mobile.form.inputsToMap = function(selector) {
    
    var map = new bdrs.mobile.form.InputMap();
    var elems = jQuery(selector).find('input[type=text], input[type=password], input[type=checkbox]:checked, input[type=radio]:checked, input[type=number], input[type=hidden], input[type=submit], input[type=button], textarea, select');

    for(var i=0; i<elems.length; i++) {
        var elem = jQuery(elems[i]);
        var name = elem.attr('name');
        if(name !== undefined) {
            map.add(name, elem.val());
        }
    }

    return map;
};