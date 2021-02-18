import requests
from unittest import TestCase
from test_arguments import test_print
from test_functions import compare_get_request, compare_post_request

class TestHash(TestCase):

    def test_hash(self):
        test_print("hello")

