from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestSetup(TestCase):

    def test_main_page(self):
        compare_get_request("/", test_name = "after_admin_login")

    def test_get_submit(self):
        compare_get_request("submit")



