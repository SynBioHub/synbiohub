import re


from test_arguments import test_print

class TestState:

    def __init__(self):
        # first create the list of all endpoints that should be checked
        self.all_get_endpoints = []
        self.all_post_endpoints = []
        self.all_all_endpoints = []

        # scrape app.js for the endpoints
        with open("../lib/app.js", 'r') as appfile:
            line = appfile.readline()
            while line:
                # regex parts:
                # look for any number of any character, then app.get
                # then loop for an open paren followed by either ' or "
                # capture what is in between that and another ' or ", and match it non-greedily
                # then match anything after it
                search = re.search('.*app\.get\((?:\'|")(.*?)(?:\'|").*', line)

                if search:
                    self.all_get_endpoints.append(search.group(1))

                search = re.search('.*app\.post\((?:\'|")(.*?)(?:\'|").*', line)

                if search:
                    self.all_post_endpoints.append(search.group(1))

                search = re.search('.*app\.all\((?:\'|")(.*?)(?:\'|").*', line)

                if search:
                    self.all_all_endpoints.append(search.group(1))

                line = appfile.readline()

        

        # keep track of all the endpoints tested to make sure all endopints are checked
        self.tested_get_endpoints = []
        self.tested_post_endpoints = []

        # keep track of the names of the tests to avoid duplicates
        self.all_tested_paths = []


    def cleanup_check(self):
        nottestedcounter = 0

        for e in self.all_get_endpoints:
            if not e in self.tested_get_endpoints:
                nottestedcounter += 1
                test_print("Warning- get endpoint " + e + " was not tested.")

        for e in self.all_post_endpoints:
            if not e in self.tested_post_endpoints:
                nottestedcounter += 1
                test_print("Warning- post endpoint " + e + " was not tested.")

        for e in self.all_all_endpoints:
            if not e in self.tested_get_endpoints and not e in self.tested_post_endpoints:
                nottestedcounter += 1
                test_print("Warning- all endpoint " + e + " was not tested.")

        if nottestedcounter != 0:
            test_print(str(nottestedcounter) + " endpoints not tested.")



    def add_post_request(self, request, testpath, test_name):

        # add to the global list of checked endpoints
        if not request in self.tested_post_endpoints:
            self.tested_post_endpoints.append(request)

        # error if it was already registered
        if testpath in self.all_tested_paths:
            if test_name == "":
                test_name = "none specified"
            raise Exception("Duplicate test name for post request " + request + " with test name " + test_name + ". When testing an endpoint multiple times, provide the test_name field to compare_post_request.")
        else:
            self.all_tested_paths.append(testpath)

    def add_get_request(self, request, testpath, test_name):
        # add to the global list of checked endpoints
        if not request in self.tested_get_endpoints:
            self.tested_get_endpoints.append(request)


        if testpath in self.all_tested_paths:
            if test_name == "":
                test_name = "none specified"
            raise Exception("Duplicate test name for get request " + request + " with test name " + test_name + ". When testing an endpoint multiple times, provide the test_name field to compare_get_request.")
        else:
            self.all_tested_paths.append(testpath)
