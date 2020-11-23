from unittest import TestCase
from test_functions import compare_get_request, compare_post_request
from test_arguments import test_print


class TestSetup(TestCase):

    def test_get(self):

        test_print("test_setup_get starting")

        # get the setup page and test it before setting up
        compare_get_request("setup")

        test_print("test_setup_get completed")

    def test_post(self):

        test_print("test_setup_post starting")

        # fill in the form and submit with test info
        setup = {
            'userName' : 'testuser',
            'userFullName' : 'Test User',
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

        compare_post_request('setup', setup)

        test_print("test_setup_post completed")



