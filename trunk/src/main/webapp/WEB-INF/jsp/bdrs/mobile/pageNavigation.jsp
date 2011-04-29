<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:if test="${context.authenticated}">


<div id="pageNavigation">
	<img class="listItemIcon" src="${pageContext.request.contextPath}/images/bdrs/mobile/40x40_Back_1.png" alt="back" onclick="history.go(-1);"/>
	<a href="#/">
		<img id="homeIcon" src="${pageContext.request.contextPath}/images/bdrs/mobile/40x40_Home_NoBorder.png" alt=""/>
	</a>
	<div id="pageNavigationText" style="margin: auto; text-align:center;">
		<a href="javascript:setNavigationBack()" id="backLink">
		<span id="previousPage"></span>
		<br/>
		<span id="currentPage"></span>
		</a>
	</div>
</div>



</c:if>



			
			