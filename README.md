### How to run

1. Run
```sh
./run.sh

# create tables and insert data
psql -h localhost -d mydb -U test_user -p 5435 -f ./user-data.sql
```

2. Stop and kill all services
```sh
./kill.sh
```
