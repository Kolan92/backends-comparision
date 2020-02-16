#!/bin/bash
set -e


psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL

  CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

  DROP DATABASE IF EXISTS testdatabase; 
  CREATE DATABASE testdatabase; 
  \c testdatabase 

  CREATE TABLE body_info (
    measuredOn  TIMESTAMPTZ   NOT NULL,
    weight_kg   NUMERIC       NOT NULL,
    height_cm   NUMERIC       NOT NULL
  );

  SELECT create_hypertable('body_info', 'time');

EOSQL

# bmi NUMERIC GENERATED ALWAYS AS (weight_kg / (height_cm / 100)) STORED
