
FROM node:11.1.0-alpine
MAINTAINER James Alastair McLaughlin <j.a.mclaughlin@ncl.ac.uk>

### synbiohub
RUN apk add git openjdk8-jre openjdk8 maven python alpine-sdk libxml2-dev yarn g++ gcc

### node
RUN yarn global add forever

RUN git clone https://github.com/synbiohub/synbiohub --depth 1

WORKDIR /synbiohub
    
RUN cd java && mvn package
RUN yarn install

RUN mkdir /mnt/data && \
    mkdir /mnt/data/backup && \
    mkdir /mnt/data/uploads && \
    mkdir /mnt/config

COPY config.local.json.standalone /mnt/config/config.local.json

RUN ln -s /mnt/config/config.local.json ./config.local.json && \
    touch /mnt/data/synbiohub.sqlite && ln -s /mnt/data/synibohub.sqlite ./synbiohub.sqlite && \
    rm -rf backup && ln -s /mnt/data/backup ./backup && \
    rm -rf uploads && ln -s /mnt/data/uploads ./uploads 

EXPOSE 8890 7777 1111

ENTRYPOINT ["forever", "synbiohub.js"]

