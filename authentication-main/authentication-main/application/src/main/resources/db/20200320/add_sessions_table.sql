CREATE TABLE authentication.session
(
    id         uuid PRIMARY KEY,
    token      text      NOT NULL UNIQUE,
    expire_in  timestamp NOT NULL,
    created_at timestamp NOT NULL,
    account_id uuid CONSTRAINT session_account_fk REFERENCES authentication.account (id)
);