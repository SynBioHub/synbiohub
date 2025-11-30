#!/bin/bash

# make directories if needed:
mkdir -p /mnt/data
mkdir -p /mnt/data/backup
mkdir -p /mnt/data/uploads
mkdir -p /mnt/data/logs
mkdir -p /mnt/data/icons
mkdir -p /mnt/config

SBH_DIR="/synbiohub"

MNT_CONFIG="/mnt/config/config.local.json"
if [ ! -e "$MNT_CONFIG" ]; then
    echo "config missing. Copying initial config"
    cp "$SBH_DIR/config.initial.local.json" "$MNT_CONFIG"
fi

MNT_SQL="/mnt/data/synbiohub.sqlite"
if [ ! -e "$MNT_SQL" ]; then
    echo "sqlite file missing. Creating."
    touch "$MNT_SQL"
fi

# check for links:
if [ ! -e "$SBH_DIR/config.local.json" ]; then
    ln -s "$MNT_CONFIG" "$SBH_DIR/config.local.json"
fi

if [ ! -e "$SBH_DIR/synbiohub.sqlite" ]; then
    ln -s "$MNT_SQL" "$SBH_DIR/synbiohub.sqlite"
fi

if [ ! -e "$SBH_DIR/data/backup" ]; then
    ln -s "/mnt/data/backup" "$SBH_DIR/."
fi

if [ ! -e "$SBH_DIR/data/uploads" ]; then
    ln -s "/mnt/data/uploads" "$SBH_DIR/."
fi

if [ ! -e "$SBH_DIR/data/logs" ]; then
    ln -s "/mnt/data/logs" "$SBH_DIR/."
fi

if [ ! -e "$SBH_DIR/data/icons" ]; then
    ln -s "/mnt/data/icons" "$SBH_DIR/public/."
fi
