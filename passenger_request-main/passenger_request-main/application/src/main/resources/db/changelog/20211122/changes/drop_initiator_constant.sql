alter table request.request_for_taxi
    drop constraint if exists fk_dispatcher_employee;
alter table request.request_for_taxi_history
    drop constraint if exists fk_taxi_history_employee;
