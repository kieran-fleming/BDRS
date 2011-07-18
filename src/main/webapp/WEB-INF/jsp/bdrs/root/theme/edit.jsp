<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<c:choose>
    <c:when test="${ editTheme.id == null }">
        <h1>Add Theme</h1>
    </c:when>
    <c:otherwise>
        <h1>Edit Theme</h1>
    </c:otherwise>
</c:choose>

<cw:getContent key="root/theme/edit" />

<form id="themeForm" method="POST" action="">
    <c:if test="${ editTheme.id != null }">
        <input type="hidden" name="themePk" value="${ editTheme.id }"/>
    </c:if>
    <input type="hidden" name="portalPk" value="${ portalId }"/>
    
    <c:if test="${editAsRoot}">
    <!-- These items should only be changable root...? -->
    <table class="form_table">
        <tbody>
            <tr>
                <th><label for="name">Name:</label></th>
                <td>
                    <input id="name" name="name" class="validate(required)" type="text" value="<c:out value="${ editTheme.name }"/>"/>
                </td>
            </tr>
            <tr>
                <th><label for="themeFileUUID">Managed File UUID:</label></th>
                <td>
                    <input id="themeFileUUID" name="themeFileUUID" class="validate(required)" type="text" value="<c:out value="${ editTheme.themeFileUUID }"/>"/>
                </td>
            </tr>
            <tr>
                <th><label for="active">Is Active:</label></th>
                <td>
                    <input id="active" name="active" type="checkbox" value="true" <c:if test="${ editTheme.active }">checked="checked"</c:if>/>
                </td>
            </tr>
        </tbody>
    </table>
    </c:if>
    
    <c:if test="${ not empty editTheme.themeElements }">
        <h3 class="sep">Theme Elements</h3>
        <cw:getContent key="root/theme/edit/themeElements" />
        
		<table class="form_table">
		    <tbody>
		        <c:forEach items="${ editTheme.themeElements }" var="themeElement">
		            <jsp:useBean id="themeElement" type="au.com.gaiaresources.bdrs.model.theme.ThemeElement"/>
		            <tr>
		                <th>
		                    <label for="theme_element_${ themeElement.id }_customValue">
		                        <c:out value="${ themeElement.key }"/>
		                    </label>
		                </th>
		                <td>
		                    <input id="theme_element_${ themeElement.id }_customValue" 
		                           name="theme_element_${ themeElement.id }_customValue" 
		                           type="text" 
		                           class="${ themeElement.type }"
		                           value="<c:out value="${ themeElement.customValue }"/>"/>
                            &nbsp;		                           
		                    <a href="javascript:void(0);" onclick="jQuery('#theme_element_${ themeElement.id }_customValue').val('<c:out value="${ themeElement.defaultValue }"/>');">
		                        Restore&nbsp;Default
		                    </a>
		                </td>
		            </tr>
		        </c:forEach>
		    </tbody>
		</table>
        <div id="advanced_editing" style="display:none">
			<h3 class="sep">Advanced Editing</h3>
	        <cw:getContent key="root/theme/edit/advanced" />
	        
	        <table>
	           <thead>
	               <tr>
	                   <th class="textright">Content Type</th>
	                   <th class="textleft">FileName</th>
	               </tr>
	           </thead>
	           <tbody>
    	           <c:forEach items="${ themeFileList }" var="themeFile">
    	               <tr>
	                       <jsp:useBean id="themeFile" type="au.com.gaiaresources.bdrs.controller.theme.ThemeFile"/>
	                       <td class="textright">
	                           <c:out value="<%= themeFile.getContentType() %>"/>
	                       </td>
	                       <td>
	                           <c:choose>
	                               <c:when test="<%= themeFile.canEdit() %>">
	                               
	                                   <a href="${pageContext.request.contextPath}/bdrs/<c:if test="${editAsRoot}">root</c:if><c:if test="${editAsAdmin}">admin</c:if>/theme/editThemeFile.htm?themeId=${ editTheme.id }&themeFileName=<%= themeFile.getFileName() %>">
                                           <c:out value="<%= themeFile.getDisplayName() %>"/>
                                       </a>
	                               </c:when>
	                               <c:when test="<%= themeFile.getFile().isDirectory() %>">
	                                   <c:out value="<%= themeFile.getDisplayName() %>"/>
	                               </c:when>
	                               <c:otherwise>
	                                   <a href="<%= themeFile.getDownloadURL()+themeFile.getFileName() %>">
                                           <c:out value="<%= themeFile.getDisplayName() %>"/>
                                       </a>
	                               </c:otherwise>
	                           </c:choose>
                           </td>
                       </tr>
	               </c:forEach>
               </tbody>
           </table>
               
        </div>
    </c:if>
    
    <div class="textright buttonpanel">
        <c:if test="${ editTheme.id != null }">
            <a href="javascript: void(0);" onclick="jQuery('#advanced_editing').toggle();">Advanced Editing</a>
            <span>&nbsp;|&nbsp;</span>
        </c:if>
        <input class="form_action" type="submit" value="Revert Theme" name="revert" onclick="return confirm('Reverting this theme will delete all local changes. Do you wish to continue?');"/>
        <input class="form_action" type="submit" value="Save Theme"/>
    </div>
</form>
