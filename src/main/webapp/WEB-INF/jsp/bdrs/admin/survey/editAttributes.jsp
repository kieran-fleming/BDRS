<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<%@page import="au.com.gaiaresources.bdrs.controller.attribute.RecordPropertyAttributeFormField"%>

<h2>Edit Project: Choose Fields</h2>
<form method="POST" action="${pageContext.request.contextPath}/bdrs/admin/survey/editAttributes.htm">
    <input type="hidden" name="surveyId" value="${survey.id}"/>
    
    <p>
        The table below allows you to define all the attributes that you want to
        see on the data entry form for your project.
    </p>
     
    <div id="attributeContainer" class="input_container">
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
	                <th>Options</th>
	                <th>Hide&nbsp;/<br/>Delete</th>
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
    
    <h2>Edit Project: Choose Census Methods</h2>
    
	<cw:getContent key="admin/editProject/chooseCensusMethods" />
	
	<div class="right">
	    <a id="censusMethodToggle" href="javascript: void(0);">Click here to add a Census Method to your Project</a>
	</div>
	<div class="clear"></div>
	<div class="input_container" id="censusMethodWrapper" style="display:none">
		<table>
			<tbody>
				<tr>
			        <th title="Whether the default 'Standard Taxonomic' census method is provided. If this is false you will need to assign a census method to this survey to create records.">Standard Taxonomic Census Method Provided:</th>
			        <td>
			            <input type="checkbox" name="defaultCensusMethodProvided"
			                <c:if test="${survey.defaultCensusMethodProvided}">
			                    checked="checked"
			                </c:if>
			            />
			        </td>
			    </tr>
			</tbody>
		</table>
		
	    <p>
	        If you have chosen not to have the default 'Standard Taxonomic' census method provided, the first census method in the list shall become the default census method for this survey.
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

<div id="htmlEditorDialog" title="HTML Editor">
    <label>Edit the HTML content that you want to display in the editor below: </label>
    <textarea id="markItUp"></textarea>
</div>

<script type="text/javascript">
    jQuery(function() {
        bdrs.dnd.attachTableDnD('#attribute_input_table');
		bdrs.attribute.setAttributeWeights('#attribute_input_table');
        
        bdrs.dnd.attachTableDnD('#censusMethod_input_table');
		        
        jQuery( "#addCensusMethodDialog" ).dialog({
            width: 'auto',
            modal: true,
            autoOpen: false,
            zIndex: bdrs.MODAL_DIALOG_Z_INDEX,
			resizable: false,
            buttons: {
                "OK": function() {
                    var selected = addCensusMethodGrid_GridHelper.getSelected();                    
                    // single select so...
                    bdrs.censusMethod.addCensusMethodRow("#censusMethod_input_table", selected);
                    jQuery( this ).dialog( "close" );
                },
                Cancel: function() {
                    jQuery( this ).dialog( "close" );
                }
            }
        });
		bdrs.fixJqDialog("#addCensusMethodDialog");

        jQuery( "#htmlEditorDialog" ).dialog({
            width: 'auto',
            modal: true,
            autoOpen: false,
			resizable: false,
            buttons: {
                Cancel: function() {
                    jQuery( this ).dialog( "close" );
                },
                "Clear": function() {
                    jQuery('#markItUp')[0].value = "";
                },
                "OK": function() {
                    bdrs.attribute.saveAndUpdateContent(jQuery("#markItUp")[0]);
                    jQuery( this ).dialog( "close" );
                }
            }
        });
		bdrs.fixJqDialog("#htmlEditorDialog");
		
        jQuery( "#addCensusMethodBtn" )
            .click(function() {
                addCensusMethodGrid_GridHelper.reload();
                jQuery( "#addCensusMethodDialog" ).dialog( "open" );
        });

        jQuery('#markItUp').markItUp(bdrs.admin.myHtmlSettings);

        // Census method expand/collapse
        jQuery("#censusMethodToggle").click(function() {
            var canSee = jQuery("#censusMethodWrapper").css('display') === 'none';
            jQuery("#censusMethodToggle").text(canSee ? "Click here to hide Census Method selection" : "Click here to add a Census Method to your Project");
            jQuery("#censusMethodWrapper").slideToggle();
        });
		
		// trigger the onchange events on all of the type fields to initialise tooltips and validation.
		jQuery(".attrTypeSelect").change();
    });
</script>
