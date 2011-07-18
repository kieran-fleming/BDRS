<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="statistics">
    <h3>Latest Statistics</h3>
    <table>
        <tbody>
           <tr>
               <th>Number of users</th>
               <td class="boldtext"><c:out value="${userCount}"/></td>
           </tr>
            <tr>
                <th>Total number of records</th>
                <td class="boldtext"><c:out value="${recordCount}"/></td>
            </tr>
            <tr>
                <th>Number of species recorded</th>
                <td class="boldtext"><c:out value="${uniqueSpeciesCount}"/></td>
            </tr>
        </tbody>
    </table>
    <c:if test="${not empty latestRecord}">
    <p>
        The last sighting was a <c:out value="${latestRecord.species.commonName}"/>,  
        <span class="scientificName"><c:out value="${latestRecord.species.scientificName}"/></span>
        in the group <c:out value="${latestRecord.species.taxonGroup.name}"/>.
    </p>
    </c:if>
</div>
