exports.Init = function() {
	bdrs.mobile.Debug("About Init");
	bdrs.template.renderCallback('about', {}, '#templatedContent', function(){
		
	});
};

exports.BeforeShow = function() {
	bdrs.mobile.Debug("About BeforeShow");
};

exports.Show = function() {
	bdrs.mobile.Debug("About Show");
};

exports.Hide = function() {
	bdrs.mobile.Debug("About Hide");
};