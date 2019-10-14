import unittest
from sys import argv
from sys import exit

from test_functions import cleanup_check


if __name__ == '__main__':
    tests = unittest.TestSuite()

    def addTestModule(m):
        newtests = unittest.defaultTestLoader.loadTestsFromModule(m)
        tests.addTests(newtests)

    # add test modules here
    import test_tests
    addTestModule(test_tests)
    
    import first_time_setup
    addTestModule(first_time_setup)

    import test_before_login
    addTestModule(test_before_login)

    import test_login
    addTestModule(test_login)

    import test_submit
    addTestModule(test_submit)

    import test_public_collection
    addTestModule(test_public_collection)

    import test_admin_views
    addTestModule(test_admin_views)

    import test_features
    addTestModule(test_features)

    import docker_test_setup
    addTestModule(docker_test_setup)

    import docker_test
    addTestModule(docker_test)
    
    runner = unittest.TextTestRunner()
    result = runner.run(tests)

    # do final check after all tests have run
    cleanup_check()

    # if any tests failed, exit with code one
    if len(result.failures) != 0 or len(result.errors) != 0:
        exit(1)
