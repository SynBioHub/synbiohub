import unittest
from sys import exit

from test_functions import cleanup_check

# hello, i am an insignificant change.

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

    import test_user
    addTestModule(test_user)

    import test_user2
    addTestModule(test_user2)

    import test_submit
    addTestModule(test_submit)

    import test_search
    addTestModule(test_search)

    import test_download
    addTestModule(test_download)

    import test_edit
    addTestModule(test_edit)

    import test_attachment
    addTestModule(test_attachment)

#    import test_collection
#    addTestModule(test_attachment)

    import test_admin
    addTestModule(test_admin)

    import test_admin2
    addTestModule(test_admin2)

    import test_twins
    addTestModule(test_twins)

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
