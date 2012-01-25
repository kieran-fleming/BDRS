
exports.Init = function() {
    jQuery('#trash-select-all').click(exports._select_all);
    jQuery('#trash-select-none').click(exports._select_none);
    jQuery('#trash-select-toggle').click(exports._select_toggle);

    jQuery('#trash-delete').click(exports._delete);
    jQuery('#trash-restore').click(exports._restore);
};
    
exports.Show = function() {
    var fieldset = exports._get_fieldset();

    var records;
    waitfor(records) {
        Record.all().filter('deleted','=',true).prefetch('species').prefetch('censusMethod').order('when', false).list(resume);
    };

    var content = jQuery('<span></span>');
    for(var i=0; i<records.length; i++) {
        
        var record = records[i];
        var descriptor = bdrs.mobile.record.util.getDescriptor(record);
        
        var tmplParams = {
            id: record.id,
            name: record.id,
            description: descriptor.title + "<br/>" + descriptor.description
        }
        var cbItem;
        waitfor(cbItem) {
            bdrs.template.renderOnlyCallback('checkBoxItem', tmplParams, resume);
        }
        content.append(cbItem);
    }

    fieldset.append(content);
    
     bdrs.template.restyle(fieldset.parent());
};

exports.Hide = function() {
    exports._get_fieldset().empty();
};

exports._get_fieldset = function() {
    return jQuery('#trash-fieldset');
};

exports._get_checkboxes = function() {
    return jQuery('#trash-fieldset input[type=checkbox]');
};

/**
 * Retrieves all selected records.
 */
exports._get_checked_records = function() {
    
    var recordIds = []
    var checked = exports._get_checkboxes().filter(":checked");
    for(var i=0; i<checked.length; i++) {
        recordIds.push(jQuery(checked[i]).attr('id'));
    }
   
    var records;
    waitfor(records) {
        var query_collection = Record.all();
        query_collection = query_collection.filter('deleted','=', true)
        query_collection = query_collection.filter('id', 'in', recordIds)
        query_collection = query_collection.prefetch('parent');
        query_collection = query_collection.prefetch('species');
        query_collection = query_collection.prefetch('censusMethod');
        query_collection.list(resume);
    }

    return records;
};

/**
 * Checks all checkboxes on the trash page.
 */
exports._select_all = function() {
    exports._get_checkboxes().prop('checked', function( i, val ) {
        return true;
    });
     bdrs.template.restyle(exports._get_fieldset());
};

/**
 * Unchecks all checkboxes on the trash page.
 */
exports._select_none = function() {
    exports._get_checkboxes().prop('checked', function( i, val ) {
        return false;
    });
     bdrs.template.restyle(exports._get_fieldset());
};

/**
 * Toggles the check state of all checkboxes on the trash page.
 */
exports._select_toggle = function() {
    var checkboxes = exports._get_checkboxes().prop('checked', function( i, val ) {
        return !val;
    });
     bdrs.template.restyle(exports._get_fieldset());
};

/**
 * Permanently deletes all selected records.
 */
exports._delete = function() {
    jQuery.mobile.changePage("#trash-delete-confirm", {showLoadMsg: false, transition: "slidedown"});
};

/**
 * Recursively deletes the specified record its attribute values and child records.
 * 
 * @param record the record to be deleted.
 */
exports._recurse_delete_record = function(record) {

    // Delete the children first
    var children;
    waitfor(children) {
        record.children().prefetch("censusMethod").prefetch("species").list(resume);
    }
    for(var i=0; i<children.length; i++) {
        exports._recurse_delete_record(children[i]);
    }

    // Delete the attribute values
    var attributeValues;
    waitfor(attributeValues) {
        AttributeValue.all().filter('record', '=', record).list(resume);
    }

    for(var j=0; j<attributeValues.length; j++) {
        var attributeValue = attributeValues[j];
        bdrs.mobile.Debug("Permanently Deleting Attribute Value: " + attributeValue.id);
        persistence.remove(attributeValue);
    }

    bdrs.mobile.Debug("Permanently Deleting Record: " + record.id);
    persistence.remove(record);
};

/**
 * Recursively deletes all records and their children on the trash page.
 */
exports.delete_records_confirmed = function() {

    bdrs.mobile.Debug("Delete Confirmed");
    jQuery.mobile.showPageLoadingMsg();

    var selected_records = exports._get_checked_records();

    for(var i=0; i<selected_records.length; i++) {
        exports._recurse_delete_record(selected_records[i]);
    }

    persistence.flush();

    jQuery.mobile.hidePageLoadingMsg();
    jQuery.mobile.changePage("#review", {showLoadMsg: false});
};

/**
 * Recursively validates the specified record (by navigating the parents up to the root).
 * A record is considered to be valid if the record is a root, or all parent records
 * are either marked for restoration or not-deleted.
 *
 * @param record_map a map containing the record id and Record object of all records
 * marked for restoration.
 * @param record the record to be validated for restoration.
 */
exports._recurse_restore_validate = function(record_map, record) {
    
    // If there is a parent and the parent is deleted and not marked for restoration
    var invalid_record = null;
    var parent = record.parent();
    if(parent === null) {
        invalid_record = null;
    } else {
        if(parent.deleted() && record_map[parent.id] === undefined) {
            invalid_record = record;
        } else {
            invalid_record = exports._recurse_restore_validate(record_map, parent);
        }
    }

    return invalid_record;
};

/**
 * Validates, then restores all records that have been checked on the trash page.
 */
exports._restore = function() {
    jQuery.mobile.showPageLoadingMsg();

    // Get all Records
    var selected_records = exports._get_checked_records();
    // { record.id: record}
    var record_map = {};
    for(var i=0; i<selected_records.length; i++) {
        var record = selected_records[i];
        record_map[record.id] = record;
    }
        
    // Validate the selected records for restoration.
    var invalid_record = null;
    jQuery.each(record_map, function(id, record) {
        if(invalid_record === null) {
            invalid_record = exports._recurse_restore_validate(record_map, record);
        }
    });

    bdrs.mobile.Debug("Validating Restoration Set: " + (invalid_record === null));

    var now = new Date();
    // { 
    //     species: { species.id : count of records with that species },
    // 
    //     // the set of records that contributed to the count of records above
    //     record_ids: { record:id : null }
    // }
    // The species attribute is a mapping of the species primary key and the number of 
    // taxonomic records using that species.
    // The record_ids attribute is a map(set) containing the primary keys of the records
    // that have been processed when calculating the record count in the species 
    // attribute map. This set prevents the double counting of records.
    var species_map = {
        species: {},
        record_ids: {}
    };
    if(invalid_record === null) {

        // Validation was successful. 
        // Proceeding with the restoration.
        for(var record_id in record_map) {
            if(record_map.hasOwnProperty(record_id)) {
                var record = record_map[record_id];
                record.deleted(false);
                
                // Update the modification date and populate the species_map
                // with the necessary information to update the SpeciesCount.
                species_map = exports._recurse_restore_children(record, now, species_map);
                bdrs.mobile.Debug("Restoring: " + record.id);
            }
        }

        // Collate the primary keys of the species that have an associated SpeciesCount
        // to be updated.
        var species_id_array = [];
        for(var species_id in species_map.species) {
            if(species_map.species.hasOwnProperty(species_id)) {
                species_id_array.push(species_id);
            }
        }

        // Retrieve the SpeciesCount objects to be updated.
        var species_count_array;
        waitfor(species_count_array) {
            var query = SpeciesCount.all();
            query = query.filter('species', 'in', species_id_array);
            query = query.prefetch('species');
            query.list(resume);
        }

        // Update the SpeciesCount objects.
        for (var c=0; c<species_count_array.length; c++) {
            var species_count = species_count_array[c];
            var increment = species_map.species[species_count.species().id];

            bdrs.mobile.Debug("Incrementing Species Count: " + 
                species_count.id + " : " + species_count.species().scientificName() + " by " + increment);

            species_count.count(species_count.count() + increment);
            species_count.userCount(species_count.userCount() + increment);
        }

        persistence.flush();

        jQuery.mobile.hidePageLoadingMsg();
        jQuery.mobile.changePage("#review", {showLoadMsg: false});
    } else {
        // Something was not valid.
        bdrs.mobile.Debug("Unable to restore "+invalid_record.id+" because the parent has been deleted.");
        jQuery.mobile.hidePageLoadingMsg();

        // Show the error dialog
        bdrs.mobile.setParameter(bdrs.mobile.pages.trash_restore_error.INVALID_RECORD_KEY, invalid_record);
        var trans = jQuery.mobile.defaultPageTransition === 'none' ? 'none' : 'slidedown';
        jQuery.mobile.changePage("#trash-restore-error", {showLoadMsg: false, transition: trans});
    }
};

/**
 * Recursively restore the specified record and all of its children. Restoration of 
 * records involves updating the modification time so that the record state is re-synchronized
 * with the server (in case the record was modified, and then deleted before it was synchronized).
 *
 * If the record is associated with a species, the species_map will be updated to indicate
 * that the SpeciesCount will require updating. The SpeciesCount is not updated in this
 * function.
 *
 * This function does NOT set the deleted flag of the record. 
 *
 * @param record the record to be restored.
 * @param modifiedDate the new modification date of the record to be restored.
 * @param species_map the map containing the species id, count of records that have 
 * been restored that refer to that species, and the set of records that have been 
 * processed by this restoration algorithm (to prevent records being counted multiple times)
 */
exports._recurse_restore_children = function(record, modifiedDate, species_map) {

    record.modifiedAt(modifiedDate);

    // If the species count for this record has not been processed before
    if(species_map.record_ids[record.id] === undefined) {
        var species = record.species();
        if(species !== undefined && species !== null) {
            if(species_map.species[species.id] === undefined) {
                species_map.species[species.id] = 1;
            } else {
                species_map.species[species.id] = species_map.species[species.id] + 1;
            }

            species_map.record_ids[record.id] = null;
        }
    }

    var children;
    waitfor(children) {
        record.children().prefetch('species').list(resume);
    }
    for(var i=0; i<children.length; i++) {
        species_map = exports._recurse_restore_children(children[i], modifiedDate, species_map);
    }

    return species_map;
};