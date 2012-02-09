<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%-- used for SingleSiteMultiTaxa AND SingleSiteAllTaxa --%>


<%@page import="au.com.gaiaresources.bdrs.servlet.RequestContextHolder"%>
<tiles:useAttribute name="recordFormFieldCollection" classname="au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordFormFieldCollection" ignore="true" />
<tiles:useAttribute name="editEnabled" ignore="true" />

<%-- when there is a record form field collection use it, it's filled with data. Otherwise use the formFieldList object to create the empty row --%>

<c:choose>
    <c:when test="${recordFormFieldCollection != null}">
        <c:set var="ffList" value="${ recordFormFieldCollection.formFields }"></c:set>
        <c:set var="highlight" value="${recordFormFieldCollection.highlight}"></c:set>
    </c:when>
    <c:otherwise>
        <c:set var="ffList" value="${ formFieldList }"></c:set>
        <c:set var="highlight" value="false"></c:set>
    </c:otherwise>
</c:choose>

<tr class="textcenter <c:if test="${highlight}">bdrsHighlight</c:if>"  >

    <input name="${recordFormFieldCollection.prefix}${sightingIndex}recordId" type="hidden" value="${recordFormFieldCollection.recordId}" class="recordRow" />
    <input name="rowPrefix" type="hidden" value="${recordFormFieldCollection.prefix}${sightingIndex}" />
    <c:forEach items="${ffList}" var="formField">
        <jsp:useBean id="formField" type="au.com.gaiaresources.bdrs.controller.attribute.formfield.AbstractRecordFormField" />
           <c:choose>
               <c:when test="<%= formField.isPropertyFormField() %>">
                   <c:if test="${ not formField.hidden }">
                       <td>
                           <tiles:insertDefinition name="propertyRenderer">
                              <tiles:putAttribute name="formField" value="${ formField }"/>
                           <tiles:putAttribute name="editEnabled" value="${ editEnabled }" />
                          </tiles:insertDefinition>
                      </td>
                   </c:if>
               </c:when>
               <c:when test="<%= formField.isAttributeFormField() %>">
                   <c:if test="<%= formField.isModerationFormField() %>">
                       <c:if test="${editEnabled}">
                           <c:set var="editEnabled" value="<%= RequestContextHolder.getContext().getUser().isModerator() %>"></c:set>
                       </c:if>
                   </c:if>
                   <td>
                       <tiles:insertDefinition name="attributeRenderer">
                           <tiles:putAttribute name="formField" value="${ formField }"/>
                           <tiles:putAttribute name="editEnabled" value="${ editEnabled }" />
                       </tiles:insertDefinition>
                   </td>
            </c:when>
        </c:choose>
    </c:forEach>
     <c:if test="${ editEnabled and not preview}">
	    <td class="delete_col">
	     	<a href="javascript: void(0);" onclick="bdrs.survey.deleteAjaxRecord('${ident}', '${recordFormFieldCollection.recordId}', jQuery(this).parents('tr'), '.messages');">
	           <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
			</a>
	    </td>
	</c:if>
</tr>


