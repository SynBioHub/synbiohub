import requests, difflib, sys
import re
from bs4 import BeautifulSoup

from test_arguments import args, test_print



# make html a little more human readable
def format_html(htmlstring):
    soup = BeautifulSoup(htmlstring, 'lxml')
    
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


def compare_request(requestcontent, request, requesttype):
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

        # temp variable to detect if we need to print the beginning of the error
        firstchangep = True

        # change list holds the strings to print in an error message
        changelist = [requesttype, " ", request, " did not match previous results. If you are adding changes to SynBioHub that change this page, please check that the page is correct and update the file using the command line argument --resetgetrequests [requests] and --resetpostrequests [requests].\nThe following is a diff of the new files compared to the old.\n"]
        
        for c in changes:
            if firstchangep:
                firstchangep = False

            # print the diff
            changelist.append(c)
            changelist.append("\n")

        if not firstchangep:
            raise ValueError(''.join(changelist))

    
# request- string, the name of the page being requested
def compare_get_request(request):
    if request[0] == '/':
        request = request[1:]
    compare_request(get_request(request), request, "get request")

# request- string, the name of the page being requested
# data- data to send in the post request
def compare_post_request(request, data):
    if request[0] == '/':
        request = request[1:]
    compare_request(post_request(request, data), request, "post request")
    
