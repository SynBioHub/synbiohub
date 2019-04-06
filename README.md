<img src="https://synbiohub.org/logo_uploaded.svg" width="100%" />

![]("https://david-dm.org/synbiohub/synbiohub.svg")
![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)


SynBioHub is a Web-based repository for synthetic biology, enabling users to browse, upload, and share synthetic biology designs.

To learn more about the SynBioHub, including installation instructions and documentation, visit [the SynBioHub wiki](http://wiki.synbiohub.org).
 
To access a sample instance of SynBioHub containing enriched _Bacillus subtilis_ data, features from the _Escherichia coli_ genome, and the complete [iGEM Registry of Standard Biological Parts](http://parts.igem.org/Main_Page), visit [synbiohub.org](http://synbiohub.org). To access a bleeding-edge version of SynBioHub, visit [dev.synbiohub.org](https://dev.synbiohub.org).


## Installation

The recommended way to install SynBioHub is via the Docker image.  See [Installation](http://wiki.synbiohub.org/wiki/Installation) for more information.


## Manual Installation

SynBioHub has both JavaScript (node.js) and Java components.

Prequisites:

* Linux (only tested with Ubuntu 18.04.01) or macOS
* A JDK
* [Apache Maven](https://maven.apache.org/)
* [node.js](https://nodejs.org/en/) >= 6.10
* [OpenLink Virtuoso](https://github.com/openlink/virtuoso-opensource) 7.x.x
* [rapper](http://librdf.org/raptor/rapper.html) (apt install `raptor2-utils`)
* [jq](https://stedolan.github.io/jq/) (apt install `jq`)

### Ubuntu 18.04.01
 1. Install Virtuoso from source at
    <https://github.com/openlink/virtuoso-opensource>
 * Follow the README on installing virtuoso from source. This involves installing all the dependencies and running build commands.
 * Currently, Virtuoso does not support versions of OpenSSL 1.1.0 and above, or versions of OpenSSL below 1.0.0. When installing the dependency, build from a binary between those versions from <https://www.openssl.org/source/>.
 2. Set up the Node.js repository 
    1. Download the Node setup script `curl -sL https://deb.nodesource.com/setup_6.x | sudo -E bash -`
    2. Update your package repositories `apt update`
 3. Install the necessary packages `apt install default-jdk maven raptor2-utils nodejs jq build-essential python`
 4. Clone the SynBioHub repository `git clone https://github.com/SynBioHub/synbiohub`
 5. Change to the SynBioHub directory `cd synbiohub`
 6. Build the Java components with Maven `cd java && mvn package`
 7. Return to the root directory and install the Node dependencies with yarn `cd ../ && yarn install`
    Make sure that yarn is being used, not 'cmdtest'.
 8. Install nodemon and forever with `npm install nodemon -g && npm install forever -g`
 9. Start virtuoso process `virtuoso-t +configfile /usr/local/virtuoso-opensource/var/lib/virtuoso/db/virtuoso.ini -f`
 10. Add SPARQL update rights to the dba user in virtuoso.
 * Visit localhost:8890, click conductor on the left hand side, and login with user name dba and password dba.
 * Visit system admin -> user accounts in the menu at the top.
 * Find the accound labled dba and edit.<br/>Add SPARQL_UPDATE to roles using the menu at the bottom.
 * If no dba account exists, add one, then add update rights.
 11. Start the SynBioHub process `npm start` or `npm run-script dev`

