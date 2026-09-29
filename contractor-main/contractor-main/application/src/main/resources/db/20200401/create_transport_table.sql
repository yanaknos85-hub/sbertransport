CREATE TABLE contractors.transport
(
    id            uuid PRIMARY KEY,
    brand         varchar(255)       not null,
    model         varchar(20)        not null,
    state_number  varchar(20) unique not null,
    color         varchar(20)        not null,
    contractor_id uuid
        constraint transport_contractor_fk references contractors.contractor (id)
);