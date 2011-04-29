<%@ page contentType="text/javascript" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>

var contextPath ="${pageContext.request.contextPath}";
var deviceType = "${deviceType}";

<jsp:include page="/js/mobile/bdrs-mobile-base.js"/>
<jsp:include page="/js/mobile/websql_database.js"/>
<jsp:include page="/js/mobile/bdrs-mobile-sync.js"/>
<jsp:include page="/js/mobile/detect-browser.js"/>
<jsp:include page="/js/mobile/templating-resig.js"/>
<jsp:include page="/js/mobile/titlecaps-resig.js"/>
<jsp:include page="/js/mobile/application-caching.js"/>
<jsp:include page="/js/mobile/bdrs-mobile-views.js"/>

<jsp:include page="/js/mobile/bdrs-mobile-test.js"/>