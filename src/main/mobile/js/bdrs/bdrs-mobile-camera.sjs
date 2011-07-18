
// Returns result with properties:
// success: bool, true if picture returned, false otherwise.
// data:    The picture as a base64 encoded string unless otherwise indicated in the cameraOptions arg. Is null if an error occured.
//          Note that if you ask for the image to be stored on the phone and a URI returned, the URI will be in this property of the
//          result map.
// message: A string with the error message. null if the operation was successful.
// 
// tested on HTC desire, android 2.2

exports.getPicture = function(cameraOptions) {
    if (!cameraOptions) {
        cameraOptions = { quality: 25 };
    };
    waitfor(result) {
        navigator.camera.getPicture(
            //success
            function(cameraResult) {
                var result = {};
                result.success = true;
                result.data = cameraResult;
                result.message = null;
                resume(result);
            },
            // failure
            function(cameraResult) {
                var result = {};
                result.success = false;
                result.message = cameraResult;
                result.data = null;
                resume(result);
            },
            cameraOptions
        );
    }
    return result;
};