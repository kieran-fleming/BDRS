<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<h1 class="error_status_code">${statusCode}</h1>
<h2 class="error_description">
	<c:choose>
	    <c:when test="${statusCode == 404}">
	        The page you are looking for cannot be found...
	    </c:when>
	    <c:otherwise>
	        We seem to be having a problem displaying your page.</br>
	        An email has been automatically generated and sent to our engineers.
	    </c:otherwise>
	</c:choose>
</h2>
<p>
    You will be automatically redirected to the <a href="${pageContext.request.contextPath}/authenticated/redirect.htm">home</a> page in 10 seconds.
</p>

<script type="text/javascript">
    timer=setTimeout('window.document.location="${pageContext.request.contextPath}/authenticated/redirect.htm"', 10000)
</script>

