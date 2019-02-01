import requests, difflib, sys
import re
from bs4 import BeautifulSoup

from test_arguments import args, test_print

# first create the list of all endpoints that should be checked
all_get_endpoints = []
all_post_endpoints = []
all_all_endpoints = []

tested_get_endpoints = []
tested_post_endpoints = []

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
def get_request(request):
    
    response = requests.get(args.serveraddress + request)
    
    response.raise_for_status()
    
    content = format_html(response.text)

    return content

# data is the data field for a request
def post_request(request, data):
    address = args.serveraddress + request

    response = requests.post(address, data = data)
    response.raise_for_status()
    
    content = format_html(response.text)
    return content


# creates a file path for a given request and request type
def request_file_path(request, requesttype):
    return 'previousresults/' + requesttype.replace(" ", "") + "_" + request + ".html"


# TODO: add functionality of route_parameters 
def compare_request(requestcontent, request, requesttype, route_parameters):
    """ Checks a request against previous results or saves the result of a request.
request is the endpoint requested, such as /setup
requesttype is the type of request performed- either 'get request' or 'post request'"""
    # if the global state is to replace all files, do that
    if args.resetalltests:
        with open(request_file_path(request, requesttype), 'w') as rfile:
            rfile.write(requestcontent)
    elif requesttype[0:3] == "get" and request in args.resetgetrequests:
        filename = request_file_path(request, requesttype)
        test_print("resetting get request " + request + " and saving to file " + filename)
        with open(filename, 'w') as rfile:
            rfile.write(requestcontent)
    elif requesttype[0:4] == "post" and request in args.resetpostrequests:
        filename = request_file_path(request, requesttype)
        test_print("resetting post request " + request + " and saving to file " + filename)
        with open(filename, 'w') as rfile:
            rfile.write(requestcontent)
    else:      
        filepath = request_file_path(request, requesttype)
        
        olddata = None
        try:
            with open (filepath, "r") as oldfile:
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


def clip_request(requeststring):
    if requeststring[0] == '/':
        return requeststring[1:]
    else:
        return requeststring
    

def compare_get_request(request, route_parameters = {}):
    """Complete a get request and error if it differs from previous results.

    request -- string, the name of the page being requested
    route_parameters -- a dictionary, with keys as the parameters and values as the values to replace the parameters with"""

    # add to the global list of checked endpoints
    tested_get_endpoints.append(request)

    # remove any leading forward slashes for consistency
    request = clip_request(request)
    compare_request(get_request(request), request, "get request", route_parameters)


def compare_post_request(request, data, route_parameters = {}):
    """Complete a post request and error if it differs from previous results.
    
    request-- string, the name of the page being requested
    data -- data to send in the post request
    route_parameters -- a dictionary, with keys as the parameters and values as the values to replace the parameters with"""

    # add to the global list of checked endpoints
    tested_post_endpoints.append(request)
    
    # remove any leading forward slashes for consistency
    request = clip_request(request)
        
    compare_request(post_request(request, data), request, "post request", route_parameters)
    

# TODO: make checking throw an error when all endpoints are not checked, instead of printing a warning.
def cleanup_check():
    """Performs final checking after all tests have run.
    Checks to make sure all endpoints were tested."""


    
    for e in all_get_endpoints:
        e = clip_request(e)
        if not e in tested_get_endpoints:
            sys.stdout.write("[synbiohub test] Warning- get endpoint " + e + " was not tested.\n")

    for e in all_post_endpoints:
        e = clip_request(e)
        if not e in tested_post_endpoints:
            sys.stdout.write("[synbiohub test] Warning- post endpoint " + e + " was not tested.\n")

    for e in all_all_endpoints:
        e = clip_request(e)
        if not e in tested_get_endpoints and not e in tested_post_endpoints:
            sys.stdout.write("[synbiohub test] Warning- all endpoint " + e + " was not tested.\n")
