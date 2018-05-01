Here be dragons...

#arhictecture

# Local build
`sbt docker:publishLocal`

# Cassandra connection:
/usr/local/Cellar/cassandra/3.11.2_1/bin/cqlsh 127.0.0.1 9042 -u cassandra -p cassandra


# CQLSH tutorial

```
DESCRIBE keyspaces;
```

# Cassandra server installation
https://www.linode.com/docs/databases/cassandra/deploy-scalable-cassandra/
```
systemctl -l status cassandra

cqlsh -u cassandra -p cassandra

describe keyspaces
drop keyspace akkasnapshot;
drop keyspace akkajournal;
```