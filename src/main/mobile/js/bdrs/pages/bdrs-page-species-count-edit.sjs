exports.SPECIES_COUNT_ID_KEY = 'speciesCountId';
exports.ADD_TASK_NAME = "Add";
exports.EDIT_TASK_NAME = "Edit";


exports.Create = function() {
    // Add click handler to the save button
    jQuery("#species-count-edit-save-button").click(function() {
        bdrs.mobile.pages.species_count_edit._saveSpeciesCount(
                bdrs.mobile.form.inputsToMap("#species-count-edit"), false);
        jQuery.mobile.changePage("#species-count-list", jQuery.mobile.defaultPageTransition, false, true);
    });

    jQuery("#species-count-edit-reset-button").click(function() {
        bdrs.mobile.pages.species_count_edit._saveSpeciesCount(
                bdrs.mobile.form.inputsToMap("#species-count-edit"), true);
        jQuery.mobile.changePage("#species-count-list", jQuery.mobile.defaultPageTransition, false, true);
    });
};
    
exports.Show = function() {
    var speciesCountId = bdrs.mobile.getParameter(bdrs.mobile.pages.species_count_edit.SPECIES_COUNT_ID_KEY);
    var resetButtonDisplayElem = jQuery("#species-count-edit-reset-button").parents(".ui-btn");

    var speciesCount;
    var taskName;
    // Initialise depending if we are adding or editing a species count
    if(speciesCountId === undefined || speciesCountId === null) {
        // Adding
        taskName = "Add";
        speciesCount = new SpeciesCount();
        speciesCount.count(0);
        speciesCount.userCount(0);
        resetButtonDisplayElem.hide();
    } else {
        // Editing
        taskName = "Edit";
        waitfor(speciesCount) {
            SpeciesCount.all().filter('id', '=', speciesCountId).prefetch('species').one(resume);
        }
        resetButtonDisplayElem.show();
    }
    bdrs.mobile.pages.species_count_edit._setTaskName(taskName);

    // Populate the user count
    var userCountElem = jQuery("#speciescount-usercount");
    userCountElem.val(speciesCount.userCount());

    // Populate the species (if there is one)
    var speciesElem = jQuery("#speciescount-species");
    if(speciesCount.species() !== undefined && speciesCount.species() !== null) {
        speciesElem.val(speciesCount.species().scientificName());
    }
    speciesElem.autocomplete({
        source: function(request, response) {
            var species;
            waitfor(species) {
                Species.all().filter('scientificName','like','%' + request.term + '%').or(new persistence.PropertyFilter('commonName','like','%' + request.term + '%')).limit(5).list(resume); // @todo add some survey awareness.
            }
            var names = [];
            for (var i = 0; i < species.length; i++) {
                names.push({ label : species[i].commonName() + ' - <i>' + species[i].scientificName()+ '</i>', value : species[i].scientificName()});
            }
            response(names);
        },
        change: function(event, ui) {
            jQuery.mobile.pageLoading(false);
            
            var scientificName = jQuery('#speciescount-species').val();
            var taxon;
            waitfor(taxon) {
                Species.all().filter('scientificName', '=', scientificName).one(resume);
            }

            jQuery.mobile.pageLoading(true);
        },
        html: true
    });
};
    
exports.Hide = function() {
    bdrs.mobile.removeParameter(bdrs.mobile.pages.species_count_edit.SPECIES_COUNT_ID_KEY);
    bdrs.mobile.pages.species_count_edit._setTaskName(null);
    jQuery("#speciescount-usercount").val('');
    jQuery("#speciescount-species").val('');
    jQuery("#species-count-edit-reset-button").parents(".ui-btn").hide();
};

exports._setTaskName = function(taskName) {
    if(taskName === null || taskName === undefined) {
        taskName = "";
    }

    jQuery(".species-count-edit-taskname").text(taskName);
};

exports._saveSpeciesCount = function(inputMap, reset) {

    var speciesCountId = bdrs.mobile.getParameter(bdrs.mobile.pages.species_count_edit.SPECIES_COUNT_ID_KEY);

    // Retrieve the existing species or create a new field species
    var scientificName = inputMap.get('speciescount-species');
    var species;
    waitfor(species) {
        Species.all().filter('scientificName', '=', scientificName).prefetch('taxonGroup').one(resume);
    }

    bdrs.mobile.Debug('Scientific Name for Species Count: ' + scientificName);
    if(species === undefined || species === null) {
        // Create a field species.
        var species = new Species({
            scientificNameAndAuthor: "Field Species",
            scientificName: scientificName,
            commonName: scientificName,
            rank: "Field Species",
            author: "Field Species",
            year: ""
        });
        
        // Check for field taxon group
        var taxonGroup;
        waitfor(taxonGroup) {
            TaxonGroup.all().filter('name', '=', 'Field Species').one(resume);
        }
        if (taxonGroup === null) {
            taxonGroup = new TaxonGroup({
                name: "Field Species"
            });
            persistence.add(taxonGroup);
        }
        persistence.add(species);
        taxonGroup.species().add(species);
        waitfor() {
            persistence.flush(resume);
        }
        bdrs.mobile.Debug('Created a new species: ' + species.commonName());
    }

    // Get or create the Species Count
    var speciesCount;
    if(speciesCountId === undefined || speciesCountId === null) {
        speciesCount = new SpeciesCount();
        speciesCount.count(0);
        speciesCount.userCount(0);
    } else {
        waitfor(speciesCount) {
            SpeciesCount.all().filter('id', '=', speciesCountId).prefetch('species').one(resume);
        }
    }

    // Update the values of the Species Count and save
    var userCount = reset ? speciesCount.count() : inputMap.get('speciescount-usercount');
    speciesCount.userCount(userCount);
    speciesCount.species(species);
    speciesCount.scientificName(species.scientificName());
    persistence.add(species);

    waitfor() {
        persistence.flush(resume);
    }
};