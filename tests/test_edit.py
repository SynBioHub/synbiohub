import requests
from unittest import TestCase
from test_arguments import test_print
from test_functions import compare_get_request, compare_post_request

class TestEdit(TestCase):

    def test_edit(self):

        test_print("test_edit_field_private_collection starting")
        data={
            'previous': '<previous>',
            'object' : 'testEditTitle',
            'pred' : '<pred>',
        }
        compare_post_request("user/:userId/:collectionId/:displayId/:version/edit/:field",route_parameters = ["testuser", "testid2", "testid2_collection", "1", "title"],data=data,  headers = {"Accept": "text/plain"}, test_name = "test_edit_field_private_collection")

        test_print("test_edit_field_private_collection completed")

       # test_update_mutableDescription
        test_print("test_update_mutableDescription starting")
        data={
            'uri': 'http://localhost:7777/public/testid1/testid1_collection/1',
            'value' : 'testUpdateMutableDescription'
        }
        compare_post_request("updateMutableDescription", data, headers = {"Accept": "text/plain"},test_name = "test_update_mutableDescription")
        test_print("test_update_mutableDescription completed")

        # test_update_mutableNotes
        test_print("test_update_mutableNotes starting")
        data={
            'uri': 'http://localhost:7777/public/testid1/testid1_collection/1',
            'value' : 'testUpdateMutableNotes'
        }
        compare_post_request("updateMutableNotes", data, headers = {"Accept": "text/plain"},test_name = "test_update_mutableNotes")
        test_print("test_update_mutableNotes completed")

        # test_update_mutableSource
        test_print("test_update_mutableSource starting")
        data={
            'uri': 'http://localhost:7777/public/testid1/testid1_collection/1',
            'value' : 'testUpdateMutableSource'
        }
        compare_post_request("updateMutableSource", data, headers = {"Accept": "text/plain"},test_name = "test_update_mutableSource")
        test_print("test_update_mutableSource completed")

        # test_edit_citations
        test_print("test_edit_citations starting")
        data={
            'uri': 'http://localhost:7777/public/testid1/testid1_collection/1',
            'value' : '1234'
        }
        compare_post_request("updateCitations", data, headers = {"Accept": "text/plain"},test_name = "test_edit_mutable_citations")
        test_print("test_edit_citations completed")

        test_print("test_add_field_wasDerviedFrom starting")
        data={
            'object' : 'testWasDerivedFrom',
            'pred' : '<pred>',
        }
        compare_post_request("user/:userId/:collectionId/:displayId/:version/add/:field",route_parameters = ["testuser", "testid2", "testid2_collection", "1", "wasDerivedFrom"],data=data,  headers = {"Accept": "text/plain"}, test_name = "test_add_wasDerivedFrom_private_collection")

        test_print("test_add_field_wasDerivedFrom completed")

        test_print("test_remove_field_private_collection starting")
        data={
            'object' : 'testWasDerivedFrom',
            'pred' : '<pred>',
        }
        compare_post_request("user/:userId/:collectionId/:displayId/:version/remove/:field",route_parameters = ["testuser", "testid2", "testid2_collection", "1", "wasDerivedFrom"],data=data,  headers = {"Accept": "text/plain"}, test_name = "test_remove_field_private_collection")

        test_print("test_remove_field_private_collection completed")

