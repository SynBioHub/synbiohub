import requests
import os
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request

class TestAttachment(TestCase):

    def test_attach(self):
        files={
            'file' : open('test_user.py', 'rb')
        }
        data = {
        }
        compare_post_request("/public/:collectionId/:displayId/:version/attach", data, route_parameters = ["testid0", "testid_collection0", "1"],headers = {"Accept": "text/plain"}, files = files, test_name = "test_attach")

    def test_attachUrl(self):
        #http://localhost:7777/public/testid0/testid0_collection/1/attachUrl
        data = {
            'url': 'testsite.com',
            'name' : 'testURLAttachment',
            'type' : 'dim'
        }
        compare_post_request("/public/:collectionId/:displayId/:version/attachUrl", data, route_parameters = ["testid0", "testid_collection0", "1"],headers = {"Accept": "text/plain"}, test_name = "test_attachUrl"), headers = {"Accept": "text/plain"}, test_name = "test_attachUrl")
