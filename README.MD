Here be dragons...

#arhictecture

User actions, system actions comes to game stream which is maintained in Redis.
Actions are validated against state, transformed to events and saved in db.
If state is not found in Redis events are replayed from db to get the state.


# Local build
`sbt docker:publishLocal`

# Cassandra connection:

export DOCKERHOST=$(ifconfig | grep -E "([0-9]{1,3}\.){3}[0-9]{1,3}" | grep -v 127.0.0.1 | awk '{ print $2 }' | cut -f2 -d: | head -n1)
docker-compose up -d