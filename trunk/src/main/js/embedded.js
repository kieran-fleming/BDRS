"use strict";

bdrs.embed = {};
bdrs.embed.widgetBuilder = {
    
    init: function(featureSelector, widthSelector, heightSelector, footerSelector, cssStylesSelector,
        embedSrcSelector, previewSelector,
        domain, port, servlet) {
    
        // Attach listeners to the inputs
        var featureElem = jQuery(featureSelector);
        var widthElem = jQuery(widthSelector);
        var heightElem = jQuery(heightSelector);
        var footerElem = jQuery(footerSelector);
        var cssStyles = jQuery(cssStylesSelector);
        
        var embedSrcElem = jQuery(embedSrcSelector);
        
        var data = {
            featureSelector: featureSelector,
            widthSelector: widthSelector,
            heightSelector: heightSelector,
            footerSelector: footerSelector,
            cssStylesSelector: cssStylesSelector,
            
            previewSelector: previewSelector,
            embedSrcSelector: embedSrcSelector,
            
            domain: domain,
            port: port,
            servlet: servlet
        };
        featureElem.bind("change", data, bdrs.embed.widgetBuilder.updatePreview);
        widthElem.bind("change", data, bdrs.embed.widgetBuilder.updatePreview);
        heightElem.bind("change", data, bdrs.embed.widgetBuilder.updatePreview);
        footerElem.bind("change", data, bdrs.embed.widgetBuilder.updatePreview);
        cssStyles.bind("change", data, bdrs.embed.widgetBuilder.updatePreview);

        embedSrcElem.click(function(event) {
            event.target.focus();
            event.target.select();
        });
    },
    
    updatePreview: function(event) {
        var data = event.data;
        
        var featureElem = jQuery(data.featureSelector);
        var widthElem = jQuery(data.widthSelector);
        var heightElem = jQuery(data.heightSelector);
        var footerElem = jQuery(data.footerSelector);
        var previewElem = jQuery(data.previewSelector);
        var embedSrcElem = jQuery(data.embedSrcSelector);
        var cssStyles = jQuery(data.cssStylesSelector);
        
        // Validate width and height
        if(isNaN(parseInt(widthElem.val(), 10))) {
            widthElem.val(300);
        }
        if(isNaN(parseInt(heightElem.val(), 10))) {
            heightElem.val(250);
        }

        var embedParams = {
            domain: data.domain,
            port: data.port,
            contextPath: bdrs.contextPath,
            width: widthElem.val(),
            height: heightElem.val(),
            feature: featureElem.val(),
        };
        if(footerElem.length > 0) {
            embedParams.showFooter = footerElem.val();
        }
        var style;
        for(var i=0; i<cssStyles.length; i++) {
            style = jQuery(cssStyles[i]);
            embedParams[style.attr("name")] = style.val();
        }
        
        var embedScript = jQuery("<script></script>");
        embedScript.attr({
            "id" : embedParams.targetId,
            "type": "text/javascript",
            "src": bdrs.contextPath+"/bdrs/public/embedded/bdrs-embed.js?"+jQuery.param(embedParams)
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
