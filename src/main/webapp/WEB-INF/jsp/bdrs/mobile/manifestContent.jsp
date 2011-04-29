<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
#custom images

<c:if test="${imagesList != null }">
<c:forEach var="imgUrl" items="${imagesList}">
../../${imgUrl}
</c:forEach>
</c:if>