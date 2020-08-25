import requests
from unittest import TestCase
from test_arguments import test_print
from test_functions import compare_get_request, compare_post_request

class TestEdit(TestCase):

    def test_edit(self):
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

#    def test_add_field(self):
#        data={
#            'object' : 'testAddField'
#        }
#        compare_post_request("http://localhost:7777/public/testid0/testid0_collection/1/add/title", data, headers = {"Accept": "text/plain"},test_name = "test_add_field")

#    def test_edit_field(self):
#        data={
#            'previous' : 'testAddField',
#            'object' : 'testEditField'
#        }
#        compare_post_request("http://localhost:7777/public/testid0/testid0_collection/1/add/title", data, headers = {"Accept": "text/plain"},test_name = "test_add_field")

