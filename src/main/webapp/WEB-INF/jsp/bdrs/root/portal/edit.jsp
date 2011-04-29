<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<c:choose>
    <c:when test="${ portal.id == null }">
        <h1>Add Portal</h1>
    </c:when>
    <c:otherwise>
        <h1>Edit Portal</h1>
    </c:otherwise>
</c:choose>

<cw:getContent key="root/portalEdit" />

<form action="" method="post" onKeyPress="return bdrs.util.preventReturnKeySubmit();">
	<c:if test="${ portal.id != null }">
	    <input type="hidden" name="portalId" value="${ portal.id }"/>
	</c:if>
	<table class="form_table">
	    <tbody>
	        <tr>
	            <th><label for="name">Name:</label></th>
	            <td>
	               <input class="validate(required)" type="text" name="name" value="${ portal.name }"/>
	            </td>
	        </tr>
	        <tr>
                <th><label for="default">Default:</label></th>
                <td>
                   <%-- 
                        At least one portal is default. If this portal is 
                        default, you cannot make it non-default because at 
                        that point, no portal is default. 
                        
                        Rather to change the default portal, check the portal
                        that you want to be default rather than uncheck the
                        portal that you want to be non-default. 
                    --%>
                   <c:choose>
                       <c:when test="${ portal.default }">
                           <input type="hidden" name="default" value="true"/>
                           Yes
                       </c:when>
                       <c:otherwise>
                           <input type="checkbox" name="default" value="true"/>
                       </c:otherwise>
                   </c:choose>
                </td>
            </tr>
	    </tbody>
	</table>
	
	<h3>Portal Entry Points</h3>
	<cw:getContent key="root/portalEdit/entryPoints" />
	
    <input id="entryPointIndex" type="hidden" value="0"/> 
    <div class="textright buttonpanel">
        <input class="form_action" type="button" value="Add Entry Point" onclick="bdrs.portal.addPortalEntryPoint('#portalEntryPointTable', '#entryPointIndex');"/>
    </div>
	<table id="portalEntryPointTable" class="datatable textcenter">
	   <thead>
	       <tr>
	           <th>Regex Pattern</th>
	           <th>Redirect URL</th>
	           <th>Delete</th>
	       </tr>
	   </thead>
	   <tbody>
	       <c:forEach var="portalEntryPoint" items="${ portalEntryPointList }">
	           <tiles:insertDefinition name="portalEntryPointRow">
                    <tiles:putAttribute name="portalEntryPoint" value="${ portalEntryPoint }"/>
                </tiles:insertDefinition>
	       </c:forEach>
	   </tbody>
	</table>
	
	<h3>Pattern Tester</h3>
    <cw:getContent key="root/portalEdit/patternTester" />
    
    <table>
        <tbody>
            <tr>
                <th>Test URL:</th>
                <td>
                    <input name="testUrl" type="text"/>
                </td>
                <td>
                    <input class="form_action" type="button" value="Test" onclick="bdrs.portal.testPortalEntryPointPattern('#urlTestResult');"/>
                </td>
            </tr>
        <tbody>
    </table>
    <div id="urlTestResult"></div>
    
    <div class="textright buttonpanel">
        <input class="form_action" type="submit" value="Save Portal"/>
    </div>
	
</form>


