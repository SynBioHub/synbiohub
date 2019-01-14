import requests, difflib, sys
import re

save_for_testing = False

number_of_errors = 0

def test_print(string):
    sys.stdout.write("[synbiohub test] ")
    sys.stdout.write(string)
    sys.stdout.write("\n")

# make html a little more human readable
def format_html(htmlstring):
    # for two angle brackets, insert a new line
    newstring = re.sub('><', '>\n<', htmlstring)
    # for an ending angle bracket and then something else, enter a new line
    newstring = re.sub('>(.)', r'>\n\1', newstring)
    # for something else then an angle bracket, also insert a new line
    newstring = re.sub('(.)<', r'\1\n<', newstring)
    
    return newstring

# perform a get request
def get_request(request):
    if request[0] == '/':
        request = request[1:]
    response = requests.get('http://localhost:7777/' + request)
    
    response.raise_for_status()
    
    content = format_html(response.text)

    return content

# perform and save a get request in previous results
def save_get_request(request):
    content = get_request(request)

    filepath = 'previousresults/getrequest_' + request + '.txt'
    
    with open(filepath, 'w') as rfile:
        rfile.write(content)

# creates a file path for a given request and request type
def request_file_path(request, requesttype):
    return 'previousresults/' + requesttype.replace(" ", "") + "_" + request + ".txt"
        
# check a request against previous results
# request should the page that was requested, such as /setup
# requesttype is the type of request performed, either "get request" or "post request"
def compare_request(requestcontent, request, requesttype):
    # if the global state is to replace all files, do that
    if save_for_testing:
        with open(request_file_path(request, requesttype), 'w') as rfile:
            rfile.write(requestcontent)
    else:
        
        filepath = request_file_path(request, requesttype)
        
        olddata = None
        try:
            with open (filepath, "r") as oldfile:
                olddata=oldfile.read()
        except IOError as e:
            raise Exception("Could not open previous result for the " + \
                            requesttype + " " + request + ". If the saved result has not yet been created because it is a new page, please use TODO to create the file.")

        olddata = olddata.splitlines()
        newdata = requestcontent.splitlines()
        
        changes = difflib.unified_diff(olddata, newdata)

        # temp variable to detect if we need to print the beginning of the error
        firstchangep = True
        
        for c in changes:
            if firstchangep:
                test_print(requesttype + " " + request + " did not match previous results. If you are adding changes to SynBioHub that change this page, please check that the page is correct and update the file using TODO.\nThe following is a diff of the new files compared to the old.\n")
                global number_of_errors
                number_of_errors += 1
                firstchangep = False

            # print the diff
            sys.stdout.write(c)
            sys.stdout.write("\n")

    
# request- string, the name of the page being requested
def compare_get_request(request):
    compare_request(get_request(request), request, "get request")

def exit_tests():
    # finally, if there were any errors, raise an Exception
    if number_of_errors > 0:
        raise ValueError("[synbiohub test] " + str(number_of_errors) + " tests failed.")

