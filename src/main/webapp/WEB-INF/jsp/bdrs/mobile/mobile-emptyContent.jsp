<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!--customJavascript see climatewatch-tiles.xml probably not used-->
<tiles:useAttribute id="customJSMobile" name="customJavaScriptMobile" classname="java.util.List"/>
<!--check doctype for html4/5-->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html>
    <head>
    
        <title>BYS | <tiles:getAsString name="title"/></title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/default.css" type="text/css"></link>
        <c:forEach var="jsFile" items="${customJSMobile}">
            <script language="javascript" src="${pageContext.request.contextPath}/js/mobile/${jsFile}"></script>
        </c:forEach>
    </head>
    <body>
    	<tiles:insertAttribute name="content"/>
    </body>
</html>	  