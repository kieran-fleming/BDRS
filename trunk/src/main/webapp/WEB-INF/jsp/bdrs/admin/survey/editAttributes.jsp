<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="au.com.gaiaresources.bdrs.controller.attribute.RecordPropertyAttributeFormField"%>

<h1>Choose Fields</h1>
<form method="POST" action="${pageContext.request.contextPath}/bdrs/admin/survey/editAttributes.htm">
    <input type="hidden" name="surveyId" value="${survey.id}"/>
    
    <p>
        The table below allows you to define all the attributes that you want to
        see on the data entry form for your project.
    </p>
     
    <div id="attributeContainer">
	    <div class="textright buttonpanel">
	        <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#attributeContainer', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
	        <input type="button" class="form_action" value="Add Another Field" onclick="bdrs.attribute.addAttributeRow('#attribute_input_table', true, false)"/>
	    </div>
	
	    <table id="attribute_input_table" class="datatable attribute_input_table">
	        <thead>
	            <tr>
	                <th>&nbsp;</th>
	                <th>Description on the Form</th>
	                <th>Name in the Database</th>
	                <th>Field Type</th>
	                <th>Mandatory</th>
	                <th>Scope</th>
	                <th>Options (separated by comma)</th>
	                <th>Delete</th>
	            </tr>
	        </thead>
	        <tbody>
	            <c:forEach items="${ formFieldList }" var="formField">
			        <tiles:insertDefinition name="attributeRow">
			            <tiles:putAttribute name="formField" value="${ formField }"/>
			            <tiles:putAttribute name="showScope" value="true"/>
			        </tiles:insertDefinition>
		        </c:forEach>    
	        </tbody>
	    </table>
    </div>
    
    <div class="textright buttonpanel">
        <input type="submit" class="form_action" value="Save"/>
        <input type="submit" class="form_action" name="saveAndPreview" value="Save And Preview"/>
        <input type="submit" class="form_action" name="saveAndContinue" value="Save And Continue"/>
    </div>

</form>

<script type="text/javascript">
    jQuery(function() {
        bdrs.dnd.attachTableDnD('#attribute_input_table');
    });
</script>
