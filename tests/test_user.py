from unittest import TestCase
from test_functions import compare_get_request, compare_post_request, login_with

#/register is tested within the docker_test_setup.py file

class TestUser(TestCase):

    def test_post_login_token(self):
        logininfo = {'email' : 'test@user.synbiohub',
                     'password' : 'test'}
        login_with(logininfo)

    def test_logout(self):
        compare_get_request("/logout")

    def test_post_login_token(self):
        logininfo = {'email' : 'test@user.synbiohub',
                     'password' : 'test'}
        login_with(logininfo)

    def test_profile(TestCase):
        compare_get_request("/profile")


