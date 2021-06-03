import unittest
from sys import exit

from test_functions import cleanup_check


if __name__ == '__main__':
    tests = unittest.TestSuite()

    def addTestModule(m):
        newtests = unittest.defaultTestLoader.loadTestsFromModule(m)
        tests.addTests(newtests)

    import test_root
    addTestModule(test_root)

    runner = unittest.TextTestRunner()
    result = runner.run(tests)

    # do final check after all tests have run
    cleanup_check()

    # if any tests failed, exit with code one
    if len(result.failures) != 0 or len(result.errors) != 0:
        exit(1)
