from unittest import TestCase

from test_arguments import test_print
from test_functions import compare_search_result_overlap, refresh_explorer_index


class TestExplorerSearch(TestCase):

    def test_indexed_search_overlap(self):
        """Index the fixture, then require semantic result identity overlap."""
        test_print("test_explorer_indexed_search starting")
        refresh_explorer_index("I0462", "BBa_I0462")
        compare_search_result_overlap(
            "/search/:query?", route_parameters=["I0462"]
        )
        test_print("test_explorer_indexed_search completed")
