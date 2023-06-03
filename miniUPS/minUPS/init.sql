CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    upsid VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE truck (
    truckid INTEGER PRIMARY KEY,
    pos_x INTEGER NOT NULL,
    pos_y INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL

);

CREATE TABLE package (
    packageid INTEGER PRIMARY KEY,
    details VARCHAR(255) NOT NULL,
    destinationx INTEGER NOT NULL,
    destinationy INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL,
    upsid VARCHAR(255), 
    warehouseid INTEGER NOT NULL,
    truckid INTEGER REFERENCES truck(truckid)
);
