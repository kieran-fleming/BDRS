<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<h1>Project Listing</h1>

<table class="datatable textcenter">
    <thead>
        <tr>
            <th>Start Date</th>
            <th>End Date</th>
            <th>Name</th>
            <th>Taxonomy</th>
            <th>Form</th>
            <th>Locations</th>
            <th>Access</th>
            <th>Publish</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var='survey' items='${surveyList}'>
            <tr>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/admin/survey/edit.htm?surveyId=${survey.id}">
                        <fmt:formatDate pattern="dd MMM yyyy" value="${survey.startDate}"/>
                    </a>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/admin/survey/edit.htm?surveyId=${survey.id}">
                        <fmt:formatDate pattern="dd MMM yyyy" value="${survey.endDate}"/>
                    </a>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/admin/survey/edit.htm?surveyId=${survey.id}">
                        <c:out value="${survey.name}"/>
                    </a>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/admin/survey/editTaxonomy.htm?surveyId=${survey.id}">
                        Edit
                    </a>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/admin/survey/editAttributes.htm?surveyId=${survey.id}">
                        Edit
                    </a>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/admin/survey/editLocations.htm?surveyId=${survey.id}">
                        Edit
                    </a>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/admin/survey/editUsers.htm?surveyId=${survey.id}">
                        Edit
                    </a>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/bdrs/admin/survey/edit.htm?surveyId=${survey.id}&publish=publish">
                        Edit
                    </a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<div class="textright">
    <input class="form_action" type="button" value="Add Project" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/edit.htm';"/>
</div>