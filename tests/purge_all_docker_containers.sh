read -p "Are you sure you want to purge ALL docker container and volume data? (y/n) " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
    docker kill $(docker ps -aq)
    docker rm $(docker ps -aq)
    docker volume prune
fi
