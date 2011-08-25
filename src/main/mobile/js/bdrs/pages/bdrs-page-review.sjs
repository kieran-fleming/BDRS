/**
 * Event handlers for the record page. 
 */
exports.Create =  function() {
}

exports.recordSyncListener = function() {
    bdrs.mobile.pages.review.Hide();
    bdrs.mobile.pages.review.Show();
}
	
exports.Show = function() {

    bdrs.mobile.syncService.addSyncListener(bdrs.mobile.pages.review.recordSyncListener);
    
    var recordListElem = jQuery(".bdrs-page-review .recordList");
	
	var records;
    waitfor(records) {
	    Record.all().filter('deleted','=',false).prefetch('species').prefetch('censusMethod').filter('parent', '=', null).order('when', false).list(resume);
    };
    
    var descriptor;
    var rec;
    var count;
    var aside;
    var tmplParams;
    
    var recordReviewItem;
    var recordReviewItemAnchor;
    
    var isModified;
        
    var now = new Date();
    
    for(var i=0; i<records.length; i++) {
        rec = records[i];
        
        waitfor(count) {
            rec.children().count(resume);                        
        };
        
        isModified = (rec.uploadedAt() === null) || 
            (rec.modifiedAt().getTime() > rec.uploadedAt().getTime());
            
        aside = [
            isModified ? "Modified" : "Synched",
            "Last Changed: "+bdrs.mobile.getDaysBetweenAsFormattedString(rec.modifiedAt(), now)
        ].join("&nbsp;|&nbsp;");
        
        descriptor = bdrs.mobile.record.util.getDescriptor(rec);
        tmplParams = {
            title: descriptor.title,
            description: descriptor.description,
            count: count,
            aside: aside
        };
        
        waitfor(recordReviewItem) {
            bdrs.template.renderOnlyCallback('recordReviewItem', tmplParams, resume);        
        };
        
        recordReviewItem.appendTo(recordListElem);

        var anchor = recordReviewItem.find("a");
        anchor.jqmData('recordId', rec.id);
        anchor.click(function(event) {
            bdrs.mobile.setParameter("selected-record", jQuery(event.currentTarget).jqmData("recordId"));
            jQuery.mobile.changePage("#record");
        });
        
    }

    recordListElem.listview('refresh');

    // Populate the number of items in the trash can
    var trashCount;
    waitfor(trashCount) {
        Record.all().filter('deleted','=',true).count(resume);
    }
    jQuery("#review-trash-count").text(trashCount);

    // Pluralize the subject of the trash can
    var subject = trashCount === 1 ? "Record" : "Records";
    jQuery("#review-trash-subject").text(subject);
}

exports.createRecordReviewItem = function(rec) {
    waitfor(recordReviewItem) {
            bdrs.template.renderOnlyCallback('recordReviewItem', tmplParams, resume);        
        };
        
        recordReviewItem.find("a").jqmData('recordId', rec.id).click(function(event) {
            bdrs.mobile.setParameter("selected-record", jQuery(event.currentTarget).jqmData("recordId"));
        });
};
	
exports.Hide = function() {
	jQuery('.bdrs-page-review .recordList').empty();
	bdrs.mobile.syncService.removeSyncListener(bdrs.mobile.pages.review.recordSyncListener);
}
