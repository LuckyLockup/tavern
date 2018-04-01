#!/usr/bin/env bash

echo "Building tavern"
sbt assembly
docker build -t tavern:latest .
docker save -o tavern_image.tar tavern:latest
scp tavern_image.tar root@balmora:/root/tavern/tavern_image.tar
#docker load -i tavern/tavern_image.tar
#docker run -p:8080:8080 tavern:latest