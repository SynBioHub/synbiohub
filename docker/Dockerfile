FROM node:11.1.0-alpine

RUN apk add nss git openjdk8-jre openjdk8 maven python alpine-sdk libxml2-dev yarn g++ gcc bash raptor2 jq curl

WORKDIR /synbiohub
COPY . . 

RUN git config --global url."https://".insteadOf git://

RUN cd java && mvn package
RUN yarn install

COPY docker/config.local.json config.initial.local.json
COPY docker/healthcheck.js healthcheck.js
COPY docker/entry.sh entry.sh
COPY docker/first_run.sh first_run.sh

EXPOSE 7777

HEALTHCHECK --interval=10s --timeout=10s --start-period=20s --retries=5 \
  CMD ["node", "healthcheck.js"]

ENTRYPOINT ["./docker/entry.sh"]
