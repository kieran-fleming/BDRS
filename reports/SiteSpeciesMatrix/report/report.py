import json
from datetime import datetime
from copy import deepcopy
from Cheetah.Template import Template


def location_sort_key(loc):
    """Provides the sorting key for BDRS locations."""
    return loc['name']

def taxon_map_sort_key(taxon_map):
    """Provides the sorting key for BDRS Taxons."""
    return taxon_map['taxon']['scientificName']

class Report:
    """The Site Species Matrix report is a tabular view that shows
    the unique taxa for a survey on the y-axis and the number of sightings
    of each species at a given location on the x-axis. Sightings that only 
    contain a coordinate but not a location will be grouped into a special
    location called 'Other'.
    """
    def content(self, json_params):
        params = json.loads(json_params)
        tmpl_params = {}
        survey_array = json.loads(bdrs.getSurveyDAO().getActiveSurveys())
        tmpl_params['survey_array'] = survey_array

        if len(survey_array) > 0:
            survey_ids = params.get('surveyId', [])
            survey_ids = map(int,survey_ids)
            tmpl_params['survey_ids'] = survey_ids

            # Compile all the reports.
            record_dao = bdrs.getRecordDAO()
            record_array = []
            for survey_id in survey_ids:
                json_rec = record_dao.getRecordsForSurvey(survey_id, True, True)
                record_array.extend(json.loads(json_rec))

            # Process the reports
            # The other location is the catch all locations for records
            # that are not associated with a location
            other_loc = {
                'id': 0,
                'name': 'Other',
            }

            # The location map is the canonical mapping of all locations
            # that we have encountered.
            # {location_id, location}
            loc_map = { other_loc['id']: other_loc, }

            # The matrix is a mapping of taxa against a map of locations 
            # where it was sighted
            # matrix = {
            #     'taxon_id': {
            #         'taxon': bdrs_taxon
            #         'locations' {
            #             'location_id': {
            #                 'count': int,
            #                 'location': bdrs_location
            #             }
            #         }
            #     },
            # }

            matrix = {}
            for rec in record_array:
                taxon = rec['species']
                loc = rec['location']
                loc = loc if loc is not None else other_loc
                loc_map[loc['id']] = loc
                if taxon is not None:
                    taxon_map = matrix.setdefault(taxon['id'], {})
                    taxon_map['taxon'] = taxon

                    taxon_loc_map = taxon_map.setdefault('locations', {})
                    loc_count_map = taxon_loc_map.setdefault(loc['id'], {'count': 0, 'location': loc})
                    loc_count_map['count'] += 1

            # Generate a sorted list of unique locations.
            loc_registry = loc_map.values()
            loc_registry.sort(key=location_sort_key)
            tmpl_params['loc_registry'] = loc_registry

            # Sort the taxa alphabetically
            taxa_array = matrix.values()
            taxa_array.sort(key=taxon_map_sort_key)
            tmpl_params['taxa_array'] = taxa_array

        # Render the data
        tmpl = Template(file=bdrs.toAbsolutePath('template/site_species_matrix.tmpl'), searchList=tmpl_params)
        response = bdrs.getResponse()
        response.setContentType(response.HTML_CONTENT_TYPE)
        response.setContent(str(tmpl))

        return