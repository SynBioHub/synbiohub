from unittest import TestCase
from test_arguments import test_print
from test_functions import compare_get_request, compare_post_request, login_with

class TestUser(TestCase):

    def test_post_register(self):
        test_print("test_post_register starting")
        data={
            'username': 'testuser2',
            'name' : 'ronald',
            'affiliation' : 'synbiohubtester',
            'email' : 'test2@user.synbiohub',
            'password1' : 'test1',
            'password2' : 'test1'
        }
        compare_post_request("register", data, headers = {"Accept": "text/plain"}, test_name = "register1")

        logininfo = {'email' : 'test2@user.synbiohub',
                     'password' : 'test1'}
        login_with(logininfo)

        compare_get_request("/profile")

        data={
             'name': 'ronnie',
             'affiliation' : 'notcovid',
             'email' : 'ronnie@user.synbiohub',
             'password1' : 'test',
             'password2' : 'test'
         }
        compare_post_request("profile", data, headers = {"Accept": "text/plain"}, test_name = "profile2")

        compare_get_request("/logout")

        test_print("test_post_register completed")

        test_print("test_post_login_token starting")
        logininfo = {'email' : 'test@user.synbiohub',
                      'password' : 'test'}
        login_with(logininfo)
        test_print("test_post_login_token completed")

