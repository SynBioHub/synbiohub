from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestSetup(TestCase):

    def test_get_main_page(self):
        compare_get_request("/")

    def test_get_login(self):
        compare_get_request("/login")

    def test_post_bad_login(self):
        bad_login_info = {'email' : 'bademail',
                          'password' : 'test'}
        compare_post_request("/login", bad_login_info, test_name='bad_admin_login')

    def test_no_username_login(self):
        no_email_info = {'password' : 'test'}
        compare_post_request("/login", no_email_info, test_name='no_email_login')


    def test_post_login_admin(self):
        logininfo = {'email' : 'test@user.synbiohub',
                     'password' : 'test'}
        compare_post_request("/login", logininfo)
