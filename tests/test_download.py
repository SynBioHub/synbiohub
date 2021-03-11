import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request

class TestDownload(TestCase):
    pass
#    def test_gff(self):
        #localhost:7777/public/testid0/BBa_I0462/1/gff
#        headers = {'Accept': 'text/plain'}
#        compare_get_request("/public/:collectionId/:displayId/:version/gff", route_parameters =["testid0","BBa_I0462", "1"])

# TEST SBOL 
    file = open("test.gb").read()
    diff_file = open("test.gb").read()

    request = { 'options': {'language' : 'GenBank',
                            'test_equality': True,
                            'check_uri_compliance': False,
                            'check_completeness': False,
                            'check_best_practices': False,
                            'fail_on_first_error': False,
                            'provide_detailed_stack_trace': False,
                            'subset_uri': '',
                            'uri_prefix': '',
                            'version': '',
                            'insert_type': False,
                            'main_file_name': 'file',
                            'diff_file_name': 'diff_file',
                            },
                'return_file': True,
                'main_file': file,
                'diff_file': diff_file
            }

    resp = requests.post("https://validator.sbolstandard.org/validate/", json=request)
    print(resp)
