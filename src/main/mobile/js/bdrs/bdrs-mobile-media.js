if (!window.bdrs) {
	bdrs = {};
}
if (!bdrs.mobile) {
	bdrs.mobile = {};
}

//Audio player
var my_media = null;
var mediaTimer = null;

bdrs.mobile.media = {
		
	playAudio : function(src){
			var my_media = new Media(src,
			        // success callback
			        function() {
			            bdrs.mobile.Debug("playAudio():Audio Success");
			        },
			        // error callback
			        function(err) {
			        	bdrs.mobile.Debug("playAudio():Audio Error: "+err);
			    });

		    // Play audio
		    my_media.play();

		    // Update my_media position every second
		    if (mediaTimer == null) {
		        mediaTimer = setInterval(function() {
		            // get my_media position
		            my_media.getCurrentPosition(
		                // success callback
		                function(position) {
		                    if (position > -1) {
		                        setAudioPosition((position/1000) + " sec");
		                    }
		                },
		                // error callback
		                function(e) {
		                	bdrs.mobile.Debug("Error getting pos=" + e);
		                    setAudioPosition("Error: " + e);
		                }
		            );
		        }, 1000);
		    }
		},
		
		test : function(msg){
						console.log("the message is = " + msg)
					}
		
};

