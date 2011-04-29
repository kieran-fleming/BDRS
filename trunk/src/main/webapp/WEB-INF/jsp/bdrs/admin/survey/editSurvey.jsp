<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType"%>
<%@page import="au.com.gaiaresources.bdrs.model.metadata.Metadata"%>
<jsp:useBean id="survey" type="au.com.gaiaresources.bdrs.model.survey.Survey" scope="request"/>
<c:choose>
    <c:when test="${survey.id == null }">
        <h1>Add Project</h1>
    </c:when>
    <c:otherwise>
        <h1>Edit ${survey.name}</h1>
    </c:otherwise>
</c:choose>

<form method="POST" action="${pageContext.request.contextPath}/bdrs/admin/survey/edit.htm" enctype="multipart/form-data">
    <c:if test="${survey.id != null }">
        <input type="hidden" name="surveyId" value="${survey.id}"/>
    </c:if>

    <table class="form_table">
        <tbody>
            <tr>
                <th>Name:</th>
                <td>
                    <input type="text" name="name" class="validate(required)"
                        maxlength="18"
                        value="<c:out value="${survey.name}" default=""/>"
                    />
                </td>
            </tr>
            <tr>
                <th>Description:</th>
                <td>
                    <textarea name="description"><c:out value="${survey.description}"/></textarea>
                </td>
            </tr>
            <tr>
                <th>Survey Date:</th>
                <td>
                    <input type="text" class="datepicker validate(required)" name="surveyDate"
                        <c:if test="${survey.date != null}">
                            value="<fmt:formatDate pattern="dd MMM yyyy" value="${survey.date}"/>"
                        </c:if>
                    />
                </td>
            </tr>
            <%-- This is covered by the user access page
            <tr>
                <th>Public:</th>
                <td>
                    <input type="checkbox" name="publik"
                        <c:if test="${survey.public}">
                            checked="checked"
                        </c:if>
                    />
                </td>
            </tr>
            --%>
            <tr>
                <th>Survey Logo:</th>
                <td>
                    <c:set var="logo_md" value="<%= survey.getMetadataByKey(Metadata.SURVEY_LOGO) %>" scope="page"/>
                    <c:if test="${ logo_md != null }">
                        <jsp:useBean id="logo_md" type="au.com.gaiaresources.bdrs.model.metadata.Metadata" scope="page"/>
                        <div id="<%= Metadata.SURVEY_LOGO %>_img">
                            <a href="${pageContext.request.contextPath}/files/download.htm?<%= logo_md.getFileURL() %>">
                                <img width="250"
                                    src="${pageContext.request.contextPath}/files/download.htm?<%= logo_md.getFileURL() %>"
                                    alt="Missing Image"/>
                            </a>
                        </div>
                    </c:if>
                    <div style="line-height: 0em;">
                        <input type="text"
                            id="<%= Metadata.SURVEY_LOGO %>"
                            name="<%= Metadata.SURVEY_LOGO %>"
                            style="visibility: hidden;height: 0em;"
                            <c:if test="${logo_md != null}">
                                value="<c:out value="${logo_md.value}"/>"
                            </c:if>
                        />
                    </div>
                    <input type="file"
                        accept="image/gif,image/jpeg,image/png"
                        id="<%= Metadata.SURVEY_LOGO %>_file"
                        name="<%= Metadata.SURVEY_LOGO %>_file"
                        class="image_file"
                        onchange="bdrs.util.file.imageFileUploadChangeHandler(this);jQuery('#<%= Metadata.SURVEY_LOGO %>').val(jQuery(this).val());"
                    />
                    <a href="javascript: void(0)"
                        onclick="jQuery('#<%= Metadata.SURVEY_LOGO %>, #<%= Metadata.SURVEY_LOGO %>_file').attr('value','');jQuery('#<%= Metadata.SURVEY_LOGO %>_img').remove();">
                        Clear
                    </a>                    
                </td>
            </tr>
            
            <tr>
                <th>Form Renderer:</th>
                <td>
                    <fieldset>
                        <c:forEach items="<%=SurveyFormRendererType.values()%>" var="rendererType">
                            <jsp:useBean id="rendererType" type="au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType" />
                            <div>
	                            <input type="radio" class="vertmiddle" name="rendererType"
	                                id="<%= rendererType.toString() %>"
	                                value="<%= rendererType.toString() %>"
	                                <c:if test="<%= rendererType.equals(survey.getFormRendererType()) || (survey.getFormRendererType() == null && SurveyFormRendererType.DEFAULT.equals(rendererType)) %>">
	                                    checked="checked"
	                                </c:if>
	                                <c:if test="<%= !rendererType.isEligible(survey) %>">
	                                    disabled="disabled"
	                                </c:if>
	                            />
	                            <label for="<%= rendererType.toString() %>">
	                                <%= rendererType.getName() %>
	                            </label>
                            </div>
                        </c:forEach>
                    </fieldset>
                </td>
            </tr>
            <tr>
                <th>Publish:</th>
                <td>
                    <input type="checkbox" name="active"
                        <c:if test="${survey.active}">
                            checked="checked"
                        </c:if>
                    />
                </td>
            </tr>
        </tbody>
    </table>

    <div class="textright buttonpanel">
        <input type="submit" class="form_action" value="Save"/>
        <c:if test="${publish == false}">
            <input type="submit" class="form_action" name="saveAndContinue" value="Save And Continue"/>
        </c:if>
    </div>
</form>
