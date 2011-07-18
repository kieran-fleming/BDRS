<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<c:choose>

    <c:when test="${fileError}">
        <p class="message">
           <c:out value="${errorMessage}"/>
        </p>
        <tiles:insertDefinition name="importForm">
            <tiles:putAttribute name="showHeader" value="true"/>
            <tiles:putAttribute name="header" value="Please Retry Your Upload"/>
        </tiles:insertDefinition>
    </c:when>

    <c:when test="${parseError}">
        <p class="message">
           <c:out value="${errorMessage}"/>
        </p>
        <tiles:insertDefinition name="importForm">
            <tiles:putAttribute name="showHeader" value="true"/>
            <tiles:putAttribute name="header" value="${errorDescription}"/>
            <tiles:putAttribute name="showHelp" value="false"/>
            <tiles:putAttribute name="extraContent">
                <table id="record_upload_error_table" class="datatable">
                    <thead>
                        <th>ID or Row Number</th>
                        <th>Message</th>
                    </thead>
                    <c:forEach items="${bulkUpload.errorRecordUploadList}" var="recordUpload">
                        <c:if test="${recordUpload.error}">
                            <tr>
                                <c:choose>
                                    <c:when test="${ empty recordUpload.id }">
                                        <td>Empty</td>
                                    </c:when>
                                    <c:otherwise>
                                        <td><c:out value="${recordUpload.id}"/></td>
                                    </c:otherwise>
                                </c:choose>
                               <td><c:out value="${recordUpload.errorMessage}"/></td>
                            </tr>
                        </c:if>
                    </c:forEach>
                </table>
            </tiles:putAttribute>
        </tiles:insertDefinition>
    </c:when>

    <c:when test="${dataError}">
        <p class="message">
           <c:out value="${errorMessage}"/>
        </p>
        <tiles:insertDefinition name="importForm">
            <tiles:putAttribute name="showHeader" value="true"/>
            <tiles:putAttribute name="header" value="${errorDescription}"/>
            <tiles:putAttribute name="showHelp" value="false"/>
            <tiles:putAttribute name="extraContent">
                <table id="record_upload_error_table" class="datatable">
                    <thead>
                        <th>Type</th>
                        <th>Name</th>
                        <th>Description</th>
                    </thead>
                    <c:forEach items="${bulkUpload.missingGroups}" var="missingGroupName">
                        <tr>
                           <td>Group</td>
                           <td><c:out value="${missingGroupName}"/></td>
                           <td>You do not have a student group by this name. Please add one.</td>
                        </tr>
                    </c:forEach>
                    <c:forEach items="${bulkUpload.missingUsers}" var="missingUserName">
                        <tr>
                           <td>User</td>
                           <td><c:out value="${missingUserName}"/></td>
                           <td>You do not have a user by this name. Please add one.</td>
                        </tr>
                    </c:forEach>
                    <c:forEach items="${bulkUpload.missingSurveys}" var="missingSurveyName">
                        <tr>
                           <td>Survey</td>
                           <td><c:out value="${missingSurveyName}"/></td>
                           <td>You do not have a survey by this name. Please contact your project administrator.</td>
                        </tr>
                    </c:forEach>
                    <c:forEach items="${bulkUpload.missingLocations}" var="locUpload">
                        <tr>
                           <td>Location</td>
                           <td><c:out value="${locUpload.locationName}"/></td>
                           <td>You do not have a location by this name. Please contact your project administrator.</td>
                        </tr>
                    </c:forEach>
                    <c:forEach items="${bulkUpload.missingIndicatorSpecies}" var="missingSpeciesName">
                        <tr>
                           <td>Species</td>
                           <td><c:out value="${missingSpeciesName}"/></td>
                           <td>We do not recognise this species. Please contact your project administrator.</td>
                        </tr>
                    </c:forEach>
                    <c:forEach items="${bulkUpload.invalidSurveySpecies}" var="invalidSurveySpecies">
                        <tr>
                           <td>Species</td>
                           <td>
                                &quot;<c:out value="${invalidSurveySpecies.key.scientificName}"/>&quot; 
                                cannot be added to &quot;<c:out value="${invalidSurveySpecies.value.name}"/>&quot;.
                            </td>
                            <td>This species cannot be recorded against this project. Please contact your project administrator.</td>
                        </tr>
                    </c:forEach>
                </table>
            </tiles:putAttribute>
        </tiles:insertDefinition>
    </c:when>

    <c:when test="${databaseError}">
        <p class="message">
           <c:out value="${errorMessage}"/>
        </p>
        <tiles:insertDefinition name="importForm">
            <tiles:putAttribute name="showHeader" value="true"/>
            <tiles:putAttribute name="header" value="${errorDescription}"/>
        </tiles:insertDefinition>
    </c:when>

    <c:otherwise>
        <h2>Import Summary</h2>
        <div class="input_container_2">
            <p>
                Congratulations. Your data has been imported and is visible
                in your <a href="${pageContext.request.contextPath}/map/mySightings.htm">My Sightings</a> page.
            </p>
        </div>
    </c:otherwise>
</c:choose>
