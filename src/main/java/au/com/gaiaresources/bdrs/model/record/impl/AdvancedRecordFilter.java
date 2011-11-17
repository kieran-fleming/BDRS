package au.com.gaiaresources.bdrs.model.record.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import au.com.gaiaresources.bdrs.db.impl.SortOrder;
import au.com.gaiaresources.bdrs.db.impl.SortingCriteria;

/**
 * Provides an advanced filtering system for making record queries.
 * Joins attributes, location, user, survey, species, and censusMethod
 * tables, allowing for the return and filtering of more than just basic 
 * record information.
 * 
 * @author stephanie
 *
 */
public class AdvancedRecordFilter extends AbstractRecordFilter {

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.AbstractRecordFilter#getOrderingClause(java.util.List)
     */
    public String getOrderingClause(List<SortingCriteria> sortCriteria) {
        StringBuilder builder = new StringBuilder();
        if(sortCriteria.isEmpty()) {
            builder.append("order by record.when desc");
        } else {
            // build up our ordering which may depend on joined tables
            builder.append("order by");
            for (SortingCriteria sc : sortCriteria) {
                builder.append(String.format(" %s ", sc.getColumn()));
                builder.append(sc.getOrder() == SortOrder.ASCENDING ? "asc" : "desc");
            }
        }
        // Always order by pk also to stabilise the ordering.
        builder.append(" record.id asc");
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.AbstractRecordFilter#getJoinedClauses(java.util.Map)
     */
    public String getJoinedClauses(Map<String, Object> paramMap) {
        StringBuilder builder = new StringBuilder();
        if (getSurveyPk() > 0) {
            builder.append(" and record.survey.id = :surveyId");
            paramMap.put("surveyId", getSurveyPk());
        }
        if (getGroupPk() > 0) {
            // Cascade classes and groups
            builder.append(" and record.user.id in (select u.id from Group c, Group g, User u where g.id in (select id from c.groups) and u.id in (select id from g.users) and (c.id = :groupId or g.id = :groupId))");
            paramMap.put("groupId", getGroupPk());
        }
        if (getTaxonGroupPk() > 0) {
            builder.append(" and record.species.id in (select s1.id from IndicatorSpecies s1 where s1.taxonGroup.id = :taxonGroupId)");
            paramMap.put("taxonGroupId", getTaxonGroupPk());
        }
        
        if (getSpeciesSearch() != null
                && !getSpeciesSearch().isEmpty()) {
            builder.append(" and (UPPER(record.species.commonName) like UPPER('%"
                    + StringEscapeUtils.escapeSql(getSpeciesSearch())
                    + "%') or UPPER(record.species.scientificName) like UPPER ('%"
                    + StringEscapeUtils.escapeSql(getSpeciesSearch())
                    + "%'))");
        }
        
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.AbstractRecordFilter#createTableJoin()
     */
    public String createTableJoin() {
        StringBuilder join = new StringBuilder();
        
        join.append(" left join record.attributes at left join at.attribute");
        join.append(" left join record.location as location");
        join.append(" left join record.user");
        join.append(" left join record.survey");
        join.append(" left join record.species as species");
        join.append(" left join record.censusMethod as censusMethod ");
        
        return join.toString();
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.AbstractRecordFilter#getQueryPredicate()
     */
    @Override
    public String getQueryPredicate() {
        String query = "select distinct record, species.scientificName, species.commonName, location.name, censusMethod.type";
        return query;
    }
}
