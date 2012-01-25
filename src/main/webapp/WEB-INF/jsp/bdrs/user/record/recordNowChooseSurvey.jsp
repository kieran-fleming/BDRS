<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<h1>Choose Project</h1>

<cw:getContent key="user/record/recordNowChooseSurvey" />

<table class="datatable">
	<tr>
		<th>Survey</th>
        <th>Record Now</th>
	</tr>
	<c:forEach var="survey" items="${surveyList}">
		<tr>
			<td title="${survey.description}">${survey.name}</td>
			<td class="textcenter">
				<form method="get" action="${pageContext.request.contextPath}/bdrs/user/taxonSurveyRenderRedirect.htm">
                    <div class="buttonpanel">
                        <input type="hidden" name="speciesId" value="${species.id}"/>
                        <input type="hidden" name="surveyId" value="${survey.id}"/>
                        <input title="Record a sighting for ${species.commonName}" class="button form_action" type="submit" value="Record Now"/>
                    </div>
                </form>
			</td>
		</tr>
	</c:forEach>
</table>