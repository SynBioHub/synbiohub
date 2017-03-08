#!/usr/bin/env bash

rapper -o ntriples $@ > ntriples.n3 && split -C 5m ntriples.n3 upload_


