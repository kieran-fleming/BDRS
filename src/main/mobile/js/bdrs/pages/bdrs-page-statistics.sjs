exports.Create = function() {
	
}
	
exports.Show = function() {
    new bdrs.mobile.widget.RecordStatistics().appendTo("#statistics-record");
}

exports.Hide = function() {
	jQuery('#statistics-record').empty();
}
