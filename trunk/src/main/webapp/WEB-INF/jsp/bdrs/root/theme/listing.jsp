<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<h1>Theme Setup</h1>
<cw:getContent key="root/theme/listing" />

<table class="datatable textcenter">
    <thead>
        <tr>
            <th>Name</th>
            <th>Is Active</th>
        </tr>
    </thead> 
    <tbody>
        <c:forEach var="theme" items="${ themeList }">
            <tr>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/root/theme/edit.htm?portalId=${ portalId }&themeId=${ theme.id }">
                        <c:out value="${ theme.name }"/>
                    </a>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${ theme.active }">
                            Yes
                        </c:when>
                        <c:otherwise>
                            &nbsp;
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
<div class="textright">
    <input class="form_action" type="button" value="Add Theme" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/root/theme/edit.htm?portalId=${ portalId }'"/>
</div>