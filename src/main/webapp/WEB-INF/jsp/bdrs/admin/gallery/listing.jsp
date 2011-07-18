<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<h1>Galleries</h1>

<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. In non imperdiet nisl. Praesent ipsum massa, ultricies nec congue vel, suscipit id quam. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Ut tellus leo, tincidunt vitae fermentum nec, dictum vel odio. Nunc a ante ac ipsum dignissim adipiscing. Etiam cursus, turpis sollicitudin placerat varius, risus massa blandit sem, ac ultrices felis metus quis nunc. Sed condimentum lorem quis diam rutrum vel tincidunt nisi lobortis. Aliquam a felis erat, ut mollis magna. Etiam laoreet tellus sit amet ante consequat hendrerit. Donec vel elit ac nibh venenatis pulvinar. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Morbi ullamcorper ligula elit. Curabitur dapibus accumsan adipiscing. Proin sit amet tortor tortor. Vivamus nibh purus, ornare sed aliquet nec, adipiscing eget orci. Nam nulla massa, mollis vitae iaculis sed, lobortis non augue. Praesent in odio metus. Quisque hendrerit ligula ut orci dapibus nec hendrerit nisl pretium.
</p>

<p>On this page you may:</p>
<ul>
	<li>Browse your available galleries</li>
	<li>Add a new gallery</li>
	<li>Edit an existing gallery</li>
	<li>Delete an existing gallery</li>
</ul>

<tiles:insertDefinition name="galleryGrid">
       <tiles:putAttribute name="widgetId" value="galleryList"/>
       <tiles:putAttribute name="multiselect" value="false"/>
       <tiles:putAttribute name="scrollbars" value="false" />
       <tiles:putAttribute name="showActions" value="true" />
       <tiles:putAttribute name="editUrl" value="${pageContext.request.contextPath}/bdrs/admin/gallery/edit.htm" />
       <tiles:putAttribute name="deleteUrl" value="${pageContext.request.contextPath}/bdrs/admin/gallery/delete.htm" />
</tiles:insertDefinition>

<div class="buttonpanel textright">
    <input class="form_action" type="button" value="Add Gallery" onclick="window.location = '${pageContext.request.contextPath}/bdrs/admin/gallery/edit.htm';"/>
</div>

