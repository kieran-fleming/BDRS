<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/markitup/jquery.markitup.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/markitup/sets/html/style.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/markitup/skins/markitup/style.css" />

<h1>Edit Website Content</h1>

<sec:authorize ifAnyGranted="ROLE_ADMIN">
<div>
    <button onclick="adminEditContent.resetContent()">Reset all content to default</button>
</div>
</sec:authorize>

<div class="input_container">
<div>
	<p>This page is where you can edit a range of pages throughout the web site.  To do this, you select the area you want to change, and then you can modify the text using the editor that is present in the page below.  These changes will be reflected immediately across the web site when you save them.</p>
	<label>Select the area of the content you would like to edit: </label>
	<select id="selectContentToEdit">
	<c:forEach items="${keys}" var="k">
		<option value="${k}">${k}</option>
	</c:forEach>
	</select>
</div>
<textarea id="markItUp"></textarea>
<div class="markItUpSubmitButton buttonpanel textright">
    <input id="resetContent" type="button" class="form_action"  value="Reset Current Content Default" onclick="adminEditContent.resetCurrentContent()"/>
    <input id="submitEditContent" type="button" class="form_action"  value="Save" />
</div>
</div>

<script type="text/javascript">

	adminEditContent =  {
		originalText: "",
		loadContent: function(key) {
		    jQuery.ajax(bdrs.contextPath + "/webservice/content/loadContent.htm", 
      		{
	      		type: "GET",
				data: {key: key}
      		})
      		.success(function(data) { 
      			adminEditContent.getTextarea().value = data.content; 
      			adminEditContent.originalText = adminEditContent.getTextarea().value; 
      		})
    		.error(function(data) { bdrs.message.set("error loading content to edit"); });
		},
		saveContent: function(key) {
			jQuery.ajax(bdrs.contextPath + "/webservice/content/saveContent.htm", 
      		{
	      		type: "POST",
				data: {	key: key, value: adminEditContent.getTextarea().value}
      		})
      		.success(function(data) { 
      			adminEditContent.originalText = adminEditContent.getTextarea().value; 
      			bdrs.message.set("'" + adminEditContent.getKeyDisplayName()+ "' content saved successfully");
    		})
    		.error(function(data) { bdrs.message.set("Failed to save") });
		},
		isChanged: function() {	  
		  	var mytext = adminEditContent.getTextarea().value;
			return mytext != adminEditContent.originalText && adminEditContent.originalText != "";
		},
		getKey: function() {
			return $("#selectContentToEdit")[0].value;
		},
		getKeyDisplayName: function() {
			return $("#selectContentToEdit")[0].options[$("#selectContentToEdit")[0].selectedIndex].text;
		},
		getTextarea: function() {
			return $('#markItUp')[0];
		},
		resetContent: function() {
            var answer = confirm("Are you sure? All content on site will be reset!")
		    if (answer) {
		        window.location = bdrs.contextPath + "/admin/resetContentToDefault.htm";
		    }
		},
		resetCurrentContent: function() {
            var answer = confirm("Are you sure? The current content will be reset and any changes will be lost!")
            if (answer) {
                window.location = bdrs.contextPath + "/admin/resetContentToDefault.htm&key=" + getKey();
            }
        }
	};
	
    jQuery(document).ready(function() {
      
      myHtmlSettings = {
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
	        {separator:'---------------' },
	        {name:'Clean', replaceWith:function(h) { return h.selection.replace(/<(.*?)>/g, "") } },
	        {name:'Preview', call:'preview', className:'preview' }
	    	]
		};

      	$('#markItUp').markItUp(myHtmlSettings);
      	
    	adminEditContent.loadContent(adminEditContent.getKey());
   
      	$("#selectContentToEdit").change(function() {
		  	if (adminEditContent.isChanged())
		  	{
			  	if (!confirm("Changes will be lost. Do you wish to continue?"))
		  		{
		  			return;
		  		}
		  	}
		  	adminEditContent.loadContent(adminEditContent.getKey());
		});
		
		$("#submitEditContent").click(function() {
			adminEditContent.saveContent(adminEditContent.getKey());
		});
		
   });
 
</script>

