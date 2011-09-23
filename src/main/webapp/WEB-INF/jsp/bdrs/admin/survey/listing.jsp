<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<h1>Edit Projects</h1>

<cw:getContent key="admin/editProjects" />

<div class="buttonpanel textright">
    <input class="form_action" type="button" value="Add Project" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/edit.htm';"/>
</div>

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
                    <fmt:formatDate pattern="dd MMM yyyy" value="${survey.startDate}"/>
                </td>
                <td>
                    <fmt:formatDate pattern="dd MMM yyyy" value="${survey.endDate}"/>
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
                    <a href="${pageContext.request.contextPath}/bdrs/admin/survey/locationListing.htm?surveyId=${survey.id}">
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
