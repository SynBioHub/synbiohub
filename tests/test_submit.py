import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestSubmit(TestCase):

    def test_main_page(self):
        headers = {'Accept':'text/plain'}
        compare_get_request("/", test_name = "after_admin_login", headers = headers)



    def test_get_submit_submissions_empty(self):
        compare_get_request("submit")
        compare_get_request("manage")


    # working curl request
    """curl -X POST -H "Accept: text/plain" -H "X-authorization: e35054aa-04e3-425c-afd2-66cb95ff66e1" -F id=green -F version="3" -F name="test" -F description="testd" -F citations="none" -F overwrite_merge="0" -F file=@"./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml" http://localhost:7777/submit"""


    def test_create_id_missing(self):
        data = {'version' : (None, '1'),
                'name' : (None, 'testcollection'),
                'description':(None, 'testdescription'),
                'citations':(None, 'none'),
                'overwrite_merge':(None, '0')}

        files = {'file':("./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml",
                                              open('./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml', 'rb'))}
        with self.assertRaises(requests.exceptions.HTTPError):
            compare_post_request("submit", data, headers = {"Accept": "text/plain"},
                                 files = files, test_name = "missing_id")

    def test_create_and_delete_collections(self):
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
                             files = files, test_name = "submit_test_BBa")
        with self.assertRaises(requests.exceptions.HTTPError):
            compare_post_request("submit", data, headers = {"Accept": "text/plain"},
                                 files = files, test_name = "submit_already_in_use")

        self.create_collection2()

        compare_get_request("manage", test_name = "two_submissions")
        compare_get_request("submit", test_name = "two_submissions")


        # now remove the collections
        compare_get_request('/user/:userId/:collectionId/:displayId/:version/removeCollection', route_parameters = ["testuser", "testid", "testid_collection", "1"])
        compare_get_request('/user/:userId/:collectionId/:displayId/:version/removeCollection', route_parameters = ["testuser", "testid2", "testid2_collection", "1"], test_name = 'remove_second')

        compare_get_request("manage", test_name = "no_submissions")




    def create_collection2(self):
        data = {'id':(None, 'testid2'),
                'version' : (None, '1'),
                'name' : (None, 'testcollection2'),
                'description':(None, 'testdescription'),
                'citations':(None, 'none'),
                'overwrite_merge':(None, '0')}

        files = {'file':("./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml",
                                              open('./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml', 'rb'))}

        compare_post_request("submit", data, headers = {"Accept": "text/plain"},
                             files = files,
                             test_name = "create_2")

        # delete collection
        #compare_get_request("/user/testuser/testid2/testid_collection2/1/removeCollection")

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

    """ def test_bad_make_public(self):
        data = self.make_new_collection("1")

        data['tabState'] = 'new'

        # try to remove the collection but don't enter a id
        del data['id']
        with self.assertRaises(requests.exceptions.HTTPError):
            compare_post_request("/user/:userId/:collectionId/:displayId/:version/makePublic", route_parameters = ["testuser", "testid1", "testid_collection1", "1"], data = data)
    TODO: uncomment when this does raise an HTTPError in synbiohub
        """


    def test_make_public(self):
        data = self.make_new_collection("0")


        # get the view
        compare_get_request("/user/:userId/:collectionId/:displayId/:version/makePublic", route_parameters = ["testuser", "testid0", "testid0_collection", "1"])

        data['tabState'] = 'new'

        # make the collection public
        compare_post_request("/user/:userId/:collectionId/:displayId/:version/makePublic", route_parameters = ["testuser", "testid0", "testid0_collection", "1"], data = data)

        # try to delete the collection
        with self.assertRaises(requests.exceptions.HTTPError):
            compare_get_request("/public/:collectionId/:displayId/:version/removeCollection", route_parameters = ["testid0", "testid0_collection", "1"], test_name = 'remove')

    def make_new_private_collection(self, uniqueid):
        # create the collection
        data = {'id':(None, 'testid1'),
                'version' : (None, '1'),
                'name' : (None, 'testcollection1'),
                'description':(None, 'testdescription'),
                'citations':(None, 'none'),
                'overwrite_merge':(None, '0')
                }

        files = {'file':("./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/toggle.xml", open('./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/toggle.xml', 'rb'))}

        compare_post_request("submit", data, headers = {"Accept": "text/plain"}, files = files, test_name = "generic_collection" + uniqueid+1 )

