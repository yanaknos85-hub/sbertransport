CREATE TABLE contractors.contractor
(
    id      uuid PRIMARY KEY,
    name varchar(255) unique not null,
    msrn varchar(20) unique not null,
    tin varchar(20) unique not null
);