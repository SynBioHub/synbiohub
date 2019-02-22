import requests, difflib, sys
from bs4 import BeautifulSoup

from test_arguments import args, test_print
from TestState import TestState, clip_request

test_state = TestState()


# now clip all the requests in the ones to reset
for i in range(len(args.resetgetrequests)):
    args.resetgetrequests[i] = clip_request(args.resetgetrequests[i])

for i in range(len(args.resetpostrequests)):
    args.resetpostrequests[i] = clip_request(args.resetpostrequests[i])


# make html a little more human readable and remove testignore elements
def format_html(htmlstring):
    soup = BeautifulSoup(htmlstring, 'lxml')

    # remove elements with class testignore
    for div in soup.find_all(class_="testignore"):
        div.decompose()
    
    return soup.prettify()


# parse the address of the endpoint being tested
def get_address(request, route_parameters):
    return args.serveraddress + request

# perform a get request
def get_request(request, headers, route_parameters):
    
    # get the current token
    user_token = test_state.get_authentification()
    if user_token != None:
        headers["X-authorization"] = user_token

    address = get_address(request, route_parameters)
        
    response = requests.get(address, headers = headers)
    
    response.raise_for_status()
    
    content = format_html(response.text)

    return content

# data is the data field for a request
def post_request(request, data, headers, route_parameters, files):
    # get the current token
    user_token = test_state.get_authentification()
    if user_token != None:
        headers["X-authorization"] = user_token
    
    address = get_address(request, route_parameters)

    response = requests.post(address, data = data, headers = headers, files = files)
    
    response.raise_for_status()
    
    content = format_html(response.text)
    return content


# creates a file path for a given request and request type
# testname is a name to avoid collisions between tests testing the same endpoint
def request_file_path(request, requesttype, testname):
    return 'previousresults/' + requesttype.replace(" ", "") + "_" + request + "_" + testname + ".html"



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
        changelist = [requesttype, " ", file_path, " did not match previous results. If you are adding changes to SynBioHub that change this page, please check that the page is correct and update the file using the command line argument --resetgetrequests [requests] and --resetpostrequests [requests].\nThe following is a diff of the new files compared to the old.\n"]

        # temp variable to detect if we need to print the beginning of the error
        numofchanges = 0
        
        for c in changes:
            numofchanges += 1

            # print the diff
            changelist.append(c)
            changelist.append("\n")

        if numofchanges>0:
            raise ValueError(''.join(changelist))



def login_with(data, headers = {'Accept':'text/plain'}):
    result = post_request("login", data, headers, {}, files = None)
    test_state.save_authentification(result)


def compare_get_request(request, test_name = "", route_parameters = {}, headers = {}):
    """Complete a get request and error if it differs from previous results.

    request -- string, the name of the page being requested
    route_parameters -- a dictionary, with keys as the parameters and values as the values to replace the parameters with"""

    # remove any leading forward slashes for consistency
    request = clip_request(request)
        
    testpath = request_file_path(request, "get request", test_name)
    test_state.add_get_request(request, testpath, test_name)
        
    compare_request(get_request(request, headers, route_parameters), request, "get request", route_parameters, testpath)


def compare_post_request(request, data, test_name = "", route_parameters = {}, headers = {}, files = None):
    """Complete a post request and error if it differs from previous results.
    
    request-- string, the name of the page being requested
    data -- data to send in the post request
    route_parameters -- a dictionary, with keys as the parameters and values as the values to replace the parameters with
    test_name -- a name for the test to make multiple tests for the same endpoint unique"""

    # remove any leading forward slashes for consistency
    request = clip_request(request)
        
    testpath = request_file_path(request, "post request", test_name)

    test_state.add_post_request(request, testpath, test_name)
    
        
    compare_request(post_request(request, data, headers, route_parameters, files = files), request, "post request", route_parameters, testpath)
    
# TODO: make checking throw an error when all endpoints are not checked, instead of printing a warning.
def cleanup_check():
    """Performs final checking after all tests have run.
    Checks to make sure all endpoints were tested."""

    test_state.cleanup_check()
