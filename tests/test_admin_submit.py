from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestSetup(TestCase):

    def test_submit_empty_then_delete(self):
        
        compare_post_request("submit"

