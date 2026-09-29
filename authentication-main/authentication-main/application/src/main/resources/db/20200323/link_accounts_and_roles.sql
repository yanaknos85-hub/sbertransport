CREATE TABLE authentication.account_roles
(
    account_id uuid CONSTRAINT account_roles_account_kf REFERENCES authentication.account (id),
    role_code  varchar(255) CONSTRAINT account_roles_role_kf REFERENCES authentication.role (code),
    PRIMARY KEY (account_id, role_code)
);