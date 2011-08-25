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
                        maxlength="255"
                        value="<c:out value="${survey.name}" default=""/>"
                    />
                </td>
            </tr>
            <tr>
                <th>Description:</th>
                <td>
                    <textarea name="description" class="validate(maxlength(1023))"><c:out value="${survey.description}"/></textarea>
                </td>
            </tr>
            <tr>
                <th>Survey Start Date:</th>
                <td>
                    <input type="text" class="datepicker_range validate(required)" name="surveyDate" id="from"
                        <c:if test="${survey.startDate != null}">
                            value="<fmt:formatDate pattern="dd MMM yyyy" value="${survey.startDate}"/>"
                        </c:if>
                    />
                </td>
            </tr>
            <tr>
                <th>Survey End Date:</th>
                <td>
                    <input type="text" class="datepicker_range" name="surveyEndDate" id="to"
                        <c:if test="${survey.endDate != null}">
                            value="<fmt:formatDate pattern="dd MMM yyyy" value="${survey.endDate}"/>"
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
	                            <input onchange="editSurveyPage.rendererTypeChanged(jQuery(this));" type="radio" class="vertmiddle" name="rendererType"
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
                <th title="The default visibility setting when creating a new record">Default Record Visibility:</th>
                <td>
                    <fieldset>
                        <c:forEach items="<%=au.com.gaiaresources.bdrs.model.record.RecordVisibility.values()%>" var="recVis">
                            <jsp:useBean id="recVis" type="au.com.gaiaresources.bdrs.model.record.RecordVisibility" />
                            <div>
                                <input type="radio" class="vertmiddle" name="defaultRecordVis"
                                    id="<%= recVis.toString() %>"
                                    value="<%= recVis.toString() %>"
                                    <c:if test="<%= recVis.equals(survey.getDefaultRecordVisibility()) %>">
                                        checked="checked"
                                    </c:if>
                                />
                                <label for="<%= recVis.toString() %>">
                                    <%= recVis.getDescription() %>
                                </label>
                            </div>
                        </c:forEach>
                    </fieldset>
                </td>
            </tr>
			<tr>
                <th title="Whether non-admins can change the visibility of their records">Record visibility modifiable by users:</th>
                <td>
                    <input type="checkbox" name="recordVisModifiable" value="true"
                        <c:if test="${survey.recordVisibilityModifiable}">
                            checked="checked"
                        </c:if>
                    />
                </td>
            </tr>
            <tr>
                <th title="Is the survey accessible for read access">Publish:</th>
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


<script type="text/javascript">
	editSurveyPage = {
	   rendererTypeChanged: function() {
	       var value = $('input:radio[name=rendererType]:checked').val();
		   var recordVisCheckBox = jQuery('input[name=recordVisModifiable]');
		   if (value === 'DEFAULT') {
		      recordVisCheckBox.removeAttr('disabled');
		   } else if (value === 'SINGLE_SITE_MULTI_TAXA') {
		      recordVisCheckBox.attr('disabled', 'disabled');
		   } else if (value === 'SINGLE_SITE_ALL_TAXA') {
              recordVisCheckBox.attr('disabled', 'disabled');
           } else if (value === 'ATLAS') {
              recordVisCheckBox.attr('disabled', 'disabled');
           } else {
		      // not expected value. default to disable the control.
			  recordVisCheckBox.attr('disabled', 'disabled');
		   }
	   }	
	};
	
	jQuery(function() {
		editSurveyPage.rendererTypeChanged();
	});
</script>