import unittest
import test_arguments


if __name__ == '__main__':
    # do not overwrite any tests for this pass
    test_arguments.args.resetalltests = False
    test_arguments.args.resetgetrequests = []
    test_arguments.args.resetpostrequests = []
    
    tests = unittest.TestSuite()

    def addTestModule(m):
        newtests = unittest.defaultTestLoader.loadTestsFromModule(m)
        tests.addTests(newtests)

    # just run the docker tests this time
    import docker_test
    addTestModule(docker_test)
    
    runner = unittest.TextTestRunner()
    result = runner.run(tests)


