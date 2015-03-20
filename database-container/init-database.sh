#!/bin/bash

: ${DB_USER:=postgres}
: ${DB_PASSWORD:=db_pass}
: ${DB_NAME:=grafterizer}
: ${DB_ENCODING:=UTF-8}
: ${DB_PG_DUMP_FILE:=/tmp/init-database.sql}

{ gosu postgres postgres --single -jE <<-EOSQL
    CREATE USER "$DB_USER" WITH PASSWORD '$DB_PASSWORD';
EOSQL
} && { gosu postgres postgres --single -jE <<-EOSQL
    CREATE DATABASE "$DB_NAME" WITH OWNER="$DB_USER" TEMPLATE=template0 ENCODING='$DB_ENCODING';
EOSQL
} && { gosu postgres pg_ctl start -w && gosu postgres psql  "$DB_NAME" < "$DB_PG_DUMP_FILE" && gosu postgres pg_ctl stop -w
}