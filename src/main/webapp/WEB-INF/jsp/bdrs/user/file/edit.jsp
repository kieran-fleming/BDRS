<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<jsp:useBean id="managedFile" scope="request" type="au.com.gaiaresources.bdrs.model.file.ManagedFile" />

<form method="POST" enctype="multipart/form-data">
	<c:choose>
	    <c:when test="${ managedFile.id == null }">
	        <h1>Add Media</h1>
	    </c:when>
	    <c:otherwise>
	        <h1>Edit Media</h1>
	        <input type="hidden" name="managedFilePk" value="${ managedFile.id }"/>        
	    </c:otherwise>
	</c:choose>
	
	<cw:getContent key="admin/manageFiles/editFile" />
	
	<table class="form_table">
	   <tbody>
           <c:if test="${ managedFile.id != null }">
               <tr>
                   <th>Identifier:</th>
                   <td>
                       <c:out value="${ managedFile.uuid }"/>
                   </td>
               </tr>
	           <tr>
	               <th>Preview/Link:</th>
	               <td id="file_preview">
	                   <c:choose>
	                       <c:when test='<%= "image/png".equals(managedFile.getContentType()) %>'>
	                           <a href="${pageContext.request.contextPath}/files/download.htm?<%= managedFile.getFileURL() %>">
                                   <img src="${pageContext.request.contextPath}/files/download.htm?<%= managedFile.getFileURL() %>" width="250"/>
                               </a>
	                       </c:when>
	                       <c:when test='<%= "image/jpg".equals(managedFile.getContentType()) %>'>
	                           <a href="${pageContext.request.contextPath}/files/download.htm?<%= managedFile.getFileURL() %>">
                                   <img src="${pageContext.request.contextPath}/files/download.htm?<%= managedFile.getFileURL() %>" width="250"/>
                               </a>
                           </c:when>
                           <c:when test='<%= "image/jpeg".equals(managedFile.getContentType()) %>'>
                               <a href="${pageContext.request.contextPath}/files/download.htm?<%= managedFile.getFileURL() %>">
                                   <img src="${pageContext.request.contextPath}/files/download.htm?<%= managedFile.getFileURL() %>" width="250"/>
                               </a>
                           </c:when>
                           <c:when test='<%= "image/gif".equals(managedFile.getContentType()) %>'>
                               <a href="${pageContext.request.contextPath}/files/download.htm?<%= managedFile.getFileURL() %>">
                                   <img src="${pageContext.request.contextPath}/files/download.htm?<%= managedFile.getFileURL() %>" width="250"/>
                               </a>
                           </c:when>
                           <c:otherwise>
                               <a href="${pageContext.request.contextPath}/files/download.htm?<%= managedFile.getFileURL() %>">
                                   <c:out value="${ managedFile.filename }"/>
                               </a>
                           </c:otherwise>
	                   </c:choose>
	               </td>
               </tr>
           </c:if>
	       <tr>
	           <th>File:</th>
	           <td>
	               <input type="file" name="file" onchange="jQuery('#filename').val(jQuery(this).val());jQuery('#file_preview').empty();"/>
                   <input id="filename" type="text"  class="validate(required)" style="visibility:hidden;height:0em;width:0em;" name="filename" value="${ managedFile.filename }"/>
	           </td>
	       </tr>
	       <tr>
               <th>Description:</th>
               <td>
                   <textarea name="description"><c:out value="${ managedFile.description }" /></textarea>
               </td>
           </tr>
           <tr>
               <th>Credit:</th>
               <td>
                   <input type="text" name="credit" value="<c:out value="${ managedFile.credit }"/>"/>
               </td>
           </tr>
           <tr>
               <th>License:</th>
               <td>
                   <input type="text" name="license" value="<c:out value="${ managedFile.license }"/>"/>
               </td>
           </tr>
	   </tbody>
	</table>
	<div class="textright">
	    <input class="form_action" type="submit" value="Save"/>
    </div>
</form>
