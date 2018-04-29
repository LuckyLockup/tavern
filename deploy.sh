#!/usr/bin/env bash

echo "Building tavern"
sbt assembly
docker build -t tavern:kotone .
docker save -o tavern_kotone.tar tavern:kotone
scp tavern_kotone.tar root@balmora:/root/tavern/tavern_kotone.tar
#docker load -i tavern/tavern_kotone.tar
#docker run --net=host tavern:kotone