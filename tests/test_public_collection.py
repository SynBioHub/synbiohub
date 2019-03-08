import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestPublicCollection(TestCase):

    def test_make_public(self):
        # create the collection
        data = {'id':(None, 'testid'),
                'version' : (None, '1'),
                'name' : (None, 'testcollection'),
                'description':(None, 'testdescription'),
                'citations':(None, 'none'),
                'overwrite_merge':(None, '0')}

        files = {'file':("./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml",
                                              open('./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml', 'rb'))}
        
        compare_post_request("submit", data, headers = {"Accept": "text/plain"},
                             files = files, test_name = "submit_test_BBa_2")

        # get the view
        compare_get_request("/user/:userId/:collectionId/:displayId/:version/makePublic", route_parameters = ["testuser", "testid", "testid_collection", "1"])

        data['tabState'] = 'new'
        # make the collection public
        compare_post_request("/user/:userId/:collectionId/:displayId/:version/makePublic", route_parameters = ["testuser", "testid", "testid_collection", "1"], data = data)

        # try to delete the collection
        with self.assertRaises(requests.exceptions.HTTPError):
            compare_get_request("/public/:collectionId/:displayId/:version/removeCollection", route_parameters = ["testid", "testid_collection", "1"], test_name = 'remove')
