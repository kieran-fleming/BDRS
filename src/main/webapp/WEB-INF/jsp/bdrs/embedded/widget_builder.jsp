<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>
<%@page import="au.com.gaiaresources.bdrs.controller.embedded.EmbeddedFeature"%>

<h1>Manage Widgets</h1>
<cw:getContent key="user/widgetBuilder" />

<h2>Configure a Widget</h2>
<p>Here you can change the settings for a particular widget, and press the Preview button to see what it would look like.</p>

<div class="input_container">
	<table class="form_table">
	    <tbody>
	        <tr>
	            <th>Widget</th>
	            <td>
	            	Latest Statistics
	            	<input type="hidden" id="feature" value="LATEST_STATISTICS" />
					<%--
					hard code to the latest statistics widget for now...
					
	                <select id="feature" name="feature">
	                    <c:forEach items="<%= EmbeddedFeature.values() %>" var="feature">
	                        <jsp:useBean id="feature" type="au.com.gaiaresources.bdrs.controller.embedded.EmbeddedFeature"/>
	                        <option value="<c:out value="<%= feature.toString() %>"/>">   
	                            <c:out value="<%= feature.getName() %>" />
	                        </option>
	                    </c:forEach>
	                </select>
					--%>
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
	<div class="textright">
		<input type="button" id="previewButton" class="form_action" value="Preview" />
	</div>
</div>

<h2>Preview</h2>
<div id="preview"></div>

<h2>Code</h2>
<p>
    The code in the box below can be copied and pasted into other BDRS pages or web sites.
</p>
<textarea id="embed_src" class="fillwidth"></textarea>

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
			port: "${ port }",
			previewButtonSelector: "#previewButton"
        };
		
		bdrs.embed.widgetBuilder.init(widgetBuilderArgs)
		
        jQuery("#feature").change();
    });
</script>