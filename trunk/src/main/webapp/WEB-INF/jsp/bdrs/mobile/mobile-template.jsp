<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<tiles:useAttribute id="customJSMobile" name="customJavaScript" classname="java.util.List"/>
<tiles:useAttribute id="customCSS" name="customCss" classname="java.util.List"/>
<tiles:useAttribute id="contentHeader" name="contentheader"/>

<!DOCTYPE html>

<c:choose>
	<c:when test="${manifest != null}">
		<html manifest="${manifest}">
	</c:when>
	<c:otherwise>
		<html>
	</c:otherwise>
</c:choose>
<html>
<head>
    <%
    /* Set session variable with deviceType */
		String[] mobileTags = {
		        "android",
		        "iphone"
		        };
		String userAgent = request.getHeader("user-agent").toLowerCase();
		session.setAttribute("device","netbook");
			for(int i=0; i<mobileTags.length; i++){
		    	if (userAgent.contains(mobileTags[i].toLowerCase())){
		    		session.setAttribute("device", mobileTags[i]);
		    		break;
		    	}
		        
		    }
	%>
	
	<title><tiles:getAsString name="sitename"/> | <tiles:getAsString name="title"/></title>
	
	<meta name="keywords" content="<tiles:getAsString name="metakeywords"/>"/>
	<meta name="description" content="<tiles:getAsString name="metadescription"/>"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0"/>
    
	<script src="${pageContext.request.contextPath}/js/mobile/jquery-1.5.min.js" type="text/javascript"></script>
	<script src="${pageContext.request.contextPath}/js/mobile/jquery.address-1.3.2.min.js" type="text/javascript"></script>
	<script src="${pageContext.request.contextPath}/js/mobile/jquery-ui-1.8.9.custom.min.js" type="text/javascript"></script>
	<script src="${pageContext.request.contextPath}/js/mobile/bdrs.js" type="text/javascript"></script>
	<script src="${pageContext.request.contextPath}/js/ketchup/jquery.ketchup.js" type="text/javascript"></script>
	<script src="${pageContext.request.contextPath}/js/ketchup/jquery.ketchup.messages.js" type="text/javascript"></script>
	<script src="${pageContext.request.contextPath}/js/ketchup/jquery.ketchup.validations.basic.js" type="text/javascript"></script>
	<script src="${pageContext.request.contextPath}/js/mobile/lightbox/scripts/jquery.lightbox.min.js" type="text/javascript"></script>
	<script src="${pageContext.request.contextPath}/js/mobile/bdrs-mobile.js" type="text/javascript"></script>
	<c:forEach var="jsFile" items="${customJSMobile}">
	   <script type="text/javascript" src="${pageContext.request.contextPath}/js/mobile/${jsFile}"></script>
	</c:forEach>
	
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/bdrs/mobile/base.css" type="text/css"/>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/vader/jquery-ui-1.8.9.custom.css" type="text/css"/>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/ketchup/jquery.ketchup.css" type="text/css"/>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/js/mobile/lightbox/styles/jquery.lightbox.min.css" type="text/css"/>
    <c:forEach var="cssFile" items="${customCSS}">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/${cssFile}" type="text/css"/>
    </c:forEach> 
    
	<tiles:insertDefinition name="<%=session.getAttribute(\"device\").toString() %>"></tiles:insertDefinition>
	
</head>

<body>
	<div id="content_container">
 		<tiles:insertAttribute name="mobileheader"/>
	 	<tiles:insertAttribute name="pageNavigation"/>
	 	 
 	<div class="messages">
	    	<noscript>
				<p class='message'>Your browser does not support javaScript or it is disabled. This application needs javaScript.</p>
			</noscript>
			
			
	           	<c:forEach items="${context.messageContents}" var="message">
	            	<p class="message">
	              		<c:out value="${message}"/>
	              	</p>
	           	</c:forEach>
           	
       </div>
      
		<div id="content">
		
			<div id="dialog" title="Basic dialog">
				<p id="dialog_content"></p>
			</div>
			
			<div id="microTemplates"><jsp:include page="microTemplates.jsp"/></div>
             
		    <tiles:insertAttribute name="content"/>
	    </div>
	    
	    <tiles:insertAttribute name="mobilefooter"/>
	</div>
	
</body>

</html>