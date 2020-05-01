# GitHub Actions
## Test
This is the workflow described in the file `test.yml`.
It runs the SBH test suite, the SynBioHub test suite, and then if both succeed the image is pushed to Docker Hub.

## Release
This is the workflow described in the file `release.yml`.
It is composed of two jobs: `retag-release` and `update-compose`.

### `retag-release`
This tag publishes a Docker image for the release
It pulls the source code for SynBioHub at the tagged release commit and uses that to build a docker image.
The resulting image is then pushed to Docker Hub with the tag `$RELEASE_VERSION-standalone`.

### `update-compose`
This tag updates the [SynBioHub Docker Compose Configuration](https://github.com/synbiohub/synbiohub-docker) to point to the latest image. 
First, it pulls the snapshot compose configuration, which points to the `snapshot-standalone` image.
Then, it updates the master branch by replacing `snapshot` with the version number.

