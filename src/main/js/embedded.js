"use strict";

bdrs.embed = {};
bdrs.embed.widgetBuilder = {
	// widget map maps a widget feature value to a selector which we use to
	// expose the special params for a particular widget
	init: function(args) {
		// You should always set the args or the embedded widget stuff won't work.
		// This is not a functional set of defaults, it's just an example.
		if (!args) {
			args = {
				widgetMap: {},
				featureSelector: "#feature",
				widgetParamSelector: ".widgetParameter",
				customSectionSelector: ".customWidgetSection",
				cssStylesSelector: ".css"
			};
		};
		
		this.widgetMap = args.widgetMap;
		this.featureSelector = args.featureSelector;
		this.widgetParamSelector = args.widgetParamSelector;
		this.customSectionSelector = args.customSectionSelector;
		this.cssStylesSelector = args.cssStylesSelector;
		this.embedSrcSelector = args.embedSrcSelector;
		this.previewSelector = args.previewSelector;
		this.domain = args.domain;
		this.port = args.port;
		
		jQuery(this.featureSelector).bind("change", null, jQuery.proxy(bdrs.embed.widgetBuilder.changeFeature, this));
		jQuery(this.widgetParamSelector).bind("change", null, jQuery.proxy(bdrs.embed.widgetBuilder.updatePreview, this));
        jQuery(this.cssStylesSelector).bind("change", null, jQuery.proxy(bdrs.embed.widgetBuilder.updatePreview, this));
		
		jQuery(this.embedSrcSelector).click(function(event) {
            event.target.focus();
            event.target.select();
        });
	},
	
	changeFeature: function(event) {
		var featureElem = jQuery(this.featureSelector);
		// 1. hide all custom sections.
		// 2. show required sections as defined in the widget map.
		jQuery(this.customSectionSelector).hide();
		var feature = featureElem.val();
		jQuery(this.widgetMap[feature]).show();
		
		this.updatePreview(event);
	},
	
	updatePreview: function(event) {
		var featureElem = jQuery(this.featureSelector);
        var paramElem = jQuery(this.widgetParamSelector);
        var previewElem = jQuery(this.previewSelector);
        var embedSrcElem = jQuery(this.embedSrcSelector);
        var cssStyles = jQuery(this.cssStylesSelector);
        var widgetParams = jQuery(this.widgetParamSelector);
		
        // Validate width and height
		var embedParams = {
            domain: this.domain,
            port: this.port,
            contextPath: bdrs.contextPath,
            feature: featureElem.val(),
        };
		
		for (var i=0; i<widgetParams.length; ++i) {
			var param = jQuery(widgetParams[i]);
			embedParams[param.attr("name")] = param.val();
		}
		
        for(var i=0; i<cssStyles.length; i++) {
            var style = jQuery(cssStyles[i]);
            embedParams[style.attr("name")] = style.val();
        }
		
        var embedScript = jQuery("<script></script>");
		var port = window.location.port;
        embedScript.attr({
            "id" : embedParams.targetId,
            "type": "text/javascript",
            "src": document.location.hostname + (port?":"+port:"") + bdrs.contextPath+"/bdrs/public/embedded/bdrs-embed.js?"+jQuery.param(embedParams)
        });
        
        embedSrcElem.text(bdrs.embed.widgetBuilder.elemToString(embedScript[0]));
        
        embedParams.targetId = 'widget_target';
        embedScript.attr("src", bdrs.contextPath+"/bdrs/public/embedded/bdrs-embed.js?"+jQuery.param(embedParams));
        
        var targetSpan = jQuery("<span></span>");
        targetSpan.attr({"id":  embedParams.targetId });
        
        previewElem.empty();
        previewElem.append(targetSpan);
        previewElem.append(embedScript);
    },
	
    
    elemToString: function(elem) {
        var buf = [];
        buf.push("<");
        buf.push(elem.tagName);
        for(var i=0; i<elem.attributes.length; i++) {
            var attr = elem.attributes[i];
            buf.push(" ");
            buf.push(attr.name);
            buf.push("=\"");
            buf.push(attr.value);
            buf.push("\"");
        }
        buf.push(">");
        buf.push(elem.innerHTML);
        buf.push("</");
        buf.push(elem.tagName);
        buf.push(">");
        return buf.join("");
    }
};
