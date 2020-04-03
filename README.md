<img src="https://synbiohub.org/logo_uploaded.svg" width="100%" />

![]("https://david-dm.org/synbiohub/synbiohub.svg")
![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)


SynBioHub is a Web-based repository for synthetic biology, enabling users to browse, upload, and share synthetic biology designs.

To learn more about the SynBioHub, including installation instructions and documentation, visit [the SynBioHub wiki](https://synbiohub.github.io/api-docs/).
 
To access a sample instance of SynBioHub containing enriched _Bacillus subtilis_ data, features from the _Escherichia coli_ genome, and the complete [iGEM Registry of Standard Biological Parts](http://parts.igem.org/Main_Page), visit [synbiohub.org](http://synbiohub.org). To access a bleeding-edge version of SynBioHub, visit [dev.synbiohub.org](https://dev.synbiohub.org).


## Installation

The recommended way to install SynBioHub is via the Docker image.  See [Installation](http://wiki.synbiohub.org/wiki/Installation) for more information.


## Manual Installation

SynBioHub has both JavaScript (node.js) and Java components.

#### Prequisites:

Linux (only tested with Ubuntu 18.04.01) or macOS
   * If you're using macOS, first install [homebrew](https://brew.sh/)

A JDK
| OS | Command |
| --- | --- |
| Ubuntu | apt install `default-jdk` |
| Mac | brew install `openjdk` |

[Apache Maven](https://maven.apache.org/)

| OS | Command |
| --- | --- |
| Ubuntu | apt install `maven` |
| Mac | brew install `maven` |

[node.js](https://nodejs.org/en/) >= 11.0.0

| OS | Command/Link |
| --- | --- |
| Ubuntu | visit https://nodejs.org/en/ |
| Mac | brew install `node` |

[OpenLink Virtuoso](https://github.com/openlink/virtuoso-opensource) 7.x.x

| OS | Command/Link |
| --- | --- |
| Ubuntu | visit https://github.com/openlink/virtuoso-opensource |
| Mac | brew install `virtuoso` |

[rapper](http://librdf.org/raptor/rapper.html)

| OS | Command |
| --- | --- |
| Ubuntu | apt install `raptor2-utils` |
| Mac | brew install `raptor` |

[jq](https://stedolan.github.io/jq/)

| OS | Command |
| --- | --- |
| Ubuntu | apt install `jq` |
| Mac | brew install `jq` |


### Ubuntu 18.04.01
 1. Install Virtuoso 7 from source at
    <https://github.com/openlink/virtuoso-opensource>
 * Switch to the branch stable/7 before installing.
 * Follow the README on installing virtuoso from source. This involves installing all the dependencies and running build commands.
 * Currently, Virtuoso does not support versions of OpenSSL 1.1.0 and above, or versions of OpenSSL below 1.0.0. When installing the dependency, build from a binary between those versions from <https://www.openssl.org/source/>.
 2. Set up the Node.js repository 
    1. Download the Node setup script `curl -sL https://deb.nodesource.com/setup_6.x | sudo -E bash -`
    2. Update your package repositories `apt update`
 3. Install the necessary packages `apt install default-jdk maven raptor2-utils nodejs jq build-essential python`
 4. Start virtuoso process `virtuoso-t +configfile /usr/local/virtuoso-opensource/var/lib/virtuoso/db/virtuoso.ini -f`

### MacOS
 1. Install the necessary packages `brew install openjdk maven node virtuoso raptor jq python`
 2. Start virtuoso process 
    1. `cd /usr/local/Cellar/virtuoso/7.2.5.1_1/var/lib/virtuoso/db`
         * The command above is based on where the virtuoso.ini file is located. Your installation might be located
           somewhere different than `/usr/local/Cellar/virtuoso/7.2.5.1_1/var/lib/virtuoso/db`, or the version might be
           different (`7.2.5.1_1` might be `7.3.6.1_1` or any other version number).
         * If you're having trouble finding the location of the virtuoso.ini file, run `sudo find / -name virtuoso.ini`.
           Press the control and c keys simultaneously to quit the search.
    2. `virtuoso-t -f`

### Both Systems
 1. Clone the SynBioHub repository `git clone https://github.com/SynBioHub/synbiohub`
 2. Change to the SynBioHub directory `cd synbiohub`
 3. Build the Java components with Maven `cd java && mvn package`
 4. Return to the root directory and install the Node dependencies with yarn `cd ../ && yarn install`
    Make sure that yarn is being used, not 'cmdtest'.
 5. Install nodemon and forever with `npm install nodemon -g && npm install forever -g`
 6. Add SPARQL update rights to the dba user in virtuoso.
 * Visit localhost:8890, click conductor on the left hand side, and login with user name dba and password dba.
 * Visit system admin -> user accounts in the menu at the top.
 * Find the accound labled dba and edit.<br/>Add SPARQL_UPDATE to roles using the menu at the bottom.
 * If no dba account exists, add one, then add update rights.
 7. Start the SynBioHub process `npm start` or `npm run-script dev`


# Publishing
The repository is set up to prohibit commits directly to the master branch.
Commits must be made in another branch, and then a GitHub PR used to merge them into master.
GitHub PRs must be approved by at least one other developer before they can be merged into master.
Additionally, they must pass Travis checks, which build a Docker image and run the [SBOLTestSuite](https://github.com/synbiodex/sboltestsuite) and SynBioHub integration tests against it.
Each time a PR is merged into master, the Travis checks are re-run on the master branch, and if they succeed the resulting image is pushed by Travis to DockerHub under the tag `snapshot-standalone`.

## Publishing a release
This automation is currently *under construction*, so the process described below may not be completely implemented.
Releases are published automatically using GitHub Actions. 
There is an action which fires on release publication.
It publishes an image to Docker Hub under the $VERSION-standalone tag, and updates the [synbiohub-docker](https://github.com/synbiohub/synbiohub-docker) master branch to point to this version.
More information available [here](https://github.com/SynBioHub/synbiohub/blob/master/.github/workflows/README.md).
