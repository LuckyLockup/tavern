FROM anapsix/alpine-java:8_server-jre_unlimited
MAINTAINER nikita.panteleev@phystech.edu
EXPOSE 8080
ENTRYPOINT /docker-entrypoint.sh
ADD riichi/target/scala-2.12/riichi-assembly-0.1-SNAPSHOT.jar /riichi-assembly-0.1-SNAPSHOT.jar
ADD docker-entrypoint.sh /docker-entrypoint.sh
