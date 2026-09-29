alter table telemechanic.driving_license
 add column active boolean not null default false;

comment on column telemechanic.driving_license.active is 'Флаг активности';

DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            SELECT d.driving_license_id
            FROM telemechanic.driver d
        LOOP
            UPDATE telemechanic.driving_license dl SET active = true WHERE dl.id = rec.driving_license_id;
        END LOOP;
    END;
$$;

alter table telemechanic.driving_license
    alter column active drop default;
