<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>
<%@page import="au.com.gaiaresources.bdrs.controller.embedded.EmbeddedFeature"%>

<h1>Embedded Widget</h1>
<cw:getContent key="user/widgetBuilder" />

<h2>Setup</h2>
<p>
    After making your selection, copy and paste the embed code below. 
    The code changes based on your selection.
</p>
<textarea id="embed_src" class="fillwidth"></textarea>

<table class="form_table">
    <tbody>
        <tr>
            <th>Widget</th>
            <td>
                <select id="feature" name="feature">
                    <c:forEach items="<%= EmbeddedFeature.values() %>" var="feature">
                        <jsp:useBean id="feature" type="au.com.gaiaresources.bdrs.controller.embedded.EmbeddedFeature"/>
                        <option value="<c:out value="<%= feature.toString() %>"/>">   
                            <c:out value="<%= feature.getName() %>" />
                        </option>
                    </c:forEach>
                </select>
            </td>
        </tr>
		<tr class="gallerySection customWidgetSection">
			<th>Gallery</th>
			<td>
				<select name="galleryId" class="widgetParameter">
					<c:forEach items="${galleryList}" var="gallery">
						<option value="${gallery.id}">${gallery.name}</option>
					</c:forEach>
				</select>
			</td>
        <tr>
        <tr class="gallerySection customWidgetSection">
        	<th>Image Width</th>
            <td><input type="text" value="250" name="slideshowWidth" class="validate(positiveInteger) widgetParameter"/></td>
        </tr>
        <tr class="gallerySection customWidgetSection">
            <th>Image Height</th>
            <td><input type="text" value="154" name="slideshowHeight" class="validate(positiveInteger) widgetParameter"/></td>
        </tr>
            <th>Widget Width</th>
            <td><input type="text" value="300" name="width" class="validate(positiveInteger) widgetParameter"/></td>
        </tr>
        <tr>
            <th>Widget Height</th>
            <td><input type="text" value="250" name="height" class="validate(positiveInteger) widgetParameter" /></td>
        </tr>
        <tr>
            <th>Show Footer</th>
            <td><input type="checkbox" value="true" checked="checked" class="widgetParameter" /></td>
        </tr>
        <tr>
            <th>Background Color</th>
            <td><input class="css validate(required)" type="text" name="backgroundColor" value="#FFFFFF" /></td>
        </tr>
        <tr>
            <th>Header Color</th>
            <td><input class="css validate(required)" type="text" name="headerColor" value="#F69900" /></td>
        </tr>
        <tr>
            <th>Font Size</th>
            <td><input class="css validate(positiveInteger)" type="text" name="fontSize" value="12"/> in pixels</td>
        </tr>
        <tr>
            <th>Text Color</th>
            <td><input class="css validate(required)" type="text" name="textColor" value="#3F3F3F"/></td>
        </tr>

    </tbody>
</table>

<h2>Preview</h2>
<div class="hr"/>
<div id="preview"></div>

<script type="text/javascript">
    jQuery(function() {
		var widgetMap = {
			"LATEST_STATISTICS": "",
			"IMAGE_SLIDESHOW": ".gallerySection"
		};
		
		var widgetBuilderArgs = {
            widgetMap: widgetMap,
            featureSelector: "#feature",
            widgetParamSelector: ".widgetParameter",
            customSectionSelector: ".customWidgetSection",
            cssStylesSelector: ".css",
			embedSrcSelector: "#embed_src",
			previewSelector: "#preview",
			domain: "${ domain }",
			port: "${ port }"
        };
		
		bdrs.embed.widgetBuilder.init(widgetBuilderArgs)
		
        jQuery("#feature").change();
    });
</script>