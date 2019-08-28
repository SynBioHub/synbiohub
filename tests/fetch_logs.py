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


def log_to_year_month_day(log):
    list1 = log.split("-")
    list1[-1] = list1[-1].split(".")[0]
    return (int(list1[1]), int(list1[2]), int(list1[3]))


# returns true if log1 has a newer date than log2
def newer_logp(log1, log2):
    date1 = log_to_year_month_day(log1)
    date2 = log_to_year_month_day(log2)
    for i in range(len(date1)):
        if date1[i] > date2[i]:
            return True
        elif date1[i] < date2[i]:
            return False
    # they must have been the same date to reach this point
    return False


# if num_of_lines is negative, returns all lines
def get_end_of_error_log(num_of_lines):
    copy_docker_log()
    directory = os.listdir("./logs_from_test_suite")
    possible_error_logs = []
    for filename in directory:
        if filename[len(filename)-5:] == "error":
            possible_error_logs.append(filename)

    newest_error_log = possible_error_logs[0]
    for potential_log in possible_error_logs:
        if newer_logp(potential_log, newest_error_log):
            newest_error_log = potential_log
    
    return file_tail("./logs_from_test_suite/" + newest_error_log, num_of_lines)

    raise Exception("Could not find error log")


def copy_docker_log():
    if os.path.isdir("./logs_from_test_suite"):
        shutil.rmtree("./logs_from_test_suite")
            
    run_bash("docker cp testsuiteproject_synbiohub_1:/mnt/data/logs ./logs")
    run_bash("mv ./logs ./logs_from_test_suite")
    


