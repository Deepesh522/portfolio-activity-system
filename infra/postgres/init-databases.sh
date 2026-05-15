#!/bin/bash
set -e

# Create separate databases for each microservice
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE portfolio_db;
    CREATE DATABASE notification_db;
EOSQL
