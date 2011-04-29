<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<h1>Portal Setup</h1>
<cw:getContent key="root/portalListing" />

<table class="datatable textcenter">
    <thead>
        <tr>
            <th>Portal Name</th>
            <th>Is Default</th>
            <th>Theme</th>
        </tr>
    </thead> 
    <tbody>
        <c:forEach var="portal" items="${portalList}">
            <tr>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/root/portal/edit.htm?id=${ portal.id }">
                        <c:out value="${ portal.name }"/>
                    </a>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${ portal.default }">
                            Yes
                        </c:when>
                        <c:otherwise>
                            &nbsp;
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/root/theme/listing.htm?portalId=${ portal.id }">Edit</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
<div class="textright">
    <input class="form_action" type="button" value="Add Portal" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/root/portal/edit.htm'"/>
</div>