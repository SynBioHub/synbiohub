FROM node:13-alpine
LABEL org.opencontainers.image.authors="j.a.mclaughlin@ncl.ac.uk"

RUN apk add nss git openjdk8-jre openjdk8 maven python3 alpine-sdk libxml2-dev yarn g++ gcc bash raptor2 jq

WORKDIR /synbiohub
COPY . . 

# RUN rm -rf .git

RUN cd java && mvn package
RUN git config --global url."https://".insteadOf git://
RUN yarn install

COPY docker/config.local.json config.initial.local.json
COPY docker/healthcheck.js healthcheck.js
COPY docker/entry.sh entry.sh
COPY docker/first_run.sh first_run.sh

EXPOSE 7777

HEALTHCHECK --start-period=60s CMD ["node", "healthcheck.js"] 
ENTRYPOINT ["./entry.sh"]

