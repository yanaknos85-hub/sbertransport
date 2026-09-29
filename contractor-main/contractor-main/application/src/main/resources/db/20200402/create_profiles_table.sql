CREATE TABLE contractors.driver_user_profile
(
    id         uuid PRIMARY KEY,
    last_name  varchar(255)        not null,
    first_name varchar(255)        not null,
    patronymic varchar(255) unique not null
);