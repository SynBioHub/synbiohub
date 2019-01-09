import requests

setupfiles = {
    'userEmail': (None, 'test@user.synbiohub'),
    'userPassword': (None, 'test'),
    'userPasswordConfirm': (None, 'test'),
    'instanceName': (None, 'Test Synbiohub'),
    'instanceURL': (None, 'http://localhost:7777/'),
    'uriPrefix': (None, 'http://localhost:7777/'),
    'color': (None, '#D25627'),
    'frontPageText': (None, 'text'),
    'virtuosoINI': (None, '/etc/virtuoso-opensource-7/virtuoso.ini'),
    'virtuosoDB': (None, '/var/lib/virtuoso-opensource-7/db'),
    'allowPublicSignup': (None, 'true'),
}

response = requests.post('http://localhost:7777/setup', files=setupfiles, verify=False)


with open("testoutput.html", "w") as f:
    f.write(response.text)
response.raise_for_status()

