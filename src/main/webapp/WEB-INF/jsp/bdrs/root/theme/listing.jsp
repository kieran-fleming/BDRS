<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<h1>Theme Setup</h1>
<cw:getContent key="root/theme/listing" />

<table class="datatable textcenter">
    <thead>
        <tr>
            <th>Name</th>
            <th>Is Active</th>
            <th>Download</th>
            <th>Edit</th>
        </tr>
    </thead> 
    <tbody>
        <c:forEach var="theme" items="${ themeList }">
            <tr>
                <td>
                    <c:out value="${ theme.name }"/>
                </td>
                <td>
                    <input type="radio" name="active" onclick="blur();" onchange="window.document.location='${pageContext.request.contextPath}/bdrs/admin/theme/refreshTheme.htm?themeId=${ theme.id }'"
                        <c:if test="${ theme.active }">
                            checked="checked"
                        </c:if>
                    />
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/admin/theme/downloadTheme.htm?themeId=${ theme.id }">
                        Download
                    </a>
                </td>
                <td>
                    <c:if test="${ not theme.default }">
                        <a href="${pageContext.request.contextPath}/bdrs/<c:if test="${editAsRoot}">root</c:if><c:if test="${editAsAdmin}">admin</c:if>/theme/edit.htm?themeId=${ theme.id }&portalId=${ portalId }">
                            Edit
                        </a>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
<div class="buttonpanel textright">
    <input class="form_action" type="button" value="Add Theme" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/<c:if test="${editAsRoot}">root</c:if><c:if test="${editAsAdmin}">admin</c:if>/theme/edit.htm?portalId=${ portalId }'"/>
</div>