import json
import requests
from bs4 import BeautifulSoup
from unittest import TestCase
from test_functions import (
    compare_get_request, compare_post_request, get_request,
    get_request_download, post_request,
)
from test_arguments import test_print

class TestCollections(TestCase):

    def test_collections(TestCase):

        test_print("test_addOwner_get starting")

#        compare_get_request("/public/:collectionId/:displayId/:version/addOwner", route_parameters = ["testid1","testid1_collection","1"], test_name = "test_get_add_owner_public")
        compare_get_request("user/:userId/:collectionId/:displayId/:version/addOwner", route_parameters = ["testuser","testid2","testid2_collection","1"], test_name = "test_get_add_owner_private")

        test_print("test_addOwner_get completed")

        test_print("test_addOwner_post starting")
        data={
            'uri': 'http://localhost:7777/user/testuser/testid2/testid2_collection/1',
            'user' : 'dockertestuser'
        }
#        compare_post_request("addOwner", data, headers = {"Accept": "text/plain"},test_name = "test_addOwnerPrivate")

        data={
            'uri': 'http://localhost:7777/public/testid1/testid1_collection/1',
            'user' : 'dockertestuser'
        }
#        compare_post_request("addOwner", data, headers = {"Accept": "text/plain"},test_name = "test_addOwnerPublic")

        test_print("test_addOwner_post completed")



        test_print("test_addOwner_get starting")

        compare_get_request("user/:userId/:collectionId/:displayId/:version/remove", route_parameters = ["testuser","testid2","testid2_collection","1"], test_name = "test_get_add_owner_private")

        test_print("test_addOwner_post completed")

        test_print("test_addOwner_get starting")

#        compare_get_request("user/:userId/:collectionId/:displayId/:version/replace", route_parameters = ["testuser","testid2","testid2_collection","1"], test_name = "test_get_add_owner_private")

        test_print("test_addOwner_post completed")

    def test_manage_submission_order(self):
        """Submission order follows names, with URI order breaking name ties."""
        test_print("test_manage_submission_order starting")
        attachment = "/user/testuser/test_attachment/test_attachment_collection/1"
        hashed = "/user/testuser/test_hash/test_hash_collection/2"
        public = "/public/testid1/testid1_collection/1"
        # Creation order, name order, and URI order deliberately differ.
        collections = [
            ("manage_order_z", "Alpha"),
            ("manage_order_middle", "Zulu"),
            ("manage_order_a", "alpha"),
        ]
        created = []
        filename = "SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml"
        try:
            for collection_id, name in collections:
                with open(filename, "rb") as sbol_file:
                    post_request(
                        "submit", {
                            "id": collection_id, "version": "1", "name": name,
                            "description": "Submission ordering test",
                            "citations": "", "overwrite_merge": "0",
                        }, {"Accept": "text/plain"}, [],
                        {"file": (filename, sbol_file)},
                    )
                created.append(
                    "/user/testuser/" + collection_id + "/" + collection_id + "_collection/1"
                )

            expected_private = [created[2], created[0], attachment, hashed, created[1]]
            html = get_request("manage", {"Accept": "text/html"}, [], 0)
            lists = BeautifulSoup(html, "lxml").select(".submission-list")
            self.assertEqual(len(lists), 2)
            for group, expected in zip(lists, [expected_private, [public]]):
                paths = [
                    item.find("a", href=True)["href"]
                    for item in group.select(".search-result-item")
                ]
                self.assertEqual(paths, expected)

            results = json.loads(get_request_download(
                "manage", {"Accept": "application/json"}, [], 0,
            ))
            self.assertEqual(
                [result["url"] for result in results], expected_private + [public],
            )
            self.assertEqual(
                [result["triplestore"] for result in results],
                ["private"] * 5 + ["public"],
            )
        finally:
            for path in reversed(created):
                get_request_download(
                    path.lstrip("/") + "/removeCollection",
                    {"Accept": "text/plain"}, [], 0,
                )

        test_print("test_manage_submission_order completed")
