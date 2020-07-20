import requests
import os
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestAdmin(TestCase):

    def test_admin_status(self):

      compare_get_request("/admin")
#    def test_admin_explorer(self):
#        compare_get_request("/admin/explorer")

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
            'key': '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',
            'fromEmail' : 'synbiohub@synbiohub.utah.edu',
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

    def test_admin_saveRegistry(self):
        data={
            'uri': 'testurl.com',
            'url' : 'testurl.com',
        }
        compare_post_request("/admin/saveRegistry", data, headers = {"Accept": "text/plain"}, test_name = "admin_saveRegistry")

    def test_admin_registries(self):
        compare_get_request("admin/registries")

    def test_admin_retreiveFromWebOfRegistries(self):
        data={
        }
        compare_post_request("/admin/retrieveFromWebOfRegistries", data, headers = {"Accept": "text/plain"}, test_name = "admin_retrieveFromWebOfRegistries")

    def test_admin_federate(self):
        data={
            'administratorEmail': 'myers@ece.utah.edu',
            'webOfRegistries' : 'http://wor.synbiohub.org',
        }
        compare_post_request("/admin/federate", data, headers = {"Accept": "text/plain"}, test_name = "admin_federate")

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
            'baseColor' : 'A32423',
            'showModuleInteractions' : 'ok',
        }
        files={
            'logo' : (logo, open('./logo.jpg', 'rb')),
        }
        compare_post_request("/admin/theme", data, headers = {"Accept": "text/plain"}, files = files, test_name = "admin_setAdministratorEmail")

    def test_updateExplorerConfig(self):
        data={
            'useSBOLExplorer': 'True',
            'SBOLExplorerEndpoint' : 'http://explorer:13162/',
            'useDistributedSearch' : 'True',
            'pagerankTolerance' : '.0002',
            'uclustIdentity' : '0.9',
            'synbiohubPublicGraph' : '',
            'elasticsearchEndpoint' : 'http://elasticsearch:9200/',
            'elasticsearchIndexName' : 'part',
            'sparqlEndpoint' : 'http://virtuoso:8890/sparql?'
        }
        compare_post_request("/admin/explorer", data, headers = {"Accept": "text/plain"}, test_name = "admin_updateExplorerConfig")

    def test_admin_explorer(self):
        compare_get_request("/admin/explorer")

    def test_explorerUpdateIndex(self):
        data={
        }
        compare_post_request("/admin/explorerUpdateIndex", data, headers = {"Accept": "text/plain"}, test_name = "admin_explorerUpdateIndex")

    def test_saveRemoveICE(self):
        data={
            'type': 'ice',
            'id' : 'test',
            'url' : 'test.com',
            'iceApiTokenClient' : 'test',
            'iceApiToken' : 'test',
            'iceApiTokenOwner' : 'test',
            'iceCollection' : 'test',
            'rejectUnauthorized' : 'True',
            'folderPrefix' : 'test',
            'sequenceSuffix' : 'test',
            'defaultFolderId' : 'test',
            'groupId' : 'test',
            'pi' : 'test',
            'piEmail' : 'test',
            'isPublic' : 'True',
            'partNumberPrefix' : 'test',
            'rootCollectionDisplayId' : 'test',
            'rootCollectionName' : 'test',
            'rootCollectionDescription' : 'test'
        }
        compare_post_request("/admin/saveRemote", data, headers = {"Accept": "text/plain"}, test_name = "admin_saveRemoteICE")

    def test_saveRemoveBenchling(self):
        data={
            'type': 'benchling',
            'id': '1',
            'benchlingApiToken': 'test',
            'rejectUnauthorized': 'test',
            'folderprefix': 'test',
            'defaultFolderId': 'test',
            'isPublic': 'True',
            'rootCollectionsDisplayId': 'test',
            'rootCollectionName': 'test',
            'rootCollectionDescription': 'test'
        }
        compare_post_request("/admin/saveRemote", data, headers = {"Accept": "text/plain"}, test_name = "admin_saveRemoteBenchling")

#    def test_newUser(self):
#        data = {
#            'username': 'adminNewUser',
#            'name' : 'adminNewUser',
#            'email' : 'adminNewUser@user.synbiohub',
#            'affiliation' : 'adminNewUser',
#            'isMember' : '1',
#            'isCurator' : '1',
#            'isAdmin' : '1',
#        }
#        compare_post_request("/admin/newUser", data, headers = {"Accept": "text/plain"}, test_name = "admin_newUser")

#    def test_updateUserConfig(self):
#        data={
#            'allowPublicSignup': 'False',
#        }
#        compare_post_request("/admin/users", data, headers = {"Accept": "text/plain"}, test_name = "admin_updateUsersConfig")
    def test_updateUser(self):
        data={
            'id': '2',
            'name' : 'ronnieUpdated',
            'email' : 'ronnieUpdated@user.synbiohub',
            'affiliation' : 'updatedAffiliation',
            'isMember' : '1',
            'isCurator' : '1',
            'isAdmin' : '1'
        }
        compare_post_request("/admin/updateUser", data, headers = {"Accept": "text/plain"}, test_name = "admin_updateUser")
