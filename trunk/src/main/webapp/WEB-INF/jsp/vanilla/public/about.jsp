<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<h1>About</h1>
	<cw:getContent key="public/about" />

<div class="input_container">
    <table class="link_table" style="color: grey;">
        <tbody>
            <tr>
                <td style="text-align: center; border-style: none;">
                    <img src="${pageContext.request.contextPath}/images/bdrs/atlasoflivingaust.png"/>
                </td>
                <td style="padding: 10px; border-style: none; color: grey;">
                    <p>This portal is running the <a href="http://code.google.com/p/ala-citizenscience/">Biological Data Recording System</a>, software developed by the <a href="http://www.ala.org.au/">Atlas of Living Australia</a>.</p>
                    
                    <p>The Atlas of Living Australia is a collaborative, national project focused on making information
                    on Australia&apos;s biodiversity more accessible and useable online. It is a joint project between CSIRO,
                    state herbaria and museums, the Australian Government Departments of Environment, Water, Heritage
                    and the Arts (DEWHA) and Agriculture, Fisheries and Forestry (DAFF), and two Australian
                    universities.</p>
                    <a href="http://www.ala.org.au/">www.ala.org.au</a>
                </td>
            </tr>
        </tbody>
    </table>
</div>

<div>
<h3>Software Information</h3>
<p>
This portal runs the <a href="http://code.google.com/p/ala-citizenscience/">Biological Data Recording System</a>, 
and this site is running the core at version ${coreRevisionNumber}
<c:if test="${implRevisionNumber != null}">and the implementation is at revision ${implRevisionNumber}</c:if>.  
</p>
<p>
For more information on the software please go to the main Google Code <a href="http://code.google.com/p/ala-citizenscience/">repository</a>.
</p>
</div>

<!-- 
Only for those that are in the know.
<div>${coreRevisionInfo}</div>
<c:if test="${implRevisionInfo != null}">
<div>${implRevisionInfo}</div>
</c:if>
-->
