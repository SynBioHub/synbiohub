import requests, difflib, sys
import re
from bs4 import BeautifulSoup

from test_arguments import args, test_print

# first create the list of all endpoints that should be checked
all_get_endpoints = []
all_post_endpoints = []
all_all_endpoints = []

# keep track of all the endpoints tested to make sure all endopints are checked
tested_get_endpoints = []
tested_post_endpoints = []

# keep track of the names of the tests to avoid duplicates
all_tested_paths = []

def clip_request(requeststring):
    if requeststring[0] == '/':
        return requeststring[1:]
    else:
        return requeststring

# now clip all the requests in the ones to reset
for i in range(len(args.resetgetrequests)):
    args.resetgetrequests[i] = clip_request(args.resetgetrequests[i])

for i in range(len(args.resetpostrequests)):
    args.resetpostrequests[i] = clip_request(args.resetpostrequests[i])
    


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
            all_get_endpoints.append(search.group(1))

        search = re.search('.*app\.post\((?:\'|")(.*?)(?:\'|").*', line)

        if search:
            all_post_endpoints.append(search.group(1))

        search = re.search('.*app\.all\((?:\'|")(.*?)(?:\'|").*', line)

        if search:
            all_all_endpoints.append(search.group(1))
        
        line = appfile.readline()



# make html a little more human readable and remove testignore elements
def format_html(htmlstring):
    soup = BeautifulSoup(htmlstring, 'lxml')

    # remove elements with class testignore
    for div in soup.find_all(class_="testignore"):
        div.decompose()
    
    return soup.prettify()

# perform a get request
def get_request(request, headers):
    
    response = requests.get(args.serveraddress + request, headers = headers)
    
    response.raise_for_status()
    
    content = format_html(response.text)

    return content

# data is the data field for a request
def post_request(request, data, headers):
    address = args.serveraddress + request

    response = requests.post(address, data = data, headers = headers)
    response.raise_for_status()
    
    content = format_html(response.text)
    return content


# creates a file path for a given request and request type
# testname is a name to avoid collisions between tests testing the same endpoint
def request_file_path(request, requesttype, testname):
    return 'previousresults/' + requesttype.replace(" ", "") + "_" + request + "_" + testname + ".html"


# TODO: add functionality of route_parameters 
def compare_request(requestcontent, request, requesttype, route_parameters, file_path):
    """ Checks a request against previous results or saves the result of a request.
request is the endpoint requested, such as /setup
requesttype is the type of request performed- either 'get request' or 'post request'"""
    # if the global state is to replace all files, do that
    if args.resetalltests:
        with open(file_path, 'w') as rfile:
            rfile.write(requestcontent)
    elif requesttype[0:3] == "get" and request in args.resetgetrequests:
        test_print("resetting get request " + request + " and saving to file " + file_path)
        with open(file_path, 'w') as rfile:
            rfile.write(requestcontent)
            
    elif requesttype[0:4] == "post" and request in args.resetpostrequests:
        test_print("resetting post request " + request + " and saving to file " + file_path)
        with open(file_path, 'w') as rfile:
            rfile.write(requestcontent)
            
    else:      
        
        olddata = None
        try:
            with open (file_path, "r") as oldfile:
                olddata=oldfile.read()
        except IOError as e:
            raise Exception("\n[synbiohub test] Could not open previous result for the " + \
                            requesttype + " " + request + ". If the saved result has not yet been created because it is a new page, please use --resetgetrequests [requests] or --resetpostrequests [requests] to create the file.") from e

        olddata = olddata.splitlines()
        newdata = requestcontent.splitlines()
        
        changes = difflib.unified_diff(olddata, newdata)

        

        # change list holds the strings to print in an error message
        changelist = [requesttype, " ", request, " did not match previous results. If you are adding changes to SynBioHub that change this page, please check that the page is correct and update the file using the command line argument --resetgetrequests [requests] and --resetpostrequests [requests].\nThe following is a diff of the new files compared to the old.\n"]

        # temp variable to detect if we need to print the beginning of the error
        numofchanges = 0
        
        for c in changes:
            numofchanges += 1

            # print the diff
            changelist.append(c)
            changelist.append("\n")

        if numofchanges>0:
            raise ValueError(''.join(changelist))




def compare_get_request(request, test_name = "", route_parameters = {}, headers = {}):
    """Complete a get request and error if it differs from previous results.

    request -- string, the name of the page being requested
    route_parameters -- a dictionary, with keys as the parameters and values as the values to replace the parameters with"""

    # remove any leading forward slashes for consistency
    request = clip_request(request)
    
    # add to the global list of checked endpoints
    tested_get_endpoints.append(request)

    # add to the global list of checked endpoints
    if not request in tested_get_endpoints:
        tested_get_endpoints.append(request)

        
    testpath = request_file_path(request, "get request", test_name)
    if testpath in all_tested_paths:
        if test_name == "":
            test_name = "none specified"
        raise Exception("Duplicate test name for get request " + request + " with test name " + test_name + ". When testing an endpoint multiple times, provide the test_name field to compare_get_request.")
    else:
        all_tested_paths.append(testpath)
        
    compare_request(get_request(request, headers), request, "get request", route_parameters, testpath)


def compare_post_request(request, data, test_name = "", route_parameters = {}, headers = {}):
    """Complete a post request and error if it differs from previous results.
    
    request-- string, the name of the page being requested
    data -- data to send in the post request
    route_parameters -- a dictionary, with keys as the parameters and values as the values to replace the parameters with
    test_name -- a name for the test to make multiple tests for the same endpoint unique"""

    # remove any leading forward slashes for consistency
    request = clip_request(request)

    
    # add to the global list of checked endpoints
    if not request in tested_post_endpoints:
        tested_post_endpoints.append(request)

        
    testpath = request_file_path(request, "post request", test_name)
    if testpath in all_tested_paths:
        if test_name == "":
            test_name = "none specified"
        raise Exception("Duplicate test name for post request " + request + " with test name " + test_name + ". When testing an endpoint multiple times, provide the test_name field to compare_post_request.")
    else:
        all_tested_paths.append(testpath)
    
        
    compare_request(post_request(request, data, headers), request, "post request", route_parameters, testpath)
    

# TODO: make checking throw an error when all endpoints are not checked, instead of printing a warning.
def cleanup_check():
    """Performs final checking after all tests have run.
    Checks to make sure all endpoints were tested."""

    nottestedcounter = 0
    
    for e in all_get_endpoints:
        e = clip_request(e)
        if not e in tested_get_endpoints:
            nottestedcounter += 1
            test_print("Warning- get endpoint " + e + " was not tested.")

    for e in all_post_endpoints:
        e = clip_request(e)
        if not e in tested_post_endpoints:
            nottestedcounter += 1
            test_print("Warning- post endpoint " + e + " was not tested.")

    for e in all_all_endpoints:
        e = clip_request(e)
        if not e in tested_get_endpoints and not e in tested_post_endpoints:
            nottestedcounter += 1
            test_print("Warning- all endpoint " + e + " was not tested.")

    if nottestedcounter != 0:
        test_print(str(nottestedcounter) + " endpoints not tested.")
