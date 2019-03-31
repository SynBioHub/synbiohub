import unittest
import test_arguments


if __name__ == '__main__':
    # do not overwrite any tests for this pass
    test_arguments.resetalltests = False
    test_arguments.resetgetrequests = []
    test_arguments.resetpostrequests = []
    
    tests = unittest.TestSuite()

    def addTestModule(m):
        newtests = unittest.defaultTestLoader.loadTestsFromModule(m)
        tests.addTests(newtests)

    # just run the docker tests this time
    import docker_test
    addTestModule(docker_test)
    
    runner = unittest.TextTestRunner()
    result = runner.run(tests)


