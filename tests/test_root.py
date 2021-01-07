# this file specifies the tests to be run.
import requests
from unittest import TestCase
from test_arguments import test_print
from test_functions import compare_get_request, compare_post_request

class TestRoot(TestCase):

        def test_root(self):

            from test_tests import test_get_address
            test_get_address()

            from first_time_setup import test_get, test_post
            test_get()
            test_post()
