bdrs.menu = {};

bdrs.menu.initHover = function() {
    sfHover = function() {
         var sfEls = document.getElementById("nav").getElementsByTagName("LI");
         for (var i=0; i<sfEls.length; i++) {
            sfEls[i].onmouseover=function() {
                this.className+=" sfhover";
            }
            sfEls[i].onmouseout=function() {
                this.className=this.className.replace(new RegExp(" sfhover\\b"), "");
            }
        }
    }
    if (window.attachEvent) window.attachEvent("onload", sfHover);
};

bdrs.menu.populateMapMenu = function(mapMenuItemSelector) {
    // populate map menu...
    var popMapMenuParams = {};
    jQuery.getJSON(bdrs.contextPath + "/bdrs/public/webservice/getAvailableMaps.htm", popMapMenuParams, function(data) {
        var menu = jQuery(mapMenuItemSelector);
        // Append items since we want to keep the 'my sightings' map at the top
        for(var i=0; i<data.length; ++i) {
            var geoMap = data[i];
            var menuitem = jQuery("<li></li>").attr({"title":geoMap.description});
            var link =  jQuery("<a></a>").attr({
                'href': bdrs.contextPath + "/bdrs/map/view.htm?geoMapId="+geoMap.id
            }).text(geoMap.name);
            menuitem.append(link);
            menu.append(menuitem);
        }
    });
};

bdrs.menu.populateSurveyItems = function(registrationKey, contribMenuSelector) {
    var params = {};
    params.ident = registrationKey;
    // javascript doing some weird closure stuff
    var getMenuRenderFcn = function(surveyMenuItem, surveyId) {
        return function(data) {
            var censusMethodList = jQuery("<ul></ul>");
            for(var j=0; j<data.length; ++j) {
                var censusMethod = data[j];
                var cmMenuItem = jQuery("<li></li>");
                var link =  jQuery("<a></a>").attr({
                    'href': bdrs.contextPath + "/bdrs/user/surveyRenderRedirect.htm?surveyId="+surveyId+"&censusMethodId="+censusMethod.id
                }).text(censusMethod.name);
                cmMenuItem.append(link);
                censusMethodList.append(cmMenuItem);
            }
            surveyMenuItem.append(censusMethodList);
        }
    };
    jQuery.getJSON(bdrs.contextPath + "/webservice/survey/surveysForUser.htm", params, function(data) {
        var menu = jQuery(contribMenuSelector);

        var survey;
        for(var i=data.length-1; i>-1; i--) {
            survey = data[i];
            var menuitem = jQuery("<li></li>").attr({"title":survey.description});
            var link =  jQuery("<a></a>").attr({
                'href': bdrs.contextPath + "/bdrs/user/surveyRenderRedirect.htm?surveyId="+survey.id
            }).text(survey.name);
            menuitem.append(link);
            menu.prepend(menuitem);
            
            // populate census method items...
            jQuery.getJSON(bdrs.contextPath + "/bdrs/user/censusMethod/getSurveyCensusMethods.htm", {surveyId:survey.id}, getMenuRenderFcn(menuitem, survey.id)); 
        }
    });
};