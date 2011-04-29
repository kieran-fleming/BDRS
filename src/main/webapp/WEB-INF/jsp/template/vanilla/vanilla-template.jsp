<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<tiles:useAttribute id="customJS" name="customJavaScript" classname="java.util.List"/>
<tiles:useAttribute id="customCSS" name="customCss" classname="java.util.List"/>
<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>
<tiles:importAttribute name="maps"/>

<!DOCTYPE html>

<html>
    <head>
        <title><tiles:getAsString name="siteName"/> | <tiles:getAsString name="title"/></title>
        <meta name="keywords" content="<tiles:getAsString name="metaKeywords"/>"/>
        <meta name="description" content="<tiles:getAsString name="metaDescription"/>"/>
        <!-- Reset all browser specific styles -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/vanilla/yui3-reset.css" type="text/css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/vanilla/yui3-fonts.css" type="text/css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/vanilla/yui3-base.css" type="text/css">

        <link type="text/css" href="${pageContext.request.contextPath}/css/south-street/jquery-ui-1.8.11.custom.css" rel="stylesheet" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/vanilla/base.css" type="text/css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/ketchup/jquery.ketchup.css" type="text/css"/>
        <c:forEach var="cssFile" items="${customCSS}">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/${cssFile}" type="text/css"/>
        </c:forEach>

        <script src="${pageContext.request.contextPath}/js/jquery-1.5.1.min.js" type="text/javascript"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.8.11.custom.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/ketchup/jquery.ketchup.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/ketchup/jquery.ketchup.messages.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/ketchup/jquery.ketchup.validations.basic.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.cj-simple-slideshow.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.tablednd_0_5.js"></script>
        
        <link rel="stylesheet" href="${pageContext.request.contextPath}/js/colorpicker/css/colorpicker.css" type="text/css"/>
        <script src="${pageContext.request.contextPath}/js/colorpicker/js/colorpicker.js" type="text/javascript"></script>
        
        <c:if test="${maps == true}">
            <script src="${pageContext.request.contextPath}/js/ol/OpenLayers.js" type="text/javascript"></script>
            <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;&amp;sensor=false&amp;key=${bdrs.google.maps.key}" type="text/javascript"></script>
            <script src="${pageContext.request.contextPath}/js/BdrsCluster.js" type="text/javascript"></script>
        </c:if>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/bdrs.js"></script>
        <script type="text/javascript"> 
            jQuery(function () {
                bdrs.contextPath = '${pageContext.request.contextPath}'; 
                bdrs.ident = '<%= context.getUser() == null ? "" : context.getUser().getRegistrationKey() %>';
                bdrs.dateFormat = 'dd M yy';
                bdrs.init();
                
                jQuery('form').ketchup();
            });
            
        </script>
        <c:forEach var="jsFile" items="${customJS}">
            <script type="text/javascript" src="${pageContext.request.contextPath}/js/${jsFile}"></script>
        </c:forEach>
    </head>

    <body>
        <div class="wrapper">
			<sec:authorize ifAnyGranted="ROLE_ADMIN,ROLE_USER">
			    <a id="signOut" href="${pageContext.request.contextPath}/logout">Sign Out</a>
			</sec:authorize>
            <tiles:insertAttribute name="header"/>
            <jsp:include page="menu.jsp"/>
            <div class="contentwrapper" id="contentwrapper">
                <div class="messages">
                    <c:forEach items="${context.messageContents}" var="message">
                       <p class="message"><c:out value="${message}"/></p>
                    </c:forEach>
                </div>
                <div class="content" id="content">
                    <tiles:insertAttribute name="content"/>
                </div>
            </div>
             <tiles:insertAttribute name="footer"/>
        </div>
    </body>
</html>
