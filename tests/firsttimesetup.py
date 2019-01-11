import requests

setup = {
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

print(setup)

response = requests.post('http://localhost:7777/setup', data=setup)


with open("testoutput.html", "w") as f:
    f.write(response.text)
response.raise_for_status()

