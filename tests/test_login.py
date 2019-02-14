from unittest import TestCase
from test_functions import compare_get_request, compare_post_request, login_with


class TestSetup(TestCase):

    def test_post_login_token(self):
        logininfo = {'email' : 'test@user.synbiohub',
                     'password' : 'test'}
        login_with(logininfo)

    
