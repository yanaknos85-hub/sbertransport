ALTER TABLE telemechanic.ewb_history
ALTER COLUMN initiator_id DROP NOT NULL;

ALTER TABLE telemechanic.request_history
ALTER COLUMN initiator_id DROP NOT NULL;

ALTER TABLE telemechanic.medic_request_history
ALTER COLUMN initiator_id DROP NOT NULL;