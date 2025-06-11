from unittest import TestCase
from test_functions import compare_get_request, compare_post_request
from test_arguments import test_print


class TestBeforeLogin(TestCase):

    def test_before_login(self):

        # test_get_main_page(self):
        test_print("test_get_main_page starting")
        compare_get_request("/")
        test_print("test_get_main_page completed")

        # test_get_login(self):
        test_print("test_get_login starting")
        compare_get_request("/login")
        test_print("test_get_login completed")

        # test_post_bad_login(self):
        test_print("test_post_bad_login starting")
        bad_login_info = {'email' : 'bademail',
                          'password' : 'test'}
        compare_post_request("/login", bad_login_info, test_name='bad_admin_login')
        test_print("test_post_bad_login completed")

        # test_no_username_login(self):
        test_print("test_no_username_login starting")
        no_email_info = {'password' : 'test'}
        compare_post_request("/login", no_email_info, test_name='no_email_login')
        test_print("test_no_username_login completed")

        # test_post_login_admin(self):
        test_print("test_post_login_admin starting")
        logininfo = {'email' : 'test@user.synbiohub',
                     'password' : 'test'}
        compare_post_request("/login", logininfo)
        test_print("test_post_login_admin completed")
