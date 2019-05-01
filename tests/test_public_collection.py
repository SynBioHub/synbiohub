import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestPublicCollection(TestCase):

    def make_new_collection(self, uniqueid):
        # create the collection
        data = {'id':(None, 'testid' + uniqueid),
                'version' : (None, '1'),
                'name' : (None, 'testcollection' + uniqueid),
                'description':(None, 'testdescription' + uniqueid),
                'citations':(None, 'none'),
                'overwrite_merge':(None, '0')}

        files = {'file':("./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml",
                                              open('./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml', 'rb'))}
        
        compare_post_request("submit", data, headers = {"Accept": "text/plain"},
                             files = files, test_name = "generic_collection" + uniqueid)
        return data
    
    def test_bad_make_public(self):
        data = self.make_new_collection("1")
        
        data['tabState'] = 'new'

        # try to remove the collection but don't enter a id
        del data['id']
        with self.assertRaises(requests.exceptions.HTTPError):
            compare_post_request("/user/:userId/:collectionId/:displayId/:version/makePublic", route_parameters = ["testuser", "testid1", "testid_collection1", "1"], data = data)
        
        

    def test_make_public(self):
        data = self.make_new_collection("0")
        
        
        # get the view
        compare_get_request("/user/:userId/:collectionId/:displayId/:version/makePublic", route_parameters = ["testuser", "testid0", "testid_collection0", "1"])

        data['tabState'] = 'new'
        
        # make the collection public
        compare_post_request("/user/:userId/:collectionId/:displayId/:version/makePublic", route_parameters = ["testuser", "testid0", "testid_collection0", "1"], data = data)

        # try to delete the collection
        with self.assertRaises(requests.exceptions.HTTPError):
            compare_get_request("/public/:collectionId/:displayId/:version/removeCollection", route_parameters = ["testid0", "testid_collection0", "1"], test_name = 'remove')
