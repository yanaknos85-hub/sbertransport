alter table telemechanic.employee
    add organization_id uuid;

comment on column telemechanic.employee.organization_id is 'Идентификатор записи об организации';

create index employee_organization_id_index
    on telemechanic.employee (organization_id);

alter table telemechanic.employee
    add constraint employee_organization_id_fk
        foreign key (organization_id) references telemechanic.organization;

DO
$do$
    BEGIN
        IF EXISTS(
                SELECT routine_schema,
                       routine_name,
                       routine_type
                FROM information_schema.routines
                WHERE routine_name = 'fill_roles'
                  and routine_schema = 'migrations'
                  and routine_type = 'PROCEDURE'
            ) THEN
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/on-the-line/'', ''ROLE_DRIVER'', true)';
        END IF;
    END
$do$;

delete from telemechanic.check_photo;
delete from telemechanic."check";
delete from telemechanic.request_check_list;
delete from telemechanic.vehicle;