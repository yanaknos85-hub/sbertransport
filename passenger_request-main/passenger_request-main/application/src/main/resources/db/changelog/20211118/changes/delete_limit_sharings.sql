DO $$
BEGIN
    IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES
                     WHERE TABLE_SCHEMA = 'limits'
                     AND TABLE_NAME = 'limit_sharing'))
        THEN DELETE FROM request.dep_limit_message WHERE id IN (SELECT id FROM limits.limit_sharing);
    END IF;
END;
$$ LANGUAGE 'plpgsql';
