from unittest import TestCase

from test_functions import get_address
from test_arguments import args


class TestTests(TestCase):

    def test_get_address(self):
        # test no parameters
        self.assertEqual(args.serveraddress +"test/url", get_address("test/url", []))
        with self.assertRaises(Exception):
            get_address("test/:green/notparam", ["two", "params"])
        with self.assertRaises(Exception):
            get_address("test/:green/notparam", [])

        self.assertEqual(args.serveraddress +"test/green", get_address("test/:one", ["green"]))

        self.assertEqual(args.serveraddress +"test/green/second", get_address("test/:one/:gewotri", ["green", "second"]))

        self.assertEqual(args.serveraddress +"test/green/second/", get_address("test/:one/:gewotri/", ["green", "second"]))
                    
