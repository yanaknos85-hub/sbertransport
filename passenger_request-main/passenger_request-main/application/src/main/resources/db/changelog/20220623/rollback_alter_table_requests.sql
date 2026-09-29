ALTER TABLE request.request_for_cargo
    drop COLUMN bonus_cost;

ALTER TABLE request.request_for_carsharing
    drop COLUMN bonus_cost;

ALTER TABLE request.request_for_import
    drop COLUMN bonus_cost;

ALTER TABLE request.request_for_personal
    drop COLUMN bonus_cost;

ALTER TABLE request.request_for_public
    drop COLUMN bonus_cost;

ALTER TABLE request.request_for_taxi
    drop COLUMN bonus_cost;

ALTER TABLE request.update_request
    drop COLUMN bonus_cost;

ALTER TABLE request_audit.request_for_public
    drop COLUMN bonus_cost;