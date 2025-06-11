import requests
from unittest import TestCase
from test_arguments import test_print
from test_functions import compare_get_request, compare_post_request, login_with

class TestSubmitOtherUser(TestCase):

    def test_submit_other_user(self):

        test_print("logging out of test_user")
        compare_get_request("/logout", test_name = "logout3")

        test_print("creating new user for test_hash")

        data={
                'username': 'testuser3',
                'name' : 'ronald',
                'affiliation' : 'synbiohubtester',
                'email' : 'test3@user.synbiohub',
                'password1' : 'test1',
                'password2' : 'test1'
                }
        compare_post_request("register", data, headers = {"Accept": "text/plain"}, test_name = "register2")

        logininfo = {'email' : 'test3@user.synbiohub',
                     'password' : 'test1'}
        login_with(logininfo)

#        test_print("creating new collection for test_hash")

#        data = {'id':(None, 'testid2'),
#                'version' : (None, '2'),
#                'name' : (None, 'testcollection2'),
#                'description':(None, 'testdescription'),
#                'citations':(None, ''),
#                'overwrite_merge':(None, '0')
#                }
#
#        files = {'file':("./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/Measure.xml", open('./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/Measure.xml', 'rb'))}

#        compare_post_request("submit", data, headers = {"Accept":"text/plain"}, files = files, test_name = "collection_for_test_hash_other_user")

#        test_print("completed")

        compare_get_request("/logout", test_name = "logout2")

        logininfo = {'email' : 'test@user.synbiohub',
                     'password' : 'test'}
        login_with(logininfo)


