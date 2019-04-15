import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestAdminViews(TestCase):

    def test_admin_status(self):
        compare_get_request("/admin")

    def test_admin_explorer(self):
        compare_get_request("/admin/explorer")

    def test_admin_users(self):
        compare_get_request("/admin/users")

    def test_admin_mail(self):
        compare_get_request("/admin/mail")

    def test_admin_plugins(self):
        compare_get_request("/admin/plugins")

    def test_admin_graphs(self):
        compare_get_request("/admin/graphs")

    def test_admin_remotes(self):
        compare_get_request("/admin/remotes")

    def test_admin_registries(self):
        compare_get_request("admin/registries")

    def test_admin_theme(self):
        compare_get_request("/admin/theme")

    def test_admin_theme(self):
        compare_get_request("/admin/sparql")

    # TODO: fix backup in docker containers
    #def test_admin_backup(self):
    #    compare_get_request("admin/backup")

    def test_admin_log(self):
        compare_get_request("admin/log")
    
