alter table request.request_for_carsharing_history
    drop column initiator_description;

alter table request.request_for_personal_history
    drop column initiator_description;

alter table request.request_for_public_history
    drop column initiator_description;

alter table request.request_for_taxi_history
    drop column initiator_description;