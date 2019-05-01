import subprocess, shutil, time, os

def run_bash(command):
    process = subprocess.Popen(command.split(), stdout=subprocess.PIPE)
    output, error = process.communicate()


# if length is negative, returns all lines
def file_tail(filename, length):
    with open(filename, "r") as file_to_read:
        lines = file_to_read.readlines()
        if length < len(lines) and length >= 0:
            return "".join(lines[len(lines)-length:])
        else:
            return "".join(lines)



# if num_of_lines is negative, returns all lines
def get_end_of_error_log(num_of_lines):
    copy_docker_log()
    directory = os.listdir("./logs_from_test_suite")
    for filename in directory:
        if filename[len(filename)-5:] == "error":
            return file_tail("./logs_from_test_suite/" + filename, num_of_lines)

    raise Exception("Could not find error log")


def copy_docker_log():
    if os.path.isdir("./logs_from_test_suite"):
        shutil.rmtree("./logs_from_test_suite")
    
    run_bash("docker cp testsuiteproject_synbiohub_1:/synbiohub/logs .")
    run_bash("mv ./logs ./logs_from_test_suite")
    


