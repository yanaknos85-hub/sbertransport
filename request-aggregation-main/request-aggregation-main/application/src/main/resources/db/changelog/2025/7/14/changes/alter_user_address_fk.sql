ALTER TABLE request_aggregation.address
    ADD column user_id UUID;

ALTER TABLE request_aggregation.address
    ADD CONSTRAINT fk_user_address
        FOREIGN KEY (user_id)
            REFERENCES request_aggregation.users(id);