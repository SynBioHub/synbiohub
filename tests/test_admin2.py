import requests
import os
from unittest import TestCase
from test_arguments import test_print
from test_functions import compare_get_request, compare_post_request

class TestAdmin2(TestCase):
    def test_admin2(self):
        # test_admin_newUser(self):
        test_print("test_admin_newUser starting")
        compare_get_request("/admin/newUser", test_name = "test_new_user")
        test_print("test_admin_newUser completed")


        # test_admin_deletePlugin(self):
        test_print("test_admin_deletePlugin starting")
        data={
            'id': '1',
            'category' : 'download',
        }
        compare_post_request("/admin/deletePlugin", data, headers = {"Accept": "text/plain"}, test_name = "admin_deletePlugin")
        test_print("test_admin_deletePlugin completed")

        # test_admin_deleteRegistry(self):
        test_print("test_admin_deleteRegistry starting")
        data={
            'uri': 'testurl.com',
        }
        compare_post_request("/admin/deleteRegistry", data, headers = {"Accept": "text/plain"}, test_name = "admin_deleteRegistry")
        test_print("test_admin_deleteRegistry completed")

        # test_admin_deleteRemoteBenchling(self):
        test_print("test_admin_deleteRemoteBenchling starting")
        data={
            'id': '1',
        }
        compare_post_request("/admin/deleteRemote", data, headers = {"Accept": "text/plain"}, test_name = "admin_deleteRemoteBenchling")
        test_print("test_admin_deleteRemoteBenchling completed")

        # test_admin_deleteRemoteICE(self):
        test_print("test_admin_deleteRemoteICE starting")
        data={
            'id': 'test',
        }
        compare_post_request("/admin/deleteRemote", data, headers = {"Accept": "text/plain"}, test_name = "admin_deleteRemoteICE")
        test_print("test_admin_deleteRemoteICE completed")

        # test_admin_DeleteUser(self):
        test_print("test_admin_DeleteUser starting")
        data={
            'id': '2',
        }
        compare_post_request("/admin/deleteUser", data, headers = {"Accept": "text/plain"}, test_name = "admin_deleteUser")
        test_print("test_admin_DeleteUser completed")

        # test_newUser(self):
        test_print("test_newUser starting")
        data = {
            'username': 'adminNewUser',
            'name' : 'adminNewUser',
            'email' : 'adminNewUser@user.synbiohub',
            'affiliation' : 'adminNewUser',
            'isMember' : '1',
            'isCurator' : '1',
            'isAdmin' : '1',
        }
        compare_post_request("/admin/newUser", data, headers = {"Accept": "text/plain"}, test_name = "admin_newUser333")
        test_print("test_newUser completed")

#    def test_admin_federate(self):
#        data={
#            'administratorEmail': 'myers@ece.utah.edu',
#            'webOfRegistries' : 'https://wor.synbiohub.org',
#        }
#        compare_post_request("/admin/federate", data, headers = {"Accept": "text/plain"}, test_name = "admin_federate")


