ALTER TABLE request.taxi_trip
    ADD COLUMN IF NOT EXISTS last_xml_received_date_time timestamp;