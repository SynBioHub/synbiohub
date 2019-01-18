import unittest
from sys import argv

# run the first time setup script
from first_time_setup import *

from test_arguments import args

if __name__ == '__main__':
    # pass in just the first arg so unittest doesn't try to use the arguments to find test cases
    unittest.main(argv = [argv[0]])
