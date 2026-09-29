ALTER TABLE request.request
    drop COLUMN time_zone;

ALTER TABLE request.request_for_carsharing
    drop COLUMN time_zone;

ALTER TABLE request.request_for_personal
    drop COLUMN time_zone;

ALTER TABLE request.request_for_public
    drop COLUMN time_zone;

ALTER TABLE request.request_for_taxi
    drop COLUMN time_zone;

ALTER TABLE request_audit.request_for_public
    drop COLUMN time_zone;
