alter table request_aggregation.address
    drop constraint fk_user_address;

alter table request_aggregation.address
    rename column user_id to employee_id;