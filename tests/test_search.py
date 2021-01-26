import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request
from test_arguments import test_print

# "/manage" is tested within test_submit.py

class TestSearch(TestCase):

    def test_search(self):

        # test_searchQuery(self):
        test_print("test_search starting")
        compare_get_request("/search/:query?", route_parameters = ["I0462"])
        test_print("test_search completed")

        # test_searchCount(self):
        test_print("test_searchCount starting")
        compare_get_request("/searchCount/:query?", route_parameters = ["I0462"])
        test_print("test_searchCount completed")

        # test_advancedSearch(self):
        test_print("test_advancedSearch GET starting")
        compare_get_request("/advancedSearch")
        test_print("test_advancedSearch completed")

        # test advancedSerach post
        test_print("test_advancedSearch POST starting")
        data={
            'description': 'BBa_I0462',
            'adv': 'Search'
        }
        compare_post_request("/advancedSearch", data = data)


        test_print("test_advancedSearch completed")



        # test_rootCollections(self):
        test_print("test_rootCollections starting")
        compare_get_request("/rootCollections")
        test_print("test_rootCollections completed")

        # test_sparql(self):
        test_print("test_sparql starting")
        compare_get_request("/sparql", headers = {"Accept": "text/html"})
        test_print("test_sparql completed")


        test_print("test_subcollections_public starting")
        compare_get_request("/public/:collectionId/:displayId/:version/subCollections", route_parameters = ["testid1","testid1_collection", "1"],headers = {"Accept": "text/html"})
        test_print("test_subcollections_public completed")

        # test_uses(self):
        test_print("test_uses starting")
        compare_get_request("user/:userId/:collectionId/:displayId/:version/uses", route_parameters = ["testuser","testid2", "BBa_B0015", "1"],headers = {"Accept": "text/html"})
        test_print("test_uses completed")

        test_print("test_subcollections_private starting")
        compare_get_request("user/:userId/:collectionId/:displayId/:version/subCollections", route_parameters = ["testuser","testid2","testid2_collection", "1"],headers = {"Accept": "text/html"})
        test_print("test_subcollections_private completed")

        test_print("test_browse_get starting")
        compare_get_request("browse", headers = {"Accept": "text/html"})
        test_print("test_browse_get completed")


