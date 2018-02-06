#!/usr/bin/env bash

service virtuoso-opensource-7 start &&
echo "USER_GRANT_ROLE('SPARQL', 'SPARQL_UPDATE');" | isql-vt &&
su synbiohub -c "cd /opt/synbiohub && npm start"



