/* ***********************************************************************************

	CJ Simple Slideshow jQuery Plugin
	Written by: Doug Jones (www.cjboco.com)
	
	Copyright (c) 2011, Creative Juices Bo. Co. All rights reserved.
	
	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:
	
	a) Redistributions of source code must retain the above copyright
	   notice, this list of conditions and the following disclaimer.
	  
	b) Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution. 
	  
	c) Neither the name of the Creative Juices, Bo. Co. nor the names of its
	   contributors may be used to endorse or promote products derived from
	   this software without specific prior written permission.
	
	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
	"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
	LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
	A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
	OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
	LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
	OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	
	Version History
	
	3.0 	(01-08-2011) - Complete rewrite of the plugin structure. 
				Added better link detection.
				Using jQuery to handle dissolves now (Dissolve amount
				is now tied in directly with fadeIn/fadeOut).
				Added pause options.
	2.1.1 	(06-27-2009) - Fixed the IE bugs.
	2.1 	(06-24-2009) - Stripped out a lot of the style features.
				It really didn't make sense to do it here, since it
				could be done easily with CSS. Plus is was causing
				issues with IE.
	2.0 	(06-14-2009) - Converted it to a JQuery plug-in.
	1.0 	(10-02-2006) - Initial release.

*********************************************************************************** */
(function ($) {
	$.fn.extend({

		cjSimpleSlideShow: function (opts) {

			var methods = {

				// sets the slide and handle the dissolve
				setSlide: function ($obj) {
					var data = $obj.data('cj'),
						o = data.options,
						s = data.sys,
						$show = $obj.find(".cj_slideshow_slide"),
						$slide = $show.eq(s.current),
						$slideNext = $show.eq(s.current + 1).length > 0 ? $show.eq(s.current + 1) : $show.eq(0);
					if (s.inited && s.started) {
						$slide.stop().fadeOut(o.dissolve);
						$slideNext.stop().fadeIn(o.dissolve, function () {
							s.current = $show.eq(s.current + 1).length > 0 ? s.current + 1 : 0;
							if (!s.paused) {
								s.timer = window.setTimeout(function () {
									methods.setSlide($obj);
								}, o.delay);
							}
						});
					}
				},

				// pauses the slideshow
				pause: function ($obj) {
					var data = $obj.data('cj'),
						s = data.sys;
					if (s.started) {
						if (s.timer) {
							window.clearTimeout(s.timer);
							s.timer = null;
						}
						$obj.find(".cj_slideshow_pause").stop().fadeIn("fast");
						s.paused = true;
					}
				},

				// resumes the slideshow
				resume: function ($obj) {
					var data = $obj.data('cj'),
						o = data.options,
						s = data.sys;
					if (s.started) {
						$obj.find(".cj_slideshow_pause").stop().fadeOut("fast");
						s.timer = window.setTimeout(function () {
							methods.setSlide($obj);
						}, o.delay);
						s.paused = false;
					}
				},

				// starts the slideshow
				start: function ($obj) {
					var data = $obj.data('cj'),
						o = data.options,
						s = data.sys;
					if (s.inited) {
						s.timer = window.setTimeout(function () {
							methods.setSlide($obj);
						}, o.delay);
						s.started = true;
					}
				}

			};

			// handle calls to our methods
			if (typeof opts === "string" && methods[opts]) {

				var $obj = $(this),
					data = $obj.data('cj'),
					s = data.sys;

				// accessible methods
				if (s.inited) {
					return methods[opts]($obj);
				}

			// call to initialize
			} else if (typeof opts === "object" || !opts) {

				// plugin main
				return this.each(function () {

					var $obj = $(this),
						data = $obj.data('cj'),
						o, s;

					// set our options (if not set)
					if (!data) {
						$obj.data('cj', {
							sys: {
								version: '3.0',
								timer: null,
								current: 0,
								paused: false,
								inited: false,
								started: false
							},
							options: {
								autoRun: true,
								delay: 5000,
								dissolve: 500,
								showCaptions: false,
								centerImage: false,
								allowPause: false,
								pauseText: "Paused"
							}
						});
						data = $obj.data('cj');
					}

					// make sure we aren't already inited and started
					if (!data.sys.inited && !data.sys.started) {

						// user defined options
						if (opts) {
							data.options = $.extend(data.options, opts);
						}
						// simplify our data variables
						o = data.options;
						s = data.sys;
						// the dissolve can't be greater than the delay.
						if (typeof o.dissolve === "number" && typeof o.delay === "number" && o.dissolve > o.delay) {
							o.dissolve = o.delay;
						}
						// init: do a check of the slide count, no slideshows of 1!
						if ($obj.find("img").length > 1) {
							var $wrap = $("<div>").css({
								"position": "absolute",
								"display": "block",
								"width": $obj.width() + "px",
								"height": $obj.height() + "px",
								"overflow": "hidden",
								"cursor": "pointer"
							}).addClass("cj_slideshow_wrapper");
							// find all the IMG tags (plus A tags)
							$obj.find("img").each(function (a, b) {
								var $img = $(b),
									$slide = $('<div>').css({
										"position": "absolute",
										"top": "0px",
										"left": "0px",
										"display": a > 0 ? "none" : "block",
										"width": $obj.width() + "px",
										"height": $obj.height() + "px"
									}).addClass("cj_slideshow_slide");
								// handle any links wrapping out image
								if ($img.parent().get(0).nodeName === "A") {
									var url = $img.parent().attr("href");
									$slide.bind("click", function () {
										document.location.href = url;
										return false;
									});
								}
								// set up pause?
								if (o.allowPause) {
									$slide.bind("mouseenter", function () {
										methods.pause($obj);
									}).bind("mouseleave", function () {
										methods.resume($obj);
									});
								}
								// center our image?
								if (o.centerImage) {
									$img.css({
										"position": "absolute",
										"top": o.centerImage ? parseInt(($obj.height() - $img.height()) / 2, 10) + "px" : "0px",
										"left": o.centerImage ? parseInt(($obj.width() - $img.width()) / 2, 10) + "px" : "0px"
									});
								}
								// apppend the image to our slide
								$slide.append($img);
								// do we need to show captions?
								if (o.showCaptions && $img.attr("alt").length > 0) {
									var $caption = $("<span>").css({
										"position": "absolute",
										"width": "100%",
										"height": "auto",
										"z-index": "5"
									}).addClass("cj_slideshow_caption").html($img.attr("alt"));
									$slide.append($caption);
								}
								// add the slide to the wrapper
								$wrap.append($slide);
							});
							// prepare the elemnt to show the slides
							$obj.html("").append($wrap);
							// do we need to add the pause?
							if (o.allowPause && o.pauseText.length > 0) {
								var $pause = $("<div>").css({
									"position": "absolute",
									"top": "5px",
									"left": "5px",
									"display": "none",
									"z-index": "10"
								}).addClass("cj_slideshow_pause").html(o.pauseText);
								$obj.append($pause);
							}
							s.inited = true;
						} else {
							s.inited = false;
						}
						// autostart if option set and we are inited
						if (s.inited && o.autoRun) {
							methods.start($obj);
						}

					}
				});

			// unknown call to our plugin
			} else {
			
				$.error('Method ' + opts + ' does not exist on jQuery.cjSimpleSlideShow');

			}
		}
	});
}(jQuery));