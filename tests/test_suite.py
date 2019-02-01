import unittest
from sys import argv
from sys import exit

from test_functions import cleanup_check

# run the first time setup script
from first_time_setup import *

from test_arguments import args


if __name__ == '__main__':
    # pass in just the first arg so unittest doesn't try to use the arguments to find test cases
    testprogram = unittest.main(argv = [argv[0]], exit =False)

    # do final check after all tests have run
    cleanup_check()

    # if any tests failed, exit with code one
    if len(testprogram.result.failures) != 0 or len(testprogram.result.errors) != 0:
        exit(1)
