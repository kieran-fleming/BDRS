<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.net.URLEncoder" %>

<html class="embed">
    <head>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cc/yui3-reset.css" type="text/css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cc/yui3-fonts.css" type="text/css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cc/yui3-base.css" type="text/css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/bdrs/public/embedded/bdrs-embed.css??a=b<c:forEach var="entry" items="${ paramMap }"><jsp:useBean id="entry" type="java.util.Map.Entry"/>&<c:out value="${ entry.key }"/>=<%= URLEncoder.encode(entry.getValue().toString()) %></c:forEach>" type="text/css"/>
    </head>
    <body>
        <tiles:insertAttribute name="content"/>
        <jsp:useBean id="showFooter" type="java.lang.String" scope="request"/>
        <c:if test="<%= Boolean.parseBoolean(showFooter) %>">
	        <div class="right" style="position:fixed; bottom: 0px;">
	            <a href="http://${ domain }:${ port }${ contextPath }/home.htm" target="_target">
	                Biological Data Recording System
	            </a>
	        </div>
        </c:if>
    </body>
</html>