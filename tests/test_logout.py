import requests
from unittest import TestCase
from test_functions import compare_get_request, compare post_request


class TestLogout(TestCase):
    def test_logout(self):
        compare_get_request("/logout")
