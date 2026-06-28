FROM node:11.1.0-alpine

RUN apk add nss git openjdk8-jre openjdk8 maven python alpine-sdk libxml2-dev yarn g++ gcc bash raptor2 jq

WORKDIR /synbiohub

RUN git config --global url."https://".insteadOf git://

# Install Node dependencies from the manifest + lockfile alone, in their own
# cached layer, so application source changes don't re-run yarn install.
# node_modules is excluded from the build context (.dockerignore), so this
# install is what populates it.
COPY package.json yarn.lock ./
RUN yarn install

# Application source and build. The yarn layer above stays cached across source
# edits, so only the copy and the Java build below re-run on a code change.
COPY . .
RUN cd java && mvn package

COPY docker/config.local.json config.initial.local.json
COPY docker/healthcheck.js healthcheck.js
COPY docker/entry.sh entry.sh
COPY docker/first_run.sh first_run.sh

EXPOSE 7777

HEALTHCHECK --start-period=60s CMD ["node", "healthcheck.js"]
ENTRYPOINT ["./entry.sh"]
