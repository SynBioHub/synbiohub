import requests
import os
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestAdmin(TestCase):

    def test_admin_status(self):
        compare_get_request("/admin")

    def test_admin_explorer(self):
        compare_get_request("/admin/explorer")

    def test_admin_users(self):
        compare_get_request("/admin/users")

    def test_admin_graphs(self):
        compare_get_request("/admin/graphs")

    def test_admin_remotes(self):
        compare_get_request("/admin/remotes")

    def test_admin_theme(self):
        compare_get_request("/admin/theme")

    def test_admin_newUser(self):
        compare_get_request("/admin/newUser")

    def test_admin_sparql(self):
        compare_get_request("/admin/sparql", headers = {"Accept": "text/html"})

    # TODO: fix backup in docker containers
    #def test_admin_backup(self):
    #    compare_get_request("admin/backup")

    #def test_admin_log(self):
    #    compare_get_request("admin/log")
    def test_admin_mail(self):
        data={
            'key': '123456',
            'fromEmail' : 'ron@test.synbiohub',
        }
        compare_post_request("/admin/mail", data, headers = {"Accept": "text/plain"}, test_name = "admin_mail")

    def test_admin_mail(self):
        compare_get_request("/admin/mail")

    def test_admin_savePlugin(self):
        data={
            'id': 'New',
            'category' : 'download',
            'name' : 'test_plugin',
            'url' : 'jimmy',
        }
        compare_post_request("/admin/savePlugin", data, headers = {"Accept": "text/plain"}, test_name = "admin_savePlugin")

    def test_admin_plugins(self):
        compare_get_request("/admin/plugins")

#    def test_admin_deletePlugin(self):
#        data={
#            'id': 'New',
#            'category' : 'download',
#        }
#        compare_post_request("/admin/deletePlugin", data, headers = {"Accept": "text/plain"}, test_name = "admin_deletePlugin")

    def test_admin_saveRegistry(self):
        data={
            'uri': 'testurl.com',
            'url' : 'testurl.com',
        }
        compare_post_request("/admin/saveRegistry", data, headers = {"Accept": "text/plain"}, test_name = "admin_saveRegistry")

    def test_admin_registries(self):
        compare_get_request("admin/registries")

#    def test_admin_deleteRegistry(self):
#        data={
#            'uri': 'testurl.com',
#        }
#        compare_post_request("/admin/deleteRegistry", data, headers = {"Accept": "text/plain"}, test_name = "admin_deleteRegistry")

    def test_admin_retreiveFromWebOfRegistries(self):
        data={
        }
        compare_post_request("/admin/retrieveFromWebOfRegistries", data, headers = {"Accept": "text/plain"}, test_name = "admin_retrieveFromWebOfRegistries")

#    def test_admin_federate(self):
#        data={
#            'administratorEmail': 'test@synbiohub.org>',
#            'webOfRegistries' : 'testwebOfRegist.com>',
#        }
#        compare_post_request("/admin/federate", data, headers = {"Accept": "text/plain"}, test_name = "admin_federate")

    def test_admin_setAdministratorEmail(self):
        data={
            'administratorEmail': 'test@synbiohub.org',
        }
        compare_post_request("/admin/setAdministratorEmail", data, headers = {"Accept": "text/plain"}, test_name = "admin_setAdministratorEmail")

    def test_admin_updateTheme(self):
        logo = os.path.basename('./logo.jpg');
        data={
            'instanceName': 'test_instance',
            'frontPageText' : 'test_instance',
            'baseColor' : '000000',
            'showModuleInteractions' : 'ok',
        }
        files={
            'logo' : (logo, open('./logo.jpg', 'rb')),
        }
        compare_post_request("/admin/theme", data, headers = {"Accept": "text/plain"}, files = files, test_name = "admin_setAdministratorEmail")


