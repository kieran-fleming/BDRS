<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.net.URLEncoder" %>

bdrs_embed = {
    run : function() {
    
        var frame = document.createElement('iframe');
        frame.setAttribute('src', 'http://${ domain }:${ port }${ contextPath }/bdrs/public/embedded/redirect.htm?a=b<c:forEach var="entry" items="${ paramMap }"><jsp:useBean id="entry" type="java.util.Map.Entry"/>&<c:out value="${ entry.key }"/>=<%= URLEncoder.encode(String.valueOf(entry.getValue())) %></c:forEach>');
        frame.setAttribute('width', <c:out value="${ width }"/>);
        frame.setAttribute('height', <c:out value="${ height }"/>);
        frame.setAttribute('style', 'border: none;');
        
        <c:choose>
	        <c:when test="${ targetId == null }">
	            // Insert before the executing script 
	            var scripts = document.getElementsByTagName("script");
                var curScript = scripts[ scripts.length - 1 ];
               	curScript.parentNode.insertBefore(frame, curScript);            
	        </c:when>
	        <c:otherwise>
	            var target = document.getElementById("${ targetId }");
                target.appendChild(frame);
	        </c:otherwise>
        </c:choose>
    }
};

bdrs_embed.run();
