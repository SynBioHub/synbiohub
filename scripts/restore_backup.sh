#!/usr/bin/env bash

PREFIX=$1

OUTPUT_PREFIX="[restore_backup.sh]"

SCRIPTS="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

VIRTUOSO_INI=$($SCRIPTS/read_config.sh -r .triplestore.virtuosoINI)
VIRTUOSO_DB=$($SCRIPTS/read_config.sh -r .triplestore.virtuosoDB)

echo $OUTPUT_PREFIX Using INI: $VIRTUOSO_INI
echo $OUTPUT_PREFIX Using DB path: $VIRTUOSO_DB

echo $OUTPUT_PREFIX Stopping Virtuoso...
sudo -n service virtuoso-opensource-7 stop

sleep 3

if [[ $? -ne 0 ]]; then
    echo $OUTPUT_PREFIX Cannot stop virtuoso as user $(whoami).  Is sudoers configured correctly?
    exit 1
fi

echo $OUTPUT_PREFIX Removing database file...

sudo -n rm -f $VIRTUOSO_DB/virtuoso.db

if [[ $? -ne 0 ]]; then
    echo $OUTPUT_PREFIX Cannot remove virtuoso.db as user $(whoami).  Is sudoers configured correctly?
    exit 1
fi

echo $OUTPUT_PREFIX Restoring backup with virtuoso-t...

pushd $VIRTUOSO_DB

sudo -n virtuoso-t +foreground +configfile $VIRTUOSO_INI +restore-backup $PREFIX

if [[ $? -ne 0 ]]; then
    echo $OUTPUT_PREFIX Cannot execute virtuoso-t as user $(whoami).  Is sudoers configured correctly?
    exit 1
fi

popd

echo $OUTPUT_PREFIX Starting Virtuoso...

sudo -n service virtuoso-opensource-7 start

if [[ $? -ne 0 ]]; then
    echo $OUTPUT_PREFIX Cannot start virtuoso as user $(whoami).  Is sudoers configured correctly?
    exit 1
fi



