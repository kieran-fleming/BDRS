// Function which takes bounding box information as input along with min/max zoom
// which populates srcArray in the format Z/X/Y which can then be used either as an
// index in the local storage or as an address for image loading from TMS
function PopulateLocal(minLat, minLon, maxLat, maxLon, bottomZoom, topZoom) {
	var pLeftTop = new Point();
	var pRightBottom = new Point();
	var zoom;
	var x;
	var y;
	srcArray = new Array()
	
	//Need to clean up storage while space is limited
	localStorage.clear()
	
	//Function calculates the number of tiles which require loading
	//	Update: Array is populated in order to preload the images so that they
	//			will be ready after the timeout for conversion to Base64
	var i = 0;
	for (zoom=bottomZoom;zoom<=topZoom;zoom++) {
        pLeftTop = CalcTileXYZ(minLat, minLon, zoom)
        pRightBottom = CalcTileXYZ(maxLat, maxLon, zoom)
        for (x=pLeftTop.x;x<=pRightBottom.x;x++) {
            for (y=pRightBottom.y;y<=pLeftTop.y;y++) {
            	srcArray[i]= (zoom + '/' + x + '/' + y)
            	var image = img;
				image.src = 'http://192.168.2.208/TMS/NEW/' + zoom + '/' + x + '/' + y + '.png';
				i++;
			}
        }
    }
	
	// Currently local storage will not be able to hold more than approx 400 tiles
	if (i > 400) {
		alert("Local Storage will not support that many tiles, please select a smaller area")
	} else {
		//alert("Saving image data to local storage, Please wait...");
		setTimeout('saveArray(srcArray)', 2000);
	}
}

//Takes the array populated by PopulateLocal and saves the images it can find on TMS to local storage
function saveArray(srcArray) {
	var j = 0;
	var counter = 0;
	for (counter=0;counter<srcArray.length;counter++) {
		var image = img;
		image.src = 'http://192.168.2.208/TMS/NEW/' + srcArray[counter] + '.png';
			
		var item = getBase64Image(image);
		if (item != 'data:,') {
			localStorage.setItem(srcArray[counter], item);
		} else {
			j++;
		}
	}
	alert("Finished saving image data, you may now reload the page offline");
	//alert(j + " images failed to load");
	//if (j > 5) {
	//	saveArray(srcArray);
	//}
}

//Function used to calculate the TMS address of a tile given lat/lon/zoom input
function CalcTileXYZ(lat, lon, zoom) {
	var Tile = new Point(lat, lon, zoom);
    var x = ((lon + 180) / 360) * (Math.pow(2,zoom));
    var y = (1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * (Math.pow(2,zoom));
    Tile.x = Math.floor(x);
    Tile.y = Math.floor(y);
    Tile.z = zoom;

    return Tile;
}

function Point(x,y,z) {
  this.x = x;
  this.y = y;
  this.z = z;
}


//Taken from http://stackoverflow.com/questions/934012/get-image-data-in-javascript
//Function takes an image as input and returns a base64 data url of the image
function getBase64Image(img) {
    // Create an empty canvas element
    var canvas = document.createElement("canvas");
    canvas.width = img.width;
    canvas.height = img.height;

    // Copy the image contents to the canvas
    var ctx = canvas.getContext("2d");
    ctx.drawImage(img, 0, 0);
    // Get the data-URL formatted image
    // Firefox supports PNG and JPEG. You could check img.src to guess the
    // original format, but be aware that using "image/jpg" will re-encode the image.
    var dataURL = canvas.toDataURL("image/png");

    return dataURL;
}