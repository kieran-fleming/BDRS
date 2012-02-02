<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="au.com.gaiaresources.bdrs.model.taxa.Attribute"%>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeScope"%>

<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>
<jsp:useBean id="today" scope="request" type="java.util.Date" />

<%-- Access the facade to retrieve the preference information --%>
<jsp:useBean id="bdrsPluginFacade" scope="request" type="au.com.gaiaresources.bdrs.servlet.BdrsPluginFacade"></jsp:useBean>
<c:set var="showScientificName" value="<%= bdrsPluginFacade.getPreferenceBooleanValue(\"taxon.showScientificName\") %>" />

<h1><c:out value="${survey.name}"/></h1>

<noscript>
    <tiles:insertDefinition name="noscriptMessage"></tiles:insertDefinition>
</noscript>

<div id="script_content" class="invisible">
	<p>
	    This is a yearly calendar form &#8212; simply pick the location you are
	    recording for, then click on the day you are reporting on (today is
	    highlighted) and then enter the number of individuals that you saw on that
	    day. 
	</p>
	<c:if test="${ not preview }">
	    <form method="post" action="${pageContext.request.contextPath}/bdrs/user/yearlySightings.htm" enctype="multipart/form-data">
	</c:if>
	    <input type="hidden" id="ident" name="ident" value="<%= context.getUser() == null ? null : context.getUser().getRegistrationKey() %>"/>
	    <input type="hidden" id="surveyId" name="surveyId" value="${survey.id}"/>
	
	    <%-- the record form header contains the unlock form icon --%>
        <tiles:insertDefinition name="recordFormHeader">
            <tiles:putAttribute name="recordWebFormContext" value="${recordWebFormContext}" />
        </tiles:insertDefinition>
        
	    <table class="form_table">
	        <tbody>
	            <tr>
	                <th>Species</th>
	                <td>
	                	<c:choose>
                            <c:when test="${showScientificName}">
                                <span class="scientificName"><c:out value="${species.scientificName}"/></span>
                            </c:when>
                            <c:otherwise>
                                <span class="commonName"><c:out value="${ species.commonName }"/></span>
                            </c:otherwise>  
                        </c:choose>
					</td>
	            </tr>
	            <tr>
	            	<th><label for="location">Location<label/></th>
					<td>
		            	<c:choose>
		            		<c:when test="${recordWebFormContext.editable}">
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
		            		</c:when>
							<c:otherwise>
								<input type="hidden" id="location" value="${location.id}" />
								<input type="hidden" name="locationId" value="" />
								<c:out value="${location.name}" />
							</c:otherwise>
		            	</c:choose>
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
								<tiles:putAttribute name="editEnabled" value="${ recordWebFormContext.editable }" />
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
									   <c:choose>
										    <c:when test="${recordWebFormContext.editable}">
										        <input type="text" class="sightingCell"
		                                            name="date_<%= date.getTime() %>"
		                                            title="<fmt:formatDate pattern="dd MMM yyyy" value="${ date }"/>"
		                                            <c:if test="<%= today.equals(date) %>">
		                                                placeholder="Today"
		                                            </c:if>
		                                        />
										    </c:when>
										    <c:otherwise>
										        <span id="date_<%= date.getTime() %>"></span>
										    </c:otherwise>
										</c:choose>
	                                    
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
	
	   <%-- the record form footer contains the 'form' close tag --%>
	<tiles:insertDefinition name="recordFormFooter">
	    <tiles:putAttribute name="recordWebFormContext" value="${recordWebFormContext}" />                    
	</tiles:insertDefinition>

</div>

<script type="text/javascript">
    jQuery(function() {
        jQuery("#script_content").removeClass("invisible");
    });
</script>

<script type="text/javascript">
    jQuery(function() {
        bdrs.contribute.yearlysightings.init();
		
		<c:choose>
		  <c:when test="${location != null && recordWebFormContext.editable}">
		      jQuery("#location").change();
		  </c:when>
		  <c:when test="${!recordWebFormContext.editable}">
		      var locationId = ${location.id};
	          var surveyId = jQuery('#surveyId').val();
	          var ident = jQuery('#ident').val();
			  bdrs.contribute.yearlysightings.loadCellData(locationId, surveyId, ident, false);
		  </c:when>
		</c:choose>
		
        /**
         * Prepopulate fields
         */
        bdrs.form.prepopulate();
    });
</script>