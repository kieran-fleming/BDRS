<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<h1>Edit Theme Element</h1>

<cw:getContent key="root/theme/edit/advanced/editFile" />

<form id="themeElementForm" method="POST" action="">
    <input type="hidden" name="themePk" value="${ editTheme.id }"/>
    <input type="hidden" name="filename" value="${ themeFileName }"/>
    <textarea name="themeFileContent">${ content }</textarea>
    
    <div class="textright buttonpanel">
        <a href="${pageContext.request.contextPath}/bdrs/root/theme/edit.htm?portalId=${ editTheme.portal.id }&themeId=${ editTheme.id }">Cancel</a>
        <span>&nbsp;|&nbsp;</span>
        <input class="form_action" type="submit" value="Revert Changes" name="revert" onsubmit="return confirm('Reverting this theme will delete all local changes. Do you wish to continue?');"/>
        <input class="form_action" type="submit" value="Save Template"/>
    </div>
</form>


<script type="text/javascript">
    var editorSettings = {
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
            ]
        };

        jQuery('textarea').markItUp(editorSettings);
</script>