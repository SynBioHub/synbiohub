import re
from bs4 import BeautifulSoup

from test_arguments import test_print

def clip_request(requeststring):
    if requeststring[0] == '/' and requeststring[-1] == '/':
        requeststring = requeststring[1:-1]
    elif requeststring[0] == '/':
        requeststring = requeststring[1:]
    elif requeststring[-1] == '/':
        requeststring = requeststring[:-1]
    return requeststring
    

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
                    self.all_get_endpoints.append(clip_request(search.group(1)))

                search = re.search('.*app\.post\((?:\'|")(.*?)(?:\'|").*', line)

                if search:
                    self.all_post_endpoints.append(clip_request(search.group(1)))

                search = re.search('.*app\.all\((?:\'|")(.*?)(?:\'|").*', line)

                if search:
                    self.all_all_endpoints.append(clip_request(search.group(1)))

                line = appfile.readline()

        

        # keep track of all the endpoints tested to make sure all endopints are checked
        self.tested_get_endpoints = []
        self.tested_post_endpoints = []

        # keep track of the names of the tests to avoid duplicates
        self.all_tested_paths = []

        # keep track of authetification after logging in
        self.login_authentification = None

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

        # test that all the endpoints that were tested were real endpoints
        for e in self.tested_get_endpoints:
            if not e in self.all_get_endpoints and not e in self.all_all_endpoints:
                raise Exception("Endpoint " + str(e) + " does not exist")

        for e in self.tested_post_endpoints:
            if not e in self.all_post_endpoints and not e in self.all_all_endpoints:
                raise Exception("Endpoint " + str(e) + " does not exist")

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

    # saves the result of a login request for future use
    def save_authentification(self, request_result):
        soup = BeautifulSoup(request_result, 'lxml')
        ptag = soup.find_all('p')
        if len(ptag)!= 1:
            raise ValueError("Invalid login response received- multiple or no elements in p tag.")
        content = ptag[0].text
        self.login_authentification = content.strip()
        
        test_print("Logging in with authentification " + str(self.login_authentification))

    def get_authentification(self):
        return self.login_authentification
