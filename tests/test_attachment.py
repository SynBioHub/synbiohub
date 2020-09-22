import requests
from unittest import TestCase
from test_arguments import test_print
from test_functions import compare_get_request, compare_post_request

class TestAttachment(TestCase):

    def test_attachment(self):
        test_print("test_attachUrl starting")
        data={
            'url': 'synbiohub.org',
            'name' : 'synbiohub_test_attachurl',
            'type' : 'test'
        }
#        http://localhost:7777/user/testuser/testid2/testid2_collection/1
        compare_post_request("/user/:userId/:collectionId/:displayId/:version/attachUrl", route_parameters = ["testuser", "testid2", "testid2_collection", "1"], data = data)

        test_print("test_attachUrl completed")

        test_print("test_attach_private_collection starting")
        files = {'file':("./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml", open('./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml', 'rb'))}

        compare_post_request("user/:userId/:collectionId/:displayId/:version/attach",route_parameters = ["testuser", "testid2", "testid2_collection", "1"],data=data,  headers = {"Accept": "text/plain"}, files = files, test_name = "test_attach_to_private_collection")
        test_print("test_attach_private_collection completed")
