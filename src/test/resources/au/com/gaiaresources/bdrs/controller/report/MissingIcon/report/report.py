import json
from Cheetah.Template import Template

class Report:
    def content(self, json_params):
        params = json.loads(json_params)
        tmpl = Template(file=bdrs.toAbsolutePath('template/report.tmpl'), searchList={'tmpl_text': 'nyan nyan nyan'})
        response = bdrs.getResponse()
        response.setContentType(response.HTML_CONTENT_TYPE)
        response.setContent(str(tmpl))
        return
