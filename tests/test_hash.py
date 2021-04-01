import requests
from unittest import TestCase
from test_arguments import test_print
from test_functions import compare_get_request, compare_post_request

class TestHash(TestCase):

    def test_hash(self):

        test_print("test_attach_collection_hash starting")
        data={
                'url': 'synbiohub.org',
                'name' : 'synbiohubtestattachurl',
                'type' : 'test',
        }
        files = {'file':("./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml", open('./SBOLTestRunner/src/main/resources/SBOLTestSuite/SBOL2/BBa_I0462.xml', 'rb'))}
        compare_post_request("user/:userId/:collectionId/:displayId/:version/:hash/share/attach",route_parameters = ["testuser", "test_hash", "test_hash_collection","2", "599c07a2540e8b1e9429c35ff76b61bd4eaa9a9a"],data=data,  headers = {"Accept": "text/plain"}, files = files, test_name = "test_attach_to_collection_hash")
        test_print("test_attach_collection_hash completed")

#        test_print("test_attachUrl_hash starting")
#        data={
#                'url': 'synbiohub.org',
#                'name' : 'synbiohubtestattachurl',
#                'type' : 'test',
#        }
#        compare_post_request("user/:userId/:collectionId/:displayId/:version/:hash/share/attachUrl", route_parameters = ["testuser", "test_hash", "test_hash_collection","2", "599c07a2540e8b1e9429c35ff76b61bd4eaa9a9a"], data = data, test_name = "test_attachURL_hash")
#        test_print("test_attachUrl_hash completed")


#[synbiohub test] Warning- post endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/makePublic was not tested.

#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/subCollections was not tested.
#[synbiohub test] Warning- post endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/removeMembership was not tested.
#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/remove was not tested.
#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/replace was not tested.
#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/makePublic was not tested.
#[synbiohub test] Warning- post endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/addOwner was not tested.
#[synbiohub test] Warning- post endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/removeOwner/:username was not tested.
#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/addOwner was not tested.

#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/sbol was not tested. lib
#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/download was not tested. use txt file
#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/metadata was not tested. use diff
#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/sbolnr was not tested. lib
#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/omex was not tested. zip file, leave to last
#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/summary was not tested. probably not deterministic
#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/fasta was not tested. use diff 
#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/gb was not tested. libsbolj 
#[synbiohub test] Warning- get endpoint user/:userId/:collectionId/:displayId/:version/:hash/share/gff was not tested. libsbolj
