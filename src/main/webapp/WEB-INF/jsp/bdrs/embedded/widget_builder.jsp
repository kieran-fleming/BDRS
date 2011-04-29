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
                <select id="feature">
                    <c:forEach items="<%= EmbeddedFeature.values() %>" var="feature">
                        <jsp:useBean id="feature" type="au.com.gaiaresources.bdrs.controller.embedded.EmbeddedFeature"/>
                        <option value="<c:out value="<%= feature.toString() %>"/>">   
                            <c:out value="<%= feature.getName() %>" />
                        </option>
                    </c:forEach>
                </select>
            </td>
        </tr>
        <tr>
            <th>Width</th>
            <td><input id="width" type="text" value="300" class="validate(positiveInteger)"/></td>
        </tr>
        <tr>
            <th>Height</th>
            <td><input id="height" type="text" value="250"/></td>
        </tr>
        <tr>
            <th>Show Footer</th>
            <td><input id="footer" type="checkbox" value="true" checked="checked"/></td>
        </tr>
        <tr>
            <th>Background Color</th>
            <td><input class="css validate(required)" type="text" name="backgroundColor" value="#FFFFFF"/></td>
        </tr>
        <tr>
            <th>Header Color</th>
            <td><input class="css validate(required)" type="text" name="headerColor" value="#F69900"/></td>
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
        bdrs.embed.widgetBuilder.init("#feature", "#width", "#height", "#footer:checked", ".css", "#embed_src", "#preview", "${ domain }", "${ port }");
        jQuery("#feature").change();
    });
</script>