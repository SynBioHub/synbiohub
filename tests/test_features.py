import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request

class TestFeatures(TestCase):

    def test_advancedSearch(self):
        compare_get_request("/advancedSearch")

    def test_createCollection(self):
        compare_get_request("/createCollection")

    def test_resetPassword(self):
        compare_get_request("/resetPassword")

    def test_browse(self):
        compare_get_request("/browse")

    def test_sparql(self):
        compare_get_request("/sparql", headers = {"Accept": "text/html"})
