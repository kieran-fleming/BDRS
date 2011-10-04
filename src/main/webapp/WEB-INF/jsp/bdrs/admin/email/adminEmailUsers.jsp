<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/markitup/jquery.markitup.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.wdTree-1.4/wdTree/src/Plugins/jquery.tree.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bdrs/admin.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/markitup/sets/html/style.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/markitup/skins/bdrs-email/style.css" />
<link href="${pageContext.request.contextPath}/css/wdTree/tree.css" rel="stylesheet" type="text/css" />

<%@page import="au.com.gaiaresources.bdrs.model.user.User"%>
<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>

<h1>Email Users</h1>

<div class="input_container">
    <div>
	    <cw:getContent key="admin/emailUsers" />
	</div>
	<div id="emailHeader">
        <table>
           <tr>
               <td><input type="button" class="right" id="selectUsers" value="To:" onclick="showPopupDialog()"></input></td>
               <td><input id="toUsers"></input></td>
               <td></td>
           </tr>
           <tr>
              <td><label class="right" for="subject">Subject: </label></td>
                    <td><input id="subject"></input></td>
              <td>
                <div class="right">
                    <label>Select email template: </label>
                    <select id="selectContentToEdit" onchange="changeTemplate()">
                    <option value="-1">--select existing template--</option>
                    <c:forEach items="${keys}" var="k">
                        <option value="${k}">${k}</option>
                    </c:forEach>
                    </select>
                </div>
              </td> 
            </tr>
        </table>
	</div>
    <textarea id="markItUp"></textarea>
	<div class="markItUpSubmitButton buttonpanel textright">
	    <input id="resetContent" type="button" class="form_action"  value="Clear Contents" onclick="bdrs.admin.adminEditContent.clearContent()"/>
	    <input id="submitEditContent" type="button" class="form_action"  value="Save as Email Template" />
	    <input id="submitEmail" type="button" class="form_action"  value="Send Message" />
	</div>
	
	<div id="popupDialog" title="Contact Selector">
        <label>Select contacts to email from the list below: </label>
        <div class="tree_container">
	        <div id="tree">
	            
	        </div>
        </div>
        <div id="buttonPanel" class="markItUpSubmitButton buttonpanel textright">
            <input id="doneSelectingContacts" type="button" class="form_action"  value="Done" onclick="addAddressees()"/>
        </div>
    </div>
    <div id="savePopup" title="Save Template As...">
        <label>Enter the name you would like to save the template as: </label>
        <input id="saveTemplateName"></input>
        <div id="buttonPanel" class="markItUpSubmitButton buttonpanel textright">
            <input id="saveTemplate" type="button" class="form_action"  value="Save"/>
        </div>
    </div>
</div>

<script type="text/javascript">
    jQuery(document).ready(function() {
		// set the popups to only open on command, not page open
        $('#savePopup').dialog({ zIndex: bdrs.MODAL_DIALOG_Z_INDEX, autoOpen: false });
        $('#popupDialog').dialog({ zIndex: bdrs.MODAL_DIALOG_Z_INDEX, autoOpen: false });
		
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
                       { name:'homepage link', className:'myDropLink', replaceWith:'\<a href=\"\$\{bdrs.application.url\}\/home.htm\"\>'}
                      ]
                });

    	$('#markItUp').markItUp(bdrs.admin.myHtmlSettings);
    	
        $("#saveTemplate").click(function() {
            // use the template name from the popup dialog
            var templateName = "email/" + $('#saveTemplateName').val();
            $('#savePopup').dialog('close');
            bdrs.admin.adminEditContent.saveContent(templateName);
        });

        $("#submitEmail").click(function() {
            sendEmail();
        });
    });

    changeTemplate = function() {
        // set the subject to the template name less the "email/" prefix
        var selTemplate = bdrs.admin.adminEditContent.getKey();
        $("#subject").val(bdrs.admin.insertSpaces(selTemplate.substr(6)));
        bdrs.admin.onSelectContentEditorChange();
    }

    // show the contacts dialog and set the width & height here to override
    // automatic dialog settings
    showPopupDialog = function() {
    	$('#popupDialog').dialog('open');
    	$('#popupDialog').dialog({ height: 420 });
        $('#popupDialog').dialog({ width: 400 });
    }

    addAddressees = function() {
        // get all of the selected addresses and add them to the toUsers input area
        var s = $("#tree").getCheckedNodes();
        if(s !=null) {
            // remove any non-email addresses from the list
            for (var i = s.length-1; i >= 0; i--) {
                if (s[i].indexOf("@") == -1) {
                    s.splice(i,1);
                }
            }
            s = s.join(", ");
        }
        else
            s = "";
        $('#toUsers').val(s);
        
        // close the dialog
        $('#popupDialog').dialog('close');
    }

    sendEmail = function() {
        jQuery.ajax(bdrs.contextPath + "/admin/sendMessage.htm", 
        {
            type: "POST",
            data: { to: $('#toUsers').val(), subject: $('#subject').val(), 
                    content: bdrs.admin.adminEditContent.getTextarea().value}
        })
        .success(function(data) { 
            bdrs.message.set("Email sent successfully");
        })
        .error(function(data) { bdrs.message.set("Failed to send email") });
    }
    
    createTreeData = function(){
        var node1 = createAllUsersNode();

        var node2 = createGroupsNode();

        var node3 = createProjectsNode();
        
        return [ node1, node2, node3 ];
    }

    createAllUsersNode = function() {
    	var node1 = createRootNode("All Users");

        jQuery.ajax({
            url: '${pageContext.request.contextPath}/webservice/user/getUsers.htm', 
            success: function(data, textStatus) {
             node1["ChildNodes"] = createUserNodes("user", data);
        },
        async: false
        });
        return node1;
    }

    createGroupsNode = function() {
    	var node2 = createRootNode("Groups");
        jQuery.ajax({
            url: '${pageContext.request.contextPath}/webservice/user/getUsers.htm?queryType=group', 
            success: function(data, textStatus) {
             
             node2["ChildNodes"] = createGroupProjectNodes("group", data);
        },
        async: false
        });
        return node2;
    }

    createProjectsNode = function() {
        var node3 = createRootNode("Projects");
        jQuery.ajax({
            url: '${pageContext.request.contextPath}/webservice/user/getUsers.htm?queryType=project', 
            success: function(data, textStatus) {
             
            node3["ChildNodes"] = createGroupProjectNodes("project", data);
        },
        async: false
        });
        return node3;
    }
    
    createRootNode = function(text) {
    	var root = {
                "id" : text,
                "text" : text,
                "value" : text,
                "showcheck" : true,
                complete : true,
                "isexpand" : false,
                "checkstate" : 0,
                "hasChildren" : true
              };
        return root;
    }

    createUserNodes = function(idPrefix, users) {
    	var arr = [];
        for(var i=0; i<users.length; i++) {
              arr.push( {
                "id" : idPrefix + i,
                "text" : users[i].firstName + " " + users[i].lastName + " (" + users[i].emailAddress + ")",
                "value" : users[i].emailAddress,
                "showcheck" : true,
                complete : true,
                "isexpand" : false,
                "checkstate" : 0,
                "hasChildren" : false
              });
        }
        return arr;
    }

    createGroupProjectNodes = function(idPrefix, data) {
    	var arr = [];
        for(var i=0; i<data.length; i++) {
            var subArr = createUserNodes(idPrefix + "_user" + i + "-", data[i].users);
            
            for(var j=0; j<data[i].groups.length; j++) {
                var subGrp = createUserNodes(idPrefix + "_user" + i + "-" + j + "-", data[i].groups[j].users);
                
                subArr.push( {
                    "id" : "subgroup" + j,
                    "text" : data[i].groups[j].name,
                    "value" : data[i].groups[j].name,
                    "showcheck" : true,
                    complete : true,
                    "isexpand" : false,
                    "checkstate" : 0,
                    "hasChildren" : true,
                    "ChildNodes" : subGrp
                   });
             }
              arr.push( {
                "id" : idPrefix + i,
                "text" : data[i].name,
                "value" : data[i].name,
                "showcheck" : true,
                complete : true,
                "isexpand" : false,
                "checkstate" : 0,
                "hasChildren" : true,
                "ChildNodes" : subArr
              });
              
        }
        return arr;
    }
    
    var userAgent = window.navigator.userAgent.toLowerCase();
    $.browser.msie8 = $.browser.msie && /msie 8\.0/i.test(userAgent);
    $.browser.msie7 = $.browser.msie && /msie 7\.0/i.test(userAgent);
    $.browser.msie6 = !$.browser.msie8 && !$.browser.msie7 && $.browser.msie && /msie 6\.0/i.test(userAgent);
    function load() {
        var o = { showcheck: true
        };
        o.cbiconpath = "${pageContext.request.contextPath}/images/wdTree/icons/";
        o.data = createTreeData();
        $("#tree").treeview(o);
    }   
    if( $.browser.msie6)
    {
        load();
    }
    else{
        $(document).ready(load);
    }
</script>
