import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_get_request_download,compare_post_request

class TestDownload(TestCase):

    def test_download(self):

        compare_get_request_download("/public/:collectionId/:displayId/sbol", route_parameters = ["testid1","part_pIKE_Toggle_1"], headers = {"Accept": "text/html"})
        compare_get_request_download("/public/:collectionId/:displayId/:version/sbol", route_parameters = ["testid1","part_pIKE_Toggle_1","1"], headers = {"Accept": "text/html"})
        compare_get_request_download("/public/:collectionId/:displayId/sbolnr", route_parameters = ["testid1","part_pIKE_Toggle_1"], headers = {"Accept": "text/html"})
        compare_get_request_download("/public/:collectionId/:displayId/:version/sbolnr", route_parameters = ["testid1","part_pIKE_Toggle_1","1"], headers = {"Accept": "text/html"})

        # user/testuser/test_attachment/part_pIKE_Toggle_1/1
        compare_get_request_download("/user/:userId/:collectionId/:displayId/sbol", route_parameters = ["testuser","test_attachment","part_pIKE_Toggle_1"], headers = {"Accept": "text/html"})
        compare_get_request_download("/user/:userId/:collectionId/:displayId/sbolnr", route_parameters = ["testuser","test_attachment","part_pIKE_Toggle_1"], headers = {"Accept": "text/html"})
        compare_get_request_download("/user/:userId/:collectionId/:displayId/:version/sbol", route_parameters = ["testuser","test_attachment","part_pIKE_Toggle_1","1"], headers = {"Accept": "text/html"})
        compare_get_request_download("/user/:userId/:collectionId/:displayId/:version/sbolnr", route_parameters = ["testuser","test_attachment","part_pIKE_Toggle_1","1"], headers = {"Accept": "text/html"})
