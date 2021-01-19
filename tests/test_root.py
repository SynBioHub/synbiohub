# this file specifies the tests to be run.
import requests
from unittest import TestCase
from test_arguments import test_print
from test_functions import compare_get_request, compare_post_request

class TestRoot(TestCase):

        def test_root(self):

            from test_tests import TestTests
            testTests=TestTests()
            testTests.test_get_address()

            from first_time_setup import TestSetup
            firstTimeSetup = TestSetup()
            firstTimeSetup.test_get()
            firstTimeSetup.test_post()

            from test_before_login import TestBeforeLogin
            testBeforeLogin = TestBeforeLogin()
            testBeforeLogin.test_before_login()

            from test_user import TestUser
            testUser = TestUser()
            testUser.test_post_register()

            from test_user2 import TestUser2
            testUser2 = TestUser2()
            testUser2.test_post_login_token()

            from test_submit import TestSubmit
            testSubmit = TestSubmit()
            testSubmit.test_submit()

            from test_search import TestSearch
            testSearch = TestSearch()
            testSearch.test_search()

            from test_download import TestDownload

            from test_edit import TestEdit
            testEdit = TestEdit()
            testEdit.test_edit()

            from test_attachment import TestAttachment
            testAttachment = TestAttachment()
            testAttachment.test_attachment()

            from test_collection import TestCollections
            testCollections = TestCollections()
            testCollections.test_collections()

            from test_admin import TestAdmin
            testAdmin = TestAdmin()
            testAdmin.test_admin1()

            from test_admin2 import TestAdmin2
            testAdmin2 = TestAdmin2()
            testAdmin2.test_admin2()

            from test_twins import TestTwins

            from  docker_test_setup import TestDockerSetup
            testDockerSetup = TestDockerSetup()
            testDockerSetup.test_get_register()
            testDockerSetup.test_create_user_and_upload()

            from docker_test import TestDocker
            testDocker = TestDocker()
            testDocker.test_dockeruser_persist()
            testDocker.test_public_persist()
            testDocker.test_admin_persist()
