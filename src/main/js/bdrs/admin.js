bdrs.admin = {};

bdrs.admin.adminEditContent =  {
		originalText: "",
		getTextarea: function() {
			return $('#markItUp')[0];
		},
		loadContent: function(key) {
			jQuery.ajax(bdrs.contextPath + "/webservice/content/loadContent.htm", 
      		{
	      		type: "GET",
				data: {key: key}
      		})
      		.success(function(data) { 
      			bdrs.admin.adminEditContent.getTextarea().value = data.content; 
      			bdrs.admin.adminEditContent.originalText = bdrs.admin.adminEditContent.getTextarea().value; 
      		})
    		.error(function(data) { 
			    bdrs.admin.adminEditContent.getTextarea().value = "";
				bdrs.admin.adminEditContent.originalText = bdrs.admin.adminEditContent.getTextarea().value;
			});
		},
		saveContent: function(key) {
			jQuery.ajax(bdrs.contextPath + "/webservice/content/saveContent.htm", 
      		{
	      		type: "POST",
				data: {	key: key, value: bdrs.admin.adminEditContent.getTextarea().value}
      		})
      		.success(function(data) { 
      			bdrs.admin.adminEditContent.originalText = bdrs.admin.adminEditContent.getTextarea().value; 
      			bdrs.message.set("'" + key + "' content saved successfully");
    		})
    		.error(function(data) { bdrs.message.set("Failed to save") });
		},
		isChanged: function() {	  
		  	var mytext = bdrs.admin.adminEditContent.getTextarea().value;
			return mytext != bdrs.admin.adminEditContent.originalText && bdrs.admin.adminEditContent.originalText != "";
		},
		getKey: function() {
			return $("#selectContentToEdit")[0].value;
		},
		getKeyDisplayName: function() {
			return $("#selectContentToEdit")[0].options[$("#selectContentToEdit")[0].selectedIndex].text;
		},
		resetContent: function() {
            var answer = confirm("Are you sure? All content on site will be reset!")
		    if (answer) {
		        window.location = bdrs.contextPath + "/admin/resetContentToDefault.htm";
		    }
		},
		resetCurrentContent: function() {
			var key = bdrs.admin.adminEditContent.getKey();
			if (!key) {
				alert("Please select content to reset");
				return;
			}
            var answer = confirm("Are you sure? The current content will be reset and any changes will be lost!")
            if (answer) {
                window.location = bdrs.contextPath + "/admin/resetContentToDefault.htm?key=" + key;
            }
        },
        clearContent: function() {
        	bdrs.admin.adminEditContent.getTextarea().value = ""; 
  			bdrs.admin.adminEditContent.originalText = bdrs.admin.adminEditContent.getTextarea().value; 
        }
	};

bdrs.admin.myHtmlSettings = {
	    nameSpace:       "html", // Useful to prevent multi-instances CSS conflict
	    onShiftEnter:    {keepDefault:false, replaceWith:'<br />\n'},
	    onCtrlEnter:     {keepDefault:false, openWith:'\n<p>', closeWith:'</p>\n'},
	    onTab:           {keepDefault:false, openWith:'     '},
	    markupSet:  [
	        {name:'Heading 1', key:'1', openWith:'<h1(!( class="[![Class]!]")!)>', closeWith:'</h1>', placeHolder:'Your title here...' },
	        {name:'Heading 2', key:'2', openWith:'<h2(!( class="[![Class]!]")!)>', closeWith:'</h2>', placeHolder:'Your title here...' },
	        {name:'Heading 3', key:'3', openWith:'<h3(!( class="[![Class]!]")!)>', closeWith:'</h3>', placeHolder:'Your title here...' },
	        {name:'Heading 4', key:'4', openWith:'<h4(!( class="[![Class]!]")!)>', closeWith:'</h4>', placeHolder:'Your title here...' },
	        {name:'Heading 5', key:'5', openWith:'<h5(!( class="[![Class]!]")!)>', closeWith:'</h5>', placeHolder:'Your title here...' },
	        {name:'Heading 6', key:'6', openWith:'<h6(!( class="[![Class]!]")!)>', closeWith:'</h6>', placeHolder:'Your title here...' },
	        {name:'Paragraph', openWith:'<p(!( class="[![Class]!]")!)>', closeWith:'</p>'  },
	        {separator:'---------------' },
	        {name:'Bold', key:'B', openWith:'<strong>', closeWith:'</strong>' },
	        {name:'Italic', key:'I', openWith:'<em>', closeWith:'</em>'  },
	        {name:'Stroke through', key:'S', openWith:'<del>', closeWith:'</del>' },
	        {separator:'---------------' },
	        {name:'Ul', openWith:'<ul>\n', closeWith:'</ul>\n' },
	        {name:'Ol', openWith:'<ol>\n', closeWith:'</ol>\n' },
	        {name:'Li', openWith:'<li>', closeWith:'</li>' },
	        {separator:'---------------' },
	        {name:'Picture', key:'P', replaceWith:'<img src="[![Source:!:http://]!]" alt="[![Alternative text]!]" />' },
	        {name:'Link', key:'L', openWith:'<a href="[![Link:!:http://]!]"(!( title="[![Title]!]")!)>', closeWith:'</a>', placeHolder:'Your text to link...' },
	        {separator:'--------------' },
	        {name:'Clean', replaceWith:function(h) { return h.selection.replace(/<(.*?)>/g, "") } },
	        {name:'Preview', call:'preview', className:'preview' }
	    ]
};

bdrs.admin.onSelectContentEditorChange = function() {
  	if (bdrs.admin.adminEditContent.isChanged())
  	{
	  	if (!confirm("Changes will be lost. Do you wish to continue?"))
  		{
  			return;
  		}
  	}
  	var key = bdrs.admin.adminEditContent.getKey();
  	if (key == -1)
  		bdrs.admin.adminEditContent.clearContent();
  	else
  		bdrs.admin.adminEditContent.loadContent(key);
};

$(document).ready(function() {
	// add a handler for the "Save Email Template" button
    $("#submitEditContent").click(function() {
		
		var key = bdrs.admin.adminEditContent.getKey();
        if (!key) {
            alert("Please select content to edit");
            return;
        }
		
        // check the template name
        var templateName;
        if ($('#selectContentToEdit option:selected')[0].value == -1) {
            // saving a new template, verify the name
            // use the Subject area as the default name, but ask the user
            templateName = $('#subject').val();
            $('#saveTemplateName').val(templateName.replace(/ /g, ""));
            $('#savePopup').dialog('open');
        } else {
            // save with the existing template name
            templateName = bdrs.admin.adminEditContent.getKey();
            bdrs.admin.adminEditContent.saveContent(templateName);
        }
    });
});
    
bdrs.admin.contactTree = {
	createAllUsersNode: function() {
		var node1 = bdrs.admin.contactTree.createRootNode("All Users");

		jQuery.ajax({
	        url: '${pageContext.request.contextPath}/webservice/user/getUsers.htm', 
	        success: function(data, textStatus) {
				node1["ChildNodes"] = bdrs.admin.contactTree.createUserNodes("user", data);
	    	},
	    	async: false
	    });
	    return node1;
	},

	createGroupsNode: function() {
		var node2 = bdrs.admin.contactTree.createRootNode("Groups");
	    jQuery.ajax({
	        url: '${pageContext.request.contextPath}/webservice/user/getUsers.htm?queryType=group', 
	        success: function(data, textStatus) {
		         node2["ChildNodes"] = bdrs.admin.contactTree.createGroupProjectNodes("group", data);
		    },
		    async: false
	    });
	    return node2;
	},

	createProjectsNode: function() {
	    var node3 = bdrs.admin.contactTree.createRootNode("Projects");
	    jQuery.ajax({
	        url: '${pageContext.request.contextPath}/webservice/user/getUsers.htm?queryType=project', 
	        success: function(data, textStatus) {
		        node3["ChildNodes"] = bdrs.admin.contactTree.createGroupProjectNodes("project", data);
		    },
		    async: false
	    });
	    return node3;
	},

	createRootNode: function(text) {
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
	},

	createUserNodes: function(idPrefix, users) {
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
	},

	createGroupProjectNodes: function(idPrefix, data) {
		var arr = [];
	    for(var i=0; i<data.length; i++) {
	        var subArr = bdrs.admin.contactTree.createUserNodes(idPrefix + "_user" + i + "-", data[i].users);
	        
	        for(var j=0; j<data[i].groups.length; j++) {
	            var subGrp = bdrs.admin.contactTree.createUserNodes(idPrefix + "_user" + i + "-" + j + "-", data[i].groups[j].users);
	            
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
	},
	createTreeData: function(){
        var node1 = bdrs.admin.contactTree.createAllUsersNode();

        var node2 = bdrs.admin.contactTree.createGroupsNode();

        var node3 = bdrs.admin.contactTree.createProjectsNode();
        
        return [ node1, node2, node3 ];
    }
};

// insert spaces into string where uppercase letters occur
// this is to re-expand the template names into subjects
bdrs.admin.insertSpaces = function(string) {
    var newString = "";
    for (var i = 0; i < string.length; i++) {
        if (string.charAt(i) == string.charAt(i).toUpperCase()) {
            // insert a space
            newString += " ";
        }
        newString += string.charAt(i);
    }
    return newString;
};