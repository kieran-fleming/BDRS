/**
 * Event handlers for the login page. 
 */
exports.Create = function() {
    jQuery(".bdrs-page-species-count-list #species-count-list-add-button").click(function() {
        jQuery.mobile.changePage("#species-count-edit", jQuery.mobile.defaultPageTransition, false, true);
    });
};
    
exports.Show = function() {
	
	var survey = bdrs.mobile.survey.getDefault();
    var speciesCountList;
    waitfor(speciesCountList) {
        SpeciesCount.all().filter('survey','=',survey.id).prefetch('species').order('userCount', false).list(resume);
    }

    // Populate the list view
    var listView;
    var listViewTmplParams = {
        dataInset:true
    };
    waitfor(listView) {
        bdrs.template.renderOnlyCallback('listView', listViewTmplParams, resume);
    };

    for(var i=0; i<speciesCountList.length; i++) {
        var speciesCount = speciesCountList[i];
        var listViewItemTmplParams = {
            linkId: speciesCount.id,
            link: "javascript: void(0);",
            header: speciesCount.species().scientificName(),
            description: speciesCount.species().commonName(),
            count: speciesCount.userCount(),
            aside: 'Sort Priority'
        };
        waitfor(listViewItem) {
            bdrs.template.renderOnlyCallback('listViewItemWithCountAside', listViewItemTmplParams, resume);
        };


        listViewItem.find("#"+speciesCount.id).click(function(event) {
            bdrs.mobile.setParameter(bdrs.mobile.pages.species_count_edit.SPECIES_COUNT_ID_KEY, 
                                     jQuery(event.currentTarget).attr('id'));
            jQuery.mobile.changePage("#species-count-edit", jQuery.mobile.defaultPageTransition, false, true);
        });
        listViewItem.appendTo(listView);
    }

    // Append the list view to the DOM
    var listViewContainer = jQuery(".bdrs-page-species-count-list #species-count-list-listview");
    listViewContainer.append(listView);
    listView.listview();
};
    
exports.Hide = function() {
    jQuery(".bdrs-page-species-count-list #species-count-list-listview").empty();
};
