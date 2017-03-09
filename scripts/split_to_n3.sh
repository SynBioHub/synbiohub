#!/usr/bin/env bash

rapper -o ntriples $@ > ntriples.n3 && split -l 5000 ntriples.n3 upload_


