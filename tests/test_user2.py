from unittest import TestCase
from test_functions import compare_get_request, compare_post_request, login_with
from test_arguments import test_print

class TestUser2(TestCase):

    def test_post_login_token(self):
        test_print("test_post_login_token starting")
        logininfo = {'email' : 'test@user.synbiohub',
                      'password' : 'test'}
        login_with(logininfo)
        test_print("test_post_login_token completed")
