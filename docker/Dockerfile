FROM node:11.1.0-alpine

RUN apk add nss git openjdk8-jre openjdk8 maven python alpine-sdk libxml2-dev yarn g++ gcc bash raptor2 jq

WORKDIR /synbiohub
COPY . . 

RUN rm -rf .git

RUN cd java && mvn package
RUN git config --global url."https://".insteadOf git://
RUN yarn install

RUN mkdir /mnt/data && \
    mkdir /mnt/data/backup && \
    mkdir /mnt/data/uploads && \
    mkdir /mnt/data/logs && \
    mkdir /mnt/data/icons && \
    mkdir /mnt/config

COPY docker/config.local.json /mnt/config/config.local.json
COPY docker/healthcheck.js healthcheck.js
COPY docker/entry.sh entry.sh

RUN ln -s /mnt/config/config.local.json ./config.local.json && \
    touch /mnt/data/synbiohub.sqlite && ln -s /mnt/data/synbiohub.sqlite ./synbiohub.sqlite && \
    ln -s /mnt/data/backup . && \
    ln -s /mnt/data/uploads . && \
    ln -s /mnt/data/logs . && \
    ln -s /mnt/data/icons public/. 

EXPOSE 7777

HEALTHCHECK --start-period=60s CMD ["node", "healthcheck.js"] 
ENTRYPOINT ["./entry.sh"]

