<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- required attributes --%>
<tiles:useAttribute name="recordWebFormContext" />  <%-- RecordWebFormContext --%>

<c:choose>
    <c:when test="${ recordWebFormContext.preview }">
        <div class="buttonpanel textright">
            <input class="form_action" type="button" value="Go Back" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/editAttributes.htm?surveyId=${recordWebFormContext.surveyId}'"/>
            <input class="form_action" type="button" value="Continue" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/locationListing.htm?surveyId=${recordWebFormContext.surveyId}'"/>
        </div>
    </c:when>
    <c:when test="${ recordWebFormContext.editable }">
    	
		<c:choose>
			<c:when test="${ recordWebFormContext.existingRecord }">
	                <div class="buttonpanel textright">
                        <input class="form_action" type="submit" name="submit" value="Save Changes"/>
                    </div>
                </form>
	        </c:when>
			<c:otherwise>
				    <div class="buttonpanel textright">
                        <input class="form_action" type="submit" name="submitAndAddAnother" value="Submit and Add Another"/>
                        <input class="form_action" type="submit" name="submit" value="Submit Sighting"/>
                    </div>
                </form>
			</c:otherwise>	
		</c:choose>
    </c:when>
    <c:when test="${ not recordWebFormContext.editable and recordWebFormContext.existingRecord }">
    	<c:if test="${recordWebFormContext.unlockable}">
    		<div class="buttonpanel textright">
	            <tiles:insertDefinition name="unlockRecordWidget">
	                <tiles:putAttribute name="recordId" value="${recordWebFormContext.recordId}" />
	                <tiles:putAttribute name="surveyId" value="${recordWebFormContext.surveyId}" />                    
	            </tiles:insertDefinition>
	        </div>
    	</c:if>
    </c:when>
</c:choose>