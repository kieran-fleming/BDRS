jQuery(function(){
//	jQuery('.messages').append("<p class='message'></p>");
/*	testajax();
	testwebsql();
	testhashevent();*/
});

var functestresultmsg = null;

function testajax(){
	jQuery.get('http://www.google.com', function(){
		jQuery('.message').append("ajax compatineushoorn1506" +
				"ble (loginmobile.js)<br>");
		console.log('ajax=true');
	});
}

function testwebsql(){
	if (window.openDatabase) {
		 jQuery('.message').append("websql compatible (offlinerequest.jsp)<br>");
		 console.log('websql=true');
		}else{
			jQuery('.message').append("websql is not supported by your browser (offlinerequest.jsp)<br>");
			console.log('websql=false');
			}
}

function testhashevent(){
	jQuery(window).bind( 'hashchange', function(e) {
		 jQuery('.message').append("hashevent compatible(offlinerequest.jsp)<br>");
		  console.log('hashevent=true');
	});
	var date=new Date();
	window.location.hash =date.getMilliseconds();
}

