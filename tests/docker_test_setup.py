import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request, login_with


class TestDockerSetup(TestCase):


    def test_get_register(self):
        compare_get_request('/register')
    
    def test_create_user_and_upload(self):
        # first, register a new user
        data = {'name' : 'dockertestuser',
                'email' : 'dockertest@user.synbiohub',
                'username' : 'dockertestuser',
                'password1' : 'test',
                'password2' : 'test'}

        compare_post_request('/register', data)

        # login as the user
        logininfo = {'email' : 'dockertestuser',
                     'password' : 'test'}
        login_with(logininfo)

        # now create a couple collections
        self.create_collection("memberAnnotations", "memberAnnotations.xml")
        self.create_collection("multipleCollections_no_Members", "multipleCollections_no_Members.xml")
        self.create_collection("sequence1", "sequence1.xml")
        

        # add an extra file to one collection
        

    def create_collection(self, collectionname, sbol2filename):
        data = {'id':(None, 'testid_'+collectionname),
                'version' : (None, '1'),
                'name' : (None, collectionname),
                'description':(None, 'testdescription'),
                'citations':(None, ''),
                'overwrite_merge':(None, '0')}

        files = {'file':("./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/" + sbol2filename,
                                              open('./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/' + sbol2filename, 'rb'))}
        
        compare_post_request("submit", data, headers = {"Accept": "text/plain"},
                             files = files,
                             test_name = "create_collection_"+collectionname)

        # delete collection
        #compare_get_request("/user/testuser/testid2/testid_collection2/1/removeCollection")



