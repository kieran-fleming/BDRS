/**
 * Sanity Checks
 */
if (!window.bdrs) {
	bdrs = {};
}
if (!bdrs.mobile) {
	bdrs.mobile = {};
}
if (!bdrs.mobile.pages) {
	bdrs.mobile.pages = {};
}

bdrs.mobile.pages.args = {};
bdrs.mobile.pages.dialog = 0;

/**
 * Wire in the dispatcher when the page is ready. This is the business end where we connect
 * the dispatcher into the 'live' event that is called when pages are transitioned. 
 */
jQuery(function() {
	// Dialog show events
	jQuery("div[data-role*='dialog']").live('pageshow', function(event, ui) {
		bdrs.mobile.pages.Dispatcher.call(this, 'show');
	});

	// Dialog hide events
	jQuery("div[data-role*='dialog']").live('pagehide', function(event, ui) {
		bdrs.mobile.pages.Dispatcher.call(this, 'hide');
	});
	
	// Dialog create events
	jQuery("div[data-role*='dialog']").live('pagecreate', function(event, ui) {
		bdrs.mobile.pages.Dispatcher.call(this, 'create');
	});

	// Page show events
	jQuery("div[data-role*='page']").live('pageshow', function(event, ui) {
		if (bdrs.mobile.pages.dialog > 0) {
			bdrs.mobile.pages.dialog--;
		} else {
			bdrs.mobile.pages.Dispatcher.call(this, 'show');
		}
	});
	
	// Page create events
	jQuery("div[data-role*='page']").live('pagecreate', function(event, ui) {
		// Execute individual page logic
		bdrs.mobile.pages.Dispatcher.call(this, 'create');
	});
	
	// Page hide events
	jQuery("div[data-role*='page']").live('pagehide', function(event, ui) {
		// Execute individual page logic
		if (ui.nextPage.attr('data-role') === 'dialog') {
			// Do something special for dialogs in the future.
			bdrs.mobile.pages.dialog++;
		} else {
			bdrs.mobile.pages.Dispatcher.call(this, 'hide');
		}
	});
});


/**
 * Each content portion needs to have the class 'bdrs-page-blah' where blah is the page name defined
 * in the corresponding javascript file. eg, the login page content section has class
 * bdrs-page-login and this will call object bdrs.mobile.pages.login.Show|Create|Hide()
 */
bdrs.mobile.pages.Dispatcher = function(eventType) {
	var thisPage = this;
	var thisRootContent = jQuery(thisPage).children("div[data-role*='content']");
	var pageTag = thisRootContent[0].className.match(/bdrs\-page\-[a-z\-]+/ig);
	
	if (String.IsNullOrEmpty(pageTag)) {
		bdrs.mobile.Warn("null tag : " + pageTag + ' for event ' + eventType);
		return;
	}
	else {
		pageTag = pageTag[0].toLowerCase().substring('bdrs-page-'.length).replace(/\-/g, "_");
		if (typeof bdrs.mobile.pages[pageTag] == 'object') {
			if (eventType === 'show' && typeof bdrs.mobile.pages[pageTag].Show === 'function') {
				bdrs.mobile.pages[pageTag].Show.call(thisPage);
			} else if (eventType === 'create' && typeof bdrs.mobile.pages[pageTag].Create === 'function') {
				bdrs.mobile.pages[pageTag].Create.call(thisPage);
			} else if (eventType === 'hide' && typeof bdrs.mobile.pages[pageTag].Hide === 'function') {
				bdrs.mobile.pages[pageTag].Hide.call(thisPage);
			}
		}
	}
};
