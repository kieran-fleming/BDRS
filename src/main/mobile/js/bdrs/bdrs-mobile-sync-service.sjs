
exports._syncListeners = [];
exports._lastSync = null;

exports.init = function() {
    // Add a connectivity listener. Synchronize with the server when a 
    // connection is present.
    bdrs.mobile.connectivity.addConnectivityListener(function(event) {
        if(event.textStatus === 'success') {
            bdrs.mobile.syncService.synchronize();
        }
    });
    bdrs.mobile.Debug("Synchronization Service Started");
};

exports.synchronize  = function() {
    var lastSyncTime = bdrs.mobile.syncService.getLastSyncTime();
    var modifiedRecords;
    waitfor(modifiedRecords) {
        Record.all().filter('modifiedAt', '>', lastSyncTime).prefetch('censusMethod').prefetch('survey').prefetch('species').list(resume);
    };
    
    var recLookup = {};
    var recAttrLookup = {};
    var idLookup = {
        'Record' : recLookup,
        'AttributeValue': recAttrLookup
    };
    
    var uploadData = [];
    for(var i=0; i<modifiedRecords.length; i++) {
        var rec = modifiedRecords[i];
        recLookup[rec.id] = rec;

        // Core Fields
        var recJSON = {};
        recJSON.id = rec.id;
        recJSON.server_id = rec.server_id();
        recJSON.latitude = rec.latitude();
        recJSON.longitude = rec.longitude();
        recJSON.accuracy = rec.accuracy();
        recJSON.when = bdrs.mobile.syncService._dateToJSON(rec.when(), rec.time());
        recJSON.lastDate = bdrs.mobile.syncService._dateToJSON(rec.lastDate(), rec.lastTime());
        recJSON.notes = rec.notes();
        recJSON.number = rec.number();
        recJSON.survey_id = rec.survey().server_id();

        // Census Methods        
        var censusMethod = rec.censusMethod();
        recJSON.censusMethod_id = censusMethod === null ? null : censusMethod.server_id();
        
        // Taxonomy
        var species = rec.species();
        recJSON.taxon_id = species === null ? null : species.server_id();
        
        // Record Attributes
        var attributeValues;
        waitfor(attributeValues) {
            rec.attributeValues().prefetch('attribute').list(resume);
        }
        var attributeValueArray = [];
        for(var j=0; j<attributeValues.length; j++) {
            var recAttr = attributeValues[j];
            recAttrLookup[recAttr.id] = recAttr;
            
            var attr = recAttr.attribute();

            var recAttrJSON = {};
            recAttrJSON.id = recAttr.id;
            recAttrJSON.server_id =  recAttr.server_id();
            recAttrJSON.attribute_id = attr.server_id();
            recAttrJSON.value = recAttr.value();
            
            // Date Type
            if(bdrs.mobile.attribute.type.DATE === attr.typeCode()) {
                var dateVal = bdrs.mobile.parseDate(recAttr.value());
                recAttrJSON.value = bdrs.mobile.syncService._dateToJSON(dateVal, null);
            } else {
                recAttrJSON.value = recAttr.value();
            }
            
            attributeValueArray.push(recAttrJSON);
        }
        
        recJSON.attributeValues = attributeValueArray;
        uploadData.push(recJSON);
    }
    
    if(uploadData.length < 1) {
        // No data to upload
        return;
    }
    
    bdrs.mobile.Debug("Uploading to the Server:");
    bdrs.mobile.Debug(JSON.stringify(uploadData));
    
    // Insert an iframe into the body    
    var containerId = new Date().getTime().toString();
    var syncContainer;
    waitfor(syncContainer) {
        bdrs.template.renderOnlyCallback('clientSyncContainer', {
            containerId: containerId
        }, resume);
    }
    jQuery("body").append(syncContainer);
    
    // Insert the form into the iframe
    var clientSyncContent;
    waitfor(clientSyncContent) {
        bdrs.template.renderOnlyCallback('clientSyncContent', {
            url: bdrs.mobile.User.server_url() + "/webservice/application/clientSync.htm",
            ident: bdrs.mobile.User.ident(),
            syncData: JSON.stringify(uploadData)
        }, resume);
    }
    var iframeSelector = "#"+containerId+" > iframe";
    jQuery(iframeSelector).contents().find("body").append(clientSyncContent);

    // Submit the form and wait for the response    
    var receiveMessage;
    var tempEventListener;
    waitfor(receiveMessage) {
        tempEventListener = resume;
        window.addEventListener('message', resume, false);
        clientSyncContent.submit();
    }
    finally {
        // Clean up the event listener
        window.removeEventListener('message', tempEventListener, false);
        delete tempEventListener;
        
        // Clean up the iframe
        syncContainer.remove();
    }
    
    bdrs.mobile.Debug("Received Server Response:");
    bdrs.mobile.Debug(receiveMessage.data);
    
    var response = jQuery.parseJSON(receiveMessage.data);
    var status = response.status;
    
    if(status === 200) {
        var uploadTime = new Date();
        var syncResult = response[status].sync_result;
        for(var i=0; i<syncResult.length; i++) {
            var result = syncResult[i];
            var instance = idLookup[result.klass][result.id];
            instance.server_id(result.server_id);
            if(instance.uploadedAt !== undefined) {
                instance.uploadedAt(uploadTime);
            }
        }
        persistence.flush();

        bdrs.mobile.syncService._lastSync = uploadTime;
        bdrs.mobile.syncService._fireSyncEvent({_type: 'sync'});
    } else {
        // Something bad this way comes
        var type = response[status].type;
        var message = response[status].message;
        bdrs.mobile.Error("Synchronization Error: Status Code - "+status);
        if(type === undefined) {
            bdrs.mobile.Error(message);
        } else {
            bdrs.mobile.Error([type, message].join(" - "));
        }
    }
};

exports._dateToJSON = function(date, timeStr) {
    if(date === null || date === undefined) {
        // intentionally ignoring that the timeStr may not be null. 
        // What does it mean to have a time but no date?
        return null;
    } else {
        var d = new Date(date.getTime());
        if(timeStr !== null && timeStr !== undefined) {
            var timeArray = timeStr.split(bdrs.mobile.TIME_DELIMITER);
            if(timeArray.length > 0) {
                d.setHours(timeArray[0]);
            }
            if(timeArray.length > 1) {
                d.setMinutes(timeArray[1]);
            }
            if(timeArray.length > 2) {
                d.setSeconds(timeArray[2]);
            }
        }
        
        return d.getTime();
    }
};

exports.getLastSyncTime = function() {
    if(bdrs.mobile.syncService._lastSync === null) {
        var lastSynchronizedRecord;
        waitfor(lastSynchronizedRecord) {
            Record.all().filter('uploadedAt','!=',null).order('uploadedAt', false).one(resume);
        }
        bdrs.mobile.syncService._lastSync = lastSynchronizedRecord === null ? new Date(0) : lastSynchronizedRecord.uploadedAt();
    } 
    return bdrs.mobile.syncService._lastSync;
};

/** 
 * Adds a synchronization listener to this service.
 * @param callback the listener to be added.
 */
exports.addSyncListener = function(callback) {
    bdrs.mobile.syncService._syncListeners.push(callback);
};

/**
 * Removes a synchronization listener from this service.
 * @param callback the listener to be removed.
 * @return true if the listener was removed successfully, false otherwise.
 */
exports.removeSyncListener = function(callback) {
    for(var i=0; i<bdrs.mobile.syncService._syncListeners.length; i++) {
        var listener = bdrs.mobile.syncService._syncListeners[i];
        if(callback === listener) {
            bdrs.mobile.syncService._syncListeners.splice(i,1);
            return true;
        }
    }
    
    return false;
};

exports._fireSyncEvent = function(event) {
    var copy = bdrs.mobile.syncService._syncListeners.slice();
    for(var i=0; i<copy.length; i++) {
        copy[i](event);
    }
};
