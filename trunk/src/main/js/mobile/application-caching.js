var webAppCache = window.applicationCache;
var cacheStatusValues = ['uncached','idle','checking','downloading','updateready','obsolete'];

webAppCache.addEventListener('checking', checkingEvent, false);
webAppCache.addEventListener('noupdate', noupdateEvent, false);
webAppCache.addEventListener('downloading', downloadingEvent, false);
webAppCache.addEventListener('progress', progressEvent, false);
webAppCache.addEventListener('cached', cachedEvent, false);
webAppCache.addEventListener('updateready',  updatereadyEvent ,false);
webAppCache.addEventListener('obsolete',obsoleteEvent, false);
webAppCache.addEventListener('error', errorEvent, false);
webAppCache.addEventListener('loadedmetadata', loadedmetadataEvent, false);
webAppCache.addEventListener('idle', idleEvent, false);

/**
 * Event that gets fired when the browser is checking for changes in the manifest.
 * @param e The actual event that got fired.
 */ 
function checkingEvent(e){
}

/**
 * Event that gets fired when files mentioned in the manifest are being downloaded.
 * TODO: This event gets fired after ...
 * @param e The actual event that got fired.
 */ 
function downloadingEvent(e){
	//downld = true;
}

/**
 * Event that gets fired when files mentioned in the manifest finished updating.
 * TODO: This event gets fired after ...
 * @param e The actual event that got fired.
 */ 
function updatereadyEvent(e){
	dataForOffline();
	webAppCache.swapCache();
	//setUpdateResultMsg(true);
}

/**
 * Event that gets fired when files mentioned in the manifest do not need to be updated.
 * TODO: This event gets fired after ...
 * @param e The actual event that got fired.
 */ 
function noupdateEvent(e){
}

/**
 * TODO: Event that gets fired when f...
 * This event gets fired after ...
 *  @param e The actual event that got fired.
 */
function cachedEvent(e){
	//initialize dialog
/*	jQuery( "#dialog" ).dialog({
		title: "Download progress",
		autoOpen: false,
		show: "drop",
		hide: "drop",
		modal: true
	});
	//inject application data progress bar in dialog
	jQuery( "#dialog_content" ).append("<div id='progressbar'/><div id='download_result_msg'>The download was succesfull.</div>");
	jQuery('#download_result_msg').hide();
	//show  download result message when the progress bar is finished loading
	jQuery("#progressbar").progressbar( { value : 0 , complete : function(event, ui) {
			jQuery('#download_result_msg').show();
		}});
	//show dialog
	jQuery( "#dialog" ).dialog( "open" );*/
	// Start download application data
	dataForOffline();
}


function idleEvent(e){}


function progressEvent(e){}


function obsoleteEvent(e){}


function loadedmetadataEvent(e){}


function errorEvent(e){
	logEvent(e);
	if(e.status == 0 || downld == true)
		setDownloadResultMsg(BrowserDetect.browser, false);
}

/**
 * Logs details about fired manifest events.
 * @param e A manifest event (e.g. noupdateEvent, cachedEvent, updatereadyEvent, ..).
 */
function logEvent(e) {
	var online = (navigator.onLine) ? 'yes' : 'no';
    var message = 'event: ' +  e.type;
    message += ', status: ' + cacheStatusValues[webAppCache.status];
    message += ', online: ' + online;
    message += ', time: ' + new Date().getUTCMilliseconds() + "ms";
    if (e.type == 'error' && navigator.onLine) {
        message+= ', (prob a syntax error in manifest)';
        logMessage(e);
    }
   logMessage(message);
}