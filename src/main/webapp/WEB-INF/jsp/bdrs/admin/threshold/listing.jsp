<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<h1>Threshold Listing</h1>

<table class="datatable textcenter">
    <thead>
        <tr>
            <th>Type</th>
            <th>Enabled</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var='entry' items='${ displayNameThresholdMap }'>
            <tr>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/admin/threshold/edit.htm?thresholdId=${ entry.key.id }">
                        <c:out value="${ entry.value }"/>
                    </a>
                </td>
                <td class="textcenter">
                    <c:choose>
                        <c:when test="${ entry.key.enabled }">
                            Yes
                        </c:when>
                        <c:otherwise>
                            No
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<div class="textright">
    <input class="form_action" type="button" value="Add Threshold" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/threshold/edit.htm';"/>
</div>