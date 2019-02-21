from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestSetup(TestCase):

    def test_main_page(self):
        headers = {'Accept':'text/plain'}
        compare_get_request("/", test_name = "after_admin_login", headers = headers)

    def test_get_submit(self):
        compare_get_request("submit")


    # working curl request
    """curl -X POST -H "Accept: text/plain" -H "X-authorization: e35054aa-04e3-425c-afd2-66cb95ff66e1" -F id=green -F version="3" -F name="test" -F description="testd" -F citations="none" -F overwrite_merge="0" -F file=@"./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml" http://localhost:7777/submit"""
    def test_create_and_delete_collection(self):
        data = {'id':(None, 'testid'),
                'version' : (None, '1'),
                'name' : (None, 'testcollection'),
                'description':(None, 'testdescription'),
                'citations':(None, 'none'),
                'overwrite_merge':(None, '0')}
        
        compare_post_request("submit", data, headers = {"Accept": "text/plain"},
                             files = {'file':("./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml",
                                              open('./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml', 'rb'))})

