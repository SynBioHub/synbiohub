# this file specifies the tests to be run.
# import requests
# from test_arguments import test_print
# from test_functions import compare_get_request, compare_post_request


def test_root():

    # no commented tests
    from test_tests import TestTests
    testTests = TestTests()
    testTests.test_get_address()

    # no commented tests
    from first_time_setup import TestSetup
    firstTimeSetup = TestSetup()
    firstTimeSetup.test_get()
    firstTimeSetup.test_post()

    # no commented tests
    from first_time_setup import TestSetup
    from test_before_login import TestBeforeLogin
    testBeforeLogin = TestBeforeLogin()
    testBeforeLogin.test_before_login()

    # no commented tests
    from test_user import TestUser
    testUser = TestUser()
    testUser.test_post_register()

    # no commented tests
    from test_submit import TestSubmit
    testSubmit = TestSubmit()
    testSubmit.test_submit()

    # no commented tests
    from test_search import TestSearch
    testSearch = TestSearch()
    testSearch.test_search()

    # TODO: TEST TEST_DOWNLOAD
    from test_download import TestDownload
    testDownload = TestDownload()
    testDownload.test_download()

    # TODO: add field edit field are commented out
    from test_edit import TestEdit
    testEdit = TestEdit()
    testEdit.test_edit()

    # TODO: both public and private attachURL seem nondeterministic
    from test_attachment import TestAttachment
    testAttachment = TestAttachment()
    testAttachment.test_attachment()

    # no commented tests
    from test_collection import TestCollections
    testCollections = TestCollections()
    testCollections.test_collections()

    # TODO: test new user commented out
    # ALL ASIDE FROM TEST_NEWUSER SHOULD WAIT UNTIL LATER
    from test_admin import TestAdmin
    testAdmin = TestAdmin()
    testAdmin.test_admin1()

    # TODO: TEST TEST_TWINS
    from test_twins import TestTwins

    from test_submit_other_user import TestSubmitOtherUser
    testSubmitOtherUser = TestSubmitOtherUser()
    testSubmitOtherUser.test_submit_other_user()

    from test_hash import TestHash
    testHash = TestHash()
    testHash.test_hash()

    from docker_test_setup import TestDockerSetup
    testDockerSetup = TestDockerSetup()
    testDockerSetup.test_get_register()
    testDockerSetup.test_create_user_and_upload()

    from docker_test import TestDocker
    testDocker = TestDocker()
    testDocker.test_dockeruser_persist()
    testDocker.test_public_persist()
    testDocker.test_admin_persist()
