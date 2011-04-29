<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<h1>Test Data Creation</h1>

<p>
    Some notes on test data creation:
</p>
<ul>
    <li>There will always be 1 survey created called 'basic survey'. It's a basic survey with no additional record attributes</li>
    <li>There will always be a user created for each role. They are named 'user', 'poweruser' and 'supervisor'. The password 
    for each of these users is 'password'.</li>
</ul>

<form id="testDataCreation" method="post" onKeyPress="return bdrs.util.preventReturnKeySubmit();">
    <table class="form_table">
        <tbody>
            <tr>
                <th>
                    <label for="taxongroup">Taxon Groups:</label>
                </th>
                <td>
                    <input id="taxongroup" name="taxongroup" type="text" class="validate(positiveInteger)" value="8"/>
                    <span>&nbsp;&#177;&nbsp;</span>
                    <input name="taxongroup_random" type="text" class="validate(positiveInteger)" value="1"/>
                </td>
            </tr>
            <tr>
                <th>
                    <label for="taxongroupattributes">Taxon Group Attributes:</label>
                </th>
                <td>
                    <input id="taxongroupattributes" name="taxongroupattributes" type="checkbox" value="true" checked="checked"/>
                </td>
            </tr>
            <tr>
                <th>
                    <label for="taxa">Taxa per Group:</label>
                </th>
                <td>
                    <input id="taxa" name="taxa" type="text" class="validate(positiveInteger)" value="10"/>
                    <span>&nbsp;&#177;&nbsp;</span>
                    <input name="taxa_random" type="text" class="validate(positiveInteger)" value="2"/>
                </td>
            </tr>
            <tr>
                <th>
                    <label for="taxonprofile">Species Profile:</label>
                </th>
                <td>
                    <input id="taxonprofile" name="taxonprofile" type="checkbox" value="true" checked="checked"/>
                </td>
            </tr>
            <tr>
                <th>
                    <label for="survey">Surveys:</label>
                </th>
                <td>
                    <input id="survey" name="survey" type="text" class="validate(positiveInteger)" value="3"/>
                    <span>&nbsp;&#177;&nbsp;</span>
                    <input name="survey_random" type="text" class="validate(positiveInteger)" value="1"/>
                </td>
            </tr>
            <tr>
                <th>
                    <label for="testusercount">Test User Count:</label>
                </th>
                <td>
                    <input name="testusercount" name"testusercount" type="text" class="validate(positiveInteger)" value="5"/>
                </td>
            </tr>
            <tr>
                <th>
                    <label>Approximate Entity Count:</label>
                </th>
                <td id="entityCount"></td>
            </tr>
        </tbody>
    </table>
    <div class="textright">
        <a id="clearTestData" href="${pageContext.request.contextPath}/bdrs/admin/testdata/clearData.htm" class="delete">Empty</a>
        &nbsp;|&nbsp;
        <input class="form_action" type="submit" value="Create Test Data"/>
    </div>
    
    <script type="text/javascript">
        jQuery(function() {
            var change_handler=  function() {
                var taxongroup = parseInt(jQuery("input[name=taxongroup]").val(), 10) + 
                    parseInt(jQuery("input[name=taxongroup_random]").val(), 10);
                var taxa = parseInt(jQuery("input[name=taxa]").val(), 10) + 
                    parseInt(jQuery("input[name=taxa_random]").val(), 10);
                var taxonprofile = jQuery("input[name=taxonprofile]:checked").length > 0
                var survey = parseInt(jQuery("input[name=survey]").val(), 10) + 
                    parseInt(jQuery("input[name=survey_random]").val(), 10);
                
                var count = "" + ((taxongroup * taxa) + survey) ;
                if(taxonprofile) {
                    count = count + " * (taxon profiles)";
                }
                jQuery("#entityCount").text(count);
            };
        
            jQuery("input").change(change_handler);
            change_handler();
            
            jQuery("#clearTestData").click(function() {
                var msg = 'This will empty this portal of all test data. Are you sure you want to proceed?';
                bdrs.util.confirmExec(msg, function() {
                    var url = jQuery("#clearTestData").attr("href");
                    bdrs.message.set("Please wait while existing test data is deleted");
                    jQuery.post(url, function(data) {
                        window.document.location = '${pageContext.request.contextPath}/authenticated/redirect.htm';
                    });
                });
                return false;
            });
        });
    </script>
</form>