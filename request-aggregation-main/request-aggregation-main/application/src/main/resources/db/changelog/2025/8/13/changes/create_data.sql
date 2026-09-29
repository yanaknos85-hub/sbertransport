DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_catalog = 'transport'
                  and table_schema = 'corporate'
                  and table_name = 'organization'
            ) THEN
            insert into request_aggregation.organization (id, digit_id, active, official_name)
            select co.id, co.digit_id, CASE WHEN co.status = 'ACTIVE' THEN true ELSE false END, co.official_name
            from corporate.organization co
            on conflict do nothing;
        END IF;
    END
$do$;

DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_catalog = 'transport'
                  and table_schema = 'corporate'
                  and table_name = 'position'
            ) THEN
            insert into request_aggregation.position (id, active, organization_id, position_name)
            select po.id, CASE WHEN po.status = 'ACTIVE' THEN true ELSE false END, po.organization_id, po.name
            from corporate.position po
            on conflict do nothing;
        END IF;
    END
$do$;

DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_catalog = 'transport'
                  and table_schema = 'corporate'
                  and table_name = 'department'
            ) THEN
            insert
            into request_aggregation.department (id, active, department_name, human_readable_id, organization_id, parent_id)
            with recursive entries as (
                select id,
                       parent_id,
                       name,
                       humanreadableid,
                       organization_id,
                       CASE WHEN status = 'ACTIVE' THEN true ELSE false END as active,
                       id                                                   as root_id,
                       1                                                    as level
                from corporate.department
                where parent_id is null-- this should be IS NULL
                union all
                select c.id,
                       c.parent_id,
                       c.name,
                       c.humanreadableid,
                       c.organization_id,
                       CASE WHEN status = 'ACTIVE' THEN true ELSE false END as active,
                       p.root_id,
                       p.level + 1
                from corporate.department c
                         join entries p on p.id = c.parent_id
            )
            select entries.id,
                   entries.active,
                   entries.name,
                   entries.humanreadableid,
                   entries.organization_id,
                   entries.parent_id
            from entries
            order by level, root_id, id
            on conflict do nothing;
        END IF;
    END
$do$;

DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_catalog = 'transport'
                  and table_schema = 'corporate'
                  and table_name = 'employee'
            ) THEN
            insert into request_aggregation.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone, personnel_number, user_id, department_id, position_id, organization_id)
            select co.id, CASE WHEN co.status = 'ACTIVE' THEN true ELSE false END, co.humanreadableid, co.first_name, co.last_name, co.patronymic, co.mobile_phone, co.personnel_number, co.user_id, co.department_id, co.position_id, co.organization_id
            from corporate.employee co
            where co.user_id is not null and co.personnel_number is not null
            on conflict do nothing;
        END IF;
    END
$do$;

