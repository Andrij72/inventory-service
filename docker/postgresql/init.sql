CREATE DATABASE IF NOT EXISTS inventory_service;


CREATE ROLE inventory_client WITH LOGIN PASSWORD 'client_pass';

GRANT CONNECT ON DATABASE inventory_service TO inventory_client;


\c inventory_service


GRANT USAGE ON SCHEMA public TO inventory_client;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO inventory_client;


ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO inventory_client;