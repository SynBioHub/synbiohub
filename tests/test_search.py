import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request

# "/manage" is tested within test_submit.py

class TestSearch(TestCase):

    def test_searchQuery(self):
        compare_get_request("/search/:query?", route_parameters = ["I0462"])

    def test_searchCount(self):
        compare_get_request("/searchCount/:query?", route_parameters = ["I0462"])

    def test_advancedSearch(self):
        compare_get_request("/advancedSearch")

    def test_rootCollections(self):
        compare_get_request("/rootCollections")

    def test_sparql(self):
        compare_get_request("/sparql", headers = {"Accept": "text/html"})
