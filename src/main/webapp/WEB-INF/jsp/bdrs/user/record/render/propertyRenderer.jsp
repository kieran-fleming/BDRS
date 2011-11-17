<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>


<%@page import="au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyType"%>
<tiles:useAttribute name="formField" classname="au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyFormField"/>
<tiles:useAttribute name="isLatitude" ignore="true"/>
<tiles:useAttribute name="isLongitude" ignore="true"/>

<tiles:useAttribute name="locations" classname="java.util.Set" ignore="true"/>

<tiles:useAttribute name="errorMap" classname="java.util.Map" ignore="true"/>
<tiles:useAttribute name="valueMap" classname="java.util.Map" ignore="true"/>

<tiles:useAttribute name="formPrefix" ignore="true"/>

<%@page import="au.com.gaiaresources.bdrs.model.record.Record"%>
<%@page import="au.com.gaiaresources.bdrs.model.method.Taxonomic"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.GregorianCalendar"%>

<c:if test="${ formPrefix == null }">
<!-- set formPrefix to the passed value -->
    <c:set var="formPrefix" value="${ formField.prefix }"></c:set>
</c:if>

<c:choose>
    <c:when test="<%= RecordPropertyType.SPECIES.getName().equals(formField.getPropertyName()) %>">
        <c:choose>
            <c:when test="${ formField.species != null }">
                <input type="hidden" name="${ formPrefix }species" value="${ formField.species.id }"/>
                <span class="scientificName"><c:out value="${ formField.species.scientificName }"/></span>
            </c:when>
            <c:when test="<%= (formField.getSurvey().getSpecies().size() > 1) || (formField.getSurvey().getSpecies().size() == 0) %>">
                <c:if test="<%= errorMap != null && errorMap.containsKey(formField.getPrefix()+\"survey_species_search\") %>">
                    <p class="error">
                        <c:out value="<%= errorMap.get(formField.getPrefix()+\"survey_species_search\") %>"/>
                    </p>
                </c:if>
                <input id="survey_species_search" type="text" name="${ formPrefix }survey_species_search"
                    <c:choose>
                        <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"survey_species_search\") %>">
                            value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"survey_species_search\") %>"/>"
                        </c:when>
                        <c:when test="${ formField.record != null }">
                            value="<c:out value="${ formField.record.species.scientificName }"/>"
                        </c:when>
                    </c:choose>
                />
                <!-- The styling below is to ensure the ketchup validation box popups up aligned to the input above -->
                <input type="text"
                    <c:choose>
                        <c:when test="${ formField.required }"> 
                            class="validate(required)"
                        </c:when>
                    </c:choose>
                    name="${ formPrefix }species" value="${ formField.record.species.id }" style="visibility:hidden;width:1px;padding:2px 0 0 0;margin:0 0 0 -4px;border-width:0;"/>
            </c:when>
            <c:when test="<%= formField.getSurvey().getSpecies().size() == 1 %>">
                <input type="hidden" name="${ formPrefix }species" value="<%= formField.getSurvey().getSpecies().iterator().next().getId() %>"/>
                <span class="scientificName"><c:out value="<%= formField.getSurvey().getSpecies().iterator().next().getScientificName() %>"/></span>
            </c:when>
            <c:otherwise>
                Misconfigured Project. No species available.</br>
                <sec:authorize ifAnyGranted="ROLE_ADMIN">
                    <a href="${pageContext.request.contextPath}/bdrs/admin/survey/editTaxonomy.htm?surveyId=${ formField.survey.id }">
                        Assign a species now.
                    </a>
                </sec:authorize>
                <sec:authorize ifNotGranted="ROLE_ADMIN">
                    Please contact the project administrator.
                </sec:authorize>
            </c:otherwise>
        </c:choose>
    </c:when>
    
    <c:when test="<%= RecordPropertyType.LOCATION.getName().equals(formField.getPropertyName()) %>">
         <select id="location" onchange="bdrs.survey.location.updateLocation(jQuery(this).val(), { attributeSelector: '#attributesContainer' });" name="${ formPrefix }location" >
            <option value="-1"></option>
            <c:forEach items="${ locations }" var="location">
                <jsp:useBean id="location" type="au.com.gaiaresources.bdrs.model.location.Location"/>
                <option value="${ location.id }"
                    <c:choose>
                        <c:when test="<%= valueMap != null && location.getId().toString().equals(valueMap.get(formField.getPrefix()+\"location\")) %>">
                            selected="selected"
                        </c:when>
                        <c:when test="${ formField.record.location == location }">
                            selected="selected"
                        </c:when>
                    </c:choose>
                >
                    <c:out value="${ location.name }"/>
                </option>
                <c:if test="<%= valueMap != null && location.getId().toString().equals(valueMap.get(formField.getPrefix()+\"location\")) %>">
                <script type="text/javascript">
                    <%-- 
                      The following snippet sets the initial value for the attributes based
                      on the location selection. 
                      
                      It is ok if the browser does not have javascript enabled because 
                      the hidden input is only present to support ketchup validation.
                    --%>
                    jQuery(window).load(function() {
                        bdrs.survey.location.updateLocation(${ location.id }, { attributeSelector: '#attributesContainer' });
                    });
                </script>
                </c:if>
                <c:if test="${ formField.record.location == location }">
                <script type="text/javascript">
                    <%-- 
                      The following snippet sets the initial value for the attributes based
                      on the location selection. 
                      
                      It is ok if the browser does not have javascript enabled because 
                      the hidden input is only present to support ketchup validation.
                    --%>
                    jQuery(window).load(function() {
                        bdrs.survey.location.updateLocation(${ location.id }, { attributeSelector: '#attributesContainer' });
                    });
                </script>
                </c:if>
            </c:forEach>
        </select>
        <div id="attributesContainer"></div>
    </c:when>
    
    <c:when test="<%= RecordPropertyType.POINT.getName().equals(formField.getPropertyName()) %>">
        <c:choose>
            <c:when test="${ isLatitude }">
                <c:if test="<%= errorMap != null && errorMap.containsKey(formField.getPrefix()+\"latitude\") %>">
                    <p class="error">
                        <c:out value="<%= errorMap.get(formField.getPrefix()+\"latitude\") %>"/>
                    </p>
                </c:if>
                <input id="latitude" type="text" name="${ formPrefix }latitude" class="validate(rangeOrBlank(-90,90), numberOrBlank<c:if test="${ formField.required }">,required</c:if>)"
                    <c:choose>
                        <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"latitude\") %>">
                            value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"latitude\") %>"/>"
                        </c:when>
                        <c:when test="${ formField.record != null }">
                            value="<%= formField.getRecord().getLatitude() == null ? "" : formField.getRecord().getLatitude() %>"
                        </c:when>
                    </c:choose>
                    <c:if test="<%= formField.getSurvey().isPredefinedLocationsOnly() %>">
                        readonly="readonly"
                    </c:if>
                />
            </c:when>
            <c:when test="${ isLongitude }">
                <c:if test="<%= errorMap != null && errorMap.containsKey(formField.getPrefix()+\"longitude\") %>">
                    <p class="error">
                        <c:out value="<%= errorMap.get(formField.getPrefix()+\"longitude\") %>"/>
                    </p>
                </c:if>
                <input id="longitude" type="text" name="${ formPrefix }longitude" class="validate(rangeOrBlank(-180,180), numberOrBlank<c:if test="${ formField.required }">,required</c:if>)"
                    <c:choose>
                        <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"longitude\") %>">
                            value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"longitude\") %>"/>"
                        </c:when>
                        <c:when test="${ formField.record != null }">
                            value="<%= formField.getRecord().getLongitude() == null ? "" : formField.getRecord().getLongitude() %>"
                        </c:when>
                    </c:choose>
                    <c:if test="<%= formField.getSurvey().isPredefinedLocationsOnly() %>">
                        readonly="readonly"
                    </c:if>
                />
            </c:when>
        </c:choose>
    </c:when>
    
    <c:when test="<%= RecordPropertyType.WHEN.getName().equals(formField.getPropertyName()) %>">
        <c:if test="<%= errorMap != null && errorMap.containsKey(formField.getPrefix()+\"date\") %>">
            <p class="error">
                <c:out value="<%= errorMap.get(formField.getPrefix()+\"date\") %>"/>
            </p>
        </c:if>
        
        <c:set var="calDate" value="<%= new GregorianCalendar() %>"/>
        <jsp:useBean id="calDate" type="java.util.Calendar"/>
        <%
            if(formField.getRecord() != null && formField.getRecord().getWhen() != null) {
                calDate.setTime(formField.getRecord().getWhen());
            }
        %>
        
        <input id="date" class="datepicker_historical validate(required)" type="text" name="${ formPrefix }date"
            <c:choose>
                <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"date\") %>">
                    value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"date\") %>"/>"
                </c:when>
                <c:when test="${ formField.record != null }">
                    value="<fmt:formatDate pattern="dd MMM yyyy" value="${ formField.record.when }"/>"
                </c:when>
                <c:otherwise>
                   value="<fmt:formatDate pattern="dd MMM yyyy" value="${ calDate }"/>"
                </c:otherwise>
            </c:choose>
        />
    </c:when>
    
    <c:when test="<%= RecordPropertyType.TIME.getName().equals(formField.getPropertyName()) %>">
        <c:set var="cal" value="<%= new GregorianCalendar() %>"/>
        <jsp:useBean id="cal" type="java.util.Calendar"/>
        <%
            if(formField.getRecord() != null && formField.getRecord().getWhen() != null) {
                cal.setTime(formField.getRecord().getWhen());
            }
        %>
        <select id="time_hour" name="${ formPrefix }time_hour">
            <c:forEach var="hr" items="<%= new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23} %>">
                <jsp:useBean id="hr" type="java.lang.Integer"/>
                <option value="${ hr }"
                    <c:if test="<%= cal.get(Calendar.HOUR_OF_DAY) == hr %>">
                        selected="selected"
                    </c:if>
                >
                    <c:out value="<%= String.format(\"%02d\", hr) %>"/>
                </option>
            </c:forEach>
        </select>
        
        <select id="time_minute" type="text" name="${ formPrefix }time_minute">
            <c:forEach var="min" items="<%= new int[]{0,5,10,15,20,25,30,35,40,45,50,55} %>">
                <jsp:useBean id="min" type="java.lang.Integer"/>
                <option value="${ min }"
                    <c:if test="<%= cal.get(Calendar.MINUTE) >= min && cal.get(Calendar.MINUTE) < min + 5 %>">
                        selected="selected"
                    </c:if>
                >
                    <c:out value="<%= String.format(\"%02d\", min) %>"/>
                </option>
            </c:forEach>
        </select>
    </c:when>
    
    <c:when test="<%= RecordPropertyType.NOTES.getName().equals(formField.getPropertyName()) %>">
        <c:if test="<%= errorMap != null && errorMap.containsKey(formField.getPrefix()+\"notes\") %>">
            <p class="error">
                <c:out value="<%= errorMap.get(formField.getPrefix()+\"notes\") %>"/>
            </p>
        </c:if>
        <%-- Has to be on one line, otherwise there will be whitespacing in the text area --%>
        <textarea id="notes" name="${ formPrefix }notes"<c:if test="${ formField.required }"> class="validate(required)"</c:if>><c:choose><c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"notes\") %>"><c:out value="<%= valueMap.get(formField.getPrefix()+\"notes\") %>"/></c:when><c:when test="${ formField.record != null }"><c:out value="${ formField.record.notes }"/></c:when></c:choose></textarea>
    </c:when>
    
    <c:when test="<%= RecordPropertyType.NUMBER.getName().equals(formField.getPropertyName()) %>">
        <c:if test="<%= errorMap != null && errorMap.containsKey(formField.getPrefix()+\"number\") %>">
            <p class="error">
                <c:out value="<%= errorMap.get(formField.getPrefix()+\"number\") %>"/>
            </p>
        </c:if>
        <input id="number" type="text" name="${ formPrefix }number" 
        
         	<c:choose>
         		<c:when test="${ formField.required }">
         			class="validate(positiveIntegerLessThanOneMillion)"
         		</c:when>
         		<c:otherwise>
         			class="validate(positiveIntegerLessThanOneMillionOrBlank)"
         		</c:otherwise>
         	</c:choose>
        
        
            <c:if test="${ formField.record != null}">
                <c:choose>
                    <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"number\") %>">
                        value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"number\") %>"/>"
                    </c:when>
                    <c:when test="${ formField.record != null }">
                        value="<c:out value="${ formField.record.number }"/>"
                    </c:when>
                </c:choose>
            </c:if>
        />
    </c:when>
    
    <c:when test="<%= RecordPropertyType.ACCURACY.getName().equals(formField.getPropertyName()) %>">
        <c:if test="<%= errorMap != null && errorMap.containsKey(formField.getPrefix()+\"accuracyInMeters\") %>">
            <p class="error">
                <c:out value="<%= errorMap.get(formField.getPrefix()+\"accuracyInMeters\") %>"/>
            </p>
        </c:if>
        <input id="accuracyInMeters" type="text" name="${ formPrefix }accuracyInMeters"
        	 <c:choose>
                    <c:when test="${ not formField.required }">
            	        class="validate(numberOrBlank)"
                    </c:when>
                    <c:otherwise>
                    	class="validate(number)"
                    </c:otherwise>
                </c:choose>
            <c:if test="${ formField.record != null}">
                <c:choose>
                    <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"accuracyInMeters\") %>">
                        value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"accuracyInMeters\") %>"/>"
                    </c:when>
                    <c:when test="${ formField.record != null }">
                        value="<c:out value="${ formField.record.accuracyInMeters }"/>"
                    </c:when>
                </c:choose>
            </c:if>
        />                         
    </c:when>
</c:choose>


