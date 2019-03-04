import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestAdminViews(TestCase):

    def test_admin_status(self):
        compare_get_request("/admin")

