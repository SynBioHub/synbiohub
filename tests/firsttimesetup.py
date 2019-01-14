import requests
from test_functions import compare_get_request



# get the setup page and test it before setting up
compare_get_request("setup")


# fill in the form and submit with test info
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


response.raise_for_status()


