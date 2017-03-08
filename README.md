SynBioHub is a Web-based repository for synthetic biology, enabling users to browse, upload, and share synthetic biology designs.

To learn more about the SynBioHub, including installation instructions and documentation, visit [the SynBioHub wiki](http://wiki.synbiohub.org).
 
To access a sample instance of SynBioHub containing enriched _Bacillus subtilis_ data, features from the _Escherichia coli_ genome, and the complete [iGEM Registry of Standard Biological Parts](http://parts.igem.org/Main_Page), visit [synbiohub.org](http://synbiohub.org).


Installation
------------

The recommended way to install SynBioHub is via the Docker image.  See [Installation](http://wiki.synbiohub.org/wiki/Installation) for more information.


Manual Installation
-------------------

SynBioHub has both JavaScript (node.js) and Java components.

Prequisites:

* Linux (only tested with Ubuntu 16.04) or macOS
* A JDK
* [Apache Maven](https://maven.apache.org/)
* [node.js](https://nodejs.org/en/) >= 6.10
* [OpenLink Virtuoso](https://github.com/openlink/virtuoso-opensource) 7.x.x
* [rapper](http://librdf.org/raptor/rapper.html) (apt install `raptor2-utils`)

For Ubuntu 16.04:

    echo 'deb http://packages.comsode.eu/debian jessie main' >> /etc/apt/sources.list
    wget http://packages.comsode.eu/key/odn.gpg.key
    apt-key add odn.gpg.key
    curl -sL https://deb.nodesource.com/setup_6.x | sudo -E bash -
    apt update
    apt install default-jdk maven raptor2-utils nodejs virtuoso-opensource

First, build the Java parts with Maven:

    cd java
    mvn compile

Then install the SynBioHub dependencies with npm:

    npm install

Then run synbiohub:

    node synbiohub.js







 
