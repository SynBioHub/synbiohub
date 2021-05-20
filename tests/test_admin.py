import requests
import os
from test_arguments import test_print
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request


class TestAdmin(TestCase):

    def test_admin1(self):
        # test_admin_status(self):
        test_print("test_admin_status starting")
        compare_get_request("/admin")
        test_print("test_admin_status completed")

        # test_admin_users(self):
        test_print("test_admin_users starting")
        compare_get_request("/admin/users")
        test_print("test_admin_users completed")

        # test_admin_graphs(self):
        test_print("test_admin_graphs starting")
        compare_get_request("/admin/graphs")
        test_print("test_admin_graphs completed")

        # test_admin_remotes(self):
        test_print("test_admin_remotes starting")
        compare_get_request("/admin/remotes")
        test_print("test_admin_remotes completed")

        # test_admin_theme(self):
        test_print("test_admin_theme starting")
        compare_get_request("/admin/theme")
        test_print("test_admin_theme completed")

        # test_admin_newUser(self):
        test_print("test_admin_newUser GET starting")
        compare_get_request("/admin/newUser")
        test_print("test_admin_newUser GET  completed")

        # test_admin_sparql(self):
        test_print("test_admin_sparql starting")
        compare_get_request("/admin/sparql", headers = {"Accept": "text/html"})
        test_print("test_admin_sparql completed")

        # TODO: fix backup in docker containers
        #def test_admin_backup(self):
        #    compare_get_request("admin/backup")

        #def test_admin_log(self):
        #    compare_get_request("admin/log")

        # test_admin_mail(self):
        test_print("test_admin_mail starting")
        compare_get_request("/admin/mail")
        test_print("test_admin_mail completed")

        # test_post_admin_mail(self):
        test_print("test_post_admin_mail starting")
        data={
            'key': 'SG.Dummy_Token',
            'fromEmail' : 'synbiohub@synbiohub.utah.edu',
        }
        compare_post_request("/admin/mail", data, headers = {"Accept": "text/plain"}, test_name = "admin_mail")
        test_print("test_post_admin_mail completed")

        # test_admin_savePlugin(self):
        test_print("test_admin_savePlugin starting")
        data={
            'id': 'New',
            'category' : 'download',
            'name' : 'test_plugin',
            'url' : 'jimmy',
        }
        compare_post_request("/admin/savePlugin", data, headers = {"Accept": "text/plain"}, test_name = "admin_savePlugin")
        test_print("test_admin_savePlugintatus completed")

        # test_admin_plugins(self):
        test_print("test_admin_plugins starting")
        compare_get_request("/admin/plugins")
        test_print("test_admin_plgins completed")

        # test_admin_saveRegistry(self):
        test_print("test_admin_saveRegistrytatus starting")
        data={
            'uri': 'testurl.com',
            'url' : 'testurl.com',
        }
        compare_post_request("/admin/saveRegistry", data, headers = {"Accept": "text/plain"}, test_name = "admin_saveRegistry")
        test_print("test_admin_saveRegistry completed")

        # test_admin_registries(self):
        test_print("test_admin_registries starting")
        compare_get_request("admin/registries")
        test_print("test_admin_registries completed")

        # TODO: FIGURE OUT ANOTHER WAY TO TEST THIS
        #    def test_admin_retreiveFromWebOfRegistries(self):
        #        data={
        #        }
        #        compare_post_request("/admin/retrieveFromWebOfRegistries", data, headers = {"Accept": "text/plain"}, test_name = "admin_retrieveFromWebOfRegistries")

        #    def test_admin_federate(self):
        #        data={
        #            'administratorEmail': 'myers@ece.utah.edu',
        #            'webOfRegistries' : 'https://wor.synbiohub.org',
        #        }
        #        compare_post_request("/admin/federate", data, headers = {"Accept": "text/plain"}, test_name = "admin_federate")

        test_print("test_admin_setAdministratorEmail starting")
        data={
            'administratorEmail': 'test@synbiohub.org',
        }
        compare_post_request("/admin/setAdministratorEmail", data, headers = {"Accept": "text/plain"}, test_name = "admin_setAdministratorEmail")
        test_print("test_admin_setAdministratorEmail completed")

        test_print("test_admin_updateTheme starting")
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
        test_print("test_admin_updateTheme completed")

        test_print("test_admin_status starting")
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
        test_print("test_admin_status completed")

        # TODO: FIGURE OUT ANOTHER WAY TO TEST THIS
        # test_admin_explorer(self):
        # test_print("test_admin_explorer starting")
        # compare_get_request("/admin/explorer")
        # test_print("test_admin_explorer completed")

        test_print("test_explorerUpdateIndex starting")
        data={
        }
        compare_post_request("/admin/explorerUpdateIndex", data, headers = {"Accept": "text/plain"}, test_name = "admin_explorerUpdateIndex")
        test_print("test_explorerUpdateIndex completed")

        test_print("test_saveRemoteICE starting")
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
        test_print("test_saveRemoteICE completed")

        test_print("test_saveRemoteBenchling starting")
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
        test_print("test_saveRemoteBenchling completed")

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

        # test_updateUser(self):
        test_print("test_updateUser starting")
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
        test_print("test_updateUser completed")

        test_print("test_admin_newUser GET starting")
        compare_get_request("/admin/newUser", test_name = "test_new_user0")
        test_print("test_admin_newUser GET completed")


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
        test_print("test_newUser POST starting")
        data = {
            'username': 'adminNewUser',
            'name' : 'adminNewUser',
            'email' : 'adminNewUser@user.synbiohub',
            'affiliation' : 'adminNewUser',
            'isMember' : '1',
            'isCurator' : '1',
            'isAdmin' : '1',
        }
        compare_post_request("/admin/newUser", data, headers = {"Accept": "text/plain"}, test_name = "admin_newUser1")
        test_print("test_newUser POST completed")


