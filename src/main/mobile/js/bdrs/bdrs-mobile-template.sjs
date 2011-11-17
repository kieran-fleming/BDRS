bdrs.template = {};
exports.cache = {};

/**
 * Checks that a template is in the database and is newer or current, otherwise overwrites or creates it. 
 */
exports.checkOrAddTemplate = function(templateName, templateText, templateVersion) {
    waitfor() {
        Template.findBy('name', templateName, function(template) {
            if (template === null) {
                var t = new Template({ name : templateName, template : templateText, version : templateVersion });
                persistence.add(t);
            } else { // version checks here...
                if (template.version() < templateVersion) {
                    template.template(templateText);
                    template.version(templateVersion);
                    persistence.flush();
                }
            }
            resume();
        });
    }
}

/**
 * Renders a template and attaches it to a the given selector 
 * @param {} templateName the name of the template to render 
 * @param {} model the Model map to pass to the template
 * @param selector the jQuery selector to use to find a place to append the template
 */
exports.render = function (templateName, model, selector) {
    if (jQuery.template[templateName] === undefined) {
        Template.findBy('name', templateName , function(template) {
            jQuery.template(templateName, (jQuery(template).data('template')));
            jQuery.tmpl(templateName, model).appendTo(selector);
        });
    } else {
        jQuery.tmpl(templateName, model).appendTo(selector);    
    }
};

/**
 * Renders a template and attaches it to a the given selector and call a callback when done
 * @param {} templateName the name of the template to render 
 * @param {} model the Model map to pass to the template
 * @param selector the jQuery selector to use to find a place to append the template
 * @param callback a callback to call when the rendering is complete.
 */
exports.renderCallback = function (templateName, model, selector, callback) {
    if (jQuery.template[templateName] == undefined) {
        Template.findBy('name', templateName , function(template) {
                jQuery.template(templateName, (jQuery(template).data('template')));
                jQuery.tmpl(templateName, model).appendTo(selector);
                callback();
            });
    } else {
        jQuery.tmpl(templateName, model).appendTo(selector);    
            callback();
    }
};

/**
 * Renders a template and attaches it to a the given selector and releases a queue lock upon completion 
 * @param {} templateName the name of the template to render 
 * @param {} model the Model map to pass to the template
 * @param selector the jQuery selector to use to find a place to append the template
 * @param queue the queue to release a lock on when complete.
 */
exports.renderSync = function (templateName, model, selector, queue) {
    if (jQuery.template[templateName] == undefined) {
        Template.findBy('name', templateName , function(template) {
                jQuery.template(templateName, (jQuery(template).data('template')));
                jQuery.tmpl(templateName, model).appendTo(selector);
                queue.next();
            });
    } else {
        jQuery.tmpl(templateName, model).appendTo(selector);    
        queue.next();
    }
};

/**
 * Renders a template and returns the DOM fragment to the callback method.
 */
exports.renderOnlyCallback = function(templateName, model, callback) {
    if (jQuery.template[templateName] == undefined) {
        Template.findBy('name', templateName , function(template) {
            jQuery.template(templateName, (jQuery(template).data('template')));
            callback(jQuery.tmpl(templateName, model));
        });
    } else {
        callback(jQuery.tmpl(templateName, model)); 
    }
};

exports.initTemplates = function() {
    bdrs.mobile.Debug('Initialising Templates');
    /**
     * Define the Template store. 
     */
    window.Template = persistence.define('Template', {
            name: "TEXT",
            template: "TEXT",
            version: "INT"
        });
        
    Template.index('name');

    waitfor() {
        persistence.schemaSync(resume);
    }

    /**
     * Here are the actual templates... 
     */
    exports.checkOrAddTemplate('footer', '<h4>Footer</h4>', 0);
    exports.checkOrAddTemplate('header', '<a href="#dashboard" class="ui-btn-right" data-icon="home" data-iconpos="notext" data-direction="reverse">Home</a>', 1);
    exports.checkOrAddTemplate('dashboard-status', '<h4>Current Survey : <a href="#choose-survey" data-transition="slidedown">${name}</a></h4>', 0);
    exports.checkOrAddTemplate('surveyListItem', '<li survey-id="${id}" class="survey-${id} downloadSurvey"><h3>${name}</h3><p>${description} : ${id}</p></li>', 0);
    exports.checkOrAddTemplate('textListItem', '<li class="${id}"><h3>${title}</h3><p>${description}</p></li>', 1);
    exports.checkOrAddTemplate('audioListItem', '<li class="${id}"><h3>${title}</h3><p><a href="#" class="btn large" onclick="bdrs.mobile.media.playAudio(\'/android_asset/www/audio/360440.mp3\');">Play Audio</a></p></li>', 3);
    
    exports.checkOrAddTemplate('profileImageListItem', '<li class="${id}"><div><img src="data:${type};base64,${imageData}"/></div><h3>title</h3><p>description</p></li>', 6);
    exports.checkOrAddTemplate('recordReviewItem', '<li><a href="javascript: void(0);"><h3>${title}</h3><p>${description}</p></a><span class="ui-li-count">${count}</span><span class="ui-li-aside record-aside">${aside}</span></li>', 1);
    exports.checkOrAddTemplate('surveyListDownloadItem', '<input type="checkbox" survey-id="${id}" name="checkbox-${id}" id="checkbox-${id}" class="downloadSurvey {{if local}}local{{/if}}" /><label for="checkbox-${id}" survey-id="${id}">${name}</label>', 1);
    
    exports.checkOrAddTemplate('siteListItem', '<li class="site-${id}"><h3>${title}</h3><p>${description}</p></li>', 0);
    exports.checkOrAddTemplate('methodListItem', '<li id="${id}" class="method-${id}"><h3>${title}</h3><p>${description}</p></li>', 0);
    exports.checkOrAddTemplate('censusTypeCheckbox', '<input type="radio" name="site-type" id="site-type-${id}" value="${name}"/><label for="site-type-${id}">${name}</label>', 0);
    exports.checkOrAddTemplate('recordSpeciesInput', '<div class="speciesEntryField" data-role="fieldcontain"><label for="record-species">Species</label><input class="validate {{if required}}required{{/if}}" type="text" name="record-species" id="record-species"/></div>', 4);
    //exports.checkOrAddTemplate('recordSpeciesInput', '<div class="speciesEntryField" data-role="fieldcontain"><label for="record-species">Species</label><input class="validate required" type="text" name="record-species" id="record-species"/></div>', 3);
    exports.checkOrAddTemplate('recordNumberSlider', '<div data-role="fieldcontain" class="numberSlider"><label for="record-number" id="record-number-label">Number</label><input class="validate {{if required}}required{{/if}}" type="number" data-type="range" name="record-number" id="record-number" value="1" min="0" max="50"></div>', 6);
    
    exports.checkOrAddTemplate('attributeValueIR', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate integerRange {{if required}}required{{/if}} " type="text" name="record-attr-${id}" id="record-attr-${id}" value="${value}" range="${minmax}"/></div>', 0);
    exports.checkOrAddTemplate('attributeValueIN', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate integer {{if required}}required{{/if}} " type="text" name="record-attr-${id}" id="record-attr-${id}" value="${value}"/></div>', 0);
    exports.checkOrAddTemplate('attributeValueDE', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate decimal {{if required}}required{{/if}} " type="text" name="record-attr-${id}" id="record-attr-${id}" value="${value}"/></div>', 0);
    exports.checkOrAddTemplate('attributeValueDA', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate date {{if required}}required{{/if}} " type="text" name="record-attr-${id}" id="record-attr-${id}" value="${value}"/></div>', 0);
    exports.checkOrAddTemplate('attributeValueST', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate {{if required}}required{{/if}}" type="text" name="record-attr-${id}" id="record-attr-${id}" value="${value}"/></div>', 0);
    exports.checkOrAddTemplate('attributeValueSA', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate {{if required}}required{{/if}}" type="text" name="record-attr-${id}" id="record-attr-${id}" value="${value}"/></div>', 0);
    exports.checkOrAddTemplate('attributeValueTA', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><textarea class="validate {{if required}}required{{/if}}" name="record-attr-${id}" id="record-attr-${id}">${value}</textarea></div>', 0);
    exports.checkOrAddTemplate('attributeValueBC-btn', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate regExp {{if required}}required{{/if}} " type="text" name="record-attr-${id}" id="record-attr-${id}" value="${value}" regexp="${regExp}"/><button id="record-btn-${id}" data-inline="true">Scan</button></div>', 0);
    exports.checkOrAddTemplate('attributeValueBC', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate regExp {{if required}}required{{/if}} " type="text" name="record-attr-${id}" id="record-attr-${id}" value="${value}" regexp="${regExp}"/></div>', 0);
    exports.checkOrAddTemplate('attributeValueRE-btn', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate regExp {{if required}}required{{/if}} " type="text" name="record-attr-${id}" id="record-attr-${id}" value="${value}" regexp="${regExp}"/></div>', 0);
    exports.checkOrAddTemplate('attributeValueRE', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate regExp {{if required}}required{{/if}} " type="text" name="record-attr-${id}" id="record-attr-${id}" value="${value}" regexp="${regExp}"/></div>', 0);
    exports.checkOrAddTemplate('attributeValueSV', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><select class="validate {{if required}}required{{/if}}" name="record-attr-${id}" id="record-attr-${id}" data-native-menu="false">{{html options}}</select></div>', 0);
    exports.checkOrAddTemplate('attributeValueSV-option', '<option value="${value}" {{if selected}}selected="selected"{{/if}}>${text}</option>', 0);
    exports.checkOrAddTemplate('attributeValueFI', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate {{if required}}required{{/if}}" type="file" name="record-attr-${id}" id="record-attr-${id}" value="${value}"/></div>', 0);
    exports.checkOrAddTemplate('attributeValueTM', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate {{if required}}required{{/if}}" type="text" name="record-attr-${id}" id="record-attr-${id}" value="${value}"/></div>', 0);
    exports.checkOrAddTemplate('attributeValueSC', '<div class="attributeValue" data-role="fieldcontain"><fieldset data-role="controlgroup"><legend>${description}{{if required}}&nbsp;*{{/if}}</legend><input type="checkbox" name="record-attr-${id}" id="record-attr-${id}" value="true" {{if value}}checked="checked"{{/if}} /><label for="record-attr-${id}">${description}</label></fieldset></div>', 0);
    
    exports.checkOrAddTemplate('attributeValueMC', '<div class="attributeValue" data-role="fieldcontain"><fieldset data-role="controlgroup"><legend>${description}{{if required}}&nbsp;*{{/if}}</legend>{{html options}}</fieldset></div>', 0);
    exports.checkOrAddTemplate('attributeValueMC-option', '<input type="checkbox" name="record-attr-${id}-${index}" id="record-attr-${id}-${index}" class="validate {{if required}}multiCheckboxRequired{{/if}}" value="${optname}" {{if checked}}checked="checked"{{/if}} /><label for="record-attr-${id}-${index}">${optname}</label>', 0);
    
    // HTML attributes
    exports.checkOrAddTemplate('attributeValueHL', '<div class="attributeValue" data-role="fieldcontain"><p>{{html description}}</p></div>', 0);
    exports.checkOrAddTemplate('attributeValueCM', '<div class="attributeValue" data-role="fieldcontain"><center><p><b>{{html description}}</b></p></center></div>', 0);
    exports.checkOrAddTemplate('attributeValueHR', '<div class="attributeValue" data-role="fieldcontain"><hr/></div>', 0);
    
    // Multi select attributes have been disabled (and replaced with multi checkboxes)
    // because the current implementation in jquery mobile is buggy.
    // 1) The dialog does not close consistently in 1.0b1
    // 2) The selection of the first item does not update the display 1.0a4 and 1.0b1
    // exports.checkOrAddTemplate('attributeValueMS', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}" class="select">${description}{{if required}}&nbsp;*{{/if}}</label><select name="record-attr-${id}" id="record-attr-${id}" multiple="multiple" data-native-menu="false" tabindex="-1">{{html options}}</select></div>', 0);
    // exports.checkOrAddTemplate('attributeValueMS-option', '<option value="${value}" {{if selected}}selected="selected"{{/if}}>${text}</option>', 0);
    
    exports.checkOrAddTemplate('attributeValueMS', '<div class="attributeValue" data-role="fieldcontain"><fieldset data-role="controlgroup"><legend>${description}{{if required}}&nbsp;*{{/if}}</legend>{{html options}}</fieldset></div>', 0);
    exports.checkOrAddTemplate('attributeValueMS-option', '<input type="checkbox" name="record-attr-${id}-${index}" id="record-attr-${id}-${index}" class="validate {{if required}}multiCheckboxRequired{{/if}}" value="${optname}" {{if checked}}checked="checked"{{/if}} /><label for="record-attr-${id}-${index}">${optname}</label>', 0);
    
    exports.checkOrAddTemplate('attributeValueIM-file', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><input class="validate {{if required}}required{{/if}}" type="file" name="record-attr-${id}" id="record-attr-${id}" value="${value}"/></div>', 0);
    exports.checkOrAddTemplate('attributeValueIM-camera', '<div class="attributeValue" data-role="fieldcontain"><label for="record-attr-${id}">${description}{{if required}}&nbsp;*{{/if}}</label><button class="takePicture" onClick="bdrs.mobile.attribute.takePicture(\'${id}\')">Take Picture</button></div><div id="record-attr-${id}">${value}</div>', 0);
    
    exports.checkOrAddTemplate('validationError', "<div class='validation-error-popup ui-loader ui-overlay-shadow ui-body-e ui-corner-all' style='display:block; opacity:0.96; top:${top}px;'><h1>${header}</h1><p>${message}</p></div>");
    
    exports.checkOrAddTemplate('recentTaxaWidget', '<div class="recent-taxa-widget" data-role="fieldcontain"><label class="ui-input-text" for="recent-taxon-fieldset">Recent Taxonomy</label><fieldset id="recent-taxon-fieldset" data-role="controlgroup" data-type="horizontal" data-role="fieldcontain">{{html radios}}</fieldset></div>', 1);
    exports.checkOrAddTemplate('recentTaxaWidget-radio', '<input type="radio" name="radio-choice-recent-taxa" id="radio-choice-${id}" value="${value}"/><label for="radio-choice-${id}">${displayName}</label>', 0);
    
    exports.checkOrAddTemplate('recordPointIntersect-substrate-radio', '<input type="radio" class="validate required" name="radio-choice-pi-substrate" id="radio-choice-${id}" value="${value}" {{if checked}}checked="checked"{{/if}}/><label for="radio-choice-${id}">${displayName}</label>', 0);
    exports.checkOrAddTemplate('recordPointIntersect-taxonomy-btn', '<button class="pi-taxonomy-btn" data-inline="true" type="button" id="${id}" value="${value}">${displayName}</button>', 0);
    exports.checkOrAddTemplate('recordPointIntersect-species', '<label for="pi-taxonomy-species-${id}" style="display:none">Taxonomy</label><input class="validate {{if required}}required{{/if}}" type="text" name="pi-taxonomy-species" id="pi-taxonomy-species-${id}" value="${value}"/>', 0);
    exports.checkOrAddTemplate('recordPointIntersect-height', '<input type="number" name="pi-taxonomy-height" id="pi-taxonomy-height-${id}" class="validate decimal" value="${value}"/>', 0);
    exports.checkOrAddTemplate('recordPointIntersect-delete','<a id="${id}" href="javascript:void(0);" data-role="button" data-icon="delete" data-iconpos="notext" data-theme="b">Delete Observation</a>',0);
    exports.checkOrAddTemplate('three-col-content', '<div id="block-a-${id}" class="ui-block-a"></div><div id="block-b-${id}" class="ui-block-b"></div><div id="block-c-${id}" class="ui-block-c"></div>', 0);
    
    exports.checkOrAddTemplate('statisticsTwoColLayout','<fieldset class="ui-grid-a"><div class="ui-block-a">{{html leftContent}}</div><div class="ui-block-b">{{html rightContent}}</div></fieldset>', 0);
    exports.checkOrAddTemplate('statisticsSpanTarget', '<span id="${id}">${content}</span>', 0);
    exports.checkOrAddTemplate('statisticsPieChartColorCell', '<td style="background-color:#${color};width:1em;"></td>', 0);
    
    exports.checkOrAddTemplate('horizontalDatatable', '{{if title}}<h5>${title}</h5>{{/if}}<table id="${id}" class="horizontal-datatable">{{html rows}}</table>', 0);
    exports.checkOrAddTemplate('horizontalDatatableRow', '<tr><td class="header">${headerContent}</td><td class="data">${dataContent}</td></tr>', 0);
    
    exports.checkOrAddTemplate('collapsibleBlock', '<div data-role="collapsible" data-collapsed="{{if collapsed}}true{{else}}false{{/if}}"><h1>${title}</h1>{{if description}}<p>${description}</p>{{/if}}</div>', 0);
    exports.checkOrAddTemplate('inlineButton', '<input type="button" data-inline="true" id="${id}" value="${text}"/>',0);
    
    exports.checkOrAddTemplate('listView', '<ul data-role="listview" data-inset="{{if dataInset}}true{{else}}false{{/if}}"></ul>', 0);
    exports.checkOrAddTemplate('listViewItem','<li><a id="${linkId}" href="${link}"><h3>${header}</h3><p>${description}</p></li>', 0);
    exports.checkOrAddTemplate('listViewItemWithCountAside', '<li><a id="${linkId}" href="${link}"><h3>${header}</h3><p>${description}</p><span class="ui-li-count">${count}</span><span class="ui-li-aside record-aside">${aside}</span></li>', 0);
    
    exports.checkOrAddTemplate('clientSyncContainer', '<div id="${containerId}" style="display:none;"><iframe width="50" height="50"><html><body></body></html></iframe></div>', 0);
    exports.checkOrAddTemplate('clientSyncContent', '<form action="${url}" method="POST"><input type="hidden" name="ident" value="${ident}"/><input type="hidden" name="syncData" value="${syncData}"/></form>', 0);
    
    exports.checkOrAddTemplate('checkBoxItem', '<input type="checkbox" name="${name}" id="${id}"/><label for="${id}">{{html description}}</label>',0);
    
    waitfor() {
        persistence.flush(resume);
    }
    bdrs.mobile.Debug('Templates Initialised');
};

if (bdrs.database.ready === true) {
    exports.initTemplates();
} else {
    bdrs.database.addDatabaseReadyListener(exports.initTemplates);
}

/**
    var movies = [
        { Name: "The Red Violin", ReleaseYear: "1998" },
        { Name: "Eyes Wide Shut", ReleaseYear: "1999" },
        { Name: "The Inheritance", ReleaseYear: "1976" }
    ];

    // Render the template with the movies data and insert
    // the rendered HTML under the "movieList" element
    exports.render('movieTemplate', movies, '#movieList');
*/
