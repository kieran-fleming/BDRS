<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<h1>Record Listing</h1>

<div class="textright">
    <a href="${pageContext.request.contextPath}/bdrs/user/record/download.htm?${pageContext.request.queryString}">Download XLS</a>
</div>
<table class="datatable textcenter">
    <thead>
        <tr>
            <th>Date</th>
            <th>Location</th>
            <th>Number</th>
            <th>Notes</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var='rec' items='${recordList}'>
            <tr>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/user/surveyRenderRedirect.htm?surveyId=${rec.survey.id}&recordId=${rec.id}">
                        <span class="nowrap"><fmt:formatDate pattern="dd MMM yyyy" value="${rec.when}"/></span>
                    </a>
                </td>
                <td>
                    <jsp:useBean id="rec" type="au.com.gaiaresources.bdrs.model.record.Record" />
                    <span class="nowrap"><%= rec.getPoint() != null ? rec.getPoint().getX() : rec.getLocation().getLocation().getX() %>,</span>
                    <span class="nowrap"><%= rec.getPoint() != null ? rec.getPoint().getY() : rec.getLocation().getLocation().getY() %></span>
                </td>
                <td>
                    <c:out value="${rec.number}"/>
                </td>
                <td>
                    <c:out value="${rec.notes}"/>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
