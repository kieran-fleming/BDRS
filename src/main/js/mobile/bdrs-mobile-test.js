function viewSource(){
	alert("going to view source");
	var a=window.open("about:blank").document;
	a.write("<!DOCTYPE html><html><head><title>Source of "+location.href+'</title><meta name="viewport" content="width=device-width" /></head><body></body></html>');
	a.close();var b=a.body.appendChild(a.createElement("pre"));
	b.style.overflow="auto";b.style.whiteSpace="pre-wrap";
	b.appendChild(a.createTextNode(document.documentElement.innerHTML));
}

/**
 * 
 * @return
 */
function deleteRecordsSync(){
	var idsMap = {};
	//createConnection();
	GR159DB.transaction(function (transaction) {
		//get local records that need to be deleted
        transaction.executeSql("SELECT r.id, r.online_recordid from record r WHERE status = 'delete';", [], function (transaction, results) {
        	var recordsLength = results.rows.length;
			for (var i=0; i<recordsLength; i++){
				var rec = results.rows.item(i);
				idsMap[rec.id]=rec.online_recordid;
			}
			//delete records online
			var regkey = getCookie("regkey");
			jQuery.post(contextPath+"/webservice/record/deleteRecords.htm", {
	            JSONrecords: JSON.stringify(idsMap),
	            ident: regkey
	        }, function (response) {
		        for(var localId in idsMap){
		        	var map= {};
		        	map.recordId = localId;
		        	deleteRecord_webSQL(map);
		        }
	        }, 'json');
        },errorHandler); 
	});
}

/**
 * 
 * @param canvas
 * @param context
 * @return
 */
function clearCanvas(canvas, context) {
  context.clearRect(0, 0, canvas.width, canvas.height);
  var w = canvas.width;
  canvas.width = 1;
  canvas.width = w;
}
/**
 * Sets the current server connection status as an icon in the statusbar
 * @param drawingCanvas
 * @param status Values can be conn (connected to server), ncon (no connection with server), sync (syncing db to server)
 */
function serverStatus(status){
	var drawingCanvas = document.getElementById('connectionStatusIcon');
	clearCanvas(drawingCanvas, drawingCanvas.getContext('2d'));
	var fillColor = "";
	var strokeColor = "";
	
	if(status != null && status != "" && drawingCanvas.getContext){
		switch(status){
			case "conn":
				fillColor = "#3fa74e"; //green
				strokeColor = "#3fa74e"; //green
				break;
			case "ncon":
				fillColor = "#d9531e"; //red
				strokeColor = "#d9531e"; //red
				break;
			case "sync":
				fillColor = "#009abb"; //blue
				strokeColor = "#009abb"; //blue
				break;
			default:
				clearCanvas(drawingCanvas, drawingCanvas.getContext('2d'));
		}
		var context = drawingCanvas.getContext('2d');
		context.strokeStyle = strokeColor;//red #d9531e, green #3fa74e
		context.fillStyle = fillColor;
		context.beginPath();
		context.arc(17/2,17/2,17/2,0,Math.PI*2,true);
		context.closePath();
		context.stroke();
		context.fill();
	}
}

/**
 * 
 * @param from Id of container that needs to be hidden
 * @param to Id of container that needs to be shown
 * @param direction Either left or right
 */
function slidePage(view){
	var divid = new Date().getTime();
	jQuery("div#refContainer > div").hide("slide",{"direction":"left"},750);
	jQuery("#refContainer").append("<div id='"+divid+"'></div>");
	jQuery("#"+divid).hide().append(view).show("slide",{"direction":"right"},750);
	jQuery("div#refContainer > div").remove();
}

