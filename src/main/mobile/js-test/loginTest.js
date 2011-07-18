describe("testLogin", function() {
    it("logs us in", function() {
		var work = {
			done: false
		};
		
		runs(function() {
			jQuery("div[data-role*='page']").live('pageshow', function(event, ui) {
		          work.done = true;
		    });
	        jQuery("#url").val("http://localhost:8080/BDRS");
			jQuery("#username").val("user");
			jQuery("#password").val("password");
			jQuery("#login_button").click();
		});
		
		waitsFor(function() {	
            return work.done; 
		});
		
		runs(function() {
			expect(jQuery('.ui-page-active').attr('id')).toEqual("dashboard");
		});
    });
});