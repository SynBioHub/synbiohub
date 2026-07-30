from unittest import TestCase

from test_arguments import test_print
from test_functions import compare_get_request, refresh_explorer_index


class TestExplorerSearch(TestCase):

    def test_indexed_search_html(self):
        """Index the fixture explicitly, then enforce SBH1's rendered result."""
        test_print("test_explorer_indexed_search starting")
        refresh_explorer_index("I0462", "BBa_I0462")
        # Reuse the established whole-page snapshot. Explorer-enabled matrix
        # rows run this focused contract instead of TestSearch, so the request
        # path remains unique in the legacy TestState registry.
        compare_get_request("/search/:query?", route_parameters=["I0462"])
        test_print("test_explorer_indexed_search completed")
