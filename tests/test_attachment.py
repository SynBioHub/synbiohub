import requests
import os
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request

class TestAttachment(TestCase):

    def test_attach(self):
        image_filename = os.path.basename('./logo.jpg');
        files={
            'file' : (image_filename, open('./logo.jpg', 'rb')),
        }
        data = {
        }
        compare_post_request("/public/:collectionId/:displayId/:version/attach", data, route_parameters = ["testid0", "testid0", "testid_collection0", "1"],headers = {"Accept": "text/plain"}, test_name = "test_attach")
