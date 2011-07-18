<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="au.com.gaiaresources.bdrs.controller.attribute.RecordPropertyAttributeFormField"%>

<h2>Choose Fields</h2>
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
    
    <h2>Choose Available Census Methods</h2>
    
    <p>
        The table below allows you to define which census methods should be available for your project.
    </p>
    
    <div id="subCensusMethodContainer">
        <div class="textright buttonpanel">
            <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#subCensusMethodContainer', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
            <input id="addCensusMethodBtn" class="form_action" type="button" value="Add Census Method" />
        </div>
        <table id="censusMethod_input_table" class="datatable attribute_input_table">
            <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th>Census Method Name</th>
                    <th>Taxonomic</th>
                    <th>Delete</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${ survey.censusMethods }" var="cm">
                    <tiles:insertDefinition name="censusMethodEditRow">
                        <tiles:putAttribute name="id" value="${cm.id}"/>
                        <tiles:putAttribute name="name" value="${cm.name}"/>
                        <tiles:putAttribute name="taxonomic" value="${cm.taxonomic}" />
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


<!--  DIALOGS GO HERE  ! -->
<div id="addCensusMethodDialog" title="Add Sub Census Methods">
    <tiles:insertDefinition name="censusMethodGrid">
           <tiles:putAttribute name="widgetId" value="addCensusMethodGrid"/>
           <tiles:putAttribute name="multiselect" value="false"/>
           <tiles:putAttribute name="scrollbars" value="true" />
    </tiles:insertDefinition>
</div>

<script type="text/javascript">
    jQuery(function() {
        bdrs.dnd.attachTableDnD('#attribute_input_table');
        
        bdrs.dnd.attachTableDnD('#censusMethod_input_table');
        
        $( "#addCensusMethodDialog" ).dialog({
            width: 'auto',
            modal: true,
            autoOpen: false,
            buttons: {
                "Ok": function() {
                    var selected = addCensusMethodGrid_GridHelper.getSelected();                    
                    // single select so...
                    bdrs.censusMethod.addCensusMethodRow("#censusMethod_input_table", selected);
                    $( this ).dialog( "close" );
                },
                Cancel: function() {
                    $( this ).dialog( "close" );
                }
            }
        });
        
        $( "#addCensusMethodBtn" )
            .click(function() {
                addCensusMethodGrid_GridHelper.reload();
                $( "#addCensusMethodDialog" ).dialog( "open" );
        });
    });
</script>
