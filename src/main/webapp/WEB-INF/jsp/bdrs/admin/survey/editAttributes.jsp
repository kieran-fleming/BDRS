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
    
    <div id="moderationSettingsLink" style="display:none;margin-top:13px;" class="right">
        <a id="moderationSettingsToggle" href="javascript: void(0);">Click here to show the moderation email settings section.</a>
    </div>
		
    <div id="moderationSettings" style="display:none">
        <h2>Edit Project: Moderation Email Settings</h2>
    
		<cw:getContent key="admin/editProject/editModerationEmailSettings" />
		
		<div class="input_container" id="moderationEmailWrapper" >
			<div style="margin-bottom:5px;">
				<label for="ownerToModeratorEmail" class="strong" title="The email to be sent from the record owner to a moderator on create/update of a record.">Email to send from owner:</label>
				<select name="ownerToModeratorEmail" id="ownerToModeratorEmail">
					<option value="-1">--select existing template--</option>
	                <c:forEach items="${emailTemplates}" var="k">
	                    <option value="${k}" 
	                    <c:if test="${ownerToModeratorEmail == k}">selected="true"</c:if>
	                    >${k}</option>
	                </c:forEach>
				</select>
				<a href="javascript: openEmailDialog('#ownerToModeratorEmail');">Edit email template.</a>
			</div>
			<div class="clear"></div>
	        <div>
		        <label for="moderatorToOwnerEmail" class="strong" title="The email to be sent from the moderator to the record owner on moderation of a record.">Email to send from moderator:</label>
	            <select name="moderatorToOwnerEmail" id="moderatorToOwnerEmail">
	            	<option value="-1">--select existing template--</option>
	                <c:forEach items="${emailTemplates}" var="k">
	                    <option value="${k}"
	                    <c:if test="${moderatorToOwnerEmail == k}">selected="true"</c:if>
	                    >${k}</option>
	                </c:forEach>
	            </select>
		        <a href="javascript: openEmailDialog('#moderatorToOwnerEmail');">Edit email template.</a>
	        </div>
	    </div>
    </div>
	<div class="clear"></div>
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

<div id="emailEditorDialog" title="Email Editor">
    <p>Edit the email content in the editor and enter a new name for the template below 
    if you would like to save this content as a new template.</p>
    <textarea id="markItUpEmail"></textarea>
    <label>Enter the name you would like to save the template as: </label>
    <input id="saveTemplateName" style="width:53%;"></input>
</div>

<script type="text/javascript">
    var openEmailDialog = function(selector) {
        jQuery( "#emailEditorDialog" ).data('selector',selector).dialog("open");
    };

    var insertAndSelectEmailOption = function(emailTemplateSelectElem, emailTemplateKey) {
        var foundMatch = false;
        emailTemplateSelectElem.find('option').each(function() {
			if (!foundMatch) {
                // find the item in the list or find the item that will be after this one
	            var thisVal = jQuery(this).val();
	            if (thisVal === emailTemplateKey) {
	                emailTemplateSelectElem.val(thisVal);
	                foundMatch = true;
	            } else if (thisVal > emailTemplateKey) {
	                // if the item is not found, insert the new item
	                var newOption = jQuery('<option></option>');
	                newOption.val(emailTemplateKey);
	                newOption.text(emailTemplateKey);
	                newOption.insertBefore(this);
	                emailTemplateSelectElem.val(emailTemplateKey);
	                foundMatch = true;
	            }
            }
        });
    };


    var toggleModerationSettings = function(modSettingsSelector, modSettingsLinkSelector) {
        var canSee = jQuery(modSettingsSelector).css('display') === 'none';
        jQuery(modSettingsLinkSelector).text(canSee ? "Click here to hide the moderation email settings section." : 
                                                          "Click here to show the moderation email settings section.");
        jQuery(modSettingsSelector).slideToggle();
    };
    
    var rowScopeChanged = function(event) {
        var index = event.data.index;
        var bNewRow = event.data.bNewRow;
        
        var changedElement = jQuery(event.currentTarget);
        var newScopeCode = changedElement.val();
        var attrScope = bdrs.model.taxa.attributeScope.value[newScopeCode];
        
        if (attrScope.isModerationScope()) {
            // show the moderation email settings div
            jQuery("#moderationSettingsLink").css("display","block");
            var isVisible = jQuery("#moderationSettings").css('display') !== 'none';
            if (!isVisible) {
            	toggleModerationSettings("#moderationSettings", "#moderationSettingsLink a");
            }
        } else {
            var isOneModScope = false;
            // if there are no moderation attributes, hide the email settings
            changedElement.parents("table").find(".attrScopeSelect").each(function() {
                var scope = bdrs.model.taxa.attributeScope.value[jQuery(this).val()];
                isOneModScope |= scope.isModerationScope();
            });
            
            if (!isOneModScope) {
                // hide the moderation email settings div
                jQuery("#moderationSettingsLink").css("display","none");
                var isVisible = jQuery("#moderationSettings").css('display') !== 'none';
                if (isVisible) {
                    toggleModerationSettings("#moderationSettings", "#moderationSettingsLink a");
                }
            }
        }
    };
    
    jQuery(function() {
        bdrs.admin.adminEditContent.setTextArea('#markItUpEmail');
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

        // create the email dialog
        jQuery( "#emailEditorDialog" ).dialog({
            width: '735px',
            modal: true,
            autoOpen: false,
			resizable: false,
            buttons: {
                Cancel: function() {	
                    jQuery( this ).dialog( "close" );
                },
                "Clear": function() {
                    jQuery('#markItUpEmail')[0].value = "";
                    jQuery('#saveTemplateName').val("");
                },
                "Save": function() {
                    var emailTemplateKey = "email/" + jQuery('#saveTemplateName').val();
                    // warn the user that they are overwriting content that exists if the 
                    // key is alreay present
                    var save = true;
                    var emailTemplateSelector = jQuery(jQuery(this).data('selector'));
                    var existingKey = jQuery(emailTemplateSelector).find("option[value='"+emailTemplateKey+"']");
                    if (existingKey.length >= 1) {
                    	save = confirm("An email template with that name already exists.  Are you sure you want to replace that content with your changes?")
                    }
                    if (save) {
	                	bdrs.admin.adminEditContent.saveContent(emailTemplateKey);
	                    // set the select box for the link that opened the dialog to 
	                    // the saved content
	                    insertAndSelectEmailOption(emailTemplateSelector, emailTemplateKey);
	                    jQuery( this ).dialog( "close" );
        		    }
                }
            },
            open: function(event, ui) {
                var emailTemplateSelector = jQuery(jQuery(this).data('selector'));
                var emailTemplate = emailTemplateSelector.val();
                if (emailTemplate !== -1) {
                    // load the content to the html editor
        			bdrs.admin.adminEditContent.loadContent(emailTemplate);
                    jQuery('#saveTemplateName').val(emailTemplate.substr(6));
                }
                
            }
        });
		bdrs.fixJqDialog("#emailEditorDialog");
		
     	// add the variable selector to myHtmlSettings before adding them to the markup editor
    	bdrs.admin.myHtmlSettings.markupSet.push(
    			{separator:'---------------' }
    	);
    	bdrs.admin.myHtmlSettings.markupSet.push(
                { name:'Insert Variable', className:'myVariableDrop',
                    dropMenu: [
                       { name:'to.username', className:'myDropLink', replaceWith:'\$\{to.name\}'},
                       { name:'to.first name', className:'myDropLink', replaceWith:'\$\{to.firstName\}'},
                       { name:'to.last name', className:'myDropLink', replaceWith:'\$\{to.lastName\}'},
                       { name:'to.email address', className:'myDropLink', replaceWith:'\$\{to.emailAddress\}'},
                       { name:'from.username', className:'myDropLink', replaceWith:'\$\{from.name\}'},
                       { name:'from.first name', className:'myDropLink', replaceWith:'\$\{from.firstName\}'},
                       { name:'from.last name', className:'myDropLink', replaceWith:'\$\{from.lastName\}'},
                       { name:'from.email address', className:'myDropLink', replaceWith:'\$\{from.emailAddress\}'},
                       { name:'homepage link', className:'myDropLink', replaceWith:'\<a href=\"\$\{bdrsApplicationUrl\}\/home.htm\"\>link\<\/a\>'}
                      ]
                });

    	jQuery('#markItUpEmail').markItUp(bdrs.admin.myHtmlSettings);
        
     	// Moderation Email Settings expand/collapse
        jQuery("#moderationSettingsToggle").click(function() {
            toggleModerationSettings("#moderationSettings", "#moderationSettingsLink a");
        });
        
        // Census method expand/collapse
        jQuery("#censusMethodToggle").click(function() {
            var canSee = jQuery("#censusMethodWrapper").css('display') === 'none';
            jQuery("#censusMethodToggle").text(canSee ? "Click here to hide Census Method selection" : "Click here to add a Census Method to your Project");
            jQuery("#censusMethodWrapper").slideToggle();
        });
		
		// trigger the onchange events on all of the type fields to initialise tooltips and validation.
		jQuery(".attrTypeSelect").change();
		jQuery(".attrScopeSelect").change();
    });
</script>
