DO
$do$
    BEGIN
        UPDATE telemechanic."check" SET status = 'DECLINE' WHERE check_type = 'VEHICLE_NUMBER' AND attempt >= 10;
    END
$do$;
