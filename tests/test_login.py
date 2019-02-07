from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestSetup(TestCase):

    def test_post_login_admin(self):
        logininfo = {'email' : 'test@synbiohub.org',
                     'password' : 'test'}
        compare_post_request("/login", logininfo)


