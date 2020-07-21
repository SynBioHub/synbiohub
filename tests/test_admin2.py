import requests
import os
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request

class TestAdmin2(TestCase):

    def test_admin_deletePlugin(self):
        data={
            'id': '1',
            'category' : 'download',
        }
        compare_post_request("/admin/deletePlugin", data, headers = {"Accept": "text/plain"}, test_name = "admin_deletePlugin")

    def test_admin_deleteRegistry(self):
        data={
            'uri': 'testurl.com',
        }
        compare_post_request("/admin/deleteRegistry", data, headers = {"Accept": "text/plain"}, test_name = "admin_deleteRegistry")

    def test_admin_deleteRemoteBenchling(self):
        data={
            'id': '1',
        }
        compare_post_request("/admin/deleteRemote", data, headers = {"Accept": "text/plain"}, test_name = "admin_deleteRemoteBenchling")
    def test_admin_deleteRemoteICE(self):
        data={
            'id': 'test',
        }
        compare_post_request("/admin/deleteRemote", data, headers = {"Accept": "text/plain"}, test_name = "admin_deleteRemoteICE")

    def test_adminDeleteUser(self):
        data={
            'id': '2',
        }
        compare_post_request("/admin/deleteUser", data, headers = {"Accept": "text/plain"}, test_name = "admin_deleteUser")
    def test_newUser(self):
        data = {
            'username': 'adminNewUser',
            'name' : 'adminNewUser',
            'email' : 'adminNewUser@user.synbiohub',
            'affiliation' : 'adminNewUser',
            'isMember' : '1',
            'isCurator' : '1',
            'isAdmin' : '1',
        }
        compare_post_request("/admin/newUser", data, headers = {"Accept": "text/plain"}, test_name = "admin_newUser")

#    def test_admin_federate(self):
#        data={
#            'administratorEmail': 'myers@ece.utah.edu',
#            'webOfRegistries' : 'https://wor.synbiohub.org',
#        }
#        compare_post_request("/admin/federate", data, headers = {"Accept": "text/plain"}, test_name = "admin_federate")


