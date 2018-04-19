Here be dragons...

#arhictecture

User actions, system actions comes to game stream which is maintained in Redis.
Actions are validated against state, transformed to events and saved in db.
If state is not found in Redis events are replayed from db to get the state.


# Local build
`sbt docker:publishLocal`

# Cassandra connection:
/usr/local/Cellar/cassandra/3.11.2_1/bin/cqlsh 127.0.0.1 9042 -u cassandra -p cassandra

# CQLSH tutorial

```
DESCRIBE keyspaces;
```
