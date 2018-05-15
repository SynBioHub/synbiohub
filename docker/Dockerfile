
FROM ubuntu:16.04
MAINTAINER James Alastair McLaughlin <j.a.mclaughlin@ncl.ac.uk>


### virtuoso

ADD http://packages.comsode.eu/key/odn.gpg.key /tmp/odn.gpg.key

RUN echo 'deb http://packages.comsode.eu/debian jessie main' >> /etc/apt/sources.list && \
    apt-key add /tmp/odn.gpg.key && \
    apt update && \
    apt install -y virtuoso-opensource && \
    rm -f /etc/virtuoso-opensource-7/virtuoso.ini && \
    rm -f /etc/default/virtuoso-opensource-7

ADD docker/etc/default/virtuoso-opensource-7 /etc/default/virtuoso-opensource-7

### node

ADD docker/setup_6.x /

RUN chmod +x /setup_6.x && \
    sync && \
    /setup_6.x && \
    apt-get update && \
    apt-get install -y nodejs && \
    npm install -g forever


### synbiohub

COPY docker/sudoers /etc/sudoers

RUN useradd synbiohub -p synbiohub -m -s /bin/bash && \
    apt install -y git && \
    cd /opt && \
    git clone https://github.com/ICO2S/synbiohub.git --depth 1 && \
    cd /opt/synbiohub && \
    git checkout && \
    rm -f config.local.json && \
    rm -rf backup && \
    chown -R synbiohub:synbiohub /opt/synbiohub
    
RUN apt install -y default-jdk maven
RUN su synbiohub -c "cd /opt/synbiohub/java && mvn package"

# to build libxmljs
RUN apt install -y python && \
    apt install -y build-essential && \
    cd /opt/synbiohub && \
    su synbiohub -c "npm install" && \
    su synbiohub -c "npm install"

RUN apt install -y raptor2-utils

RUN mkdir /mnt/data && \
    mkdir /mnt/config && \
    mkdir /mnt/data/backup

COPY docker/config.local.json /mnt/config/
COPY docker/virtuoso.ini /mnt/config/

RUN ln -s /mnt/config/virtuoso.ini /etc/virtuoso-opensource-7/virtuoso.ini && \
    ln -s /mnt/config/config.local.json /opt/synbiohub/config.local.json && \
    ln -s /mnt/data/backup /opt/synbiohub/backup && \
    touch /mnt/data/synbiohub.sqlite && \
    ln -s /mnt/config/synbiohub.sqlite /opt/synbiohub/synbiohub.sqlite

RUN chown -R synbiohub:synbiohub /mnt

COPY docker/startup.sh /
RUN chmod +x /startup.sh

EXPOSE 8890 7777 1111

ENTRYPOINT ["/startup.sh"]


