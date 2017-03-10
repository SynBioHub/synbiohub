#!/usr/bin/env bash

#
# This script splits large rdf+xml files up into multiple n3 triple files.
#
# Args: the filename of an rdf+xml file to split
# Output: a set of n3 triple files named upload_* 
# 

rapper -o ntriples $@ > ntriples.n3 && split -l 5000 ntriples.n3 upload_


