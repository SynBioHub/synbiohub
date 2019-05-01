import requests
from unittest import TestCase
from test_functions import compare_get_request, compare_post_request, login_with


class TestDocker(TestCase):

    def test_dockeruser_persist(self):

        # login as the user
        logininfo = {'email' : 'dockertestuser',
                     'password' : 'test'}
        login_with(logininfo)

        # test the project view
        compare_get_request("manage", test_name="docker")
        compare_get_request("/user/:userId/:collectionId/:displayId",
                            route_parameters = ["dockertestuser","testid_sequence1","testid_sequence1_collection"], test_name="docker")


    def test_public_persist(self):
        # login as the user
        logininfo = {'email' : 'dockertestuser',
                     'password' : 'test'}
        login_with(logininfo)
        
        # test public ones while signed in as user
        # also wait 2 seconds for things to load
        compare_get_request("public/:collectionId/:displayId",
                            route_parameters = ["testid", "testid_collection"],
                            test_name = "docker_public", re_render_time = 2000)
        
        compare_get_request("public/:collectionId/:displayId(*)/:version/full",
                            route_parameters = ["testid", "testid_collection", "1"])

        # test the component page
        compare_get_request("public/:collectionId/:displayId",
                            route_parameters = ["testid", "BBa_I0462"], test_name="bbapublic")

    def test_admin_persist(self):
        logininfo = {'email' : 'test@user.synbiohub',
                     'password' : 'test'}
        login_with(logininfo)
        compare_get_request("manage", test_name="docker_admin")
        

