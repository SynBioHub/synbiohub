from unittest import TestCase
from test_functions import compare_get_request, compare_post_request, login_with

#/register is tested within the docker_test_setup.py file

class TestUser(TestCase):

    def test_post_login_token(self):
        logininfo = {'email' : 'test@user.synbiohub',
                     'password' : 'test'}
        login_with(logininfo)

    # GET <SynBioHub URL>/logout
    def test_logout(self):
        compare_get_request("/logout")

    def test_post_login_token(self):
        logininfo = {'email' : 'test@user.synbiohub',
                     'password' : 'test'}
        login_with(logininfo)

    # GET <SynBioHub URL>/profile
    def test_profile(TestCase):
        compare_get_request("/profile")

    # POST <SynBioHub URL>/profile
    def test_post_profile(self):
        data={
            'name': 'Jimmy',
            'affiliation' : 'NerdsInc',
            'email' : '',
            'password1' : '<password1>',
            'password2' : '<password2>'}
        compare_post_request("profile", data, headers = {"Accept":\
            "text/plain"}, test_name = "test_update_profile")

