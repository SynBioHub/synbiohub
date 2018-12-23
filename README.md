<img src="https://synbiohub.org/logo_uploaded.svg" width="100%" />

![](https://david-dm.org/synbiohub/synbiohub.svg) 
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

### Ubuntu 18.04.01:
 1. Install Virtuoso from source at
    <https://github.com/openlink/virtuoso-opensource>
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
 10. Start the SynBioHub process `npm start` or `npm run-script dev`

