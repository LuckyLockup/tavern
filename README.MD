Here be dragons...

#arhictecture

User actions, system actions comes to game stream which is maintained in Redis.
Actions are validated against state, transformed to events and saved in db.
If state is not found in Redis events are replayed from db to get the state.


# Api


## Create game
`POST http://127.0.0.1:8080/api/v0.1/games/riichi`
with body
42

## Count games
`GET http://127.0.0.1:8080/api/v0.1/games/riichi`
