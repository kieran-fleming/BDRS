<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:if test="${context.authenticated}">

	<div id="mobileSubHeader">
	
		<div id="pageContentHeader">${content_header}</div>
		<div id="help_btn" class="subHeader_btn" >
			<a href="#mode=getHelp">?</a>
		</div>
		
		<c:if test="${content_header == 'HOME' && offline != 'true'}">
			<div class="subHeader_btn">
				<a href="${pageContext.request.contextPath}/logout" >LOG OUT</a>
			</div>
		</c:if>
  
  	<div id="home_btn" class="subHeader_btn">
       		<a href="${pageContext.request.contextPath}/bdrs/mobile/home.htm"> home </a>
        </div>
  
       	<div id="back_btn" class="subHeader_btn">
       		<a href="javascript:history.go(-1)"> &#60; </a>
        </div>
        
	</div>
	
</c:if>
