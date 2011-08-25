exports.INVALID_RECORD_KEY = "invalid-record";

exports._RECORD_DESCRIPTOR_SELECTOR = "#trash-restore-error-record-descriptor";
exports._PARENT_RECORD_DESCRIPTOR_SELECTOR = "#trash-restore-error-parent-descriptor";

exports.Create = function() {
    // do nothing
};
    
exports.Show = function() {
    var record = bdrs.mobile.removeParameter(exports.INVALID_RECORD_KEY);

    // Perform prefetching to generate descriptor
    var rec;
    waitfor(rec) {
        var query = Record.all()
        query = query.filter('id', '=', record.id);
        query = query.prefetch('censusMethod');
        query = query.prefetch('species');
        query = query.prefetch('parent')
        query = query.one(resume);
    }

    var parent;
    waitfor(parent) {
        var query = Record.all()
        query = query.filter('id', '=', record.parent().id);
        query = query.prefetch('censusMethod');
        query = query.prefetch('species');
        query = query.prefetch('parent')
        query = query.one(resume);
    }

    var descriptor;

    var recordDescriptor = bdrs.mobile.record.util.getDescriptor(rec);
    descriptor = [recordDescriptor.title, recordDescriptor.description].join('<br/>');
    jQuery(exports._RECORD_DESCRIPTOR_SELECTOR).append(descriptor);

    var parentRecordDescriptor = bdrs.mobile.record.util.getDescriptor(parent);
    descriptor = [parentRecordDescriptor.title, parentRecordDescriptor.description].join('<br/>');
    jQuery(exports._PARENT_RECORD_DESCRIPTOR_SELECTOR).append(descriptor);
};

exports.Hide = function() {
    jQuery(exports._RECORD_DESCRIPTOR_SELECTOR).empty();
    jQuery(exports._PARENT_RECORD_DESCRIPTOR_SELECTOR).empty();
};
