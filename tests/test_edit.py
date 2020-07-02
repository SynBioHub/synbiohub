import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request

class TestEdit(TestCase):

    def test_edit_field(self):
        data={
            'uri': 'http://localhost:7777/public/testid0/testid0_collection/1',
            'value' : 'jimmy'
        }
        compare_post_request("updateMutableDescription", data, headers = {"Accept": "text/plain"},test_name = "test_edit_mutable")
