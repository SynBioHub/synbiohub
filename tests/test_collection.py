import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request
from test_arguments import test_print

class TestCollections(TestCase):

    def test_collections(TestCase):

        test_print("test_addOwner_get starting")

#        compare_get_request("/public/:collectionId/:displayId/:version/addOwner", route_parameters = ["testid1","testid1_collection","1"], test_name = "test_get_add_owner_public")
        compare_get_request("user/:userId/:collectionId/:displayId/:version/addOwner", route_parameters = ["testuser","testid2","testid2_collection","1"], test_name = "test_get_add_owner_private")

        test_print("test_addOwner_get completed")

        test_print("test_addOwner_post starting")
        data={
            'uri': 'http://localhost:7777/user/testuser/testid2/testid2_collection/1',
            'user' : 'dockertestuser'
        }
#        compare_post_request("addOwner", data, headers = {"Accept": "text/plain"},test_name = "test_addOwnerPrivate")

        data={
            'uri': 'http://localhost:7777/public/testid1/testid1_collection/1',
            'user' : 'dockertestuser'
        }
#        compare_post_request("addOwner", data, headers = {"Accept": "text/plain"},test_name = "test_addOwnerPublic")

        test_print("test_addOwner_post completed")



        test_print("test_addOwner_get starting")

        compare_get_request("user/:userId/:collectionId/:displayId/:version/remove", route_parameters = ["testuser","testid2","testid2_collection","1"], test_name = "test_get_add_owner_private")

        test_print("test_addOwner_post completed")

        test_print("test_addOwner_get starting")

#        compare_get_request("user/:userId/:collectionId/:displayId/:version/replace", route_parameters = ["testuser","testid2","testid2_collection","1"], test_name = "test_get_add_owner_private")

        test_print("test_addOwner_post completed")
