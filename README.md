# Superset SSO with Keycloak(OAuth2)

## How to run

1. Run
```sh
./run.sh

# create tables and insert data
psql -h localhost -d mydb -U test_user -p 5435 -f ./postgres/user-data.sql
```

2. Open [superset](http://localhost:8088)

3. Stop and kill all services
```sh
./kill.sh
```

## Setup

1. Create user data
   - `PGPASSWORD=test_password psql -h localhost -U test_user -d mydb -f postgres/user-data.sql`
1. Open [Keycloak](http://localhost:8080)
1. Login to Keycloak
   - username: `admin`
   - password: `change_me`
1. Import realm (upload `keycloak/superset-realm.json`)
1. Open [Superset](http://localhost:8088)
1. Login to Superset
   - username: `st_001`
   - password: `password001`

