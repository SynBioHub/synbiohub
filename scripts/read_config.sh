#!/usr/bin/env bash

SBH_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/..

jq -s '.[0] * .[1]' $SBH_ROOT/config.json $SBH_ROOT/config.local.json | jq $@

