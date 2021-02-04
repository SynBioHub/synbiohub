import requests
from unittest import TestCase
from test_arguments import test_print
from test_functions import compare_get_request, compare_post_request

class TestAttachment(TestCase):

    def test_attachment(self):
        test_print("test_attachUrl_private starting")
        data={
                'url': 'synbiohub.org',
                'name' : 'synbiohubtestattachurl',
                'type' : 'test',
        }
#        compare_post_request("/user/:userId/:collectionId/:displayId/:version/attachUrl", route_parameters = ["testuser", "testid2", "testid2_collection", "1"], data = data, test_name = "test_attachURL_private")

        test_print("test_attachUrl_private completed")

        test_print("test_attach_private_collection starting")
        files = {'file':("./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml", open('./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml', 'rb'))}

        compare_post_request("user/:userId/:collectionId/:displayId/:version/attach",route_parameters = ["testuser", "testid2", "testid2_collection", "1"],data=data,  headers = {"Accept": "text/plain"}, files = files, test_name = "test_attach_to_private_collection2")

        test_print("test_attach_private_collection completed")

        test_print("test_attach_public_collection starting")
        files = {'file':("./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml", open('./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml', 'rb'))}

        compare_post_request("public/:collectionId/:displayId/:version/attach",route_parameters = ["testid1", "testid1_collection", "1"],data=data,  headers = {"Accept": "text/plain"}, files = files, test_name = "test_attach_to_public_collection")
        test_print("test_attach_private_collection completed")

#      test_print("test_attachUrl_public starting")
        data={
                'url': 'synbiohub.org',
                'name' : 'synbiohubtestattachurl',
                'type' : 'test',
        }
#        compare_post_request("/public/:collectionId/:displayId/:version/attachUrl", route_parameters = ["testid1", "testid1_collection", "1"], data = data)

 #       test_print("test_attachUrl_public completed")


