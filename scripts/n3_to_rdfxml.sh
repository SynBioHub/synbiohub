#!/usr/bin/env bash

#
# Combined with the accompanying sorted-n3-to-rdfxml.js, this script
# can convert n3 triples (in no particular order) into well-formed
# RDF/XML, where the prefixes are defined at the top of the file and
# there is a single rdf:Description for each subject.
#
# We first sort the n3 triples using sort(1).  Because the subject is
# first in n3, this results in all of the triples for each subject
# being grouped together, and it's also very fast.  Then we pass the
# sorted n3 to a script that does two passes: one to work out all the
# required prefixes, and one to actually print the RDF+XML (which can
# now use consistent prefixes throughout, and can have xmlns
# declarations in the opening rdf tag.
#
# stdin: n3 triples in no particular order
# stdout: filename of a temporary file containing "clean" rdf+xml
#

N3_TEMP=$(mktemp)
XML_TEMP=$(mktemp)

sort - > $N3_TEMP && \
$NODE sorted-n3-to-rdfxml $N3_TEMP > $XML_TEMP

(>&2 echo "Sorted N3 temp file: $N3_TEMP")
(>&2 echo "XML temp file: $XML_TEMP")

rm -f $N3_TEMP

printf $XML_TEMP

