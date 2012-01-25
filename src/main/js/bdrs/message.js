// Display message on browser in same style as JSP approach
bdrs.message = {
    set: function(msg) {
        this.clear();
        this.append(msg);
    },
    append: function(msg) {
		// force scroll to the top so we can see the displayed messages
        jQuery(window).scrollTop(0);
        this.getDom().append('<p class="message">'+msg+'</p>');
    },
    clear: function() {
        this.getDom().empty();
    },
    getDom: function() {
        return jQuery(".messages");
    },
	// to be used with $.ajax() for the error property
	getAjaxErrorFunc: function(msg) {
		return function(jqXhr, textStatus, errorThrown) {
			bdrs.message.set(msg + ". Failed with: '" + textStatus + "', Error thrown was: " + errorThrown);
		};
	}
};