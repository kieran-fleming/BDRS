<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<tiles:useAttribute name="showHeader"/>
<tiles:useAttribute name="header"/>
<tiles:useAttribute name="showHelp"/>
<tiles:useAttribute name="extraContent"/>

<c:if test="${ showHeader }">
    <h2><tiles:insertAttribute name="header" ignore="true"/></h2>
</c:if>

<form id="uploadSpreadsheetForm" action="${pageContext.request.contextPath}/bulkdata/upload.htm" method="post" enctype="multipart/form-data">
    <c:if test="${ showHelp }">
        <p>
            Once you have filled out your template, upload it below. When you 
            upload a file, it will be checked against the project to make sure
            it matches the project requirements.
        </p>
    </c:if>
    <tiles:insertAttribute name="extraContent"/>
    <table class="form_table">
        <tbody>
            <tr>
                <th>
                    <label for="surveySelect">Project:</label>
                </th>
                <td>
                    <select id="surveySelect" name="surveyPk">
                        <c:forEach items="${surveyList}" var="curSurvey">
                            <option value="${curSurvey.id}"
                                <c:if test="${ curSurvey == survey }">
                                    selected="selected"
                                </c:if>
                            >
                                <c:out value="${curSurvey.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </td>
            </tr>
            <tr>
                <th>
                    <label class="strong" for="spreadsheetInput">Spreadsheet:</label>
                </th>
                <td>
                    <input id="spreadsheetInput" name="spreadsheet" type="file"/>
                </td>
            </tr>
        </tbody>
    </table>
    <div class="textright">
        <input type="submit" value="Upload Spreadsheet" class="form_action"/>
    </div>
</form>
