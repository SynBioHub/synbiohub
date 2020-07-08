import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request

class TestEdit(TestCase):

    def test_edit_field(self):
        data={
            'uri': 'http://localhost:7777/public/testid0/testid0_collection/1',
            'value' : 'testUpdateMutableDescription'
        }
        compare_post_request("updateMutableDescription", data, headers = {"Accept": "text/plain"},test_name = "test_edit_mutable_description")

    def test_edit_field(self):
        data={
            'uri': 'http://localhost:7777/public/testid0/testid0_collection/1',
            'value' : 'testUpdateMutableNotes'
        }
        compare_post_request("updateMutableNotes", data, headers = {"Accept": "text/plain"},test_name = "test_edit_mutable_notes")

    def test_edit_source(self):
        data={
            'uri': 'http://localhost:7777/public/testid0/testid0_collection/1',
            'value' : 'testUpdateMutableSource'
        }
        compare_post_request("updateMutableSource", data, headers = {"Accept": "text/plain"},test_name = "test_edit_mutable_source")

#    def test_edit_citations(self):
#        data={
#            'uri': 'http://localhost:7777/public/testid0/testid0_collection/1',
#           'value' : 'testUpdateCitations'
#       }
#        compare_post_request("updateCitations", data, headers = {"Accept": "text/plain"},test_name = "test_edit_mutable_citations")
