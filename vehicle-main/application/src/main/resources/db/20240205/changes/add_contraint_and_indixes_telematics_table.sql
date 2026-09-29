CREATE INDEX telematics_imei_idx
    ON vehicle.telematics (imei);

CREATE INDEX telematics_title_upper_idx
    ON vehicle.telematics (UPPER(title));