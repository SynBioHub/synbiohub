import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request

class TestFeatures(TestCase):
        def test_user_part_addOwner(self):
                compare_get_request("/user/:userId/:collectionId/:displayId/:version/addOwner", route_parameters = ["testuser","testid0","BBa_I0462", "1"])

        def test_user_part_createTest(self):
                compare_get_request("/user/:userId/:collectionId/:displayId/:version/createTest", route_parameters = ["testuser","testid0","BBa_I0462", "1"])
