DO
$do$
    BEGIN
        UPDATE telemechanic.check_photo SET file_status = 'UPLOADED' WHERE file_status is null;
    END
$do$;