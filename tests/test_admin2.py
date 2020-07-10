import requests
import os
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestAdmin2(TestCase):

    def test_admin_deletePlugin(self):
        data={
            'id': '1',
            'category' : 'download',
        }
        compare_post_request("/admin/deletePlugin", data, headers = {"Accept": "text/plain"}, test_name = "admin_deletePlugin")

    def test_admin_deleteRegistry(self):
        data={
            'uri': 'testurl.com',
        }
        compare_post_request("/admin/deleteRegistry", data, headers = {"Accept": "text/plain"}, test_name = "admin_deleteRegistry")

