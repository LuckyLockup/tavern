#!/usr/bin/env bash

echo "Pulling latest changes..."
git pull
echo "Building jar..."
sbt assembly
echo "Building docker..."
docker build -t tavern:kotone .
#docker save -o tavern_kotone.tar tavern:kotone
#scp tavern_kotone.tar root@balmora:/root/tavern/tavern_kotone.tar
#docker load -i tavern/tavern_kotone.tar
echo "Killing the previous docker container and starting new one..."
docker kill $(docker ps | grep 'tavern.*' | awk '{print $1}')
docker run -d --net=host tavern:kotone