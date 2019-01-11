import requests, difflib

saveForTesting = True

# request- string, the name of the page being requested
# todo- test and use, currently a rough outline
def comparegetrequest(request):
    if request[0] == '/':
        request = request[1:]
    response = requests.get('http://localhost:7777/' + request)

    filepath = 'tests/previousresults/getrequest_' + request + '.txt'
    if saveForTesting:
        with open(filepath, 'w') as rfile:
            rfile.write(response.text)
    else:
        olddata = None
        with open (filepath, "r") as oldfile:
            olddata=oldfile.readlines

        
        newdata = request.text.splitlines()
        
        
        changes = difflib.unified_diff(olddata, newdata)

        for c in changes:
            print(c)


setup = {
    'userName' : 'testuser',
    'userEmail': 'test@user.synbiohub',
    'userPassword': 'test',
    'userPasswordConfirm': 'test',
    'instanceName': 'Test Synbiohub',
    'instanceURL': 'http://localhost:7777/',
    'uriPrefix': 'http://localhost:7777/',
    'color': '#D25627',
    'frontPageText': 'text',
    'virtuosoINI': '/etc/virtuoso-opensource-7/virtuoso.ini',
    'virtuosoDB': '/var/lib/virtuoso-opensource-7/db',
    'allowPublicSignup': 'true',
}


response = requests.post('http://localhost:7777/setup', data=setup)


with open("testoutput.html", "w") as f:
    f.write(response.text)
response.raise_for_status()

