<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<h2>Shapefile Import Summary</h2>
<c:choose>

    <c:when test="${errors != null}">
    	<div>
            <p class="message">
                Errors have been detected in your uploaded data. Correct the errors below and reupload.
            </p>
        </div>

        <table id="record_upload_error_table" class="datatable">
            <thead>
                <th>Object ID</th>
                <th>Field</th>
				<th>Message</th>
            </thead>
					
	        <c:forEach items="${errors}" var="item">
				<tr><td colspan="3" class="strong">${item.recordEntry.description}</td></tr>
	        	<c:forEach items="${item.errorMap}" var="entry">
	        		<tr>
	        			<td></td>
						<td>${entry.key}</td>
						<td>${entry.value}</td>
	        		</tr>
	        	</c:forEach> 
	        </c:forEach>
        </table>
    </c:when>

    <c:otherwise>
        <div>
            <p class="message">
                Congratulations. Your data has been imported and is visible
                in your <a href="${pageContext.request.contextPath}/map/mySightings.htm">My Sightings</a> page.
            </p>
        </div>
    </c:otherwise>
</c:choose>
