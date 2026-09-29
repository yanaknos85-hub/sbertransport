CREATE TABLE contractors.driver
(
    id              uuid PRIMARY KEY,
    user_profile_id uuid
        constraint driver_user_profile_fk references contractors.driver_user_profile (id),
    contractor_id   uuid
        constraint driver_contractor_fk references contractors.contractor (id)
);