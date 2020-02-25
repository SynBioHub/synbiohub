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

    def test_searchQuery(self):
        compare_get_request("/search/:query?", route_parameters = ["I0462"])

    def test_searchCount(self):
        compare_get_request("/searchCount/:query?", route_parameters = ["I0462"])        

#    def test_advancedSearchQuery(self):
#        compare_get_request("/advancedSearch/:query?", route_parameters = ["I0462"])

    def test_rootCollections(self):
        compare_get_request("/rootCollections")
       
    def test_typeCount(self):
        compare_get_request("/:type/count", route_parameters = ["Component"])
        
    def test_public_collection_collectionid_displayid_copyFromRemote(self):
        compare_get_request("public/:collectionId/:displayId/:version/copyFromRemote", route_parameters = ["testid0","BBa_I0462", "1"])
      
#    def test_public_collection_collectionid_displayid_sbolnr(self):
#        compare_get_request("/public/:collectionId/:displayId/sbolnr", route_parameters = ["testid0","BBa_I0462"])

#    def test_public_collection_collectionid_displayid_search_query(self):
#        compare_get_request("public/:collectionId/:displayId/search/:query?", route_parameters = ["testid0","BBa_I0462", "test"])

    def test_public_collection_collectionid_displayId_verion_sharing(self):
        compare_get_request("public/:collectionId/:displayId/:version/sharing", route_parameters = ["testid0","BBa_I0462", "1"])

    def test_public_collection_collectionid_displayid_version_metadata(self):
        compare_get_request("/public/:collectionId/:displayId/:version/metadata", route_parameters = ["testid0","BBa_I0462", "1"])
        
    def test_public_collection_collectionid_displayid_version_metadata(self):
        compare_get_request("/public/:collectionId/:displayId/:version/visualization", route_parameters = ["testid0","BBa_I0462", "1"])

    def test_public_collection_collectionid_displayid_version_uses(self):
        compare_get_request("/public/:collectionId/:displayId/:version/uses", route_parameters = ["testid0","BBa_I0462", "1"])
        
#    def test_public_collection_collectionid_displayid_version_uses(self):
#        compare_get_request("/public/:collectionId/:displayId/:version/search/:query?", route_parameters = ["testid0","BBa_I0462", "1", "test"])

    def test_public_collection_collectionid_displayid_version_metadata(self):
        compare_get_request("/public/:collectionId/:displayId/:version/visualization", route_parameters = ["testid0","BBa_I0462", "1"])

    def test_public_collection_collectionid_displayid_version_metadata(self):
        compare_get_request("/public/:collectionId/:displayId(*)/:version", route_parameters = ["testid0","BBa_I0462", "1"])
        
    def test_public_collection_collectionid_displayid_version_metadata(self):
        compare_get_request("/public/:collectionId/:displayId/:version/visualization", route_parameters = ["testid0","BBa_I0462", "1"])

