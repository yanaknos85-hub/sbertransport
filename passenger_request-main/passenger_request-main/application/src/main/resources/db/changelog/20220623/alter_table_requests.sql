ALTER TABLE request.request_for_cargo
    ADD COLUMN IF NOT EXISTS
        bonus_cost bigint;

ALTER TABLE request.request_for_carsharing
    ADD COLUMN IF NOT EXISTS
        bonus_cost bigint;

ALTER TABLE request.request_for_import
    ADD COLUMN IF NOT EXISTS
        bonus_cost bigint;

ALTER TABLE request.request_for_personal
    ADD COLUMN IF NOT EXISTS
        bonus_cost bigint;

ALTER TABLE request.request_for_public
    ADD COLUMN IF NOT EXISTS
        bonus_cost bigint;

ALTER TABLE request.request_for_taxi
    ADD COLUMN IF NOT EXISTS
        bonus_cost bigint;

ALTER TABLE request.update_request
    ADD COLUMN IF NOT EXISTS
        bonus_cost bigint;

ALTER TABLE request_audit.request_for_public
    ADD COLUMN IF NOT EXISTS
        bonus_cost bigint;