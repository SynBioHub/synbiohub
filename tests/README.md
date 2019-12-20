
# Testing SynBioHub

## Running the test suite

First, install dependencies. The dependencies are python 3, the python packages in tests/test_requirements.txt, and jq.

Ubuntu:\
`sudo apt-get install jq`\
`sudo apt-get install -y python3 python3-pip`\
`sudo pip3 install -r tests/test_requirements.txt`\

Then build a docker image from the local version of synbiohub using
`docker build -t synbiohub/synbiohub:snapshot-standalone -f docker/Dockerfile .`

Finally, run the test suite using
`bash tests/test.sh`

## Changing tests to reflect changes to SynBioHub

When changing the output of any SynBioHub endpont, tests will fail and reflect the new changes. This is to prevent unintended changes to SynBioHub.

If a change is intended, use the command line options --resetgetrequests and --resetpostrequests. For example, if I were to reset the saved result from the setup page of SynBioHub, I would run
`bash tests/test.sh --resetgetrequests setup`

For a list of options, perform `bash tests/test.sh --help`

## Writing new tests

Tests are written as unittest test cases. Use the compare_get_request and compare_post_request functions previded by test_functions to test endpoints.

The test suite requires that each endpoint in lib/app.js is tested at least once. The tests perform the request and save the result to compare against future requests.

## Modules
If adding a new module with test cases in it, the module must be imported and added to the test suite in test_suite.py using the addTestModule function.

Test functions within a module should be independent of each other, but tests in new modules can depend on the tests run in previous modules. For example, all modules depend on the first_time_setup module to have run first, to set up the new synbiohub instance.


## Saving results in order to make tests pass
In order to save the new test results, use the --resetgetrequests and --resetpostrequests options.

If making changes to the test suite implementation, use the --resetalltests option to completely refresh all saved results.


## Ignoring elements

If you are making a change that should be ignored by the test suite for a very good reason, then use the class testignore. Any html elements and their child elements are ignored by the testing procedure. This is currently used to ignore the version number of synbiohub that apprears on some pages.

## Arguments
  -h, --help            show this help message and exit
  --resetalltests       reset all tests for requests by saving responses for
                        future comparisons. Should only be run if working on
                        the test suite implementation itself and all tests
                        have passed.

  --serveraddress SERVERADDRESS
                        specify the synbiohub server to test.

  --resetgetrequests [RESETGETREQUESTS [RESETGETREQUESTS ...]]
                        reset a get request test by saving the result of the
                        request for future comparison. Use this option after
                        verifying that a request works correctly.

  --resetpostrequests [RESETPOSTREQUESTS [RESETPOSTREQUESTS ...]]
                        reset a post request test by saving the result of the
                        request for future comparison. Use this option after
                        verifying that a request works correctly.

  --stopaftertestsuite  stop after the test suite has run in order to keep the
                        test server running. This can be used to view the
                        state of the synbiohub instance after the tests have
                        run but before sboltestrunner has been run.

  --stopafterstart      do not run the test suite, just start up a new test
                        synbiohub instance.



