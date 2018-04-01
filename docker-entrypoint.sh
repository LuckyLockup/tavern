#!/bin/bash
echo "Calling the entry-point"
APP_NAME=tavern

echo "Starting the JVM process"
command="java -Xmx2048m -DAPP_NAME=$APP_NAME -jar riichi-assembly-0.1-SNAPSHOT.jar"
echo "going to exec $command"
exec $command
