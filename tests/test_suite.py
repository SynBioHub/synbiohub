from sys import argv
from test_functions import exit_tests, set_command_line_arguments

set_command_line_arguments(argv[1:])

# run the first time setup script
import firsttimesetup

exit_tests()
