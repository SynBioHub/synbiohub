SynBioHub is a Web-based repository for synthetic biology, enabling users to browse, upload, and share synthetic biology designs.

To learn more about the SynBioHub, including installation instructions and documentation, visit [the SynBioHub wiki](http://wiki.synbiohub.org).
 
To access a sample instance of SynBioHub containing enriched _Bacillus subtilis_ data, features from the _Escherichia coli_ genome, and the complete [iGEM Registry of Standard Biological Parts](http://parts.igem.org/Main_Page), visit [synbiohub.org](http://synbiohub.org).


Building
--------

SynBioHub has both JavaScript (node.js) and Java components.

Prequisites:

* A JDK
* [Apache Maven](https://maven.apache.org/)
* [node.js](https://nodejs.org/en/) >= 6.10
* [rapper](http://librdf.org/raptor/rapper.html) (apt install `raptor2-utils`)

First, build the Java parts with Maven:

    cd java
    mvn compile

Then install the SynBioHub dependencies with npm:

    npm install

Then run synbiohub:

    node synbiohub.js







 
