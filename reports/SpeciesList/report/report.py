import os
import json
from Cheetah.Template import Template

def taxon_sort_key(taxon):
    """Provides the sorting key for a BDRS taxon."""
    return "%s%s%s" % (taxon['taxonGroup']['name'], taxon['scientificName'], taxon['commonName'],)

class Report:
    """The Species List report provides a tabular view of all taxa that have
    been recorded in a survey.
    """
    def content(self, json_params):
        params = json.loads(json_params)
        tmpl_params = {}

        survey_array = json.loads(bdrs.getSurveyDAO().getActiveSurveys())
        tmpl_params['survey_array'] = survey_array

        if len(survey_array) > 0:
            survey_id = params.get('surveyId', [survey_array[0]['id']])[0]
            survey_id = int(survey_id)
            tmpl_params['survey_id'] = survey_id

            taxa_dao = bdrs.getTaxaDAO()
            taxa_array = json.loads(taxa_dao.getDistinctRecordedTaxaForSurvey(survey_id))
            group_array = json.loads(taxa_dao.getTaxonGroupsForSurvey(survey_id))
            group_map = dict([(g['id'], g,) for g in group_array])
            
            for taxon in taxa_array:
                taxon['taxonGroup'] = group_map[taxon['taxonGroup']]

            taxa_array.sort(key=taxon_sort_key)
            tmpl_params['taxa_array'] = taxa_array

        tmpl = Template(file=bdrs.toAbsolutePath('template/species_list.tmpl'), searchList=tmpl_params)
        response = bdrs.getResponse()
        response.setContentType(response.HTML_CONTENT_TYPE)
        response.setContent(str(tmpl))

        return
