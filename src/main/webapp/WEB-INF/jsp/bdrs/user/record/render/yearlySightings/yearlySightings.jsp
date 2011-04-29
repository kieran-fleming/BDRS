<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="au.com.gaiaresources.bdrs.model.taxa.Attribute"%>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeScope"%>

<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>
<jsp:useBean id="today" scope="request" type="java.util.Date" />

<h1><c:out value="${survey.name}"/></h1>

<span id="script_content" class="hidden">
	<p>
	    This is a yearly calendar form &#8212; simply pick the location you are
	    recording for, then click on the day you are reporting on (today is
	    highlighted) and then enter the number of individuals that you saw on that
	    day. 
	</p>
	<c:if test="${ not preview }">
	    <form method="post" action="${pageContext.request.contextPath}/bdrs/user/yearlySightings.htm" enctype="multipart/form-data">
	</c:if>
	    <input type="hidden" id="ident" name="ident" value="<%= context.getUser().getRegistrationKey() %>"/>
	    <input type="hidden" id="surveyId" name="surveyId" value="${survey.id}"/>
	
	    <table class="form_table">
	        <tbody>
	            <tr>
	                <th>Species</th>
	                <td><span class="scientificName"><c:out value="${species.scientificName}"/></span></td>
	            </tr>
	            <tr>
	                <th><label for="location">Location<label/></th>
	                <td>
	                    <select id="location">
	                        <option></option>
	                        <c:forEach items="${locations}" var="loc">
	                            <option value="${loc.id}"
	                                <c:if test="${loc == location}">
	                                    selected="selected"
	                                </c:if>
	                            >
	                                <c:out value="${loc.name}"/>
	                            </option>
	                        </c:forEach>
	                    </select>
	                    <input type="text" name="locationId" class="validate(required)" style="width: 0px; visibility: hidden;"/>
	                    <c:if test="${ predefinedLocationsOnly == false }">
	                        <a href="${pageContext.request.contextPath}/bdrs/location/editUserLocations.htm?redirect=/bdrs/user/yearlySightings.htm%3FsurveyId=${survey.id}">Add Location</a>
                        </c:if>
	                </td>
	            </tr>
	            <c:forEach items="${formFieldList}" var="formField">
	                <tr>
	                    <th>
	                        <label for="attribute_${formField.attribute.id}">
	                            <c:out value="${formField.attribute.description}"/>
	                        </label>
	                    </th>
	                    <td>
	                        <tiles:insertDefinition name="attributeRenderer">
	                            <tiles:putAttribute name="formField" value="${formField}"/>
	                        </tiles:insertDefinition>
	                    </td>
	                </tr>
	            </c:forEach>
	        </tbody>
	    </table>
	
	    <table class="yearlysightings">
	        <thead>
	            <tr>
	                <th></th>
	                <c:forEach items="${dateMatrix[0]}" var="date">
	                    <th>
	                        <fmt:formatDate pattern="MMM yy" value="${date}"/>
	                    </th>
	                </c:forEach>
	            </tr>
	        </thead>
	        <tbody>
	            <c:forEach items="${dateMatrix}" var="monthArray" varStatus="monthArrayStatus">
	                <tr>
	                    <th>
	                        <c:out value="${monthArrayStatus.count}"/>
	                    </th>
	                    <c:forEach items="${monthArray}" var="date">
	                        <c:choose>
	                            <c:when test="${date != null}">
	                                <jsp:useBean id="date" type="java.util.Date"/>
	                                <td
	                                    <c:if test="<%= today.equals(date) %>">
	                                        class="today"
	                                    </c:if>
	                                >
	                                    <input type="text" class="sightingCell"
	                                        name="date_<%= date.getTime() %>"
	                                        title="<fmt:formatDate pattern="dd MMM yyyy" value="${ date }"/>"
	                                        <c:if test="<%= today.equals(date) %>">
	                                            placeholder="Today"
	                                        </c:if>
	                                    />
	                                </td>
	                            </c:when>
	                            <c:otherwise>
	                                <td></td>
	                            </c:otherwise>
	                        </c:choose>
	                    </c:forEach>
	                </tr>
	            </c:forEach>
	        </tbody>
	    </table>
	
	<c:choose>
	    <c:when test="${ preview }">
	        <div class="textright">
	            <input class="form_action" type="button" value="Go Back" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/editAttributes.htm?surveyId=${survey.id}'"/>
	            <input class="form_action" type="button" value="Continue" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/editLocations.htm?surveyId=${survey.id}'"/>
	        </div>
	    </c:when>
	    <c:otherwise>
	            <div class="textright">
	                <input type="submit" value="Submit Sightings" class="form_action"/>
	            </div>
	        </form>
	    </c:otherwise>
	</c:choose>
</span>

<noscript>
    <tiles:insertDefinition name="noscriptMessage"></tiles:insertDefinition>
</noscript>

<script type="text/javascript">
    jQuery(function() {
        jQuery("#script_content").removeClass("hidden");
    });
</script>

<script type="text/javascript">
    jQuery(function() {
        bdrs.contribute.yearlysightings.init();
        <c:if test="${location != null}">
            jQuery("#location").change();
        </c:if>
    });
</script>