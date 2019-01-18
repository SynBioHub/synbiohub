import argparse, sys

def test_print(string):
    sys.stdout.write("[synbiohub test] ")
    sys.stdout.write(string)
    sys.stdout.write("\n")


parser = argparse.ArgumentParser()

parser.add_argument("--resetalltests", help="reset all tests for requests by saving responses for future comparisons. Should only be run if working on the test suite implementation itself and all tests have passed.",
                    action = "store_true")

parser.add_argument("--serveraddress",
                    help="specify the synbiohub server to test.",
                    default = "http://localhost:7777/")

parser.add_argument("--resetgetrequests",
                    help = "reset a get request test by saving the result of the request for future comparison. Use this option after verifying that a request works correctly.",
                    default = [],
                    nargs = '*')

parser.add_argument("--resetpostrequests",
                    help = "reset a post request test by saving the result of the request for future comparison. Use this option after verifying that a request works correctly.",
                    default = [],
                    nargs = '*')



args = parser.parse_args()

# format it with a slash if the slash is missing
if args.serveraddress[-1] != '/':
    args.serveraddress = args.serveraddress + '/'


if args.resetalltests:
    test_print("resetting all tests by saving every request for future comparisons. You should not have run this unless you are working on the test suite implementation itself and have verified that all tests passed before the reset.")
