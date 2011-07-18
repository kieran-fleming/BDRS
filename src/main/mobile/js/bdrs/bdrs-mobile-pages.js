/** 
 * This class sets up Persistence jQuery Mobile integration.
 * I've switched it off for now, but it may prove useful when we get to subsamples.
 *    
var Page = persistence.define('Page', {
	        path: "TEXT",
	        data: "TEXT",
      	});
      
var Image = persistence.define('Image', {
	        path: "TEXT",
	        data: "TEXT",
      	});
      	
persistence.schemaSync();
   	
var t = new Page({ path : 'page2.html', data : '<div data-role="page" id="page2">\
      														<div data-role="header">\
      															<h1>Page2</h1>\
      														</div>\
      														<div data-role="content">\
      															<h2>Page2</h2>\
      														</div>\
      														<div data-role="footer">\
      															<h4>Page Footer</h4>\
      														</div>\
      													</div>' });
persistence.add(t);
persistence.flush();
*/