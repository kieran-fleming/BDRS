CACHE MANIFEST
<%@ page contentType="text/cache-manifest; charset=UTF-8" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tiles:useAttribute id="customJS" name="customJavaScript" classname="java.util.List"/>
<tiles:useAttribute id="customCSS" name="customCss" classname="java.util.List"/>
#VERSION  <c:out value="${manifestVersion}"/> 
    
CACHE:

# CSS Files
../../css/bdrs/mobile/base.css
../../css/vader/jquery-ui-1.8.9.custom.css
../../css/ketchup/jquery.ketchup.css

../../css/bdrs/mobile/netbook.css
../../css/bdrs/mobile/iphone.css
../../css/bdrs/mobile/android.css
../../js/mobile/lightbox/styles/jquery.lightbox.min.css
<c:if test="${customCSS != null}">
	<c:forEach var="cssFile" items="${customCSS}">  
		{pageContext.request.contextPath}/css/${cssFile}"
	</c:forEach>
</c:if>

# JavaScript Files
../../js/mobile/jquery-1.5.min.js
../../js/mobile/jquery.address-1.3.2.min.js
../../js/mobile/jquery-ui-1.8.9.custom.min.js
../../js/mobile/bdrs.js
../../js/ketchup/jquery.ketchup.js
../../js/ketchup/jquery.ketchup.messages.js
../../js/ketchup/jquery.ketchup.validations.basic.js
../../js/mobile/bdrs-mobile.js
#../../js/mobile/bdrs-mobile-micro-templates.js
#../../js/mobile/lightbox/scripts/jquery.color.min.js
../../js/mobile/lightbox/scripts/jquery.lightbox.min.js
<c:if test="${customJS != null}">
	<c:forEach var="jsFile" items="${customJS}">
		${pageContext.request.contextPath}/js/${jsFile}"
	</c:forEach>
</c:if>

#Images
../../css/vader/images/ui-bg_flat_0_aaaaaa_40x100.png
../../css/vader/images/ui-bg_glass_95_fef1ec_1x400.png
../../css/vader/images/ui-bg_gloss-wave_16_121212_500x100.png
../../css/vader/images/ui-bg_highlight-hard_15_888888_1x100.png
../../css/vader/images/ui-bg_highlight-hard_55_555555_1x100.png
../../css/vader/images/ui-bg_highlight-soft_35_adadad_1x100.png
../../css/vader/images/ui-bg_highlight-soft_60_dddddd_1x100.png
../../css/vader/images/ui-bg_inset-soft_15_121212_1x100.png
../../css/vader/images/ui-icons_666666_256x240.png
../../css/vader/images/ui-icons_aaaaaa_256x240.png
../../css/vader/images/ui-icons_bbbbbb_256x240.png
../../css/vader/images/ui-icons_c98000_256x240.png
../../css/vader/images/ui-icons_cccccc_256x240.png
../../css/vader/images/ui-icons_cd0a0a_256x240.png
../../css/vader/images/ui-icons_f29a00_256x240.png


../../images/bdrs/mobile/logo_80x80.png
../../images/bdrs/mobile/sheet_40x40.png
../../images/bdrs/mobile/40x40_Home_NoBorder.png
../../images/bdrs/mobile/40x40_Back_1.png
../../images/bdrs/mobile/40x40_Survey_NoBorder.png
../../images/bdrs/mobile/40x40_Help_NoBorder.png
../../images/bdrs/mobile/40x40_About_NoBorder.png
../../images/bdrs/mobile/40x40_Contact_NoBorder.png
../../images/bdrs/mobile/40x40_SelectAll_Grey.png
../../images/bdrs/mobile/40x40_Add_Grey.png
../../images/bdrs/mobile/40x40_Delete_Grey.png
../../images/bdrs/mobile/40x40_FieldGuide_Grey.png
../../images/bdrs/mobile/40x40_HelpID_Grey_MagGlass.png
../../images/bdrs/mobile/taxongroup/group_Birds_ref.png
../../images/bdrs/mobile/species/350/360738.jpg
../../images/bdrs/mobile/species/350/361502.jpg

<tiles:insertAttribute name="customContent"/>

FALLBACK:

NETWORK:
webservice/user/ping.htm
*